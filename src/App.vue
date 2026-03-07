<template>
  <div class="app-shell">
    <header class="topbar">
      <div>
        <h1>SmartSG Student Living</h1>
        <p class="subtitle">A home. A roommate. A fresh start in SG.</p>
      </div>

      <nav class="nav">
        <template v-if="loggedIn">
          <RouterLink to="/search">Search</RouterLink>
          <RouterLink to="/favorites">Favorites</RouterLink>
          <RouterLink to="/groups">Groups</RouterLink>
          <RouterLink to="/invitations">Invitations</RouterLink>
          <RouterLink to="/profile">My Profile</RouterLink>
          <span class="user-chip">Hi, {{ currentUserName }}</span>
          <button class="btn btn-secondary" @click="handleLogout">Logout</button>
        </template>

        <template v-else>
          <RouterLink to="/login">Login</RouterLink>
          <RouterLink to="/register">Register</RouterLink>
        </template>
      </nav>
    </header>

    <main class="page">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { store } from './store/mockStore'
import { isLoggedIn, logoutUser } from './services/authService'

const router = useRouter()

const loggedIn = computed(() => isLoggedIn())
const currentUserName = computed(() => store.currentUserName)

function handleLogout() {
  logoutUser()
  router.push('/login')
}
</script>

<style>
* {
  box-sizing: border-box;
}

body {
  margin: 0;
  font-family: Arial, Helvetica, sans-serif;
  background: #f5f7fb;
  color: #1f2937;
}

a {
  text-decoration: none;
  color: inherit;
}

button {
  font: inherit;
}

.app-shell {
  min-height: 100vh;
}

.topbar {
  background: white;
  border-bottom: 1px solid #e5e7eb;
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.subtitle {
  margin: 4px 0 0;
  color: #6b7280;
  font-size: 14px;
}

.nav {
  display: flex;
  gap: 16px;
  font-weight: 600;
  align-items: center;
}

.nav a.router-link-active {
  color: #2563eb;
}

.page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.card {
  background: white;
  border-radius: 14px;
  padding: 18px;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.05);
  border: 1px solid #edf2f7;
}

.btn {
  border: none;
  border-radius: 10px;
  padding: 10px 14px;
  cursor: pointer;
  font-weight: 600;
}

.btn-primary {
  background: #2563eb;
  color: white;
}

.btn-secondary {
  background: #eef2ff;
  color: #1d4ed8;
}

.btn-danger {
  background: #fee2e2;
  color: #b91c1c;
}

.input,
select {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  background: white;
}

.label {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 6px;
  display: block;
}

.grid {
  display: grid;
  gap: 16px;
}

.muted {
  color: #6b7280;
}

.badge {
  display: inline-block;
  padding: 5px 10px;
  border-radius: 999px;
  background: #eef2ff;
  color: #3730a3;
  font-size: 12px;
  font-weight: 700;
}

.section-title {
  margin-top: 0;
}

.user-chip {
  padding: 8px 12px;
  border-radius: 999px;
  background: #f3f4f6;
  color: #374151;
  font-size: 14px;
}
</style>