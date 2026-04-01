<script setup lang="ts">
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { nextTick, onMounted, ref } from 'vue'
import { statsOverview } from '../api/stats'
import { hotspots } from '../api/warning'
import { formatRiskLevel } from '../utils/labels'

const chartRef = ref<HTMLDivElement>()
const hotspotRows = ref<Record<string, unknown>[]>([])
const warningTotal = ref(0)
const highCount = ref(0)
const accidentTotal = ref(0)
const periodDays = ref(7)
let chart: echarts.ECharts | null = null

async function loadData() {
  try {
    const [hotspotData, statsData] = await Promise.all([
      hotspots(),
      statsOverview(periodDays.value),
    ])
    hotspotRows.value = hotspotData
    warningTotal.value = Number(statsData.warningTotal || 0)
    accidentTotal.value = Number(statsData.accidentTotal || 0)
    highCount.value = hotspotRows.value.filter((x) => x.riskLevel === 'HIGH').length
    renderChart()
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
  }
}

function renderChart() {
  if (!chartRef.value) return
  chart?.dispose()
  chart = echarts.init(chartRef.value)
  chart.setOption({
    title: { text: '高风险路段前10' },
    tooltip: {},
    xAxis: {
      type: 'category',
      data: hotspotRows.value.slice(0, 10).map((x) => String(x.roadName)),
    },
    yAxis: { type: 'value' },
    series: [
      {
        type: 'bar',
        data: hotspotRows.value.slice(0, 10).map((x) => Number(x.score || 0)),
        itemStyle: { color: '#ef4444' },
      },
    ],
  })
}

function onPeriodChange() {
  loadData()
}

onMounted(async () => {
  await nextTick()
  await loadData()
})
</script>

<template>
  <el-card class="page-card">
    <div class="toolbar">
      <span>统计口径：</span>
      <el-radio-group v-model="periodDays" @change="onPeriodChange">
        <el-radio-button :label="7">本周(7天)</el-radio-button>
        <el-radio-button :label="30">本月(30天)</el-radio-button>
      </el-radio-group>
    </div>
  </el-card>

  <el-row :gutter="12" style="margin: 12px">
    <el-col :span="8">
      <el-card>
        <div>风险路段总数（实时）</div>
        <div style="font-size: 28px; font-weight: 700">{{ hotspotRows.length }}</div>
      </el-card>
    </el-col>
    <el-col :span="8">
      <el-card>
        <div>高风险路段数</div>
        <div style="font-size: 28px; font-weight: 700; color: #dc2626">{{ highCount }}</div>
      </el-card>
    </el-col>
    <el-col :span="8">
      <el-card>
        <div>{{ periodDays === 7 ? '近7天事故量' : '近30天事故量' }}</div>
        <div style="font-size: 28px; font-weight: 700">{{ accidentTotal }}</div>
      </el-card>
    </el-col>
  </el-row>

  <el-row :gutter="12" style="margin: 0 12px 12px">
    <el-col :span="8">
      <el-card>
        <div>预警总量（累计）</div>
        <div style="font-size: 28px; font-weight: 700">{{ warningTotal }}</div>
      </el-card>
    </el-col>
  </el-row>

  <el-card class="page-card">
    <div ref="chartRef" style="height: 360px" />
  </el-card>

  <el-card class="page-card">
    <template #header>风险热点明细</template>
    <el-table :data="hotspotRows" stripe>
      <el-table-column prop="roadName" label="道路" />
      <el-table-column prop="score" label="评分" width="120" />
      <el-table-column label="风险等级" width="120">
        <template #default="{ row }">
          {{ formatRiskLevel(String(row.riskLevel || '')) }}
        </template>
      </el-table-column>
      <el-table-column prop="triggerReason" label="触发原因" />
    </el-table>
  </el-card>
</template>
