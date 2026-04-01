import { defineStore } from 'pinia'
import { me } from '../api/auth'
import type { UserInfo } from '../types'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: '' as string,
    user: null as UserInfo | null,
  }),
  getters: {
    isLogin: (state) => !!state.token,
    isAdmin: (state) => state.user?.roleCode === 'ROLE_ADMIN',
  },
  actions: {
    setLogin(token: string, user: UserInfo) {
      this.token = token
      this.user = user
      localStorage.setItem('token', token)
      localStorage.setItem('user', JSON.stringify(user))
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    },
    restore() {
      this.token = localStorage.getItem('token') || ''
      const raw = localStorage.getItem('user')
      this.user = raw ? (JSON.parse(raw) as UserInfo) : null
    },
    async refreshMe() {
      this.user = await me()
      localStorage.setItem('user', JSON.stringify(this.user))
    },
  },
})
