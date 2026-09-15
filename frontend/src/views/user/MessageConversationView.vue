<template>
  <div class="message-conversation page-animate">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-button icon="ArrowLeft" @click="goBack">返回</el-button>
          <div class="property-info">
            <span class="property-title">{{ propertyTitle }}</span>
            <el-tag type="success" v-if="landlordInfo">房东：{{ landlordInfo }}</el-tag>
          </div>
        </div>
      </template>

      <div class="conversation-container">
        <div class="conversation-messages" ref="messagesContainer">
          <div 
            v-for="msg in messages" 
            :key="msg.id"
            :class="['message-item', msg.userId === getCurrentUserId() ? 'message-self' : 'message-other']"
          >
            <div class="message-header">
              <span class="sender-name">
                <el-tag :type="msg.userId === getCurrentUserId() ? 'success' : 'warning'" size="small">
                  {{ msg.userId === getCurrentUserId() ? '我' : '房东' }}
                </el-tag>
              </span>
              <span class="message-time">{{ formatTime(msg.createTime) }}</span>
            </div>
            <div class="message-content">
              {{ msg.content }}
            </div>
            <!-- 显示房东回复 -->
            <div v-if="msg.reply" class="message-reply">
              <div class="reply-header">
                <el-tag type="warning" size="small">房东回复</el-tag>
              </div>
              <div class="reply-content">{{ msg.reply }}</div>
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
            placeholder="输入消息内容..."
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
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { messageApi } from '@/api/message'

const route = useRoute()
const router = useRouter()

const propertyId = ref(Number(route.params.propertyId))
const propertyTitle = ref('')
const landlordInfo = ref('')
const messages = ref<any[]>([])
const newMessage = ref('')
const messagesContainer = ref<HTMLElement | null>(null)

const currentUserRole = computed(() => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (!userInfoStr) return 0
  const userInfo = JSON.parse(userInfoStr)
  return userInfo.role || 0
})

const getCurrentUserId = () => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (!userInfoStr) return null
  const userInfo = JSON.parse(userInfoStr)
  return userInfo.id
}

const formatTime = (time: string) => {
  return new Date(time).toLocaleString('zh-CN')
}

const goBack = () => {
  router.back()
}

const loadMessages = async () => {
  try {
    const res = await messageApi.getConversationMessages(propertyId.value)
    if (res.code === 200 && res.data) {
      messages.value = res.data
      
      // 从第一条消息获取房源信息
      if (messages.value.length > 0) {
        // 可以通过propertyId查询房源信息，这里暂时简化处理
        propertyTitle.value = '房源对话'
      }
      
      setTimeout(() => {
        scrollToBottom()
      }, 100)
    }
  } catch (error) {
    console.error('加载消息失败:', error)
  }
}

const sendMessage = async () => {
  if (!newMessage.value.trim()) {
    return
  }
  
  try {
    const res = await messageApi.sendConversationMessage({
      propertyId: propertyId.value,
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
.message-conversation {
  height: 100%;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.property-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.property-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
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
  background: #f5f7fa;
  border-radius: 8px;
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
  margin-bottom: 5px;
  font-size: 12px;
}

.sender-name {
  display: flex;
  align-items: center;
  gap: 5px;
  font-weight: bold;
  color: #303133;
}

.message-time {
  color: #909399;
  font-size: 11px;
}

.message-content {
  max-width: 60%;
  padding: 12px 16px;
  border-radius: 8px;
  background: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  word-wrap: break-word;
  white-space: pre-wrap;
}

.message-self .message-content {
  background: #409eff;
  color: white;
}

.message-other .message-content {
  background: white;
  color: #303133;
}

.message-reply {
  max-width: 60%;
  margin-top: 8px;
  padding: 10px;
  background: #fff3e0;
  border-left: 3px solid #ff9800;
  border-radius: 4px;
}

.reply-header {
  margin-bottom: 5px;
}

.reply-content {
  color: #606266;
  font-size: 13px;
}

.no-messages {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
  font-size: 14px;
}

.conversation-input {
  border-top: 1px solid #dcdfe6;
  padding-top: 15px;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.input-tip {
  font-size: 12px;
  color: #909399;
}
</style>
