package com.nus.cs5224.smartsg.controller;

import com.nus.cs5224.smartsg.dto.request.ProfileUpdateRequest;
import com.nus.cs5224.smartsg.dto.response.ProfileResponse;
import com.nus.cs5224.smartsg.entity.UserProfile;
import com.nus.cs5224.smartsg.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    /**
     * GET /api/profile/me
     * Get current user's profile
     */
    @GetMapping("/me")
    public ProfileResponse getMyProfile(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("currentUserId");
        UserProfile profile = profileService.getOrCreateProfile(userId);
        return profileService.convertToResponse(profile);
    }

    /**
     * PUT /api/profile/me
     * Update current user's profile
     */
    @PutMapping("/me")
    public Map<String, Boolean> updateMyProfile(@RequestBody ProfileUpdateRequest request,
                                                HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("currentUserId");
        profileService.updateProfile(userId, request);
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", true);
        return response;
    }
}