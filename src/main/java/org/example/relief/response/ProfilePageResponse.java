package org.example.relief.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfilePageResponse {
    private String userId;
    private String username;
    private String fullName;
    private String email;
    private String contact;
    private String address;
    private boolean isVolunteer;
    private ImageResponse organizationImage;
}
