import { store, persistStore } from '../store/mockStore'
import { getGroupByListingId } from './groupService'
import { getCurrentUser } from './authService'

export function inviteCandidateToGroup(candidateUserId, listingId) {
  if (!store.currentUserId) return

  const group = getGroupByListingId(listingId)
  if (!group) return

  const alreadyInvited = store.invitations.some(
    (inv) =>
      inv.groupId === group.id &&
      inv.toUserId === Number(candidateUserId) &&
      inv.status === 'pending'
  )

  if (alreadyInvited) return

  const targetUser = store.users.find((u) => u.id === Number(candidateUserId))

  store.invitations.push({
    id: Date.now() + Math.random(),
    groupId: group.id,
    listingId: Number(listingId),
    listingTitle: group.listingTitle,
    fromUserId: store.currentUserId,
    fromUserName: store.currentUserName,
    toUserId: Number(candidateUserId),
    toUserName: targetUser?.name || 'Unknown',
    status: 'pending'
  })

  persistStore()
}

export function getInvitationsForCurrentUser() {
  if (!store.currentUserId) return []
  return store.invitations.filter((inv) => inv.toUserId === store.currentUserId)
}

export function acceptInvitation(invitationId) {
  if (!store.currentUserId) return

  const invitation = store.invitations.find((inv) => inv.id === invitationId)
  if (!invitation || invitation.status !== 'pending') return
  if (invitation.toUserId !== store.currentUserId) return

  invitation.status = 'accepted'

  const group = store.groups.find((g) => g.id === invitation.groupId)
  const user = getCurrentUser()
  const profile = store.userProfiles[store.currentUserId] || {}

  if (group && user && !group.members.some((m) => m.userId === user.id)) {
    group.members.push({
      userId: user.id,
      name: user.name,
      ...profile
    })
  }

  if (group && group.members.length >= group.requiredPeople) {
    group.status = 'ready'
  }

  persistStore()
}

export function rejectInvitation(invitationId) {
  if (!store.currentUserId) return

  const invitation = store.invitations.find((inv) => inv.id === invitationId)
  if (!invitation || invitation.status !== 'pending') return
  if (invitation.toUserId !== store.currentUserId) return

  invitation.status = 'rejected'
  persistStore()
}