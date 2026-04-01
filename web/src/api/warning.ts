import { get, post } from './http'
import type { PageResult } from '../types'

export interface WarningItem {
  id: number
  warningCode: string
  riskLevel: string
  targetRoad: string
  triggerReason: string
  status: string
  createdAt: string
}

export function evaluateRisk() {
  return post<Record<string, unknown>[]>('/api/risk/evaluate', {})
}

export function hotspots() {
  return get<Record<string, unknown>[]>('/api/risk/hotspots')
}

export function pageWarnings(params: Record<string, unknown>) {
  return get<PageResult<WarningItem>>('/api/warnings', params)
}

export function confirmWarning(id: number) {
  return post<void>(`/api/warnings/${id}/confirm`, {})
}

export function publishWarning(id: number) {
  return post<void>(`/api/warnings/${id}/publish`, {})
}

export function resolveWarning(id: number) {
  return post<void>(`/api/warnings/${id}/resolve`, {})
}

export function batchConfirm(ids: number[]) {
  return post<Record<string, any>>('/api/warnings/batch/confirm', { ids })
}

export function batchPublish(ids: number[]) {
  return post<Record<string, any>>('/api/warnings/batch/publish', { ids })
}

export function batchResolve(ids: number[]) {
  return post<Record<string, any>>('/api/warnings/batch/resolve', { ids })
}

export function addDisposal(id: number, actionDesc: string) {
  return post<void>(`/api/warnings/${id}/disposal-records`, { actionDesc })
}

export function aiExplain(id: number) {
  return get<Record<string, unknown>>(`/api/warnings/${id}/ai-explanation`)
}
