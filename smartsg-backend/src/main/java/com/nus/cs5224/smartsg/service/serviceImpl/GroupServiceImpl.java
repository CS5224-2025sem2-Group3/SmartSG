package com.nus.cs5224.smartsg.service.serviceImpl;

import com.nus.cs5224.smartsg.dto.request.CreateGroupRequest;
import com.nus.cs5224.smartsg.dto.response.GroupMemberResponse;
import com.nus.cs5224.smartsg.dto.response.GroupResponse;
import com.nus.cs5224.smartsg.entity.Group;
import com.nus.cs5224.smartsg.entity.GroupMember;
import com.nus.cs5224.smartsg.entity.Listing;
import com.nus.cs5224.smartsg.mapper.GroupMapper;
import com.nus.cs5224.smartsg.mapper.ListingMapper;
import com.nus.cs5224.smartsg.service.GroupService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class GroupServiceImpl implements GroupService {

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private ListingMapper listingMapper;

    // Create a group: creator is automatically added as LEADER
    @Override
    @Transactional
    public GroupResponse createGroup(CreateGroupRequest request, Long userId) {
        Listing listing = listingMapper.findById(request.getListingId());
        if (listing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found");
        }

        Group group = new Group();
        group.setListingId(request.getListingId());
        group.setStatus("recruiting");
        group.setMaxPeople(listing.getTotalTenants());
        group.setCurPeople(1);
        groupMapper.insertGroup(group);

        GroupMember member = new GroupMember();
        member.setGroupId(group.getGroupId());
        member.setUserId(userId);
        member.setRole("leader");
        groupMapper.insertMember(member);

        return toGroupResponse(group, listing.getTitle(), userId);
    }

    // Join a group
    @Override
    @Transactional
    public void joinGroup(Long userId, int groupId) {
        Group group = groupMapper.findById(groupId);
        if (group == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found");
        }
        if (group.getCurPeople() >= group.getMaxPeople()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group is full");
        }
        GroupMember existing = groupMapper.findMember(groupId, userId);
        if (existing != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already a member of this group");
        }

        GroupMember member = new GroupMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setRole("member");
        groupMapper.insertMember(member);
        groupMapper.updateCurPeople(groupId, group.getCurPeople() + 1);
    }

    // Leave a group
    @Override
    @Transactional
    public void leaveGroup(Long userId, int groupId) {
        Group group = groupMapper.findById(groupId);
        if (group == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found");
        }
        GroupMember member = groupMapper.findMember(groupId, userId);
        if (member == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a member of this group");
        }
        groupMapper.deleteMember(groupId, userId);
        groupMapper.updateCurPeople(groupId, group.getCurPeople() - 1);
    }

    // Get group details
    @Override
    public GroupResponse getGroup(int groupId, Long currentUserId) {
        Group group = groupMapper.findById(groupId);
        if (group == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found");
        }
        Listing listing = listingMapper.findById((long) group.getListingId());
        String title = listing != null ? listing.getTitle() : null;
        return toGroupResponse(group, title, currentUserId);
    }

    // Get all groups the current user is in
    @Override
    public List<GroupResponse> getMyGroups(Long userId) {
        List<Group> groups = groupMapper.findByUserId(userId);
        return groups.stream()
                .map(g -> {
                    Listing listing = listingMapper.findById((long) g.getListingId());
                    String title = listing != null ? listing.getTitle() : null;
                    return toGroupResponse(g, title, userId);
                })
                .collect(Collectors.toList());
    }

    // Get all groups for a given listing
    @Override
    public List<GroupResponse> getGroupsByListing(Long listingId, Long currentUserId) {
        List<Group> groups = groupMapper.findByListingId(listingId);
        Listing listing = listingMapper.findById(listingId);
        String title = listing != null ? listing.getTitle() : null;
        return groups.stream()
                .map(g -> toGroupResponse(g, title, currentUserId))
                .collect(Collectors.toList());
    }

    // Delete a group (only the leader can delete)
    @Override
    @Transactional
    public void deleteGroup(int groupId, Long userId) {
        Group group = groupMapper.findById(groupId);
        if (group == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found");
        }
        GroupMember member = groupMapper.findMember(groupId, userId);
        if (member == null || !"leader".equals(member.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the group leader can delete the group");
        }
        groupMapper.deleteAllMembersByGroupId(groupId);
        groupMapper.deleteGroup(groupId);
    }

    // Confirm group (leader sets status to closed)
    @Override
    @Transactional
    public void confirmGroup(int groupId, Long userId) {
        Group group = groupMapper.findById(groupId);
        if (group == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found");
        }
        GroupMember member = groupMapper.findMember(groupId, userId);
        if (member == null || !"leader".equals(member.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the group leader can confirm the group");
        }
        groupMapper.updateStatus(groupId, "closed");
    }

    // Entity → Response DTO
    private GroupResponse toGroupResponse(Group group, String listingTitle, Long currentUserId) {
        List<GroupMember> members = groupMapper.findMembersByGroupId(group.getGroupId());

        GroupResponse response = new GroupResponse();
        response.setGroupId(group.getGroupId());
        response.setListingId((int) group.getListingId());
        response.setListingTitle(listingTitle);
        response.setStatus(group.getStatus());
        response.setRequiredPeople(group.getMaxPeople());
        response.setCurPeople(group.getCurPeople());
        response.setMembers(
                members.stream()
                        .map(this::toGroupMemberResponse)
                        .collect(Collectors.toList())
        );

        GroupMember leader = members.stream()
                .filter(member -> "leader".equals(member.getRole()))
                .findFirst()
                .orElse(null);
        response.setLeaderUserId(leader != null ? leader.getUserId() : null);

        GroupMember currentUserMember = currentUserId == null
                ? null
                : members.stream()
                        .filter(member -> Objects.equals(member.getUserId(), currentUserId))
                        .findFirst()
                        .orElse(null);
        response.setCurrentUserRole(currentUserMember != null ? currentUserMember.getRole() : null);
        response.setCurrentUserIsLeader(currentUserMember != null && "leader".equals(currentUserMember.getRole()));

        return response;
    }

    private GroupMemberResponse toGroupMemberResponse(GroupMember member) {
        GroupMemberResponse response = new GroupMemberResponse();
        response.setUserId(member.getUserId());
        response.setName(member.getName());
        response.setRole(member.getRole());
        response.setBudgetMax(member.getBudgetMax());
        response.setMoveInWindow(member.getMoveInWindow() != null ? member.getMoveInWindow().toString() : null);
        response.setLeasePreference(member.getLeasePreference());
        return response;
    }
}
