<template>
  <section>
    <h2 class="section-title">My Favorites</h2>
    <p class="muted">Shortlisted homes and shared-living options you want to revisit.</p>

    <div v-if="loading" class="card">
      Loading favorites...
    </div>

    <div v-else-if="error" class="card error-card">
      {{ error }}
    </div>

    <div v-else-if="favoriteListings.length === 0" class="card">
      You have no favorite listings yet.
    </div>

    <div v-else class="grid" style="grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));">
      <article class="card favorite-card" v-for="listing in favoriteListings" :key="listing.id">
        <img :src="listing.image" :alt="listing.title" class="favorite-image" />

        <div class="favorite-body">
          <div class="favorite-title-row">
            <h3 class="favorite-title">{{ listing.title }}</h3>
            <span class="badge">{{ listing.type === 'whole' ? 'Whole Unit' : 'Room' }}</span>
          </div>

          <p><strong>Rent:</strong> SGD {{ listing.totalRent }}</p>
          <p><strong>Available:</strong> {{ listing.availableFrom }}</p>
          <p><strong>Set-up:</strong> {{ listing.rooms }} room{{ listing.rooms > 1 ? 's' : '' }}</p>

          <div class="favorite-actions">
            <RouterLink class="btn btn-primary" :to="`/listing/${listing.id}`">View Details</RouterLink>
            <button class="btn btn-secondary" @click="handleToggleFavorite(listing.id)">Remove</button>
          </div>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getCurrentUserFavorites, loadFavorites, removeFavorite } from '../services/favoriteService'
import { getAllListings } from '../services/listingService'

const favoriteListings = ref([])
const loading = ref(true)
const error = ref('')

function refreshFavorites(listings) {
  const favorites = getCurrentUserFavorites()
  favoriteListings.value = listings.filter((listing) => favorites.includes(listing.id))
}

onMounted(async () => {
  try {
    await loadFavorites()
    const listings = await getAllListings()
    refreshFavorites(listings)
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
})

async function handleToggleFavorite(listingId) {
  try {
    await removeFavorite(listingId)
    favoriteListings.value = favoriteListings.value.filter((listing) => listing.id !== Number(listingId))
  } catch (err) {
    error.value = err.message
  }
}
</script>

<style scoped>
.error-card {
  color: #b91c1c;
}

.favorite-card {
  padding: 0;
  overflow: hidden;
}

.favorite-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  background: #e5e7eb;
}

.favorite-body {
  padding: 18px;
}

.favorite-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.favorite-title {
  margin: 0;
}

.favorite-actions {
  display: flex;
  gap: 10px;
  margin-top: 14px;
  flex-wrap: wrap;
}
</style>
