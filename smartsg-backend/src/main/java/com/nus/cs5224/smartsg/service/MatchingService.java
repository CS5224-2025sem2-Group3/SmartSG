package com.nus.cs5224.smartsg.service;

import com.nus.cs5224.smartsg.dto.response.RecommendationResponse;

import java.util.List;

public interface MatchingService {
    List<RecommendationResponse> getRecommendations(
            int groupId,
            Long currentUserId,
            Integer budgetMax,
            String moveInWindow,
            Integer leasePreference,
            String sleepHabit,
            String studyPreference,
            String smoking,
            String cleanliness
    );
}
