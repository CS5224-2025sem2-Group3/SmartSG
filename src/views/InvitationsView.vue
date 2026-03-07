<template>
  <section>
    <h2 class="section-title">My Invitations</h2>
    <p class="muted">Only invitations for the logged-in user are shown here.</p>

    <div v-if="invitations.length === 0" class="card">
      No invitations yet.
    </div>

    <div v-else class="grid">
      <article class="card" v-for="inv in invitations" :key="inv.id">
        <h3 style="margin-top: 0;">Invitation for {{ inv.toUserName }}</h3>
        <p><strong>Listing:</strong> {{ inv.listingTitle }}</p>
        <p><strong>From:</strong> {{ inv.fromUserName }}</p>
        <p><strong>Status:</strong> {{ inv.status }}</p>

        <div v-if="inv.status === 'pending'" style="display: flex; gap: 10px; margin-top: 14px;">
          <button class="btn btn-primary" @click="acceptInvitation(inv.id)">Accept</button>
          <button class="btn btn-danger" @click="rejectInvitation(inv.id)">Reject</button>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import {
  getInvitationsForCurrentUser,
  acceptInvitation,
  rejectInvitation
} from '../services/invitationService'

const invitations = computed(() => getInvitationsForCurrentUser())
</script>