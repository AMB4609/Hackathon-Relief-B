package org.example.relief.request;

import lombok.Data;

@Data
public class DonationRequest {
    private Long incidentId;
    private String donationName;
    private String donationLimit;
    private String collectedAmount;
    private boolean isOpen;
}
