<template>
  <div v-if="listing">
    <section class="card" style="margin-bottom: 18px;">
      <div class="hero">
        <img :src="listing.image" :alt="listing.title" class="hero-image" />
        <div>
          <div style="display: flex; gap: 10px; align-items: center; margin-bottom: 12px;">
            <h2 style="margin: 0;">{{ listing.title }}</h2>
            <span class="badge">{{ listing.type === 'whole' ? 'Whole Unit' : 'Room' }}</span>
          </div>

          <p><strong>Total Rent:</strong> SGD {{ listing.totalRent }}</p>
          <p><strong>Available From:</strong> {{ listing.availableFrom }} ({{ listing.moveInLabel }})</p>
          <p><strong>Lease Options:</strong> {{ listing.leaseOptions.join(' / ') }} months</p>
          <p><strong>Rooms:</strong> {{ listing.rooms }}</p>
          <p><strong>Status:</strong> {{ listing.status }}</p>

          <div style="margin-top: 12px;">
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
        <p>This is a room listing. You can decide individually without roommate matching.</p>
      </div>
    </section>

    <section v-if="listing.type === 'whole'" class="grid" style="grid-template-columns: 420px 1fr;">
      <div class="card">
        <h3 class="section-title">Create Your Roommate Profile</h3>

        <div style="margin-bottom: 12px;">
          <label class="label">Budget Max (SGD)</label>
          <input class="input" type="number" v-model.number="profile.budgetMax" />
        </div>

        <div style="margin-bottom: 12px;">
          <label class="label">Move-in Window</label>
          <select v-model="profile.moveInWindow">
            <option value="Early August">Early August</option>
            <option value="Mid August">Mid August</option>
            <option value="Late August">Late August</option>
          </select>
        </div>

        <div style="margin-bottom: 12px;">
          <label class="label">Lease Preference</label>
          <select v-model="profile.leasePreference">
            <option value="6">6 months</option>
            <option value="12">12 months</option>
            <option value="6-12">Flexible (6-12)</option>
          </select>
        </div>

        <div style="margin-bottom: 12px;">
          <label class="label">Sleep Habit</label>
          <select v-model="profile.sleepHabit">
            <option value="Early Sleeper">Early Sleeper</option>
            <option value="Regular">Regular</option>
            <option value="Night Owl">Night Owl</option>
          </select>
        </div>

        <div style="margin-bottom: 12px;">
          <label class="label">Study Environment Preference</label>
          <select v-model="profile.studyPreference">
            <option value="Quiet">Quiet</option>
            <option value="Okay with Normal Activity">Okay with Normal Activity</option>
          </select>
        </div>

        <div style="margin-bottom: 12px;">
          <label class="label">Smoking Preference</label>
          <select v-model="profile.smoking">
            <option value="No Smoking">No Smoking</option>
            <option value="Okay with Smoking">Okay with Smoking</option>
            <option value="I Smoke">I Smoke</option>
          </select>
        </div>

        <div style="margin-bottom: 16px;">
          <label class="label">Cleanliness Habit</label>
          <select v-model="profile.cleanliness">
            <option value="Very Clean">Very Clean</option>
            <option value="Average">Average</option>
            <option value="Casual">Casual</option>
          </select>
        </div>

        <div style="display: flex; gap: 10px; flex-wrap: wrap;">
          <button class="btn btn-primary" @click="loadMatches">
            Match Roommates
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
        <h3 class="section-title">Recommended Roommates</h3>

        <div v-if="matches.length === 0" class="muted">
          Save your roommate profile first to see recommended candidates.
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
import { computed, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getListingById, getRecommendedGroupSize } from '../services/listingService'
import { saveUserProfile, getCurrentUserProfile, getDefaultProfile } from '../services/profileService'
import {
  getOrCreateGroupForListing,
  getGroupByListingId,
  recommendRoommates
} from '../services/groupService'
import { inviteCandidateToGroup } from '../services/invitationService'

const route = useRoute()
const listingId = Number(route.params.id)
const listing = getListingById(listingId)

const existingProfile = getCurrentUserProfile()

const profile = reactive(existingProfile || getDefaultProfile())

const matches = ref([])
const group = computed(() => getGroupByListingId(listingId))

const recommended = computed(() => {
  return getRecommendedGroupSize(listing, profile.budgetMax)
})

function loadMatches() {
  saveUserProfile({ ...profile })
  matches.value = recommendRoommates(listingId, profile)
}

function createGroupOnly() {
  saveUserProfile({ ...profile })
  getOrCreateGroupForListing(listingId, profile)
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
}

.hero-image {
  width: 100%;
  height: 240px;
  object-fit: cover;
  border-radius: 14px;
  background: #e5e7eb;
}

.mini-badge {
  background: #f3f4f6;
  color: #374151;
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 12px;
  margin-right: 8px;
  display: inline-block;
  margin-bottom: 8px;
}

.candidate-card {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 14px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}
</style>