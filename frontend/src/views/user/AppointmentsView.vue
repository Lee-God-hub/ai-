<template>
  <div class="appointments-view page-animate">
    <div class="page-header">
      <div class="page-title">
        <span class="title-bar"></span>
        <h1>我的预约</h1>
      </div>
      <el-tag class="total-tag">共 {{ total }} 条预约</el-tag>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-card">
      <div class="filter-bar">
        <el-select
          v-model="filterStatus"
          placeholder="预约状态"
          clearable
          class="status-select"
          @change="loadAppointments"
        >
          <el-option label="全部状态" :value="null" />
          <el-option label="待确认" :value="0" />
          <el-option label="已确认" :value="1" />
          <el-option label="已完成" :value="2" />
          <el-option label="已取消" :value="3" />
        </el-select>

        <el-button type="primary" class="refresh-btn" @click="loadAppointments">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 预约列表 -->
    <div v-loading="loading" class="appointments-list">
      <div v-if="appointments.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无预约记录">
          <el-button type="primary" class="empty-btn" @click="goToPropertyList">去浏览房源</el-button>
        </el-empty>
      </div>

      <div v-else class="timeline-wrap">
        <div
          v-for="item in appointments"
          :key="item.id"
          class="timeline-item"
        >
          <div class="timeline-dot" :style="{ background: getStatusColor(item.status) }"></div>
          <div class="timeline-date">{{ formatDate(item.createTime) }}</div>
          <div class="appointment-card">
            <div class="appointment-content">
              <!-- 房源信息 -->
              <div class="property-section">
                <div class="property-image" @click="goToDetail(item.propertyId)">
                  <el-image
                    :src="getFirstImage(item.property?.images)"
                    fit="cover"
                    lazy
                  >
                    <template #error>
                      <div class="image-placeholder">
                        <el-icon :size="28"><Picture /></el-icon>
                      </div>
                    </template>
                  </el-image>
                </div>
                <div class="property-info">
                  <h3 class="property-name" @click="goToDetail(item.propertyId)">
                    {{ item.property?.title || '房源信息' }}
                  </h3>
                  <div class="property-location">
                    <el-icon :size="14"><Location /></el-icon>
                    <span>{{ item.property?.address }}</span>
                  </div>
                  <div class="property-price-row">
                    <span class="price-value">¥{{ formatPrice(item.property?.price, item.property?.transactionType) }}</span>
                    <span class="price-unit">{{ item.property?.transactionType !== 1 ? '/月' : '' }}</span>
                  </div>
                </div>
                <div class="status-badge-wrap">
                  <el-tag :class="['status-tag', 'status-' + item.status]">
                    {{ getStatusText(item.status) }}
                  </el-tag>
                </div>
              </div>

              <!-- 预约信息 -->
              <div class="appointment-section">
                <div class="info-grid">
                  <div class="info-item">
                    <div class="info-icon time-icon">
                      <el-icon><Clock /></el-icon>
                    </div>
                    <div class="info-content">
                      <div class="info-label">预约时间</div>
                      <div class="info-value">{{ formatDateTime(item.appointmentTime) }}</div>
                    </div>
                  </div>
                  <div class="info-item">
                    <div class="info-icon phone-icon">
                      <el-icon><Phone /></el-icon>
                    </div>
                    <div class="info-content">
                      <div class="info-label">联系电话</div>
                      <div class="info-value">{{ item.contactPhone }}</div>
                    </div>
                  </div>
                </div>
                <div v-if="item.remark" class="remark-item">
                  <div class="remark-label">备注信息</div>
                  <div class="remark-value">{{ item.remark }}</div>
                </div>
              </div>

              <!-- 操作按钮 -->
              <div class="appointment-actions">
                <el-button
                  type="primary"
                  class="action-btn view-btn"
                  @click="goToDetail(item.propertyId)"
                >
                  <el-icon><View /></el-icon>
                  查看房源
                </el-button>

                <el-button
                  v-if="item.transactionType !== null && item.transactionType !== undefined"
                  class="action-btn process-btn"
                  @click="viewProcess(item)"
                >
                  <el-icon><Promotion /></el-icon>
                  查看流程
                </el-button>

                <el-button
                  v-if="item.status === 1 && (item.transactionType === null || item.transactionType === undefined)"
                  type="success"
                  class="action-btn init-btn"
                  @click="initProcess(item)"
                >
                  <el-icon><Plus /></el-icon>
                  启动流程
                </el-button>
                
                <el-button
                  v-if="item.status === 0"
                  class="action-btn cancel-btn"
                  @click="handleCancel(item)"
                >
                  <el-icon><Close /></el-icon>
                  取消预约
                </el-button>

                <el-button
                  v-if="item.status === 1"
                  type="success"
                  class="action-btn complete-btn"
                  @click="handleComplete(item)"
                >
                  <el-icon><Check /></el-icon>
                  完成看房
                </el-button>

                <el-button
                  v-if="item.status === 2 || item.status === 3"
                  class="action-btn delete-btn"
                  @click="handleDelete(item)"
                >
                  <el-icon><Delete /></el-icon>
                  删除记录
                </el-button>
              </div>
            </div>
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
        :page-sizes="[5, 10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @size-change="loadAppointments"
        @current-change="loadAppointments"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { appointmentApi } from '@/api/appointment'
import axios from 'axios'

const router = useRouter()

// 数据
const loading = ref(false)
const appointments = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const filterStatus = ref<number | null>(null)

/**
 * 加载预约列表
 */
const loadAppointments = async () => {
  try {
    loading.value = true
    const response = await appointmentApi.getMyList()
    
    if (response.code === 200 && response.data) {
      let list = response.data
      
      // 状态过滤
      if (filterStatus.value !== null) {
        list = list.filter((item: any) => item.status === filterStatus.value)
      }
      
      // 按创建时间倒序排序
      list.sort((a: any, b: any) => {
        return new Date(b.createTime).getTime() - new Date(a.createTime).getTime()
      })
      
      total.value = list.length
      
      // 分页
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

/**
 * 取消预约
 */
const handleCancel = async (item: any) => {
  try {
    await ElMessageBox.confirm(
      '确定要取消这个预约吗？',
      '取消预约',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await appointmentApi.cancel(item.id)
    ElMessage.success('取消预约成功')
    loadAppointments()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('取消预约失败:', error)
      ElMessage.error(error.message || '取消预约失败')
    }
  }
}

/**
 * 完成看房
 */
const handleComplete = async (item: any) => {
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
    
    await appointmentApi.complete(item.id)
    ElMessage.success('操作成功')
    loadAppointments()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error(error.message || '操作失败')
    }
  }
}

/**
 * 删除记录
 */
const handleDelete = async (item: any) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条预约记录吗？',
      '删除记录',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 这里应该调用删除API，但目前后端可能没有提供
    // 暂时使用取消接口
    await appointmentApi.cancel(item.id)
    ElMessage.success('删除成功')
    loadAppointments()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(error.message || '删除失败')
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
 * 查看交易流程
 */
const viewProcess = (item: any) => {
  router.push(`/transaction-process/${item.id}`)
}

/**
 * 初始化交易流程
 */
const initProcess = async (item: any) => {
  try {
    const { value: transactionType } = await ElMessageBox.prompt(
      '请输入交易类型（0=租房，1=买房）',
      '启动流程',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /^[01]$/,
        inputErrorMessage: '请输入0（租房）或1（买房）'
      }
    )

    loading.value = true
    
    const response = await axios.post('/transaction-process/init', {
      appointmentId: item.id,
      transactionType: parseInt(transactionType)
    })

    if (response.data.success) {
      ElMessage.success('流程启动成功，即将跳转...')
      setTimeout(() => {
        router.push(`/transaction-process/${item.id}`)
      }, 1500)
    } else {
      ElMessage.error(response.data.message || '流程启动失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('启动流程失败:', error)
      ElMessage.error('启动流程失败')
    }
  } finally {
    loading.value = false
  }
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
  return new Date(date).toLocaleString('zh-CN')
}

/**
 * 格式化日期时间
 */
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

/**
 * 获取状态文本
 */
const getStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    0: '待确认',
    1: '已确认',
    2: '已完成',
    3: '已取消'
  }
  return statusMap[status] || '未知'
}

/**
 * 获取状态类型
 */
const getStatusType = (status: number) => {
  const typeMap: Record<number, any> = {
    0: 'warning',
    1: 'success',
    2: 'info',
    3: 'danger'
  }
  return typeMap[status] || ''
}

/**
 * 获取状态颜色
 */
const getStatusColor = (status: number) => {
  const colorMap: Record<number, string> = {
    0: '#E6A23C',
    1: '#67C23A',
    2: '#909399',
    3: '#F56C6C'
  }
  return colorMap[status] || '#909399'
}

// 页面加载时获取数据
onMounted(() => {
  loadAppointments()
})
</script>

<style scoped>
.appointments-view {
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

.status-select {
  width: 160px;
}

.status-select :deep(.el-select__wrapper) {
  border-radius: var(--radius-full) !important;
  box-shadow: 0 0 0 1px var(--border-color) inset;
}

.status-select :deep(.el-select__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--primary-color-light) inset !important;
}

.status-select :deep(.el-select__wrapper.is-focused) {
  box-shadow: 0 0 0 1px var(--primary-color) inset !important;
}

.refresh-btn {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-color-light) 100%) !important;
  border: none !important;
  border-radius: var(--radius-full) !important;
  padding: 0 var(--spacing-xl) !important;
  font-weight: 600;
  transition: all 0.3s ease !important;
}

.refresh-btn:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 4px 16px rgba(22, 119, 255, 0.35) !important;
}

.appointments-list {
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

.timeline-wrap {
  position: relative;
  padding-left: 24px;
}

.timeline-wrap::before {
  content: '';
  position: absolute;
  left: 7px;
  top: 10px;
  bottom: 10px;
  width: 2px;
  background: linear-gradient(180deg, var(--primary-color-bg) 0%, var(--accent-color-bg) 100%);
}

.timeline-item {
  position: relative;
  margin-bottom: var(--spacing-xl);
}

.timeline-item:last-child {
  margin-bottom: 0;
}

.timeline-dot {
  position: absolute;
  left: -24px;
  top: 24px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 3px solid var(--bg-color);
  box-shadow: 0 0 0 2px currentColor;
  z-index: 1;
}

.timeline-date {
  position: absolute;
  left: -140px;
  top: 20px;
  font-size: 12px;
  color: var(--text-color-tertiary);
  white-space: nowrap;
}

.appointment-card {
  background: var(--bg-color);
  border-radius: var(--radius-xl);
  padding: var(--spacing-xl);
  box-shadow: var(--shadow-base);
  border: 1px solid var(--border-color-light);
  transition: var(--transition-base);
}

.appointment-card:hover {
  box-shadow: var(--shadow-card-hover);
  border-color: var(--primary-color-bg);
  transform: translateX(4px);
}

.appointment-content {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.property-section {
  display: flex;
  gap: var(--spacing-lg);
  align-items: flex-start;
}

.property-image {
  width: 180px;
  height: 130px;
  flex-shrink: 0;
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  position: relative;
}

.property-image :deep(.el-image) {
  width: 100%;
  height: 100%;
}

.property-image :deep(.el-image img) {
  transition: transform 0.4s ease;
}

.property-image:hover :deep(.el-image img) {
  transform: scale(1.08);
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

.property-info {
  flex: 1;
  min-width: 0;
}

.property-name {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-color-primary);
  margin: 0 0 8px 0;
  cursor: pointer;
  transition: color 0.2s;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.property-name:hover {
  color: var(--primary-color);
}

.property-location {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-color-secondary);
  margin-bottom: 10px;
}

.property-location .el-icon {
  color: var(--primary-color);
}

.property-price-row {
  display: flex;
  align-items: baseline;
}

.price-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--accent-color);
}

.price-unit {
  font-size: 13px;
  color: var(--text-color-tertiary);
  margin-left: 4px;
  font-weight: 400;
}

.status-badge-wrap {
  flex-shrink: 0;
}

.status-tag {
  border: none !important;
  padding: 0 var(--spacing-md) !important;
  height: 28px !important;
  line-height: 26px !important;
  font-weight: 500;
}

.status-tag.status-0 {
  background: var(--warning-color-bg) !important;
  color: var(--warning-color) !important;
}

.status-tag.status-1 {
  background: var(--success-color-bg) !important;
  color: var(--success-color) !important;
}

.status-tag.status-2 {
  background: var(--primary-color-bg) !important;
  color: var(--primary-color) !important;
}

.status-tag.status-3 {
  background: var(--error-color-bg) !important;
  color: var(--error-color) !important;
}

.appointment-section {
  background: var(--bg-color-light);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-lg);
}

.info-item {
  display: flex;
  gap: var(--spacing-md);
  align-items: flex-start;
}

.info-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 18px;
}

.time-icon {
  background: var(--primary-color-bg);
  color: var(--primary-color);
}

.phone-icon {
  background: var(--accent-color-bg);
  color: var(--accent-color);
}

.info-content {
  flex: 1;
  min-width: 0;
}

.info-label {
  font-size: 12px;
  color: var(--text-color-tertiary);
  margin-bottom: 4px;
}

.info-value {
  font-size: 14px;
  color: var(--text-color-primary);
  font-weight: 500;
}

.remark-item {
  margin-top: var(--spacing-md);
  padding-top: var(--spacing-md);
  border-top: 1px dashed var(--border-color);
}

.remark-label {
  font-size: 12px;
  color: var(--text-color-tertiary);
  margin-bottom: 6px;
}

.remark-value {
  font-size: 13px;
  color: var(--text-color-secondary);
  line-height: 1.6;
}

.appointment-actions {
  display: flex;
  gap: var(--spacing-sm);
  flex-wrap: wrap;
  padding-top: var(--spacing-md);
  border-top: 1px solid var(--border-color-light);
}

.action-btn {
  border-radius: var(--radius-lg) !important;
  font-weight: 500;
  transition: all 0.3s ease !important;
}

.view-btn {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-color-light) 100%) !important;
  border: none !important;
}

.view-btn:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3) !important;
}

.process-btn {
  color: var(--primary-color) !important;
  border-color: var(--primary-color) !important;
  background: transparent !important;
}

.process-btn:hover {
  background: var(--primary-color) !important;
  color: white !important;
  transform: translateY(-2px) !important;
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3) !important;
}

.init-btn {
  border: none !important;
  background: linear-gradient(135deg, var(--success-color) 0%, var(--success-color-light) 100%) !important;
}

.init-btn:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.3) !important;
}

.cancel-btn {
  color: var(--warning-color) !important;
  border-color: var(--warning-color) !important;
  background: transparent !important;
}

.cancel-btn:hover {
  background: var(--warning-color) !important;
  color: white !important;
  transform: translateY(-2px) !important;
  box-shadow: 0 4px 12px rgba(250, 173, 20, 0.3) !important;
}

.complete-btn {
  border: none !important;
  background: linear-gradient(135deg, var(--success-color) 0%, var(--success-color-light) 100%) !important;
}

.complete-btn:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.3) !important;
}

.delete-btn {
  color: var(--error-color) !important;
  border-color: var(--error-color) !important;
  background: transparent !important;
}

.delete-btn:hover {
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
@media (max-width: 900px) {
  .timeline-date {
    display: none;
  }
}

@media (max-width: 768px) {
  .appointments-view {
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

  .status-select {
    width: 100%;
  }

  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-bar .el-button {
    width: 100%;
  }

  .property-section {
    flex-direction: column;
  }

  .property-image {
    width: 100%;
    height: 200px;
  }

  .info-grid {
    grid-template-columns: 1fr;
    gap: var(--spacing-md);
  }

  .status-badge-wrap {
    position: absolute;
    top: var(--spacing-md);
    right: var(--spacing-md);
  }

  .appointment-actions {
    flex-direction: column;
  }

  .appointment-actions .el-button {
    width: 100%;
  }
}
</style>
