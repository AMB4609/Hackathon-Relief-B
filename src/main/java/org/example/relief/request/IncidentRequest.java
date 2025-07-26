    package org.example.relief.request;

    import lombok.AllArgsConstructor;
    import lombok.Builder;
    import lombok.Data;
    import lombok.NoArgsConstructor;

    import java.time.LocalDateTime;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public class IncidentRequest {
        private String title;
        private double latitude;
        private double longitude;
        private String urgencyLevel;
        private String description;
        private String organizationType;
        private LocalDateTime incidentDate;
        //images as another request part
        private Long uploaderId; // Link to User
    }
