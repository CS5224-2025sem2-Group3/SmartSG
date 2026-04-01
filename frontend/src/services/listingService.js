import { reactive } from 'vue'
import { apiRequest } from '../api/apiClient'

const listingState = reactive({
  listingsById: {}
})

const DEFAULT_LISTING_IMAGE = '/no-image.svg'

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
    image: listing.image || listing.imageUrl || DEFAULT_LISTING_IMAGE,
    facilities: Array.isArray(listing.facilities) ? listing.facilities : [],
    leaseOptions: Array.isArray(listing.leaseOptions) ? listing.leaseOptions : [],
    rooms: typeof listing.rooms === 'number' ? listing.rooms : listing.totalTenants || 1,
    status: listing.status || 'Available',
    moveInLabel: listing.moveInLabel || getMoveInLabel(listing.availableFrom)
  }

  if (typeof normalized.distance === 'undefined' && typeof normalized.distanceKm !== 'undefined') {
    normalized.distance = normalized.distanceKm
  }

  if (typeof normalized.distance === 'undefined' && typeof listing.uniDistances === 'object' && listing.uniDistances) {
    normalized.distance = Object.values(listing.uniDistances)[0] ?? null
  }

  if (normalized.distance && typeof normalized.distance === 'object') {
    normalized.distance = Object.values(normalized.distance)[0] ?? null
  }

  if (normalized.distanceKm && typeof normalized.distanceKm === 'object') {
    normalized.distanceKm = Object.values(normalized.distanceKm)[0] ?? null
  }

  return normalized
}

function addClientComputedFields(listing, budgetMax) {
  let budgetFit = false
  let recommendedHousemates = 0
  let minPeopleNeeded = 1

  for (let people = 1; people <= Math.max(listing.rooms, 6); people += 1) {
    const perPerson = listing.totalRent / people
    if (perPerson <= Number(budgetMax || 999999)) {
      minPeopleNeeded = people
      recommendedHousemates = people - 1
      budgetFit = true
      break
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
  const listings = await apiRequest('/api/listings')

  const normalized = listings.map(normalizeListing)
  cacheListings(normalized)
  return normalized
}

export function getCachedListingById(id) {
  return listingState.listingsById[Number(id)] || null
}

export async function getListingById(id) {
  const cached = getCachedListingById(id)
  if (cached && cached.facilities.length && cached.image !== DEFAULT_LISTING_IMAGE) {
    return cached
  }

  const listing = await apiRequest(`/api/listings/${Number(id)}`)

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
  const listings = await apiRequest(`/api/listings${query ? `?${query}` : ''}`)

  const normalized = listings
    .map(normalizeListing)
    .filter((listing) => !leaseLength || !listing.leaseOptions.length || listing.leaseOptions.includes(Number(leaseLength)))
    .filter((listing) => !facilities.length || facilities.every((f) => listing.facilities.includes(f)))
    .map((listing) => addClientComputedFields(listing, budgetMax))

  cacheListings(normalized)
  return normalized
}

export function getRecommendedGroupSize(listing, budgetMax) {
  if (!listing) return { minPeopleNeeded: 1, perPerson: 0 }

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
