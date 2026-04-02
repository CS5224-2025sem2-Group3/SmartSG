package com.nus.cs5224.smartsg.entity;

import lombok.Data;
import java.time.LocalDate;

@Data
public class GroupMember {
    private int groupId;
    private long userId;
    private String role;
    private String name;
    private Integer budgetMax;
    private LocalDate moveInWindow;
    private Integer leasePreference;
}
