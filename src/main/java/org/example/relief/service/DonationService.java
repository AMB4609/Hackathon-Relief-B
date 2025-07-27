package org.example.relief.service;

import org.example.relief.request.DonationContributionRequest;
import org.example.relief.request.DonationRequest;
import org.example.relief.response.DonationResponse;

import java.util.List;

public interface DonationService {
    DonationResponse createDonation(DonationRequest request);
    List<DonationResponse> getOpenDonations();
    void contributeToDonation(DonationContributionRequest request);
    void updateDonationLimit(Long donationId, double newLimit);
    void closeDonation(Long donationId);

}
