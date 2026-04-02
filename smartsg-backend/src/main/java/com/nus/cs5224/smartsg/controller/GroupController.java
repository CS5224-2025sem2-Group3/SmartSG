package com.nus.cs5224.smartsg.controller;

import com.nus.cs5224.smartsg.dto.request.CreateGroupRequest;
import com.nus.cs5224.smartsg.dto.response.GroupResponse;
import com.nus.cs5224.smartsg.service.GroupService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    @Autowired
    private GroupService groupService;

    // GET /api/groups/me - get all groups the current user is in
    @GetMapping("/me")
    public ResponseEntity<List<GroupResponse>> getMyGroups(HttpServletRequest httpRequest) {
        Long currentUserId = (Long) httpRequest.getAttribute("currentUserId");
        return ResponseEntity.ok(groupService.getMyGroups(currentUserId));
    }

    // GET /api/groups/by-listing/:listingId - get all groups for a listing
    @GetMapping("/by-listing/{listingId}")
    public ResponseEntity<List<GroupResponse>> getGroupsByListing(@PathVariable Long listingId,
                                                                  HttpServletRequest httpRequest) {
        Long currentUserId = (Long) httpRequest.getAttribute("currentUserId");
        return ResponseEntity.ok(groupService.getGroupsByListing(listingId, currentUserId));
    }

    // GET /api/groups/:groupId - get group details
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getGroup(@PathVariable int groupId,
                                                  HttpServletRequest httpRequest) {
        Long currentUserId = (Long) httpRequest.getAttribute("currentUserId");
        GroupResponse group = groupService.getGroup(groupId, currentUserId);
        return ResponseEntity.ok(group);
    }

    // POST /api/groups - create a group
    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@RequestBody CreateGroupRequest request,
                                                     HttpServletRequest httpRequest) {
        Long currentUserId = (Long) httpRequest.getAttribute("currentUserId");
        GroupResponse group = groupService.createGroup(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(group);
    }

    // DELETE /api/groups/:groupId - delete a group (leader only)
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Map<String, Boolean>> deleteGroup(@PathVariable int groupId,
                                                            HttpServletRequest httpRequest) {
        Long currentUserId = (Long) httpRequest.getAttribute("currentUserId");
        groupService.deleteGroup(groupId, currentUserId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    // POST /api/groups/:groupId/join - join a group
    @PostMapping("/{groupId}/join")
    public ResponseEntity<Map<String, Boolean>> joinGroup(@PathVariable int groupId,
                                                          HttpServletRequest httpRequest) {
        Long currentUserId = (Long) httpRequest.getAttribute("currentUserId");
        groupService.joinGroup(currentUserId, groupId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    // POST /api/groups/:groupId/leave - leave a group
    @PostMapping("/{groupId}/leave")
    public ResponseEntity<Map<String, Boolean>> leaveGroup(@PathVariable int groupId,
                                                           HttpServletRequest httpRequest) {
        Long currentUserId = (Long) httpRequest.getAttribute("currentUserId");
        groupService.leaveGroup(currentUserId, groupId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    // POST /api/groups/:groupId/confirm - confirm group (leader sets status to closed)
    @PostMapping("/{groupId}/confirm")
    public ResponseEntity<Map<String, Boolean>> confirmGroup(@PathVariable int groupId,
                                                             HttpServletRequest httpRequest) {
        Long currentUserId = (Long) httpRequest.getAttribute("currentUserId");
        groupService.confirmGroup(groupId, currentUserId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
}
