import { store, persistStore } from '../store/mockStore'
import { getListingById } from './listingService'

function overlapMoveIn(a, b) {
  if (!a || !b) return true
  return a === b
}

function compatibleLease(userLease, candidateLease, listingLeaseOptions = []) {
  const normalizedUser = String(userLease || '')
  const normalizedCandidate = String(candidateLease || '')

  const leaseOkForListing =
    normalizedUser === '6-12'
      ? listingLeaseOptions.length > 0
      : listingLeaseOptions.includes(Number(normalizedUser))

  if (!leaseOkForListing) return false
  if (normalizedCandidate === '6-12') return true
  return normalizedCandidate === normalizedUser
}

function habitScore(userProfile, candidate) {
  let score = 0
  if (userProfile.sleepHabit === candidate.sleepHabit) score += 2
  if (userProfile.studyPreference === candidate.studyPreference) score += 2
  if (userProfile.smoking === candidate.smoking) score += 2
  if (userProfile.cleanliness === candidate.cleanliness) score += 2
  return score
}

export function getGroupByListingId(listingId) {
  return store.groups.find((g) => g.listingId === Number(listingId)) || null
}

export function getGroupsForCurrentUser() {
  if (!store.currentUserId) return []

  return store.groups.filter((g) =>
    g.members.some((m) => m.userId === store.currentUserId)
  )
}

export function getOrCreateGroupForListing(listingId, creatorProfile) {
  if (!store.currentUserId) return null

  const listing = getListingById(listingId)
  if (!listing) return null

  const existing = getGroupByListingId(listingId)

  if (existing) {
    const alreadyInside = existing.members.some((m) => m.userId === store.currentUserId)
    if (!alreadyInside) {
      existing.members.push({
        userId: store.currentUserId,
        name: store.currentUserName,
        ...creatorProfile
      })
      persistStore()
    }
    return existing
  }

  const group = {
    id: Date.now(),
    listingId: Number(listingId),
    listingTitle: listing.title,
    createdByUserId: store.currentUserId,
    requiredPeople: listing.rooms,
    status: 'recruiting',
    selected: false,
    members: [
      {
        userId: store.currentUserId,
        name: store.currentUserName,
        ...creatorProfile
      }
    ]
  }

  store.groups.push(group)
  persistStore()
  return group
}

export function getDiscoverableRoommates() {
  if (!store.currentUserId) return []

  return store.users
    .filter((user) => user.id !== store.currentUserId)
    .filter((user) => !!store.userProfiles[user.id])
    .map((user) => ({
      userId: user.id,
      name: user.name,
      ...store.userProfiles[user.id]
    }))
}

export function recommendRoommates(listingId, userProfile) {
  const listing = getListingById(listingId)
  if (!listing || !store.currentUserId) return []

  const existingGroup = getGroupByListingId(listingId)
  const existingMemberIds = existingGroup
    ? existingGroup.members.map((m) => m.userId)
    : [store.currentUserId]

  return getDiscoverableRoommates()
    .filter((candidate) => !existingMemberIds.includes(candidate.userId))
    .filter((candidate) => {
      const minRequiredBudget = listing.totalRent / Math.max(2, listing.rooms)
      return Number(candidate.budgetMax || 0) >= minRequiredBudget
    })
    .filter((candidate) => overlapMoveIn(userProfile.moveInWindow, candidate.moveInWindow))
    .filter((candidate) =>
      compatibleLease(userProfile.leasePreference, candidate.leasePreference, listing.leaseOptions)
    )
    .map((candidate) => ({
      ...candidate,
      matchScore: habitScore(userProfile, candidate)
    }))
    .sort((a, b) => b.matchScore - a.matchScore)
}

export function confirmGroupDecision(groupId) {
  const group = store.groups.find((g) => g.id === groupId)
  if (!group) return

  if (group.members.length >= group.requiredPeople) {
    group.selected = true
    group.status = 'selected'

    const listing = getListingById(group.listingId)
    if (listing) listing.status = 'selected'

    persistStore()
  }
}

export function calculateGroupSummary(group) {
  const listing = getListingById(group.listingId)
  if (!listing) return null

  const budgets = group.members.map((m) => Number(m.budgetMax || 0))
  const leaseValues = group.members.map((m) => m.leasePreference).filter(Boolean)
  const moveInValues = group.members.map((m) => m.moveInWindow).filter(Boolean)

  return {
    totalBudget: budgets.reduce((a, b) => a + b, 0),
    perPerson: listing.totalRent / group.members.length,
    leaseIntersection: leaseValues.length ? [...new Set(leaseValues)].join(', ') : '-',
    moveInIntersection: moveInValues.length ? [...new Set(moveInValues)].join(', ') : '-'
  }
}

export function deleteGroup(groupId) {
  if (!store.currentUserId) return { ok: false, message: 'Not logged in.' }

  const groupIndex = store.groups.findIndex((g) => g.id === Number(groupId))
  if (groupIndex === -1) {
    return { ok: false, message: 'Group not found.' }
  }

  const group = store.groups[groupIndex]

  if (group.createdByUserId !== store.currentUserId) {
    return { ok: false, message: 'Only the group creator can delete this group.' }
  }

  store.groups.splice(groupIndex, 1)

  store.invitations = store.invitations.filter(
    (inv) => inv.groupId !== Number(groupId)
  )

  persistStore()
  return { ok: true }
}

export function leaveGroup(groupId) {
  if (!store.currentUserId) return { ok: false, message: 'Not logged in.' }

  const group = store.groups.find((g) => g.id === Number(groupId))
  if (!group) {
    return { ok: false, message: 'Group not found.' }
  }

  if (group.createdByUserId === store.currentUserId) {
    return {
      ok: false,
      message: 'Group creator cannot leave directly. Please delete the group instead.'
    }
  }

  if (group.selected) {
    return {
      ok: false,
      message: 'This group has already confirmed a listing and cannot be left now.'
    }
  }

  group.members = group.members.filter(
    (member) => member.userId !== store.currentUserId
  )

  if (group.members.length < group.requiredPeople) {
    group.status = 'recruiting'
  }

  store.invitations = store.invitations.filter(
    (inv) =>
      !(
        inv.groupId === Number(groupId) &&
        inv.toUserId === store.currentUserId &&
        inv.status === 'accepted'
      )
  )

  persistStore()
  return { ok: true }
}