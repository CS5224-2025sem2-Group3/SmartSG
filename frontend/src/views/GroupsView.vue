<template>
  <section>
    <h2 class="section-title">My Groups</h2>
    
    <div v-if="loading" class="card">
      Loading groups...
    </div>

    <div v-else-if="error" class="card">
      {{ error }}
    </div>

    <div v-else-if="myGroups.length === 0" class="card">
      No groups yet. Go to a listing and start a housemate group.
    </div>

    <div v-else class="grid">
      <article class="card" v-for="group in myGroups" :key="group.id">
        <div class="group-header">
          <div>
            <h3 style="margin: 0 0 6px;">{{ group.listingTitle }}</h3>
            <p class="muted" style="margin: 0;">
              Status: {{ group.status }}
            </p>
          </div>
          <span class="badge">
            {{ group.curPeople }} / {{ group.requiredPeople }} people
          </span>
        </div>

        <div v-if="group.members.length" class="member-block">
          <h4>Members</h4>
          <ul>
            <li v-for="member in group.members" :key="member.userId">
              {{ member.name }}
              <span v-if="member.role"> · {{ member.role }}</span>
              <span v-if="member.budgetMax"> · Budget {{ member.budgetMax }}</span>
              <span v-if="member.moveInWindow"> · {{ member.moveInWindow }}</span>
              <span v-if="member.leasePreference"> · {{ member.leasePreference }} months</span>
            </li>
          </ul>
        </div>

        <div v-if="calculateGroupSummary(group)" class="summary-block">
          <h4>Group Summary</h4>
          <p><strong>Total Budget:</strong> SGD {{ calculateGroupSummary(group).totalBudget }}</p>
          <p><strong>Current Rent Per Person:</strong> SGD {{ calculateGroupSummary(group).perPerson.toFixed(0) }}</p>
          <p v-if="group.members.length > 1">
            <strong>Lease Info:</strong> {{ calculateGroupSummary(group).leaseIntersection }}
          </p>
          <p v-if="group.members.length > 1">
            <strong>Move-in Info:</strong> {{ calculateGroupSummary(group).moveInIntersection }}
          </p>
        </div>

        <div style="display: flex; gap: 10px; margin-top: 14px; flex-wrap: wrap;">
          <RouterLink class="btn btn-secondary" :to="`/listing/${group.listingId}`">
            View Listing
          </RouterLink>

          <button
            class="btn btn-primary"
            :disabled="group.curPeople < group.requiredPeople || group.status === 'closed' || !group.currentUserIsLeader"
            @click="handleConfirm(group.id)"
          >
            {{ group.status === 'closed' ? 'Confirmed' : 'Confirm Listing' }}
          </button>

          <button v-if="group.currentUserIsLeader" class="btn btn-danger" @click="handleDelete(group.id)">
            Delete Group
          </button>

          <button v-else class="btn btn-secondary" @click="handleLeave(group.id)">
            Leave Group
          </button>
        </div>

      </article>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import {
  getGroupsForCurrentUser,
  calculateGroupSummary,
  confirmGroupDecision,
  loadGroupsForCurrentUser,
  deleteGroup,
  leaveGroup
} from '../services/groupService'

const myGroups = computed(() => getGroupsForCurrentUser())
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    await loadGroupsForCurrentUser()
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
})

async function handleConfirm(groupId) {
  try {
    await confirmGroupDecision(groupId)
  } catch (err) {
    alert(err.message)
  }
}

async function handleDelete(groupId) {
  try {
    await deleteGroup(groupId)
    alert('Group deleted.')
  } catch (err) {
    alert(err.message)
  }
}

async function handleLeave(groupId) {
  try {
    await leaveGroup(groupId)
    alert('You have left the group.')
  } catch (err) {
    alert(err.message)
  }
}
</script>

<style scoped>
.group-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.member-block,
.summary-block {
  margin-top: 16px;
  padding: 14px;
  background: #f9fafb;
  border-radius: 12px;
}
</style>
