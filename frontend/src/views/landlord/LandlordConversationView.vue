<template>
  <div class="landlord-conversation page-animate">
    <div class="page-header">
      <div class="section-title">
        <span class="title-bar"></span>
        <h1>对话详情</h1>
      </div>
      <el-button @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>

    <el-card class="conversation-card">
      <template #header>
        <div class="card-header">
          <div class="property-info">
            <el-icon><House /></el-icon>
            <span class="property-title">{{ propertyTitle || '房源对话' }}</span>
          </div>
          <el-tag type="warning" v-if="tenantName">租客：{{ tenantName }}</el-tag>
        </div>
      </template>

      <div class="conversation-container">
        <div class="conversation-messages" ref="messagesContainer">
          <div
            v-for="msg in messages"
            :key="msg.id"
            :class="['message-item', isSelf(msg) ? 'message-self' : 'message-other']"
          >
            <div class="message-header">
              <span class="sender-name">
                <el-tag :type="isSelf(msg) ? 'success' : 'warning'" size="small">
                  {{ isSelf(msg) ? '我' : '租客' }}
                </el-tag>
              </span>
              <span class="message-time">{{ formatTime(msg.createTime) }}</span>
            </div>
            <div class="message-content">
              {{ msg.content }}
            </div>
          </div>
          <div v-if="messages.length === 0" class="no-messages">
            暂无对话记录，发送第一条消息吧！
          </div>
        </div>

        <div class="conversation-input">
          <el-input
            v-model="newMessage"
            type="textarea"
            :rows="3"
            placeholder="输入回复内容..."
            maxlength="500"
            show-word-limit
            @keydown.enter.prevent="sendMessage"
          />
          <div class="input-actions">
            <span class="input-tip">Enter 快速发送</span>
            <el-button type="primary" @click="sendMessage" :disabled="!newMessage.trim()">
              发送消息
            </el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, House } from '@element-plus/icons-vue'
import { messageApi } from '@/api/message'
import type { Message } from '@/types'

const route = useRoute()
const router = useRouter()

const propertyId = ref(Number(route.params.propertyId))
const userId = ref(Number(route.params.userId))
const propertyTitle = ref('')
const tenantName = ref('')
const messages = ref<Message[]>([])
const newMessage = ref('')
const messagesContainer = ref<HTMLElement | null>(null)

const getCurrentUserId = () => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (!userInfoStr) return null
  const userInfo = JSON.parse(userInfoStr)
  return userInfo.id
}

const isSelf = (msg: Message) => {
  return msg.senderRole === 1 || msg.userId === getCurrentUserId()
}

const formatTime = (time: string | undefined) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const goBack = () => {
  router.back()
}

const loadMessages = async () => {
  try {
    const res = await messageApi.getLandlordConversationMessages(propertyId.value, userId.value)
    if (res.code === 200 && res.data) {
      messages.value = res.data

      if (messages.value.length > 0) {
        const firstMsg = messages.value[0]
        propertyTitle.value = firstMsg.property?.title || '房源对话'
        tenantName.value = firstMsg.user?.username || firstMsg.user?.realName || '租客'
      }

      setTimeout(() => {
        scrollToBottom()
      }, 100)
    }
  } catch (error) {
    console.error('加载消息失败:', error)
    ElMessage.error('加载消息失败')
  }
}

const sendMessage = async () => {
  if (!newMessage.value.trim()) {
    return
  }

  try {
    const res = await messageApi.landlordSendConversationMessage({
      propertyId: propertyId.value,
      userId: userId.value,
      content: newMessage.value
    })

    if (res.code === 200) {
      newMessage.value = ''
      await loadMessages()
      ElMessage.success('消息发送成功')
    } else {
      ElMessage.error(res.message || '发送失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '发送失败')
  }
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

onMounted(() => {
  loadMessages()
})
</script>

<style scoped>
.landlord-conversation {
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

.conversation-card {
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-base);
  border: 1px solid var(--border-color-light);
}

.conversation-card :deep(.el-card__header) {
  background: var(--bg-color-light);
  border-bottom: 1px solid var(--border-color-lighter);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.property-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.property-info .el-icon {
  color: var(--primary-color);
  font-size: 18px;
}

.property-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-color-primary);
}

.conversation-container {
  display: flex;
  flex-direction: column;
  height: 600px;
}

.conversation-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: var(--bg-color-page);
  border-radius: var(--radius-lg);
  margin-bottom: 20px;
}

.message-item {
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
}

.message-self {
  align-items: flex-end;
}

.message-other {
  align-items: flex-start;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
  font-size: 12px;
}

.sender-name {
  display: flex;
  align-items: center;
  gap: 5px;
  font-weight: 600;
  color: var(--text-color-primary);
}

.message-time {
  color: var(--text-color-placeholder);
  font-size: 12px;
}

.message-content {
  max-width: 60%;
  padding: 12px 16px;
  border-radius: var(--radius-lg);
  background: var(--bg-color);
  box-shadow: var(--shadow-xs);
  color: var(--text-color-primary);
  word-wrap: break-word;
  white-space: pre-wrap;
  line-height: 1.5;
}

.message-self .message-content {
  background: var(--primary-color);
  color: var(--text-color-inverse);
}

.message-other .message-content {
  background: var(--bg-color);
  border: 1px solid var(--border-color-light);
}

.no-messages {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-color-placeholder);
  font-size: 14px;
}

.conversation-input {
  border-top: 1px solid var(--border-color-lighter);
  padding-top: 20px;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.input-tip {
  font-size: 12px;
  color: var(--text-color-placeholder);
}

@media (max-width: 768px) {
  .landlord-conversation {
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

  .conversation-container {
    height: 500px;
  }

  .message-content {
    max-width: 80%;
  }
}
</style>
