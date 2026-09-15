<template>
  <div class="favorites-view page-animate">
    <div class="page-header">
      <div class="page-title">
        <span class="title-bar"></span>
        <h1>我的收藏</h1>
      </div>
      <el-tag class="total-tag">共 {{ total }} 个收藏</el-tag>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-card">
      <div class="filter-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索房源标题或地址"
          clearable
          class="search-input"
          @clear="loadFavorites"
          @keyup.enter="loadFavorites"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" class="search-btn" @click="loadFavorites">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        <el-button class="clear-btn" @click="handleClearAll" :disabled="favorites.length === 0">
          <el-icon><Delete /></el-icon>
          清空收藏
        </el-button>
      </div>
    </div>

    <!-- 收藏列表 -->
    <div v-loading="loading" class="favorites-list">
      <div v-if="favorites.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无收藏房源">
          <el-button type="primary" class="empty-btn" @click="goToPropertyList">去浏览房源</el-button>
        </el-empty>
      </div>

      <div v-else class="property-grid">
        <div
          v-for="item in favorites"
          :key="item.id"
          class="property-card"
        >
          <!-- 房源图片 -->
          <div class="property-image" @click="goToDetail(item.property.id)">
            <el-image
              :src="getFirstImage(item.property.images)"
              fit="cover"
              lazy
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon :size="32"><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="card-badge type-badge">
              {{ getPropertyTypeText(item.property.propertyType) }}
            </div>
            <div class="card-badge trans-badge">
              {{ getTransactionTypeText(item.property.transactionType) }}
            </div>
            <div class="favorite-badge" @click.stop="handleRemove(item)">
              <el-icon><Star /></el-icon>
            </div>
          </div>

          <!-- 房源信息 -->
          <div class="property-body">
            <h3 class="property-name" @click="goToDetail(item.property.id)">
              {{ item.property.title }}
            </h3>
            
            <div class="property-price-row">
              <span class="price-value">¥{{ formatPrice(item.property.price, item.property.transactionType) }}</span>
              <span class="price-unit">{{ item.property.transactionType !== 1 ? '/月' : '' }}</span>
            </div>

            <div class="property-tags">
              <span class="ptag">{{ item.property.area }}㎡</span>
              <span class="ptag">{{ item.property.rooms || item.property.bedrooms || 0 }}室{{ item.property.halls || 1 }}厅</span>
              <span v-if="item.property.bathrooms" class="ptag">{{ item.property.bathrooms }}卫</span>
              <span v-if="item.property.source && item.property.source.includes('爬虫')" class="ptag source-tag">
                {{ item.property.source.replace('爬虫-', '') }}
              </span>
            </div>

            <div class="property-location">
              <el-icon :size="14"><Location /></el-icon>
              <span>{{ item.property.city }} {{ item.property.district }}</span>
            </div>

            <div class="property-time">
              <el-icon :size="14"><Clock /></el-icon>
              <span>收藏于 {{ formatDate(item.createTime) }}</span>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="property-actions">
            <el-button type="primary" class="action-btn detail-btn" @click="goToDetail(item.property.id)">
              <el-icon><View /></el-icon>
              查看详情
            </el-button>
            <el-button class="action-btn remove-btn" @click="handleRemove(item)">
              <el-icon><Delete /></el-icon>
              取消收藏
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <el-pagination
        v-if="total > 0"
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[6, 12, 24, 48]"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @size-change="loadFavorites"
        @current-change="loadFavorites"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { favoriteApi } from '@/api/favorite'
import { PROPERTY_TYPE_TEXTS, TRANSACTION_TYPE_TEXTS } from '@/utils/constants'

const router = useRouter()

// 数据
const loading = ref(false)
const favorites = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(12)
const searchKeyword = ref('')

/**
 * 加载收藏列表
 */
const loadFavorites = async () => {
  try {
    loading.value = true
    let list: any[] = []
    
    try {
      const response = await favoriteApi.getMyList()
      if (response.code === 200 && response.data && response.data.length > 0) {
        list = response.data
      }
    } catch (apiError) {
      console.log('API请求失败，使用爬虫收藏数据:', apiError)
    }
    

    
    // 搜索过滤
    if (searchKeyword.value) {
      const keyword = searchKeyword.value.toLowerCase()
      list = list.filter((item: any) => 
        (item.property.title && item.property.title.toLowerCase().includes(keyword)) ||
        (item.property.address && item.property.address.toLowerCase().includes(keyword)) ||
        (item.property.city && item.property.city.toLowerCase().includes(keyword))
      )
    }
    
    total.value = list.length
    
    // 分页
    const start = (pageNum.value - 1) * pageSize.value
    const end = start + pageSize.value
    favorites.value = list.slice(start, end)
  } catch (error: any) {
    console.error('加载收藏列表失败:', error)
    ElMessage.error(error.message || '加载收藏列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 取消收藏
 */
const handleRemove = async (item: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消收藏"${item.property.title}"吗？`,
      '取消收藏',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await favoriteApi.remove(item.propertyId)
    ElMessage.success('取消收藏成功')
    loadFavorites()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('取消收藏失败:', error)
      ElMessage.error(error.message || '取消收藏失败')
    }
  }
}

/**
 * 清空所有收藏
 */
const handleClearAll = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空所有收藏吗？此操作不可恢复！',
      '清空收藏',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 批量删除
    for (const item of favorites.value) {
      await favoriteApi.remove(item.propertyId)
    }
    
    ElMessage.success('清空收藏成功')
    loadFavorites()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('清空收藏失败:', error)
      ElMessage.error(error.message || '清空收藏失败')
    }
  }
}

/**
 * 跳转到房源详情
 */
const goToDetail = (propertyId: number) => {
  router.push(`/property-detail/${propertyId}`)
}

/**
 * 跳转到房源列表
 */
const goToPropertyList = () => {
  router.push('/properties')
}

/**
 * 获取第一张图片
 */
const getFirstImage = (images: string) => {
  if (!images) return 'https://placehold.co/400x300/e5e7eb/0a0a0a?text=暂无图片'
  const imageList = images.split(',')
  return imageList[0] || 'https://placehold.co/400x300/e5e7eb/0a0a0a?text=暂无图片'
}

/**
 * 格式化价格
 */
const formatPrice = (price: number, transactionType?: number) => {
  if (!price) return '0'
  
  let displayPrice = price
  
  if (transactionType === 0 && price > 10000) {
    displayPrice = Math.round(price / 12)
  }
  
  if (displayPrice >= 10000) {
    return (displayPrice / 10000).toFixed(1) + '万'
  }
  return Math.round(displayPrice).toLocaleString()
}

/**
 * 格式化日期
 */
const formatDate = (date: string) => {
  if (!date) return '-'
  const d = new Date(date)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  
  // 小于1分钟
  if (diff < 60000) {
    return '刚刚'
  }
  // 小于1小时
  if (diff < 3600000) {
    return Math.floor(diff / 60000) + '分钟前'
  }
  // 小于1天
  if (diff < 86400000) {
    return Math.floor(diff / 3600000) + '小时前'
  }
  // 小于7天
  if (diff < 604800000) {
    return Math.floor(diff / 86400000) + '天前'
  }
  
  return d.toLocaleDateString('zh-CN')
}

/**
 * 获取房源类型文本
 */
const getPropertyTypeText = (type: number) => {
  return PROPERTY_TYPE_TEXTS[type] || '未知'
}

/**
 * 获取交易类型文本
 */
const getTransactionTypeText = (type: number) => {
  return TRANSACTION_TYPE_TEXTS[type] || '出售'
}

// 页面加载时获取数据
onMounted(() => {
  loadFavorites()
})
</script>

<style scoped>
.favorites-view {
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

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title h1 {
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

.total-tag {
  background: var(--primary-color-bg) !important;
  color: var(--primary-color) !important;
  border: none !important;
  font-weight: 500;
  padding: 0 var(--spacing-md) !important;
  height: 28px !important;
  line-height: 26px !important;
}

.filter-card {
  background: var(--bg-color);
  border-radius: var(--radius-xl);
  padding: var(--spacing-lg) var(--spacing-xl);
  box-shadow: var(--shadow-base);
  margin-bottom: var(--spacing-xl);
  border: 1px solid var(--border-color-light);
}

.filter-bar {
  display: flex;
  gap: var(--spacing-md);
  flex-wrap: wrap;
  align-items: center;
}

.search-input {
  width: 320px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: var(--radius-full) !important;
  padding: 0 var(--spacing-lg);
  box-shadow: 0 0 0 1px var(--border-color) inset;
}

.search-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--primary-color-light) inset !important;
}

.search-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--primary-color) inset !important;
}

.search-btn {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-color-light) 100%) !important;
  border: none !important;
  border-radius: var(--radius-full) !important;
  padding: 0 var(--spacing-xl) !important;
  font-weight: 600;
  transition: all 0.3s ease !important;
}

.search-btn:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 4px 16px rgba(22, 119, 255, 0.35) !important;
}

.clear-btn {
  border-radius: var(--radius-full) !important;
  padding: 0 var(--spacing-lg) !important;
  transition: all 0.3s ease !important;
}

.clear-btn:hover {
  color: var(--error-color) !important;
  border-color: var(--error-color) !important;
  background: var(--error-color-bg) !important;
}

.favorites-list {
  min-height: 400px;
}

.empty-state {
  background: var(--bg-color);
  border-radius: var(--radius-xl);
  padding: var(--spacing-3xl);
  box-shadow: var(--shadow-base);
  border: 1px solid var(--border-color-light);
}

.empty-btn {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--accent-color) 100%) !important;
  border: none !important;
  border-radius: var(--radius-full) !important;
  padding: 0 var(--spacing-xl) !important;
  font-weight: 600;
  transition: all 0.3s ease !important;
}

.empty-btn:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 4px 16px rgba(22, 119, 255, 0.3) !important;
}

.property-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: var(--spacing-lg);
}

.property-card {
  background: var(--bg-color);
  border-radius: var(--radius-xl);
  overflow: hidden;
  cursor: pointer;
  transition: var(--transition-base);
  box-shadow: var(--shadow-base);
  border: 1px solid var(--border-color-light);
  display: flex;
  flex-direction: column;
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

.favorite-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 36px;
  height: 36px;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--accent-color);
  cursor: pointer;
  transition: var(--transition-base);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.favorite-badge:hover {
  background: var(--accent-color);
  color: white;
  transform: scale(1.1);
}

.property-body {
  padding: var(--spacing-lg);
  flex: 1;
  display: flex;
  flex-direction: column;
}

.property-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-color-primary);
  margin: 0 0 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.2s;
  cursor: pointer;
}

.property-card:hover .property-name {
  color: var(--primary-color);
}

.property-price-row {
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

.source-tag {
  background: var(--success-color-bg);
  color: var(--success-color);
}

.property-location {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-color-tertiary);
  margin-bottom: 6px;
}

.property-location .el-icon {
  color: var(--primary-color);
}

.property-time {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-color-tertiary);
  margin-top: auto;
  padding-top: 8px;
}

.property-time .el-icon {
  color: var(--text-color-tertiary);
}

.property-actions {
  display: flex;
  gap: var(--spacing-sm);
  padding: 0 var(--spacing-lg) var(--spacing-lg);
}

.action-btn {
  flex: 1;
  border-radius: var(--radius-lg) !important;
  font-weight: 500;
  transition: all 0.3s ease !important;
}

.detail-btn {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-color-light) 100%) !important;
  border: none !important;
}

.detail-btn:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3) !important;
}

.remove-btn {
  color: var(--error-color) !important;
  border-color: var(--error-color) !important;
  background: transparent !important;
}

.remove-btn:hover {
  background: var(--error-color) !important;
  color: white !important;
  transform: translateY(-2px) !important;
  box-shadow: 0 4px 12px rgba(255, 77, 79, 0.3) !important;
}

.pagination-wrap {
  margin-top: var(--spacing-2xl);
  display: flex;
  justify-content: center;
  background: var(--bg-color);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-base);
  border: 1px solid var(--border-color-light);
}

.pagination {
  padding: var(--spacing-lg) 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .favorites-view {
    padding: var(--spacing-md);
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-md);
  }

  .page-title h1 {
    font-size: 20px;
  }

  .search-input {
    width: 100%;
  }

  .property-grid {
    grid-template-columns: 1fr;
  }

  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-bar .el-button {
    width: 100%;
  }
}
</style>
