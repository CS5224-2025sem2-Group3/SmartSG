package com.nus.cs5224.smartsg.service;

import com.nus.cs5224.smartsg.dto.request.SendInvitationRequest;
import com.nus.cs5224.smartsg.dto.response.InvitationResponse;

import java.util.List;

public interface InvitationService {
    List<InvitationResponse> getMyInvitations(Long userId);
    void sendInvitation(SendInvitationRequest request, Long senderId);
    void acceptInvitation(int invitationId, Long userId);
    void rejectInvitation(int invitationId, Long userId);
}
