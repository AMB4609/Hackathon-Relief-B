package org.example.relief.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.example.relief.model.Incident;
import org.example.relief.request.IncidentFilterRequest;
import org.example.relief.request.IncidentRequest;
import org.example.relief.request.LocationUpdateRequest;
import org.example.relief.response.ApiResponse;
import org.example.relief.response.IncidentResponse;
import org.example.relief.service.IncidentService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/incident")
public class IncidentController {
    public final IncidentService incidentService;

    @PostMapping("/create")
    public ApiResponse createIncident(@RequestPart("incident") String incidentString,
                                      @RequestPart(value = "images") List<MultipartFile> images) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        IncidentRequest incident = objectMapper.readValue(
                incidentString, IncidentRequest.class);

        IncidentResponse savedIncident = incidentService.reportIncident(incident, images);
        return ApiResponse.success(200,"incident successfully reported", savedIncident);
    }


    @PutMapping("/updateLocation")
    public void updateUserLocation(@RequestBody LocationUpdateRequest request) throws Exception {
        incidentService.updateUserLocationAfterIncident(request);
    }

    @GetMapping("/{id}")
    public ApiResponse getIncidentDetails(@PathVariable Long id) throws Exception {
        IncidentResponse response = incidentService.getIncidentDetails(id);
        return ApiResponse.success(200, "Incident fetched successfully", response);
    }

    @PostMapping("/filter")
    public ApiResponse filterIncidents(@RequestBody IncidentFilterRequest request) {
        Page<IncidentResponse> result = incidentService.filterIncidents(request);
        return ApiResponse.success(200, "Filtered incidents retrieved", result);
    }

//    @PostMapping("/sendFcmToken")
//    public ResponseEntity<Incident> testFcm(
//            @RequestParam("token") String token,
//            @RequestBody IncidentRequest req) throws Exception {
//        Incident saved = incidentService.reportIncident(req);
//        incidentService.sendToSingleToken(token, saved);
//
//        return ResponseEntity.ok(saved);
//    }


}
