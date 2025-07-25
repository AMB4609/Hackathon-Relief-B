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

import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;

@Service
@RequiredArgsConstructor
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;

    /** Creates an incident, stores it, pushes FCM to all users with a token */
    public Incident reportIncident(IncidentRequest req) {

        // 1️⃣ find uploader -----------------------------------------------------
        User uploader = userRepository.findById(req.getUploaderId())
                .orElse(null);

        // 2️⃣ build GeoLatte Point<G2D>  (X = lon, Y = lat) --------------------
        Point<G2D> point = DSL.point(
                WGS84,
                new G2D(req.getLongitude(), req.getLatitude())
        );

        // 3️⃣ build + persist Incident -----------------------------------------
        Incident incident = Incident.builder()
                .title(req.getTitle())
                .location(point)                     // raw Point is accepted
                .urgencyLevel(req.getUrgencyLevel())
                .description(req.getDescription())
                .organizationType(req.getOrganizationType())
                .incidentDate(LocalDateTime.now())
                .listedDate(LocalDateTime.now())
                .uploader(uploader)
                .build();

        incident = incidentRepository.save(incident);

        // 4️⃣ FCM broadcast (very simple – refine later) -----------------------
        Incident finalIncident = incident;
        userRepository.findAll().stream()
                .filter(u -> u.getFcmToken() != null && !u.getFcmToken().isBlank())
                .forEach(u -> pushToToken(u.getFcmToken(), finalIncident));

        return incident;
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
