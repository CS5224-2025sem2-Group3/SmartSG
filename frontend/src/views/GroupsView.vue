<template>
  <section>
    <h2 class="section-title">My Groups</h2>
    <p class="muted">Groups that include the logged-in user.</p>

    <div v-if="myGroups.length === 0" class="card">
      No groups yet. Go to a whole-unit listing and start a housemate group.
    </div>

    <div v-else class="grid">
      <article class="card" v-for="group in myGroups" :key="group.id">
        <div class="group-header">
          <div>
            <h3 style="margin: 0 0 6px;">{{ group.listingTitle }}</h3>
            <p class="muted" style="margin: 0;">
              Group #{{ group.id }} · Status: {{ group.status }}
            </p>
          </div>
          <span class="badge">
            {{ group.members.length }} / {{ group.requiredPeople }} people
          </span>
        </div>

        <div class="member-block">
          <h4>Members</h4>
          <ul>
            <li v-for="member in group.members" :key="member.userId">
              {{ member.name }} - Budget {{ member.budgetMax || '-' }} / {{ member.moveInWindow || '-' }} / {{ member.leasePreference || '-' }}
            </li>
          </ul>
        </div>

        <div v-if="calculateGroupSummary(group)" class="summary-block">
          <h4>Auto Summary</h4>
          <p><strong>Total Budget:</strong> SGD {{ calculateGroupSummary(group).totalBudget }}</p>
          <p><strong>Current Rent Per Person:</strong> SGD {{ calculateGroupSummary(group).perPerson.toFixed(0) }}</p>
          <p><strong>Lease Info:</strong> {{ calculateGroupSummary(group).leaseIntersection }}</p>
          <p><strong>Move-in Info:</strong> {{ calculateGroupSummary(group).moveInIntersection }}</p>
        </div>

        <div style="display: flex; gap: 10px; margin-top: 14px; flex-wrap: wrap;">
          <RouterLink class="btn btn-secondary" :to="`/listing/${group.listingId}`">
            View Listing
          </RouterLink>

          <button
            class="btn btn-primary"
            :disabled="group.members.length < group.requiredPeople || group.selected"
            @click="handleConfirm(group.id)"
          >
            {{ group.selected ? 'Selected' : 'Confirm Listing' }}
          </button>

          <button
            v-if="group.createdByUserId === currentUserId"
            class="btn btn-danger"
            @click="handleDelete(group.id)"
          >
            Delete Group
          </button>

          <button
            v-else
            class="btn btn-secondary"
            @click="handleLeave(group.id)"
          >
            Leave Group
          </button>
        </div>

      </article>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { store } from '../store/mockStore'
import {
  getGroupsForCurrentUser,
  calculateGroupSummary,
  confirmGroupDecision,
  deleteGroup,
  leaveGroup
} from '../services/groupService'

const myGroups = computed(() => getGroupsForCurrentUser())
const currentUserId = computed(() => store.currentUserId)

function handleConfirm(groupId) {
  confirmGroupDecision(groupId)
}

function handleDelete(groupId) {
  const result = deleteGroup(groupId)
  if (!result.ok) {
    alert(result.message)
    return
  }
  alert('Group deleted.')
}

function handleLeave(groupId) {
  const result = leaveGroup(groupId)
  if (!result.ok) {
    alert(result.message)
    return
  }
  alert('You have left the group.')
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