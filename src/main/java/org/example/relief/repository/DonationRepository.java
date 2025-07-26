package org.example.relief.repository;

import org.example.relief.model.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DonationRepository extends JpaRepository<Donation, Long> {

    @Query(value = "SELECT * FROM donations WHERE open = true", nativeQuery = true)
    List<Donation> findAllByOpenTrue();

}
