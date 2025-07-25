package org.example.relief.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "images")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer imageId;
    @Column(nullable = false, unique = true)
    private String imagePath;
    private String imageType;
    private String imagePublicId;

    @OneToOne(mappedBy = "organizationImage")
    private User organization;

    @ManyToOne()
    private Incident incident;
}
