package com.nus.cs5224.smartsg.dto.response;

import lombok.Data;

@Data
public class ProfileResponse {
    private Integer budgetMax;           // max_budget
    private String moveInWindow;          // ISO 日期字符串，如 "2026-08-05"
    private Integer leasePreference;      // lease_preference
    private String sleepHabit;            // 转换后：Early Bird / Regular / Night Owl
    private String smoking;                // "Yes Smoking" / "No Smoking"
    private String cleanliness;            // "Low" / "Average" / "High"
    private String gender;                 // 性别
}