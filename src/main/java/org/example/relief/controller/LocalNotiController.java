package org.example.relief.controller;

import lombok.RequiredArgsConstructor;
import org.example.relief.model.LocalNoti;
import org.example.relief.repository.LocalNotiRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class LocalNotiController {

    private final LocalNotiRepository notificationRepository;

    @GetMapping("/me")
    public List<LocalNoti> myNotifications(@RequestParam Long userId) {
        return notificationRepository
                .findByReceiverUserIdOrderBySentAtDesc(userId);
    }

    @PostMapping("/{id}/read")
    public void markRead(@PathVariable Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }
}

