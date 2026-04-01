import { reactive } from 'vue'
import { apiRequest } from '../api/apiClient'
import { getAuthHeaders } from './authService'
import { getCachedListingById } from './listingService'

const groupState = reactive({
  items: [],
  byListing: {},
  loaded: false
})

const matchingSleepHabitMap = {
  EarlyBird: 'early_bird',
  Regular: 'normal',
  NightOwl: 'night_owl'
}

const matchingCleanlinessMap = {
  Low: 'low',
  Average: 'medium',
  High: 'high'
}

const matchingSmokingMap = {
  Yes: 'true',
  No: 'false'
}

function normalizeGroup(group) {
  if (!group) return null

  return {
    id: Number(group.groupId ?? group.id),
    groupId: Number(group.groupId ?? group.id),
    listingId: Number(group.listingId),
    listingTitle: group.listingTitle || '',
    status: group.status || 'recruiting',
    requiredPeople: Number(group.requiredPeople ?? 0),
    curPeople: Number(group.curPeople ?? 0),
    leaderUserId: group.leaderUserId ?? null,
    currentUserRole: group.currentUserRole || null,
    currentUserIsLeader: Boolean(group.currentUserIsLeader),
    members: Array.isArray(group.members) ? group.members : []
  }
}

function cacheGroups(groups = []) {
  groups.forEach((group) => {
    if (!group) return
    groupState.items = [
      ...groupState.items.filter((item) => item.id !== group.id),
      group
    ]

    const listingGroups = groupState.byListing[group.listingId] || []
    groupState.byListing[group.listingId] = [
      ...listingGroups.filter((item) => item.id !== group.id),
      group
    ]
  })
}

function replaceListingGroups(listingId, groups = []) {
  const normalizedListingId = Number(listingId)
  const groupIds = new Set(groups.map((group) => group.id))

  groupState.byListing[normalizedListingId] = groups
  groupState.items = [
    ...groupState.items.filter(
      (group) => group.listingId !== normalizedListingId || groupIds.has(group.id)
    ),
    ...groups.filter(
      (group) => !groupState.items.some((item) => item.id === group.id)
    )
  ]
}

export async function loadGroupsForCurrentUser() {
  const groups = await apiRequest('/api/groups/me', {
    headers: getAuthHeaders()
  })

  const normalized = groups.map(normalizeGroup)
  groupState.items = normalized
  groupState.byListing = {}
  normalized.forEach((group) => {
    groupState.byListing[group.listingId] = [group]
  })
  groupState.loaded = true
  return normalized
}

export function getGroupsForCurrentUser() {
  return groupState.items
}

export async function loadGroupsByListingId(listingId) {
  const groups = await apiRequest(`/api/groups/by-listing/${Number(listingId)}`, {
    headers: getAuthHeaders()
  })

  const normalized = groups.map(normalizeGroup)
  replaceListingGroups(listingId, normalized)
  return normalized
}

export function getGroupByListingId(listingId) {
  const listingGroups = groupState.byListing[Number(listingId)] || []
  return listingGroups[0] || null
}

export async function getOrCreateGroupForListing(listingId) {
  if (!groupState.loaded) {
    await loadGroupsForCurrentUser()
  }

  const existingGroup = getGroupByListingId(listingId)
  if (existingGroup) {
    throw new Error('You already have a group for this listing.')
  }

  const createdGroup = await apiRequest('/api/groups', {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify({
      listingId: Number(listingId)
    })
  })

  const normalized = normalizeGroup(createdGroup)
  cacheGroups([normalized])
  return normalized
}

export async function recommendHousemates(listingId, idealProfile) {
  if (!groupState.loaded) {
    await loadGroupsForCurrentUser()
  }

  const group = getGroupByListingId(listingId)
  if (!group) {
    throw new Error('Create a group first before matching housemates for this listing.')
  }

  const params = new URLSearchParams()
  if (idealProfile.budgetMax) params.set('budgetMax', String(idealProfile.budgetMax))
  if (idealProfile.moveInWindow) params.set('moveInWindow', idealProfile.moveInWindow)
  if (idealProfile.leasePreference) params.set('leasePreference', String(Number(idealProfile.leasePreference)))
  if (idealProfile.sleepHabit) {
    params.set('sleepHabit', matchingSleepHabitMap[idealProfile.sleepHabit] || idealProfile.sleepHabit)
  }
  if (idealProfile.studyPreference) params.set('studyPreference', idealProfile.studyPreference)
  if (idealProfile.smoking) {
    params.set('smoking', matchingSmokingMap[idealProfile.smoking] || idealProfile.smoking)
  }
  if (idealProfile.cleanliness) {
    params.set('cleanliness', matchingCleanlinessMap[idealProfile.cleanliness] || idealProfile.cleanliness)
  }

  const query = params.toString()
  return await apiRequest(`/api/groups/${group.id}/recommendations${query ? `?${query}` : ''}`, {
    headers: getAuthHeaders()
  })
}

export async function confirmGroupDecision(groupId) {
  await apiRequest(`/api/groups/${Number(groupId)}/confirm`, {
    method: 'POST',
    headers: getAuthHeaders()
  })

  const group = groupState.items.find((item) => item.id === Number(groupId))
  if (group) {
    group.status = 'closed'
  }
}

export function calculateGroupSummary(group) {
  const listing = getCachedListingById(group.listingId)
  if (!listing || !group.curPeople) return null

  const budgets = group.members
    .map((member) => Number(member.budgetMax || 0))
    .filter((value) => value > 0)
  const leaseValues = group.members
    .map((member) => member.leasePreference)
    .filter((value) => value != null)
  const moveInValues = group.members
    .map((member) => member.moveInWindow)
    .filter(Boolean)

  return {
    totalBudget: budgets.reduce((sum, value) => sum + value, 0),
    perPerson: listing.totalRent / group.curPeople,
    leaseIntersection: leaseValues.length ? [...new Set(leaseValues)].join(', ') : '-',
    moveInIntersection: moveInValues.length ? [...new Set(moveInValues)].join(', ') : '-'
  }
}

export async function deleteGroup(groupId) {
  await apiRequest(`/api/groups/${Number(groupId)}`, {
    method: 'DELETE',
    headers: getAuthHeaders()
  })

  groupState.items = groupState.items.filter((group) => group.id !== Number(groupId))
  Object.keys(groupState.byListing).forEach((listingId) => {
    groupState.byListing[listingId] = groupState.byListing[listingId].filter(
      (group) => group.id !== Number(groupId)
    )
  })

  return { ok: true }
}

export async function leaveGroup(groupId) {
  await apiRequest(`/api/groups/${Number(groupId)}/leave`, {
    method: 'POST',
    headers: getAuthHeaders()
  })

  groupState.items = groupState.items.filter((group) => group.id !== Number(groupId))
  Object.keys(groupState.byListing).forEach((listingId) => {
    groupState.byListing[listingId] = groupState.byListing[listingId].filter(
      (group) => group.id !== Number(groupId)
    )
  })

  return { ok: true }
}
