import { createRouter, createWebHashHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/books',
    name: 'Books',
    component: () => import('@/views/BookSearch.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/books/:id',
    name: 'BookDetail',
    component: () => import('@/views/BookDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/borrow-records',
    name: 'BorrowRecords',
    component: () => import('@/views/BorrowRecords.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/borrowing',
    name: 'Borrowing',
    component: () => import('@/views/Borrowing.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/overdue',
    name: 'Overdue',
    component: () => import('@/views/Overdue.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/fee-records',
    name: 'FeeRecords',
    component: () => import('@/views/FeeRecords.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/renew-logs',
    name: 'RenewLogs',
    component: () => import('@/views/RenewLogs.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
  } else {
    next()
  }
})

export default router
