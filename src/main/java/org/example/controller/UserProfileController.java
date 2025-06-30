package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.UserProfileRequest;
import org.example.dto.UserProfileResponse;
import org.example.service.JwtService;
import org.example.service.UserProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class UserProfileController {
    private final UserProfileService userProfileService;
    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<UserProfileResponse> getUserProfile(
            @RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        Long userId = jwtService.extractUserId(jwt);
        return ResponseEntity.ok(userProfileService.getUserProfile(userId));
    }

    @PutMapping
    public ResponseEntity<UserProfileResponse> updateUserProfile(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody UserProfileRequest request) {
        String jwt = token.substring(7);
        Long userId = jwtService.extractUserId(jwt);
        return ResponseEntity.ok(userProfileService.updateUserProfile(userId, request));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody UserProfileRequest request) {
        String jwt = token.substring(7);
        Long userId = jwtService.extractUserId(jwt);
        userProfileService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }
} 