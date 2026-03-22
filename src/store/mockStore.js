import { reactive } from 'vue'

export const STORAGE_KEY = 'student-rental-mvp-store'

const initialData = {
  currentUserId: null,
  currentUserName: '',

  users: [
    { id: 1, name: 'Demo User', email: 'demo@test.com', password: '123456' },
    { id: 2, name: 'Alice', email: 'alice@test.com', password: '123456' },
    { id: 3, name: 'Ben', email: 'ben@test.com', password: '123456' },
    { id: 4, name: 'Cheryl', email: 'cheryl@test.com', password: '123456' },
    { id: 5, name: 'Daniel', email: 'daniel@test.com', password: '123456' }
  ],
  listings: [
    {
      id: 101,
      title: 'Common Room near Clementi',
      type: 'room',
      totalRent: 1200,
      availableFrom: '2026-08-05',
      moveInLabel: 'Early August',
      leaseOptions: [6, 12],
      commuteTime: { NUS: 18, NTU: 35, SMU: 30, SUTD: 42, OTHER: 28 },
      facilities: ['Near MRT', 'Fully Furnished', 'Cooking Allowed'],
      distanceKm: { NUS: 3.1, NTU: 11.5, SMU: 10.1, SUTD: 18.4, OTHER: 7.8 },
      rooms: 1,
      image: 'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=1200&q=80',
      status: 'open'
    },
    {
      id: 102,
      title: '2BR Apartment near Buona Vista',
      type: 'whole',
      totalRent: 3200,
      availableFrom: '2026-08-10',
      moveInLabel: 'Early August',
      leaseOptions: [12],
      commuteTime: { NUS: 15, NTU: 38, SMU: 24, SUTD: 35, OTHER: 20 },
      facilities: ['Near MRT', 'Fully Furnished', 'Cooking Allowed'],
      distanceKm: { NUS: 4.2, NTU: 13, SMU: 8.5, SUTD: 16.2, OTHER: 6.6 },
      rooms: 2,
      image: 'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=1200&q=80',
      status: 'open'
    },
    {
      id: 103,
      title: '3BR Whole Unit near Queenstown',
      type: 'whole',
      totalRent: 4200,
      availableFrom: '2026-08-14',
      moveInLabel: 'Mid August',
      leaseOptions: [6, 12],
      commuteTime: { NUS: 22, NTU: 40, SMU: 18, SUTD: 33, OTHER: 17 },
      facilities: ['Fully Furnished', 'Cooking Allowed'],
      distanceKm: { NUS: 5.8, NTU: 14.3, SMU: 6.3, SUTD: 14.8, OTHER: 5.1 },
      rooms: 3,
      image: 'https://images.unsplash.com/photo-1484154218962-a197022b5858?auto=format&fit=crop&w=1200&q=80',
      status: 'open'
    },
    {
      id: 104,
      title: 'Studio near Paya Lebar',
      type: 'whole',
      totalRent: 2500,
      availableFrom: '2026-08-01',
      moveInLabel: 'Early August',
      leaseOptions: [6],
      commuteTime: { NUS: 35, NTU: 50, SMU: 18, SUTD: 15, OTHER: 20 },
      facilities: ['Near MRT', 'Fully Furnished'],
      distanceKm: { NUS: 12.2, NTU: 22.5, SMU: 5.5, SUTD: 3.6, OTHER: 7 },
      rooms: 1,
      image: 'https://images.unsplash.com/photo-1494526585095-c41746248156?auto=format&fit=crop&w=1200&q=80',
      status: 'open'
    }
  ],

  favoritesByUser: {},
  userProfiles: {
    2: {
      budgetMax: 1500,
      moveInWindow: 'Early August',
      leasePreference: '12',
      sleepHabit: 'Regular',
      studyPreference: 'Quiet',
      smoking: 'No Smoking',
      cleanliness: 'Average'
    },
    3: {
      budgetMax: 1400,
      moveInWindow: 'Mid August',
      leasePreference: '6-12',
      sleepHabit: 'Night Owl',
      studyPreference: 'Okay with Normal Activity',
      smoking: 'Okay with Smoking',
      cleanliness: 'Casual'
    },
    4: {
      budgetMax: 1800,
      moveInWindow: 'Early August',
      leasePreference: '12',
      sleepHabit: 'Early Sleeper',
      studyPreference: 'Quiet',
      smoking: 'No Smoking',
      cleanliness: 'Very Clean'
    },
    5: {
      budgetMax: 1300,
      moveInWindow: 'Mid August',
      leasePreference: '6',
      sleepHabit: 'Regular',
      studyPreference: 'Okay with Normal Activity',
      smoking: 'I Smoke',
      cleanliness: 'Average'
    }
  },
  groups: [],
  invitations: []
}

function mergeListings(savedListings = []) {
  const savedById = new Map(savedListings.map((listing) => [listing.id, listing]))

  return initialData.listings.map((listing) => {
    const savedListing = savedById.get(listing.id)
    if (!savedListing) return listing

    return {
      ...listing,
      ...savedListing,
      image: listing.image
    }
  })
}

function loadState() {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (!raw) return initialData

  try {
    const parsed = JSON.parse(raw)
    return {
      ...initialData,
      ...parsed,
      listings: mergeListings(parsed.listings)
    }
  } catch {
    return initialData
  }
}

export const store = reactive(loadState())

export function persistStore() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(store))
}

export function getNextUserId() {
  if (!store.users.length) return 1
  return Math.max(...store.users.map((u) => u.id)) + 1
}
