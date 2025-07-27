package org.example.relief.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.relief.model.Donation;
import org.example.relief.model.Incident;
import org.example.relief.model.Transaction;
import org.example.relief.repository.*;
import org.example.relief.request.DonationContributionRequest;
import org.example.relief.request.DonationRequest;
import org.example.relief.response.DonationResponse;
import org.example.relief.service.DonationService;
import org.example.relief.service.LocalNotiService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DonationServiceImpl implements DonationService {

    private final DonationRepository donationRepository;
    private final LocalNotiService notificationService;
    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public DonationResponse createDonation(DonationRequest request) {
        Incident incident = incidentRepository.findById(request.getIncidentId())
                .orElseThrow(() -> new IllegalArgumentException("Incident not found"));

        Donation donation = donationRepository.save(Donation.builder()
                .incident(incident)
                .collectedAmount(0D)
                .isOpen(true)
                .donationCount(0)
                .donationLimit(request.getDonationLimit())
                .build());

        userRepository.findUsersByRole("USER").forEach(user -> notificationService.save(
                user,
                "🆕 Donation Campaign: " + incident.getTitle(),
                "Help reach NPR " + request.getDonationLimit() + ". You can contribute from incidents page.",
                "DONATION_CREATE",
                donation.getDonationId().toString()
        ));

        return mapToResponse(donation);
    }

    @Override
    public List<DonationResponse> getOpenDonations() {
        return donationRepository.findAllByOpenTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    @Override
    public void contributeToDonation(DonationContributionRequest request) {
        Donation donation = donationRepository.findById(request.getDonationId())
                .orElseThrow(() -> new IllegalArgumentException("Donation not found"));

        if (!donation.isOpen()) {
            throw new IllegalStateException("Donation is closed.");
        }

        //the payment always succeeds as this is for demo

        double updated = donation.getCollectedAmount() + request.getAmount();
        donation.setCollectedAmount(updated);
        donation.setDonationCount(donation.getDonationCount() + 1);

        if (updated >= donation.getDonationLimit()) {
            donation.setOpen(false);
        }

        Transaction.TransactionBuilder txBuilder = Transaction.builder()
                .amount(request.getAmount())
                .donatedAt(LocalDateTime.now())
                .donation(donation)
                .anonymous(request.isAnonymous());

        if (!request.isAnonymous() && request.getUserId() != null) {
            userRepository.findById(request.getUserId()).ifPresent(txBuilder::user);
        }

        transactionRepository.save(txBuilder.build());
        donationRepository.save(donation);
    }
    @Override
    public void updateDonationLimit(Long donationId, double newLimit) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new IllegalArgumentException("Donation not found"));

        donation.setDonationLimit(newLimit);
        donationRepository.save(donation);
    }
    @Override
    public void closeDonation(Long donationId) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new IllegalArgumentException("Donation not found"));

        donation.setOpen(false);
        donationRepository.save(donation);
    }

    private DonationResponse mapToResponse(Donation donation) {
        return DonationResponse.builder()
                .id(donation.getDonationId())
                .collectedAmount(donation.getCollectedAmount())
                .donationLimit(donation.getDonationLimit())
                .isOpen(donation.isOpen())
                .incidentId(donation.getIncident().getIncidentId())
                .incidentTitle(donation.getIncident().getTitle())
                .build();
    }
}