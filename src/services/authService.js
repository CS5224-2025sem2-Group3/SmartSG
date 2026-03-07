import { store, persistStore, getNextUserId } from '../store/mockStore'

export function loginUser({ email, password }) {
  const normalizedEmail = String(email).trim().toLowerCase()

  const user = store.users.find(
    (u) => u.email.toLowerCase() === normalizedEmail && u.password === password
  )

  if (!user) {
    return { ok: false, message: 'Invalid email or password.' }
  }

  store.currentUserId = user.id
  store.currentUserName = user.name

  if (!store.favoritesByUser[user.id]) {
    store.favoritesByUser[user.id] = []
  }

  persistStore()
  return { ok: true, user }
}

export function registerUser({ name, email, password }) {
  const normalizedEmail = String(email).trim().toLowerCase()

  const exists = store.users.some(
    (u) => u.email.toLowerCase() === normalizedEmail
  )

  if (exists) {
    return { ok: false, message: 'Email already exists.' }
  }

  const newUser = {
    id: getNextUserId(),
    name: String(name).trim(),
    email: normalizedEmail,
    password: String(password)
  }

  store.users.push(newUser)
  store.currentUserId = newUser.id
  store.currentUserName = newUser.name
  store.favoritesByUser[newUser.id] = []

  persistStore()
  return { ok: true, user: newUser }
}

export function logoutUser() {
  store.currentUserId = null
  store.currentUserName = ''
  persistStore()
}

export function getCurrentUser() {
  if (!store.currentUserId) return null
  return store.users.find((u) => u.id === store.currentUserId) || null
}

export function isLoggedIn() {
  return !!store.currentUserId
}