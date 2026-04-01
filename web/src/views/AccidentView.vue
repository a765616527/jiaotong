<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { onMounted, reactive, ref } from 'vue'
import {
  createAccident,
  deleteAccident,
  importAccidentCsv,
  pageAccidents,
  updateAccident,
} from '../api/accident'
import { extractAccidentFields } from '../api/ai'
import { listDictItems } from '../api/dict'

const loading = ref(false)
const rows = ref<Record<string, any>[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const csvInputRef = ref<HTMLInputElement>()
const accidentTypeOptions = ref<Array<{ dictCode: string; dictName: string }>>([])
const accidentTypeDictReady = ref(false)
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  roadName: '',
  accidentType: '',
})
const form = reactive({
  occurTime: '',
  roadName: '',
  areaCode: '',
  longitude: 114.307,
  latitude: 34.797,
  accidentType: '追尾',
  vehicleCount: 2,
  casualtyCount: 0,
  description: '',
})

function escapeHtml(input: string) {
  return input
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;')
}

async function loadData() {
  loading.value = true
  try {
    const data = await pageAccidents(query)
    rows.value = data.records || []
    total.value = data.total || 0
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function loadAccidentTypeOptions() {
  try {
    const data = await listDictItems('ACCIDENT_TYPE', 1)
    accidentTypeOptions.value = data.map((x) => ({
      dictCode: String(x.dictCode || ''),
      dictName: String(x.dictName || ''),
    }))
    accidentTypeDictReady.value = true
  } catch (e: any) {
    accidentTypeDictReady.value = false
    ElMessage.warning(e.message || '事故类型字典加载失败，已切换为手工输入')
  }
}

function onCreate() {
  editingId.value = null
  form.occurTime = new Date().toISOString().slice(0, 19)
  form.roadName = ''
  form.areaCode = ''
  form.longitude = 114.307
  form.latitude = 34.797
  form.accidentType = '追尾'
  form.vehicleCount = 2
  form.casualtyCount = 0
  form.description = ''
  dialogVisible.value = true
}

function onEdit(row: Record<string, any>) {
  editingId.value = row.id
  form.occurTime = row.occurTime
  form.roadName = row.roadName
  form.areaCode = row.areaCode
  form.longitude = row.longitude
  form.latitude = row.latitude
  form.accidentType = row.accidentType
  form.vehicleCount = row.vehicleCount
  form.casualtyCount = row.casualtyCount
  form.description = row.description
  dialogVisible.value = true
}

async function onSubmit() {
  try {
    if (editingId.value) {
      await updateAccident(editingId.value, form)
    } else {
      await createAccident(form)
    }
    dialogVisible.value = false
    ElMessage.success('保存成功')
    await loadData()
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  }
}

async function onDelete(id: number) {
  try {
    await deleteAccident(id)
    ElMessage.success('删除成功')
    await loadData()
  } catch (e: any) {
    ElMessage.error(e.message || '删除失败')
  }
}

async function onAiExtract() {
  if (!form.description) {
    ElMessage.warning('请先输入事故描述')
    return
  }
  try {
    const data = await extractAccidentFields(form.description)
    if (typeof data.accidentType === 'string' && data.accidentType) {
      const raw = data.accidentType.trim()
      const byName = accidentTypeOptions.value.find((x) => x.dictName === raw)
      const byCode = accidentTypeOptions.value.find((x) => x.dictCode === raw.toUpperCase())
      if (byName) {
        form.accidentType = byName.dictName
      } else if (byCode) {
        form.accidentType = byCode.dictName
      } else {
        ElMessage.warning(`智能抽取到的类型[${raw}]未在字典中，请手工选择`)
      }
    }
    ElMessage.success('智能抽取完成')
  } catch (e: any) {
    ElMessage.error(e.message || '智能抽取失败')
  }
}

function onPickCsv() {
  csvInputRef.value?.click()
}

function downloadCsvTemplate() {
  const header =
    'occurTime,roadName,areaCode,longitude,latitude,accidentType,vehicleCount,casualtyCount,description\n'
  const sample =
    '2026-03-29 08:20:00,金明大道,410200,114.307000,34.797000,追尾,2,0,早高峰路段轻微追尾\n'
  const blob = new Blob([`\uFEFF${header}${sample}`], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'accident_import_template.csv'
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

async function onCsvChange(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return
  try {
    const result = await importAccidentCsv(file)
    ElMessage.success(`导入完成：成功${result.success}，失败${result.failed}`)
    if (Array.isArray(result.errors) && result.errors.length > 0) {
      const detail = result.errors
        .slice(0, 20)
        .map(
          (x: Record<string, any>, idx: number) =>
            `${idx + 1}. 第${x.line}行：${x.reason || '解析失败'}\n内容：${x.content || ''}`,
        )
        .join('\n\n')
      const suffix = result.errors.length > 20 ? `\n\n仅展示前20条，共${result.errors.length}条` : ''
      await ElMessageBox.alert(
        `<pre style="white-space: pre-wrap; margin: 0;">${escapeHtml(detail + suffix)}</pre>`,
        '导入错误明细',
        { dangerouslyUseHTMLString: true },
      )
    }
    await loadData()
  } catch (e: any) {
    ElMessage.error(e.message || '导入失败')
  } finally {
    target.value = ''
  }
}

onMounted(loadData)
onMounted(loadAccidentTypeOptions)
</script>

<template>
  <el-card class="page-card">
    <div class="toolbar">
      <el-input v-model="query.roadName" placeholder="按道路筛选" style="width: 220px" />
      <el-select v-model="query.accidentType" clearable placeholder="事故类型" style="width: 160px">
        <el-option
          v-for="item in accidentTypeOptions"
          :key="item.dictCode"
          :label="item.dictName"
          :value="item.dictName"
        />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button type="success" @click="onCreate">新增事故</el-button>
      <el-button type="warning" plain @click="onPickCsv">导入逗号分隔文件</el-button>
      <el-button plain @click="downloadCsvTemplate">下载导入模板</el-button>
      <input
        ref="csvInputRef"
        type="file"
        accept=".csv,text/csv"
        style="display: none"
        @change="onCsvChange"
      />
    </div>
    <el-table :data="rows" stripe v-loading="loading">
      <el-table-column prop="occurTime" label="事故时间" width="180" />
      <el-table-column prop="roadName" label="道路" />
      <el-table-column prop="accidentType" label="类型" width="120" />
      <el-table-column prop="vehicleCount" label="车辆数" width="100" />
      <el-table-column prop="casualtyCount" label="伤亡数" width="100" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="onEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="onDelete(row.id)">删除</el-button>
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

  <el-dialog v-model="dialogVisible" :title="editingId ? '编辑事故' : '新增事故'" width="640px">
    <el-form label-width="90px">
      <el-form-item label="事故时间">
        <el-input v-model="form.occurTime" placeholder="2026-03-29T10:00:00" />
      </el-form-item>
      <el-form-item label="道路">
        <el-input v-model="form.roadName" />
      </el-form-item>
      <el-form-item label="区域编码">
        <el-input v-model="form.areaCode" />
      </el-form-item>
      <el-form-item label="经纬度">
        <div style="display: flex; gap: 8px; width: 100%">
          <el-input-number v-model="form.longitude" :precision="6" :step="0.0001" />
          <el-input-number v-model="form.latitude" :precision="6" :step="0.0001" />
        </div>
      </el-form-item>
      <el-form-item label="事故类型">
        <el-select
          v-if="accidentTypeDictReady && accidentTypeOptions.length > 0"
          v-model="form.accidentType"
          filterable
          style="width: 100%"
          placeholder="请选择事故类型"
        >
          <el-option
            v-for="item in accidentTypeOptions"
            :key="item.dictCode"
            :label="item.dictName"
            :value="item.dictName"
          />
        </el-select>
        <el-input v-else v-model="form.accidentType" placeholder="请输入事故类型" />
      </el-form-item>
      <el-form-item label="车辆数">
        <el-input-number v-model="form.vehicleCount" :min="1" />
      </el-form-item>
      <el-form-item label="伤亡数">
        <el-input-number v-model="form.casualtyCount" :min="0" />
      </el-form-item>
      <el-form-item label="事故描述">
        <el-input v-model="form.description" type="textarea" :rows="4" />
      </el-form-item>
      <el-form-item>
        <el-button @click="onAiExtract">智能抽取字段</el-button>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="onSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>
