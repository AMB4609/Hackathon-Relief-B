package org.example.relief.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSignupResponse {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String contact;
    private String address;
}
