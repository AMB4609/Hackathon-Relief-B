package org.example.relief.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.relief.model.LocalNoti;
import org.example.relief.model.User;
import org.example.relief.repository.LocalNotiRepository;
import org.example.relief.service.LocalNotiService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LocalNotiServiceImpl implements LocalNotiService {
    private final LocalNotiRepository notificationRepository;

    @Override
    public void save(User receiver,
                     String title,
                     String body,
                     String type,
                     String referenceId) {

        notificationRepository.save(LocalNoti.builder()
                .receiver(receiver)
                .title(title)
                .body(body)
                .type(type)
                .referenceId(referenceId)
                .sentAt(LocalDateTime.now())
                .build());
    }
}
