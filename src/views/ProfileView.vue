<template>
  <div class="profile-wrap">
    <section class="card profile-card">
      <h2 class="section-title">My Roommate Profile</h2>
      <p class="muted">
        Update your roommate preferences here. Other users can be matched with you based on this profile.
      </p>

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

      <button class="btn btn-primary" @click="saveProfile">
        Save Profile
      </button>

      <p v-if="savedMessage" class="saved-text">{{ savedMessage }}</p>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { getCurrentUserProfile, saveUserProfile, getDefaultProfile } from '../services/profileService'

const existingProfile = getCurrentUserProfile()

const profile = reactive(existingProfile || getDefaultProfile())

const savedMessage = ref('')

function saveProfile() {
  saveUserProfile({ ...profile })
  savedMessage.value = 'Profile saved successfully.'

  setTimeout(() => {
    savedMessage.value = ''
  }, 2000)
}
</script>

<style scoped>
.profile-wrap {
  display: flex;
  justify-content: center;
}

.profile-card {
  width: 100%;
  max-width: 560px;
}

.saved-text {
  margin-top: 12px;
  color: #15803d;
  font-weight: 600;
}
</style>