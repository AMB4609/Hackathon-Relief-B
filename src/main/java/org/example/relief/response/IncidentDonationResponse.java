package org.example.relief.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IncidentDonationResponse {
    private Long donationId;
    private Double donationLimit;
    private Double collectedAmount;
    private boolean isOpen;
}
