import { get, post } from './http'
import type { PageResult } from '../types'

export function pageUsers(params: Record<string, unknown>) {
  return get<PageResult<Record<string, unknown>>>('/api/admin/users', params)
}

export function createUser(payload: Record<string, unknown>) {
  return post<void>('/api/admin/users', payload)
}

export function getRules() {
  return get<Record<string, unknown>[]>('/api/admin/rules')
}

export function updateRule(payload: Record<string, unknown>) {
  return post<void>('/api/admin/rules/update', payload)
}

export function pageLogs(params: Record<string, unknown>) {
  return get<PageResult<Record<string, unknown>>>('/api/admin/logs', params)
}

export function pageAiLogs(params: Record<string, unknown>) {
  return get<PageResult<Record<string, unknown>>>('/api/admin/ai-logs', params)
}

export function pageRuleChangeLogs(params: Record<string, unknown>) {
  return get<PageResult<Record<string, unknown>>>('/api/admin/rule-change-logs', params)
}

export function getAdminSummary() {
  return get<Record<string, unknown>>('/api/admin/summary')
}

export function getAdminSummaryTrend(days = 7) {
  return get<Record<string, unknown>>('/api/admin/summary/trend', { days })
}

export function pageAdminDicts(params: Record<string, unknown>) {
  return get<PageResult<Record<string, unknown>>>('/api/admin/dicts', params)
}

export function createAdminDict(payload: Record<string, unknown>) {
  return post<void>('/api/admin/dicts', payload)
}

export function updateAdminDict(id: number, payload: Record<string, unknown>) {
  return post<void>(`/api/admin/dicts/${id}/update`, payload)
}

export function deleteAdminDict(id: number) {
  return post<void>(`/api/admin/dicts/${id}/delete`)
}
