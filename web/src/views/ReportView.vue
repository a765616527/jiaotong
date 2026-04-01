<script setup lang="ts">
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { nextTick, onMounted, ref } from 'vue'
import { statsOverview } from '../api/stats'
import { formatWarningStatus } from '../utils/labels'

const trendRef = ref<HTMLDivElement>()
const pieRef = ref<HTMLDivElement>()
const topRef = ref<HTMLDivElement>()
const statusRef = ref<HTMLDivElement>()
const warningTotal = ref(0)
const accidentTotal = ref(0)
let trendChart: echarts.ECharts | null = null
let typeChart: echarts.ECharts | null = null
let topChart: echarts.ECharts | null = null
let statusChart: echarts.ECharts | null = null

async function loadStats() {
  try {
    const data = await statsOverview()
    warningTotal.value = Number(data.warningTotal || 0)
    accidentTotal.value = Number(data.accidentTotal || 0)
    renderTrend(data.trend || {})
    renderTypePie(data.typeDistribution || {})
    renderTopRoad(data.topRoads || [])
    renderStatusPie(data.warningStatus || {})
  } catch (e: any) {
    ElMessage.error(e.message || '统计报表加载失败')
  }
}

function renderTrend(trend: Record<string, number>) {
  if (!trendRef.value) return
  trendChart?.dispose()
  trendChart = echarts.init(trendRef.value)
  trendChart.setOption({
    title: { text: '近7天事故趋势' },
    xAxis: { type: 'category', data: Object.keys(trend) },
    yAxis: { type: 'value' },
    series: [{ type: 'line', smooth: true, data: Object.values(trend) }],
  })
}

function renderTypePie(typeDistribution: Record<string, number>) {
  if (!pieRef.value) return
  const data = Object.entries(typeDistribution).map(([name, value]) => ({ name, value }))
  typeChart?.dispose()
  typeChart = echarts.init(pieRef.value)
  typeChart.setOption({
    title: { text: '事故类型占比' },
    tooltip: { trigger: 'item' },
    series: [{ type: 'pie', radius: '65%', data }],
  })
}

function renderTopRoad(topRoads: Array<{ roadName: string; count: number }>) {
  if (!topRef.value) return
  topChart?.dispose()
  topChart = echarts.init(topRef.value)
  topChart.setOption({
    title: { text: '高发路段前10' },
    xAxis: { type: 'category', data: topRoads.map((x) => x.roadName) },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: topRoads.map((x) => x.count), itemStyle: { color: '#f59e0b' } }],
  })
}

function renderStatusPie(statusMap: Record<string, number>) {
  if (!statusRef.value) return
  const data = Object.entries(statusMap).map(([name, value]) => ({
    name: formatWarningStatus(name),
    value,
  }))
  statusChart?.dispose()
  statusChart = echarts.init(statusRef.value)
  statusChart.setOption({
    title: { text: '预警状态占比' },
    tooltip: { trigger: 'item' },
    series: [{ type: 'pie', radius: '65%', data }],
  })
}

function exportChart(chart: echarts.ECharts | null, filename: string) {
  if (!chart) return
  const url = chart.getDataURL({ type: 'png', pixelRatio: 2, backgroundColor: '#fff' })
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
}

onMounted(async () => {
  await nextTick()
  await loadStats()
})
</script>

<template>
  <el-row :gutter="12" style="margin: 12px">
    <el-col :span="12">
      <el-card>
        <div>近7天事故总量</div>
        <div style="font-size: 30px; font-weight: 700">{{ accidentTotal }}</div>
      </el-card>
    </el-col>
    <el-col :span="12">
      <el-card>
        <div>预警总量</div>
        <div style="font-size: 30px; font-weight: 700">{{ warningTotal }}</div>
      </el-card>
    </el-col>
  </el-row>

  <el-card class="page-card">
    <template #header>
      <div style="display: flex; justify-content: space-between; align-items: center">
        <span>趋势分析</span>
        <el-button size="small" @click="exportChart(trendChart, '近7天事故趋势.png')">导出趋势图</el-button>
      </div>
    </template>
    <div ref="trendRef" style="height: 300px" />
  </el-card>
  <el-row :gutter="12" style="margin: 0 12px 12px">
    <el-col :span="12">
      <el-card>
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center">
            <span>类型占比</span>
            <el-button size="small" @click="exportChart(typeChart, '事故类型占比.png')">导出</el-button>
          </div>
        </template>
        <div ref="pieRef" style="height: 320px" />
      </el-card>
    </el-col>
    <el-col :span="12">
      <el-card>
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center">
            <span>高发路段前10</span>
            <el-button size="small" @click="exportChart(topChart, '高发路段前10.png')">导出</el-button>
          </div>
        </template>
        <div ref="topRef" style="height: 320px" />
      </el-card>
    </el-col>
  </el-row>
  <el-row :gutter="12" style="margin: 0 12px 12px">
    <el-col :span="12">
      <el-card>
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center">
            <span>预警状态分布</span>
            <el-button size="small" @click="exportChart(statusChart, '预警状态占比.png')">导出</el-button>
          </div>
        </template>
        <div ref="statusRef" style="height: 320px" />
      </el-card>
    </el-col>
  </el-row>
</template>
