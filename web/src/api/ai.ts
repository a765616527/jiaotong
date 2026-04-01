import { post } from './http'

export function extractAccidentFields(text: string) {
  return post<Record<string, unknown>>('/api/ai/extract-accident-fields', { text })
}

export function explainWarning(payload: Record<string, unknown>) {
  return post<Record<string, unknown>>('/api/ai/generate-warning-explanation', payload)
}

export function disposalSuggest(payload: Record<string, unknown>) {
  return post<Record<string, unknown>>('/api/ai/generate-disposal-suggestions', payload)
}

export function assistantQuery(question: string) {
  return post<Record<string, unknown>>('/api/ai/assistant-query', { question })
}
