package org.example.relief.controller;


import lombok.RequiredArgsConstructor;
import org.example.relief.request.IncidentRequest;
import org.example.relief.model.Incident;
import org.example.relief.response.ApiResponse;
import org.example.relief.service.IncidentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/incident")
public class IncidentController {
    public final IncidentService incidentService;

    @PostMapping("create")
    public ApiResponse createIncident(IncidentRequest incident) {
        Incident savedIncident = incidentService.reportIncident(incident);
        return ApiResponse.success(200,"incident sucessfully reported",savedIncident);
    }
    @PostMapping("/sendFcmToken")
    public ResponseEntity<Incident> testFcm(
            @RequestParam("token") String token,
            @RequestBody IncidentRequest req) {
        Incident saved = incidentService.reportIncident(req);
        incidentService.sendToSingleToken(token, saved);

        return ResponseEntity.ok(saved);
    }
}
