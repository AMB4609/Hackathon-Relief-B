package org.example.relief.service.impl;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import org.example.relief.request.IncidentRequest;
import org.example.relief.model.Incident;
import org.example.relief.model.User;
import org.example.relief.repository.IncidentRepository;
import org.example.relief.repository.UserRepository;
import org.example.relief.service.IncidentService;
import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;
import org.geolatte.geom.builder.DSL;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;

@Service
@RequiredArgsConstructor
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;

    /** Creates an incident, stores it, pushes FCM to all users with a token */
    public Incident reportIncident(IncidentRequest req) {
        // 1️⃣ find uploader
        User uploader = userRepository.findById(req.getUploaderId())
                .orElse(null);

        // 2️⃣ build GeoLatte Point<G2D> (X = lon, Y = lat)
        Point<G2D> point = DSL.point(
                WGS84,
                new G2D(req.getLongitude(), req.getLatitude())
        );

        // 3️⃣ build + persist Incident
        Incident incident = Incident.builder()
                .title(req.getTitle())
                .location(point)
                .urgencyLevel(req.getUrgencyLevel())
                .description(req.getDescription())
                .organizationType(req.getOrganizationType())
                .incidentDate(LocalDateTime.now())
                .listedDate(LocalDateTime.now())
                .uploader(uploader)
                .build();

        incident = incidentRepository.save(incident);

        // 4️⃣ Get all volunteers and filter by distance (within 4km)
        List<User> nearbyVolunteers = userRepository.findAll().stream()
                .filter(User::isVolunteer)
                .filter(user -> {
                    if (user.getAvailableLocation() == null) return false;
                    Point<G2D> loc = user.getAvailableLocation();
                    return isWithinRadius(loc, point, 4_000); // 4km in meters
                })
                .collect(Collectors.toList());

        // 5️⃣ Get all organizations
        List<User> organizations = userRepository.findAll().stream()
                .filter(user -> user.getRoles() != null &&
                        user.getRoles().stream()
                                .anyMatch(role -> role.getName().equalsIgnoreCase("organization")))
                .collect(Collectors.toList());

        // 6️⃣ Send FCM to both groups
        Incident finalIncident1 = incident;
        nearbyVolunteers.forEach(user -> {
            if (user.getFcmToken() != null) {
                pushToToken(user.getFcmToken(), finalIncident1);
            }
        });

        Incident finalIncident = incident;
        organizations.forEach(user -> {
            if (user.getFcmToken() != null) {
                pushToToken(user.getFcmToken(), finalIncident);
            }
        });

        return incident;
    }

    private boolean isWithinRadius(Point<G2D> a, Point<G2D> b, double radiusMeters) {
        double lat1 = a.getPosition().getLat();
        double lon1 = a.getPosition().getLon();
        double lat2 = b.getPosition().getLat();
        double lon2 = b.getPosition().getLon();

        double R = 6371000; // Earth radius in meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double rLat1 = Math.toRadians(lat1);
        double rLat2 = Math.toRadians(lat2);

        double aH = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(rLat1) * Math.cos(rLat2) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(aH), Math.sqrt(1 - aH));

        double distance = R * c;

        return distance <= radiusMeters;
    }

    private void pushToToken(String fcmToken, Incident incident) {

        Notification notif = Notification.builder()
                .setTitle("🚨 Emergency: " + incident.getTitle())
                .setBody(incident.getDescription())
                .build();

        /*
         * Incident.location is raw Point, so getPosition() returns raw Position.
         * Cast it once to G2D to access getX()/getY() (lon/lat).
         */

        Message msg = Message.builder()
                .setToken(fcmToken)
                .setNotification(notif)
                .putData("urgency", incident.getUrgencyLevel().name())
                .putData("description", incident.getDescription())
                .build();

        try {
            FirebaseMessaging.getInstance().send(msg);
        } catch (FirebaseMessagingException e) {
            System.err.println("⚠️  FCM send failed for token " + fcmToken + ": " + e.getMessage());
        }
    }
    /* inside IncidentServiceImpl */

    public void sendToSingleToken(String fcmToken, Incident incident) {
        // reuse the same private method you already have
        pushToToken(fcmToken, incident);
    }

}
