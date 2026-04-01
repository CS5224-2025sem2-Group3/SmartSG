package com.nus.cs5224.smartsg.dto.response;

import lombok.Data;

@Data
public class GroupResponse {
    private int groupId;
    private int listingId;
    private String listingTitle;
    private String status;
    private int requiredPeople;  // = maxPeople (alias per API doc)
    private int curPeople;
}
