package org.example.relief.service;


import org.example.relief.request.IncidentFilterRequest;
import org.example.relief.request.IncidentRequest;
import org.example.relief.model.Incident;
import org.example.relief.request.LocationUpdateRequest;
import org.example.relief.response.IncidentResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IncidentService {

    IncidentResponse reportIncident(IncidentRequest req, List<MultipartFile> images) throws Exception;

//    void sendToSingleToken(String fcmToken, Incident incident);

    void flagIncident(Long userId, Long incidentId) throws Exception;

    void updateUserLocationAfterIncident(LocationUpdateRequest request) throws Exception;

    IncidentResponse getIncidentDetails(Long incidentId) throws Exception;
    Page<IncidentResponse> filterIncidents(IncidentFilterRequest request);

}