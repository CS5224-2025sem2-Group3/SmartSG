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
            <span class="badge">{{ listing.type }}</span>
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

      <p>
        This listing is designed for shared living, so group planning is based on the expected number of housemates.
      </p>
      <p>
        <strong>Recommendation:</strong> At least {{ recommended.minPeopleNeeded }} people are needed.
        Estimated rent per person: SGD {{ recommended.perPerson }}
      </p>
    </section>

    <section class="grid" style="grid-template-columns: 420px 1fr;">
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
          <select v-model.number="idealProfile.leasePreference">
            <option :value="6">6 months</option>
            <option :value="12">12 months</option>
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
          <button class="btn btn-primary" :disabled="matchesLoading" @click="loadMatches">
            Match Housemates
          </button>
          <button class="btn btn-secondary" :disabled="groupActionLoading" @click="createGroupOnly">
            Create Group
          </button>
        </div>

        <p v-if="group" class="muted" style="margin-top: 12px;">
          Current group: {{ group.curPeople }}/{{ group.requiredPeople }} people
        </p>

        <div v-if="group?.members?.length" class="member-preview">
          <h4>Current Members</h4>
          <ul>
            <li v-for="member in group.members" :key="member.userId">
              {{ member.name }}
              <span v-if="member.role"> · {{ member.role }}</span>
              <span v-if="member.budgetMax"> · Budget {{ member.budgetMax }}</span>
            </li>
          </ul>
        </div>
      </div>

      <div class="card">
        <h3 class="section-title">Recommended Housemates</h3>

        <p v-if="matchesError" class="muted" style="color: #b91c1c;">{{ matchesError }}</p>

        <div v-if="matchesLoading" class="muted empty-state">
          Finding matches...
        </div>

        <div v-else-if="matches.length === 0" class="muted empty-state">
          {{ group ? 'Use the ideal housemate filter above to see recommended candidates.' : 'Create a group first, then use the ideal housemate filter to see recommended candidates.' }}
        </div>

        <div v-else class="grid">
          <article class="candidate-card" v-for="candidate in matches" :key="candidate.userId">
            <div>
              <h4 style="margin: 0 0 8px;">{{ candidate.name }}</h4>
              <p>Budget Max: SGD {{ candidate.budgetMax }}</p>
              <p>Move-in Window: {{ candidate.moveInWindow }}</p>
              <p v-if="candidate.leasePreference">Lease Preference: {{ candidate.leasePreference }} months</p>
              <p v-if="candidate.sleepHabit">Sleep Habit: {{ candidate.sleepHabit }}</p>
              <p v-if="candidate.smoking">Smoking: {{ candidate.smoking }}</p>
              <p v-if="candidate.cleanliness">Cleanliness: {{ candidate.cleanliness }}</p>
              <p><strong>Match Score:</strong> {{ candidate.matchScore }}</p>
            </div>

            <div>
              <button class="btn btn-primary" :disabled="inviteLoadingId === candidate.userId" @click="invite(candidate.userId)">
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
import { getDefaultProfile } from '../services/profileService'
import {
  getOrCreateGroupForListing,
  getGroupByListingId,
  loadGroupsForCurrentUser,
  recommendHousemates
} from '../services/groupService'
import { inviteCandidateToGroup } from '../services/invitationService'

const route = useRoute()
const listingId = Number(route.params.id)
const listing = ref(null)
const loading = ref(true)

const idealProfile = reactive(getDefaultProfile())

const matches = ref([])
const matchesLoading = ref(false)
const groupActionLoading = ref(false)
const inviteLoadingId = ref(null)
const matchesError = ref('')
const group = ref(null)

const recommended = computed(() => {
  return getRecommendedGroupSize(listing.value, idealProfile.budgetMax)
})

onMounted(async () => {
  try {
    listing.value = await getListingById(listingId)
    await loadGroupsForCurrentUser()
    group.value = getGroupByListingId(listingId)
  } finally {
    loading.value = false
  }
})

async function loadMatches() {
  matchesLoading.value = true
  matchesError.value = ''

  try {
    matches.value = await recommendHousemates(listingId, idealProfile)
    group.value = getGroupByListingId(listingId)
  } catch (err) {
    matchesError.value = err.message
    matches.value = []
  } finally {
    matchesLoading.value = false
  }
}

async function createGroupOnly() {
  groupActionLoading.value = true

  try {
    group.value = await getOrCreateGroupForListing(listingId)
    alert('Group created for this listing.')
  } catch (err) {
    alert(err.message)
  } finally {
    groupActionLoading.value = false
  }
}

async function invite(candidateId) {
  if (!group.value) {
    alert('Please create a group first.')
    return
  }

  inviteLoadingId.value = candidateId

  try {
    await inviteCandidateToGroup(candidateId, listingId)
    alert('Invitation sent.')
  } catch (err) {
    alert(err.message)
  } finally {
    inviteLoadingId.value = null
  }
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

.member-preview {
  margin-top: 16px;
  position: relative;
  z-index: 1;
  padding: 14px;
  background: rgba(248, 250, 252, 0.84);
  border-radius: 14px;
}

.member-preview h4 {
  margin: 0 0 10px;
}

.member-preview ul {
  margin: 0;
  padding-left: 18px;
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
