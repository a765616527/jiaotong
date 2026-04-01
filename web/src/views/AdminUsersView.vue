<script setup lang="ts">
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { nextTick, onMounted, reactive, ref } from 'vue'
import {
  createUser,
  getAdminSummary,
  getAdminSummaryTrend,
  getRules,
  pageAiLogs,
  pageLogs,
  pageRuleChangeLogs,
  pageUsers,
  updateRule,
} from '../api/admin'
import { formatRoleCode } from '../utils/labels'

const users = ref<Record<string, unknown>[]>([])
const logs = ref<Record<string, unknown>[]>([])
const aiLogs = ref<Record<string, unknown>[]>([])
const ruleChangeLogs = ref<Record<string, unknown>[]>([])
const rules = ref<Record<string, unknown>[]>([])
const userTotal = ref(0)
const ruleChangeTotal = ref(0)
const trendDays = ref(7)
const trendRef = ref<HTMLDivElement>()
let trendChart: echarts.ECharts | null = null
const summary = reactive({
  userTotal: 0,
  enabledUserCount: 0,
  accidentTotal: 0,
  warningTotal: 0,
  activeWarningCount: 0,
  resolvedWarningCount: 0,
  dictTotal: 0,
  ruleTotal: 0,
  aiCallTotal: 0,
  todayAiCallCount: 0,
  todayLoginCount: 0,
  todayLoginFailCount: 0,
})
const trendCompare = reactive({
  accidentCurrent: 0,
  accidentPrevious: 0,
  accidentRate: null as number | null,
  warningCurrent: 0,
  warningPrevious: 0,
  warningRate: null as number | null,
  loginCurrent: 0,
  loginPrevious: 0,
  loginRate: null as number | null,
  loginFailCurrent: 0,
  loginFailPrevious: 0,
  loginFailRate: null as number | null,
})
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  username: '',
})
const ruleChangeQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  ruleKey: '',
  timeRange: [] as string[],
})
const newUser = reactive({
  username: '',
  password: '',
  displayName: '',
  roleCode: 'ROLE_TRAFFIC_OFFICER',
})

async function loadUsers() {
  const data = await pageUsers(query)
  users.value = data.records || []
  userTotal.value = data.total || 0
}

async function loadRules() {
  rules.value = await getRules()
}

async function loadLogs() {
  const data = await pageLogs({ pageNum: 1, pageSize: 10 })
  logs.value = data.records || []
}

async function loadAiLogs() {
  const data = await pageAiLogs({ pageNum: 1, pageSize: 10 })
  aiLogs.value = data.records || []
}

async function loadRuleChangeLogs() {
  const params: Record<string, unknown> = {
    pageNum: ruleChangeQuery.pageNum,
    pageSize: ruleChangeQuery.pageSize,
  }
  if (ruleChangeQuery.ruleKey.trim()) {
    params.ruleKey = ruleChangeQuery.ruleKey.trim()
  }
  if (ruleChangeQuery.timeRange.length === 2) {
    params.startTime = ruleChangeQuery.timeRange[0]
    params.endTime = ruleChangeQuery.timeRange[1]
  }
  const data = await pageRuleChangeLogs(params)
  ruleChangeLogs.value = data.records || []
  ruleChangeTotal.value = data.total || 0
}

async function loadSummary() {
  const data = await getAdminSummary()
  summary.userTotal = Number(data.userTotal || 0)
  summary.enabledUserCount = Number(data.enabledUserCount || 0)
  summary.accidentTotal = Number(data.accidentTotal || 0)
  summary.warningTotal = Number(data.warningTotal || 0)
  summary.activeWarningCount = Number(data.activeWarningCount || 0)
  summary.resolvedWarningCount = Number(data.resolvedWarningCount || 0)
  summary.dictTotal = Number(data.dictTotal || 0)
  summary.ruleTotal = Number(data.ruleTotal || 0)
  summary.aiCallTotal = Number(data.aiCallTotal || 0)
  summary.todayAiCallCount = Number(data.todayAiCallCount || 0)
  summary.todayLoginCount = Number(data.todayLoginCount || 0)
  summary.todayLoginFailCount = Number(data.todayLoginFailCount || 0)
}

async function loadSummaryTrend() {
  const data = await getAdminSummaryTrend(trendDays.value)
  const compare = (data.compare as Record<string, unknown>) || {}
  trendCompare.accidentCurrent = Number(compare.accidentCurrent || 0)
  trendCompare.accidentPrevious = Number(compare.accidentPrevious || 0)
  trendCompare.accidentRate = compare.accidentRate == null ? null : Number(compare.accidentRate)
  trendCompare.warningCurrent = Number(compare.warningCurrent || 0)
  trendCompare.warningPrevious = Number(compare.warningPrevious || 0)
  trendCompare.warningRate = compare.warningRate == null ? null : Number(compare.warningRate)
  trendCompare.loginCurrent = Number(compare.loginCurrent || 0)
  trendCompare.loginPrevious = Number(compare.loginPrevious || 0)
  trendCompare.loginRate = compare.loginRate == null ? null : Number(compare.loginRate)
  trendCompare.loginFailCurrent = Number(compare.loginFailCurrent || 0)
  trendCompare.loginFailPrevious = Number(compare.loginFailPrevious || 0)
  trendCompare.loginFailRate = compare.loginFailRate == null ? null : Number(compare.loginFailRate)
  renderTrend(
    (data.dates as string[]) || [],
    (data.accidentCounts as number[]) || [],
    (data.warningCreatedCounts as number[]) || [],
    (data.loginCounts as number[]) || [],
    (data.loginFailCounts as number[]) || [],
  )
}

function renderTrend(
  dates: string[],
  accidents: number[],
  warnings: number[],
  logins: number[],
  loginFails: number[],
) {
  if (!trendRef.value) return
  trendChart?.dispose()
  trendChart = echarts.init(trendRef.value)
  trendChart.setOption({
    title: { text: `管理端趋势分析（近${trendDays.value}天）` },
    tooltip: { trigger: 'axis' },
    legend: { data: ['事故', '新预警', '登录成功', '登录失败'] },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value' },
    series: [
      { name: '事故', type: 'line', smooth: true, data: accidents },
      { name: '新预警', type: 'line', smooth: true, data: warnings },
      { name: '登录成功', type: 'line', smooth: true, data: logins },
      { name: '登录失败', type: 'line', smooth: true, data: loginFails },
    ],
  })
}

function formatRate(value: number | null) {
  if (value == null) return '与上周期对比：--'
  const abs = Math.abs(value).toFixed(2)
  const up = value >= 0
  const label = up ? `+${abs}%` : `-${abs}%`
  return `与上周期对比：${up ? '上升' : '下降'} ${label}`
}

function exportTrendChart() {
  if (!trendChart) return
  const url = trendChart.getDataURL({ type: 'png', pixelRatio: 2, backgroundColor: '#fff' })
  const a = document.createElement('a')
  a.href = url
  a.download = `管理端趋势分析_${trendDays.value}天.png`
  a.click()
}

async function onTrendDaysChange() {
  try {
    await loadSummaryTrend()
  } catch (e: any) {
    ElMessage.error(e.message || '趋势加载失败')
  }
}

async function onSearchRuleChangeLogs() {
  ruleChangeQuery.pageNum = 1
  try {
    await loadRuleChangeLogs()
  } catch (e: any) {
    ElMessage.error(e.message || '筛选失败')
  }
}

async function onResetRuleChangeLogs() {
  ruleChangeQuery.ruleKey = ''
  ruleChangeQuery.timeRange = []
  ruleChangeQuery.pageNum = 1
  try {
    await loadRuleChangeLogs()
  } catch (e: any) {
    ElMessage.error(e.message || '重置失败')
  }
}

async function onCreateUser() {
  try {
    await createUser(newUser)
    ElMessage.success('创建成功')
    newUser.username = ''
    newUser.password = ''
    newUser.displayName = ''
    newUser.roleCode = 'ROLE_TRAFFIC_OFFICER'
    await Promise.all([loadUsers(), loadSummary()])
  } catch (e: any) {
    ElMessage.error(e.message || '创建失败')
  }
}

async function onUpdateRule(row: Record<string, any>) {
  try {
    await updateRule({
      ruleKey: row.ruleKey,
      ruleValue: row.ruleValue,
      description: row.description,
    })
    ElMessage.success('规则已更新')
    await Promise.all([loadRuleChangeLogs(), loadSummary()])
  } catch (e: any) {
    ElMessage.error(e.message || '更新失败')
  }
}

onMounted(async () => {
  try {
    await nextTick()
    await Promise.all([
      loadSummary(),
      loadSummaryTrend(),
      loadUsers(),
      loadRules(),
      loadLogs(),
      loadAiLogs(),
      loadRuleChangeLogs(),
    ])
  } catch (e: any) {
    ElMessage.error(e.message || '初始化失败')
  }
})
</script>

<template>
  <el-row :gutter="12" style="margin: 12px">
    <el-col :span="4"><el-card><div>用户总数</div><div style="font-size: 26px; font-weight: 700">{{ summary.userTotal }}</div></el-card></el-col>
    <el-col :span="4"><el-card><div>启用用户</div><div style="font-size: 26px; font-weight: 700">{{ summary.enabledUserCount }}</div></el-card></el-col>
    <el-col :span="4"><el-card><div>事故总量</div><div style="font-size: 26px; font-weight: 700">{{ summary.accidentTotal }}</div></el-card></el-col>
    <el-col :span="4"><el-card><div>活跃预警</div><div style="font-size: 26px; font-weight: 700; color: #d97706">{{ summary.activeWarningCount }}</div></el-card></el-col>
    <el-col :span="4"><el-card><div>智能调用(今日)</div><div style="font-size: 26px; font-weight: 700">{{ summary.todayAiCallCount }}</div></el-card></el-col>
    <el-col :span="4"><el-card><div>登录失败(今日)</div><div style="font-size: 26px; font-weight: 700; color: #dc2626">{{ summary.todayLoginFailCount }}</div></el-card></el-col>
  </el-row>

  <el-row :gutter="12" style="margin: 12px">
    <el-col :span="24">
      <el-card>
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center">
            <span>全局趋势</span>
            <div style="display: flex; align-items: center; gap: 8px">
              <el-radio-group v-model="trendDays" @change="onTrendDaysChange">
                <el-radio-button :label="7">近7天</el-radio-button>
                <el-radio-button :label="30">近30天</el-radio-button>
              </el-radio-group>
              <el-button size="small" @click="exportTrendChart">导出趋势图</el-button>
            </div>
          </div>
        </template>
        <div ref="trendRef" style="height: 320px" />
        <el-row :gutter="12" style="margin-top: 8px">
          <el-col :span="6">
            <el-card shadow="never">
              <div>事故（当前/上周期）</div>
              <div style="font-size: 20px; font-weight: 700">{{ trendCompare.accidentCurrent }} / {{ trendCompare.accidentPrevious }}</div>
              <div style="font-size: 12px; color: #64748b">{{ formatRate(trendCompare.accidentRate) }}</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="never">
              <div>新预警（当前/上周期）</div>
              <div style="font-size: 20px; font-weight: 700">{{ trendCompare.warningCurrent }} / {{ trendCompare.warningPrevious }}</div>
              <div style="font-size: 12px; color: #64748b">{{ formatRate(trendCompare.warningRate) }}</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="never">
              <div>登录成功（当前/上周期）</div>
              <div style="font-size: 20px; font-weight: 700">{{ trendCompare.loginCurrent }} / {{ trendCompare.loginPrevious }}</div>
              <div style="font-size: 12px; color: #64748b">{{ formatRate(trendCompare.loginRate) }}</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="never">
              <div>登录失败（当前/上周期）</div>
              <div style="font-size: 20px; font-weight: 700; color: #dc2626">{{ trendCompare.loginFailCurrent }} / {{ trendCompare.loginFailPrevious }}</div>
              <div style="font-size: 12px; color: #64748b">{{ formatRate(trendCompare.loginFailRate) }}</div>
            </el-card>
          </el-col>
        </el-row>
      </el-card>
    </el-col>
  </el-row>

  <el-row :gutter="12" style="margin: 12px">
    <el-col :span="12">
      <el-card>
        <template #header>用户管理</template>
        <div class="toolbar">
          <el-input v-model="query.username" placeholder="用户名搜索" style="width: 180px" />
          <el-button type="primary" @click="loadUsers">查询</el-button>
        </div>
        <el-table :data="users" stripe>
          <el-table-column prop="username" label="用户名" />
          <el-table-column prop="displayName" label="姓名" />
          <el-table-column label="角色">
            <template #default="{ row }">
              {{ formatRoleCode(String(row.roleCode || '')) }}
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          style="margin-top: 12px"
          layout="total, prev, pager, next"
          :total="userTotal"
          :page-size="query.pageSize"
          :current-page="query.pageNum"
          @current-change="(p:number) => { query.pageNum = p; loadUsers() }"
        />

        <el-divider />
        <el-form label-width="80px">
          <el-form-item label="用户名"><el-input v-model="newUser.username" /></el-form-item>
          <el-form-item label="密码"><el-input v-model="newUser.password" /></el-form-item>
          <el-form-item label="姓名"><el-input v-model="newUser.displayName" /></el-form-item>
          <el-form-item label="角色">
            <el-select v-model="newUser.roleCode" style="width: 100%">
              <el-option label="交通管理人员" value="ROLE_TRAFFIC_OFFICER" />
              <el-option label="管理员" value="ROLE_ADMIN" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="success" @click="onCreateUser">新建用户</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </el-col>

    <el-col :span="12">
      <el-card>
        <template #header>规则配置</template>
        <el-table :data="rules" stripe>
          <el-table-column prop="ruleKey" label="规则键" width="160" />
          <el-table-column label="规则值">
            <template #default="{ row }">
              <el-input v-model="row.ruleValue" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button size="small" type="primary" @click="onUpdateRule(row)">保存</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card style="margin-top: 12px">
        <template #header>操作日志</template>
        <el-table :data="logs" stripe>
          <el-table-column prop="module" label="模块" width="90" />
          <el-table-column prop="action" label="动作" width="120" />
          <el-table-column prop="detail" label="详情" />
          <el-table-column prop="operatedAt" label="时间" width="180" />
        </el-table>
      </el-card>

      <el-card style="margin-top: 12px">
        <template #header>智能调用日志</template>
        <el-table :data="aiLogs" stripe>
          <el-table-column prop="endpoint" label="接口" width="220" />
          <el-table-column prop="success" label="成功" width="80" />
          <el-table-column prop="inputLength" label="输入长度" width="100" />
          <el-table-column prop="outputLength" label="输出长度" width="100" />
          <el-table-column prop="errorMessage" label="错误信息" />
          <el-table-column prop="calledAt" label="调用时间" width="180" />
        </el-table>
      </el-card>

      <el-card style="margin-top: 12px">
        <template #header>规则修改历史</template>
        <div class="toolbar">
          <el-input v-model="ruleChangeQuery.ruleKey" placeholder="规则键" style="width: 150px" />
          <el-date-picker
            v-model="ruleChangeQuery.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
          <el-button type="primary" @click="onSearchRuleChangeLogs">筛选</el-button>
          <el-button @click="onResetRuleChangeLogs">重置</el-button>
        </div>
        <el-table :data="ruleChangeLogs" stripe>
          <el-table-column prop="ruleKey" label="规则键" width="150" />
          <el-table-column prop="oldValue" label="旧值" width="100" />
          <el-table-column prop="newValue" label="新值" width="100" />
          <el-table-column prop="note" label="备注" />
          <el-table-column prop="changedAt" label="修改时间" width="180" />
        </el-table>
        <el-pagination
          style="margin-top: 12px"
          layout="total, prev, pager, next"
          :total="ruleChangeTotal"
          :page-size="ruleChangeQuery.pageSize"
          :current-page="ruleChangeQuery.pageNum"
          @current-change="(p:number) => { ruleChangeQuery.pageNum = p; loadRuleChangeLogs() }"
        />
      </el-card>
    </el-col>
  </el-row>
</template>
