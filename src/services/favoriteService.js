import { store, persistStore } from '../store/mockStore'

export function getCurrentUserFavorites() {
  if (!store.currentUserId) return []
  return store.favoritesByUser[store.currentUserId] || []
}

export function isFavorite(listingId) {
  return getCurrentUserFavorites().includes(Number(listingId))
}

export function toggleFavorite(listingId) {
  if (!store.currentUserId) return

  const id = Number(listingId)
  const currentFavorites = store.favoritesByUser[store.currentUserId] || []

  if (currentFavorites.includes(id)) {
    store.favoritesByUser[store.currentUserId] = currentFavorites.filter((x) => x !== id)
  } else {
    store.favoritesByUser[store.currentUserId] = [...currentFavorites, id]
  }

  persistStore()
}