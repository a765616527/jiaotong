import { get } from './http'

export function statsOverview(days = 7) {
  return get<Record<string, any>>('/api/stats/overview', { days })
}
