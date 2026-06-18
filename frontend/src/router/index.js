import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/data-screen',
    name: 'DataScreen',
    component: () => import('@/views/statistics/DataScreen.vue'),
    meta: {
      fullscreen: true,
      title: '数据大屏'
    }
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
  },
  {
    path: '/activities',
    name: 'Activities',
    component: () => import('@/views/ActivityList.vue')
  },
  {
    path: '/activities/:id',
    name: 'ActivityDetail',
    component: () => import('@/views/ActivityDetail.vue'),
    meta: { title: '活动详情' }
  },
  {
    path: '/statistics/collection',
    name: 'CollectionStats',
    component: () => import('@/views/statistics/CollectionStats.vue'),
    meta: { title: '馆藏统计' }
  },
  {
    path: '/statistics/borrow',
    name: 'BorrowStats',
    component: () => import('@/views/statistics/BorrowStats.vue'),
    meta: { title: '借阅统计' }
  },
  {
    path: '/statistics/reader',
    name: 'ReaderStats',
    component: () => import('@/views/statistics/ReaderStats.vue'),
    meta: { title: '读者统计' }
  },
  {
    path: '/statistics/hot',
    name: 'HotResources',
    component: () => import('@/views/statistics/HotResources.vue'),
    meta: { title: '热门资源' }
  },
  {
    path: '/statistics/fee',
    name: 'FeeStats',
    component: () => import('@/views/statistics/FeeStats.vue'),
    meta: { title: '费用统计' }
  },
  {
    path: '/statistics/report',
    name: 'OperationReport',
    component: () => import('@/views/statistics/OperationReport.vue'),
    meta: { title: '运营报告' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
