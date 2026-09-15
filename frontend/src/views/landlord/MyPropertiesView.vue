<template>
  <div class="my-properties-view page-animate">
    <div class="page-header">
      <div class="section-title">
        <span class="title-bar"></span>
        <h1>我的房源</h1>
      </div>
      <el-button type="primary" @click="goPublish" class="publish-btn">
        <el-icon><Plus /></el-icon>
        发布新房源
      </el-button>
    </div>

    <div class="stats-cards" v-if="properties.length > 0">
      <div class="stat-card total">
        <div class="stat-icon">
          <el-icon><House /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ properties.length }}</div>
          <div class="stat-label">房源总数</div>
        </div>
      </div>
      <div class="stat-card online">
        <div class="stat-icon">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ onlineCount }}</div>
          <div class="stat-label">已上架</div>
        </div>
      </div>
      <div class="stat-card pending">
        <div class="stat-icon">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ pendingCount }}</div>
          <div class="stat-label">待审核</div>
        </div>
      </div>
      <div class="stat-card offline">
        <div class="stat-icon">
          <el-icon><Warning /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ offlineCount }}</div>
          <div class="stat-label">已下架</div>
        </div>
      </div>
    </div>

    <div class="table-card">
      <el-table :data="properties" v-loading="loading" class="property-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="房源标题" min-width="200">
          <template #default="scope">
            <span class="property-title">{{ scope.row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="140">
          <template #default="scope">
            <span class="price-text">
              ¥{{ scope.row.price.toLocaleString() }}
              <span class="price-unit">{{ scope.row.priceType === 0 ? '/月' : '' }}</span>
            </span>
          </template>
        </el-table-column>
        <el-table-column label="面积" width="100">
          <template #default="scope">
            <span>{{ scope.row.area }} ㎡</span>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="110">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" effect="light" round>
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="edit(scope.row.id)"
              class="action-btn edit-btn"
            >
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button
              v-if="scope.row.status === 1"
              type="success"
              size="small"
              @click="online(scope.row.id)"
              class="action-btn"
            >
              <el-icon><Top /></el-icon>
              上架
            </el-button>
            <el-button
              v-if="scope.row.status === 3"
              type="warning"
              size="small"
              @click="offline(scope.row.id)"
              class="action-btn"
            >
              <el-icon><Bottom /></el-icon>
              下架
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="remove(scope.row.id)"
              class="action-btn delete-btn"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="properties.length === 0 && !loading" description="暂无房源" class="empty-state" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  House,
  CircleCheck,
  Clock,
  Warning,
  Edit,
  Top,
  Bottom,
  Delete
} from '@element-plus/icons-vue'
import { propertyApi } from '@/api/user'
import type { Property } from '@/types/user'

const router = useRouter()
const loading = ref(false)
const properties = ref<Property[]>([])

const onlineCount = computed(() =>
  properties.value.filter(p => p.status === 3).length
)

const pendingCount = computed(() =>
  properties.value.filter(p => p.status === 0).length
)

const offlineCount = computed(() =>
  properties.value.filter(p => p.status === 4).length
)

const loadData = async () => {
  try {
    loading.value = true
    const res = await propertyApi.getMyList()
    if (res.code === 200 && res.data) {
      properties.value = res.data
    }
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const getStatusType = (status: number) => {
  const map: Record<number, any> = {
    0: 'info',
    1: 'success',
    2: 'danger',
    3: 'success',
    4: 'info'
  }
  return map[status] || 'info'
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

const online = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定上架该房源吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await propertyApi.online(id)
    if (res.code === 200) {
      ElMessage.success('上架成功')
      loadData()
    } else {
      ElMessage.error(res.message)
    }
  } catch {
    // 取消
  }
}

const offline = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定下架该房源吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await propertyApi.offline(id)
    if (res.code === 200) {
      ElMessage.success('下架成功')
      loadData()
    } else {
      ElMessage.error(res.message)
    }
  } catch {
    // 取消
  }
}

const remove = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定删除该房源吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await propertyApi.delete(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    } else {
      ElMessage.error(res.message)
    }
  } catch {
    // 取消
  }
}

const goPublish = () => {
  router.push('/landlord/publish')
}

const edit = (id: number) => {
  router.push(`/landlord/edit/${id}`)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.my-properties-view {
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

.publish-btn {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--accent-color) 100%) !important;
  border: none !important;
  border-radius: var(--radius-full) !important;
  padding: 0 24px !important;
  height: 42px !important;
  font-weight: 600 !important;
  transition: all 0.3s ease !important;
}

.publish-btn:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 6px 20px rgba(22, 119, 255, 0.35) !important;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
}

.stat-card {
  background: var(--bg-color);
  border-radius: var(--radius-xl);
  padding: var(--spacing-lg) var(--spacing-xl);
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  box-shadow: var(--shadow-sm);
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
}

.stat-card:hover::before {
  transform: scaleX(1);
}

.stat-card.total .stat-icon {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-color-light) 100%);
}

.stat-card.online .stat-icon {
  background: linear-gradient(135deg, var(--success-color) 0%, #73d13d 100%);
}

.stat-card.pending .stat-icon {
  background: linear-gradient(135deg, var(--warning-color) 0%, #ffc53d 100%);
}

.stat-card.offline .stat-icon {
  background: linear-gradient(135deg, var(--text-color-tertiary) 0%, var(--text-color-secondary) 100%);
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  flex-shrink: 0;
}

.stat-content {
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

.table-card {
  background: var(--bg-color);
  border-radius: var(--radius-2xl);
  padding: var(--spacing-xl);
  box-shadow: var(--shadow-base);
  border: 1px solid var(--border-color-light);
  transition: var(--transition-base);
}

.table-card:hover {
  box-shadow: var(--shadow-lg);
}

.property-table {
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.property-table :deep(.el-table__header th) {
  background: var(--bg-color-light) !important;
  font-weight: 600 !important;
  color: var(--text-color-primary) !important;
  font-size: 14px;
}

.property-table :deep(.el-table__body tr) {
  transition: background-color 0.2s ease;
}

.property-table :deep(.el-table__body tr:hover > td) {
  background-color: var(--primary-color-bg-light) !important;
}

.property-table :deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background-color: var(--bg-color-light);
}

.property-table :deep(.el-table__body td) {
  border-bottom: 1px solid var(--border-color-lighter);
}

.property-title {
  font-weight: 500;
  color: var(--text-color-primary);
}

.price-text {
  font-weight: 700;
  color: var(--accent-color);
  font-size: 15px;
}

.price-unit {
  font-weight: 400;
  font-size: 12px;
  color: var(--text-color-tertiary);
  margin-left: 2px;
}

.action-btn {
  border-radius: var(--radius-md) !important;
  transition: all 0.25s ease !important;
  padding: 6px 14px !important;
}

.action-btn:hover {
  transform: translateY(-1px) !important;
}

.edit-btn:hover {
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3) !important;
}

.delete-btn:hover {
  box-shadow: 0 4px 12px rgba(255, 77, 79, 0.3) !important;
}

.empty-state {
  padding: var(--spacing-3xl) 0;
}

@media (max-width: 1024px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .my-properties-view {
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

  .stats-cards {
    grid-template-columns: 1fr;
  }

  .table-card {
    padding: var(--spacing-md);
    border-radius: var(--radius-xl);
  }
}
</style>
