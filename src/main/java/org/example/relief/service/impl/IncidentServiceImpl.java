package org.example.relief.service.impl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.example.relief.enums.OrganizationType;
import org.example.relief.enums.UrgencyLevel;
import org.example.relief.model.Incident;
import org.example.relief.model.User;
import org.example.relief.repository.IncidentRepository;
import org.example.relief.repository.UserRepository;
import org.example.relief.request.IncidentFilterRequest;
import org.example.relief.request.IncidentRequest;
import org.example.relief.response.ImageResponse;
import org.example.relief.response.IncidentResponse;
import org.example.relief.response.UserNameResponse;
import org.example.relief.service.IncidentService;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;

    /** Creates an incident, stores it, pushes FCM to all users with a token */
    @Override
    public Incident reportIncident(IncidentRequest req) throws Exception {

        Incident savedIncident = saveIncident(req);

        //Finding nearby volunteers and relevant organizations.
        List<User> nearbyVolunteers = userRepository.
                findUsersWithinDistance(savedIncident.getLocation(), 4000D); //distance is in meters

        List<User> organizations = userRepository.findAll().stream()
                .filter(user -> user.getRoles() != null &&
                        user.getRoles().stream()
                                .anyMatch(role -> role.getName().equalsIgnoreCase("organization")))
                .collect(Collectors.toList());

        //Send FCM to both groups
        nearbyVolunteers.forEach(user -> {
            if (user.getFcmToken() != null) {
                pushToToken(user.getFcmToken(), savedIncident);
            }
        });

        organizations.forEach(user -> {
            if (user.getFcmToken() != null) {
                pushToToken(user.getFcmToken(), savedIncident);
            }
        });

        return savedIncident;
    }

    private void pushToToken(String fcmToken, Incident incident) {

        Notification notif = Notification.builder()
                .setTitle("🚨 Emergency: " + incident.getTitle())
                .setBody(incident.getDescription())
                .build();

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

    public void sendToSingleToken(String fcmToken, Incident incident) {
        pushToToken(fcmToken, incident);
    }

    private Incident saveIncident(IncidentRequest req) throws Exception {
        User uploader = userRepository.findById(req.getUploaderId())
                .orElseThrow(()-> new Exception("User with given id not found."));
        Point location = new GeometryFactory()
                .createPoint(new Coordinate(req.getLongitude(), req.getLatitude()));

        return incidentRepository.save(Incident.builder()
                .title(req.getTitle())
                .location(location)
                .urgencyLevel(convertToUrgencyLevelEnum(req.getUrgencyLevel()))
                .description(req.getDescription())
                .organizationType(convertToOrganizationTypeEnum(req.getOrganizationType()))
                .incidentDate(req.getIncidentDate())
                .listedDate(LocalDateTime.now())
                .uploader(uploader)
                .build()) ;
    }

    @Override
    public IncidentResponse getIncidentDetails(Long incidentId) throws Exception {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new Exception("Incident with given id not found."));

        return mapToIncidentResponse(incident);
    }

    @Override
    public Page<IncidentResponse> filterIncidents(IncidentFilterRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by("incidentDate").descending());

        Specification<Incident> spec = (root, query, cb) -> cb.conjunction();

        if (request.getUrgencyLevel() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("urgencyLevel"), UrgencyLevel.valueOf(request.getUrgencyLevel().toUpperCase())));
        }

        if (request.getOrganizationType() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("organizationType"), OrganizationType.valueOf(request.getOrganizationType().toUpperCase())));
        }

        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("title")), "%" + request.getKeyword().toLowerCase() + "%"));
        }

        if (request.getDateFilter() != null) {
            LocalDateTime start = switch (request.getDateFilter().toUpperCase()) {
                case "TODAY" -> LocalDate.now().atStartOfDay();
                case "THIS_WEEK" -> LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
                case "THIS_MONTH" -> LocalDate.now().withDayOfMonth(1).atStartOfDay();
                default -> null;
            };
            if (start != null) {
                spec = spec.and((root, query, cb) ->
                        cb.greaterThanOrEqualTo(root.get("incidentDate"), start));
            }
        }

        if (request.getLatitude() != null && request.getLongitude() != null && request.getRadiusInKm() != null) {
            GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
            Point center = geometryFactory.createPoint(new Coordinate(request.getLongitude(), request.getLatitude()));
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(
                            cb.function("ST_DistanceSphere", Double.class, root.get("location"), cb.literal(center)),
                            request.getRadiusInKm() * 1000
                    ));
        }

        return incidentRepository.findAll(spec, pageable).map(this::mapToIncidentResponse);

    }

    private IncidentResponse mapToIncidentResponse(Incident incident) {
        List<ImageResponse> imageResponses = incident.getImages().stream()
                .map(img -> ImageResponse.builder()
                        .imagePath(img.getImagePath())
                        .imageType(img.getImageType())
                        .build())
                .toList();

        User uploader = incident.getUploader();
        UserNameResponse uploaderResponse = null;
        if (uploader != null) {
            uploaderResponse = UserNameResponse.builder()
                    .userId(uploader.getUserId())
                    .firstName(uploader.getFirstName())
                    .lastName(uploader.getLastName())
                    .build();
        }

        return IncidentResponse.builder()
                .incidentId(incident.getIncidentId())
                .title(incident.getTitle())
                .location(incident.getLocation())
                .urgencyLevel(incident.getUrgencyLevel().name())
                .description(incident.getDescription())
                .organizationType(incident.getOrganizationType() != null ? incident.getOrganizationType().name() : null)
                .incidentDate(incident.getIncidentDate())
                .listedDate(incident.getListedDate())
                .images(imageResponses)
                .uploader(uploaderResponse)
                .build();
    }

    private OrganizationType convertToOrganizationTypeEnum(String type) throws Exception {
        if(type.equalsIgnoreCase("police")) return OrganizationType.POLICE;
        if(type.equalsIgnoreCase("fire")) return OrganizationType.FIRE;
        if(type.equalsIgnoreCase("ambulance")) return OrganizationType.AMBULANCE;
        if(type.equalsIgnoreCase("vet")) return OrganizationType.VET;
        throw new Exception("Organization Type Not Found");
    }

    private UrgencyLevel convertToUrgencyLevelEnum(String level) throws Exception {
        if(level.equalsIgnoreCase("high")) return UrgencyLevel.HIGH;
        if(level.equalsIgnoreCase("medium")) return UrgencyLevel.MEDIUM;
        if(level.equalsIgnoreCase("low")) return UrgencyLevel.LOW;
        throw new Exception("Urgency Level Not Found");
    }


}
