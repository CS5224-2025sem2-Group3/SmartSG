import { reactive } from 'vue'
import { apiRequest } from '../api/apiClient'
import { store, persistStore } from '../store/mockStore'
import {
  clearStoredAuthSession,
  loadStoredAuthSession,
  persistStoredAuthSession
} from './authStorage'

const storedSession = loadStoredAuthSession()

export const authState = reactive({
  token: storedSession.token,
  user: storedSession.user,
  initialized: false
})

function persistAuthSession() {
  persistStoredAuthSession({
    token: authState.token,
    user: authState.user
  })
}

function clearAuthSession() {
  authState.token = null
  authState.user = null
  clearStoredAuthSession()
}

function syncMockStoreUser(user) {
  store.currentUserId = user?.id ?? null
  store.currentUserName = user?.name ?? ''
  persistStore()
}

function setAuthenticatedSession({ token = null, user }) {
  authState.token = token
  authState.user = user
  persistAuthSession()
  syncMockStoreUser(user)
  return { ok: true, user }
}

export function getAuthHeaders() {
  return authState.token
    ? { Authorization: `Bearer ${authState.token}` }
    : {}
}

export async function loginUser({ email, password }) {
  const response = await apiRequest('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({
      email: String(email).trim().toLowerCase(),
      password: String(password)
    })
  })

  return setAuthenticatedSession(response)
}

export async function registerUser({ name, email, password }) {
  const response = await apiRequest('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify({
      name: String(name).trim(),
      email: String(email).trim().toLowerCase(),
      password: String(password)
    })
  })

  return setAuthenticatedSession(response)
}

export async function logoutUser() {
  try {
    await apiRequest('/api/auth/logout', {
      method: 'POST',
      headers: getAuthHeaders()
    })
  } finally {
    clearAuthSession()
    syncMockStoreUser(null)
  }
}

export async function fetchCurrentUser() {
  const user = await apiRequest('/api/auth/me', {
    headers: getAuthHeaders()
  })

  setAuthenticatedSession({
    token: authState.token,
    user
  })
  return user
}

export async function initializeAuth() {
  if (authState.initialized) return authState.user

  if (!authState.token) {
    clearAuthSession()
    authState.initialized = true
    syncMockStoreUser(null)
    return null
  }

  try {
    const user = await fetchCurrentUser()
    authState.initialized = true
    return user
  } catch {
    clearAuthSession()
    syncMockStoreUser(null)
    authState.initialized = true
    return null
  }
}

export function getCurrentUser() {
  return authState.user
}

export function isLoggedIn() {
  return !!authState.user
}
