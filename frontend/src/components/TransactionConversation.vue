<template>
  <div class="transaction-conversation">
    <el-card>
      <template #header>
        <div class="conversation-header">
          <span>交易沟通</span>
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="unread-badge">
            <el-icon><ChatDotRound /></el-icon>
          </el-badge>
        </div>
      </template>

      <!-- 消息列表 -->
      <div class="messages-container" ref="messagesContainer">
        <div 
          v-for="msg in messages" 
          :key="msg.id"
          :class="['message-item', msg.senderId === currentUserId ? 'message-self' : 'message-other']"
        >
          <div class="message-avatar">
            <el-avatar :size="32">
              <el-icon><User /></el-icon>
            </el-avatar>
          </div>
          <div class="message-body">
            <div class="message-info">
              <span class="sender-name">{{ msg.senderName }}</span>
              <el-tag :type="getRoleTagType(msg.senderRole)" size="small">
                {{ getRoleText(msg.senderRole) }}
              </el-tag>
              <span class="message-time">{{ formatTime(msg.createTime) }}</span>
            </div>
            <div class="message-content">
              {{ msg.messageContent }}
            </div>
          </div>
        </div>
        <div v-if="messages.length === 0" class="no-messages">
          <el-empty description="暂无对话消息，开始沟通吧！" />
        </div>
      </div>

      <!-- 输入框 -->
      <div class="input-container">
        <el-input
          v-model="messageInput"
          type="textarea"
          :rows="3"
          placeholder="输入消息内容... (Enter 发送)"
          maxlength="500"
          show-word-limit
          @keydown.enter.prevent="sendMessage"
        />
        <div class="input-actions">
          <el-button type="primary" @click="sendMessage" :loading="sending" :disabled="!messageInput.trim()">
            <el-icon><Position /></el-icon>
            发送消息
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, User, Position } from '@element-plus/icons-vue'
import { appointmentApi } from '@/api/appointment'

// Props
const props = defineProps<{
  appointmentId: number
  currentUserId: number
  currentUserName: string
  currentUserRole: number
}>()

// 状态
const messages = ref<any[]>([])
const messageInput = ref('')
const sending = ref(false)
const unreadCount = ref(0)
const messagesContainer = ref<HTMLElement | null>(null)

// 角色映射
const getRoleText = (role: number) => {
  const roleMap: Record<number, string> = {
    0: '租客/买家',
    1: '房东',
    2: '管理员'
  }
  return roleMap[role] || '未知'
}

const getRoleTagType = (role: number) => {
  const typeMap: Record<number, string> = {
    0: 'success',
    1: 'warning',
    2: 'danger'
  }
  return typeMap[role] || 'info'
}

// 时间格式化
const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  // 小于1分钟
  if (diff < 60000) {
    return '刚刚'
  }
  // 小于1小时
  if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  }
  // 小于1天
  if (diff < 86400000) {
    return `${Math.floor(diff / 3600000)}小时前`
  }
  // 显示完整时间
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

// 加载消息
const loadMessages = async () => {
  try {
    const res = await appointmentApi.getConversationMessages(props.appointmentId)
    if (res.code === 200 && res.data) {
      messages.value = (res.data as any).data || res.data || []
      
      // 计算未读数
      unreadCount.value = messages.value.filter(
        msg => msg.senderId !== props.currentUserId && msg.isRead === 0
      ).length
      
      scrollToBottom()
      
      // 标记为已读
      if (unreadCount.value > 0) {
        await markAsRead()
      }
    }
  } catch (error) {
    console.error('加载消息失败:', error)
  }
}

// 发送消息
const sendMessage = async () => {
  if (!messageInput.value.trim()) {
    ElMessage.warning('请输入消息内容')
    return
  }
  
  sending.value = true
  try {
    const res = await appointmentApi.sendConversationMessage({
      appointmentId: props.appointmentId,
      senderId: props.currentUserId,
      senderName: props.currentUserName,
      senderRole: props.currentUserRole,
      messageContent: messageInput.value.trim()
    })
    
    if (res.code === 200 || res.data?.success) {
      messageInput.value = ''
      await loadMessages()
      ElMessage.success('消息发送成功')
    } else {
      ElMessage.error(res.message || '发送失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '发送失败')
  } finally {
    sending.value = false
  }
}

// 标记为已读
const markAsRead = async () => {
  try {
    await appointmentApi.markConversationAsRead(props.appointmentId, props.currentUserId)
    unreadCount.value = 0
  } catch (error) {
    console.error('标记已读失败:', error)
  }
}

// 监听appointmentId变化
watch(() => props.appointmentId, (newVal) => {
  if (newVal) {
    loadMessages()
  }
})

// 初始化
onMounted(() => {
  loadMessages()
  
  // 定时刷新消息（每30秒）
  setInterval(() => {
    loadMessages()
  }, 30000)
})

// 暴露刷新方法
defineExpose({
  loadMessages
})
</script>

<style scoped>
.transaction-conversation {
  height: 100%;
}

.conversation-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.unread-badge {
  font-size: 18px;
}

.messages-container {
  height: 400px;
  overflow-y: auto;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 15px;
}

.message-item {
  display: flex;
  margin-bottom: 20px;
  gap: 10px;
}

.message-self {
  flex-direction: row-reverse;
}

.message-other {
  flex-direction: row;
}

.message-avatar {
  flex-shrink: 0;
}

.message-body {
  max-width: 70%;
  display: flex;
  flex-direction: column;
}

.message-self .message-body {
  align-items: flex-end;
}

.message-other .message-body {
  align-items: flex-start;
}

.message-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 5px;
  font-size: 12px;
}

.sender-name {
  font-weight: bold;
  color: #303133;
}

.message-time {
  color: #909399;
  font-size: 11px;
}

.message-content {
  padding: 10px 15px;
  border-radius: 8px;
  word-wrap: break-word;
  white-space: pre-wrap;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.message-self .message-content {
  background: #409eff;
  color: white;
}

.message-other .message-content {
  background: white;
  color: #303133;
}

.no-messages {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.input-container {
  border-top: 1px solid #dcdfe6;
  padding-top: 15px;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}
</style>
