package org.example.relief.repository;

import org.example.relief.model.Flag;
import org.example.relief.model.Incident;
import org.example.relief.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FlagRepository extends JpaRepository<Flag, Long> {
    boolean existsByUserAndIncident(User user, Incident incident);
    Optional<Flag> findByUserAndIncident(User user, Incident incident);
    int countByIncident(Incident incident);
}
