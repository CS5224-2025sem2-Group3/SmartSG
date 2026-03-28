package com.nus.cs5224.smartsg.controller;

import com.nus.cs5224.smartsg.dto.request.SendInvitationRequest;
import com.nus.cs5224.smartsg.dto.response.InvitationResponse;
import com.nus.cs5224.smartsg.service.InvitationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invitations")
public class InvitationController {

    @Autowired
    private InvitationService invitationService;

    // GET /api/invitations/me - get all invitations received by current user
    @GetMapping("/me")
    public List<InvitationResponse> getMyInvitations(HttpServletRequest httpRequest) {
        Long currentUserId = (Long) httpRequest.getAttribute("currentUserId");
        return invitationService.getMyInvitations(currentUserId);
    }

    // POST /api/invitations - send an invitation {groupId, candidateUserId}
    @PostMapping
    public Map<String, Boolean> sendInvitation(@RequestBody SendInvitationRequest request,
                                               HttpServletRequest httpRequest) {
        Long currentUserId = (Long) httpRequest.getAttribute("currentUserId");
        invitationService.sendInvitation(request, currentUserId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", true);
        return response;
    }

    // POST /api/invitations/:id/accept
    @PostMapping("/{id}/accept")
    public Map<String, Boolean> acceptInvitation(@PathVariable int id,
                                                 HttpServletRequest httpRequest) {
        Long currentUserId = (Long) httpRequest.getAttribute("currentUserId");
        invitationService.acceptInvitation(id, currentUserId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", true);
        return response;
    }

    // POST /api/invitations/:id/reject
    @PostMapping("/{id}/reject")
    public Map<String, Boolean> rejectInvitation(@PathVariable int id,
                                                 HttpServletRequest httpRequest) {
        Long currentUserId = (Long) httpRequest.getAttribute("currentUserId");
        invitationService.rejectInvitation(id, currentUserId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", true);
        return response;
    }
}
