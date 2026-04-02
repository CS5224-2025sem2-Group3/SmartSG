package com.nus.cs5224.smartsg.service.serviceImpl;

import com.nus.cs5224.smartsg.dto.response.RecommendationResponse;
import com.nus.cs5224.smartsg.entity.UserProfile;
import com.nus.cs5224.smartsg.mapper.GroupMapper;
import com.nus.cs5224.smartsg.mapper.MatchMapper;
import com.nus.cs5224.smartsg.service.MatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchingServiceImpl implements MatchingService {

    @Autowired
    private MatchMapper matchMapper;

    @Autowired
    private GroupMapper groupMapper;

    @Override
    public List<RecommendationResponse> getRecommendations(
            int groupId,
            Long currentUserId,
            Integer budgetMax,
            String moveInWindow,
            Integer leasePreference,
            String sleepHabit,
            String studyPreference,
            String smoking,
            String cleanliness) {

        // Validate group exists
        if (groupMapper.findById(groupId) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found");
        }

        // Get all candidate users (not already in the group)
        List<UserProfile> candidates = matchMapper.findCandidates(groupId, currentUserId);

        // Parse moveInWindow filter date (optional)
        LocalDate filterDate = null;
        if (moveInWindow != null && !moveInWindow.isBlank()) {
            try {
                filterDate = LocalDate.parse(moveInWindow);
            } catch (DateTimeParseException e) {
                // ignore invalid date filter
            }
        }
        final LocalDate finalFilterDate = filterDate;

        // Score each candidate and build response
        return candidates.stream()
                .map(profile -> {
                    int score = computeScore(profile, budgetMax, finalFilterDate,
                            leasePreference, sleepHabit, smoking, cleanliness);
                    String moveInStr = profile.getMoveInWindow() != null
                            ? profile.getMoveInWindow().toString() : null;
                    return new RecommendationResponse(
                            profile.getUserId(),
                            profile.getUserName(),
                            profile.getMaxBudget(),
                            moveInStr,
                            profile.getLeasePreference(),
                            convertSleepHabit(profile),
                            convertSmoking(profile),
                            convertCleanliness(profile),
                            score
                    );
                })
                .sorted((a, b) -> b.getMatchScore() - a.getMatchScore())
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * Scoring dimensions (max 7 points):
     * - budgetMax: candidate's maxBudget >= filter budgetMax  → +1
     * - moveInWindow: date within ±30 days                    → +2
     * - leasePreference: same lease months                    → +1
     * - sleepHabit: same sleep_schedule enum                  → +1
     * - smoking: same smoking boolean                         → +1
     * - cleanliness: same cleanliness enum                    → +1
     */
    private int computeScore(UserProfile profile,
                             Integer budgetMax,
                             LocalDate filterDate,
                             Integer leasePreference,
                             String sleepHabit,
                             String smoking,
                             String cleanliness) {
        int score = 0;

        if (budgetMax != null && profile.getMaxBudget() != null
                && profile.getMaxBudget() >= budgetMax) {
            score += 1;
        }

        if (filterDate != null && profile.getMoveInWindow() != null) {
            long daysDiff = Math.abs(profile.getMoveInWindow().toEpochDay() - filterDate.toEpochDay());
            if (daysDiff <= 30) {
                score += 2;
            }
        }

        if (leasePreference != null && leasePreference.equals(profile.getLeasePreference())) {
            score += 1;
        }

        if (sleepHabit != null && profile.getSleepSchedule() != null
                && sleepHabit.equalsIgnoreCase(profile.getSleepSchedule().name())) {
            score += 1;
        }

        if (smoking != null && profile.getSmoking() != null) {
            boolean filterSmoking = Boolean.parseBoolean(smoking) ||
                    "true".equalsIgnoreCase(smoking) || "smoking".equalsIgnoreCase(smoking);
            if (filterSmoking == profile.getSmoking()) {
                score += 1;
            }
        }

        if (cleanliness != null && profile.getCleanliness() != null
                && cleanliness.equalsIgnoreCase(profile.getCleanliness().name())) {
            score += 1;
        }

        return score;
    }

    private String convertSleepHabit(UserProfile profile) {
        if (profile.getSleepSchedule() == null) return null;
        return switch (profile.getSleepSchedule()) {
            case early_bird -> "EarlyBird";
            case normal -> "Regular";
            case night_owl -> "NightOwl";
        };
    }

    private String convertSmoking(UserProfile profile) {
        return profile.getSmoking() == null ? null : (profile.getSmoking() ? "Yes" : "No");
    }

    private String convertCleanliness(UserProfile profile) {
        if (profile.getCleanliness() == null) return null;
        return switch (profile.getCleanliness()) {
            case low -> "Low";
            case medium -> "Average";
            case high -> "High";
        };
    }
}
