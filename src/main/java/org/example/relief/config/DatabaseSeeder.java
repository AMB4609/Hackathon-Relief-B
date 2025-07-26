package org.example.relief.config;

import lombok.RequiredArgsConstructor;
import org.example.relief.enums.OrganizationType;
import org.example.relief.enums.UrgencyLevel;
import org.example.relief.model.Image;
import org.example.relief.model.Incident;
import org.example.relief.model.Role;
import org.example.relief.model.User;
import org.example.relief.repository.ImageRepository;
import org.example.relief.repository.IncidentRepository;
import org.example.relief.repository.RoleRepository;
import org.example.relief.repository.UserRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageRepository imageRepository;
    private final IncidentRepository incidentRepository;

    @Override
    public void run(String... args) {
        seedRoles();
        seedUsers();
        seedIncidents();
    }

    private void seedRoles() {
        // Check if roles already exist
        if (roleRepository.count() == 0) {
            List<String> roleNames = Arrays.asList("USER", "ADMIN", "STAFF");

            for (String roleName : roleNames) {
                Role role = Role.builder()
                        .name(roleName)
                        .users(new ArrayList<>())
                        .build();

                roleRepository.save(role);
            }

            System.out.println("Roles have been seeded successfully");
        } else {
            System.out.println("Roles are already seeded");
        }
    }

    private void seedUsers() {
        Optional<User> existingAdmin = userRepository.findUserByUsername("admin");
        Optional<User> existingUser = userRepository.findUserByUsername("user1");
        Optional<User> existingOrg = userRepository.findUserByUsername("org1");

        if (existingAdmin.isEmpty()) {
            // Get all roles for admin
            List<Role> allRoles = roleRepository.findAll();

            // Create admin user
            User admin = User.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin@123"))
                    .contact("1234567890")
                    .address("Kathmandu, Nepal")
                    .firstName("Admin")
                    .lastName("Istrator")
                    .roles(allRoles)
                    .build();

            userRepository.save(admin);

            System.out.println("Admin user has been seeded successfully");
        } else {
            System.out.println("Admin user already exists");
        }

        if (existingUser.isEmpty()) {
            // Get all roles for admin
            Role role = roleRepository.findRoleByName("USER").get();
            List<Role> roles = new ArrayList<>();
            roles.add(role);

            // Create user user
            User user = User.builder()
                    .username("user1")
                    .email("user1@example.com")
                    .password(passwordEncoder.encode("user1@123"))
                    .contact("1234567891")
                    .address("Kathmandu, Nepal")
                    .firstName("User")
                    .lastName("One")
                    .roles(roles)
                    .build();

            userRepository.save(user);

            System.out.println("User user has been seeded successfully");
        } else {
            System.out.println("User user already exists");
        }

        if (existingOrg.isEmpty()) {
            // Get all roles for admin
            Role role = roleRepository.findRoleByName("ORGANIZATION").get();
            List<Role> roles = new ArrayList<>();
            roles.add(role);

            Image image = Image.builder()
                    .imageId(1)
                    .imageType("png")
                    .imagePath("https://res.cloudinary.com/deytqgusq/image/upload/v1753457793/user_profiles/tbwdl9uanh9pit5lratd.png")
                    .imagePublicId("user_profiles/tbwdl9uanh9pit5lratd")
                    .build();
            imageRepository.save(image);

            // Create user user
            User user = User.builder()
                    .username("org1")
                    .email("org1@example.com")
                    .password(passwordEncoder.encode("org1@123"))
                    .contact("1234567892")
                    .address("Kathmandu, Nepal")
                    .name("Org One")
                    .organizationType(OrganizationType.POLICE)
                    .roles(roles)
                    .organizationImage(image)
                    .build();

            userRepository.save(user);

            System.out.println("Organization user and image has been seeded successfully");
        } else {
            System.out.println("Organization user already exists");
        }
    }

    private void seedIncidents() {
        // Check if roles already exist
        if (incidentRepository.count() == 0) {
            Point location = new GeometryFactory()
                    .createPoint(new Coordinate(85.324, 27.7172));
            location.setSRID(4326);

            Incident incident = Incident.builder()
                    .incidentId(1L)
                    .title("Huge Floods in Lalitpur")
                    .description("Two houses have been swept with the flood. The people are stranded.")
                    .incidentDate(LocalDateTime.now())
                    .listedDate(LocalDateTime.now())
                    .location(location)
                    .urgencyLevel(UrgencyLevel.HIGH)
                    .organizationType(OrganizationType.POLICE)
                    .uploader(User.builder().userId(2L).build())
                    .build();
            incidentRepository.save(incident);

            List<Image> images = new ArrayList<>();
            Image image1 = Image.builder()
                    .imageId(2)
                    .imageType("jpg")
                    .imagePath("https://res.cloudinary.com/deytqgusq/image/upload/v1753517062/incidents/smk35mfftdw2vxy3bfec.jpg")
                    .imagePublicId("incidents/smk35mfftdw2vxy3bfec")
                    .incident(incident)
                    .build();
            Image image2 = Image.builder()
                    .imageId(3)
                    .imageType("jpg")
                    .imagePath("https://res.cloudinary.com/deytqgusq/image/upload/v1753517063/incidents/s9jbnsois6oufcvdjnhq.jpg")
                    .imagePublicId("incidents/s9jbnsois6oufcvdjnhq")
                    .incident(incident)
                    .build();
            images.add(image1);
            images.add(image2);
            imageRepository.saveAll(images);

            incident.setImages(images);
            incidentRepository.save(incident);

            System.out.println("Incident and its images have been seeded successfully");
        } else {
            System.out.println("Incidents are already seeded");
        }
    }
}
