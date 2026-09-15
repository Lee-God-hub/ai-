<template>
  <div class="edit-property-view page-animate">
    <div class="page-header">
      <div class="section-title">
        <span class="title-bar"></span>
        <h1>编辑房源</h1>
      </div>
      <el-button
        type="primary"
        :icon="MagicStick"
        @click="showAIGenerator"
        class="ai-btn"
      >
        AI生成文案
      </el-button>
    </div>

    <div class="form-card" v-loading="pageLoading">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="110px"
        class="property-form"
      >
        <div class="form-section">
          <div class="section-subtitle">
            <el-icon><Edit /></el-icon>
            基本信息
          </div>
          <el-form-item label="房源标题" prop="title">
            <el-input v-model="formData.title" placeholder="请输入房源标题" />
          </el-form-item>
          <el-form-item label="详细描述" prop="description">
            <el-input
              v-model="formData.description"
              type="textarea"
              :rows="5"
              placeholder="请输入详细描述，或使用AI生成文案"
            />
          </el-form-item>
        </div>

        <div class="form-section">
          <div class="section-subtitle">
            <el-icon><Money /></el-icon>
            价格信息
          </div>
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="价格类型" prop="priceType">
                <el-select v-model="formData.priceType" placeholder="请选择" style="width: 100%">
                  <el-option label="月租" :value="0" />
                  <el-option label="总价" :value="1" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="价格" prop="price">
                <el-input-number
                  v-model="formData.price"
                  :min="0"
                  :precision="2"
                  placeholder="请输入价格"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <div class="form-section">
          <div class="section-subtitle">
            <el-icon><House /></el-icon>
            房屋详情
          </div>
          <el-row :gutter="24">
            <el-col :span="8">
              <el-form-item label="面积" prop="area">
                <el-input-number
                  v-model="formData.area"
                  :min="0"
                  :precision="2"
                  placeholder="面积（㎡）"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="卧室数量" prop="bedrooms">
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
              <el-form-item label="卫生间数量" prop="bathrooms">
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
          <div class="section-subtitle">
            <el-icon><Location /></el-icon>
            位置信息
          </div>
          <el-row :gutter="24">
            <el-col :span="8">
              <el-form-item label="城市" prop="city">
                <el-input v-model="formData.city" placeholder="请输入城市" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="区域" prop="district">
                <el-input v-model="formData.district" placeholder="请输入区域" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="详细地址" prop="address">
                <el-input v-model="formData.address" placeholder="请输入详细地址" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <div class="form-section">
          <div class="section-subtitle">
            <el-icon><Menu /></el-icon>
            房源分类
          </div>
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="房源类型" prop="propertyType">
                <el-select v-model="formData.propertyType" placeholder="请选择" style="width: 100%">
                  <el-option v-for="item in PROPERTY_TYPE_LIST" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="交易类型" prop="transactionType">
                <el-select v-model="formData.transactionType" placeholder="请选择" style="width: 100%">
                  <el-option v-for="item in TRANSACTION_TYPE_LIST" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <div class="form-actions">
          <el-button type="primary" @click="submit" :loading="loading" class="submit-btn">
            <el-icon><Check /></el-icon>
            保存修改
          </el-button>
          <el-button @click="goBack" class="cancel-btn">
            <el-icon><Close /></el-icon>
            取消
          </el-button>
        </div>
      </el-form>
    </div>

    <!-- AI文案生成组件 -->
    <AIContentGenerator
      v-model="showGenerator"
      :property-data="formData"
      @apply="handleApplyContent"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  MagicStick,
  Edit,
  Money,
  House,
  Location,
  Menu,
  Check,
  Close
} from '@element-plus/icons-vue'
import { propertyApi } from '@/api/user'
import type { Property } from '@/types/user'
import { PROPERTY_TYPE_LIST, TRANSACTION_TYPE_LIST } from '@/utils/constants'
import AIContentGenerator from '@/components/AIContentGenerator.vue'

const route = useRoute()
const router = useRouter()

const isAdminMode = computed(() => route.path.startsWith('/admin'))
const returnPath = computed(() => isAdminMode.value ? '/admin/property' : '/landlord/my-properties')

const formRef = ref()
const loading = ref(false)
const pageLoading = ref(false)
const showGenerator = ref(false)

const formData = reactive<Property>({
  id: undefined,
  title: '',
  description: '',
  price: 0,
  priceType: 0,
  area: 0,
  bedrooms: 0,
  bathrooms: 0,
  city: '',
  district: '',
  address: '',
  propertyType: 0,
  transactionType: 0
})

const rules = {
  title: [{ required: true, message: '请输入房源标题', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'change' }],
  area: [{ required: true, message: '请输入面积', trigger: 'change' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  address: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

/**
 * 显示AI文案生成器
 */
const showAIGenerator = () => {
  showGenerator.value = true
}

/**
 * 应用生成的文案
 */
const handleApplyContent = (content: string) => {
  formData.description = content
}

/**
 * 加载房源详情
 */
const loadPropertyDetail = async () => {
  const id = route.params.id as string
  if (!id) {
    ElMessage.error('房源ID不存在')
    goBack()
    return
  }

  pageLoading.value = true
  try {
    const response = await propertyApi.getDetail(Number(id))
    if (response.code === 200 && response.data) {
      Object.assign(formData, response.data)
    } else {
      ElMessage.error(response.message || '加载房源信息失败')
      goBack()
    }
  } catch (error) {
    console.error('加载房源详情失败:', error)
    ElMessage.error('加载房源信息失败')
    goBack()
  } finally {
    pageLoading.value = false
  }
}

/**
 * 提交表单
 */
const submit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    try {
      loading.value = true
      const res = isAdminMode.value
        ? await propertyApi.adminUpdate(formData)
        : await propertyApi.update(formData)
      if (res.code === 200) {
        ElMessage.success('修改成功')
        goBack()
      } else {
        ElMessage.error(res.message || '修改失败')
      }
    } catch (error) {
      console.error('修改房源失败:', error)
      ElMessage.error('修改失败')
    } finally {
      loading.value = false
    }
  })
}

/**
 * 返回上一页
 */
const goBack = () => {
  router.push(returnPath.value)
}

// 页面加载时获取房源详情
onMounted(() => {
  loadPropertyDetail()
})
</script>

<style scoped>
.edit-property-view {
  padding: var(--spacing-xl) var(--spacing-2xl);
  min-height: 100%;
  background: var(--bg-color-page);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-xl);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.section-title h1 {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-color-primary);
  margin: 0;
  letter-spacing: 0.5px;
}

.title-bar {
  width: 5px;
  height: 28px;
  background: linear-gradient(180deg, var(--primary-color) 0%, var(--accent-color) 100%);
  border-radius: 3px;
}

.ai-btn {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--accent-color) 100%) !important;
  border: none !important;
  border-radius: var(--radius-full) !important;
  padding: 0 24px !important;
  height: 40px !important;
  font-weight: 600 !important;
  transition: all 0.3s ease !important;
}

.ai-btn:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 6px 20px rgba(22, 119, 255, 0.35) !important;
}

.form-card {
  background: var(--bg-color);
  border-radius: var(--radius-2xl);
  padding: var(--spacing-2xl);
  box-shadow: var(--shadow-base);
  border: 1px solid var(--border-color-light);
  transition: var(--transition-base);
}

.form-card:hover {
  box-shadow: var(--shadow-lg);
}

.property-form {
  max-width: 900px;
  margin: 0 auto;
}

.form-section {
  margin-bottom: var(--spacing-2xl);
  padding-bottom: var(--spacing-xl);
  border-bottom: 1px dashed var(--border-color-light);
}

.form-section:last-of-type {
  border-bottom: none;
  margin-bottom: var(--spacing-xl);
  padding-bottom: 0;
}

.section-subtitle {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-color-primary);
  margin-bottom: var(--spacing-lg);
  padding-left: var(--spacing-sm);
}

.section-subtitle .el-icon {
  color: var(--primary-color);
  font-size: 18px;
}

.property-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--text-color-regular);
}

.property-form :deep(.el-input__wrapper),
.property-form :deep(.el-textarea__inner),
.property-form :deep(.el-select__wrapper),
.property-form :deep(.el-input-number) {
  border-radius: var(--radius-md) !important;
  transition: all 0.25s ease !important;
}

.property-form :deep(.el-input__wrapper:hover),
.property-form :deep(.el-textarea__inner:hover),
.property-form :deep(.el-select__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--primary-color-light) inset !important;
}

.property-form :deep(.el-input__wrapper.is-focus),
.property-form :deep(.el-textarea__inner:focus),
.property-form :deep(.el-select__wrapper.is-focused) {
  box-shadow: 0 0 0 1px var(--primary-color) inset !important;
}

.property-form :deep(.el-input-number .el-input__inner) {
  text-align: left;
}

.property-form :deep(.el-form-item__error) {
  padding-top: 4px;
  font-size: 12px;
  color: var(--error-color);
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: var(--spacing-lg);
  padding-top: var(--spacing-xl);
}

.submit-btn {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-color-dark) 100%) !important;
  border: none !important;
  border-radius: var(--radius-full) !important;
  padding: 0 40px !important;
  height: 48px !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  transition: all 0.3s ease !important;
}

.submit-btn:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 8px 24px rgba(22, 119, 255, 0.35) !important;
  background: linear-gradient(135deg, var(--primary-color-light) 0%, var(--primary-color) 100%) !important;
}

.cancel-btn {
  border-radius: var(--radius-full) !important;
  padding: 0 32px !important;
  height: 48px !important;
  font-size: 15px !important;
  font-weight: 500 !important;
  transition: all 0.3s ease !important;
}

.cancel-btn:hover {
  border-color: var(--primary-color) !important;
  color: var(--primary-color) !important;
  transform: translateY(-2px) !important;
}

@media (max-width: 768px) {
  .edit-property-view {
    padding: var(--spacing-md);
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-md);
  }

  .section-title h1 {
    font-size: 20px;
  }

  .form-card {
    padding: var(--spacing-lg);
  }

  .form-actions {
    flex-direction: column;
  }

  .submit-btn,
  .cancel-btn {
    width: 100%;
  }
}
</style>
