import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: () => import('../views/LoginView.vue') },
    {
      path: '/',
      component: () => import('../layouts/MainLayout.vue'),
      children: [
        { path: '', redirect: '/dashboard' },
        { path: 'dashboard', component: () => import('../views/DashboardView.vue') },
        { path: 'accidents', component: () => import('../views/AccidentView.vue') },
        { path: 'map-analysis', component: () => import('../views/MapAnalysisView.vue') },
        { path: 'warnings', component: () => import('../views/WarningView.vue') },
        { path: 'reports', component: () => import('../views/ReportView.vue') },
        { path: 'three-risk', component: () => import('../views/ThreeRiskView.vue') },
        { path: 'ai-lab', component: () => import('../views/AiLabView.vue') },
        { path: 'admin-users', component: () => import('../views/AdminUsersView.vue') },
        { path: 'admin-dicts', component: () => import('../views/AdminDictView.vue') },
      ],
    },
  ],
})

router.beforeEach((to) => {
  const authStore = useAuthStore()
  if (to.path === '/login') return true
  if (!authStore.isLogin) {
    return '/login'
  }
  if (to.path.startsWith('/admin') && !authStore.isAdmin) {
    return '/dashboard'
  }
  return true
})

export default router
