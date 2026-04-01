<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { onMounted, reactive, ref } from 'vue'
import { createAdminDict, deleteAdminDict, pageAdminDicts, updateAdminDict } from '../api/admin'
import { formatDictType } from '../utils/labels'

const rows = ref<Record<string, any>[]>([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)

const dictTypeOptions = [
  { label: '事故类型', value: 'ACCIDENT_TYPE' },
  { label: '道路类型', value: 'ROAD_TYPE' },
  { label: '预警等级', value: 'WARNING_LEVEL' },
]

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  dictType: '',
  enabled: '' as '' | '0' | '1',
})

const form = reactive({
  dictType: 'ACCIDENT_TYPE',
  dictCode: '',
  dictName: '',
  sortNo: 100,
  enabled: 1,
  remark: '',
})

async function loadData() {
  loading.value = true
  try {
    const params: Record<string, unknown> = {
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    }
    if (query.dictType) params.dictType = query.dictType
    if (query.enabled !== '') params.enabled = Number(query.enabled)
    const data = await pageAdminDicts(params)
    rows.value = data.records || []
    total.value = data.total || 0
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function onSearch() {
  query.pageNum = 1
  loadData()
}

function onReset() {
  query.pageNum = 1
  query.dictType = ''
  query.enabled = ''
  loadData()
}

function onCreate() {
  editingId.value = null
  form.dictType = 'ACCIDENT_TYPE'
  form.dictCode = ''
  form.dictName = ''
  form.sortNo = 100
  form.enabled = 1
  form.remark = ''
  dialogVisible.value = true
}

function onEdit(row: Record<string, any>) {
  editingId.value = Number(row.id)
  form.dictType = String(row.dictType || '')
  form.dictCode = String(row.dictCode || '')
  form.dictName = String(row.dictName || '')
  form.sortNo = Number(row.sortNo || 100)
  form.enabled = Number(row.enabled ?? 1)
  form.remark = String(row.remark || '')
  dialogVisible.value = true
}

async function onSubmit() {
  try {
    if (!form.dictName.trim()) {
      ElMessage.warning('字典名称不能为空')
      return
    }
    if (editingId.value) {
      await updateAdminDict(editingId.value, {
        dictName: form.dictName,
        sortNo: form.sortNo,
        enabled: form.enabled,
        remark: form.remark,
      })
    } else {
      if (!form.dictCode.trim()) {
        ElMessage.warning('字典编码不能为空')
        return
      }
      await createAdminDict({
        dictType: form.dictType,
        dictCode: form.dictCode,
        dictName: form.dictName,
        sortNo: form.sortNo,
        enabled: form.enabled,
        remark: form.remark,
      })
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadData()
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  }
}

async function onDelete(id: number) {
  try {
    await ElMessageBox.confirm('确认删除该字典项？', '删除确认', { type: 'warning' })
    await deleteAdminDict(id)
    ElMessage.success('删除成功')
    await loadData()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '删除失败')
    }
  }
}

onMounted(loadData)
</script>

<template>
  <el-card class="page-card">
    <template #header>字典管理</template>
    <div class="toolbar">
      <el-select v-model="query.dictType" clearable placeholder="字典类型" style="width: 180px">
        <el-option v-for="item in dictTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-select v-model="query.enabled" clearable placeholder="启用状态" style="width: 140px">
        <el-option label="启用" value="1" />
        <el-option label="停用" value="0" />
      </el-select>
      <el-button type="primary" @click="onSearch">查询</el-button>
      <el-button @click="onReset">重置</el-button>
      <el-button type="success" @click="onCreate">新增字典项</el-button>
    </div>

    <el-table :data="rows" stripe v-loading="loading">
      <el-table-column label="字典类型" width="180">
        <template #default="{ row }">
          {{ formatDictType(String(row.dictType || '')) }}
        </template>
      </el-table-column>
      <el-table-column prop="dictCode" label="字典编码" width="160" />
      <el-table-column prop="dictName" label="字典名称" />
      <el-table-column prop="sortNo" label="排序" width="80" />
      <el-table-column label="启用" width="90">
        <template #default="{ row }">
          <el-tag :type="row.enabled === 1 ? 'success' : 'info'">{{ row.enabled === 1 ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" />
      <el-table-column label="操作" width="170">
        <template #default="{ row }">
          <el-button size="small" @click="onEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="onDelete(Number(row.id))">删除</el-button>
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

  <el-dialog v-model="dialogVisible" :title="editingId ? '编辑字典项' : '新增字典项'" width="620px">
    <el-form label-width="90px">
      <el-form-item label="字典类型">
        <el-select v-model="form.dictType" :disabled="!!editingId" style="width: 100%">
          <el-option v-for="item in dictTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="字典编码">
        <el-input v-model="form.dictCode" :disabled="!!editingId" placeholder="例如：追尾编码（大写字母与下划线）" />
      </el-form-item>
      <el-form-item label="字典名称">
        <el-input v-model="form.dictName" placeholder="如 追尾" />
      </el-form-item>
      <el-form-item label="排序号">
        <el-input-number v-model="form.sortNo" :min="0" :max="9999" />
      </el-form-item>
      <el-form-item label="启用">
        <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="3" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="onSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>
