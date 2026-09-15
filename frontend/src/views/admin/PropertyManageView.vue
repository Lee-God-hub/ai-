<template>
  <div class="property-manage-view page-animate">
    <div class="page-header">
      <div class="page-title">
        <span class="title-bar"></span>
        <h2>全量房源管理</h2>
      </div>
      <div class="header-actions">
        <el-button type="primary" class="refresh-btn" @click="loadData">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>

    <div class="stats-section">
      <div class="stat-card">
        <div class="stat-icon icon-total">
          <el-icon><OfficeBuilding /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ statsTotal }}</div>
          <div class="stat-label">房源总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon icon-online">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ onlineCount }}</div>
          <div class="stat-label">已上架</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon icon-pending">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ pendingCount }}</div>
          <div class="stat-label">待审核</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon icon-offline">
          <el-icon><CircleClose /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ offlineCount }}</div>
          <div class="stat-label">已下架</div>
        </div>
      </div>
    </div>

    <div class="content-card">
      <div class="filter-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索房源标题或地址"
          class="search-input"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="statusFilter" placeholder="状态筛选" class="status-select" clearable @change="handleSearch">
          <el-option label="待审核" :value="0" />
          <el-option label="审核通过" :value="1" />
          <el-option label="已驳回" :value="2" />
          <el-option label="已上架" :value="3" />
          <el-option label="已下架" :value="4" />
        </el-select>
      </div>

      <el-table :data="properties" v-loading="loading" stripe class="manage-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="房源标题" min-width="220" show-overflow-tooltip />
        <el-table-column label="价格" width="140">
          <template #default="scope">
            <span class="price-text">¥{{ formatPrice(scope.row.price, scope.row.transactionType) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-tag :class="getStatusClass(scope.row.status)" size="small">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="viewDetail(scope.row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalCount"
          layout="total, sizes, prev, pager, next, jumper"
          background
          class="pagination"
          @current-change="loadData"
          @size-change="handleSizeChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import type { Property } from '@/types'
import { ElMessage } from 'element-plus'
import { Refresh, Search, View, Edit, OfficeBuilding, CircleCheck, Clock, CircleClose } from '@element-plus/icons-vue'
import { propertyApi } from '@/api/user'

const router = useRouter()

const loading = ref(false)
const properties = ref<Property[]>([])
const searchKeyword = ref('')
const statusFilter = ref<number | null>(null)
const currentPage = ref(1)
const pageSize = ref(10)
const totalCount = ref(0)
const onlineCount = ref(0)
const pendingCount = ref(0)
const offlineCount = ref(0)
const statsTotal = ref(0)

const loadData = async () => {
  try {
    loading.value = true
    const res = await propertyApi.getAdminList({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value,
      status: statusFilter.value
    })
    if (res.code === 200) {
      properties.value = res.data.records
      totalCount.value = res.data.total
    }
  } catch {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const res = await propertyApi.getAdminStatistics()
    if (res.code === 200) {
      statsTotal.value = res.data.total || 0
      onlineCount.value = res.data.online || 0
      pendingCount.value = res.data.pending || 0
      offlineCount.value = res.data.offline || 0
    }
  } catch {
    // 统计失败不影响列表展示
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
  loadData()
}

const getStatusText = (status: number) => {
  const map: Record<number, string> = {
    0: '待审核',
    1: '审核通过',
    2: '已驳回',
    3: '已上架',
    4: '已下架'
  }
  return map[status] || '未知'
}

const getStatusClass = (status: number) => {
  const map: Record<number, string> = {
    0: 'status-pending',
    1: 'status-approved',
    2: 'status-rejected',
    3: 'status-online',
    4: 'status-offline'
  }
  return map[status] || 'status-unknown'
}

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

const viewDetail = (row: Property) => {
  router.push(`/property-detail/${row.id}`)
}

const handleEdit = (row: Property) => {
  router.push(`/admin/property/edit/${row.id}`)
}

onMounted(() => {
  loadData()
  loadStatistics()
})
</script>

<style scoped>
.property-manage-view {
  padding: var(--spacing-2xl);
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

.page-title h2 {
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

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  border-radius: var(--radius-full);
  padding: 10px 20px;
  font-weight: 500;
  transition: var(--transition-base);
}

.refresh-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
}

.stats-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
}

.stat-card {
  background: var(--bg-color);
  border-radius: var(--radius-xl);
  padding: var(--spacing-xl) var(--spacing-lg);
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  box-shadow: var(--shadow-base);
  border: 1px solid var(--border-color-light);
  transition: var(--transition-base);
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
  transform: translateY(-4px);
  box-shadow: var(--shadow-card-hover);
  border-color: var(--primary-color-bg);
}

.stat-card:hover::before {
  transform: scaleX(1);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
  flex-shrink: 0;
}

.icon-total {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-color-light) 100%);
}

.icon-online {
  background: linear-gradient(135deg, var(--success-color) 0%, var(--success-color-light) 100%);
}

.icon-pending {
  background: linear-gradient(135deg, var(--warning-color) 0%, var(--warning-color-light) 100%);
}

.icon-offline {
  background: linear-gradient(135deg, var(--text-color-tertiary) 0%, var(--text-color-secondary) 100%);
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-color-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: var(--text-color-secondary);
  margin-top: 4px;
  font-weight: 500;
}

.content-card {
  background: var(--bg-color);
  border-radius: var(--radius-xl);
  padding: var(--spacing-xl);
  box-shadow: var(--shadow-base);
  border: 1px solid var(--border-color-light);
  transition: var(--transition-base);
}

.content-card:hover {
  box-shadow: var(--shadow-lg);
}

.filter-bar {
  display: flex;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
  flex-wrap: wrap;
}

.search-input {
  flex: 1;
  min-width: 240px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: var(--radius-full);
}

.status-select {
  width: 160px;
}

.status-select :deep(.el-input__wrapper) {
  border-radius: var(--radius-full);
}

.manage-table {
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.manage-table :deep(.el-table__header th) {
  font-weight: 600;
  color: var(--text-color-primary);
  background: var(--bg-color-light);
}

.manage-table :deep(.el-table__row:hover > td) {
  background-color: var(--primary-color-bg-light) !important;
}

.manage-table :deep(.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell) {
  background-color: var(--bg-color-light);
}

.price-text {
  color: var(--accent-color);
  font-weight: 700;
  font-size: 16px;
}

.status-pending {
  background: var(--warning-color-bg);
  color: var(--warning-color);
  border-color: var(--warning-color-bg);
}

.status-approved {
  background: var(--primary-color-bg);
  color: var(--primary-color);
  border-color: var(--primary-color-bg);
}

.status-rejected {
  background: var(--error-color-bg);
  color: var(--error-color);
  border-color: var(--error-color-bg);
}

.status-online {
  background: var(--success-color-bg);
  color: var(--success-color);
  border-color: var(--success-color-bg);
}

.status-offline {
  background: var(--bg-color-light);
  color: var(--text-color-secondary);
  border-color: var(--bg-color-light);
}

.status-unknown {
  background: var(--bg-color-light);
  color: var(--text-color-tertiary);
  border-color: var(--bg-color-light);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: var(--spacing-xl);
}

.pagination {
  --el-pagination-hover-color: var(--primary-color);
}

@media (max-width: 1024px) {
  .stats-section {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .property-manage-view {
    padding: var(--spacing-lg);
  }

  .page-title h2 {
    font-size: 18px;
  }

  .stats-section {
    grid-template-columns: 1fr;
  }

  .filter-bar {
    flex-direction: column;
  }

  .search-input,
  .status-select {
    width: 100%;
  }
}
</style>
