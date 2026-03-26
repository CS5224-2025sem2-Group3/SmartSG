package com.nus.cs5224.smartsg.service;

import com.nus.cs5224.smartsg.dto.request.CreateGroupRequest;
import com.nus.cs5224.smartsg.dto.response.GroupResponse;

public interface GroupService {
    public GroupResponse createGroup(CreateGroupRequest request, Long userId);

    public void joinGroup(Long userId, int groupId);

    public void leaveGroup(Long userId, int groupId);

    public GroupResponse getGroup(int groupId);

}
