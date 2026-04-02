package com.nus.cs5224.smartsg.dto.response;

import lombok.Data;

@Data
public class GroupMemberResponse {
    private Long userId;
    private String name;
    private String role;
    private Integer budgetMax;
    private String moveInWindow;
    private Integer leasePreference;
}
