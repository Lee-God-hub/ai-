<template>
  <!-- 悬浮AI助手按钮 -->
  <div class="ai-assistant-float" :class="{ expanded: isExpanded }">
    <!-- 悬浮按钮 -->
    <div v-if="!isExpanded" class="float-btn" @click="toggleExpand">
      <div class="float-btn-inner">
        <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
          <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z"/>
          <circle cx="9" cy="10" r="1.5"/>
          <circle cx="15" cy="10" r="1.5"/>
          <path d="M12 16c-1.48 0-2.75-.81-3.45-2h6.9c-.7 1.19-1.97 2-3.45 2z"/>
        </svg>
      </div>
      <span class="float-label">AI助手</span>
      <div class="pulse-ring"></div>
    </div>

    <!-- 展开的对话面板 -->
    <transition name="scale-up">
      <div v-if="isExpanded" class="chat-panel">
        <!-- 面板头部 -->
        <div class="panel-header">
          <div class="panel-title">
            <div class="ai-avatar-sm">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z"/>
                <circle cx="9" cy="10" r="1.5"/>
                <circle cx="15" cy="10" r="1.5"/>
                <path d="M12 16c-1.48 0-2.75-.81-3.45-2h6.9c-.7 1.19-1.97 2-3.45 2z"/>
              </svg>
            </div>
            <div>
              <span class="title-text">AI 智能助手</span>
              <span class="status-dot"></span>
            </div>
          </div>
          <div class="panel-actions">
            <el-button :icon="Refresh" circle size="small" @click="clearMessages" title="清空对话" />
            <el-button :icon="Close" circle size="small" @click="toggleExpand" title="关闭" />
          </div>
        </div>

        <!-- 消息区域 -->
        <div class="messages-area" ref="messagesArea">
          <!-- 欢迎 -->
          <div v-if="messages.length === 0" class="welcome-section">
            <div class="welcome-icon">
              <svg viewBox="0 0 48 48" width="48" height="48" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="24" cy="24" r="20"/>
                <circle cx="18" cy="20" r="2" fill="currentColor"/>
                <circle cx="30" cy="20" r="2" fill="currentColor"/>
                <path d="M16 30c2 3 5 5 8 5s6-2 8-5" stroke-linecap="round"/>
              </svg>
            </div>
            <p class="welcome-text">您好！我是AI房产助手</p>
            <p class="welcome-sub">有什么可以帮您的？</p>
            <div class="quick-actions">
              <button
                v-for="(q, i) in quickQuestions"
                :key="i"
                class="quick-btn"
                @click="sendQuestion(q)"
              >
                {{ q }}
              </button>
            </div>
          </div>

          <!-- 消息列表 -->
          <div v-for="(msg, index) in messages" :key="index" :class="['msg-item', msg.type]">
            <div v-if="msg.type === 'user'" class="msg-user">
              <div class="msg-bubble user-bubble">{{ msg.content }}</div>
            </div>
            <div v-else class="msg-ai">
              <div class="ai-icon-sm">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                  <circle cx="12" cy="12" r="10"/>
                </svg>
              </div>
              <div class="msg-bubble ai-bubble">
                <span>{{ msg.content }}</span>
                <!-- 相关问题 -->
                <div v-if="msg.relatedQuestions && msg.relatedQuestions.length" class="related-qs">
                  <button
                    v-for="(rq, ri) in msg.relatedQuestions"
                    :key="ri"
                    class="related-btn"
                    @click="sendQuestion(rq)"
                  >
                    {{ rq }}
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- 加载动画 -->
          <div v-if="loading" class="msg-item ai animate-fade-in-up">
            <div class="msg-ai">
              <div class="ai-icon-sm">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                  <circle cx="12" cy="12" r="10"/>
                </svg>
              </div>
              <div class="msg-bubble ai-bubble loading-dots">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-area">
          <el-input
            v-model="inputText"
            placeholder="输入您的问题..."
            :disabled="loading"
            @keydown.enter.prevent="handleSend"
            class="chat-input"
            size="default"
          >
            <template #append>
              <el-button
                :icon="Promotion"
                @click="handleSend"
                :loading="loading"
                :disabled="!inputText.trim()"
                class="send-btn btn-shimmer btn-press"
              />
            </template>
          </el-input>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Close, Refresh, Promotion } from '@element-plus/icons-vue'
import { aiApi } from '@/api/ai'
import type { QAResponse } from '@/types/ai'

interface Message {
  type: 'user' | 'ai'
  content: string
  time: string
  relatedQuestions?: string[]
}

// 与侧边栏 AI 助手、移动端 AI 助手共用同一存储 key
const AI_CHAT_MESSAGES_KEY = 'ai_chat_messages'

// 格式化时间
const formatTime = (date: Date): string => {
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
}

const isExpanded = ref(false)
const messages = ref<Message[]>([])
const inputText = ref('')
const loading = ref(false)
const messagesArea = ref<HTMLElement>()

const quickQuestions = [
  '买房注意事项？',
  '租房如何避坑？',
  '最新房产政策'
]

/**
 * 从localStorage加载历史消息
 */
const loadMessages = () => {
  try {
    const savedMessages = localStorage.getItem(AI_CHAT_MESSAGES_KEY)
    if (savedMessages) {
      messages.value = JSON.parse(savedMessages)
      scrollToBottom()
    }
  } catch (error) {
    console.error('加载历史消息失败:', error)
    messages.value = []
  }
}

/**
 * 保存消息到localStorage
 */
const saveMessages = () => {
  try {
    localStorage.setItem(AI_CHAT_MESSAGES_KEY, JSON.stringify(messages.value))
  } catch (error) {
    console.error('保存消息失败:', error)
  }
}

const toggleExpand = () => {
  isExpanded.value = !isExpanded.value
  if (isExpanded.value) {
    // 展开时加载历史消息
    loadMessages()
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesArea.value) {
      messagesArea.value.scrollTop = messagesArea.value.scrollHeight
    }
  })
}

const sendQuestion = (question: string) => {
  inputText.value = question
  handleSend()
}

const handleSend = async () => {
  const question = inputText.value.trim()
  if (!question || loading.value) return

  messages.value.push({ type: 'user', content: question, time: formatTime(new Date()) })
  saveMessages() // 保存消息
  inputText.value = ''
  scrollToBottom()

  loading.value = true
  try {
    const response = await aiApi.askQuestion({ question })
    if (response.code === 200 && response.data) {
      const data: QAResponse = response.data
      messages.value.push({
        type: 'ai',
        content: data.answer,
        time: formatTime(new Date()),
        relatedQuestions: data.relatedQuestions || []
      })
    } else {
      messages.value.push({
        type: 'ai',
        content: response.message || '抱歉，暂时无法回答。',
        time: formatTime(new Date())
      })
    }
    saveMessages() // 保存消息
  } catch (error: any) {
    messages.value.push({
      type: 'ai',
      content: '网络异常，请稍后重试。',
      time: formatTime(new Date())
    })
    saveMessages() // 保存消息
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

const clearMessages = () => {
  messages.value = []
  // 同时清空localStorage
  localStorage.removeItem(AI_CHAT_MESSAGES_KEY)
  ElMessage.success('对话已清空')
}
</script>

<style scoped>
.ai-assistant-float {
  position: fixed;
  bottom: 32px;
  right: 32px;
  z-index: 2000;
}

/* 悬浮按钮 */
.float-btn {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(135deg, #ff7d00 0%, #ff9a2e 100%);
  color: white;
  padding: 12px 20px;
  border-radius: 28px;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(255, 125, 0, 0.4);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.float-btn:hover {
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 6px 28px rgba(255, 125, 0, 0.5);
}

.float-btn-inner {
  display: flex;
  align-items: center;
}

.float-label {
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.pulse-ring {
  position: absolute;
  top: -4px;
  right: -4px;
  width: 12px;
  height: 12px;
  background: #52c41a;
  border-radius: 50%;
  border: 2px solid white;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.3); opacity: 0.7; }
  100% { transform: scale(1); opacity: 1; }
}

/* 对话面板 */
.chat-panel {
  width: 380px;
  height: 520px;
  background: var(--bg-color);
  border-radius: var(--radius-xl);
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid var(--border-color-light);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-color-dark) 100%);
  color: white;
  position: relative;
  overflow: hidden;
}

.panel-header::before {
  content: '';
  position: absolute;
  top: -30px;
  right: -30px;
  width: 100px;
  height: 100px;
  background: radial-gradient(circle, rgba(255, 125, 0, 0.3) 0%, transparent 70%);
  border-radius: 50%;
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 10px;
  position: relative;
  z-index: 1;
}

.ai-avatar-sm {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(255,255,255,0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
}

.title-text {
  font-size: 15px;
  font-weight: 600;
}

.status-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  background: #52c41a;
  border-radius: 50%;
  margin-left: 6px;
  vertical-align: middle;
  box-shadow: 0 0 6px rgba(82, 196, 26, 0.6);
}

.panel-actions :deep(.el-button) {
  color: white;
  border-color: rgba(255,255,255,0.3);
  background: rgba(255,255,255,0.1);
  transition: all 0.2s;
}

.panel-actions :deep(.el-button:hover) {
  background: rgba(255,255,255,0.25);
  border-color: rgba(255,255,255,0.4);
}

/* 消息区域 */
.messages-area {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: var(--bg-color-page);
}

.welcome-section {
  text-align: center;
  padding: 30px 16px;
  color: var(--text-color-tertiary);
}

.welcome-icon {
  color: var(--primary-color);
  margin-bottom: 12px;
}

.welcome-text {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-color-primary);
  margin: 0 0 4px;
}

.welcome-sub {
  font-size: 13px;
  color: var(--text-color-tertiary);
  margin: 0 0 20px;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.quick-btn {
  padding: 6px 14px;
  border: 1px solid var(--border-color-light);
  border-radius: var(--radius-full);
  background: var(--bg-color);
  color: var(--text-color-secondary);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-btn:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
  background: var(--primary-color-bg-light);
}

/* 消息气泡 */
.msg-item {
  margin-bottom: 12px;
  animation: fadeInUp 0.3s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.msg-user {
  display: flex;
  justify-content: flex-end;
}

.msg-ai {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.ai-icon-sm {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--accent-color) 0%, var(--accent-color-light) 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 4px;
  box-shadow: 0 2px 6px rgba(255, 125, 0, 0.3);
}

.msg-bubble {
  max-width: 80%;
  padding: 10px 14px;
  border-radius: var(--radius-lg);
  font-size: 13px;
  line-height: 1.5;
  word-break: break-word;
}

.user-bubble {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-color-light) 100%);
  color: white;
  border-bottom-right-radius: 4px;
  box-shadow: 0 2px 8px rgba(22, 119, 255, 0.2);
}

.ai-bubble {
  background: var(--bg-color);
  color: var(--text-color-primary);
  border: 1px solid var(--border-color-light);
  border-bottom-left-radius: 4px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
}

.related-qs {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid var(--border-color-lighter);
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.related-btn {
  padding: 4px 10px;
  border: 1px solid var(--border-color-light);
  border-radius: var(--radius-full);
  background: var(--bg-color-light);
  font-size: 11px;
  color: var(--text-color-secondary);
  cursor: pointer;
  transition: all 0.2s;
}

.related-btn:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
  background: var(--primary-color-bg-light);
}

/* 加载动画 */
.loading-dots {
  display: flex;
  gap: 4px;
  padding: 12px 16px;
}

.loading-dots span {
  width: 6px;
  height: 6px;
  background: var(--text-color-tertiary);
  border-radius: 50%;
  animation: dot-bounce 1.4s infinite ease-in-out both;
}

.loading-dots span:nth-child(1) { animation-delay: -0.32s; }
.loading-dots span:nth-child(2) { animation-delay: -0.16s; }

@keyframes dot-bounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

/* 输入区域 */
.input-area {
  padding: 12px 16px;
  border-top: 1px solid var(--border-color-lighter);
  background: var(--bg-color);
}

.chat-input :deep(.el-input-group__append) {
  padding: 0;
}

.send-btn {
  background: linear-gradient(135deg, var(--accent-color) 0%, var(--accent-color-light) 100%) !important;
  border-color: transparent !important;
  color: white !important;
  border-radius: 0 var(--radius-md) var(--radius-md) 0;
  padding: 8px 14px;
  transition: all 0.2s !important;
}

.send-btn:hover {
  box-shadow: 0 2px 8px rgba(255, 125, 0, 0.3) !important;
}

/* 过渡动画 */
.scale-up-enter-active {
  animation: scaleUp 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.scale-up-leave-active {
  animation: scaleUp 0.2s cubic-bezier(0.4, 0, 0.2, 1) reverse;
}

@keyframes scaleUp {
  from {
    opacity: 0;
    transform: scale(0.8) translateY(20px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

/* 响应式 */
@media (max-width: 480px) {
  .chat-panel {
    width: calc(100vw - 32px);
    height: calc(100vh - 100px);
    position: fixed;
    bottom: 16px;
    right: 16px;
  }

  .ai-assistant-float {
    bottom: 16px;
    right: 16px;
  }
}
</style>
