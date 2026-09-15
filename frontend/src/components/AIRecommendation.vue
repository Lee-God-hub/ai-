<template>
  <div class="ai-recommendation">
    <el-card :loading="loading">
      <template #header>
        <div class="header-content">
          <div class="title-section">
            <el-icon :size="20" color="#409EFF"><TrendCharts /></el-icon>
            <span class="title">AI智能推荐</span>
            <el-tag type="success" size="small">个性化推荐</el-tag>
          </div>
        </div>
      </template>

      <!-- 加载状态 -->
      <div class="recommendation-content">
        <!-- 空状态 -->
        <el-empty
          v-if="recommendations.length === 0 && !loading"
          description="暂无推荐房源"
          :image-size="120"
        >
          <template #description>
            <p>暂无推荐房源</p>
            <p class="empty-tip">浏览更多房源后，AI将为您推荐更合适的房源</p>
          </template>
        </el-empty>

        <!-- 推荐房源列表 -->
        <div v-else class="properties-grid">
          <div
            v-for="item in recommendations"
            :key="item.property.id"
            class="property-card"
            @click="goToDetail(item.property.id!)"
          >
            <!-- 房源图片 -->
            <div class="property-image">
              <el-image
                :src="getFirstImage(item.property.images)"
                fit="cover"
                lazy
              >
                <template #error>
                  <div class="image-slot">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              
              <!-- 推荐标签 -->
              <div class="recommend-badge">
                <el-icon><Star /></el-icon>
                <span>AI推荐</span>
              </div>
              
              
            </div>

            <!-- 房源信息 -->
            <div class="property-info">
              <h3 class="property-title">{{ item.property.title }}</h3>
              
              <div class="property-price">
                <span class="price">¥{{ formatPrice(item.property.price, item.property.transactionType) }}</span>
                <span class="price-unit">{{ item.property.transactionType !== 1 ? '/月' : '' }}</span>
              </div>
              
              <div class="property-details">
                <el-tag size="small" type="info">{{ item.property.area }}㎡</el-tag>
                <el-tag size="small" type="info" v-if="item.property.bedrooms">
                  {{ item.property.bedrooms }}室
                </el-tag>
                <el-tag size="small" type="info" v-if="item.property.bathrooms">
                  {{ item.property.bathrooms }}卫
                </el-tag>
              </div>
              
              <div class="property-address">
                <el-icon><Location /></el-icon>
                <span>{{ item.property.city }} {{ item.property.district }}</span>
              </div>

              <!-- 推荐理由 -->
              <div class="recommend-reason">
                <el-icon><InfoFilled /></el-icon>
                <span>{{ item.recommendReason }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { aiApi } from '@/api/ai'
import type { RecommendationDTO } from '@/types/ai'
import {
  TrendCharts,
  Picture,
  Star,
  Location,
  InfoFilled
} from '@element-plus/icons-vue'

// Props
interface Props {
  limit?: number
}

const props = withDefaults(defineProps<Props>(), {
  limit: 8
})

const router = useRouter()

// 推荐列表
const recommendations = ref<RecommendationDTO[]>([])

// 加载状态
const loading = ref(false)

/**
 * 加载推荐房源
 */
const loadRecommendations = async () => {
  loading.value = true
  
  try {
    const response = await aiApi.getRecommendations(props.limit)
    
    if (response.code === 200 && response.data) {
      recommendations.value = response.data
      
      if (recommendations.value.length === 0) {
        ElMessage.info('暂无推荐房源，请先浏览一些房源')
      }
    } else {
      ElMessage.error(response.message || '加载推荐失败')
    }
  } catch (error: any) {
    console.error('加载推荐失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 跳转到房源详情
 */
const goToDetail = async (id: number) => {
  // 记录点击行为
  try {
    await aiApi.recordRecommendClick({
      propertyId: id,
      source: 'recommendation'
    })
  } catch (error) {
    console.error('记录点击失败:', error)
  }
  
  // 跳转到详情页
  router.push(`/property-detail/${id}`)
}

/**
 * 获取第一张图片
 */
const getFirstImage = (images?: string): string => {
  if (!images) return 'https://placehold.co/400x300/e5e7eb/0a0a0a?text=暂无图片'
  const imageList = images.split(',')
  return imageList[0] || 'https://placehold.co/400x300/e5e7eb/0a0a0a?text=暂无图片'
}

/**
 * 格式化价格
 */
const formatPrice = (price: number, transactionType?: number): string => {
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

// 组件挂载时加载推荐
onMounted(() => {
  loadRecommendations()
})

// 暴露方法供父组件调用
defineExpose({
  loadRecommendations
})
</script>

<style scoped>
.ai-recommendation {
  margin-bottom: 32px;
}

.ai-recommendation :deep(.el-card) {
  border-radius: var(--hw-radius-lg);
}

/* 头部样式 */
.header-content {
  display: flex;
  align-items: center;
}

.title-section {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-section :deep(.el-icon) {
  color: var(--hw-red) !important;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: var(--hw-black);
}

/* 内容区域 */
.recommendation-content {
  min-height: 280px;
}

.empty-tip {
  font-size: 13px;
  color: var(--hw-gray-500);
  margin-top: 8px;
}

/* 房源网格 */
.properties-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}

/* 房源卡片 */
.property-card {
  background: white;
  border-radius: var(--hw-radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: var(--hw-transition);
  box-shadow: var(--hw-shadow);
  border: 2px solid transparent;
}

.property-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--hw-shadow-hover);
  border-color: var(--hw-red);
}

/* 房源图片 */
.property-image {
  position: relative;
  width: 100%;
  height: 180px;
  overflow: hidden;
}

.property-image :deep(.el-image) {
  width: 100%;
  height: 100%;
}

.property-image :deep(.el-image img) {
  transition: transform 0.4s;
}

.property-card:hover .property-image :deep(.el-image img) {
  transform: scale(1.08);
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background-color: var(--hw-gray-100);
  color: var(--hw-gray-300);
  font-size: 40px;
}

/* 推荐标签 */
.recommend-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  background: var(--hw-red);
  color: white;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 房源信息 */
.property-info {
  padding: 14px 16px;
}

.property-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--hw-black);
  margin: 0 0 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.property-price {
  margin-bottom: 10px;
}

.property-price .price {
  font-size: 22px;
  font-weight: 700;
  color: var(--hw-red);
}

.property-price .price-unit {
  font-size: 13px;
  color: var(--hw-gray-500);
  margin-left: 2px;
}

.property-details {
  display: flex;
  gap: 6px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.property-details :deep(.el-tag) {
  border-radius: 4px;
}

.property-address {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--hw-gray-500);
  margin-bottom: 10px;
}

/* 推荐理由 */
.recommend-reason {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  padding: 10px;
  background: #FFF5F5;
  border-radius: 8px;
  font-size: 12px;
  color: var(--hw-gray-700);
  line-height: 1.5;
}

.recommend-reason .el-icon {
  color: var(--hw-red);
  margin-top: 2px;
  flex-shrink: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .properties-grid {
    grid-template-columns: 1fr;
  }
}
</style>
