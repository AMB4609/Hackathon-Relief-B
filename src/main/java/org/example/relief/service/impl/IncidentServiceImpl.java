package org.example.relief.service.impl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.example.relief.enums.OrganizationType;
import org.example.relief.enums.UrgencyLevel;
import org.example.relief.model.Image;
import org.example.relief.model.Incident;
import org.example.relief.model.User;
import org.example.relief.repository.ImageRepository;
import org.example.relief.repository.IncidentRepository;
import org.example.relief.repository.UserRepository;
import org.example.relief.request.IncidentFilterRequest;
import org.example.relief.request.IncidentRequest;
import org.example.relief.request.LocationUpdateRequest;
import org.example.relief.response.ImageResponse;
import org.example.relief.response.IncidentResponse;
import org.example.relief.response.UserNameResponse;
import org.example.relief.service.CloudinaryService;
import org.example.relief.service.IncidentService;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;
    private final Executor asyncExecutor;
    private final ImageRepository imageRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    public IncidentResponse reportIncident(IncidentRequest req, List<MultipartFile> images) throws Exception {

        Incident savedIncident = saveIncident(req, images);

        asyncExecutor.execute(() -> processNotifications(savedIncident));

        return mapToIncidentResponse(savedIncident);
    }

    public void processNotifications(Incident incident) {

        //updating volunteers locations
        //get volunteer tokens -> create and send location request to it
        List<String> volunteerTokens = userRepository.findAllByVolunteerTrue()
                .stream()
                .map(User::getFcmToken)
                .filter(t -> t != null && !t.isBlank())
                .toList();
        sendLocationRequestToTokens(volunteerTokens, incident.getIncidentId());
        //volunteers' location will be updated from the updateLocation method hit from the front end

        //sending incident noti to orgs
        //relevant organizations - get their fcm token directly -> create and send noti to it
        userRepository.findUsersByRole("ORGANIZATION")
                .stream()
                .map(User::getFcmToken)
                .filter(t -> t != null && !t.isBlank())
                .forEach(token -> pushVisibleIncidentToToken(token, incident));

        //sending incident noti to volunteers within 4km after getting the updated location
        //nearby volunteers
        userRepository.findUsersWithinDistance(incident.getLocation(), 4000D) //distance is in meters
                .stream()
                .map(User::getFcmToken)
                .filter(t -> t != null && !t.isBlank())
                .forEach(token -> pushVisibleIncidentToToken(token, incident));
    }

    private void pushVisibleIncidentToToken(String fcmToken, Incident incident) {

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

    private void sendLocationRequestToTokens(List<String> tokens, Long incidentId) {

        final int BATCH_SIZE = 500;
        for (int i = 0; i < tokens.size(); i += BATCH_SIZE) {
            List<Message> batch = tokens.subList(i, Math.min(i + BATCH_SIZE, tokens.size()))
                    .stream()
                    .map(t ->
                            Message.builder()
                                    .setToken(t)
                                    .putData("type", "REQUEST_LOCATION")
                                    .putData("incidentId", incidentId.toString())
                                    .putData("timestamp", String.valueOf(System.currentTimeMillis()))
                                    .build())
                    .toList();

            try {
                FirebaseMessaging.getInstance().sendAll(batch);
            } catch (FirebaseMessagingException e) {
                System.err.println("⚠️  Silent FCM batch failed: " + e.getMessage());
            }
        }
    }

//    @Override
//    public void sendToSingleToken(String fcmToken, Incident incident) {
//        pushVisibleIncidentToToken(fcmToken, incident);
//    }

    @Override
    public void updateUserLocationAfterIncident(LocationUpdateRequest request) throws Exception {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(()-> new Exception("User with given id not found."));
        Incident incident = incidentRepository.findById(request.getIncidentId())
                .orElseThrow(() -> new Exception("Incident with given id not found."));
        Point location = new GeometryFactory()
                .createPoint(new Coordinate(request.getLongitude(), request.getLatitude()));
        location.setSRID(4326);

        //Saving new location to repo
        userRepository.updateUserLocation(request.getUserId(), location, LocalDateTime.now());
        //Checking if user is within 4km of the incident
        boolean nearby = incidentRepository.isWithinDistance(
                request.getIncidentId(), location, 4000);
        //if yes, sending noti to that user
        if (nearby && user.getFcmToken() != null) {
            pushVisibleIncidentToToken(user.getFcmToken(), incident);
        }
    }

    private Incident saveIncident(IncidentRequest req,
                                  List<MultipartFile> images) throws Exception {
        User uploader = userRepository.findById(req.getUploaderId())
                .orElseThrow(()-> new Exception("User with given id not found."));
        Point location = new GeometryFactory()
                .createPoint(new Coordinate(req.getLongitude(), req.getLatitude()));
        location.setSRID(4326);

        Incident savedIncident = incidentRepository.save(Incident.builder()
                .title(req.getTitle())
                .location(location)
                .urgencyLevel(convertToUrgencyLevelEnum(req.getUrgencyLevel()))
                .description(req.getDescription())
                .organizationType(convertToOrganizationTypeEnum(req.getOrganizationType()))
                .incidentDate(req.getIncidentDate())
                .listedDate(LocalDateTime.now())
                .uploader(uploader)
                .build());

        if (images != null && !images.isEmpty()) {
            List<Image> incidentImages = new ArrayList<>();

            for (MultipartFile image : images) {
                if (image != null && !image.isEmpty()) {
                    // Upload image to cloudinary
                    Map uploadResult = cloudinaryService.uploadImage(image, "incidents");

                    incidentImages.add(imageRepository.save(Image.builder()
                            .imagePath(uploadResult.get("secure_url").toString())
                            .imageType(uploadResult.get("format").toString())
                            .imagePublicId(uploadResult.get("public_id").toString())
                            .incident(savedIncident)
                            .build()));
                }
            }
            savedIncident.setImages(incidentImages);
        }
        return incidentRepository.save(savedIncident);
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

        if (request.getUrgencyLevel() != null && !request.getUrgencyLevel().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("urgencyLevel"), UrgencyLevel.valueOf(request.getUrgencyLevel().toUpperCase())));
        }

        if (request.getOrganizationType() != null && !request.getOrganizationType().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("organizationType"), OrganizationType.valueOf(request.getOrganizationType().toUpperCase())));
        }

        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("title")), "%" + request.getKeyword().toLowerCase() + "%"));
        }

        if (request.getDateFilter() != null && !request.getDateFilter().isEmpty()) {
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

        Point point = incident.getLocation();

        return IncidentResponse.builder()
                .incidentId(incident.getIncidentId())
                .title(incident.getTitle())
                .latitude(point.getY()) // latitude
                .longitude(point.getX()) // longitude
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
