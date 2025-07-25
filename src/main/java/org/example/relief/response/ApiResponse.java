package org.example.relief.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private Integer  code;
    private LocalDateTime timestamp;
    private boolean success;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(int code,String message, T data) {
        return ApiResponse.<T>builder()
                .code(code)
                .timestamp(LocalDateTime.now())
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(int code, String message, T data) {
        return ApiResponse.<T>builder()
                .code(code)
                .timestamp(LocalDateTime.now())
                .success(false)
                .message(message)
                .data(data)
                .build();
    }
}
