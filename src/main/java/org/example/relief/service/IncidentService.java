package org.example.relief.service;


import org.example.relief.request.IncidentRequest;
import org.example.relief.model.Incident;

public interface IncidentService {
    Incident reportIncident(IncidentRequest req);
    void sendToSingleToken(String fcmToken, Incident incident);
}
