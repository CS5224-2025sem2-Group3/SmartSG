package com.nus.cs5224.smartsg.controller;

import com.nus.cs5224.smartsg.dto.request.ProfileUpdateRequest;
import com.nus.cs5224.smartsg.dto.response.ProfileResponse;
import com.nus.cs5224.smartsg.dto.response.UserResponse;
import com.nus.cs5224.smartsg.entity.UserProfile;
import com.nus.cs5224.smartsg.service.ProfileService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    private Long getCurrentUserId(HttpSession session) {
        UserResponse currentUser = (UserResponse) session.getAttribute("currentUser");
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not logged in");
        }
        return currentUser.getId();
    }

    @GetMapping("/me")
    public ProfileResponse getMyProfile(HttpSession session) {
        Long userId = getCurrentUserId(session);
        UserProfile profile = profileService.getOrCreateProfile(userId);
        return profileService.convertToResponse(profile);
    }

    @PutMapping("/me")
    public Map<String, Boolean> updateMyProfile(@RequestBody ProfileUpdateRequest request, HttpSession session) {
        Long userId = getCurrentUserId(session);
        profileService.updateProfile(userId, request);
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", true);
        return response;
    }
}