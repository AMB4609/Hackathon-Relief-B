package org.example.relief.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.relief.repository.UserRepository;
import org.example.relief.request.*;
import org.example.relief.response.LoginResponse;
import org.example.relief.response.OrganizationSignupResponse;
import org.example.relief.response.ProfilePageResponse;
import org.example.relief.response.UserSignupResponse;
import org.example.relief.service.UserService;
import org.example.relief.service.impl.UserServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final UserServiceImpl userServiceImpl;

    public UserController(UserService userService, UserRepository userRepository, UserServiceImpl userServiceImpl){
        this.userService = userService;
        this.userRepository = userRepository;
        this.userServiceImpl = userServiceImpl;
    }

    @PostMapping("/signup/user")
    public ResponseEntity<?> registerUser(@RequestBody UserSignupRequest userSignupRequest){
        try{
            UserSignupResponse user = userService.registerUser(userSignupRequest);
            return new ResponseEntity<>(user, HttpStatus.CREATED);
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value="/signup/organization", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerOrganization(@RequestPart("organization") String organizationString,
                                                  @RequestPart(value = "image") MultipartFile image){
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            OrganizationSignupRequest organizationSignupRequest = objectMapper.readValue(
                    organizationString, OrganizationSignupRequest.class);

            OrganizationSignupResponse organization = userService.registerOrganization(organizationSignupRequest, image);
            return new ResponseEntity<>(organization, HttpStatus.CREATED);
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest){
        try{
            LoginResponse result = userService.login(loginRequest);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> profile(@PathVariable Long userId){
        try{
            ProfilePageResponse profile = userService.getProfilePage(userId);
            return new ResponseEntity<>(profile, HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/updateVolunteerStatus")
    public void updateVolunteerStatus(@RequestBody VolunteerStatusUpdateRequest request) throws Exception {
        userServiceImpl.updateVolunteerStatus(request);
    }

}
