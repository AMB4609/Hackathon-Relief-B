package org.example.relief.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.relief.enums.OrganizationType;
import org.locationtech.jts.geom.Point;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User implements UserDetails {

    //Common fields
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    @Column(nullable = false)
    private String username;
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
    private boolean canPost = true;

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

    private String fcmToken;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .toList();
    }
}

