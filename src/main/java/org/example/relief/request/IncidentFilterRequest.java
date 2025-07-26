package org.example.relief.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IncidentFilterRequest {
    private String urgencyLevel;
    private String organizationType;
    private String dateFilter;
    private String keyword;
    private Double latitude;
    private Double longitude;
    private Double radiusInKm;
    private int page = 0;
    private int size = 10;
}
