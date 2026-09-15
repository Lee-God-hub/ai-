<template>
  <div class="landlord-messages page-animate">
    <div class="page-header">
      <div class="section-title">
        <span class="title-bar"></span>
        <h1>我的消息</h1>
      </div>
      <el-tag v-if="unreadCount > 0" type="warning" effect="light" class="unread-tag">
        <el-icon><Bell /></el-icon>
        {{ unreadCount }} 条未读
      </el-tag>
    </div>

    <div class="table-card">
      <el-table :data="messages" v-loading="loading" class="message-table">
        <el-table-column label="用户" width="160">
          <template #default="{ row }">
            <div class="user-cell">
              <div class="user-avatar">
                {{ getAvatarText(row.user?.username || row.user?.realName) }}
              </div>
              <div class="user-info">
                <div class="user-name">{{ row.user?.username || row.user?.realName || '未知用户' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="房源" min-width="200">
          <template #default="{ row }">
            <div class="property-cell">
              <el-icon><House /></el-icon>
              <span>{{ row.property?.title || '未知房源' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="留言内容" min-width="280" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="content-cell">
              <span :class="{ 'unread-content': row.isRead !== 1 }">{{ row.content }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isRead === 1 ? 'success' : 'warning'" effect="light" round>
              {{ row.isRead === 1 ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            <div class="time-cell">
              <el-icon><Clock /></el-icon>
              <span>{{ formatDate(row.createTime) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleReply(row)"
              class="action-btn reply-btn"
            >
              <el-icon><ChatDotRound /></el-icon>
              回复
            </el-button>
            <el-button
              v-if="row.isRead !== 1"
              type="success"
              size="small"
              @click="handleRead(row)"
              :loading="row.reading"
              class="action-btn read-btn"
            >
              <el-icon><Check /></el-icon>
              已读
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(row)"
              :loading="row.deleting"
              class="action-btn delete-btn"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="messages.length === 0 && !loading" description="暂无消息" class="empty-state" />
      <el-pagination
        v-if="total > 0"
        v-model:current-page="filters.pageNum"
        v-model:page-size="filters.pageSize"
        :total="total"
        layout="total, prev, pager, next"
        class="pagination"
        @size-change="loadData"
        @current-change="loadData"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  Bell,
  House,
  Clock,
  Check,
  Delete,
  ChatDotRound
} from '@element-plus/icons-vue'
import { messageApi } from '@/api/message'
import type { Message } from '@/types'

const router = useRouter()

const loading = ref(false)
const messages = ref<Message[]>([])
const total = ref(0)

const filters = reactive({
  pageNum: 1,
  pageSize: 10
})

const unreadCount = computed(() =>
  messages.value.filter(m => m.isRead !== 1).length
)

const getAvatarText = (name: string) => {
  if (!name) return 'U'
  return name.charAt(0).toUpperCase()
}

const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

const loadData = async () => {
  try {
    loading.value = true
    const res = await messageApi.getLandlordMessageList(filters)
    if (res.code === 200 && res.data) {
      messages.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const handleRead = async (row: any) => {
  if (!row.id) return
  try {
    row.reading = true
    await messageApi.markAsRead(row.id)
    ElMessage.success('标记成功')
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    row.reading = false
  }
}

const handleReply = (row: Message) => {
  if (!row.propertyId || !row.userId) {
    ElMessage.warning('消息信息不完整，无法进入对话')
    return
  }
  router.push(`/landlord/messages/conversation/${row.propertyId}/${row.userId}`)
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
.landlord-messages {
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

.unread-tag {
  display: flex;
  align-items: center;
  gap: 6px;
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

.message-table {
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.message-table :deep(.el-table__header th) {
  background: var(--bg-color-light) !important;
  font-weight: 600 !important;
  color: var(--text-color-primary) !important;
  font-size: 14px;
}

.message-table :deep(.el-table__body tr) {
  transition: background-color 0.2s ease;
}

.message-table :deep(.el-table__body tr:hover > td) {
  background-color: var(--primary-color-bg-light) !important;
}

.message-table :deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background-color: var(--bg-color-light);
}

.message-table :deep(.el-table__body td) {
  border-bottom: 1px solid var(--border-color-lighter);
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-color-light) 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
  flex-shrink: 0;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-weight: 500;
  color: var(--text-color-primary);
  font-size: 14px;
}

.property-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-color-regular);
  font-size: 14px;
}

.property-cell .el-icon {
  color: var(--primary-color);
  font-size: 16px;
  flex-shrink: 0;
}

.content-cell {
  font-size: 14px;
  color: var(--text-color-secondary);
  line-height: 1.5;
}

.unread-content {
  font-weight: 500;
  color: var(--text-color-primary);
}

.time-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-color-secondary);
}

.time-cell .el-icon {
  color: var(--primary-color);
  font-size: 14px;
  flex-shrink: 0;
}

.action-btn {
  border-radius: var(--radius-md) !important;
  transition: all 0.25s ease !important;
  padding: 6px 12px !important;
}

.action-btn:hover {
  transform: translateY(-1px) !important;
}

.reply-btn:hover {
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3) !important;
}

.read-btn:hover {
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3) !important;
}

.delete-btn:hover {
  box-shadow: 0 4px 12px rgba(255, 77, 79, 0.3) !important;
}

.empty-state {
  padding: var(--spacing-3xl) 0;
}

.pagination {
  margin-top: var(--spacing-lg);
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .landlord-messages {
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

  .table-card {
    padding: var(--spacing-md);
    border-radius: var(--radius-xl);
  }
}
</style>
