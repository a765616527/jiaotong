import { get, post } from './http'
import type { UserInfo } from '../types'

export interface LoginResult {
  token: string
  user: UserInfo
}

export function login(username: string, password: string) {
  return post<LoginResult>('/api/auth/login', { username, password })
}

export function me() {
  return get<UserInfo>('/api/auth/me')
}
