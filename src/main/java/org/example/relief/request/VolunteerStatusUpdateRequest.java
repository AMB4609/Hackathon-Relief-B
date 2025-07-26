    package org.example.relief.request;

    import lombok.AllArgsConstructor;
    import lombok.Builder;
    import lombok.Data;
    import lombok.NoArgsConstructor;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public class VolunteerStatusUpdateRequest {
        private boolean status;
        private double longitude;
        private double latitude;
        private Long userId; // Link to User
        private Long incidentId; // Link to Incident
    }
