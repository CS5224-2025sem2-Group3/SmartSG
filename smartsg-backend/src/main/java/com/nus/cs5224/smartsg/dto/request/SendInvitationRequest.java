package com.nus.cs5224.smartsg.dto.request;

import lombok.Data;

@Data
public class SendInvitationRequest {
    private int groupId;
    private Long candidateUserId;
}
