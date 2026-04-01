const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || ''

import { getStoredAuthToken } from '../services/authStorage'

function buildUrl(url) {
  if (/^https?:\/\//.test(url)) return url
  return `${API_BASE_URL}${url}`
}

export async function apiRequest(url, options = {}) {
  const token = getStoredAuthToken()

  const res = await fetch(buildUrl(url), {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.headers || {})
    }
  })

  if (!res.ok) {
    let message = 'Request failed'

    try {
      const err = await res.json()
      message = err.message || err.error?.message || message
    } catch {}

    throw new Error(message)
  }

  if (res.status === 204) return null

  return await res.json()
}
