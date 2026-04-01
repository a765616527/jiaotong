const riskLevelMap: Record<string, string> = {
  HIGH: '高风险',
  MEDIUM: '中风险',
  LOW: '低风险',
}

const warningStatusMap: Record<string, string> = {
  DRAFT: '草稿',
  CONFIRMED: '已确认',
  PUBLISHED: '已发布',
  PROCESSING: '处置中',
  RESOLVED: '已解除',
}

const roleCodeMap: Record<string, string> = {
  ROLE_ADMIN: '管理员',
  ROLE_TRAFFIC_OFFICER: '交通管理人员',
}

const dictTypeMap: Record<string, string> = {
  ACCIDENT_TYPE: '事故类型',
  ROAD_TYPE: '道路类型',
  WARNING_LEVEL: '预警等级',
}

export function formatRiskLevel(level?: string | null) {
  if (!level) return ''
  return riskLevelMap[level] || level
}

export function formatWarningStatus(status?: string | null) {
  if (!status) return ''
  return warningStatusMap[status] || status
}

export function formatRoleCode(role?: string | null) {
  if (!role) return ''
  return roleCodeMap[role] || role
}

export function formatDictType(dictType?: string | null) {
  if (!dictType) return ''
  return dictTypeMap[dictType] || dictType
}

