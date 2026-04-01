<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { onMounted, reactive, ref } from 'vue'
import {
  addDisposal,
  aiExplain,
  batchConfirm,
  batchPublish,
  batchResolve,
  confirmWarning,
  evaluateRisk,
  pageWarnings,
  publishWarning,
  resolveWarning,
} from '../api/warning'
import { formatRiskLevel, formatWarningStatus } from '../utils/labels'

const rows = ref<Record<string, any>[]>([])
const total = ref(0)
const loading = ref(false)
const selectedRows = ref<Array<Record<string, any>>>([])
const selectedIds = ref<number[]>([])
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  status: '',
})

async function loadData() {
  loading.value = true
  try {
    const data = await pageWarnings(query)
    rows.value = data.records || []
    total.value = data.total || 0
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function onEvaluate() {
  try {
    await evaluateRisk()
    ElMessage.success('风险评估完成')
    await loadData()
  } catch (e: any) {
    ElMessage.error(e.message || '风险评估失败')
  }
}

async function onConfirm(id: number) {
  try {
    await confirmWarning(id)
    ElMessage.success('已确认')
    await loadData()
  } catch (e: any) {
    ElMessage.error(e.message || '确认失败')
  }
}

async function onPublish(id: number) {
  try {
    await ElMessageBox.confirm('确认发布该预警？发布后将进入对外处置流程。', '发布确认', {
      type: 'warning',
      confirmButtonText: '确认发布',
      cancelButtonText: '取消',
    })
    await publishWarning(id)
    ElMessage.success('已发布')
    await loadData()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '发布失败')
    }
  }
}

async function onResolve(id: number) {
  try {
    await ElMessageBox.confirm('确认解除该预警？请确保处置动作已完成。', '解除确认', {
      type: 'warning',
      confirmButtonText: '确认解除',
      cancelButtonText: '取消',
    })
    await resolveWarning(id)
    ElMessage.success('已解除')
    await loadData()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '解除失败')
    }
  }
}

async function onDisposal(id: number) {
  try {
    const { value } = await ElMessageBox.prompt('请输入处置说明', '登记处置')
    await addDisposal(id, value)
    ElMessage.success('登记成功')
    await loadData()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '登记失败')
    }
  }
}

async function onAiExplain(id: number) {
  try {
    const data = await aiExplain(id)
    await ElMessageBox.alert(String(data.explanation || '暂无结果'), '智能预警解释')
  } catch (e: any) {
    ElMessage.error(e.message || '智能解释失败')
  }
}

function canConfirm(status: string) {
  return status === 'DRAFT'
}

function canPublish(status: string) {
  return status === 'CONFIRMED'
}

function canDisposal(status: string) {
  return status === 'PUBLISHED' || status === 'PROCESSING'
}

function canResolve(status: string) {
  return status === 'PUBLISHED' || status === 'PROCESSING'
}

function onSelectionChange(rows: Array<Record<string, any>>) {
  selectedRows.value = rows
  selectedIds.value = rows.map((x) => Number(x.id))
}

function isAllowedForBatch(action: 'confirm' | 'publish' | 'resolve', status: string) {
  if (action === 'confirm') return status === 'DRAFT'
  if (action === 'publish') return status === 'CONFIRMED'
  return status === 'PUBLISHED' || status === 'PROCESSING'
}

function formatBatchAction(action: 'confirm' | 'publish' | 'resolve') {
  if (action === 'confirm') return '确认'
  if (action === 'publish') return '发布'
  return '解除'
}

function escapeHtml(input: string) {
  return input
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;')
}

async function runBatch(action: 'confirm' | 'publish' | 'resolve') {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择预警记录')
    return
  }
  const allowed = selectedRows.value.filter((x) => isAllowedForBatch(action, String(x.status || '')))
  const skipped = selectedRows.value.filter((x) => !isAllowedForBatch(action, String(x.status || '')))
  const allowedIds = allowed.map((x) => Number(x.id))
  if (allowedIds.length === 0) {
    ElMessage.warning(`所选记录都不满足批量${formatBatchAction(action)}的状态要求`)
    return
  }
  try {
    if (action === 'publish') {
      await ElMessageBox.confirm(`确认批量发布 ${allowedIds.length} 条预警？`, '批量发布确认', {
        type: 'warning',
      })
    } else if (action === 'resolve') {
      await ElMessageBox.confirm(`确认批量解除 ${allowedIds.length} 条预警？`, '批量解除确认', {
        type: 'warning',
      })
    }
    let result: Record<string, any> = {}
    if (action === 'confirm') {
      result = await batchConfirm(allowedIds)
    } else if (action === 'publish') {
      result = await batchPublish(allowedIds)
    } else {
      result = await batchResolve(allowedIds)
    }
    ElMessage.success(`批量操作完成：成功${result.success}，失败${result.failed}，跳过${skipped.length}`)
    if (Array.isArray(result.fails) && result.fails.length > 0) {
      const detail = result.fails
        .map((x: Record<string, any>, idx: number) => `${idx + 1}. ID=${x.id}，原因=${x.reason || '操作失败'}`)
        .join('\n')
      await ElMessageBox.alert(
        `<pre style="white-space: pre-wrap; margin: 0;">${escapeHtml(detail)}</pre>`,
        '批量失败明细',
        { dangerouslyUseHTMLString: true },
      )
    }
    await loadData()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '批量操作失败')
    }
  }
}

onMounted(loadData)
</script>

<template>
  <el-card class="page-card">
    <div class="toolbar">
      <el-select v-model="query.status" placeholder="状态筛选" clearable style="width: 180px">
        <el-option label="草稿" value="DRAFT" />
        <el-option label="已确认" value="CONFIRMED" />
        <el-option label="已发布" value="PUBLISHED" />
        <el-option label="处置中" value="PROCESSING" />
        <el-option label="已解除" value="RESOLVED" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button type="warning" @click="onEvaluate">触发风险评估</el-button>
      <el-button type="success" plain @click="runBatch('confirm')">批量确认</el-button>
      <el-button type="primary" plain @click="runBatch('publish')">批量发布</el-button>
      <el-button type="danger" plain @click="runBatch('resolve')">批量解除</el-button>
    </div>
    <el-table :data="rows" v-loading="loading" stripe row-key="id" @selection-change="onSelectionChange">
      <el-table-column type="selection" width="42" />
      <el-table-column prop="warningCode" label="预警编号" width="220" />
      <el-table-column prop="targetRoad" label="目标道路" />
      <el-table-column label="风险等级" width="100">
        <template #default="{ row }">
          {{ formatRiskLevel(String(row.riskLevel || '')) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="130">
        <template #default="{ row }">
          {{ formatWarningStatus(String(row.status || '')) }}
        </template>
      </el-table-column>
      <el-table-column prop="triggerReason" label="触发原因" />
      <el-table-column label="操作" width="400">
        <template #default="{ row }">
          <el-button size="small" :disabled="!canConfirm(row.status)" @click="onConfirm(row.id)">确认</el-button>
          <el-button size="small" type="primary" :disabled="!canPublish(row.status)" @click="onPublish(row.id)">
            发布
          </el-button>
          <el-button size="small" :disabled="!canDisposal(row.status)" @click="onDisposal(row.id)">处置</el-button>
          <el-button size="small" type="danger" :disabled="!canResolve(row.status)" @click="onResolve(row.id)">
            解除
          </el-button>
          <el-button size="small" type="success" plain @click="onAiExplain(row.id)">智能解释</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      style="margin-top: 12px"
      layout="total, prev, pager, next"
      :total="total"
      :page-size="query.pageSize"
      :current-page="query.pageNum"
      @current-change="(p:number) => { query.pageNum = p; loadData() }"
    />
  </el-card>
</template>
