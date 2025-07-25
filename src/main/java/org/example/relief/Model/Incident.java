package org.example.relief.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.relief.Enums.OrganizationType;
import org.example.relief.Enums.UrgencyLevel;
import org.geolatte.geom.Point;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long incidentId;
    private String title;
    private Point location;
    @Enumerated(EnumType.STRING)
    private UrgencyLevel urgencyLevel;
    private String description;
    @Enumerated(EnumType.STRING)
    private OrganizationType organizationType;
    private LocalDateTime incidentDate;
    private LocalDateTime listedDate;

    @OneToMany(mappedBy = "incident")
    private List<Image> images;

    @ManyToOne()
    private User uploader;
}
