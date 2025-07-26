package org.example.relief.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double amount;
    private LocalDateTime donatedAt;
    private boolean anonymous;

    @ManyToOne
    private Donation donation;

    @ManyToOne(optional = true)
    private User user;
}
