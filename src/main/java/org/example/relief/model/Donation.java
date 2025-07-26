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
@Table(name = "donations")
public class Donation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long donationId;

    private Double collectedAmount;

    private Double donationLimit;

    private boolean isOpen = true;

    private Integer donationCount;

    @OneToOne
    private Incident incident;

}
