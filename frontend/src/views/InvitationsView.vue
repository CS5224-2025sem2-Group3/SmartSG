<template>
  <section>
    <h2 class="section-title">My Invitations</h2>

    <div v-if="loading" class="card">
      Loading invitations...
    </div>

    <div v-else-if="error" class="card">
      {{ error }}
    </div>

    <div v-else-if="invitations.length === 0" class="card">
      No invitations yet.
    </div>

    <div v-else class="grid">
      <article class="card" v-for="inv in invitations" :key="inv.id">
        <h3 style="margin-top: 0;">Invitation from {{ inv.fromUserName }}</h3>
        <p><strong>Listing:</strong> {{ inv.listingTitle }}</p>
        <p><strong>Status:</strong> {{ inv.status }}</p>

        <div v-if="inv.status === 'pending'" style="display: flex; gap: 10px; margin-top: 14px;">
          <button class="btn btn-primary" @click="handleAccept(inv.id)">Accept</button>
          <button class="btn btn-danger" @click="handleReject(inv.id)">Reject</button>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import {
  getInvitationsForCurrentUser,
  loadInvitationsForCurrentUser,
  acceptInvitation,
  rejectInvitation
} from '../services/invitationService'

const invitations = computed(() => getInvitationsForCurrentUser())
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    await loadInvitationsForCurrentUser()
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
})

async function handleAccept(invitationId) {
  try {
    await acceptInvitation(invitationId)
  } catch (err) {
    alert(err.message)
  }
}

async function handleReject(invitationId) {
  try {
    await rejectInvitation(invitationId)
  } catch (err) {
    alert(err.message)
  }
}
</script>
