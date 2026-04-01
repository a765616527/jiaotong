<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { reactive, ref } from 'vue'
import { assistantQuery, disposalSuggest, explainWarning, extractAccidentFields } from '../api/ai'

const extractText = ref('夜间22点在东京大道发生两车追尾，1人受伤。')
const queryText = ref('请给出夜间事故防控建议。')
const explainForm = reactive({
  roadName: '东京大道',
  score: 78.6,
  triggerReason: '近7天事故频次高且夜间占比高',
})
const disposalForm = reactive({
  riskLevel: '高风险',
  roadName: '东京大道',
  triggerReason: '夜间事故集中',
})
const result = ref('等待调用...')
const TEMPLATE_KEY = 'ai_prompt_templates'

type TemplateType = 'query' | 'extract' | 'explain' | 'disposal'
type PromptTemplate = { id: string; name: string; type: TemplateType; content: string; builtin?: boolean }

const builtinTemplates: PromptTemplate[] = [
  { id: 'q1', name: '夜间防控建议', type: 'query', content: '请基于近30天夜间事故特点，给出3条可执行防控建议。', builtin: true },
  { id: 'q2', name: '路段治理问答', type: 'query', content: '东京大道本月事故高发的可能原因是什么？', builtin: true },
  { id: 'e1', name: '事故抽取模板', type: 'extract', content: '晚高峰在黄河大街发生三车追尾，1人轻伤，路面湿滑。', builtin: true },
  { id: 'w1', name: '预警解释模板', type: 'explain', content: '近7天事故频次高且夜间占比高，请给出100字内解释与建议。', builtin: true },
  { id: 'd1', name: '处置建议模板', type: 'disposal', content: '高风险，东京大道，夜间事故集中，请给出三条处置建议。', builtin: true },
]

const customTemplates = ref<PromptTemplate[]>([])
const templateForm = reactive({
  name: '',
  type: 'query' as TemplateType,
  content: '',
})
const selectedTemplateId = ref('')

function loadTemplates() {
  try {
    const raw = localStorage.getItem(TEMPLATE_KEY)
    customTemplates.value = raw ? (JSON.parse(raw) as PromptTemplate[]) : []
  } catch {
    customTemplates.value = []
  }
}

function saveTemplates() {
  localStorage.setItem(TEMPLATE_KEY, JSON.stringify(customTemplates.value))
}

function allTemplates() {
  return [...builtinTemplates, ...customTemplates.value]
}

function formatTemplateType(type: TemplateType) {
  if (type === 'query') return '问答'
  if (type === 'extract') return '抽取'
  if (type === 'explain') return '解释'
  return '处置'
}

function applyTemplate() {
  const target = allTemplates().find((x) => x.id === selectedTemplateId.value)
  if (!target) {
    ElMessage.warning('请选择模板')
    return
  }
  if (target.type === 'query') {
    queryText.value = target.content
  } else if (target.type === 'extract') {
    extractText.value = target.content
  } else if (target.type === 'explain') {
    explainForm.triggerReason = target.content
  } else if (target.type === 'disposal') {
    disposalForm.triggerReason = target.content
  }
  ElMessage.success('模板已应用')
}

function addTemplate() {
  if (!templateForm.name || !templateForm.content) {
    ElMessage.warning('名称和内容不能为空')
    return
  }
  customTemplates.value.unshift({
    id: `c_${Date.now()}`,
    name: templateForm.name,
    type: templateForm.type,
    content: templateForm.content,
  })
  saveTemplates()
  templateForm.name = ''
  templateForm.type = 'query'
  templateForm.content = ''
  ElMessage.success('模板已保存')
}

function removeTemplate(id: string) {
  customTemplates.value = customTemplates.value.filter((x) => x.id !== id)
  saveTemplates()
}

async function runExtract() {
  try {
    const data = await extractAccidentFields(extractText.value)
    result.value = JSON.stringify(data, null, 2)
  } catch (e: any) {
    ElMessage.error(e.message || '调用失败')
  }
}

async function runExplain() {
  try {
    const data = await explainWarning(explainForm)
    result.value = JSON.stringify(data, null, 2)
  } catch (e: any) {
    ElMessage.error(e.message || '调用失败')
  }
}

async function runDisposal() {
  try {
    const data = await disposalSuggest(disposalForm)
    result.value = JSON.stringify(data, null, 2)
  } catch (e: any) {
    ElMessage.error(e.message || '调用失败')
  }
}

async function runQuery() {
  try {
    const data = await assistantQuery(queryText.value)
    result.value = JSON.stringify(data, null, 2)
  } catch (e: any) {
    ElMessage.error(e.message || '调用失败')
  }
}

loadTemplates()
</script>

<template>
  <el-row :gutter="12" style="margin: 12px">
    <el-col :span="24">
      <el-card style="margin-bottom: 12px">
        <template #header>提示词模板管理（本地）</template>
        <div class="toolbar">
          <el-select v-model="selectedTemplateId" placeholder="选择模板" style="width: 260px">
            <el-option
              v-for="t in allTemplates()"
              :key="t.id"
              :label="`${t.name} [${formatTemplateType(t.type)}]${t.builtin ? '（内置）' : ''}`"
              :value="t.id"
            />
          </el-select>
          <el-button type="primary" @click="applyTemplate">应用模板</el-button>
        </div>
        <el-form inline>
          <el-form-item label="名称"><el-input v-model="templateForm.name" style="width: 160px" /></el-form-item>
          <el-form-item label="类型">
            <el-select v-model="templateForm.type" style="width: 130px">
              <el-option label="问答" value="query" />
              <el-option label="抽取" value="extract" />
              <el-option label="解释" value="explain" />
              <el-option label="处置" value="disposal" />
            </el-select>
          </el-form-item>
          <el-form-item label="内容">
            <el-input v-model="templateForm.content" style="width: 520px" />
          </el-form-item>
          <el-form-item>
            <el-button type="success" @click="addTemplate">保存模板</el-button>
          </el-form-item>
        </el-form>
        <el-table :data="customTemplates" size="small" style="margin-top: 8px">
          <el-table-column prop="name" label="名称" width="180" />
          <el-table-column label="类型" width="100">
            <template #default="{ row }">
              {{ formatTemplateType(row.type) }}
            </template>
          </el-table-column>
          <el-table-column prop="content" label="内容" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button size="small" type="danger" @click="removeTemplate(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-col>

    <el-col :span="12">
      <el-card>
        <template #header>事故文本结构化抽取</template>
        <el-input v-model="extractText" type="textarea" :rows="4" />
        <el-button style="margin-top: 10px" type="primary" @click="runExtract">调用</el-button>
      </el-card>
      <el-card style="margin-top: 12px">
        <template #header>预警解释生成</template>
        <el-form label-width="90px">
          <el-form-item label="道路"><el-input v-model="explainForm.roadName" /></el-form-item>
          <el-form-item label="评分"><el-input-number v-model="explainForm.score" /></el-form-item>
          <el-form-item label="触发原因"><el-input v-model="explainForm.triggerReason" /></el-form-item>
        </el-form>
        <el-button type="primary" @click="runExplain">调用</el-button>
      </el-card>
    </el-col>
    <el-col :span="12">
      <el-card>
        <template #header>处置建议生成</template>
        <el-form label-width="90px">
          <el-form-item label="风险等级">
            <el-select v-model="disposalForm.riskLevel" style="width: 100%">
              <el-option label="高风险" value="高风险" />
              <el-option label="中风险" value="中风险" />
              <el-option label="低风险" value="低风险" />
            </el-select>
          </el-form-item>
          <el-form-item label="道路"><el-input v-model="disposalForm.roadName" /></el-form-item>
          <el-form-item label="触发原因"><el-input v-model="disposalForm.triggerReason" /></el-form-item>
        </el-form>
        <el-button type="primary" @click="runDisposal">调用</el-button>
      </el-card>
      <el-card style="margin-top: 12px">
        <template #header>管理员问答</template>
        <el-input v-model="queryText" type="textarea" :rows="3" />
        <el-button style="margin-top: 10px" type="primary" @click="runQuery">调用</el-button>
      </el-card>
    </el-col>
  </el-row>

  <el-card class="page-card">
    <template #header>模型输出</template>
    <pre style="white-space: pre-wrap">{{ result }}</pre>
  </el-card>
</template>
