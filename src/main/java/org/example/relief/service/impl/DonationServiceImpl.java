package org.example.relief.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.relief.model.Donation;
import org.example.relief.model.Incident;
import org.example.relief.model.Transaction;
import org.example.relief.repository.DonationRepository;
import org.example.relief.repository.IncidentRepository;
import org.example.relief.repository.TransactionRepository;
import org.example.relief.repository.UserRepository;
import org.example.relief.request.DonationContributionRequest;
import org.example.relief.request.DonationRequest;
import org.example.relief.response.DonationResponse;
import org.example.relief.service.DonationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DonationServiceImpl implements DonationService {

    private final DonationRepository donationRepository;
    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public DonationResponse createDonation(DonationRequest request) {
        Incident incident = incidentRepository.findById(request.getIncidentId())
                .orElseThrow(() -> new IllegalArgumentException("Incident not found"));

        Donation donation = Donation.builder()
                .incident(incident)
                .collectedAmount(request.getCollectedAmount())
                .donationLimit(request.getDonationLimit())
                .open(request.isOpen())
                .build();

        donationRepository.save(donation);

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

        double current = Double.parseDouble(donation.getCollectedAmount());
        double incoming = request.getAmount();
        double limit = Double.parseDouble(donation.getDonationLimit());

        double updated = current + incoming;
        donation.setCollectedAmount(String.valueOf(updated));
        donation.setDonationCount(donation.getDonationCount() + 1);

        if (updated >= limit) {
            donation.setOpen(false);
        }

        Transaction.TransactionBuilder txBuilder = Transaction.builder()
                .amount(incoming)
                .donatedAt(LocalDateTime.now())
                .donation(donation)
                .anonymous(request.isAnonymous());

        if (!request.isAnonymous() && request.getUserId() != null) {
            userRepository.findById(request.getUserId()).ifPresent(txBuilder::user);
        }


        transactionRepository.save(txBuilder.build());
        donationRepository.save(donation);
    }

    private DonationResponse mapToResponse(Donation donation) {
        return DonationResponse.builder()
                .id(donation.getId())
                .collectedAmount(donation.getCollectedAmount())
                .donationLimit(donation.getDonationLimit())
                .isOpen(donation.isOpen())
                .incidentId(donation.getIncident().getIncidentId())
                .incidentTitle(donation.getIncident().getTitle())
                .build();
    }
}
