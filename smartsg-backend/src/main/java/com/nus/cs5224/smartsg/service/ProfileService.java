package com.nus.cs5224.smartsg.service;

import com.nus.cs5224.smartsg.dto.request.ProfileUpdateRequest;
import com.nus.cs5224.smartsg.dto.response.ProfileResponse;
import com.nus.cs5224.smartsg.entity.UserProfile;

public interface ProfileService {
    public UserProfile getOrCreateProfile(Long userId);
    public void updateProfile(Long userId, ProfileUpdateRequest request);
    public ProfileResponse convertToResponse(UserProfile profile);
}
