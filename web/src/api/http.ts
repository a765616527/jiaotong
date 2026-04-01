import axios from 'axios'
import type { ApiResponse } from '../types'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || 'http://localhost:8080',
  timeout: 15000,
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error) => {
    const message = error?.response?.data?.message || error.message || '请求失败'
    return Promise.reject(new Error(message))
  },
)

export async function get<T>(url: string, params?: Record<string, unknown>): Promise<T> {
  const resp = await http.get<ApiResponse<T>>(url, { params })
  if (resp.data.code !== 0) {
    throw new Error(resp.data.message)
  }
  return resp.data.data
}

export async function post<T>(url: string, data?: Record<string, unknown>): Promise<T> {
  const resp = await http.post<ApiResponse<T>>(url, data)
  if (resp.data.code !== 0) {
    throw new Error(resp.data.message)
  }
  return resp.data.data
}

export async function postForm<T>(url: string, formData: FormData): Promise<T> {
  const resp = await http.post<ApiResponse<T>>(url, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  if (resp.data.code !== 0) {
    throw new Error(resp.data.message)
  }
  return resp.data.data
}
