package com.nus.cs5224.smartsg.service;

import com.nus.cs5224.smartsg.dto.request.SendInvitationRequest;
import com.nus.cs5224.smartsg.dto.response.InvitationResponse;
import com.nus.cs5224.smartsg.entity.Group;
import com.nus.cs5224.smartsg.entity.GroupRequest;
import com.nus.cs5224.smartsg.mapper.GroupMapper;
import com.nus.cs5224.smartsg.mapper.InvitationMapper;
import com.nus.cs5224.smartsg.service.serviceImpl.InvitationServiceImpl;
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
class InvitationServiceImplTest {

    @Mock
    private InvitationMapper invitationMapper;

    @Mock
    private GroupMapper groupMapper;

    @Mock
    private GroupService groupService;

    @InjectMocks
    private InvitationServiceImpl invitationService;

    private Group sampleGroup;
    private GroupRequest pendingRequest;

    @BeforeEach
    void setUp() {
        sampleGroup = new Group();
        sampleGroup.setGroupId(10);
        sampleGroup.setListingId(1L);
        sampleGroup.setStatus("recruiting");
        sampleGroup.setMaxPeople(3);
        sampleGroup.setCurPeople(1);

        pendingRequest = new GroupRequest();
        pendingRequest.setRequestId(1);
        pendingRequest.setGroupId(10);
        pendingRequest.setSenderId(100L);
        pendingRequest.setReceiverId(200L);
        pendingRequest.setStatus("pending");
        pendingRequest.setListingId(1);
        pendingRequest.setListingTitle("Test Listing");
        pendingRequest.setSenderName("Alice");
    }

    // ========== Send Invitation Tests ==========

    @Nested
    @DisplayName("sendInvitation")
    class SendInvitationTests {

        @Test
        @DisplayName("I1: 正常发送邀请")
        void sendInvitation_success() {
            SendInvitationRequest request = new SendInvitationRequest();
            request.setGroupId(10);
            request.setCandidateUserId(200L);

            when(groupMapper.findById(10)).thenReturn(sampleGroup);
            when(groupMapper.findByUserId(200L)).thenReturn(Collections.emptyList());

            assertDoesNotThrow(() -> invitationService.sendInvitation(request, 100L));

            verify(invitationMapper).insertRequest(any(GroupRequest.class));
        }

        @Test
        @DisplayName("I2: 目标群组不存在 - 返回404")
        void sendInvitation_groupNotFound() {
            SendInvitationRequest request = new SendInvitationRequest();
            request.setGroupId(999);
            request.setCandidateUserId(200L);

            when(groupMapper.findById(999)).thenReturn(null);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> invitationService.sendInvitation(request, 100L));

            assertEquals(404, ex.getStatusCode().value());
        }

        @Test
        @DisplayName("I3: 被邀请人已有该listing的群组 - 返回409")
        void sendInvitation_receiverAlreadyHasGroup() {
            SendInvitationRequest request = new SendInvitationRequest();
            request.setGroupId(10);
            request.setCandidateUserId(200L);

            // Receiver already in a group for the same listing
            Group existingGroup = new Group();
            existingGroup.setGroupId(20);
            existingGroup.setListingId(1L); // same listing

            when(groupMapper.findById(10)).thenReturn(sampleGroup);
            when(groupMapper.findByUserId(200L)).thenReturn(List.of(existingGroup));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> invitationService.sendInvitation(request, 100L));

            assertEquals(409, ex.getStatusCode().value());
            assertTrue(ex.getReason().contains("already has a group"));
        }
    }

    // ========== Accept Invitation Tests ==========

    @Nested
    @DisplayName("acceptInvitation")
    class AcceptInvitationTests {

        @Test
        @DisplayName("I4: 正常接受邀请")
        void acceptInvitation_success() {
            when(invitationMapper.findById(1)).thenReturn(pendingRequest);

            assertDoesNotThrow(() -> invitationService.acceptInvitation(1, 200L));

            verify(groupService).joinGroup(200L, 10);
            verify(invitationMapper).updateStatus(1, "accepted");
        }

        @Test
        @DisplayName("I5: 邀请不存在 - 返回404")
        void acceptInvitation_notFound() {
            when(invitationMapper.findById(999)).thenReturn(null);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> invitationService.acceptInvitation(999, 200L));

            assertEquals(404, ex.getStatusCode().value());
        }

        @Test
        @DisplayName("I6: 不是自己的邀请 - 返回403")
        void acceptInvitation_notYourInvitation() {
            when(invitationMapper.findById(1)).thenReturn(pendingRequest);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> invitationService.acceptInvitation(1, 300L)); // 300 != 200

            assertEquals(403, ex.getStatusCode().value());
            assertTrue(ex.getReason().contains("Not your invitation"));
        }

        @Test
        @DisplayName("I7: 邀请已处理 - 返回400")
        void acceptInvitation_alreadyProcessed() {
            pendingRequest.setStatus("accepted");
            when(invitationMapper.findById(1)).thenReturn(pendingRequest);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> invitationService.acceptInvitation(1, 200L));

            assertEquals(400, ex.getStatusCode().value());
            assertTrue(ex.getReason().contains("no longer pending"));
        }
    }

    // ========== Reject Invitation Tests ==========

    @Nested
    @DisplayName("rejectInvitation")
    class RejectInvitationTests {

        @Test
        @DisplayName("I9: 正常拒绝邀请")
        void rejectInvitation_success() {
            when(invitationMapper.findById(1)).thenReturn(pendingRequest);

            assertDoesNotThrow(() -> invitationService.rejectInvitation(1, 200L));

            verify(invitationMapper).updateStatus(1, "rejected");
        }

        @Test
        @DisplayName("I10: 已处理的邀请再拒绝 - 返回400")
        void rejectInvitation_alreadyProcessed() {
            pendingRequest.setStatus("accepted");
            when(invitationMapper.findById(1)).thenReturn(pendingRequest);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> invitationService.rejectInvitation(1, 200L));

            assertEquals(400, ex.getStatusCode().value());
        }
    }

    // ========== Get My Invitations Tests ==========

    @Nested
    @DisplayName("getMyInvitations")
    class GetMyInvitationsTests {

        @Test
        @DisplayName("I11: 有邀请时返回列表")
        void getMyInvitations_withResults() {
            when(invitationMapper.findByReceiverId(200L)).thenReturn(List.of(pendingRequest));

            List<InvitationResponse> responses = invitationService.getMyInvitations(200L);

            assertEquals(1, responses.size());
            assertEquals(1, responses.get(0).getId());
            assertEquals(10, responses.get(0).getGroupId());
            assertEquals("Test Listing", responses.get(0).getListingTitle());
            assertEquals("Alice", responses.get(0).getFromUserName());
            assertEquals("pending", responses.get(0).getStatus());
        }

        @Test
        @DisplayName("I12: 无邀请时返回空列表")
        void getMyInvitations_empty() {
            when(invitationMapper.findByReceiverId(999L)).thenReturn(Collections.emptyList());

            List<InvitationResponse> responses = invitationService.getMyInvitations(999L);

            assertTrue(responses.isEmpty());
        }
    }
}
