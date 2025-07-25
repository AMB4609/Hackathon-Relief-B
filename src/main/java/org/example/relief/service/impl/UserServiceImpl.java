package org.example.relief.service.impl;

import org.example.relief.enums.OrganizationType;
import org.example.relief.model.Image;
import org.example.relief.model.Role;
import org.example.relief.model.User;
import org.example.relief.repository.ImageRepository;
import org.example.relief.repository.UserRepository;
import org.example.relief.request.LoginRequest;
import org.example.relief.request.OrganizationSignupRequest;
import org.example.relief.request.UserSignupRequest;
import org.example.relief.response.ImageResponse;
import org.example.relief.response.LoginResponse;
import org.example.relief.response.OrganizationSignupResponse;
import org.example.relief.response.UserSignupResponse;
import org.example.relief.service.CloudinaryService;
import org.example.relief.service.RoleService;
import org.example.relief.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RoleService roleService;
    private final JwtServiceImpl jwtServiceImpl;
    private final CloudinaryService cloudinaryService;
    private final ImageRepository imageRepository;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           RoleService roleService,
                           JwtServiceImpl jwtServiceImpl,
                           CloudinaryService cloudinaryService,
                           ImageRepository imageRepository){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.roleService = roleService;
        this.jwtServiceImpl = jwtServiceImpl;
        this.cloudinaryService = cloudinaryService;
        this.imageRepository = imageRepository;
    }

    @Override
    public UserSignupResponse registerUser(UserSignupRequest userSignupRequest) throws Exception {

        credentialsCheck(userSignupRequest.getPassword(), userSignupRequest.getConfirmPassword(),
                userSignupRequest.getUsername(), userSignupRequest.getEmail(),userSignupRequest.getContact());

        validatePasswordStrength(userSignupRequest.getPassword());

        //Adding the Role to user's Role
        List<Role> userRoles = new ArrayList<>();
        try {
            Role userRole = roleService.getRoleByName("USER");
            userRoles.add(userRole);
        } catch (Exception e){
            throw new Exception("Role not found.");
        }

        User createdUser = userRepository.save(User.builder()
                .username(userSignupRequest.getUsername())
                .firstName(userSignupRequest.getFirstName())
                .lastName(userSignupRequest.getLastName())
                .email(userSignupRequest.getEmail())
                .contact(userSignupRequest.getContact())
                .password(passwordEncoder.encode(userSignupRequest.getPassword()))
                .address(userSignupRequest.getAddress())
                .roles(userRoles)
                .build());

        return UserSignupResponse.builder()
                .username(userSignupRequest.getUsername())
                .firstName(userSignupRequest.getFirstName())
                .lastName(userSignupRequest.getLastName())
                .email(userSignupRequest.getEmail())
                .contact(userSignupRequest.getContact())
                .address(userSignupRequest.getAddress())
                .build();
    }

    @Override
    public OrganizationSignupResponse registerOrganization(
            OrganizationSignupRequest organizationSignupRequest,
            MultipartFile image)
            throws Exception {

        credentialsCheck(organizationSignupRequest.getPassword(), organizationSignupRequest.getConfirmPassword(),
                organizationSignupRequest.getUsername(), organizationSignupRequest.getEmail(),organizationSignupRequest.getContact());

        validatePasswordStrength(organizationSignupRequest.getPassword());

        //Adding the Role to user's Role
        List<Role> organizationRoles = new ArrayList<>();
        try {
            Role userRole = roleService.getRoleByName("ORGANIZATION");
            organizationRoles.add(userRole);
        } catch (Exception e){
            throw new Exception("Role not found.");
        }

        //Todo : cloudinary
        if (image == null || image.isEmpty()){
            throw new Exception("Didn't receive organization image.");
        }
        //upload to cloudinary
        Map uploadResult = cloudinaryService.uploadImage(image, "user_profiles");

        //Saving organization to db
        User savedOrganization = userRepository.save(User.builder()
                .username(organizationSignupRequest.getUsername())
                .roles(organizationRoles)
                .name(organizationSignupRequest.getName())
                .email(organizationSignupRequest.getEmail())
                .contact(organizationSignupRequest.getContact())
                .password(passwordEncoder.encode(organizationSignupRequest.getPassword()))
                .address(organizationSignupRequest.getAddress())
                .organizationType(convertToOrganizationTypeEnum(organizationSignupRequest.getOrganizationType()))
                .build());

        //save details to local image entity and saving to db
        Image savedImageInDb = imageRepository.save(Image.builder()
                .imagePath(uploadResult.get("secure_url").toString())
                .imageType(uploadResult.get("format").toString())
                .imagePublicId(uploadResult.get("public_id").toString())
                .organization(savedOrganization)
                .build());

        //linking image to saved organization and saving it again
        savedOrganization.setOrganizationImage(savedImageInDb);
        userRepository.save(savedOrganization);

        return OrganizationSignupResponse.builder()
                .username(organizationSignupRequest.getUsername())
                .name(organizationSignupRequest.getName())
                .email(organizationSignupRequest.getEmail())
                .contact(organizationSignupRequest.getContact())
                .address(organizationSignupRequest.getAddress())
                .image(ImageResponse.builder()
                        .imagePath(savedImageInDb.getImagePath())
                        .imageType(savedImageInDb.getImageType())
                        .build())
                .build();
    }

    @Override
    public LoginResponse login(LoginRequest userLoginDTO) {

        User user = userRepository.findByEmail(userLoginDTO.getEmailOrContact())
                .orElseGet(() -> userRepository.findByContact(userLoginDTO.getEmailOrContact())
                        .orElseThrow(() -> new UsernameNotFoundException("User not found")));

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userLoginDTO.getEmailOrContact(),
                        userLoginDTO.getPassword()
                )
        );

        String jwtToken = jwtServiceImpl.generateToken(user);

        //Roles - Admin has all the roles assigned to him, but user and organizations only have their respective role
        String roleName = null;
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            // Check if the user has the "ADMIN" role
            boolean isAdmin = user.getRoles().stream()
                    .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));
            if (isAdmin) {
                roleName = "ADMIN";
            } else {
                roleName = user.getRoles().get(0).getName();
            }
        }

        return LoginResponse.builder()
                .userId(user.getUserId())
                .token(jwtToken)
                .expiresIn("1 Day")
                .role(roleName)
                .build();
    }

    private void credentialsCheck(
            String password, String confirmPassword,
            String username, String email, String contact) throws Exception {
        if(!password.equals(confirmPassword)){
            throw new Exception("Passwords don't match.");
        }

        Optional<User> usernameCheck = userRepository.findUserByUsername(username);
        if(usernameCheck.isPresent()){
            throw new Exception("User with username already exists.");
        }

        Optional<User> emailCheck = userRepository.findByEmail(email);
        if(emailCheck.isPresent()){
            throw new Exception("User with email already exists.");
        }

        Optional<User> contactCheck = userRepository.findByContact(contact);
        if(contactCheck.isPresent()){
            throw new Exception("User with contact already exists.");
        }
    }

    private void validatePasswordStrength(String password) throws Exception {
        if (password == null || password.length() < 8) {
            throw new Exception("Password must be at least 8 characters long");
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new Exception("Password must contain at least one uppercase letter");
        }

        if (!password.matches(".*[a-z].*")) {
            throw new Exception("Password must contain at least one lowercase letter");
        }

        if (!password.matches(".*\\d.*")) {
            throw new Exception("Password must contain at least one digit");
        }

        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new Exception("Password must contain at least one special character");
        }
    }

    private OrganizationType convertToOrganizationTypeEnum(String type) throws Exception {
        if(type.equalsIgnoreCase("police")) return OrganizationType.POLICE;
        if(type.equalsIgnoreCase("fire")) return OrganizationType.FIRE;
        if(type.equalsIgnoreCase("ambulance")) return OrganizationType.AMBULANCE;
        if(type.equalsIgnoreCase("vet")) return OrganizationType.VET;
        throw new Exception("Organization Type Not Found");
    }
}
