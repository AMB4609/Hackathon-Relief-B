package org.example.relief.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class LocalNotiDto {
    private Long id;
    private String title;
    private String body;
    private String type;
    private LocalDateTime sentAt;
    private boolean read;
    private Long userId;
}