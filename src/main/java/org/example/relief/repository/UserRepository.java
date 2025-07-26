package org.example.relief.repository;

import org.example.relief.model.User;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByContact(String contact);

    @Query("SELECT u FROM User u WHERE ST_DWithin(u.availableLocation, :point, :distance)")
    List<User> findUsersWithinDistance(@Param("point") Point point, @Param("distance") Double distance);
}
