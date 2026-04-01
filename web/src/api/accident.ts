import { get, post, postForm } from './http'
import type { PageResult } from '../types'

export interface AccidentRecord {
  id: number
  occurTime: string
  roadName: string
  areaCode: string
  longitude: number
  latitude: number
  accidentType: string
  vehicleCount: number
  casualtyCount: number
  description: string
}

export function pageAccidents(params: Record<string, unknown>) {
  return get<PageResult<AccidentRecord>>('/api/accidents', params)
}

export function createAccident(payload: Record<string, unknown>) {
  return post<void>('/api/accidents', payload)
}

export function updateAccident(id: number, payload: Record<string, unknown>) {
  return post<void>(`/api/accidents/${id}/update`, payload)
}

export function deleteAccident(id: number) {
  return post<void>(`/api/accidents/${id}/delete`)
}

export function importAccidentCsv(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return postForm<Record<string, any>>('/api/accidents/import-csv', formData)
}
