    package org.example.relief.Model;

    import jakarta.persistence.*;
    import lombok.AllArgsConstructor;
    import lombok.Builder;
    import lombok.Data;
    import lombok.NoArgsConstructor;
    import org.example.relief.Enums.OrganizationType;
    import org.springframework.data.geo.Point;

    import javax.annotation.Nullable;
    import java.time.LocalDateTime;
    import java.util.List;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public class User {

        //Common fields
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long userId;
        @Column(nullable = false)
        private String email;
        @Column(nullable = false)
        private String password;
        @Column(nullable = false)
        private String contact;
        @Column(nullable = false)
        private String address;

        //Person Specific
        private String firstName;
        private String lastName;

        //Organization Specific
        private String name;
        @Enumerated(EnumType.STRING)
        private OrganizationType organizationType;

        //Volunteer specific
        @Column(nullable = false)
        private boolean isVolunteer = false;
        @Column(columnDefinition = "geometry(Point, 4326)")
        private Point availableLocation;
        private LocalDateTime locationUpdatedAt;

        //Relationship Attributes
        @ManyToMany()
        private List<Role> roles;

        @OneToOne()
        private Image organizationImage;

        @OneToMany(mappedBy = "uploader")
        private List<Incident> uploadedIncidents;
    }
