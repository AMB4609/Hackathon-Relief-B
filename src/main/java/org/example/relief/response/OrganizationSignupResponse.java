package org.example.relief.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationSignupResponse {
    private String username;
    private String name;
    private String lastName;
    private String email;
    private String contact;
    private String address;
    private ImageResponse image;
}
