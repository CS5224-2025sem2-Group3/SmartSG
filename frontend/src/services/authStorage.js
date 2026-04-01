const AUTH_STORAGE_KEY = 'smartsg-auth-session'

export function loadStoredAuthSession() {
  const raw = localStorage.getItem(AUTH_STORAGE_KEY)
  if (!raw) {
    return {
      token: null,
      user: null
    }
  }

  try {
    const parsed = JSON.parse(raw)
    return {
      token: parsed.token || null,
      user: parsed.user || null
    }
  } catch {
    return {
      token: null,
      user: null
    }
  }
}

export function persistStoredAuthSession(session) {
  localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(session))
}

export function clearStoredAuthSession() {
  localStorage.removeItem(AUTH_STORAGE_KEY)
}

export function getStoredAuthToken() {
  return loadStoredAuthSession().token
}
