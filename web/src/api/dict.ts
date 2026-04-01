import { get } from './http'

export interface DictItem {
  id: number
  dictType: string
  dictCode: string
  dictName: string
  sortNo: number
  enabled: number
  remark?: string
}

export function listDictItems(dictType: string, enabled = 1) {
  return get<DictItem[]>('/api/dicts/items', { dictType, enabled })
}
