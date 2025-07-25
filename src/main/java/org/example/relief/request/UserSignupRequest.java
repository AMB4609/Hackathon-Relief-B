package org.example.relief.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSignupRequest {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String contact;
    private String password;
    private String confirmPassword;
    private String address;
}
