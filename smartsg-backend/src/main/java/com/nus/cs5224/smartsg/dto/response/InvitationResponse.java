package com.nus.cs5224.smartsg.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvitationResponse {
    private int id;
    private int groupId;
    private int listingId;
    private String listingTitle;
    private String fromUserName;
    private String status;
}
