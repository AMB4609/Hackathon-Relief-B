package org.example.relief.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DonationResponse {
    private Long id;
    private Double donationLimit;
    private Double collectedAmount;
    private boolean isOpen;
    private Long incidentId;
    private String incidentTitle;
}
