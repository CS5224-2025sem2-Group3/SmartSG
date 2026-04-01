package com.nus.cs5224.smartsg.entity;

import lombok.Data;

@Data
public class GroupMember {
    private int groupId;
    private long userId;
    private String role;
}
