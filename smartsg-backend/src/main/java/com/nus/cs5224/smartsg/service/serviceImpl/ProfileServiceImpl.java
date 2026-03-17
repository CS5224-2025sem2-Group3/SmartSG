package com.nus.cs5224.smartsg.service.serviceImpl;

import com.nus.cs5224.smartsg.dto.request.ProfileUpdateRequest;
import com.nus.cs5224.smartsg.dto.response.ProfileResponse;
import com.nus.cs5224.smartsg.entity.Cleanliness;
import com.nus.cs5224.smartsg.entity.Gender;
import com.nus.cs5224.smartsg.entity.SleepSchedule;
import com.nus.cs5224.smartsg.entity.UserProfile;
import com.nus.cs5224.smartsg.mapper.ProfileMapper;
import com.nus.cs5224.smartsg.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
        if (request.getSleepHabit() != null) {
            profile.setSleepSchedule(parseSleepHabit(request.getSleepHabit()));
        }
        if (request.getCleanliness() != null) {
            profile.setCleanliness(parseCleanliness(request.getCleanliness()));
        }
        if (request.getSmoking() != null) {
            profile.setSmoking(parseSmoking(request.getSmoking()));
        }
        if (request.getGender() != null) {
            profile.setGender(parseGender(request.getGender()));
        }

        if (profile.getUserId() == null) { // 实际不会发生，但保留
            profileMapper.insert(profile);
        } else {
            profileMapper.update(profile);
        }
    }

    private SleepSchedule parseSleepHabit(String habit) {
        switch (habit) {
            case "EarlyBird": return SleepSchedule.early_bird;
            case "Regular": return SleepSchedule.normal;
            case "NightOwl": return SleepSchedule.night_owl;
            default: throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sleepHabit: " + habit);
        }
    }

    private Cleanliness parseCleanliness(String value) {
        switch (value) {
            case "Low": return Cleanliness.low;
            case "Average": return Cleanliness.medium;
            case "High": return Cleanliness.high;
            default: throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid cleanliness: " + value);
        }
    }

    private Boolean parseSmoking(String value) {
        switch (value) {
            case "Yes": return true;
            case "No": return false;
            default: throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid smoking: " + value);
        }
    }

    private Gender parseGender(String value) {
        switch (value) {
            case "Male":
                return Gender.male;
            case "Female":
                return Gender.female;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid gender: " + value);
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
                case early_bird: resp.setSleepHabit("EarlyBird"); break;
                case normal: resp.setSleepHabit("Regular"); break;
                case night_owl: resp.setSleepHabit("NightOwl"); break;
            }
        }

        // smoking 转换
        if (profile.getSmoking() != null) {
            resp.setSmoking(profile.getSmoking() ? "Yes" : "No");
        }

        // cleanliness 转换
        if (profile.getCleanliness() != null) {
            switch (profile.getCleanliness()) {
                case low: resp.setCleanliness("Low"); break;
                case medium: resp.setCleanliness("Average"); break;
                case high: resp.setCleanliness("High"); break;
            }
        }

        // gender 转换
        if (profile.getGender() != null) {
            switch (profile.getGender()) {
                case male: resp.setGender("Male"); break;
                case female: resp.setGender("Female"); break;
            }
        }

        return resp;
    }
}