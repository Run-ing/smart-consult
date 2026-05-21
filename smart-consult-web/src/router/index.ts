import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import HomeView from '../views/HomeView.vue'
import ProfileSetupView from '../views/ProfileSetupView.vue'
import { getStoredUser, getToken } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/home'
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/home',
      name: 'home',
      component: HomeView,
      meta: {
        requiresAuth: true
      }
    },
    {
      path: '/profile-setup',
      name: 'profile-setup',
      component: ProfileSetupView,
      meta: {
        requiresAuth: true
      }
    }
  ]
})

router.beforeEach((to) => {
  const token = getToken()
  const user = getStoredUser()
  if (to.meta.requiresAuth && !token) {
    return '/login'
  }
  if (token && user && !user.profileCompleted && to.path !== '/profile-setup') {
    return '/profile-setup'
  }
  if (to.path === '/login' && token) {
    return user?.profileCompleted === false ? '/profile-setup' : '/home'
  }
  if (to.path === '/profile-setup' && token && user?.profileCompleted) {
    return '/home'
  }
  return true
})

export default router
