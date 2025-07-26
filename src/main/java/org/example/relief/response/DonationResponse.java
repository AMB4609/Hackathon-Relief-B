package org.example.relief.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DonationResponse {
    private Long id;
    private String donationName;
    private String donationLimit;
    private String collectedAmount;
    private boolean isOpen;
    private Long incidentId;
    private String incidentTitle;
}
