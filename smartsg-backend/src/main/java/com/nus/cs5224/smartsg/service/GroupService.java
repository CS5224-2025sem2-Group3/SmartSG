package com.nus.cs5224.smartsg.service;

import com.nus.cs5224.smartsg.dto.request.CreateGroupRequest;
import com.nus.cs5224.smartsg.dto.response.GroupResponse;

import java.util.List;

public interface GroupService {
    GroupResponse createGroup(CreateGroupRequest request, Long userId);
    void joinGroup(Long userId, int groupId);
    void leaveGroup(Long userId, int groupId);
    GroupResponse getGroup(int groupId);
    List<GroupResponse> getMyGroups(Long userId);
    List<GroupResponse> getGroupsByListing(Long listingId);
    void deleteGroup(int groupId, Long userId);
    void confirmGroup(int groupId, Long userId);
}
