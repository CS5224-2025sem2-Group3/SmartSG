package com.nus.cs5224.smartsg.service;

import com.nus.cs5224.smartsg.dto.response.RecommendationResponse;
import com.nus.cs5224.smartsg.entity.*;
import com.nus.cs5224.smartsg.mapper.GroupMapper;
import com.nus.cs5224.smartsg.mapper.MatchMapper;
import com.nus.cs5224.smartsg.service.serviceImpl.MatchingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchingServiceImplTest {

    @Mock
    private MatchMapper matchMapper;

    @Mock
    private GroupMapper groupMapper;

    @InjectMocks
    private MatchingServiceImpl matchingService;

    private Group sampleGroup;

    @BeforeEach
    void setUp() {
        sampleGroup = new Group();
        sampleGroup.setGroupId(10);
        sampleGroup.setListingId(1L);
        sampleGroup.setStatus("recruiting");
        sampleGroup.setMaxPeople(3);
        sampleGroup.setCurPeople(1);
    }

    private UserProfile createCandidate(Long userId, String name, Integer budget,
                                         LocalDate moveIn, Integer lease,
                                         SleepSchedule sleep, Boolean smoking,
                                         Cleanliness cleanliness) {
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setUserName(name);
        profile.setMaxBudget(budget);
        profile.setMoveInWindow(moveIn);
        profile.setLeasePreference(lease);
        profile.setSleepSchedule(sleep);
        profile.setSmoking(smoking);
        profile.setCleanliness(cleanliness);
        return profile;
    }

    @Nested
    @DisplayName("getRecommendations")
    class GetRecommendationsTests {

        @Test
        @DisplayName("M1: 无筛选获取推荐 - 返回结果按score降序")
        void getRecommendations_noFilter() {
            when(groupMapper.findById(10)).thenReturn(sampleGroup);

            UserProfile c1 = createCandidate(1L, "Bob", 2000, LocalDate.of(2026, 6, 1),
                    12, SleepSchedule.night_owl, false, Cleanliness.high);
            UserProfile c2 = createCandidate(2L, "Carol", 1500, null,
                    6, SleepSchedule.early_bird, true, Cleanliness.low);

            when(matchMapper.findCandidates(10, 100L)).thenReturn(List.of(c1, c2));

            List<RecommendationResponse> results = matchingService.getRecommendations(
                    10, 100L, null, null, null, null, null, null, null);

            assertEquals(2, results.size());
            // With no filters, all scores should be 0
            assertEquals(0, results.get(0).getMatchScore());
        }

        @Test
        @DisplayName("M2: 群组不存在 - 返回404")
        void getRecommendations_groupNotFound() {
            when(groupMapper.findById(999)).thenReturn(null);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> matchingService.getRecommendations(999, 100L,
                            null, null, null, null, null, null, null));

            assertEquals(404, ex.getStatusCode().value());
        }

        @Test
        @DisplayName("M3: budget匹配加分 (+1)")
        void getRecommendations_budgetMatch() {
            when(groupMapper.findById(10)).thenReturn(sampleGroup);

            UserProfile c1 = createCandidate(1L, "HighBudget", 2000, null,
                    null, null, null, null);
            UserProfile c2 = createCandidate(2L, "LowBudget", 500, null,
                    null, null, null, null);

            when(matchMapper.findCandidates(10, 100L)).thenReturn(List.of(c1, c2));

            List<RecommendationResponse> results = matchingService.getRecommendations(
                    10, 100L, 1500, null, null, null, null, null, null);

            // c1 (budget 2000 >= 1500) gets +1, c2 (500 < 1500) gets 0
            assertEquals(1, results.get(0).getMatchScore());
            assertEquals("HighBudget", results.get(0).getName());
            assertEquals(0, results.get(1).getMatchScore());
        }

        @Test
        @DisplayName("M4: moveInWindow匹配加分 (+2)")
        void getRecommendations_moveInWindowMatch() {
            when(groupMapper.findById(10)).thenReturn(sampleGroup);

            UserProfile c1 = createCandidate(1L, "CloseDate", null,
                    LocalDate.of(2026, 6, 15), null, null, null, null);
            UserProfile c2 = createCandidate(2L, "FarDate", null,
                    LocalDate.of(2027, 1, 1), null, null, null, null);

            when(matchMapper.findCandidates(10, 100L)).thenReturn(List.of(c1, c2));

            List<RecommendationResponse> results = matchingService.getRecommendations(
                    10, 100L, null, "2026-06-01", null, null, null, null, null);

            // c1 (15 days diff <= 30) gets +2, c2 (far away) gets 0
            assertEquals(2, results.get(0).getMatchScore());
            assertEquals("CloseDate", results.get(0).getName());
        }

        @Test
        @DisplayName("M5: leasePreference匹配 (+1)")
        void getRecommendations_leaseMatch() {
            when(groupMapper.findById(10)).thenReturn(sampleGroup);

            UserProfile c1 = createCandidate(1L, "Match", null, null,
                    12, null, null, null);
            UserProfile c2 = createCandidate(2L, "NoMatch", null, null,
                    6, null, null, null);

            when(matchMapper.findCandidates(10, 100L)).thenReturn(List.of(c1, c2));

            List<RecommendationResponse> results = matchingService.getRecommendations(
                    10, 100L, null, null, 12, null, null, null, null);

            assertEquals(1, results.get(0).getMatchScore());
            assertEquals("Match", results.get(0).getName());
        }

        @Test
        @DisplayName("M6: sleepHabit匹配 (+1)")
        void getRecommendations_sleepMatch() {
            when(groupMapper.findById(10)).thenReturn(sampleGroup);

            UserProfile c1 = createCandidate(1L, "NightOwl", null, null,
                    null, SleepSchedule.night_owl, null, null);

            when(matchMapper.findCandidates(10, 100L)).thenReturn(List.of(c1));

            List<RecommendationResponse> results = matchingService.getRecommendations(
                    10, 100L, null, null, null, "night_owl", null, null, null);

            assertEquals(1, results.get(0).getMatchScore());
        }

        @Test
        @DisplayName("M7: smoking匹配 (+1)")
        void getRecommendations_smokingMatch() {
            when(groupMapper.findById(10)).thenReturn(sampleGroup);

            UserProfile c1 = createCandidate(1L, "NonSmoker", null, null,
                    null, null, false, null);

            when(matchMapper.findCandidates(10, 100L)).thenReturn(List.of(c1));

            List<RecommendationResponse> results = matchingService.getRecommendations(
                    10, 100L, null, null, null, null, null, "false", null);

            assertEquals(1, results.get(0).getMatchScore());
        }

        @Test
        @DisplayName("M8: cleanliness匹配 (+1)")
        void getRecommendations_cleanlinessMatch() {
            when(groupMapper.findById(10)).thenReturn(sampleGroup);

            UserProfile c1 = createCandidate(1L, "Clean", null, null,
                    null, null, null, Cleanliness.high);

            when(matchMapper.findCandidates(10, 100L)).thenReturn(List.of(c1));

            List<RecommendationResponse> results = matchingService.getRecommendations(
                    10, 100L, null, null, null, null, null, null, "high");

            assertEquals(1, results.get(0).getMatchScore());
        }

        @Test
        @DisplayName("M9: 所有条件匹配 - 满分7分")
        void getRecommendations_perfectMatch() {
            when(groupMapper.findById(10)).thenReturn(sampleGroup);

            UserProfile perfect = createCandidate(1L, "Perfect", 2000,
                    LocalDate.of(2026, 6, 10), 12,
                    SleepSchedule.night_owl, false, Cleanliness.high);

            when(matchMapper.findCandidates(10, 100L)).thenReturn(List.of(perfect));

            List<RecommendationResponse> results = matchingService.getRecommendations(
                    10, 100L, 1500, "2026-06-01", 12,
                    "night_owl", null, "false", "high");

            assertEquals(7, results.get(0).getMatchScore());
        }

        @Test
        @DisplayName("M10: 无效日期格式 - 忽略不报错")
        void getRecommendations_invalidDate() {
            when(groupMapper.findById(10)).thenReturn(sampleGroup);

            UserProfile c1 = createCandidate(1L, "Bob", null,
                    LocalDate.of(2026, 6, 1), null, null, null, null);

            when(matchMapper.findCandidates(10, 100L)).thenReturn(List.of(c1));

            // Should not throw despite invalid date
            assertDoesNotThrow(() -> {
                List<RecommendationResponse> results = matchingService.getRecommendations(
                        10, 100L, null, "invalid-date", null, null, null, null, null);
                assertEquals(1, results.size());
                assertEquals(0, results.get(0).getMatchScore()); // no date bonus
            });
        }

        @Test
        @DisplayName("M12: 最多返回10个候选人")
        void getRecommendations_limitTo10() {
            when(groupMapper.findById(10)).thenReturn(sampleGroup);

            // Create 15 candidates
            List<UserProfile> candidates = new ArrayList<>();
            IntStream.rangeClosed(1, 15).forEach(i -> {
                candidates.add(createCandidate((long) i, "User" + i, null, null,
                        null, null, null, null));
            });

            when(matchMapper.findCandidates(10, 100L)).thenReturn(candidates);

            List<RecommendationResponse> results = matchingService.getRecommendations(
                    10, 100L, null, null, null, null, null, null, null);

            assertEquals(10, results.size());
        }
    }
}
