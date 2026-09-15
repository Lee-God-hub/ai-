<template>
  <div class="user-messages page-animate">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的对话</span>
        </div>
      </template>
      
      <div class="conversation-list">
        <div 
          v-for="conversation in conversations" 
          :key="conversation.id"
          class="conversation-item"
          @click="openConversation(conversation)"
        >
          <div class="conversation-info">
            <div class="property-title">
              <el-icon><House /></el-icon>
              {{ conversation.property?.title || '房源信息' }}
            </div>
            <div class="last-message">
              {{ conversation.lastMessageContent || '暂无消息' }}
            </div>
          </div>
          <div class="conversation-meta">
            <div class="message-time">{{ formatTime(conversation.lastMessageTime || conversation.createTime) }}</div>
            <el-badge 
              v-if="conversation.unreadCount > 0" 
              :value="conversation.unreadCount" 
              class="unread-badge"
            />
            <el-tag 
              :type="conversation.isRead === 1 ? 'success' : 'warning'" 
              size="small"
            >
              {{ conversation.isRead === 1 ? '已读' : '未读' }}
            </el-tag>
          </div>
        </div>
        <el-empty v-if="conversations.length === 0 && !loading" description="暂无对话，访问房源详情页可以向房东留言" />
      </div>
      
      <el-pagination
        v-if="total > 0"
        v-model:current-page="filters.pageNum"
        v-model:page-size="filters.pageSize"
        :total="total"
        layout="total, prev, pager, next"
        style="margin-top: 20px; justify-content: center"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { House } from '@element-plus/icons-vue'
import { messageApi } from '@/api/message'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const conversations = ref<any[]>([])
const total = ref(0)

const filters = reactive({
  pageNum: 1,
  pageSize: 10
})

const formatTime = (time: string) => {
  if (!time) return '-'
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else if (days === 1) {
    return '昨天'
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}

const loadData = async () => {
  try {
    loading.value = true
    const res = await messageApi.getUserMessageList(filters)
    if (res.code === 200 && res.data) {
      // 按房源分组对话
      const grouped = new Map()
      res.data.records.forEach((msg: any) => {
        const key = msg.propertyId
        if (!grouped.has(key)) {
          grouped.set(key, {
            id: msg.id,
            propertyId: msg.propertyId,
            property: msg.property,
            lastMessageContent: msg.content,
            lastMessageTime: msg.createTime,
            isRead: msg.isRead,
            unreadCount: msg.isRead === 0 ? 1 : 0,
            createTime: msg.createTime
          })
        } else {
          const existing = grouped.get(key)
          if (new Date(msg.createTime) > new Date(existing.lastMessageTime)) {
            existing.lastMessageContent = msg.content
            existing.lastMessageTime = msg.createTime
          }
          if (msg.isRead === 0) {
            existing.unreadCount++
          }
        }
      })
      conversations.value = Array.from(grouped.values())
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const openConversation = (conversation: any) => {
  router.push(`/messages/conversation/${conversation.propertyId}`)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.user-messages {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: bold;
}

.conversation-list {
  min-height: 400px;
}

.conversation-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  transition: background-color 0.3s;
}

.conversation-item:hover {
  background-color: #f5f7fa;
}

.conversation-item:last-child {
  border-bottom: none;
}

.conversation-info {
  flex: 1;
  min-width: 0;
}

.property-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.last-message {
  color: #909399;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.conversation-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  margin-left: 15px;
}

.message-time {
  color: #909399;
  font-size: 12px;
  white-space: nowrap;
}

.unread-badge {
  margin-bottom: 5px;
}
</style>
