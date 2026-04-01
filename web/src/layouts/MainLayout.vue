<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { formatRoleCode } from '../utils/labels'

const router = useRouter()
const authStore = useAuthStore()

function onLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<template>
  <el-container style="height: 100vh">
    <el-aside width="220px" style="background: #001529">
      <div style="height: 56px; color: #fff; display: flex; align-items: center; justify-content: center; font-weight: 700">
        交通事故预警系统
      </div>
      <el-menu
        background-color="#001529"
        text-color="#c9d1d9"
        active-text-color="#ffd04b"
        router
      >
        <el-menu-item index="/dashboard">首页驾驶舱</el-menu-item>
        <el-menu-item index="/accidents">事故管理</el-menu-item>
        <el-menu-item index="/map-analysis">地图分析（二维）</el-menu-item>
        <el-menu-item index="/warnings">预警中心</el-menu-item>
        <el-menu-item index="/reports">统计报表</el-menu-item>
        <el-menu-item index="/three-risk">三维风险态势</el-menu-item>
        <el-menu-item index="/ai-lab">智能能力</el-menu-item>
        <el-menu-item v-if="authStore.isAdmin" index="/admin-users">管理员配置</el-menu-item>
        <el-menu-item v-if="authStore.isAdmin" index="/admin-dicts">字典管理</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="background: #fff; display: flex; justify-content: space-between; align-items: center">
        <div>
          当前用户：{{ authStore.user?.displayName || authStore.user?.username }}
          （{{ formatRoleCode(authStore.user?.roleCode) }}）
        </div>
        <el-button type="danger" plain @click="onLogout">退出登录</el-button>
      </el-header>
      <el-main style="padding: 0">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>
