import { reactive } from 'vue'
import { apiRequest } from '../api/apiClient'
import { getAuthHeaders } from './authService'
import { getGroupByListingId, loadGroupsForCurrentUser } from './groupService'

const invitationState = reactive({
  items: [],
  loaded: false
})

export async function inviteCandidateToGroup(candidateUserId, listingId) {
  let group = getGroupByListingId(listingId)
  if (!group) {
    await loadGroupsForCurrentUser()
    group = getGroupByListingId(listingId)
  }

  if (!group) {
    throw new Error('Create a group first before sending invitations.')
  }

  if (!group) return

  await apiRequest('/api/invitations', {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify({
      groupId: group.id,
      candidateUserId: Number(candidateUserId)
    })
  })
}

export async function loadInvitationsForCurrentUser() {
  const invitations = await apiRequest('/api/invitations/me', {
    headers: getAuthHeaders()
  })

  invitationState.items = invitations
  invitationState.loaded = true
  return invitationState.items
}

export function getInvitationsForCurrentUser() {
  return invitationState.items
}

export function getPendingInvitationCount() {
  return invitationState.items.filter((item) => item.status === 'pending').length
}

export async function acceptInvitation(invitationId) {
  await apiRequest(`/api/invitations/${Number(invitationId)}/accept`, {
    method: 'POST',
    headers: getAuthHeaders()
  })

  const invitation = invitationState.items.find((item) => item.id === Number(invitationId))
  if (invitation) invitation.status = 'accepted'
}

export async function rejectInvitation(invitationId) {
  await apiRequest(`/api/invitations/${Number(invitationId)}/reject`, {
    method: 'POST',
    headers: getAuthHeaders()
  })

  const invitation = invitationState.items.find((item) => item.id === Number(invitationId))
  if (invitation) invitation.status = 'rejected'
}
