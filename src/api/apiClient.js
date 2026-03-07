export async function apiRequest(url, options = {}) {
  const res = await fetch(url, {
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
      message = err.message || message
    } catch {}

    throw new Error(message)
  }

  if (res.status === 204) return null

  return await res.json()
}