package org.example.relief.request;

import lombok.Data;

@Data
public class DonationRequest {
    private Long incidentId;
    private Double donationLimit;
}
