<template>
  <div class="profile-wrap">
    <section class="card profile-card">
      <div class="profile-header">
        <span class="profile-kicker">Personal Preferences</span>
        <h2 class="section-title">My Profile</h2>
        <p class="muted">
          Other users can be matched with you based on this profile.
        </p>
      </div>

      <div class="field-block">
        <label class="label">Budget Max (SGD)</label>
        <input class="input" type="number" v-model.number="profile.budgetMax" />
      </div>

      <div class="field-block">
        <label class="label">Move-in Date</label>
        <input class="input" type="date" v-model="profile.moveInWindow" />
      </div>

      <div class="field-block">
        <label class="label">Lease Preference</label>
        <select v-model="profile.leasePreference">
          <option value="6">6 months</option>
          <option value="12">12 months</option>
          <option value="6-12">Flexible (6-12)</option>
        </select>
      </div>

      <div class="field-block">
        <label class="label">Sleep Habit</label>
        <select v-model="profile.sleepHabit">
          <option value="EarlyBird">EarlyBird</option>
          <option value="Regular">Regular</option>
          <option value="NightOwl">NightOwl</option>
        </select>
      </div>

      <div class="field-block">
        <label class="label">Smoking Preference</label>
        <select v-model="profile.smoking">
          <option value="No">No</option>
          <option value="Yes">Yes</option>
        </select>
      </div>

      <div class="field-block">
        <label class="label">Cleanliness Habit</label>
        <select v-model="profile.cleanliness">
          <option value="Low">Low</option>
          <option value="Average">Average</option>
          <option value="High">High</option>
        </select>
      </div>

      <div class="field-block field-block-last">
        <label class="label">Gender</label>
        <select v-model="profile.gender">
          <option value="Male">Male</option>
          <option value="Female">Female</option>
        </select>
      </div>

      <button class="btn btn-primary" :disabled="loading" @click="saveProfile">
        {{ loading ? 'Saving...' : 'Save Profile' }}
      </button>

      <p v-if="savedMessage" class="saved-text">{{ savedMessage }}</p>
    </section>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import {
  getCurrentUserProfile,
  loadCurrentUserProfile,
  saveUserProfile,
  getDefaultProfile
} from '../services/profileService'

const existingProfile = getCurrentUserProfile()

const profile = reactive(existingProfile || getDefaultProfile())

const savedMessage = ref('')
const loading = ref(false)

onMounted(async () => {
  try {
    const loadedProfile = await loadCurrentUserProfile()
    Object.assign(profile, loadedProfile || getDefaultProfile())
  } catch {}
})

async function saveProfile() {
  loading.value = true

  try {
    const savedProfile = await saveUserProfile({ ...profile })
    Object.assign(profile, savedProfile)
    savedMessage.value = 'Profile saved successfully.'

    setTimeout(() => {
      savedMessage.value = ''
    }, 2000)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.profile-wrap {
  display: flex;
  justify-content: center;
  padding-top: 6px;
}

.profile-card {
  width: 100%;
  max-width: 560px;
  position: relative;
  overflow: hidden;
}

.profile-card::before {
  content: "";
  position: absolute;
  inset: 0 0 auto 0;
  height: 140px;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.12), rgba(14, 165, 233, 0.04));
  pointer-events: none;
}

.profile-header {
  position: relative;
  z-index: 1;
  margin-bottom: 18px;
}

.profile-kicker {
  display: inline-block;
  margin-bottom: 10px;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(232, 240, 255, 0.96);
  color: #1d4ed8;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.field-block {
  margin-bottom: 12px;
}

.field-block-last {
  margin-bottom: 18px;
}

.saved-text {
  margin-top: 12px;
  color: #15803d;
  font-weight: 600;
}
</style>
