<template>
  <div class="grid" style="grid-template-columns: 320px 1fr;">
    <section class="card filter-panel">
      <div class="panel-intro">
        <span class="panel-kicker">Student Rental Planner</span>
      <h2 class="section-title">Search Filters</h2>
        <p class="muted panel-copy">
          Narrow by commute, budget, and move-in timing to find a place that works for both campus life and shared living.
        </p>
      </div>

      <div class="field-row">
        <label class="label">University</label>
        <select v-model="filters.university">
          <option v-for="u in universities" :key="u.value" :value="u.value">
            {{ u.label }}
          </option>
        </select>
      </div>

      <div class="field-row">
        <label class="label">Monthly Budget Max (SGD)</label>
        <input class="input" v-model.number="filters.budgetMax" type="number" placeholder="e.g. 1500" />
      </div>

      <div class="field-row">
        <label class="label">Maximum Distance (km)</label>
        <input class="input" v-model.number="filters.commuteMax" type="number" step="0.1" placeholder="e.g. 8.0" />
      </div>

      <div class="field-row">
        <label class="label">Move-in Date</label>
        <input class="input" type="date" v-model="filters.moveInWindow"/>
        <p class="quick-select-label">Quick Select</p>
        <div class="quick-chip-row">
          <button
            v-for="intake in standardIntakes"
            :key="intake.value"
            type="button"
            class="quick-chip"
            :class="{ active: filters.moveInWindow === intake.value }"
            @click="selectStandardIntake(intake.value)"
          >
            {{ intake.label }}
          </button>
        </div>
      </div>

      <div class="field-row">
        <label class="label">Lease Length</label>
        <select v-model="filters.leaseLength">
          <option value="">Any</option>
          <option :value="6">6 months</option>
          <option :value="12">12 months</option>
          <option :value="24">24 months</option>
        </select>
      </div>

      <div class="field-row field-row-last">
        <label class="label">Facilities</label>

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
        <button class="btn btn-primary" :disabled="loading" @click="runSearch">
          {{ loading ? 'Searching...' : 'Search' }}
        </button>
        <button class="btn btn-secondary" :disabled="loading" @click="resetForm">Reset</button>
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
        <p class="result-copy">
          Designed for students balancing commute, rent, and the right housemate setup.
        </p>
      </div>

      <p v-if="error" class="error-text">{{ error }}</p>

      <div v-if="loading" class="card empty-card">
        Loading listings...
      </div>

      <div v-else class="list-grid">
        <div v-if="results.length === 0" class="card empty-card">
          No listings matched your current filters.
        </div>

        <article class="card listing-card" v-for="listing in results" :key="listing.id">
          <div class="listing-media">
            <img :src="listing.image" :alt="listing.title" class="listing-image" />
          </div>

          <div class="listing-body">
            <div class="title-row">
              <h3>{{ listing.title }}</h3>
              <span class="badge">{{ listing.type }}</span>
            </div>

            <p><strong>Total Rent:</strong> SGD {{ listing.totalRent }}</p>
            <p><strong>Distance to {{ appliedFilters.university }}:</strong> {{ listing.distance ?? '-' }} km</p>
            <p><strong>Available:</strong> {{ listing.availableFrom }}</p>
            <p>
              <strong>Lease Options:</strong>
              {{ listing.leaseOptions.length ? `${listing.leaseOptions.join(' / ')} months` : 'Not specified' }}
            </p>

            <div class="facility-row" v-if="listing.facilities.length">
              <span class="mini-badge" v-for="f in listing.facilities" :key="f">{{ f }}</span>
            </div>

            <div class="actions">
              <button class="btn btn-secondary" :disabled="favoriteLoadingId === listing.id" @click="toggleFav(listing.id)">
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
import { onMounted, reactive, ref } from 'vue'
import { searchListings } from '../services/listingService'
import { UNIVERSITIES } from '../constants/universities'
import { loadFavorites, toggleFavorite, isFavorite } from '../services/favoriteService'

const universities = UNIVERSITIES
const standardIntakes = [
  { label: 'AY26/27 Semester 1', value: '2026-08-01' },
  { label: 'AY26/27 Semester 2', value: '2027-01-01' }
]

const filters = reactive({
  university: 'NUS',
  budgetMax: 1500,
  commuteMax: 8,
  moveInWindow: '',
  leaseLength: '',
  facilities: []
})

const results = ref([])
const loading = ref(false)
const error = ref('')
const favoriteLoadingId = ref(null)
const appliedFilters = reactive({
  university: filters.university,
  budgetMax: filters.budgetMax,
  commuteMax: filters.commuteMax,
  moveInWindow: filters.moveInWindow,
  leaseLength: filters.leaseLength,
  facilities: [...filters.facilities]
})

function syncAppliedFilters() {
  appliedFilters.university = filters.university
  appliedFilters.budgetMax = filters.budgetMax
  appliedFilters.commuteMax = filters.commuteMax
  appliedFilters.moveInWindow = filters.moveInWindow
  appliedFilters.leaseLength = filters.leaseLength
  appliedFilters.facilities = [...filters.facilities]
}

function selectStandardIntake(date) {
  filters.moveInWindow = date
}

async function runSearch() {
  loading.value = true
  error.value = ''

  try {
    results.value = await searchListings(filters)
    syncAppliedFilters()
  } catch (err) {
    error.value = err.message
    results.value = []
  } finally {
    loading.value = false
  }
}

async function resetForm() {
  filters.university = 'NUS'
  filters.budgetMax = 1500
  filters.commuteMax = 8
  filters.moveInWindow = ''
  filters.leaseLength = ''
  filters.facilities = []
  await runSearch()
}

async function toggleFav(id) {
  favoriteLoadingId.value = id

  try {
    await toggleFavorite(id)
  } catch (err) {
    error.value = err.message
  } finally {
    favoriteLoadingId.value = null
  }
}

onMounted(async () => {
  loading.value = true
  error.value = ''

  try {
    await loadFavorites()
    if (universities.length && !universities.some((u) => u.value === filters.university)) {
      filters.university = universities[0].value
    }
    results.value = await searchListings(filters)
    syncAppliedFilters()
  } catch (err) {
    error.value = err.message
    results.value = []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.result-header {
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: end;
  flex-wrap: wrap;
}

.list-grid {
  display: grid;
  gap: 16px;
}

.filter-panel {
  position: relative;
  overflow: hidden;
}

.filter-panel::before {
  content: "";
  position: absolute;
  inset: 0 0 auto 0;
  height: 160px;
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.12), rgba(245, 158, 11, 0.05));
  pointer-events: none;
}

.panel-intro,
.field-row {
  position: relative;
  z-index: 1;
}

.panel-intro {
  margin-bottom: 18px;
}

.panel-kicker {
  display: inline-block;
  margin-bottom: 10px;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(221, 246, 239, 0.96);
  color: #0f766e;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.panel-copy {
  max-width: 28ch;
}

.field-row {
  margin-bottom: 14px;
}

.quick-chip-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 8px;
}

.quick-select-label {
  margin: 10px 0 0;
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.03em;
  text-transform: uppercase;
}

.quick-chip {
  border: 1px solid rgba(15, 118, 110, 0.14);
  background: rgba(221, 246, 239, 0.75);
  color: #0f766e;
  border-radius: 999px;
  padding: 6px 10px;
  font-weight: 600;
  font-size: 12px;
  cursor: pointer;
  transition: background 0.18s ease, border-color 0.18s ease, transform 0.18s ease;
}

.quick-chip:hover {
  transform: translateY(-1px);
  background: rgba(221, 246, 239, 0.95);
}

.quick-chip.active {
  background: rgba(221, 246, 239, 0.96);
  border-color: rgba(15, 118, 110, 0.28);
  box-shadow: inset 0 0 0 1px rgba(15, 118, 110, 0.08);
}

.field-row-last {
  margin-bottom: 16px;
}

.result-copy {
  max-width: 34ch;
  margin: 0;
  color: #5b6b85;
  text-align: right;
}

.error-text {
  color: #b91c1c;
  margin: 0 0 16px;
}

.empty-card {
  text-align: center;
  color: #64748b;
}

.listing-card {
  display: grid;
  grid-template-columns: 280px 1fr;
  padding: 0;
  overflow: hidden;
}

.listing-media {
  position: relative;
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
  background: rgba(241, 245, 249, 0.92);
  color: #374151;
  border-radius: 999px;
  padding: 5px 10px;
  font-size: 12px;
  border: 1px solid rgba(148, 163, 184, 0.15);
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
  color: #334155;
}

@media (max-width: 980px) {
  .listing-card {
    grid-template-columns: 1fr;
  }

  .listing-image {
    min-height: 240px;
  }

  .result-copy {
    text-align: left;
  }
}
</style>
