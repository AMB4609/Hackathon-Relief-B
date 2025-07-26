package org.example.relief.repository;

import org.example.relief.model.Incident;
import org.example.relief.model.User;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByContact(String contact);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findUsersByRole(@Param("roleName") String roleName);

    @Query(value = "SELECT * FROM users WHERE is_volunteer = true", nativeQuery = true)
    List<User> findAllByVolunteerTrue();

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.availableLocation = :location, " +
            "u.locationUpdatedAt = :updatedAt WHERE u.userId = :userId")
    void updateUserLocation(@Param("userId") long userId,
                            @Param("location") Point updatedLocation,
                            @Param("updatedAt") LocalDateTime updatedAt);

//    @Query("SELECT u FROM User u WHERE ST_DWithin(u.availableLocation, :point, :distance)")
    @Query(value = "SELECT * FROM users WHERE ST_DWithin(available_location, :point, :distance) ",
            nativeQuery = true)
    List<User> findUsersWithinDistance(@Param("point") Point point,
                                       @Param("distance") Double distance);



}
