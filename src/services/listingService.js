import { reactive } from 'vue'
import { apiRequest } from '../api/apiClient'
import { authState } from './authService'

const listingState = reactive({
  listingsById: {}
})

function getAuthHeaders() {
  return authState.token
    ? { Authorization: `Bearer ${authState.token}` }
    : {}
}

function getMoveInLabel(availableFrom) {
  if (!availableFrom) return ''

  const date = new Date(availableFrom)
  const day = date.getUTCDate()

  if (day <= 10) return 'Early August'
  if (day <= 20) return 'Mid August'
  return 'Late August'
}

function cacheListings(listings = []) {
  listings.forEach((listing) => {
    listingState.listingsById[listing.id] = listing
  })
}

function normalizeListing(listing) {
  if (!listing) return null

  const normalized = {
    ...listing,
    moveInLabel: listing.moveInLabel || getMoveInLabel(listing.availableFrom)
  }

  if (typeof normalized.rooms !== 'number') {
    normalized.rooms = normalized.type === 'room' ? 1 : 1
  }

  return normalized
}

function addClientComputedFields(listing, budgetMax) {
  let budgetFit = false
  let recommendedHousemates = 0
  let minPeopleNeeded = 1

  if (listing.type === 'room') {
    budgetFit = listing.totalRent <= Number(budgetMax || 999999)
  } else {
    for (let people = 1; people <= Math.max(listing.rooms, 6); people += 1) {
      const perPerson = listing.totalRent / people
      if (perPerson <= Number(budgetMax || 999999)) {
        minPeopleNeeded = people
        recommendedHousemates = people - 1
        budgetFit = true
        break
      }
    }
  }

  return {
    ...listing,
    budgetFit,
    minPeopleNeeded,
    recommendedHousemates
  }
}

export async function getAllListings() {
  const listings = await apiRequest('/api/listings', {
    headers: getAuthHeaders()
  })

  const normalized = listings.map(normalizeListing)
  cacheListings(normalized)
  return normalized
}

export function getCachedListingById(id) {
  return listingState.listingsById[Number(id)] || null
}

export async function getListingById(id) {
  const cached = getCachedListingById(id)
  if (cached) return cached

  const listing = await apiRequest(`/api/listings/${Number(id)}`, {
    headers: getAuthHeaders()
  })

  const normalized = normalizeListing(listing)
  cacheListings([normalized])
  return normalized
}

export async function searchListings(filters) {
  const {
    university,
    budgetMax,
    commuteMax,
    moveInWindow,
    leaseLength,
    facilities = []
  } = filters

  const params = new URLSearchParams()

  if (budgetMax) params.set('budgetMax', String(budgetMax))
  if (moveInWindow) params.set('availableFrom', moveInWindow)
  if (university) params.set('university', university)
  if (commuteMax) params.set('distance', Number(commuteMax).toFixed(1))

  const query = params.toString()
  const listings = await apiRequest(`/api/listings${query ? `?${query}` : ''}`, {
    headers: getAuthHeaders()
  })

  const normalized = listings
    .map(normalizeListing)
    .filter((listing) => !leaseLength || listing.leaseOptions.includes(Number(leaseLength)))
    .filter((listing) => !facilities.length || facilities.every((f) => listing.facilities.includes(f)))
    .map((listing) => addClientComputedFields(listing, budgetMax))

  cacheListings(normalized)
  return normalized
}

export function getRecommendedGroupSize(listing, budgetMax) {
  if (!listing) return { minPeopleNeeded: 1, perPerson: 0 }

  if (listing.type === 'room') {
    return { minPeopleNeeded: 1, perPerson: listing.totalRent }
  }

  for (let people = 1; people <= Math.max(listing.rooms, 6); people += 1) {
    const perPerson = listing.totalRent / people
    if (perPerson <= Number(budgetMax || 999999)) {
      return {
        minPeopleNeeded: people,
        perPerson: perPerson.toFixed(0)
      }
    }
  }

  return {
    minPeopleNeeded: listing.rooms,
    perPerson: (listing.totalRent / listing.rooms).toFixed(0)
  }
}
