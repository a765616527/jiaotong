<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { login } from '../api/auth'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const form = reactive({
  username: 'admin',
  password: 'Admin@123456',
})

async function onSubmit() {
  loading.value = true
  try {
    const data = await login(form.username, form.password)
    authStore.setLogin(data.token, data.user)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (e: any) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div style="height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #dbeafe 0%, #eff6ff 100%)">
    <el-card style="width: 420px">
      <template #header>
        <div style="font-size: 18px; font-weight: 700">城市交通事故预警系统</div>
      </template>
      <el-form :model="form" label-width="72px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" style="width: 100%" @click="onSubmit">
            登录
          </el-button>
        </el-form-item>
      </el-form>
      <div style="color: #909399; font-size: 12px">
        默认管理员账号：`admin / Admin@123456`
      </div>
    </el-card>
  </div>
</template>
