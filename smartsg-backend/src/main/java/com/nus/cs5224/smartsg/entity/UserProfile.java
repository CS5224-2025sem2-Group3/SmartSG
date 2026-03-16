package com.nus.cs5224.smartsg.entity;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UserProfile {
    private Long userId;
    private User user;
    private LocalDate moveInWindow;
    private Integer leasePreference;
    private SleepSchedule sleepSchedule;
    private Cleanliness cleanliness;
    private Boolean smoking;
    private Gender gender;
}