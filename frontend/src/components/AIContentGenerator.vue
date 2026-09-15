<template>
  <el-dialog
    v-model="dialogVisible"
    title="AI文案生成"
    width="720px"
    :close-on-click-modal="false"
    @close="handleClose"
    class="ai-generator-dialog"
  >
    <div class="content-generator">
      <div class="ai-alert">
        <div class="ai-alert-icon">
          <el-icon><MagicStick /></el-icon>
        </div>
        <div class="ai-alert-content">
          <div class="ai-alert-title">AI智能文案生成</div>
          <div class="ai-alert-desc">根据您填写的房源信息，AI将自动生成优质宣传文案</div>
        </div>
      </div>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
        size="default"
        class="ai-form"
      >
        <div class="form-section">
          <div class="section-title">
            <el-icon><Edit /></el-icon>
            基本信息
          </div>
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="房源标题" prop="title">
                <el-input v-model="formData.title" placeholder="请输入房源标题" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="房源类型" prop="propertyType">
                <el-select v-model="formData.propertyType" placeholder="请选择" style="width: 100%">
                  <el-option v-for="item in PROPERTY_TYPE_LIST" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <div class="form-section">
          <div class="section-title">
            <el-icon><Money /></el-icon>
            价格与面积
          </div>
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="交易类型" prop="transactionType">
                <el-select v-model="formData.transactionType" placeholder="请选择" style="width: 100%">
                  <el-option v-for="item in TRANSACTION_TYPE_LIST" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="价格" prop="price">
                <el-input-number
                  v-model="formData.price"
                  :min="0"
                  placeholder="请输入价格"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="24">
            <el-col :span="8">
              <el-form-item label="面积(㎡)" prop="area">
                <el-input-number
                  v-model="formData.area"
                  :min="0"
                  placeholder="面积"
                  style="width: 100%"
                  controls-position="right"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="卧室数" prop="bedrooms">
                <el-input-number
                  v-model="formData.bedrooms"
                  :min="0"
                  :max="10"
                  placeholder="卧室"
                  style="width: 100%"
                  controls-position="right"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="卫生间数" prop="bathrooms">
                <el-input-number
                  v-model="formData.bathrooms"
                  :min="0"
                  :max="10"
                  placeholder="卫生间"
                  style="width: 100%"
                  controls-position="right"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <div class="form-section">
          <div class="section-title">
            <el-icon><Location /></el-icon>
            位置信息
          </div>
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="城市" prop="city">
                <el-input v-model="formData.city" placeholder="请输入城市" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="区域" prop="district">
                <el-input v-model="formData.district" placeholder="请输入区域" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="详细地址" prop="address">
            <el-input v-model="formData.address" placeholder="请输入详细地址" />
          </el-form-item>
        </div>

        <div class="form-section">
          <div class="section-title">
            <el-icon><Star /></el-icon>
            特色配置
          </div>
          <el-form-item label="配套设施">
            <el-input
              v-model="formData.facilities"
              placeholder="例如：地铁,学校,商场,公园（逗号分隔）"
            />
          </el-form-item>
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="装修情况">
                <el-input v-model="formData.decoration" placeholder="例如：精装修" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="朝向">
                <el-input v-model="formData.orientation" placeholder="例如：南北通透" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="楼层">
            <el-input v-model="formData.floor" placeholder="例如：10/30层" />
          </el-form-item>
        </div>

        <div class="form-section">
          <div class="section-title">
            <el-icon><Document /></el-icon>
            文案设置
          </div>
          <el-form-item label="文案版本" prop="version">
            <el-radio-group v-model="formData.version" class="version-radio">
              <el-radio label="short" class="version-radio-item">
                <div class="radio-card">
                  <div class="radio-card-title">简短版</div>
                  <div class="radio-card-desc">100-200字，精炼突出卖点</div>
                </div>
              </el-radio>
              <el-radio label="detailed" class="version-radio-item">
                <div class="radio-card">
                  <div class="radio-card-title">详细版</div>
                  <div class="radio-card-desc">300-500字，全面展示优势</div>
                </div>
              </el-radio>
            </el-radio-group>
          </el-form-item>
        </div>
      </el-form>

      <div class="generate-actions">
        <el-button
          type="primary"
          :icon="MagicStick"
          @click="handleGenerate"
          :loading="generating"
          size="large"
          class="generate-btn"
        >
          {{ generating ? 'AI生成中...' : '一键生成文案' }}
        </el-button>
      </div>

      <div v-if="generatedContent" class="generated-content">
        <div class="result-header">
          <div class="result-title">
            <el-icon><Document /></el-icon>
            生成结果
          </div>
        </div>

        <el-tabs v-model="activeTab" type="card" class="result-tabs">
          <el-tab-pane label="简短版文案" name="short">
            <div class="content-display">
              <div class="content-textarea-wrap">
                <el-input
                  v-model="generatedContent.shortVersion"
                  type="textarea"
                  :rows="8"
                  readonly
                  class="content-textarea"
                />
              </div>
              <div class="content-info">
                <div class="info-left">
                  <el-tag size="small" type="primary" effect="light" round>简短版</el-tag>
                  <el-tag size="small" type="success" effect="light" round>
                    {{ generatedContent.shortVersion.length }} 字
                  </el-tag>
                </div>
                <span v-if="generatedContent.generationTime" class="generate-time">
                  耗时：{{ generatedContent.generationTime }}ms
                </span>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="详细版文案" name="detailed">
            <div class="content-display">
              <div class="content-textarea-wrap">
                <el-input
                  v-model="generatedContent.detailVersion"
                  type="textarea"
                  :rows="12"
                  readonly
                  class="content-textarea"
                />
              </div>
              <div class="content-info">
                <div class="info-left">
                  <el-tag size="small" type="primary" effect="light" round>详细版</el-tag>
                  <el-tag size="small" type="success" effect="light" round>
                    {{ generatedContent.detailVersion.length }} 字
                  </el-tag>
                </div>
                <span v-if="generatedContent.generationTime" class="generate-time">
                  耗时：{{ generatedContent.generationTime }}ms
                </span>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>

        <div class="content-actions">
          <el-button
            type="success"
            :icon="CopyDocument"
            @click="handleCopy"
            class="action-btn"
          >
            复制文案
          </el-button>
          <el-button
            type="primary"
            :icon="Check"
            @click="handleApply"
            class="action-btn apply-btn"
          >
            应用到描述框
          </el-button>
          <el-button
            :icon="RefreshRight"
            @click="handleRegenerate"
            class="action-btn"
          >
            重新生成
          </el-button>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose" class="footer-cancel-btn">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { aiApi } from '@/api/ai'
import type { ContentRequest, ContentGenerationResponse } from '@/types/ai'
import { PROPERTY_TYPE_LIST, TRANSACTION_TYPE_LIST } from '@/utils/constants'
import {
  MagicStick,
  Document,
  CopyDocument,
  Check,
  RefreshRight,
  Edit,
  Money,
  Location,
  Star
} from '@element-plus/icons-vue'

interface Props {
  modelValue: boolean
  propertyData?: any
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  propertyData: undefined
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'apply', content: string): void
}>()

const dialogVisible = ref(props.modelValue)
const formRef = ref()

const formData = reactive<ContentRequest>({
  title: '',
  propertyType: 1,
  transactionType: 1,
  price: 0,
  area: 0,
  bedrooms: 0,
  bathrooms: 0,
  city: '',
  district: '',
  address: '',
  facilities: '',
  decoration: '',
  orientation: '',
  floor: '',
  version: 'short'
})

const rules = {
  title: [{ required: true, message: '请输入房源标题', trigger: 'blur' }],
  propertyType: [{ required: true, message: '请选择房源类型', trigger: 'change' }],
  transactionType: [{ required: true, message: '请选择交易类型', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'change' }],
  area: [{ required: true, message: '请输入面积', trigger: 'change' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  address: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
  version: [{ required: true, message: '请选择文案版本', trigger: 'change' }]
}

const generating = ref(false)

interface GeneratedContent {
  shortVersion: string
  detailVersion: string
  generationId?: number
  generationTime?: number
  selectedVersion: 'short' | 'detailed'
}

const generatedContent = ref<GeneratedContent | null>(null)
const activeTab = ref('short')

const syncPropertyData = (data: any) => {
  if (data) {
    Object.assign(formData, {
      title: data.title || '',
      propertyType: data.propertyType || 1,
      transactionType: data.transactionType || 1,
      price: data.price || 0,
      area: data.area || 0,
      bedrooms: data.bedrooms || 0,
      bathrooms: data.bathrooms || 0,
      city: data.city || '',
      district: data.district || '',
      address: data.address || '',
      facilities: data.facilities || '',
      decoration: data.decoration || '',
      orientation: data.orientation || '',
      floor: data.floor || ''
    })
  }
}

watch(() => props.modelValue, (val) => {
  dialogVisible.value = val
})

watch(dialogVisible, (val) => {
  emit('update:modelValue', val)
  if (val) {
    syncPropertyData(props.propertyData)
  }
})

watch(() => props.propertyData, (data) => {
  syncPropertyData(data)
}, { immediate: true })

const handleGenerate = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch {
    ElMessage.warning('请完善必填信息')
    return
  }

  generating.value = true

  try {
    const response = await aiApi.generateContent(formData)

    if (response.code === 200 && response.data) {
      generatedContent.value = {
        shortVersion: response.data.shortVersion,
        detailVersion: response.data.detailVersion,
        generationId: response.data.generationId,
        generationTime: response.data.generationTime,
        selectedVersion: formData.version as 'short' | 'detailed'
      }
      ElMessage.success('文案生成成功')
      activeTab.value = formData.version === 'short' ? 'short' : 'detailed'
    } else {
      ElMessage.error(response.message || '文案生成失败')
    }
  } catch (error: any) {
    console.error('文案生成失败:', error)
    ElMessage.error(error.message || 'AI服务异常，请稍后重试')
  } finally {
    generating.value = false
  }
}

const handleRegenerate = () => {
  generatedContent.value = null
  handleGenerate()
}

const handleCopy = async () => {
  if (!generatedContent.value) return

  const contentToCopy = activeTab.value === 'short' 
    ? generatedContent.value.shortVersion 
    : generatedContent.value.detailVersion

  try {
    await navigator.clipboard.writeText(contentToCopy)
    ElMessage.success('文案已复制到剪贴板')
  } catch (error) {
    const textarea = document.createElement('textarea')
    textarea.value = contentToCopy
    textarea.style.position = 'fixed'
    textarea.style.opacity = '0'
    document.body.appendChild(textarea)
    textarea.select()
    try {
      document.execCommand('copy')
      ElMessage.success('文案已复制到剪贴板')
    } catch (err) {
      ElMessage.error('复制失败，请手动复制')
    }
    document.body.removeChild(textarea)
  }
}

const handleApply = () => {
  if (!generatedContent.value) return

  const contentToApply = activeTab.value === 'short' 
    ? generatedContent.value.shortVersion 
    : generatedContent.value.detailVersion

  emit('apply', contentToApply)
  ElMessage.success('文案已应用到描述框')
  handleClose()
}

const handleClose = () => {
  dialogVisible.value = false
}
</script>

<style scoped>
.ai-generator-dialog :deep(.el-dialog) {
  border-radius: var(--radius-2xl) !important;
  overflow: hidden;
}

.ai-generator-dialog :deep(.el-dialog__header) {
  padding: var(--spacing-xl) var(--spacing-xl) var(--spacing-lg);
  background: linear-gradient(135deg, var(--primary-color-bg-light) 0%, white 100%);
  border-bottom: 1px solid var(--border-color-light);
}

.ai-generator-dialog :deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-color-primary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.ai-generator-dialog :deep(.el-dialog__body) {
  padding: var(--spacing-lg) var(--spacing-xl);
  max-height: 70vh;
  overflow-y: auto;
}

.ai-generator-dialog :deep(.el-dialog__footer) {
  padding: var(--spacing-md) var(--spacing-xl);
  border-top: 1px solid var(--border-color-light);
  background: var(--bg-color-light);
}

.content-generator {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.ai-alert {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-md) var(--spacing-lg);
  background: linear-gradient(135deg, var(--primary-color-bg-light) 0%, var(--accent-color-bg-light) 100%);
  border-radius: var(--radius-lg);
  border: 1px solid var(--primary-color-bg);
}

.ai-alert-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-lg);
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--accent-color) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 22px;
  flex-shrink: 0;
}

.ai-alert-content {
  flex: 1;
}

.ai-alert-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-color-primary);
  margin-bottom: 2px;
}

.ai-alert-desc {
  font-size: 13px;
  color: var(--text-color-secondary);
}

.ai-form {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.form-section {
  background: var(--bg-color-light);
  border-radius: var(--radius-lg);
  padding: var(--spacing-md) var(--spacing-lg);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-color-primary);
  margin-bottom: var(--spacing-md);
}

.section-title .el-icon {
  color: var(--primary-color);
  font-size: 16px;
}

.ai-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--text-color-regular);
}

.ai-form :deep(.el-input__wrapper),
.ai-form :deep(.el-textarea__inner),
.ai-form :deep(.el-select__wrapper),
.ai-form :deep(.el-input-number) {
  border-radius: var(--radius-md) !important;
  transition: all 0.25s ease !important;
}

.ai-form :deep(.el-input__wrapper:hover),
.ai-form :deep(.el-textarea__inner:hover),
.ai-form :deep(.el-select__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--primary-color-light) inset !important;
}

.ai-form :deep(.el-input__wrapper.is-focus),
.ai-form :deep(.el-textarea__inner:focus),
.ai-form :deep(.el-select__wrapper.is-focused) {
  box-shadow: 0 0 0 1px var(--primary-color) inset !important;
}

.version-radio {
  display: flex;
  gap: var(--spacing-md);
  width: 100%;
}

.version-radio-item {
  flex: 1;
  margin: 0 !important;
}

.version-radio-item :deep(.el-radio__input) {
  display: none;
}

.version-radio-item :deep(.el-radio__label) {
  padding: 0;
  width: 100%;
}

.radio-card {
  padding: var(--spacing-md) var(--spacing-lg);
  border: 2px solid var(--border-color-light);
  border-radius: var(--radius-lg);
  background: var(--bg-color);
  transition: all 0.25s ease;
  cursor: pointer;
}

.version-radio-item:hover .radio-card {
  border-color: var(--primary-color-light);
}

.version-radio-item.is-checked .radio-card {
  border-color: var(--primary-color);
  background: var(--primary-color-bg-light);
}

.radio-card-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-color-primary);
  margin-bottom: 4px;
}

.radio-card-desc {
  font-size: 12px;
  color: var(--text-color-secondary);
}

.generate-actions {
  text-align: center;
  padding: var(--spacing-sm) 0;
}

.generate-btn {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--accent-color) 100%) !important;
  border: none !important;
  border-radius: var(--radius-full) !important;
  padding: 0 40px !important;
  height: 48px !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  transition: all 0.3s ease !important;
}

.generate-btn:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 8px 24px rgba(22, 119, 255, 0.35) !important;
}

.generated-content {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.result-header {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-sm) 0;
}

.result-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-color-primary);
}

.result-title .el-icon {
  color: var(--primary-color);
  font-size: 18px;
}

.result-tabs {
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.result-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.result-tabs :deep(.el-tabs__item) {
  border-radius: var(--radius-md) var(--radius-md) 0 0;
  font-weight: 500;
}

.result-tabs :deep(.el-tabs__item.is-active) {
  color: var(--primary-color);
  font-weight: 600;
}

.content-display {
  padding-top: var(--spacing-sm);
}

.content-textarea-wrap {
  margin-bottom: var(--spacing-sm);
}

.content-textarea :deep(.el-textarea__inner) {
  font-size: 14px;
  line-height: 1.8;
  color: var(--text-color-primary);
  background: var(--bg-color-light);
  border-radius: var(--radius-md);
  padding: var(--spacing-md);
  resize: none;
}

.content-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.info-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.generate-time {
  font-size: 12px;
  color: var(--text-color-tertiary);
}

.content-actions {
  display: flex;
  justify-content: center;
  gap: var(--spacing-md);
  margin-top: var(--spacing-md);
  flex-wrap: wrap;
}

.action-btn {
  border-radius: var(--radius-full) !important;
  padding: 0 24px !important;
  height: 40px !important;
  font-weight: 500 !important;
  transition: all 0.25s ease !important;
}

.action-btn:hover {
  transform: translateY(-1px) !important;
}

.apply-btn {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-color-dark) 100%) !important;
  border: none !important;
}

.apply-btn:hover {
  box-shadow: 0 4px 16px rgba(22, 119, 255, 0.3) !important;
}

.footer-cancel-btn {
  border-radius: var(--radius-full) !important;
  padding: 0 24px !important;
  transition: all 0.25s ease !important;
}

.footer-cancel-btn:hover {
  border-color: var(--primary-color) !important;
  color: var(--primary-color) !important;
}

.ai-generator-dialog :deep(.el-dialog__body)::-webkit-scrollbar {
  width: 6px;
}

.ai-generator-dialog :deep(.el-dialog__body)::-webkit-scrollbar-track {
  background: transparent;
}

.ai-generator-dialog :deep(.el-dialog__body)::-webkit-scrollbar-thumb {
  background: var(--text-color-placeholder);
  border-radius: 3px;
}

.ai-generator-dialog :deep(.el-dialog__body)::-webkit-scrollbar-thumb:hover {
  background: var(--text-color-tertiary);
}

@media (max-width: 768px) {
  .ai-generator-dialog :deep(.el-dialog) {
    margin: 5vh var(--spacing-md) !important;
    width: auto !important;
  }

  .version-radio {
    flex-direction: column;
  }

  .content-actions {
    flex-direction: column;
  }

  .action-btn {
    width: 100%;
  }
}
</style>
