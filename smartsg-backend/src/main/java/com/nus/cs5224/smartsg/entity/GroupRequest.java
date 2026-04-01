package com.nus.cs5224.smartsg.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GroupRequest {
    private int requestId;
    private int groupId;
    private Long senderId;
    private Long receiverId;
    private String status;  // pending, accepted, rejected
    private LocalDateTime createdAt;

    // Extra fields populated by JOIN queries in InvitationMapper
    private int listingId;
    private String listingTitle;
    private String senderName;
}
