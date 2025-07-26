package org.example.relief.response;

import lombok.Builder;
import lombok.Data;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
public class IncidentResponse {
    private Long incidentId;
    private String title;
    private double longitude;
    private double latitude;
    private String urgencyLevel;
    private String description;
    private String organizationType;
    private LocalDateTime incidentDate;
    private LocalDateTime listedDate;
    private int flagCount;
    private List<ImageResponse> images;
    private UserNameResponse uploader;
}
