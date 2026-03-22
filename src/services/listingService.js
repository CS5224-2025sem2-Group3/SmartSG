import { store } from '../store/mockStore'

export function getUniversities() {
  return store.universities
}

export function getAllListings() {
  return store.listings
}

export function getListingById(id) {
  return store.listings.find((item) => item.id === Number(id)) || null
}

export function searchListings(filters) {
  const {
    university,
    budgetMax,
    commuteMax,
    moveInWindow,
    leaseLength,
    facilities = []
  } = filters

  return store.listings
    .filter((listing) => listing.distanceKm[university] <= 8)
    .filter((listing) => listing.commuteTime[university] <= Number(commuteMax || 999))
    .filter((listing) => !leaseLength || listing.leaseOptions.includes(Number(leaseLength)))
    .filter((listing) => !moveInWindow || listing.availableFrom >= moveInWindow)
    .filter((listing) => !facilities.length || facilities.every((f) => listing.facilities.includes(f)))
    .map((listing) => {
      let budgetFit = false
      let recommendedHousemates = 0
      let minPeopleNeeded = 1

      if (listing.type === 'room') {
        budgetFit = listing.totalRent <= Number(budgetMax || 999999)
      } else {
        for (let people = 1; people <= Math.max(listing.rooms, 6); people++) {
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
    })
}

export function getRecommendedGroupSize(listing, budgetMax) {
  if (!listing) return { minPeopleNeeded: 1, perPerson: 0 }

  if (listing.type === 'room') {
    return { minPeopleNeeded: 1, perPerson: listing.totalRent }
  }

  for (let people = 1; people <= Math.max(listing.rooms, 6); people++) {
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