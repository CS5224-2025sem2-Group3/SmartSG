<template>
  <div class="auth-wrap">
    <section class="card auth-card">
      <h2 class="section-title">Login</h2>
      <p class="muted">Use a demo account or your registered account.</p>

      <div style="margin-bottom: 14px;">
        <label class="label">Email</label>
        <input v-model="form.email" class="input" type="email" placeholder="Enter your email" />
      </div>

      <div style="margin-bottom: 16px;">
        <label class="label">Password</label>
        <input v-model="form.password" class="input" type="password" placeholder="Enter your password" />
      </div>

      <p v-if="error" class="error-text">{{ error }}</p>

      <button class="btn btn-primary auth-btn" @click="handleLogin">Login</button>

      <div class="helper-block">
        <p><strong>Demo accounts:</strong></p>
        <p>demo@test.com / 123456</p>
        <p>alice@test.com / 123456</p>
        <p>ben@test.com / 123456</p>
      </div>

      <p class="switch-text">
        No account yet?
        <RouterLink to="/register">Register here</RouterLink>
      </p>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { loginUser } from '../services/authService'

const router = useRouter()

const form = reactive({
  email: '',
  password: ''
})

const error = ref('')

function handleLogin() {
  error.value = ''

  if (!form.email || !form.password) {
    error.value = 'Please enter email and password.'
    return
  }

  const result = loginUser(form)

  if (!result.ok) {
    error.value = result.message
    return
  }

  router.push('/search')
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

.helper-block {
  margin-top: 16px;
  padding: 14px;
  background: #f9fafb;
  border-radius: 12px;
  font-size: 14px;
}

.switch-text {
  margin-top: 16px;
  font-size: 14px;
}
</style>