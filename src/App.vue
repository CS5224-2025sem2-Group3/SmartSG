<template>
  <div class="app-shell">
    <header class="topbar">
      <div class="brand-block">
        <span class="brand-kicker">Off-Campus Home Search</span>
        <h1>SmartSG Student Living</h1>
        <p class="subtitle">Find a student-friendly rental, compare commute, and meet housemates who fit your lifestyle.</p>
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
:root {
  --bg-top: #eef8f4;
  --bg-bottom: #f8f4ea;
  --surface: rgba(255, 255, 255, 0.82);
  --surface-strong: #ffffff;
  --surface-border: rgba(148, 163, 184, 0.18);
  --text-main: #172033;
  --text-soft: #66758f;
  --primary: #0f766e;
  --primary-soft: #ddf6ef;
  --danger-soft: #fff1f2;
  --danger-text: #be123c;
  --shadow-soft: 0 18px 40px rgba(15, 23, 42, 0.08);
  --shadow-card: 0 16px 32px rgba(30, 41, 59, 0.08);
}

* {
  box-sizing: border-box;
}

html,
body,
#app {
  min-height: 100%;
}

body {
  margin: 0;
  font-family: "Segoe UI", "Avenir Next", "Helvetica Neue", sans-serif;
  background:
    radial-gradient(circle at top left, rgba(110, 231, 183, 0.24), transparent 28%),
    radial-gradient(circle at top right, rgba(251, 191, 36, 0.12), transparent 24%),
    linear-gradient(180deg, var(--bg-top) 0%, var(--bg-bottom) 100%);
  color: var(--text-main);
}

a {
  text-decoration: none;
  color: inherit;
  transition: color 0.18s ease, background 0.18s ease;
}

button {
  font: inherit;
}

.app-shell {
  min-height: 100vh;
  padding: 18px;
  position: relative;
}

.app-shell::before,
.app-shell::after {
  content: "";
  position: fixed;
  border-radius: 999px;
  pointer-events: none;
  filter: blur(8px);
  opacity: 0.5;
}

.app-shell::before {
  width: 240px;
  height: 240px;
  top: 110px;
  left: -60px;
  background: radial-gradient(circle, rgba(16, 185, 129, 0.2), transparent 68%);
}

.app-shell::after {
  width: 280px;
  height: 280px;
  right: -100px;
  bottom: 100px;
  background: radial-gradient(circle, rgba(245, 158, 11, 0.14), transparent 68%);
}

.topbar {
  background: var(--surface);
  border: 1px solid var(--surface-border);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  padding: 18px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 18px;
  border-radius: 24px;
  box-shadow: var(--shadow-soft);
}

.brand-block {
  position: relative;
}

.brand-kicker {
  display: inline-block;
  margin-bottom: 10px;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(221, 246, 239, 0.92);
  color: #0f766e;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.topbar h1 {
  margin: 0;
  font-size: clamp(1.45rem, 2.2vw, 2rem);
  letter-spacing: -0.03em;
}

.subtitle {
  margin: 6px 0 0;
  color: var(--text-soft);
  font-size: 14px;
}

.nav {
  display: flex;
  gap: 8px;
  font-weight: 600;
  align-items: center;
  flex-wrap: wrap;
}

.nav a {
  padding: 10px 14px;
  border-radius: 999px;
  color: #334155;
}

.nav a:hover {
  color: var(--primary);
  background: rgba(232, 240, 255, 0.9);
}

.nav a.router-link-active {
  color: var(--primary);
  background: var(--primary-soft);
  box-shadow: inset 0 0 0 1px rgba(15, 118, 110, 0.08);
}

.page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 28px 24px 40px;
}

.card {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.96) 0%, rgba(248, 250, 252, 0.94) 100%);
  border-radius: 22px;
  padding: 22px;
  box-shadow: var(--shadow-card);
  border: 1px solid rgba(226, 232, 240, 0.9);
}

.btn {
  border: none;
  border-radius: 999px;
  padding: 11px 16px;
  cursor: pointer;
  font-weight: 600;
  transition: transform 0.18s ease, box-shadow 0.18s ease, background 0.18s ease, color 0.18s ease;
}

.btn:hover {
  transform: translateY(-1px);
}

.btn-primary {
  background: linear-gradient(135deg, #0f766e 0%, #0f9b8e 55%, #2cbfae 100%);
  color: white;
  box-shadow: 0 12px 24px rgba(15, 118, 110, 0.24);
}

.btn-secondary {
  background: rgba(221, 246, 239, 0.95);
  color: var(--primary);
  box-shadow: inset 0 0 0 1px rgba(15, 118, 110, 0.08);
}

.btn-danger {
  background: var(--danger-soft);
  color: var(--danger-text);
}

.btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.input,
select {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.98);
  color: var(--text-main);
  transition: border-color 0.18s ease, box-shadow 0.18s ease, background 0.18s ease;
}

.input:focus,
select:focus {
  outline: none;
  border-color: rgba(15, 118, 110, 0.55);
  box-shadow: 0 0 0 4px rgba(45, 212, 191, 0.16);
  background: #fff;
}

.label {
  font-size: 13px;
  font-weight: 700;
  margin-bottom: 8px;
  display: block;
  color: #334155;
  letter-spacing: 0.01em;
}

.grid {
  display: grid;
  gap: 16px;
}

.muted {
  color: var(--text-soft);
}

.badge {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 999px;
  background: linear-gradient(135deg, #ddf6ef, #f0fdf4);
  color: #0f766e;
  font-size: 12px;
  font-weight: 700;
  border: 1px solid rgba(16, 185, 129, 0.12);
}

.section-title {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 1.2rem;
  letter-spacing: -0.02em;
}

.user-chip {
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.76);
  color: #334155;
  font-size: 14px;
  border: 1px solid rgba(148, 163, 184, 0.16);
}

@media (max-width: 900px) {
  .app-shell {
    padding: 12px;
  }

  .topbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .page {
    padding: 20px 8px 28px;
  }
}

@media (prefers-reduced-motion: reduce) {
  a,
  .btn,
  .input,
  select {
    transition: none;
  }
}
</style>
