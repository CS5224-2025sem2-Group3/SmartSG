package com.nus.cs5224.smartsg.service;

import com.nus.cs5224.smartsg.dto.request.ProfileUpdateRequest;
import com.nus.cs5224.smartsg.dto.response.ProfileResponse;
import com.nus.cs5224.smartsg.entity.*;
import com.nus.cs5224.smartsg.mapper.ProfileMapper;
import com.nus.cs5224.smartsg.service.serviceImpl.ProfileServiceImpl;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock
    private ProfileMapper profileMapper;

    @InjectMocks
    private ProfileServiceImpl profileService;

    private UserProfile existingProfile;

    @BeforeEach
    void setUp() {
        existingProfile = new UserProfile();
        existingProfile.setUserId(1L);
        existingProfile.setMaxBudget(2000);
        existingProfile.setMoveInWindow(LocalDate.of(2026, 6, 1));
        existingProfile.setLeasePreference(12);
        existingProfile.setSleepSchedule(SleepSchedule.early_bird);
        existingProfile.setCleanliness(Cleanliness.high);
        existingProfile.setSmoking(false);
        existingProfile.setGender(Gender.male);
    }

    // ========== Get or Create Profile Tests ==========

    @Nested
    @DisplayName("getOrCreateProfile")
    class GetOrCreateProfileTests {

        @Test
        @DisplayName("P1: 首次获取 - 自动创建空profile")
        void getOrCreateProfile_newUser() {
            when(profileMapper.findByUserId(999L)).thenReturn(null);

            UserProfile profile = profileService.getOrCreateProfile(999L);

            assertNotNull(profile);
            assertEquals(999L, profile.getUserId());
            verify(profileMapper).insert(any(UserProfile.class));
        }

        @Test
        @DisplayName("P2: 已有profile - 直接返回")
        void getOrCreateProfile_existingUser() {
            when(profileMapper.findByUserId(1L)).thenReturn(existingProfile);

            UserProfile profile = profileService.getOrCreateProfile(1L);

            assertEquals(existingProfile, profile);
            verify(profileMapper, never()).insert(any());
        }
    }

    // ========== Update Profile Tests ==========

    @Nested
    @DisplayName("updateProfile")
    class UpdateProfileTests {

        @Test
        @DisplayName("P3: 更新全部字段")
        void updateProfile_allFields() {
            when(profileMapper.findByUserId(1L)).thenReturn(existingProfile);

            ProfileUpdateRequest request = new ProfileUpdateRequest();
            request.setBudgetMax(3000);
            request.setMoveInWindow(LocalDate.of(2026, 7, 1));
            request.setLeasePreference(6);
            request.setSleepHabit("NightOwl");
            request.setCleanliness("Low");
            request.setSmoking("Yes");
            request.setGender("Female");

            assertDoesNotThrow(() -> profileService.updateProfile(1L, request));

            assertEquals(3000, existingProfile.getMaxBudget());
            assertEquals(LocalDate.of(2026, 7, 1), existingProfile.getMoveInWindow());
            assertEquals(6, existingProfile.getLeasePreference());
            assertEquals(SleepSchedule.night_owl, existingProfile.getSleepSchedule());
            assertEquals(Cleanliness.low, existingProfile.getCleanliness());
            assertTrue(existingProfile.getSmoking());
            assertEquals(Gender.female, existingProfile.getGender());

            verify(profileMapper).update(existingProfile);
        }

        @Test
        @DisplayName("P4: 部分更新 - 只修改传入的字段")
        void updateProfile_partialUpdate() {
            when(profileMapper.findByUserId(1L)).thenReturn(existingProfile);

            ProfileUpdateRequest request = new ProfileUpdateRequest();
            request.setBudgetMax(5000);
            // all other fields are null -> should not change

            profileService.updateProfile(1L, request);

            assertEquals(5000, existingProfile.getMaxBudget());
            // Other fields remain unchanged
            assertEquals(SleepSchedule.early_bird, existingProfile.getSleepSchedule());
            assertEquals(Cleanliness.high, existingProfile.getCleanliness());
            assertFalse(existingProfile.getSmoking());
        }

        @Test
        @DisplayName("P5: 无效sleepHabit - 返回400")
        void updateProfile_invalidSleepHabit() {
            when(profileMapper.findByUserId(1L)).thenReturn(existingProfile);

            ProfileUpdateRequest request = new ProfileUpdateRequest();
            request.setSleepHabit("InvalidValue");

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> profileService.updateProfile(1L, request));

            assertEquals(400, ex.getStatusCode().value());
            assertTrue(ex.getReason().contains("Invalid sleepHabit"));
        }

        @Test
        @DisplayName("P6: 无效cleanliness - 返回400")
        void updateProfile_invalidCleanliness() {
            when(profileMapper.findByUserId(1L)).thenReturn(existingProfile);

            ProfileUpdateRequest request = new ProfileUpdateRequest();
            request.setCleanliness("VeryHigh");

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> profileService.updateProfile(1L, request));

            assertEquals(400, ex.getStatusCode().value());
            assertTrue(ex.getReason().contains("Invalid cleanliness"));
        }

        @Test
        @DisplayName("P7: 无效smoking - 返回400")
        void updateProfile_invalidSmoking() {
            when(profileMapper.findByUserId(1L)).thenReturn(existingProfile);

            ProfileUpdateRequest request = new ProfileUpdateRequest();
            request.setSmoking("Sometimes");

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> profileService.updateProfile(1L, request));

            assertEquals(400, ex.getStatusCode().value());
            assertTrue(ex.getReason().contains("Invalid smoking"));
        }

        @Test
        @DisplayName("P8: 无效gender - 返回400")
        void updateProfile_invalidGender() {
            when(profileMapper.findByUserId(1L)).thenReturn(existingProfile);

            ProfileUpdateRequest request = new ProfileUpdateRequest();
            request.setGender("Other");

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> profileService.updateProfile(1L, request));

            assertEquals(400, ex.getStatusCode().value());
            assertTrue(ex.getReason().contains("Invalid gender"));
        }

        @Test
        @DisplayName("P9: 三种sleepHabit枚举值都能正常解析")
        void updateProfile_allSleepHabitValues() {
            when(profileMapper.findByUserId(1L)).thenReturn(existingProfile);

            // Test EarlyBird
            ProfileUpdateRequest req1 = new ProfileUpdateRequest();
            req1.setSleepHabit("EarlyBird");
            profileService.updateProfile(1L, req1);
            assertEquals(SleepSchedule.early_bird, existingProfile.getSleepSchedule());

            // Test Regular
            ProfileUpdateRequest req2 = new ProfileUpdateRequest();
            req2.setSleepHabit("Regular");
            profileService.updateProfile(1L, req2);
            assertEquals(SleepSchedule.normal, existingProfile.getSleepSchedule());

            // Test NightOwl
            ProfileUpdateRequest req3 = new ProfileUpdateRequest();
            req3.setSleepHabit("NightOwl");
            profileService.updateProfile(1L, req3);
            assertEquals(SleepSchedule.night_owl, existingProfile.getSleepSchedule());
        }
    }

    // ========== Convert to Response Tests ==========

    @Nested
    @DisplayName("convertToResponse")
    class ConvertToResponseTests {

        @Test
        @DisplayName("完整profile转换为response")
        void convertToResponse_allFields() {
            ProfileResponse response = profileService.convertToResponse(existingProfile);

            assertEquals(2000, response.getBudgetMax());
            assertEquals("2026-06-01", response.getMoveInWindow());
            assertEquals(12, response.getLeasePreference());
            assertEquals("EarlyBird", response.getSleepHabit());
            assertEquals("High", response.getCleanliness());
            assertEquals("No", response.getSmoking());
            assertEquals("Male", response.getGender());
        }

        @Test
        @DisplayName("空profile转换 - 所有字段为null")
        void convertToResponse_nullFields() {
            UserProfile emptyProfile = new UserProfile();
            emptyProfile.setUserId(2L);

            ProfileResponse response = profileService.convertToResponse(emptyProfile);

            assertNull(response.getBudgetMax());
            assertNull(response.getMoveInWindow());
            assertNull(response.getLeasePreference());
            assertNull(response.getSleepHabit());
            assertNull(response.getCleanliness());
            assertNull(response.getSmoking());
            assertNull(response.getGender());
        }
    }
}
