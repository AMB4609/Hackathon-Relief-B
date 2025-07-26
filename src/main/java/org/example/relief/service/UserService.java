package org.example.relief.service;

import org.example.relief.request.LoginRequest;
import org.example.relief.request.OrganizationSignupRequest;
import org.example.relief.request.UserSignupRequest;
import org.example.relief.request.VolunteerStatusUpdateRequest;
import org.example.relief.response.LoginResponse;
import org.example.relief.response.OrganizationSignupResponse;
import org.example.relief.response.ProfilePageResponse;
import org.example.relief.response.UserSignupResponse;
import org.locationtech.jts.geom.Point;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserSignupResponse registerUser(UserSignupRequest userSignupRequest) throws Exception;

    OrganizationSignupResponse registerOrganization(
            OrganizationSignupRequest organizationSignupRequest,
            MultipartFile image)
            throws Exception;

    LoginResponse login(LoginRequest userLoginDTO);

    ProfilePageResponse getProfilePage(Long userId) throws Exception;

    void updateVolunteerStatus(VolunteerStatusUpdateRequest request) throws Exception;

    void updateUserLocation(long userId, double longitude, double latitude);

    void updateUserLocation(long userId, Point location);

    void disableUserPosting(Long userId) throws Exception;
}
