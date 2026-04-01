import { reactive } from 'vue'
import { apiRequest } from '../api/apiClient'
import { getAuthHeaders } from './authService'

const favoriteState = reactive({
  items: [],
  loaded: false
})

function normalizeFavorites(payload = []) {
  return payload.map((item) => {
    if (typeof item === 'number') return item
    return Number(item.listingId ?? item.id)
  })
}

export async function loadFavorites() {
  const favorites = await apiRequest('/api/favorites', {
    headers: getAuthHeaders()
  })

  favoriteState.items = normalizeFavorites(favorites)
  favoriteState.loaded = true
  return favoriteState.items
}

export function getCurrentUserFavorites() {
  return favoriteState.items
}

export function isFavorite(listingId) {
  return favoriteState.items.includes(Number(listingId))
}

export async function addFavorite(listingId) {
  await apiRequest(`/api/favorites/${Number(listingId)}`, {
    method: 'POST',
    headers: getAuthHeaders()
  })

  if (!isFavorite(listingId)) {
    favoriteState.items = [...favoriteState.items, Number(listingId)]
  }
}

export async function removeFavorite(listingId) {
  await apiRequest(`/api/favorites/${Number(listingId)}`, {
    method: 'DELETE',
    headers: getAuthHeaders()
  })

  favoriteState.items = favoriteState.items.filter((id) => id !== Number(listingId))
}

export async function toggleFavorite(listingId) {
  if (isFavorite(listingId)) {
    await removeFavorite(listingId)
    return false
  }

  await addFavorite(listingId)
  return true
}
