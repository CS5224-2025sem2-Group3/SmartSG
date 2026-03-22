<template>
  <div class="auth-wrap">
    <section class="card auth-card">
      <h2 class="section-title">Register</h2>
      <p class="muted">Create a mock account for this demo.</p>

      <div style="margin-bottom: 14px;">
        <label class="label">Name</label>
        <input v-model="form.name" class="input" type="text" placeholder="Enter your name" />
      </div>

      <div style="margin-bottom: 14px;">
        <label class="label">Email</label>
        <input v-model="form.email" class="input" type="email" placeholder="Enter your email" />
      </div>

      <div style="margin-bottom: 16px;">
        <label class="label">Password</label>
        <input v-model="form.password" class="input" type="password" placeholder="Create a password" />
      </div>

      <p v-if="error" class="error-text">{{ error }}</p>

      <button class="btn btn-primary auth-btn" :disabled="loading" @click="handleRegister">
        {{ loading ? 'Creating account...' : 'Register' }}
      </button>

      <p class="switch-text">
        Already have an account?
        <RouterLink to="/login">Login here</RouterLink>
      </p>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { registerUser } from '../services/authService'

const router = useRouter()

const form = reactive({
  name: '',
  email: '',
  password: ''
})

const error = ref('')
const loading = ref(false)

async function handleRegister() {
  error.value = ''

  if (!form.name || !form.email || !form.password) {
    error.value = 'Please fill in all fields.'
    return
  }

  if (form.password.length < 6) {
    error.value = 'Password must be at least 6 characters.'
    return
  }

  loading.value = true

  try {
    await registerUser(form)
    router.push('/search')
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-wrap {
  min-height: calc(100vh - 140px);
  display: grid;
  place-items: center;
}

.auth-card {
  width: 100%;
  max-width: 420px;
}

.auth-btn {
  width: 100%;
}

.error-text {
  color: #b91c1c;
  margin-bottom: 12px;
  font-size: 14px;
}

.switch-text {
  margin-top: 16px;
  font-size: 14px;
}
</style>
