<template>
  <div class="property-list-view page-animate">
    <div class="page-header">
      <div class="page-title">
        <span class="title-bar"></span>
        <h2>房源列表</h2>
      </div>
      <el-button type="primary" @click="loadData" :icon="Refresh">
        刷新
      </el-button>
    </div>

    <el-card class="filter-card animate-fade-in-up" shadow="never">
      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="城市">
          <el-input v-model="filters.city" placeholder="请输入城市" clearable class="filter-input" />
        </el-form-item>
        <el-form-item label="区域">
          <el-input v-model="filters.district" placeholder="请输入区域" clearable class="filter-input" />
        </el-form-item>
        <el-form-item label="房源类型">
          <el-select
            v-model="filters.propertyType"
            placeholder="请选择"
            clearable
            class="filter-select">
            <el-option v-for="item in PROPERTY_TYPE_LIST" :key="item.value" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="交易类型">
          <el-select
            v-model="filters.transactionType"
            placeholder="请选择"
            clearable
            class="filter-select">
            <el-option v-for="item in TRANSACTION_TYPE_LIST" :key="item.value" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item class="filter-actions">
          <el-button type="primary" @click="loadData" :icon="Search">搜索</el-button>
          <el-button @click="resetFilters" :icon="RefreshRight">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div v-loading="loading" class="properties-grid">
      <div
        v-for="(item, index) in properties"
        :key="item.id"
        class="property-card animate-fade-in-up"
        :style="{ animationDelay: `${index * 0.08 + 0.15}s` }"
        @click="viewDetail(item.id!)"
      >
        <div class="property-image">
          <el-image :src="getFirstImage(item.images || '')" fit="cover" lazy>
            <template #error>
              <div class="image-placeholder">
                <el-icon :size="40"><Picture /></el-icon>
              </div>
            </template>
          </el-image>
          <div class="card-badge type-badge">
            {{ getPropertyTypeText(item.propertyType ?? 0) }}
          </div>
          <div class="card-badge trans-badge">
            {{ getTransactionTypeText(item.transactionType ?? 0) }}
          </div>
        </div>
        <div class="property-body">
          <h3 class="property-title">{{ item.title }}</h3>
          <div class="property-price">
            <span class="price-value">{{ formatPrice(item.price, item.transactionType) }}</span>
            <span class="price-unit">{{ item.transactionType !== 1 ? '/月' : '' }}</span>
          </div>
          <div class="property-tags">
            <span class="ptag">{{ item.area }}㎡</span>
            <span class="ptag">{{ item.bedrooms ?? 0 }}室{{ item.bathrooms ?? 0 }}卫</span>
          </div>
          <div class="property-location">
            <el-icon><Location /></el-icon>
            <span>{{ item.city }} · {{ item.district }}</span>
          </div>
        </div>
      </div>
    </div>

    <el-empty v-if="properties.length === 0 && !loading" description="暂无房源" class="empty-state" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture, Search, Refresh, RefreshRight, Location } from '@element-plus/icons-vue'
import { propertyApi } from '@/api/property'
import type { Property } from '@/types'
import { PROPERTY_TYPE_LIST, TRANSACTION_TYPE_LIST, PROPERTY_TYPE_TEXTS, TRANSACTION_TYPE_TEXTS } from '@/utils/constants'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const properties = ref<Property[]>([])

const getFirstImage = (images: string) => {
  if (!images) return 'https://placehold.co/400x300/f5f5f5/999?text=No+Image'
  return images.split(',')[0] || 'https://placehold.co/400x300/f5f5f5/999?text=No+Image'
}

const formatPrice = (price: number, transactionType?: number) => {
  if (!price) return '0'
  
  let displayPrice = price
  
  if (transactionType === 0 && price > 10000) {
    displayPrice = Math.round(price / 12)
  }
  
  if (displayPrice >= 10000) return (displayPrice / 10000).toFixed(1) + '万'
  return Math.round(displayPrice).toLocaleString()
}

const getPropertyTypeText = (type: number) => {
  return PROPERTY_TYPE_TEXTS[type] || '住宅'
}

const getTransactionTypeText = (type: number) => {
  return TRANSACTION_TYPE_TEXTS[type] || '出售'
}

const filters = reactive({
  city: '',
  district: '',
  propertyType: null as number | null,
  transactionType: null as number | null
})

// 从路由参数初始化筛选条件
const initFiltersFromQuery = () => {
  if (route.query.city) filters.city = route.query.city as string
  if (route.query.district) filters.district = route.query.district as string
  if (route.query.propertyType !== undefined) {
    const val = Number(route.query.propertyType)
    if (!Number.isNaN(val)) filters.propertyType = val
  }
  if (route.query.transactionType !== undefined) {
    const val = Number(route.query.transactionType)
    if (!Number.isNaN(val)) filters.transactionType = val
  }
}

const loadData = async () => {
  try {
    loading.value = true
    const params: any = {
      pageNum: 1,
      pageSize: 100
    }
    
    if (filters.city) params.city = filters.city
    if (filters.district) params.district = filters.district
    if (filters.propertyType !== null) params.propertyType = filters.propertyType
    if (filters.transactionType !== null) params.transactionType = filters.transactionType
    
    const res = await propertyApi.getPropertyList(params)
    if (res.code === 200 && res.data) {
      properties.value = (res.data as any).records || res.data || []
    }
  } catch (error) {
    console.error('获取房源列表失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const viewDetail = (id: number) => {
  router.push(`/property-detail/${id}`)
}

const resetFilters = () => {
  filters.city = ''
  filters.district = ''
  filters.propertyType = null
  filters.transactionType = null
  loadData()
}

onMounted(() => {
  initFiltersFromQuery()
  loadData()
})
</script>

<style scoped>
.property-list-view {
  padding: var(--spacing-lg);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-xl);
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title h2 {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-color-primary);
  margin: 0;
  letter-spacing: 0.5px;
}

.title-bar {
  width: 5px;
  height: 26px;
  background: linear-gradient(180deg, var(--primary-color) 0%, var(--accent-color) 100%);
  border-radius: 3px;
}

.filter-card {
  border-radius: var(--radius-xl);
  margin-bottom: var(--spacing-xl);
  border: 1px solid var(--border-color-light);
  box-shadow: var(--shadow-sm);
}

.filter-form {
  margin: 0;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: var(--spacing-lg);
}

.filter-input {
  width: 160px;
}

.filter-select {
  width: 160px;
}

.filter-actions {
  margin-right: 0 !important;
}

.properties-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--spacing-lg);
  min-height: 300px;
}

.property-card {
  background: var(--bg-color);
  border-radius: var(--radius-xl);
  overflow: hidden;
  cursor: pointer;
  transition: var(--transition-base);
  box-shadow: var(--shadow-base);
  border: 1px solid var(--border-color-light);
}

.property-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-card-hover);
  border-color: var(--primary-color-bg);
}

.property-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.property-image :deep(.el-image) {
  width: 100%;
  height: 100%;
}

.property-image :deep(.el-image img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.property-card:hover .property-image :deep(.el-image img) {
  transform: scale(1.1);
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-color-light);
  color: var(--text-color-tertiary);
}

.card-badge {
  position: absolute;
  top: 12px;
  padding: 4px 12px;
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: 600;
  color: white;
  backdrop-filter: blur(10px);
}

.type-badge {
  left: 12px;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-color-light) 100%);
  box-shadow: 0 2px 8px rgba(22, 119, 255, 0.3);
}

.trans-badge {
  right: 12px;
  background: linear-gradient(135deg, var(--accent-color) 0%, var(--accent-color-light) 100%);
  box-shadow: 0 2px 8px rgba(255, 125, 0, 0.3);
}

.property-body {
  padding: 16px 18px 18px;
}

.property-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-color-primary);
  margin: 0 0 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.2s;
}

.property-card:hover .property-title {
  color: var(--primary-color);
}

.property-price {
  margin-bottom: 12px;
}

.price-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--accent-color);
}

.price-unit {
  font-size: 13px;
  color: var(--text-color-tertiary);
  margin-left: 4px;
  font-weight: 400;
}

.property-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.ptag {
  padding: 3px 10px;
  background: var(--bg-color-light);
  border-radius: var(--radius-sm);
  font-size: 12px;
  color: var(--text-color-secondary);
  font-weight: 500;
}

.property-location {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-color-tertiary);
}

.property-location .el-icon {
  color: var(--primary-color);
}

.empty-state {
  padding: var(--spacing-2xl);
}

@media (max-width: 768px) {
  .property-list-view {
    padding: var(--spacing-md);
  }

  .properties-grid {
    grid-template-columns: 1fr;
  }

  .filter-input,
  .filter-select {
    width: 100%;
  }
}
</style>
