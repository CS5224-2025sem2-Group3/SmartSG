import { reactive } from 'vue'
import { apiRequest } from '../api/apiClient'
import { store, persistStore } from '../store/mockStore'

const profileState = reactive({
  current: null,
  loaded: false
})

const sleepHabitMap = {
  'Early Sleeper': 'EarlyBird',
  Regular: 'Regular',
  'Night Owl': 'NightOwl'
}

const cleanlinessMap = {
  Casual: 'Low',
  Average: 'Average',
  'Very Clean': 'High'
}

const smokingMap = {
  'No Smoking': 'No',
  'Okay with Smoking': 'Yes',
  'I Smoke': 'Yes'
}

function normalizeProfile(profile) {
  if (!profile) return null

  const leasePreference =
    profile.leasePreference === '' || profile.leasePreference == null
      ? null
      : Number(profile.leasePreference)

  return {
    ...profile,
    leasePreference: Number.isNaN(leasePreference) ? null : leasePreference,
    sleepHabit: sleepHabitMap[profile.sleepHabit] || profile.sleepHabit || 'Regular',
    cleanliness: cleanlinessMap[profile.cleanliness] || profile.cleanliness || 'Average',
    smoking: smokingMap[profile.smoking] || profile.smoking || 'No',
    gender: profile.gender || 'Male'
  }
}

function syncMockProfile(profile) {
  if (!store.currentUserId) return
  store.userProfiles[store.currentUserId] = { ...profile }
  persistStore()
}

export function getCurrentUserProfile() {
  return profileState.current
}

export async function loadCurrentUserProfile() {
  const profile = await apiRequest('/api/profile/me')

  profileState.current = normalizeProfile(profile)
  profileState.loaded = true
  syncMockProfile(profileState.current)
  return profileState.current
}

export async function saveUserProfile(profile) {
  const normalized = normalizeProfile(profile)

  await apiRequest('/api/profile/me', {
    method: 'PUT',
    body: JSON.stringify(normalized)
  })

  profileState.current = normalized
  profileState.loaded = true
  syncMockProfile(profileState.current)
  return profileState.current
}

export function getDefaultProfile() {
  return {
    budgetMax: 1500,
    moveInWindow: '2026-08-10',
    leasePreference: 12,
    sleepHabit: 'Regular',
    smoking: 'No',
    cleanliness: 'Average',
    gender: 'Male'
  }
}
