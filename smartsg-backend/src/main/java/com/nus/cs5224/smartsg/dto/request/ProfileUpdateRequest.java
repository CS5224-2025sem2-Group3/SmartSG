package com.nus.cs5224.smartsg.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ProfileUpdateRequest {
    private Integer budgetMax;            // 每位租客的最高预算
    private LocalDate moveInWindow;        // 可入住日期 (ISO 格式)
    private Integer leasePreference;       // 租期偏好（月）
}