package org.example.relief.service;


import org.example.relief.request.IncidentFilterRequest;
import org.example.relief.request.IncidentRequest;
import org.example.relief.model.Incident;
import org.example.relief.response.IncidentResponse;
import org.springframework.data.domain.Page;

public interface IncidentService {

    Incident reportIncident(IncidentRequest req) throws Exception;
    void sendToSingleToken(String fcmToken, Incident incident);

    IncidentResponse getIncidentDetails(Long incidentId) throws Exception;
    Page<IncidentResponse> filterIncidents(IncidentFilterRequest request);

}