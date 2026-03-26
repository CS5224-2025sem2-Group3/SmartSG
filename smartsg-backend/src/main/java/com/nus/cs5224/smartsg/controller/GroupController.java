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
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    @Autowired
    private GroupService groupService;

    /**
     * Create a group
     * POST /api/groups
     */
    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@RequestBody CreateGroupRequest request,
                                                     HttpServletRequest httpRequest) {
        Long currentUserId = (Long) httpRequest.getAttribute("currentUserId");
        GroupResponse group = groupService.createGroup(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(group);
    }

    /**
     * Get group details
     * GET /api/groups/{groupId}
     */
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getGroup(@PathVariable int groupId) {
        GroupResponse group = groupService.getGroup(groupId);
        return ResponseEntity.ok(group);
    }

    /**
     * Join a group
     * POST /api/groups/{groupId}/join
     */
    @PostMapping("/{groupId}/join")
    public ResponseEntity<Map<String, Boolean>> joinGroup(@PathVariable int groupId,
                                                          HttpServletRequest httpRequest) {
        Long currentUserId = (Long) httpRequest.getAttribute("currentUserId");
        groupService.joinGroup(currentUserId, groupId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    /**
     * Leave a group
     * POST /api/groups/{groupId}/leave
     */
    @PostMapping("/{groupId}/leave")
    public ResponseEntity<Map<String, Boolean>> leaveGroup(@PathVariable int groupId,
                                                           HttpServletRequest httpRequest) {
        Long currentUserId = (Long) httpRequest.getAttribute("currentUserId");
        groupService.leaveGroup(currentUserId, groupId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
}
