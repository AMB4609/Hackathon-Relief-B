package org.example.relief.request;

import lombok.Data;

@Data
public class DonationContributionRequest {
    private Long donationId;
    private Double amount;
    private Long userId;
    private boolean anonymous;// Optional
}
