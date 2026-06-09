import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue')
  },
  {
    path: '/books',
    name: 'Books',
    component: () => import('@/views/BookList.vue')
  },
  {
    path: '/readers',
    name: 'Readers',
    component: () => import('@/views/ReaderList.vue')
  },
  {
    path: '/borrow',
    name: 'Borrow',
    component: () => import('@/views/BorrowRecord.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
