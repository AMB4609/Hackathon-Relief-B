    package org.example.relief.request;

    import lombok.Data;
    import org.example.relief.enums.OrganizationType;
    import org.example.relief.enums.UrgencyLevel;

    @Data
    public class IncidentRequest {
        private String title;
        private double latitude;
        private double longitude;
        private UrgencyLevel urgencyLevel;
        private String description;
        private OrganizationType organizationType;
        private Long uploaderId; // Link to User
    }
