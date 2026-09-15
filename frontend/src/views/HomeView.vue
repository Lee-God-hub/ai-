<template>
  <div class="home-view page-animate">
    <!-- 顶部 Hero 区域 -->
    <div class="hero-section">
      <div class="hero-bg"></div>
      <div class="hero-content">
        <h1 class="hero-title animate-fade-in-up">智慧房产 · 美好生活</h1>
        <p class="hero-subtitle animate-fade-in-up animate-delay-1">AI赋能，让找房更简单、更高效</p>
        <div class="hero-search animate-fade-in-up animate-delay-2">
          <el-input
            v-model="searchForm.city"
            placeholder="输入城市或区域搜索房源"
            size="large"
            class="hero-input"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
            <template #append>
              <el-button type="primary" class="search-btn btn-shimmer btn-press" @click="handleSearch">
                搜索房源
              </el-button>
            </template>
          </el-input>
        </div>
        <!-- 热门城市标签 -->
        <div class="hot-tags animate-fade-in-up animate-delay-3">
          <span class="hot-label">热门：</span>
          <button
            v-for="city in hotCities.slice(0, 8)"
            :key="city"
            class="city-tag"
            @click="selectCity(city)"
          >
            {{ city }}
          </button>
        </div>
      </div>
    </div>

    <!-- 数据统计 -->
    <div class="stats-section">
      <div class="stats-grid">
        <div class="stat-card animate-fade-in-up animate-delay-1">
          <div class="stat-number">10,000<span class="stat-plus">+</span></div>
          <div class="stat-label">优质房源</div>
        </div>
        <div class="stat-card animate-fade-in-up animate-delay-2">
          <div class="stat-number">5,000<span class="stat-plus">+</span></div>
          <div class="stat-label">注册用户</div>
        </div>
        <div class="stat-card animate-fade-in-up animate-delay-3">
          <div class="stat-number">98<span class="stat-unit">%</span></div>
          <div class="stat-label">满意度</div>
        </div>
        <div class="stat-card animate-fade-in-up animate-delay-4">
          <div class="stat-number">16<span class="stat-plus">+</span></div>
          <div class="stat-label">覆盖城市</div>
        </div>
      </div>
    </div>

    <!-- AI智能推荐（仅普通用户可见） -->
    <AIRecommendation v-if="userRole === 0" :limit="8" />

    <!-- 热门房源 -->
    <div class="section">
      <div class="section-header">
        <div class="section-title">
          <span class="title-bar"></span>
          <h2>热门房源</h2>
        </div>
        <el-button type="primary" link @click="goToProperties" class="more-link">
          查看全部
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>

      <div v-loading="loading" class="properties-grid">
        <el-empty v-if="hotProperties.length === 0 && !loading" description="暂无热门房源" />

        <div
          v-for="(property, index) in hotProperties"
          :key="property.id"
          class="property-card animate-fade-in-up"
          :style="{ animationDelay: `${index * 0.08 + 0.1}s` }"
          @click="goToDetail(property.id)"
        >
          <div class="property-image">
            <el-image :src="getFirstImage(property.images)" fit="cover" lazy>
              <template #error>
                <div class="image-placeholder">
                  <el-icon :size="32"><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="card-badge type-badge">
              {{ getPropertyTypeText(property.propertyType) }}
            </div>
            <div class="card-badge trans-badge">
              {{ getTransactionTypeText(property.transactionType) }}
            </div>
          </div>
          <div class="property-body">
            <h3 class="property-name">{{ property.title }}</h3>
            <div class="property-price-row">
              <span class="price-value">¥{{ formatPrice(property.price, property.transactionType) }}</span>
              <span class="price-unit">{{ property.transactionType !== 1 ? '/月' : '' }}</span>
            </div>
            <div class="property-tags">
              <span class="ptag">{{ property.area }}㎡</span>
              <span v-if="property.bedrooms" class="ptag">{{ property.bedrooms }}室</span>
              <span v-if="property.bathrooms" class="ptag">{{ property.bathrooms }}卫</span>
            </div>
            <div class="property-location">
              <el-icon :size="14"><Location /></el-icon>
              <span>{{ property.city }} {{ property.district }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 平台优势 -->
    <div class="section features-section">
      <div class="section-header">
        <div class="section-title">
          <span class="title-bar"></span>
          <h2>为什么选择我们</h2>
        </div>
      </div>
      <div class="features-grid">
        <div
          v-for="(feature, index) in features"
          :key="feature.title"
          class="feature-card animate-fade-in-up"
          :style="{ animationDelay: `${index * 0.15 + 0.2}s` }"
        >
          <div class="feature-icon-wrap" :style="{ background: feature.color }">
            <component :is="feature.icon" />
          </div>
          <h3>{{ feature.title }}</h3>
          <p>{{ feature.description }}</p>
        </div>
      </div>
    </div>

    <!-- AI助手悬浮窗 -->
    <AIAssistant />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { propertyApi } from '@/api/property'
import { useUserStore } from '@/stores/user'
import { PROPERTY_TYPE_TEXTS, TRANSACTION_TYPE_TEXTS } from '@/utils/constants'
import AIRecommendation from '@/components/AIRecommendation.vue'
import AIAssistant from '@/components/AIAssistant.vue'
import {
  Location,
  Search,
  ArrowRight,
  Picture,
  Lock,
  Timer,
  Service as ServiceIcon,
  CircleCheck
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const userRole = computed(() => userStore.userRole)

const hotCities = ref([
  '北京', '上海', '广州', '深圳', '杭州', '成都', '重庆', '武汉'
])

const searchForm = reactive({
  city: '',
  district: '',
  propertyType: null as number | null,
  transactionType: null as number | null
})

const loading = ref(false)
const hotProperties = ref<any[]>([])

const features = ref([
  {
    icon: Lock,
    title: '安全保障',
    description: '实名认证体系，每一笔交易都安全有保障',
    color: 'linear-gradient(135deg, #1677ff 0%, #4096ff 100%)'
  },
  {
    icon: Timer,
    title: '极速响应',
    description: 'AI即时问答，24小时智能客服在线',
    color: 'linear-gradient(135deg, #ff7d00 0%, #ff9a2e 100%)'
  },
  {
    icon: ServiceIcon,
    title: '智能推荐',
    description: 'AI算法精准匹配，为您推荐最合适的房源',
    color: 'linear-gradient(135deg, #722ed1 0%, #9254de 100%)'
  },
  {
    icon: CircleCheck,
    title: '真实房源',
    description: '严格审核机制，杜绝虚假信息',
    color: 'linear-gradient(135deg, #52c41a 0%, #73d13d 100%)'
  }
])

const loadHotProperties = async () => {
  try {
    loading.value = true
    const response = await propertyApi.getPropertyList({ pageNum: 1, pageSize: 8 })
    if (response.code === 200 && response.data) {
      hotProperties.value = response.data.sort((a: any, b: any) =>
        (b.viewCount || 0) - (a.viewCount || 0)
      ).slice(0, 8)
    }
  } catch (error: any) {
    console.error('加载热门房源失败:', error)
  } finally {
    loading.value = false
  }
}

const selectCity = (city: string) => {
  searchForm.city = city
  handleSearch()
}

const handleSearch = () => {
  const query: any = {}
  if (searchForm.city) query.city = searchForm.city
  if (searchForm.district) query.district = searchForm.district
  if (searchForm.propertyType !== null) query.propertyType = searchForm.propertyType
  if (searchForm.transactionType !== null) query.transactionType = searchForm.transactionType
  router.push({ path: '/property', query }).catch(() => {})
}

const goToProperties = () => {
  router.push('/property').catch(() => {})
}

const goToDetail = (id: number) => {
  router.push(`/property-detail/${id}`).catch(() => {})
}

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

onMounted(() => {
  loadHotProperties()
})
</script>

<style scoped>
.home-view {
  min-height: 100%;
}

/* Hero 区域 */
.hero-section {
  position: relative;
  padding: 70px 32px 56px;
  border-radius: var(--radius-2xl);
  overflow: hidden;
  margin-bottom: var(--spacing-2xl);
}

.hero-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #1677ff 0%, #0958d9 40%, #003eb3 100%);
  opacity: 1;
}

.hero-bg::before {
  content: '';
  position: absolute;
  top: -100px;
  right: -100px;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(255, 125, 0, 0.2) 0%, transparent 70%);
  border-radius: 50%;
}

.hero-bg::after {
  content: '';
  position: absolute;
  bottom: -80px;
  left: 50px;
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(64, 150, 255, 0.3) 0%, transparent 70%);
  border-radius: 50%;
}

.hero-content {
  position: relative;
  z-index: 1;
  text-align: center;
  max-width: 720px;
  margin: 0 auto;
}

.hero-title {
  font-size: 40px;
  font-weight: 700;
  color: white;
  margin: 0 0 16px;
  letter-spacing: 2px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.hero-subtitle {
  font-size: 17px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0 0 36px;
  font-weight: 400;
}

.hero-search {
  max-width: 600px;
  margin: 0 auto 24px;
}

.hero-search :deep(.el-input-group) {
  display: flex;
  border-radius: var(--radius-full);
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  gap: 0;
}

.hero-search :deep(.el-input__wrapper) {
  border-radius: var(--radius-full) 0 0 var(--radius-full);
  padding: 0 20px;
  height: 52px;
  border: none;
  box-shadow: none;
  background: white;
}

.hero-search :deep(.el-input__inner) {
  height: 52px;
  line-height: 52px;
  font-size: 15px;
}

.hero-search :deep(.el-input__prefix) {
  color: var(--text-color-tertiary);
  font-size: 18px;
}

.hero-search :deep(.el-input-group__append) {
  border-radius: 0 var(--radius-full) var(--radius-full) 0;
  padding: 0;
  border: none;
  box-shadow: none;
  background: transparent;
  height: 52px;
}

.search-btn {
  background: linear-gradient(135deg, #ff7d00 0%, #ff9a2e 100%) !important;
  border-color: transparent !important;
  color: white !important;
  border-radius: 0 var(--radius-full) var(--radius-full) 0 !important;
  padding: 0 36px !important;
  font-weight: 600;
  font-size: 15px;
  height: 52px;
  line-height: 52px;
  border: none !important;
  margin: 0 !important;
  transition: all 0.3s ease;
  letter-spacing: 1px;
}

.search-btn:hover {
  background: linear-gradient(135deg, #ff9a2e 0%, #ffb86b 100%) !important;
  box-shadow: 0 4px 20px rgba(255, 125, 0, 0.4) !important;
  transform: none;
}

.search-btn:active {
  box-shadow: 0 2px 10px rgba(255, 125, 0, 0.3) !important;
}

.hot-tags {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  flex-wrap: wrap;
}

.hot-label {
  color: rgba(255, 255, 255, 0.75);
  font-size: 13px;
}

.city-tag {
  padding: 6px 16px;
  border: 1px solid rgba(255, 255, 255, 0.35);
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.12);
  color: white;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.25s ease;
  backdrop-filter: blur(10px);
}

.city-tag:hover {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.7);
  transform: translateY(-1px);
}

/* 统计数据 */
.stats-section {
  margin-bottom: var(--spacing-2xl);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-lg);
}

.stat-card {
  background: var(--bg-color);
  border-radius: var(--radius-xl);
  padding: var(--spacing-2xl) var(--spacing-lg);
  text-align: center;
  box-shadow: var(--shadow-base);
  transition: var(--transition-base);
  border: 1px solid var(--border-color-light);
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--primary-color), var(--accent-color));
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-card-hover);
  border-color: var(--primary-color-bg);
}

.stat-card:hover::before {
  transform: scaleX(1);
}

.stat-number {
  font-size: 36px;
  font-weight: 700;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--accent-color) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1.2;
}

.stat-plus, .stat-unit {
  font-size: 20px;
  font-weight: 600;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--accent-color) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stat-label {
  font-size: 14px;
  color: var(--text-color-secondary);
  margin-top: 10px;
  font-weight: 500;
}

/* 通用区块 */
.section {
  margin-bottom: var(--spacing-2xl);
}

.section-header {
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

.section-title h2 {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-color-primary);
  margin: 0;
  letter-spacing: 0.5px;
}

.title-bar {
  width: 5px;
  height: 24px;
  background: linear-gradient(180deg, var(--primary-color) 0%, var(--accent-color) 100%);
  border-radius: 3px;
}

.more-link {
  font-size: 14px;
  color: var(--primary-color) !important;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.2s ease;
}

.more-link:hover {
  color: var(--accent-color) !important;
  gap: 8px;
}

/* 房源网格 */
.properties-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--spacing-lg);
  min-height: 200px;
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

.property-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-color-primary);
  margin: 0 0 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.2s;
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

/* 平台优势 */
.features-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-lg);
}

.feature-card {
  background: var(--bg-color);
  border-radius: var(--radius-xl);
  padding: var(--spacing-2xl) var(--spacing-lg);
  text-align: center;
  box-shadow: var(--shadow-base);
  transition: var(--transition-base);
  border: 1px solid var(--border-color-light);
  position: relative;
  overflow: hidden;
}

.feature-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, var(--primary-color-bg-light) 0%, transparent 50%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-card-hover);
  border-color: var(--primary-color-bg);
}

.feature-card:hover::before {
  opacity: 1;
}

.feature-icon-wrap {
  width: 64px;
  height: 64px;
  margin: 0 auto 18px;
  border-radius: var(--radius-xl);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 28px;
  position: relative;
  z-index: 1;
  transition: transform 0.3s ease;
}

.feature-card:hover .feature-icon-wrap {
  transform: scale(1.1) rotate(5deg);
}

.feature-card h3 {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-color-primary);
  margin: 0 0 10px;
  position: relative;
  z-index: 1;
}

.feature-card p {
  font-size: 13px;
  color: var(--text-color-secondary);
  margin: 0;
  line-height: 1.6;
  position: relative;
  z-index: 1;
}

/* 响应式 */
@media (max-width: 768px) {
  .hero-title {
    font-size: 24px;
  }

  .hero-subtitle {
    font-size: 14px;
  }

  .hero-search {
    max-width: 100%;
    padding: 0 16px;
  }

  .hero-search :deep(.el-input__wrapper) {
    height: 44px;
    padding: 0 12px;
  }

  .hero-search :deep(.el-input__inner) {
    height: 44px;
    line-height: 44px;
    font-size: 14px;
  }

  .hero-search :deep(.el-input-group__append) {
    height: 44px;
  }

  .search-btn {
    height: 44px !important;
    line-height: 44px !important;
    padding: 0 20px !important;
    font-size: 14px;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .properties-grid {
    grid-template-columns: 1fr;
  }

  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
