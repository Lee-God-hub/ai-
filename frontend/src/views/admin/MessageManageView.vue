<template>
  <div class="message-manage-view page-animate">
    <div class="page-header">
      <div class="page-title">
        <span class="title-bar"></span>
        <h2>留言管理</h2>
      </div>
      <el-button type="primary" class="refresh-btn" @click="loadData">
        <el-icon><Refresh /></el-icon>
        刷新数据
      </el-button>
    </div>

    <div class="stats-section">
      <div class="stat-card">
        <div class="stat-icon icon-total">
          <el-icon><ChatDotRound /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ totalMessages }}</div>
          <div class="stat-label">留言总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon icon-unread">
          <el-icon><Bell /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ unreadCount }}</div>
          <div class="stat-label">未读留言</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon icon-read">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ readCount }}</div>
          <div class="stat-label">已读留言</div>
        </div>
      </div>
    </div>

    <div class="content-card">
      <div class="filter-bar">
        <el-select v-model="filters.isRead" placeholder="状态筛选" clearable class="filter-select" @change="handleFilterChange">
          <el-option label="未读" :value="0" />
          <el-option label="已读" :value="1" />
        </el-select>
        <el-button type="primary" class="btn-search" @click="loadData">
          <el-icon><Search /></el-icon>
          查询
        </el-button>
      </div>

      <el-table :data="messages" v-loading="loading" stripe class="message-table">
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
        <el-table-column label="房源" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="property-title">{{ row.property?.title || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="留言内容" min-width="250" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="message-content">{{ row.content }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :class="row.isRead === 1 ? 'tag-read' : 'tag-unread'" size="small">
              {{ row.isRead === 1 ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">
            <span class="time-text">{{ formatDate(row.createTime) }}</span>
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
      <el-empty v-if="messages.length === 0 && !loading" description="暂无留言" class="empty-state" />
      <div class="pagination-wrapper">
        <el-pagination
          v-if="total > 0"
          v-model:current-page="filters.pageNum"
          v-model:page-size="filters.pageSize"
          :page-sizes="[10, 20, 50]"
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
import { Refresh, Search, ChatDotRound, Bell, CircleCheck, User, Delete } from '@element-plus/icons-vue'
import { messageApi } from '@/api/message'
import type { Message } from '@/types'

const loading = ref(false)
const messages = ref<Message[]>([])
const total = ref(0)
const allMessages = ref<any[]>([])

const filters = reactive({
  pageNum: 1,
  pageSize: 10,
  isRead: null as number | null
})

const totalMessages = computed(() => allMessages.value.length)
const unreadCount = computed(() => allMessages.value.filter(m => m.isRead === 0).length)
const readCount = computed(() => allMessages.value.filter(m => m.isRead === 1).length)

const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

const handleFilterChange = () => {
  filters.pageNum = 1
}

const loadData = async () => {
  try {
    loading.value = true
    const res = await messageApi.getAdminMessageList({
      pageNum: 1,
      pageSize: 1000
    })
    if (res.code === 200 && res.data) {
      let list = Array.isArray(res.data) ? res.data : (res.data.records || [])
      allMessages.value = list
      
      if (filters.isRead !== null) {
        list = list.filter((item: any) => item.isRead === filters.isRead)
      }
      
      total.value = list.length
      
      const start = (filters.pageNum - 1) * filters.pageSize
      messages.value = list.slice(start, start + filters.pageSize)
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
    await ElMessageBox.confirm('确定删除该留言吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    row.deleting = true
    await messageApi.deleteMessage(row.id)
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
.message-manage-view {
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
  grid-template-columns: repeat(3, 1fr);
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

.icon-unread {
  background: linear-gradient(135deg, var(--warning-color) 0%, var(--warning-color-light) 100%);
}

.icon-read {
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

.message-table {
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.message-table :deep(.el-table__header th) {
  font-weight: 600;
  color: var(--text-color-primary);
  background: var(--bg-color-light);
}

.message-table :deep(.el-table__row:hover > td) {
  background-color: var(--primary-color-bg-light) !important;
}

.message-table :deep(.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell) {
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

.message-content {
  color: var(--text-color-regular);
  line-height: 1.5;
}

.tag-unread {
  background: var(--warning-color-bg);
  color: var(--warning-color);
  border-color: var(--warning-color-bg);
}

.tag-read {
  background: var(--success-color-bg);
  color: var(--success-color);
  border-color: var(--success-color-bg);
}

.time-text {
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
  .message-manage-view {
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
