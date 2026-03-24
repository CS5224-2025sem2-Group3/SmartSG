<template>
  <div v-if="loading" class="card">
    Loading listing...
  </div>

  <div v-else-if="listing">
    <section class="card" style="margin-bottom: 18px;">
      <div class="hero">
        <img :src="listing.image" :alt="listing.title" class="hero-image" />
        <div class="hero-content">
          <div class="hero-title-row">
            <h2 class="hero-title">{{ listing.title }}</h2>
            <span class="badge">{{ listing.type === 'whole' ? 'Whole Unit' : 'Room' }}</span>
          </div>

          <div class="hero-stats">
            <p><strong>Total Rent:</strong> SGD {{ listing.totalRent }}</p>
            <p><strong>Available From:</strong> {{ listing.availableFrom }} ({{ listing.moveInLabel }})</p>
            <p><strong>Lease Options:</strong> {{ listing.leaseOptions.join(' / ') }} months</p>
            <p><strong>Rooms:</strong> {{ listing.rooms }}</p>
            <p><strong>Status:</strong> {{ listing.status }}</p>
          </div>

          <div class="facility-row">
            <span class="mini-badge" v-for="f in listing.facilities" :key="f">{{ f }}</span>
          </div>
        </div>
      </div>
    </section>

    <section class="card" style="margin-bottom: 18px;">
      <h3 class="section-title">Decision Support</h3>

      <div v-if="listing.type === 'whole'">
        <p>
          This is a whole-unit listing. In this demo, the target group size is based on the number of rooms.
        </p>
        <p>
          <strong>Recommendation:</strong> At least {{ recommended.minPeopleNeeded }} people are needed.
          Estimated rent per person: SGD {{ recommended.perPerson }}
        </p>
      </div>

      <div v-else>
        <p>This is a room listing. You can decide individually without housemate matching.</p>
      </div>
    </section>

    <section v-if="listing.type === 'whole'" class="grid" style="grid-template-columns: 420px 1fr;">
      <div class="card filter-card">
        <h3 class="section-title">Fill in Your Ideal Housemate Profile</h3>
        <p class="muted filter-copy">
          This filter is separate from My Profile. Use it to describe the kind of housemate you want
          for this listing. It is only used for the current matching session.
        </p>

        <div class="form-block">
          <label class="label">Preferred Budget Max (SGD)</label>
          <input class="input" type="number" v-model.number="idealProfile.budgetMax" />
        </div>

        <div class="form-block">
          <label class="label">Preferred Move-in Date</label>
          <input class="input" type="date" v-model="idealProfile.moveInWindow" />
        </div>

        <div class="form-block">
          <label class="label">Preferred Lease Preference</label>
          <select v-model="idealProfile.leasePreference">
            <option value="6">6 months</option>
            <option value="12">12 months</option>
            <option value="6-12">Flexible (6-12)</option>
          </select>
        </div>

        <div class="form-block">
          <label class="label">Preferred Sleep Habit</label>
          <select v-model="idealProfile.sleepHabit">
            <option value="EarlyBird">EarlyBird</option>
            <option value="Regular">Regular</option>
            <option value="NightOwl">NightOwl</option>
          </select>
        </div>

        <div class="form-block">
          <label class="label">Smoking Tolerance</label>
          <select v-model="idealProfile.smoking">
            <option value="No">No</option>
            <option value="Yes">Yes</option>
          </select>
        </div>

        <div class="form-block form-block-last">
          <label class="label">Preferred Cleanliness Habit</label>
          <select v-model="idealProfile.cleanliness">
            <option value="Low">Low</option>
            <option value="Average">Average</option>
            <option value="High">High</option>
          </select>
        </div>

        <div class="action-row">
          <button class="btn btn-primary" @click="loadMatches">
            Match Housemates
          </button>
          <button class="btn btn-secondary" @click="createGroupOnly">
            Create Group
          </button>
        </div>

        <p v-if="group" class="muted" style="margin-top: 12px;">
          This listing already has a group: #{{ group.id }} (1 listing = 1 group)
        </p>
      </div>

      <div class="card">
        <h3 class="section-title">Recommended Housemates</h3>

        <div v-if="matches.length === 0" class="muted empty-state">
          Use the ideal housemate filter above to see recommended candidates.
        </div>

        <div v-else class="grid">
          <article class="candidate-card" v-for="candidate in matches" :key="candidate.id">
            <div>
              <h4 style="margin: 0 0 8px;">{{ candidate.name }}</h4>
              <p>Budget Max: SGD {{ candidate.budgetMax }}</p>
              <p>Move-in Window: {{ candidate.moveInWindow }}</p>
              <p>Lease Preference: {{ candidate.leasePreference }}</p>
              <p>Sleep Habit: {{ candidate.sleepHabit }}</p>
              <p>Study Preference: {{ candidate.studyPreference }}</p>
              <p>Smoking: {{ candidate.smoking }}</p>
              <p>Cleanliness: {{ candidate.cleanliness }}</p>
              <p><strong>Match Score:</strong> {{ candidate.matchScore }}</p>
            </div>

            <div>
              <button class="btn btn-primary" @click="invite(candidate.userId)">
                Invite to Group
              </button>
            </div>
          </article>
        </div>
      </div>
    </section>
  </div>

  <div v-else class="card">
    Listing not found.
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getListingById, getRecommendedGroupSize } from '../services/listingService'
import {
  getCurrentUserProfile,
  getDefaultProfile
} from '../services/profileService'
import {
  getOrCreateGroupForListing,
  getGroupByListingId,
  recommendHousemates
} from '../services/groupService'
import { inviteCandidateToGroup } from '../services/invitationService'

const route = useRoute()
const listingId = Number(route.params.id)
const listing = ref(null)
const loading = ref(true)

const idealProfile = reactive(getDefaultProfile())

const matches = ref([])
const group = computed(() => getGroupByListingId(listingId))

const recommended = computed(() => {
  return getRecommendedGroupSize(listing.value, idealProfile.budgetMax)
})

onMounted(async () => {
  try {
    listing.value = await getListingById(listingId)
  } finally {
    loading.value = false
  }
})

function loadMatches() {
  matches.value = recommendHousemates(listingId, idealProfile)
}

function createGroupOnly() {
  const myProfile = getCurrentUserProfile() || getDefaultProfile()
  getOrCreateGroupForListing(listingId, myProfile)
  alert('Group created or joined for this listing.')
}

function invite(candidateId) {
  if (!group.value) {
    alert('Please create a group first.')
    return
  }

  inviteCandidateToGroup(candidateId, listingId)
  alert('Invitation sent.')
}
</script>

<style scoped>
.hero {
  display: grid;
  grid-template-columns: 340px 1fr;
  gap: 18px;
  align-items: stretch;
}

.hero-image {
  width: 100%;
  height: 240px;
  object-fit: cover;
  border-radius: 18px;
  background: linear-gradient(135deg, #dbeafe, #e5e7eb);
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.12);
}

.hero-content {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.hero-title-row {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 14px;
  flex-wrap: wrap;
}

.hero-title {
  margin: 0;
  font-size: clamp(1.5rem, 2vw, 2rem);
  letter-spacing: -0.03em;
}

.hero-stats p {
  margin: 0 0 10px;
}

.facility-row {
  margin-top: 14px;
}

.mini-badge {
  background: rgba(241, 245, 249, 0.92);
  color: #374151;
  border-radius: 999px;
  padding: 6px 11px;
  font-size: 12px;
  margin-right: 8px;
  display: inline-block;
  margin-bottom: 8px;
  border: 1px solid rgba(148, 163, 184, 0.15);
}

.filter-card {
  position: relative;
  overflow: hidden;
}

.filter-card::before {
  content: "";
  position: absolute;
  inset: 0 0 auto 0;
  height: 150px;
  background: linear-gradient(135deg, rgba(96, 165, 250, 0.12), rgba(191, 219, 254, 0.04));
  pointer-events: none;
}

.filter-copy {
  margin-bottom: 16px;
  position: relative;
  z-index: 1;
}

.form-block {
  margin-bottom: 12px;
  position: relative;
  z-index: 1;
}

.form-block-last {
  margin-bottom: 18px;
}

.action-row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  position: relative;
  z-index: 1;
}

.candidate-card {
  border: 1px solid rgba(226, 232, 240, 0.95);
  border-radius: 18px;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.05);
}

.candidate-card p {
  margin: 0 0 8px;
}

.empty-state {
  min-height: 180px;
  display: grid;
  place-items: center;
  text-align: center;
  border: 1px dashed rgba(148, 163, 184, 0.35);
  border-radius: 18px;
  background: rgba(248, 250, 252, 0.72);
  padding: 18px;
}

@media (max-width: 900px) {
  .hero {
    grid-template-columns: 1fr;
  }
}
</style>
