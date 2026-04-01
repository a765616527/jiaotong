<script setup lang="ts">
import * as THREE from 'three'
import { ElMessage } from 'element-plus'
import { onMounted, ref } from 'vue'
import { hotspots } from '../api/warning'

const canvasWrap = ref<HTMLDivElement>()
const riskLegend = [
  { label: '高风险 (>=70)', color: '#dc2626' },
  { label: '中风险 (40~69)', color: '#f59e0b' },
  { label: '低风险 (<40)', color: '#16a34a' },
]

function createTextSprite(text: string) {
  const canvas = document.createElement('canvas')
  canvas.width = 256
  canvas.height = 64
  const ctx = canvas.getContext('2d')
  if (!ctx) return null
  ctx.fillStyle = 'rgba(255,255,255,0.92)'
  ctx.fillRect(0, 0, canvas.width, canvas.height)
  ctx.strokeStyle = '#111827'
  ctx.strokeRect(0, 0, canvas.width, canvas.height)
  ctx.fillStyle = '#111827'
  ctx.font = '20px sans-serif'
  ctx.fillText(text.slice(0, 12), 10, 38)

  const texture = new THREE.CanvasTexture(canvas)
  const material = new THREE.SpriteMaterial({ map: texture, depthTest: false })
  const sprite = new THREE.Sprite(material)
  sprite.scale.set(16, 4, 1)
  return sprite
}

async function initScene() {
  if (!canvasWrap.value) return

  const width = canvasWrap.value.clientWidth
  const height = 460
  const scene = new THREE.Scene()
  scene.background = new THREE.Color(0xf1f5f9)

  const camera = new THREE.PerspectiveCamera(65, width / height, 0.1, 1000)
  camera.position.set(0, 80, 120)
  camera.lookAt(0, 0, 0)

  const renderer = new THREE.WebGLRenderer({ antialias: true })
  renderer.setSize(width, height)
  canvasWrap.value.innerHTML = ''
  canvasWrap.value.appendChild(renderer.domElement)

  const light = new THREE.DirectionalLight(0xffffff, 1)
  light.position.set(40, 80, 40)
  scene.add(light)
  scene.add(new THREE.AmbientLight(0xffffff, 0.5))

  const plane = new THREE.Mesh(
    new THREE.PlaneGeometry(180, 120),
    new THREE.MeshPhongMaterial({ color: 0xe2e8f0 }),
  )
  plane.rotation.x = -Math.PI / 2
  scene.add(plane)

  try {
    const data = await hotspots()
    data.slice(0, 40).forEach((item, index) => {
      const score = Number(item.score || 0)
      const h = Math.max(2, Math.min(30, score / 4))
      const color = score >= 70 ? 0xdc2626 : score >= 40 ? 0xf59e0b : 0x16a34a
      const bar = new THREE.Mesh(
        new THREE.BoxGeometry(3, h, 3),
        new THREE.MeshPhongMaterial({ color }),
      )
      const x = (index % 10) * 16 - 72
      const z = Math.floor(index / 10) * 16 - 24
      bar.position.set(x, h / 2, z)
      scene.add(bar)

      const label = createTextSprite(String(item.roadName || `路段${index + 1}`))
      if (label) {
        label.position.set(x, h + 3, z)
        scene.add(label)
      }
    })
  } catch (e: any) {
    ElMessage.error(e.message || '热点数据加载失败')
  }

  const tick = () => {
    renderer.render(scene, camera)
    requestAnimationFrame(tick)
  }
  tick()
}

onMounted(initScene)
</script>

<template>
  <el-card class="page-card">
    <template #header>
      <div>三维风险态势图（必做）</div>
    </template>
    <div style="display: flex; gap: 14px; margin-bottom: 8px">
      <div v-for="item in riskLegend" :key="item.label" style="display: flex; align-items: center; gap: 6px">
        <span :style="{ width: '14px', height: '14px', display: 'inline-block', background: item.color }" />
        <span style="font-size: 13px">{{ item.label }}</span>
      </div>
    </div>
    <div ref="canvasWrap" style="width: 100%; height: 460px" />
  </el-card>
</template>
