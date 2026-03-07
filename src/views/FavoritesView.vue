<template>
  <section>
    <h2 class="section-title">My Favorites</h2>
    <p class="muted">Saved per logged-in user.</p>

    <div v-if="favoriteListings.length === 0" class="card">
      You have no favorite listings yet.
    </div>

    <div v-else class="grid" style="grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));">
      <article class="card" v-for="listing in favoriteListings" :key="listing.id">
        <h3 style="margin-top: 0;">{{ listing.title }}</h3>
        <p><strong>Type:</strong> {{ listing.type === 'whole' ? 'Whole Unit' : 'Room' }}</p>
        <p><strong>Rent:</strong> SGD {{ listing.totalRent }}</p>
        <p><strong>Available:</strong> {{ listing.availableFrom }}</p>

        <div style="display: flex; gap: 10px; margin-top: 14px;">
          <RouterLink class="btn btn-primary" :to="`/listing/${listing.id}`">View Details</RouterLink>
          <button class="btn btn-secondary" @click="toggleFavorite(listing.id)">Remove</button>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { getCurrentUserFavorites, toggleFavorite } from '../services/favoriteService'
import { getAllListings } from '../services/listingService'

const favoriteListings = computed(() => {
  const favorites = getCurrentUserFavorites()
  const listings = getAllListings()
  return listings.filter((listing) => favorites.includes(listing.id))
})
</script>