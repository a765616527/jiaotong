<script setup lang="ts">
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { nextTick, onMounted, ref } from 'vue'
import { pageAccidents } from '../api/accident'

const chartRef = ref<HTMLDivElement>()
const areaRef = ref<HTMLDivElement>()
const count = ref(0)
const mode = ref<'scatter' | 'heat'>('scatter')
const allRecords = ref<Record<string, any>[]>([])
let chart: echarts.ECharts | null = null
let areaChart: echarts.ECharts | null = null

function roundBy(value: number, step: number) {
  return Math.round(value / step) * step
}

function renderScatter(points: Array<[number, number, string]>) {
  if (!chart) return
  chart.setOption({
    title: { text: '事故点位二维分布图（经纬度）' },
    tooltip: {
      trigger: 'item',
      formatter: (p: any) => `道路: ${p.value[2]}<br/>经度: ${p.value[0]}<br/>纬度: ${p.value[1]}`,
    },
    xAxis: { type: 'value', name: '经度', min: 'dataMin', max: 'dataMax' },
    yAxis: { type: 'value', name: '纬度', min: 'dataMin', max: 'dataMax' },
    visualMap: undefined,
    series: [
      {
        type: 'scatter',
        symbolSize: 12,
        itemStyle: { color: '#ef4444' },
        data: points,
      },
    ],
  })
}

function renderHeat(points: Array<[number, number, string]>) {
  if (!chart || points.length === 0) return

  const step = 0.002
  const bucket = new Map<string, number>()
  points.forEach(([lng, lat]) => {
    const x = roundBy(lng, step).toFixed(3)
    const y = roundBy(lat, step).toFixed(3)
    const key = `${x}_${y}`
    bucket.set(key, (bucket.get(key) || 0) + 1)
  })

  const xs = Array.from(new Set(Array.from(bucket.keys()).map((k) => k.split('_')[0]))).sort()
  const ys = Array.from(new Set(Array.from(bucket.keys()).map((k) => k.split('_')[1]))).sort()
  const xIndex = new Map(xs.map((x, i) => [x, i]))
  const yIndex = new Map(ys.map((y, i) => [y, i]))
  const heatData: Array<[number, number, number]> = []
  let maxV = 0
  bucket.forEach((v, key) => {
    const [x, y] = key.split('_')
    maxV = Math.max(maxV, v)
    heatData.push([xIndex.get(x) || 0, yIndex.get(y) || 0, v])
  })

  chart.setOption({
    title: { text: '事故热力分布图（网格聚合）' },
    tooltip: {
      formatter: (p: any) => `经度: ${xs[p.value[0]]}<br/>纬度: ${ys[p.value[1]]}<br/>事故数: ${p.value[2]}`,
    },
    xAxis: { type: 'category', data: xs, name: '经度' },
    yAxis: { type: 'category', data: ys, name: '纬度' },
    visualMap: {
      min: 0,
      max: Math.max(1, maxV),
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: 8,
      inRange: { color: ['#bae6fd', '#38bdf8', '#0284c7', '#b91c1c'] },
    },
    series: [{ type: 'heatmap', data: heatData }],
  })
}

function renderMap() {
  if (!chartRef.value) return
  const points = allRecords.value.map((x: any) => [Number(x.longitude), Number(x.latitude), String(x.roadName)] as [number, number, string])
  chart?.dispose()
  chart = echarts.init(chartRef.value)
  if (mode.value === 'scatter') {
    renderScatter(points)
  } else {
    renderHeat(points)
  }
}

function renderAreaStats() {
  if (!areaRef.value) return
  const areaCounter = new Map<string, number>()
  allRecords.value.forEach((x) => {
    const area = String(x.areaCode || '未知区域')
    areaCounter.set(area, (areaCounter.get(area) || 0) + 1)
  })
  const rows = Array.from(areaCounter.entries())
    .sort((a, b) => b[1] - a[1])
    .slice(0, 10)
  areaChart?.dispose()
  areaChart = echarts.init(areaRef.value)
  areaChart.setOption({
    title: { text: '区域事故数量前10' },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: rows.map((x) => x[0]) },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: rows.map((x) => x[1]), itemStyle: { color: '#2563eb' } }],
  })
}

async function loadData() {
  try {
    const data = await pageAccidents({ pageNum: 1, pageSize: 2000 })
    allRecords.value = data.records || []
    count.value = allRecords.value.length
    renderMap()
    renderAreaStats()
  } catch (e: any) {
    ElMessage.error(e.message || '地图分析加载失败')
  }
}

function onModeChange() {
  renderMap()
}

onMounted(async () => {
  await nextTick()
  await loadData()
})
</script>

<template>
  <el-card class="page-card">
    <template #header>
      <div style="display: flex; justify-content: space-between; align-items: center">
        <span>事故空间可视化（二维）- 点位总数：{{ count }}</span>
        <el-radio-group v-model="mode" @change="onModeChange">
          <el-radio-button label="scatter">点位模式</el-radio-button>
          <el-radio-button label="heat">热力模式</el-radio-button>
        </el-radio-group>
      </div>
    </template>
    <div ref="chartRef" style="height: 520px" />
  </el-card>

  <el-card class="page-card">
    <div ref="areaRef" style="height: 320px" />
  </el-card>
</template>
