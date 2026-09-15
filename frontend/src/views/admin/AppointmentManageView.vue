<template>
  <div class="appointment-manage-view page-animate">
    <div class="page-header">
      <div class="page-title">
        <span class="title-bar"></span>
        <h2>预约管理</h2>
      </div>
      <el-button type="primary" class="refresh-btn" @click="loadData">
        <el-icon><Refresh /></el-icon>
        刷新数据
      </el-button>
    </div>

    <div class="stats-section">
      <div class="stat-card">
        <div class="stat-icon icon-total">
          <el-icon><Calendar /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ totalAppointments }}</div>
          <div class="stat-label">预约总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon icon-pending">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ pendingCount }}</div>
          <div class="stat-label">待确认</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon icon-confirmed">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ confirmedCount }}</div>
          <div class="stat-label">已确认</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon icon-completed">
          <el-icon><Finished /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ completedCount }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </div>
    </div>

    <div class="content-card">
      <div class="filter-bar">
        <el-select v-model="filters.status" placeholder="状态筛选" clearable class="filter-select" @change="handleFilterChange">
          <el-option label="待确认" :value="0" />
          <el-option label="已确认" :value="1" />
          <el-option label="已完成" :value="2" />
          <el-option label="已取消" :value="3" />
        </el-select>
        <el-button type="primary" class="btn-search" @click="loadData">
          <el-icon><Search /></el-icon>
          查询
        </el-button>
      </div>

      <el-table :data="appointments" v-loading="loading" stripe class="appointment-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="用户" width="160">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="32" class="user-avatar">
                <el-icon :size="16"><User /></el-icon>
              </el-avatar>
              <span class="user-name">{{ row.user?.username || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="房源" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="property-title">{{ row.property?.title || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="预约时间" width="180">
          <template #default="{ row }">
            <div class="time-cell">
              <el-icon :size="14"><Calendar /></el-icon>
              <span>{{ formatDate(row.appointmentTime) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :class="getStatusClass(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">
            <span class="create-time">{{ formatDate(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              type="danger"
              size="small"
              link
              class="btn-delete"
              @click="handleDelete(row)"
              :loading="row.deleting"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="appointments.length === 0 && !loading" description="暂无预约记录" class="empty-state" />

      <div class="pagination-wrapper">
        <el-pagination
          v-if="total > 0"
          v-model:current-page="filters.pageNum"
          v-model:page-size="filters.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          class="pagination"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search, Calendar, Clock, CircleCheck, Finished, User, Delete } from '@element-plus/icons-vue'
import { appointmentApi } from '@/api/appointment'
import type { Appointment } from '@/types'

const loading = ref(false)
const appointments = ref<Appointment[]>([])
const total = ref(0)
const allAppointments = ref<any[]>([])

const filters = reactive({
  pageNum: 1,
  pageSize: 10,
  status: null as number | null
})

const totalAppointments = computed(() => allAppointments.value.length)
const pendingCount = computed(() => allAppointments.value.filter(a => a.status === 0).length)
const confirmedCount = computed(() => allAppointments.value.filter(a => a.status === 1).length)
const completedCount = computed(() => allAppointments.value.filter(a => a.status === 2).length)

const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

const getStatusClass = (status?: number) => {
  const map: Record<number, string> = {
    0: 'status-pending',
    1: 'status-confirmed',
    2: 'status-completed',
    3: 'status-cancelled'
  }
  return map[status!] || 'status-unknown'
}

const getStatusText = (status?: number) => {
  const map: Record<number, string> = {
    0: '待确认',
    1: '已确认',
    2: '已完成',
    3: '已取消'
  }
  return map[status!] || '未知'
}

const handleFilterChange = () => {
  filters.pageNum = 1
}

const loadData = async () => {
  try {
    loading.value = true
    const res = await appointmentApi.getAdminAppointmentList({
      pageNum: 1,
      pageSize: 1000
    })
    if (res.code === 200 && res.data) {
      let list: any[] = Array.isArray(res.data) ? res.data : ((res.data as any).records || [])
      allAppointments.value = list
      
      if (filters.status !== null) {
        list = list.filter((item: any) => item.status === filters.status)
      }
      
      total.value = list.length
      
      const start = (filters.pageNum - 1) * filters.pageSize
      appointments.value = list.slice(start, start + filters.pageSize)
    }
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const handleDelete = async (row: any) => {
  if (!row.id) return
  try {
    await ElMessageBox.confirm('确定删除该预约吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    row.deleting = true
    await appointmentApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  } finally {
    row.deleting = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.appointment-manage-view {
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

.icon-pending {
  background: linear-gradient(135deg, var(--warning-color) 0%, var(--warning-color-light) 100%);
}

.icon-confirmed {
  background: linear-gradient(135deg, #722ed1 0%, #9254de 100%);
}

.icon-completed {
  background: linear-gradient(135deg, var(--success-color) 0%, var(--success-color-light) 100%);
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
  align-items: center;
}

.filter-select {
  width: 160px;
}

.filter-select :deep(.el-input__wrapper) {
  border-radius: var(--radius-full);
}

.btn-search {
  display: flex;
  align-items: center;
  gap: 6px;
  border-radius: var(--radius-full);
  padding: 10px 20px;
  font-weight: 500;
  transition: var(--transition-base);
}

.btn-search:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
}

.appointment-table {
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.appointment-table :deep(.el-table__header th) {
  font-weight: 600;
  color: var(--text-color-primary);
  background: var(--bg-color-light);
}

.appointment-table :deep(.el-table__row:hover > td) {
  background-color: var(--primary-color-bg-light) !important;
}

.appointment-table :deep(.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell) {
  background-color: var(--bg-color-light);
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  background: var(--primary-color-bg);
  border: 2px solid var(--border-color-light);
}

.user-avatar :deep(.el-icon) {
  color: var(--primary-color);
}

.user-name {
  font-weight: 500;
  color: var(--text-color-primary);
}

.property-title {
  color: var(--text-color-primary);
}

.time-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--text-color-regular);
}

.time-cell .el-icon {
  color: var(--primary-color);
}

.status-pending {
  background: var(--warning-color-bg);
  color: var(--warning-color);
  border-color: var(--warning-color-bg);
}

.status-confirmed {
  background: var(--primary-color-bg);
  color: var(--primary-color);
  border-color: var(--primary-color-bg);
}

.status-completed {
  background: var(--success-color-bg);
  color: var(--success-color);
  border-color: var(--success-color-bg);
}

.status-cancelled {
  background: var(--bg-color-light);
  color: var(--text-color-secondary);
  border-color: var(--bg-color-light);
}

.status-unknown {
  background: var(--bg-color-light);
  color: var(--text-color-tertiary);
  border-color: var(--bg-color-light);
}

.create-time {
  color: var(--text-color-secondary);
  font-size: 13px;
}

.btn-delete {
  color: var(--error-color) !important;
}

.empty-state {
  padding: var(--spacing-3xl);
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
  .appointment-manage-view {
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
    align-items: stretch;
  }

  .filter-select,
  .btn-search {
    width: 100%;
  }
}
</style>
