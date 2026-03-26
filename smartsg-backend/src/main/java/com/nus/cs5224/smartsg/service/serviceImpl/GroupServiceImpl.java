package com.nus.cs5224.smartsg.service.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.nus.cs5224.smartsg.dto.request.CreateGroupRequest;
import com.nus.cs5224.smartsg.dto.response.GroupResponse;
import com.nus.cs5224.smartsg.entity.Group;
import com.nus.cs5224.smartsg.entity.GroupMember;
import com.nus.cs5224.smartsg.entity.Listing;
import com.nus.cs5224.smartsg.mapper.GroupMapper;
import com.nus.cs5224.smartsg.mapper.ListingMapper;
import com.nus.cs5224.smartsg.service.GroupService;

import jakarta.transaction.Transactional;

@Service
public class GroupServiceImpl implements GroupService {

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private ListingMapper listingMapper;

    /**
     * 创建小组：根据 listingId 创建小组，创建者自动加入
     */
    @Override
    @Transactional
    public GroupResponse createGroup(CreateGroupRequest request, Long userId) {
        // 1. 从 Listing 获取 maxPeople
        Listing listing = listingMapper.findById(request.getListingId());
        if (listing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found");
        }

        // 2. 创建 Group 记录
        Group group = new Group();
        group.setListingId(request.getListingId());
        group.setStatus("RECRUITING");
        group.setMaxPeople(listing.getTotalTenants());
        group.setCurPeople(1); // 创建者算一个人
        groupMapper.insertGroup(group);

        // 3. 将创建者加入 GroupMember
        GroupMember member = new GroupMember();
        member.setGroupId(group.getGroupId());
        member.setUserId(userId);
        member.setRole("CREATOR");
        groupMapper.insertMember(member);

        // 4. 返回响应
        return toGroupResponse(group);
    }

    /**
     * 加入小组：检查是否满员，未满则加入
     */
    @Override
    @Transactional
    public void joinGroup(Long userId, int groupId) {
        // 1. 查找小组
        Group group = groupMapper.findById(groupId);
        if (group == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found");
        }

        // 2. 检查是否已满
        if (group.getCurPeople() >= group.getMaxPeople()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group is full");
        }

        // 3. 检查是否已经是成员
        GroupMember existing = groupMapper.findMember(groupId, userId);
        if (existing != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already a member of this group");
        }

        // 4. 插入成员记录
        GroupMember member = new GroupMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setRole("MEMBER");
        groupMapper.insertMember(member);

        // 5. 更新 curPeople + 1
        groupMapper.updateCurPeople(groupId, group.getCurPeople() + 1);
    }

    /**
     * 离开小组：删除成员记录，更新人数
     */
    @Override
    @Transactional
    public void leaveGroup(Long userId, int groupId) {
        // 1. 查找小组
        Group group = groupMapper.findById(groupId);
        if (group == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found");
        }

        // 2. 检查是否是成员
        GroupMember member = groupMapper.findMember(groupId, userId);
        if (member == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a member of this group");
        }

        // 3. 普通成员离开
        groupMapper.deleteMember(groupId, userId);
        groupMapper.updateCurPeople(groupId, group.getCurPeople() - 1);
    }

    /**
     * 查询小组详情
     */
    @Override
    public GroupResponse getGroup(int groupId) {
        Group group = groupMapper.findById(groupId);
        if (group == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found");
        }
        return toGroupResponse(group);
    }

    /**
     * Entity → Response DTO 转换
     */
    private GroupResponse toGroupResponse(Group group) {
        GroupResponse response = new GroupResponse();
        response.setGroupId(group.getGroupId());
        response.setListingId((int) group.getListingId());
        response.setStatus(group.getStatus());
        response.setMaxPeople(group.getMaxPeople());
        response.setCurPeople(group.getCurPeople());
        return response;
    }
}