package org.example.relief.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private Long userId;
    private String token;
    private String expiresIn;
    private String role;
}
