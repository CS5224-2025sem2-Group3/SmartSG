import { store, persistStore } from '../store/mockStore'

export function getCurrentUserProfile() {
  if (!store.currentUserId) return null
  return store.userProfiles[store.currentUserId] || null
}

export function saveUserProfile(profile) {
  if (!store.currentUserId) return

  store.userProfiles[store.currentUserId] = { ...profile }
  persistStore()
}

export function getDefaultProfile() {
  return {
    budgetMax: 1500,
    moveInWindow: 'Early August',
    leasePreference: '12',
    sleepHabit: 'Regular',
    studyPreference: 'Quiet',
    smoking: 'No Smoking',
    cleanliness: 'Average'
  }
}
