package org.example.relief.service;

import org.example.relief.request.LoginRequest;
import org.example.relief.request.OrganizationSignupRequest;
import org.example.relief.request.UserSignupRequest;
import org.example.relief.response.LoginResponse;
import org.example.relief.response.OrganizationSignupResponse;
import org.example.relief.response.UserSignupResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserSignupResponse registerUser(UserSignupRequest userSignupRequest) throws Exception;

    OrganizationSignupResponse registerOrganization(
            OrganizationSignupRequest organizationSignupRequest,
            MultipartFile image)
            throws Exception;

    LoginResponse login(LoginRequest userLoginDTO);
}
