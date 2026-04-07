package com.nus.cs5224.smartsg.service;

import com.nus.cs5224.smartsg.dto.request.CreateGroupRequest;
import com.nus.cs5224.smartsg.dto.response.GroupResponse;
import com.nus.cs5224.smartsg.entity.Group;
import com.nus.cs5224.smartsg.entity.GroupMember;
import com.nus.cs5224.smartsg.entity.Listing;
import com.nus.cs5224.smartsg.mapper.GroupMapper;
import com.nus.cs5224.smartsg.mapper.ListingMapper;
import com.nus.cs5224.smartsg.service.serviceImpl.GroupServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceImplTest {

    @Mock
    private GroupMapper groupMapper;

    @Mock
    private ListingMapper listingMapper;

    @InjectMocks
    private GroupServiceImpl groupService;

    private Group sampleGroup;
    private Listing sampleListing;
    private GroupMember leaderMember;
    private GroupMember normalMember;

    @BeforeEach
    void setUp() {
        sampleListing = new Listing();
        sampleListing.setListingId(1L);
        sampleListing.setTitle("Test Listing");
        sampleListing.setTotalTenants(3);

        sampleGroup = new Group();
        sampleGroup.setGroupId(10);
        sampleGroup.setListingId(1L);
        sampleGroup.setStatus("recruiting");
        sampleGroup.setMaxPeople(3);
        sampleGroup.setCurPeople(1);

        leaderMember = new GroupMember();
        leaderMember.setGroupId(10);
        leaderMember.setUserId(100L);
        leaderMember.setRole("leader");

        normalMember = new GroupMember();
        normalMember.setGroupId(10);
        normalMember.setUserId(200L);
        normalMember.setRole("member");
    }

    // ========== Create Group Tests ==========

    @Nested
    @DisplayName("createGroup")
    class CreateGroupTests {

        @Test
        @DisplayName("G1: 正常创建群组 - creator成为leader")
        void createGroup_success() {
            CreateGroupRequest request = new CreateGroupRequest();
            request.setListingId(1L);

            when(listingMapper.findById(1L)).thenReturn(sampleListing);
            when(groupMapper.insertGroup(any(Group.class))).thenAnswer(invocation -> {
                Group g = invocation.getArgument(0);
                g.setGroupId(10);
                return 1;
            });
            when(groupMapper.findMembersByGroupId(10)).thenReturn(List.of(leaderMember));

            GroupResponse response = groupService.createGroup(request, 100L);

            assertNotNull(response);
            assertEquals("recruiting", response.getStatus());
            assertEquals(1, response.getCurPeople());
            assertEquals(3, response.getRequiredPeople());
            assertEquals(100L, response.getLeaderUserId());
            assertTrue(response.isCurrentUserIsLeader());

            verify(groupMapper).insertGroup(any(Group.class));
            verify(groupMapper).insertMember(any(GroupMember.class));
        }

        @Test
        @DisplayName("G2: listingId不存在 - 返回404")
        void createGroup_listingNotFound() {
            CreateGroupRequest request = new CreateGroupRequest();
            request.setListingId(999L);

            when(listingMapper.findById(999L)).thenReturn(null);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> groupService.createGroup(request, 100L));

            assertEquals(404, ex.getStatusCode().value());
            assertTrue(ex.getReason().contains("Listing not found"));
        }
    }

    // ========== Join Group Tests ==========

    @Nested
    @DisplayName("joinGroup")
    class JoinGroupTests {

        @Test
        @DisplayName("G4: 正常加入群组")
        void joinGroup_success() {
            when(groupMapper.findById(10)).thenReturn(sampleGroup);
            when(groupMapper.findMember(10, 200L)).thenReturn(null);

            assertDoesNotThrow(() -> groupService.joinGroup(200L, 10));

            verify(groupMapper).insertMember(any(GroupMember.class));
            verify(groupMapper).updateCurPeople(10, 2);
        }

        @Test
        @DisplayName("G5: 群组已满 - 返回400")
        void joinGroup_groupFull() {
            sampleGroup.setCurPeople(3); // maxPeople == 3, already full
            when(groupMapper.findById(10)).thenReturn(sampleGroup);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> groupService.joinGroup(200L, 10));

            assertEquals(400, ex.getStatusCode().value());
            assertTrue(ex.getReason().contains("Group is full"));
        }

        @Test
        @DisplayName("G6: 已是成员 - 返回409")
        void joinGroup_alreadyMember() {
            when(groupMapper.findById(10)).thenReturn(sampleGroup);
            when(groupMapper.findMember(10, 200L)).thenReturn(normalMember);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> groupService.joinGroup(200L, 10));

            assertEquals(409, ex.getStatusCode().value());
            assertTrue(ex.getReason().contains("Already a member"));
        }

        @Test
        @DisplayName("G7: 群组不存在 - 返回404")
        void joinGroup_groupNotFound() {
            when(groupMapper.findById(999)).thenReturn(null);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> groupService.joinGroup(200L, 999));

            assertEquals(404, ex.getStatusCode().value());
        }
    }

    // ========== Leave Group Tests ==========

    @Nested
    @DisplayName("leaveGroup")
    class LeaveGroupTests {

        @Test
        @DisplayName("G8: 正常离开群组")
        void leaveGroup_success() {
            when(groupMapper.findById(10)).thenReturn(sampleGroup);
            when(groupMapper.findMember(10, 200L)).thenReturn(normalMember);

            assertDoesNotThrow(() -> groupService.leaveGroup(200L, 10));

            verify(groupMapper).deleteMember(10, 200L);
            verify(groupMapper).updateCurPeople(10, 0); // curPeople was 1, minus 1 = 0
        }

        @Test
        @DisplayName("G9: 非成员离开 - 返回400")
        void leaveGroup_notMember() {
            when(groupMapper.findById(10)).thenReturn(sampleGroup);
            when(groupMapper.findMember(10, 300L)).thenReturn(null);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> groupService.leaveGroup(300L, 10));

            assertEquals(400, ex.getStatusCode().value());
            assertTrue(ex.getReason().contains("Not a member"));
        }

        @Test
        @DisplayName("G10: 群组不存在 - 返回404")
        void leaveGroup_groupNotFound() {
            when(groupMapper.findById(999)).thenReturn(null);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> groupService.leaveGroup(200L, 999));

            assertEquals(404, ex.getStatusCode().value());
        }
    }

    // ========== Delete Group Tests ==========

    @Nested
    @DisplayName("deleteGroup")
    class DeleteGroupTests {

        @Test
        @DisplayName("G11: Leader删除群组 - 成功")
        void deleteGroup_byLeader() {
            when(groupMapper.findById(10)).thenReturn(sampleGroup);
            when(groupMapper.findMember(10, 100L)).thenReturn(leaderMember);

            assertDoesNotThrow(() -> groupService.deleteGroup(10, 100L));

            verify(groupMapper).deleteAllMembersByGroupId(10);
            verify(groupMapper).deleteGroup(10);
        }

        @Test
        @DisplayName("G12: 非leader删除 - 返回403")
        void deleteGroup_byMember_forbidden() {
            when(groupMapper.findById(10)).thenReturn(sampleGroup);
            when(groupMapper.findMember(10, 200L)).thenReturn(normalMember);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> groupService.deleteGroup(10, 200L));

            assertEquals(403, ex.getStatusCode().value());
            assertTrue(ex.getReason().contains("Only the group leader"));
        }

        @Test
        @DisplayName("G13: 非成员删除 - 返回403")
        void deleteGroup_byNonMember_forbidden() {
            when(groupMapper.findById(10)).thenReturn(sampleGroup);
            when(groupMapper.findMember(10, 300L)).thenReturn(null);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> groupService.deleteGroup(10, 300L));

            assertEquals(403, ex.getStatusCode().value());
        }

        @Test
        @DisplayName("G14: 群组不存在 - 返回404")
        void deleteGroup_notFound() {
            when(groupMapper.findById(999)).thenReturn(null);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> groupService.deleteGroup(999, 100L));

            assertEquals(404, ex.getStatusCode().value());
        }
    }

    // ========== Confirm Group Tests ==========

    @Nested
    @DisplayName("confirmGroup")
    class ConfirmGroupTests {

        @Test
        @DisplayName("G15: Leader确认群组 - status变为closed")
        void confirmGroup_byLeader() {
            when(groupMapper.findById(10)).thenReturn(sampleGroup);
            when(groupMapper.findMember(10, 100L)).thenReturn(leaderMember);

            assertDoesNotThrow(() -> groupService.confirmGroup(10, 100L));

            verify(groupMapper).updateStatus(10, "closed");
        }

        @Test
        @DisplayName("G16: 非leader确认 - 返回403")
        void confirmGroup_byMember_forbidden() {
            when(groupMapper.findById(10)).thenReturn(sampleGroup);
            when(groupMapper.findMember(10, 200L)).thenReturn(normalMember);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> groupService.confirmGroup(10, 200L));

            assertEquals(403, ex.getStatusCode().value());
        }

        @Test
        @DisplayName("G17: 群组不存在 - 返回404")
        void confirmGroup_notFound() {
            when(groupMapper.findById(999)).thenReturn(null);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> groupService.confirmGroup(999, 100L));

            assertEquals(404, ex.getStatusCode().value());
        }
    }

    // ========== Query Group Tests ==========

    @Nested
    @DisplayName("getGroup / getMyGroups / getGroupsByListing")
    class QueryGroupTests {

        @Test
        @DisplayName("G18: 获取指定群组详情")
        void getGroup_success() {
            when(groupMapper.findById(10)).thenReturn(sampleGroup);
            when(listingMapper.findById(1L)).thenReturn(sampleListing);
            when(groupMapper.findMembersByGroupId(10)).thenReturn(List.of(leaderMember, normalMember));

            GroupResponse response = groupService.getGroup(10, 100L);

            assertNotNull(response);
            assertEquals(10, response.getGroupId());
            assertEquals("Test Listing", response.getListingTitle());
            assertEquals(2, response.getMembers().size());
            assertEquals(100L, response.getLeaderUserId());
            assertEquals("leader", response.getCurrentUserRole());
            assertTrue(response.isCurrentUserIsLeader());
        }

        @Test
        @DisplayName("G19: 群组不存在 - 返回404")
        void getGroup_notFound() {
            when(groupMapper.findById(999)).thenReturn(null);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> groupService.getGroup(999, 100L));

            assertEquals(404, ex.getStatusCode().value());
        }

        @Test
        @DisplayName("G20: 获取我的群组列表")
        void getMyGroups_success() {
            when(groupMapper.findByUserId(100L)).thenReturn(List.of(sampleGroup));
            when(listingMapper.findById(1L)).thenReturn(sampleListing);
            when(groupMapper.findMembersByGroupId(10)).thenReturn(List.of(leaderMember));

            List<GroupResponse> groups = groupService.getMyGroups(100L);

            assertEquals(1, groups.size());
            assertEquals(10, groups.get(0).getGroupId());
        }

        @Test
        @DisplayName("G21: 新用户无群组 - 返回空列表")
        void getMyGroups_empty() {
            when(groupMapper.findByUserId(999L)).thenReturn(Collections.emptyList());

            List<GroupResponse> groups = groupService.getMyGroups(999L);

            assertTrue(groups.isEmpty());
        }

        @Test
        @DisplayName("G22: 按listing查群组")
        void getGroupsByListing_success() {
            when(groupMapper.findByListingId(1L)).thenReturn(List.of(sampleGroup));
            when(listingMapper.findById(1L)).thenReturn(sampleListing);
            when(groupMapper.findMembersByGroupId(10)).thenReturn(List.of(leaderMember));

            List<GroupResponse> groups = groupService.getGroupsByListing(1L, 100L);

            assertEquals(1, groups.size());
        }

        @Test
        @DisplayName("G23: currentUserRole验证 - 非成员时为null")
        void getGroup_nonMember_roleIsNull() {
            when(groupMapper.findById(10)).thenReturn(sampleGroup);
            when(listingMapper.findById(1L)).thenReturn(sampleListing);
            when(groupMapper.findMembersByGroupId(10)).thenReturn(List.of(leaderMember));

            GroupResponse response = groupService.getGroup(10, 999L); // 999 is not a member

            assertNull(response.getCurrentUserRole());
            assertFalse(response.isCurrentUserIsLeader());
        }
    }
}
