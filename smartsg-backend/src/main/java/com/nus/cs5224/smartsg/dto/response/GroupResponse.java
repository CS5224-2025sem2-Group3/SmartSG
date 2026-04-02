package com.nus.cs5224.smartsg.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class GroupResponse {
    private int groupId;
    private int listingId;
    private String listingTitle;
    private String status;
    private int requiredPeople;  // = maxPeople (alias per API doc)
    private int curPeople;
    private Long leaderUserId;
    private String currentUserRole;
    private boolean currentUserIsLeader;
    private List<GroupMemberResponse> members;
}
