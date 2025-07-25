package org.example.relief.controller;

import org.example.relief.request.FcmTokenRequest;
import org.example.relief.model.User;
import org.example.relief.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class FcmTokenController {

    private final UserRepository userRepository;

    public FcmTokenController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/fcm-token")
    public ResponseEntity<String> updateFcmToken(@RequestBody FcmTokenRequest request) {
        return userRepository.findById(request.getId())
                .map(user -> {
                    user.setFcmToken(request.getFcmToken());
                    userRepository.save(user);
                    return ResponseEntity.ok("FCM token updated successfully.");
                })
                .orElse(ResponseEntity.badRequest().body("User not found."));
    }
}
