<template>
  <div class="grid" style="grid-template-columns: 320px 1fr;">
    <section class="card">
      <h2 class="section-title">Search Filters</h2>

      <div style="margin-bottom: 14px;">
        <label class="label">University</label>
        <select v-model="filters.university">
          <option v-for="u in universities" :key="u.value" :value="u.value">
            {{ u.label }}
          </option>
        </select>
      </div>

      <div style="margin-bottom: 14px;">
        <label class="label">Monthly Budget Max (SGD)</label>
        <input class="input" v-model.number="filters.budgetMax" type="number" placeholder="e.g. 1500" />
      </div>

      <div style="margin-bottom: 14px;">
        <label class="label">Maximum Commute Time (mins)</label>
        <input class="input" v-model.number="filters.commuteMax" type="number" placeholder="e.g. 30" />
      </div>

      <div style="margin-bottom: 14px;">
        <label class="label">Move-in Window</label>
        <select v-model="filters.moveInWindow">
          <option value="">Any</option>
          <option value="Early August">Early August</option>
          <option value="Mid August">Mid August</option>
          <option value="Late August">Late August</option>
        </select>
      </div>

      <div style="margin-bottom: 14px;">
        <label class="label">Lease Length</label>
        <select v-model="filters.leaseLength">
          <option value="">Any</option>
          <option :value="6">6 months</option>
          <option :value="12">12 months</option>
        </select>
      </div>

      <div style="margin-bottom: 16px;">
        <label class="label">Facilities</label>

        <label class="checkbox-row">
          <input type="checkbox" value="Near MRT" v-model="filters.facilities" />
          Near MRT
        </label>
        <label class="checkbox-row">
          <input type="checkbox" value="Fully Furnished" v-model="filters.facilities" />
          Fully Furnished
        </label>
        <label class="checkbox-row">
          <input type="checkbox" value="Cooking Allowed" v-model="filters.facilities" />
          Cooking Allowed
        </label>
      </div>

      <div style="display: flex; gap: 10px;">
        <button class="btn btn-primary" @click="runSearch">Search</button>
        <button class="btn btn-secondary" @click="resetForm">Reset</button>
      </div>

    </section>

    <section>
      <div class="result-header">
        <div>
          <h2 style="margin: 0;">Search Results</h2>
          <p class="muted" style="margin: 6px 0 0;">
            {{ results.length }} matching listings
          </p>
        </div>
      </div>

      <div class="list-grid">
        <article class="card listing-card" v-for="listing in results" :key="listing.id">
          <img :src="listing.image" :alt="listing.title" class="listing-image" />

          <div class="listing-body">
            <div class="title-row">
              <h3>{{ listing.title }}</h3>
              <span class="badge">{{ listing.type === 'whole' ? 'Whole Unit' : 'Room' }}</span>
            </div>

            <p><strong>Total Rent:</strong> SGD {{ listing.totalRent }}</p>
            <p><strong>Commute Time:</strong> {{ listing.commuteTime[filters.university] }} mins</p>
            <p><strong>Available:</strong> {{ listing.availableFrom }} / {{ listing.moveInLabel }}</p>
            <p><strong>Lease Options:</strong> {{ listing.leaseOptions.join(' / ') }} months</p>

            <p v-if="listing.type === 'whole'">
              <strong>Suggested Minimum Group Size:</strong>
              At least {{ listing.minPeopleNeeded }} people to fit budget
            </p>

            <p v-else>
              <strong>Budget Fit:</strong>
              <span :style="{ color: listing.budgetFit ? '#15803d' : '#b91c1c' }">
                {{ listing.budgetFit ? 'Within Budget' : 'Over Budget' }}
              </span>
            </p>

            <div class="facility-row">
              <span class="mini-badge" v-for="f in listing.facilities" :key="f">{{ f }}</span>
            </div>

            <div class="actions">
              <button class="btn btn-secondary" @click="toggleFav(listing.id)">
                {{ isFavorite(listing.id) ? 'Remove Favorite' : 'Favorite' }}
              </button>

              <RouterLink class="btn btn-primary fake-btn" :to="`/listing/${listing.id}`">
                View Details
              </RouterLink>
            </div>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { getUniversities, searchListings } from '../services/listingService'
import { toggleFavorite, isFavorite } from '../services/favoriteService'

const universities = getUniversities()

const filters = reactive({
  university: 'NUS',
  budgetMax: 1500,
  commuteMax: 30,
  moveInWindow: '',
  leaseLength: '',
  facilities: []
})

const results = ref(searchListings(filters))

function runSearch() {
  results.value = searchListings(filters)
}

function resetForm() {
  filters.university = 'NUS'
  filters.budgetMax = 1500
  filters.commuteMax = 30
  filters.moveInWindow = ''
  filters.leaseLength = ''
  filters.facilities = []
  runSearch()
}

function toggleFav(id) {
  toggleFavorite(id)
}
</script>

<style scoped>
.result-header {
  margin-bottom: 16px;
}

.list-grid {
  display: grid;
  gap: 16px;
}

.listing-card {
  display: grid;
  grid-template-columns: 280px 1fr;
  padding: 0;
  overflow: hidden;
}

.listing-image {
  width: 100%;
  height: 100%;
  min-height: 220px;
  object-fit: cover;
  background: #e5e7eb;
}

.listing-body {
  padding: 18px;
}

.title-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.title-row h3 {
  margin: 0;
}

.facility-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin: 12px 0;
}

.mini-badge {
  background: #f3f4f6;
  color: #374151;
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 12px;
}

.actions {
  display: flex;
  gap: 10px;
  margin-top: 16px;
}

.fake-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.checkbox-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 8px 0;
}
</style>