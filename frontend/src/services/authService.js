import { reactive } from 'vue'
import { apiRequest } from '../api/apiClient'
import { store, persistStore } from '../store/mockStore'

const AUTH_STORAGE_KEY = 'smartsg-auth-session'

function loadStoredSession() {
  const raw = localStorage.getItem(AUTH_STORAGE_KEY)
  if (!raw) {
    return {
      user: null
    }
  }

  try {
    const parsed = JSON.parse(raw)
    return {
      user: parsed.user || null
    }
  } catch {
    return {
      user: null
    }
  }
}

const storedSession = loadStoredSession()

export const authState = reactive({
  user: storedSession.user,
  initialized: false
})

function persistAuthSession() {
  localStorage.setItem(
    AUTH_STORAGE_KEY,
    JSON.stringify({
      user: authState.user
    })
  )
}

function clearAuthSession() {
  authState.user = null
  localStorage.removeItem(AUTH_STORAGE_KEY)
}

function syncMockStoreUser(user) {
  store.currentUserId = user?.id ?? null
  store.currentUserName = user?.name ?? ''
  persistStore()
}

function setAuthenticatedUser(user) {
  authState.user = user
  persistAuthSession()
  syncMockStoreUser(user)
  return { ok: true, user }
}

export async function loginUser({ email, password }) {
  const response = await apiRequest('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({
      email: String(email).trim().toLowerCase(),
      password: String(password)
    })
  })

  return setAuthenticatedUser(response)
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

  return { ok: true, user: response }
}

export async function logoutUser() {
  try {
    await apiRequest('/api/auth/logout', {
      method: 'POST'
    })
  } finally {
    clearAuthSession()
    syncMockStoreUser(null)
  }
}

export async function fetchCurrentUser() {
  const user = await apiRequest('/api/auth/me')

  setAuthenticatedUser(user)
  return user
}

export async function initializeAuth() {
  if (authState.initialized) return authState.user

  if (!authState.user) {
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
