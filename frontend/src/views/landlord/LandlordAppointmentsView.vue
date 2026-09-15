<template>
  <div class="landlord-appointments-view page-animate">
    <div class="page-header">
      <div class="section-title">
        <span class="title-bar"></span>
        <h1>预约管理</h1>
      </div>
      <div class="header-stats">
        <el-tag type="warning" effect="light" class="stat-tag">
          待确认：{{ pendingCount }}
        </el-tag>
        <el-tag type="info" effect="light" class="stat-tag">
          共 {{ total }} 条预约
        </el-tag>
      </div>
    </div>

    <div class="table-card">
      <div class="filter-bar">
        <div class="filter-left">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索房源标题或用户信息"
            clearable
            class="filter-input"
            @clear="loadAppointments"
            @keyup.enter="loadAppointments"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>

          <el-select
            v-model="filterStatus"
            placeholder="预约状态"
            clearable
            class="filter-select"
            @change="loadAppointments"
          >
            <el-option label="全部状态" :value="null" />
            <el-option label="待确认" :value="0" />
            <el-option label="已确认" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="已取消" :value="3" />
          </el-select>
        </div>

        <div class="filter-right">
          <el-button type="primary" @click="loadAppointments" class="search-btn">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>

          <el-button @click="handleReset" class="reset-btn">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="appointments"
        stripe
        class="appointment-table"
      >
        <el-table-column prop="id" label="ID" width="70" />
        
        <el-table-column label="房源信息" min-width="220">
          <template #default="{ row }">
            <div class="property-info">
              <div class="property-title">{{ row.property?.title || '未知房源' }}</div>
              <div class="property-address">
                <el-icon><Location /></el-icon>
                <span>{{ row.property?.address }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="用户信息" width="160">
          <template #default="{ row }">
            <div class="user-info">
              <div class="user-name">{{ row.user?.realName || row.user?.username }}</div>
              <div class="user-phone">
                <el-icon><Phone /></el-icon>
                {{ row.contactPhone }}
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="appointmentTime" label="预约时间" width="170">
          <template #default="{ row }">
            <div class="time-info">
              <el-icon><Clock /></el-icon>
              <span>{{ formatDateTime(row.appointmentTime) }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="light" round>
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />

        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              type="success"
              size="small"
              @click="handleConfirm(row)"
              class="action-btn"
            >
              <el-icon><Check /></el-icon>
              确认
            </el-button>
            <el-button
              v-if="row.status === 0"
              type="danger"
              size="small"
              @click="handleReject(row)"
              class="action-btn"
            >
              <el-icon><Close /></el-icon>
              拒绝
            </el-button>
            <el-button
              v-if="row.status === 1"
              type="primary"
              size="small"
              @click="handleComplete(row)"
              class="action-btn"
            >
              <el-icon><Check /></el-icon>
              完成
            </el-button>
            <el-button
              v-if="row.transactionType !== null && row.transactionType !== undefined"
              type="primary"
              size="small"
              plain
              @click="viewProcess(row)"
              class="action-btn"
            >
              <el-icon><Promotion /></el-icon>
              流程
            </el-button>
            <el-button
              type="info"
              size="small"
              @click="handleViewDetail(row)"
              class="action-btn"
            >
              <el-icon><View /></el-icon>
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @size-change="loadAppointments"
        @current-change="loadAppointments"
      />
    </div>

    <el-dialog
      v-model="detailDialogVisible"
      title="预约详情"
      width="640px"
      class="detail-dialog"
    >
      <div class="detail-content">
        <div class="detail-section">
          <div class="detail-section-title">
            <el-icon><InfoFilled /></el-icon>
            基本信息
          </div>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label">预约ID</span>
              <span class="detail-value">{{ currentAppointment?.id }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">预约状态</span>
              <el-tag
                v-if="currentAppointment"
                :type="getStatusType(currentAppointment.status)"
                effect="light"
                round
              >
                {{ getStatusText(currentAppointment.status) }}
              </el-tag>
            </div>
            <div class="detail-item">
              <span class="detail-label">预约时间</span>
              <span class="detail-value">{{ formatDateTime(currentAppointment?.appointmentTime) }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">创建时间</span>
              <span class="detail-value">{{ formatDateTime(currentAppointment?.createTime) }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <div class="detail-section-title">
            <el-icon><House /></el-icon>
            房源信息
          </div>
          <div class="property-detail">
            <div class="property-detail-title">{{ currentAppointment?.property?.title }}</div>
            <div class="property-detail-row">
              <div class="detail-item full-width">
                <span class="detail-label">房源地址</span>
                <span class="detail-value">{{ currentAppointment?.property?.address }}</span>
              </div>
            </div>
            <div class="property-detail-row">
              <div class="detail-item">
                <span class="detail-label">房源价格</span>
                <span class="detail-value price">
                  ¥{{ currentAppointment?.property?.price?.toLocaleString() }}
                  <span class="price-unit">
                    {{ currentAppointment?.property?.priceType === 0 ? '/月' : '' }}
                  </span>
                </span>
              </div>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <div class="detail-section-title">
            <el-icon><User /></el-icon>
            预约用户
          </div>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label">用户姓名</span>
              <span class="detail-value">
                {{ currentAppointment?.user?.realName || currentAppointment?.user?.username }}
              </span>
            </div>
            <div class="detail-item">
              <span class="detail-label">联系电话</span>
              <span class="detail-value">{{ currentAppointment?.contactPhone }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section" v-if="currentAppointment?.remark">
          <div class="detail-section-title">
            <el-icon><ChatDotRound /></el-icon>
            备注信息
          </div>
          <div class="remark-content">{{ currentAppointment?.remark }}</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false" class="dialog-cancel-btn">
          关闭
        </el-button>
        <el-button
          v-if="currentAppointment?.status === 0"
          type="success"
          @click="handleConfirm(currentAppointment)"
          class="dialog-action-btn"
        >
          确认预约
        </el-button>
        <el-button
          v-if="currentAppointment?.status === 1"
          type="primary"
          @click="handleComplete(currentAppointment)"
          class="dialog-action-btn"
        >
          完成看房
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="rejectDialogVisible"
      title="拒绝预约"
      width="480px"
      class="reject-dialog"
    >
      <div class="reject-icon">
        <el-icon><WarningFilled /></el-icon>
      </div>
      <div class="reject-title">确定要拒绝此预约吗？</div>
      <div class="reject-desc">拒绝后用户将收到通知，请填写拒绝原因（选填）</div>
      <el-form :model="rejectForm" class="reject-form">
        <el-form-item>
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false" class="dialog-cancel-btn">
          取消
        </el-button>
        <el-button type="danger" @click="confirmReject" :loading="rejecting" class="dialog-action-btn">
          确认拒绝
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  Check,
  Close,
  Promotion,
  View,
  Calendar,
  Location,
  Phone,
  Clock,
  InfoFilled,
  House,
  User,
  ChatDotRound,
  WarningFilled
} from '@element-plus/icons-vue'
import { appointmentApi } from '@/api/appointment'

const router = useRouter()

const loading = ref(false)
const rejecting = ref(false)
const appointments = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const filterStatus = ref<number | null>(null)

const detailDialogVisible = ref(false)
const rejectDialogVisible = ref(false)
const currentAppointment = ref<any>(null)

const rejectForm = reactive({
  reason: ''
})

const pendingCount = computed(() =>
  appointments.value.filter(a => a.status === 0).length
)

const loadAppointments = async () => {
  try {
    loading.value = true
    const response = await appointmentApi.getLandlordList()
    
    if (response.code === 200 && response.data) {
      let list = response.data
      
      if (searchKeyword.value) {
        const keyword = searchKeyword.value.toLowerCase()
        list = list.filter((item: any) =>
          item.property?.title?.toLowerCase().includes(keyword) ||
          item.user?.username?.toLowerCase().includes(keyword) ||
          item.user?.realName?.toLowerCase().includes(keyword) ||
          item.contactPhone?.includes(keyword)
        )
      }
      
      if (filterStatus.value !== null) {
        list = list.filter((item: any) => item.status === filterStatus.value)
      }
      
      list.sort((a: any, b: any) => {
        return new Date(b.createTime).getTime() - new Date(a.createTime).getTime()
      })
      
      total.value = list.length
      
      const start = (pageNum.value - 1) * pageSize.value
      const end = start + pageSize.value
      appointments.value = list.slice(start, end)
    }
  } catch (error: any) {
    console.error('加载预约列表失败:', error)
    ElMessage.error(error.message || '加载预约列表失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  searchKeyword.value = ''
  filterStatus.value = null
  pageNum.value = 1
  loadAppointments()
}

const handleViewDetail = (row: any) => {
  currentAppointment.value = row
  detailDialogVisible.value = true
}

const viewProcess = (row: any) => {
  router.push(`/transaction-process/${row.id}`)
}

const handleConfirm = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      '确定要确认这个预约吗？',
      '确认预约',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'success'
      }
    )
    
    await appointmentApi.confirm(row.id)
    ElMessage.success('预约已确认')
    detailDialogVisible.value = false
    loadAppointments()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('确认预约失败:', error)
      ElMessage.error(error.message || '确认预约失败')
    }
  }
}

const handleReject = (row: any) => {
  currentAppointment.value = row
  rejectForm.reason = ''
  rejectDialogVisible.value = true
}

const confirmReject = async () => {
  try {
    rejecting.value = true
    await appointmentApi.cancel(currentAppointment.value.id)
    ElMessage.success('预约已拒绝')
    rejectDialogVisible.value = false
    loadAppointments()
  } catch (error: any) {
    console.error('拒绝预约失败:', error)
    ElMessage.error(error.message || '拒绝预约失败')
  } finally {
    rejecting.value = false
  }
}

const handleComplete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      '确认已完成看房吗？',
      '完成看房',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'success'
      }
    )
    
    await appointmentApi.complete(row.id)
    ElMessage.success('操作成功')
    detailDialogVisible.value = false
    loadAppointments()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const formatDateTime = (date: string) => {
  if (!date) return '-'
  const d = new Date(date)
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    0: '待确认',
    1: '已确认',
    2: '已完成',
    3: '已取消'
  }
  return statusMap[status] || '未知'
}

const getStatusType = (status: number) => {
  const typeMap: Record<number, any> = {
    0: 'warning',
    1: 'success',
    2: 'info',
    3: 'danger'
  }
  return typeMap[status] || ''
}

onMounted(() => {
  loadAppointments()
})
</script>

<style scoped>
.landlord-appointments-view {
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

.header-stats {
  display: flex;
  gap: var(--spacing-md);
}

.stat-tag {
  font-size: 13px !important;
  padding: 6px 14px !important;
  height: auto !important;
  line-height: 1.5 !important;
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

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
  gap: var(--spacing-md);
  flex-wrap: wrap;
}

.filter-left {
  display: flex;
  gap: var(--spacing-md);
  flex-wrap: wrap;
}

.filter-right {
  display: flex;
  gap: var(--spacing-sm);
}

.filter-input {
  width: 280px;
}

.filter-select {
  width: 140px;
}

.search-btn {
  border-radius: var(--radius-full) !important;
  padding: 0 20px !important;
  transition: all 0.25s ease !important;
}

.search-btn:hover {
  transform: translateY(-1px) !important;
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3) !important;
}

.reset-btn {
  border-radius: var(--radius-full) !important;
  padding: 0 20px !important;
  transition: all 0.25s ease !important;
}

.reset-btn:hover {
  border-color: var(--primary-color) !important;
  color: var(--primary-color) !important;
}

.appointment-table {
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.appointment-table :deep(.el-table__header th) {
  background: var(--bg-color-light) !important;
  font-weight: 600 !important;
  color: var(--text-color-primary) !important;
  font-size: 14px;
}

.appointment-table :deep(.el-table__body tr) {
  transition: background-color 0.2s ease;
}

.appointment-table :deep(.el-table__body tr:hover > td) {
  background-color: var(--primary-color-bg-light) !important;
}

.appointment-table :deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background-color: var(--bg-color-light);
}

.appointment-table :deep(.el-table__body td) {
  border-bottom: 1px solid var(--border-color-lighter);
}

.property-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.property-title {
  font-weight: 600;
  color: var(--text-color-primary);
  font-size: 14px;
}

.property-address {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-color-secondary);
}

.property-address .el-icon {
  color: var(--primary-color);
  font-size: 12px;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  font-weight: 500;
  color: var(--text-color-primary);
  font-size: 14px;
}

.user-phone {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-color-tertiary);
}

.user-phone .el-icon {
  font-size: 12px;
}

.time-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-color-regular);
}

.time-info .el-icon {
  color: var(--primary-color);
  font-size: 14px;
}

.action-btn {
  border-radius: var(--radius-md) !important;
  transition: all 0.25s ease !important;
  padding: 6px 12px !important;
}

.action-btn:hover {
  transform: translateY(-1px) !important;
}

.pagination {
  margin-top: var(--spacing-lg);
  display: flex;
  justify-content: center;
}

.detail-dialog :deep(.el-dialog__body) {
  padding: var(--spacing-lg) var(--spacing-xl);
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.detail-section {
  background: var(--bg-color-light);
  border-radius: var(--radius-lg);
  padding: var(--spacing-md) var(--spacing-lg);
}

.detail-section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-color-primary);
  margin-bottom: var(--spacing-md);
}

.detail-section-title .el-icon {
  color: var(--primary-color);
  font-size: 16px;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-md);
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item.full-width {
  grid-column: 1 / -1;
}

.detail-label {
  font-size: 12px;
  color: var(--text-color-tertiary);
}

.detail-value {
  font-size: 14px;
  color: var(--text-color-primary);
  font-weight: 500;
}

.detail-value.price {
  color: var(--accent-color);
  font-size: 18px;
  font-weight: 700;
}

.price-unit {
  font-size: 12px;
  font-weight: 400;
  color: var(--text-color-tertiary);
  margin-left: 2px;
}

.property-detail-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-color-primary);
  margin-bottom: var(--spacing-sm);
}

.property-detail-row {
  margin-bottom: var(--spacing-sm);
}

.property-detail-row:last-child {
  margin-bottom: 0;
}

.remark-content {
  background: var(--bg-color);
  border-radius: var(--radius-md);
  padding: var(--spacing-md);
  font-size: 14px;
  color: var(--text-color-regular);
  line-height: 1.6;
}

.dialog-cancel-btn {
  border-radius: var(--radius-full) !important;
  padding: 0 24px !important;
  transition: all 0.25s ease !important;
}

.dialog-cancel-btn:hover {
  border-color: var(--text-color-tertiary) !important;
}

.dialog-action-btn {
  border-radius: var(--radius-full) !important;
  padding: 0 28px !important;
  transition: all 0.25s ease !important;
}

.dialog-action-btn:hover {
  transform: translateY(-1px) !important;
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3) !important;
}

.reject-dialog :deep(.el-dialog__body) {
  text-align: center;
  padding: var(--spacing-xl) var(--spacing-2xl);
}

.reject-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto var(--spacing-md);
  border-radius: 50%;
  background: var(--error-color-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--error-color);
  font-size: 32px;
}

.reject-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-color-primary);
  margin-bottom: var(--spacing-sm);
}

.reject-desc {
  font-size: 14px;
  color: var(--text-color-secondary);
  margin-bottom: var(--spacing-lg);
}

.reject-form {
  text-align: left;
}

.reject-form :deep(.el-textarea__inner) {
  border-radius: var(--radius-md);
}

@media (max-width: 1024px) {
  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-left,
  .filter-right {
    justify-content: flex-start;
  }
}

@media (max-width: 768px) {
  .landlord-appointments-view {
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

  .header-stats {
    width: 100%;
  }

  .table-card {
    padding: var(--spacing-md);
    border-radius: var(--radius-xl);
  }

  .filter-input {
    width: 100%;
  }

  .filter-select {
    width: 100%;
  }

  .filter-left,
  .filter-right {
    width: 100%;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
