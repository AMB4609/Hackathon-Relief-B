package org.example.relief.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.relief.enums.OrganizationType;
import org.example.relief.enums.UrgencyLevel;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "incidents")
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long incidentId;

    private String title;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point location;

    @Enumerated(EnumType.STRING)
    private UrgencyLevel urgencyLevel;

    private String description;

    @Enumerated(EnumType.STRING)
    private OrganizationType organizationType;

    private LocalDateTime incidentDate;

    private LocalDateTime listedDate;

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images;

    @ManyToOne(optional = true)
    private User uploader;

    @OneToOne(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
    private Donation donation;

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Flag> flags;
}
