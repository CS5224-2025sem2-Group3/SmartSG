package com.nus.cs5224.smartsg.service.serviceImpl;

import com.nus.cs5224.smartsg.dto.request.SendInvitationRequest;
import com.nus.cs5224.smartsg.dto.response.InvitationResponse;
import com.nus.cs5224.smartsg.entity.GroupRequest;
import com.nus.cs5224.smartsg.mapper.InvitationMapper;
import com.nus.cs5224.smartsg.service.GroupService;
import com.nus.cs5224.smartsg.service.InvitationService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvitationServiceImpl implements InvitationService {

    @Autowired
    private InvitationMapper invitationMapper;

    @Autowired
    private GroupService groupService;

    // GET /api/invitations/me - get all invitations received by current user
    @Override
    public List<InvitationResponse> getMyInvitations(Long userId) {
        List<GroupRequest> requests = invitationMapper.findByReceiverId(userId);
        return requests.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // POST /api/invitations - send an invitation
    @Override
    public void sendInvitation(SendInvitationRequest request, Long senderId) {
        GroupRequest groupRequest = new GroupRequest();
        groupRequest.setGroupId(request.getGroupId());
        groupRequest.setSenderId(senderId);
        groupRequest.setReceiverId(request.getCandidateUserId());
        invitationMapper.insertRequest(groupRequest);
    }

    // POST /api/invitations/:id/accept
    @Override
    @Transactional
    public void acceptInvitation(int invitationId, Long userId) {
        GroupRequest request = getAndValidate(invitationId, userId);
        if (!"pending".equals(request.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invitation is no longer pending");
        }

        groupService.joinGroup(userId, request.getGroupId());
        invitationMapper.updateStatus(invitationId, "accepted");
    }

    // POST /api/invitations/:id/reject
    @Override
    public void rejectInvitation(int invitationId, Long userId) {
        GroupRequest request = getAndValidate(invitationId, userId);
        if (!"pending".equals(request.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invitation is no longer pending");
        }
        invitationMapper.updateStatus(invitationId, "rejected");
    }

    private GroupRequest getAndValidate(int invitationId, Long userId) {
        GroupRequest request = invitationMapper.findById(invitationId);
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found");
        }
        if (!userId.equals(request.getReceiverId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your invitation");
        }
        return request;
    }

    // The InvitationMapper's findByReceiverId query joins listing and sender info
    // using column aliases — these are passed back via the raw GroupRequest fields.
    // We need the joined columns, so we use a custom approach with extra transient fields.
    private InvitationResponse toResponse(GroupRequest r) {
        InvitationResponse resp = new InvitationResponse();
        resp.setId(r.getRequestId());
        resp.setGroupId(r.getGroupId());
        // These come from the JOIN columns in InvitationMapper
        resp.setListingId(r.getListingId());
        resp.setListingTitle(r.getListingTitle());
        resp.setFromUserName(r.getSenderName());
        resp.setStatus(r.getStatus());
        return resp;
    }
}
