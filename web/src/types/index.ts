export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
}

export interface UserInfo {
  id: number
  username: string
  displayName?: string
  roleCode: string
}
