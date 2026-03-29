package com.nus.cs5224.smartsg.controller;

import com.nus.cs5224.smartsg.dto.response.RecommendationResponse;
import com.nus.cs5224.smartsg.service.MatchingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class MatchingController {

    @Autowired
    private MatchingService matchingService;

    /**
     * GET /api/groups/:listingId/recommendations
     * Returns ranked list of recommended roommates for the given group.
     * All query params are optional filters that affect the match score.
     */
    @GetMapping("/{groupId}/recommendations")
    public List<RecommendationResponse> getRecommendations(
            @PathVariable int groupId,
            @RequestParam(required = false) Integer budgetMax,
            @RequestParam(required = false) String moveInWindow,
            @RequestParam(required = false) Integer leasePreference,
            @RequestParam(required = false) String sleepHabit,
            @RequestParam(required = false) String studyPreference,
            @RequestParam(required = false) String smoking,
            @RequestParam(required = false) String cleanliness,
            HttpServletRequest httpRequest) {

        Long currentUserId = (Long) httpRequest.getAttribute("currentUserId");
        return matchingService.getRecommendations(
                groupId, currentUserId,
                budgetMax, moveInWindow, leasePreference,
                sleepHabit, studyPreference, smoking, cleanliness);
    }
}
