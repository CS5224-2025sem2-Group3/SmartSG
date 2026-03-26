package com.nus.cs5224.smartsg.entity;

import lombok.Data;

@Data
public class Group {
    private int groupId;
    private long listingId;
    private String status;
    private int maxPeople;
    private int curPeople; // Can be replaced by SQL count and removed in the future
}
