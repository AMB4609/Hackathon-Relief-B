package org.example.relief.controller;

import lombok.RequiredArgsConstructor;
import org.example.relief.request.DonationContributionRequest;
import org.example.relief.request.DonationRequest;
import org.example.relief.response.DonationResponse;
import org.example.relief.service.DonationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donations")
@RequiredArgsConstructor
public class DonationController {

    private final DonationService donationService;

    @PostMapping
    public ResponseEntity<DonationResponse> createDonation(@RequestBody DonationRequest request) {
        return ResponseEntity.ok(donationService.createDonation(request));
    }

    @GetMapping("/open")
    public ResponseEntity<List<DonationResponse>> getOpenDonations() {
        return ResponseEntity.ok(donationService.getOpenDonations());
    }
    // 🔹 User: Contribute to a donation campaign (anonymous or public)
    @PostMapping("/contribute")
    public ResponseEntity<String> contribute(@RequestBody DonationContributionRequest request) {
        donationService.contributeToDonation(request);
        return ResponseEntity.ok("Donation recorded.");
    }
}
