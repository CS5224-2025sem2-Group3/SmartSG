import { createRouter, createWebHistory } from 'vue-router'
import { isLoggedIn } from '../services/authService'

import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import SearchView from '../views/SearchView.vue'
import FavoritesView from '../views/FavoritesView.vue'
import ListingDetailView from '../views/ListingDetailView.vue'
import GroupsView from '../views/GroupsView.vue'
import InvitationsView from '../views/InvitationsView.vue'
import ProfileView from '../views/ProfileView.vue'

const routes = [
  {
  path: '/',
  redirect: () => (isLoggedIn() ? '/search' : '/login')
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { guestOnly: true }
  },
  {
    path: '/register',
    name: 'register',
    component: RegisterView,
    meta: { guestOnly: true }
  },
  {
    path: '/search',
    name: 'search',
    component: SearchView,
    meta: { requiresAuth: true }
  },
  {
    path: '/favorites',
    name: 'favorites',
    component: FavoritesView,
    meta: { requiresAuth: true }
  },
  {
    path: '/listing/:id',
    name: 'listing-detail',
    component: ListingDetailView,
    meta: { requiresAuth: true }
  },
  {
    path: '/groups',
    name: 'groups',
    component: GroupsView,
    meta: { requiresAuth: true }
  },
  {
    path: '/invitations',
    name: 'invitations',
    component: InvitationsView,
    meta: { requiresAuth: true }
  },
  {
  path: '/profile',
  name: 'profile',
  component: ProfileView,
  meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const loggedIn = isLoggedIn()

  if (to.meta.requiresAuth && !loggedIn) {
    return '/login'
  }

  if (to.meta.guestOnly && loggedIn) {
    return '/search'
  }

  return true
})

export default router