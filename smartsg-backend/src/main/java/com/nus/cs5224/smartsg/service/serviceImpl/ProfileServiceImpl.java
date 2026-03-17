package com.nus.cs5224.smartsg.service.serviceImpl;

import com.nus.cs5224.smartsg.dto.request.ProfileUpdateRequest;
import com.nus.cs5224.smartsg.dto.response.ProfileResponse;
import com.nus.cs5224.smartsg.entity.UserProfile;
import com.nus.cs5224.smartsg.mapper.ProfileMapper;
import com.nus.cs5224.smartsg.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileServiceImpl implements ProfileService {

    @Autowired
    private ProfileMapper profileMapper;

    @Transactional
    @Override
    public UserProfile getOrCreateProfile(Long userId) {
        UserProfile profile = profileMapper.findByUserId(userId);
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(userId);
            // 所有字段默认为 null
            profileMapper.insert(profile);
        }
        return profile;
    }

    @Transactional
    @Override
    public void updateProfile(Long userId, ProfileUpdateRequest request) {
        UserProfile profile = profileMapper.findByUserId(userId);
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(userId);
        }
        // 仅覆盖非 null 字段
        if (request.getBudgetMax() != null) {
            profile.setMaxBudget(request.getBudgetMax());
        }
        if (request.getMoveInWindow() != null) {
            profile.setMoveInWindow(request.getMoveInWindow());
        }
        if (request.getLeasePreference() != null) {
            profile.setLeasePreference(request.getLeasePreference());
        }

        if (profile.getUserId() == null) { // 实际不会发生，但保留
            profileMapper.insert(profile);
        } else {
            profileMapper.update(profile);
        }
    }

    @Override
    public ProfileResponse convertToResponse(UserProfile profile) {
        ProfileResponse resp = new ProfileResponse();
        resp.setBudgetMax(profile.getMaxBudget());
        resp.setMoveInWindow(profile.getMoveInWindow() != null ? profile.getMoveInWindow().toString() : null);
        resp.setLeasePreference(profile.getLeasePreference());

        // sleepHabit 转换
        if (profile.getSleepSchedule() != null) {
            switch (profile.getSleepSchedule()) {
                case early_bird: resp.setSleepHabit("Early Bird"); break;
                case normal: resp.setSleepHabit("Regular"); break;
                case night_owl: resp.setSleepHabit("Night Owl"); break;
            }
        }

        // smoking 转换
        if (profile.getSmoking() != null) {
            resp.setSmoking(profile.getSmoking() ? "Yes Smoking" : "No Smoking");
        }

        // cleanliness 转换
        if (profile.getCleanliness() != null) {
            switch (profile.getCleanliness()) {
                case low: resp.setCleanliness("Low"); break;
                case medium: resp.setCleanliness("Average"); break;
                case high: resp.setCleanliness("High"); break;
            }
        }
        return resp;
    }
}