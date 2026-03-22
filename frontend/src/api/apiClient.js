const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || ''

function buildUrl(url) {
  if (/^https?:\/\//.test(url)) return url
  return `${API_BASE_URL}${url}`
}

export async function apiRequest(url, options = {}) {
  const res = await fetch(buildUrl(url), {
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {})
    },
    ...options
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
