package org.example.relief.controller;

import lombok.RequiredArgsConstructor;
import org.example.relief.model.LocalNoti;
import org.example.relief.repository.LocalNotiRepository;
import org.example.relief.response.LocalNotiDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class LocalNotiController {

    private final LocalNotiRepository notificationRepository;

    @GetMapping("/me/{userId}")
    public List<LocalNotiDto> myNotifications(@PathVariable Long userId) {
        return notificationRepository
                .findByReceiverUserIdOrderBySentAtDesc(userId)
                .stream()
                .map(noti -> LocalNotiDto.builder()
                        .id(noti.getId())
                        .title(noti.getTitle())
                        .body(noti.getBody())
                        .type(noti.getType())
                        .sentAt(noti.getSentAt())
                        .read(noti.isRead())
                        .userId(noti.getReceiver().getUserId())
                        .build())
                .collect(Collectors.toList());
    }

    @PostMapping("/read/{id}")
    public void markRead(@PathVariable Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }
}

