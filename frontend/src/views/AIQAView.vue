<template>
  <div class="ai-qa-view page-animate">
    <div class="qa-container">
      <!-- 页面标题 -->
      <div class="header-section">
        <div class="header-bg"></div>
        <div class="header-content">
          <div class="title-section">
            <div class="ai-avatar">
              <el-icon :size="28"><ChatDotRound /></el-icon>
            </div>
            <div class="title-wrap">
              <h1 class="title animate-fade-in-up">AI智能问答</h1>
              <p class="subtitle animate-fade-in-up animate-delay-1">房产专业助手 · 24小时在线</p>
            </div>
          </div>
          <el-tag class="header-tag">房产专业助手</el-tag>
        </div>
      </div>

      <!-- 消息列表区域 -->
      <div class="messages-container" ref="messagesContainer">
        <!-- 欢迎消息 -->
        <div v-if="messages.length === 0" class="welcome-message">
          <div class="welcome-icon">
            <el-icon :size="56"><ChatDotRound /></el-icon>
          </div>
          <h2>您好！我是AI房产助手</h2>
          <p>我可以帮您解答关于房产的各种问题</p>
          <div class="quick-questions">
            <p class="quick-title">✨ 快速提问</p>
            <div class="quick-buttons">
              <el-button
                v-for="(question, index) in quickQuestions"
                :key="index"
                class="quick-btn"
                @click="sendQuickQuestion(question)"
              >
                {{ question }}
              </el-button>
            </div>
          </div>
        </div>

        <!-- 对话消息列表 -->
        <div
          v-for="(message, index) in messages"
          :key="index"
          :class="['message-item', message.type]"
        >
          <!-- 用户消息 -->
          <div v-if="message.type === 'user'" class="message-content user-message">
            <div class="message-bubble">
              <p class="message-text">{{ message.content }}</p>
              <span class="message-time">{{ message.time }}</span>
            </div>
            <div class="message-avatar user-avatar">
              <el-icon><User /></el-icon>
            </div>
          </div>

          <!-- AI回复消息 -->
          <div v-else class="message-content ai-message">
            <div class="message-avatar ai-avatar-msg">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="message-bubble ai-bubble">
              <p class="message-text">{{ message.content }}</p>
              <span class="message-time">{{ message.time }}</span>
              
              <!-- 相关问题推荐 -->
              <div v-if="message.relatedQuestions && message.relatedQuestions.length > 0" class="related-questions">
                <p class="related-title">💡 相关问题</p>
                <div class="related-buttons">
                  <el-button
                    v-for="(q, idx) in message.relatedQuestions"
                    :key="idx"
                    class="related-btn"
                    @click="sendQuickQuestion(q)"
                  >
                    {{ q }}
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 加载中提示 -->
        <div v-if="loading" class="message-item ai animate-fade-in-up">
          <div class="message-content ai-message">
            <div class="message-avatar ai-avatar-msg">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="message-bubble loading-bubble">
              <div class="loading-dots">
                <span></span>
                <span></span>
                <span></span>
              </div>
              <span>AI正在思考中...</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-section">
        <div class="input-container">
          <el-input
            v-model="inputQuestion"
            type="textarea"
            :rows="3"
            placeholder="请输入您的问题，例如：买房需要注意什么？"
            :disabled="loading"
            @keydown.enter.prevent="handleSend"
            maxlength="500"
            show-word-limit
            class="input-textarea"
          />
          <div class="input-actions">
            <el-button class="clear-btn" @click="clearMessages" :icon="Delete">清空对话</el-button>
            <el-button
              type="primary"
              class="send-btn btn-shimmer btn-press"
              :icon="Promotion"
              @click="handleSend"
              :loading="loading"
              :disabled="!inputQuestion.trim()"
            >
              {{ loading ? '发送中...' : '发送 (Enter)' }}
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { aiApi } from '@/api/ai'
import type { QAResponse } from '@/types/ai'
import {
  ChatDotRound,
  User,
  Loading,
  Promotion,
  Delete
} from '@element-plus/icons-vue'

// 消息类型定义
interface Message {
  type: 'user' | 'ai'
  content: string
  time: string
  relatedQuestions?: string[]
}

// 与悬浮窗 AI 助手、移动端 AI 助手共用同一存储 key
const AI_CHAT_MESSAGES_KEY = 'ai_chat_messages'

// 消息列表 - 从localStorage加载
const messages = ref<Message[]>([])

// 输入框内容
const inputQuestion = ref('')

// 加载状态
const loading = ref(false)

// 消息容器引用
const messagesContainer = ref<HTMLElement>()

// 快速提问列表
const quickQuestions = ref([
  '买房需要注意什么？',
  '租房如何避免被骗？',
  '三室两厅适合几口人住？',
  '最新的房产政策有哪些？'
])

/**
 * 从localStorage加载历史消息
 */
const loadMessages = () => {
  try {
    const savedMessages = localStorage.getItem(AI_CHAT_MESSAGES_KEY)
    if (savedMessages) {
      messages.value = JSON.parse(savedMessages)
      // 加载后滚动到底部
      nextTick(() => {
        scrollToBottom()
      })
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

/**
 * 发送问题
 */
const handleSend = async () => {
  const question = inputQuestion.value.trim()
  
  if (!question) {
    ElMessage.warning('请输入问题')
    return
  }

  if (question.length < 2) {
    ElMessage.warning('问题内容太短，请输入至少2个字符')
    return
  }

  // 添加用户消息
  messages.value.push({
    type: 'user',
    content: question,
    time: formatTime(new Date())
  })

  // 保存消息
  saveMessages()

  // 清空输入框
  inputQuestion.value = ''

  // 滚动到底部
  await nextTick()
  scrollToBottom()

  // 调用AI接口
  loading.value = true
  
  try {
    const response = await aiApi.askQuestion({ question })
    
    if (response.code === 200 && response.data) {
      const aiResponse: QAResponse = response.data
      
      // 添加AI回复消息
      messages.value.push({
        type: 'ai',
        content: aiResponse.answer,
        time: formatTime(new Date(aiResponse.answerTime)),
        relatedQuestions: aiResponse.relatedQuestions || []
      })
      
      // 保存消息
      saveMessages()
      
      // 滚动到底部
      await nextTick()
      scrollToBottom()
    } else {
      ElMessage.error(response.message || 'AI回答失败')
    }
  } catch (error: any) {
    console.error('AI问答失败:', error)
    ElMessage.error(error.message || 'AI服务异常，请稍后重试')
    
    // 添加错误提示消息
    messages.value.push({
      type: 'ai',
      content: '抱歉，我暂时无法回答您的问题，请稍后再试。',
      time: formatTime(new Date())
    })
    
    // 保存消息
    saveMessages()
    
    await nextTick()
    scrollToBottom()
  } finally {
    loading.value = false
  }
}

/**
 * 发送快速问题
 */
const sendQuickQuestion = (question: string) => {
  inputQuestion.value = question
  handleSend()
}

/**
 * 清空对话
 */
const clearMessages = () => {
  messages.value = []
  // 同时清空localStorage
  localStorage.removeItem(AI_CHAT_MESSAGES_KEY)
  ElMessage.success('对话已清空')
}

/**
 * 滚动到底部
 */
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

/**
 * 格式化时间
 */
const formatTime = (date: Date): string => {
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
}

// 页面加载时的初始化
onMounted(() => {
  // 加载历史消息
  loadMessages()
})
</script>

<style scoped>
.ai-qa-view {
  max-width: 1000px;
  margin: 0 auto;
  padding: var(--spacing-xl) var(--spacing-2xl);
  min-height: 100%;
}

.qa-container {
  height: calc(100vh - 180px);
  min-height: 600px;
  display: flex;
  flex-direction: column;
  border-radius: var(--radius-2xl);
  overflow: hidden;
  background: var(--bg-color);
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border-color-light);
}

/* 头部样式 */
.header-section {
  position: relative;
  padding: var(--spacing-xl) var(--spacing-2xl);
  overflow: hidden;
}

.header-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-color-dark) 50%, var(--primary-color-darker) 100%);
}

.header-bg::before {
  content: '';
  position: absolute;
  top: -60px;
  right: -60px;
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(255, 125, 0, 0.25) 0%, transparent 70%);
  border-radius: 50%;
}

.header-bg::after {
  content: '';
  position: absolute;
  bottom: -40px;
  left: 30px;
  width: 150px;
  height: 150px;
  background: radial-gradient(circle, rgba(64, 150, 255, 0.3) 0%, transparent 70%);
  border-radius: 50%;
}

.header-content {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-section {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.ai-avatar {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-xl);
  background: linear-gradient(135deg, var(--accent-color) 0%, var(--accent-color-light) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 12px rgba(255, 125, 0, 0.35);
}

.title-wrap {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.title {
  font-size: 22px;
  font-weight: 700;
  color: white;
  margin: 0;
  letter-spacing: 0.5px;
}

.subtitle {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.75);
  margin: 0;
  font-weight: 400;
}

.header-tag {
  background: rgba(255, 255, 255, 0.15) !important;
  border: 1px solid rgba(255, 255, 255, 0.3) !important;
  color: white !important;
  backdrop-filter: blur(10px);
  font-weight: 500;
  padding: 0 var(--spacing-md) !important;
  height: 28px !important;
  line-height: 26px !important;
}

/* 消息容器 */
.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-2xl);
  background-color: var(--bg-color-page);
}

/* 欢迎消息 */
.welcome-message {
  text-align: center;
  padding: var(--spacing-3xl) var(--spacing-xl);
  color: var(--text-color-secondary);
}

.welcome-icon {
  width: 100px;
  height: 100px;
  margin: 0 auto var(--spacing-xl);
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-color-bg) 0%, var(--accent-color-bg) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary-color);
  box-shadow: 0 8px 24px rgba(22, 119, 255, 0.1);
}

.welcome-message h2 {
  margin: 0 0 var(--spacing-sm);
  font-size: 24px;
  color: var(--text-color-primary);
  font-weight: 700;
}

.welcome-message p {
  margin: 0 0 var(--spacing-2xl);
  font-size: 15px;
  color: var(--text-color-tertiary);
}

.quick-questions {
  max-width: 600px;
  margin: 0 auto;
  background: var(--bg-color);
  border-radius: var(--radius-xl);
  padding: var(--spacing-xl);
  box-shadow: var(--shadow-base);
  border: 1px solid var(--border-color-light);
}

.quick-title {
  font-size: 14px;
  color: var(--text-color-primary);
  margin: 0 0 var(--spacing-md);
  font-weight: 600;
  text-align: left;
}

.quick-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
  justify-content: flex-start;
}

.quick-btn {
  border-radius: var(--radius-full) !important;
  padding: 0 var(--spacing-md) !important;
  height: 32px !important;
  font-size: 13px !important;
  background: var(--bg-color-light) !important;
  border: 1px solid var(--border-color) !important;
  color: var(--text-color-secondary) !important;
  transition: all 0.25s ease !important;
}

.quick-btn:hover {
  background: var(--primary-color-bg) !important;
  border-color: var(--primary-color) !important;
  color: var(--primary-color) !important;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.15);
}

/* 消息项 */
.message-item {
  margin-bottom: var(--spacing-xl);
  animation: fadeInUp 0.3s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-content {
  display: flex;
  gap: var(--spacing-md);
  max-width: 80%;
}

/* 用户消息 */
.user-message {
  margin-left: auto;
  flex-direction: row-reverse;
}

.user-message .message-bubble {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-color-light) 100%);
  color: white;
  border-bottom-right-radius: var(--radius-sm);
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.25);
}

.user-message .message-time {
  color: rgba(255, 255, 255, 0.7);
}

.message-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 18px;
}

.user-avatar {
  background: linear-gradient(135deg, var(--accent-color) 0%, var(--accent-color-light) 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(255, 125, 0, 0.3);
}

/* AI消息 */
.ai-message {
  margin-right: auto;
}

.ai-bubble {
  background: var(--bg-color);
  color: var(--text-color-primary);
  border: 1px solid var(--border-color-light);
  border-bottom-left-radius: var(--radius-sm);
  box-shadow: var(--shadow-sm);
}

.ai-avatar-msg {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-color-light) 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
}

/* 消息气泡 */
.message-bubble {
  padding: var(--spacing-md) var(--spacing-lg);
  border-radius: var(--radius-lg);
  word-wrap: break-word;
  word-break: break-word;
  max-width: 100%;
}

.message-text {
  margin: 0;
  line-height: 1.7;
  font-size: 14px;
  white-space: pre-wrap;
}

.message-time {
  display: block;
  margin-top: 8px;
  font-size: 11px;
  color: var(--text-color-tertiary);
}

/* 相关问题 */
.related-questions {
  margin-top: var(--spacing-md);
  padding-top: var(--spacing-md);
  border-top: 1px solid var(--border-color-light);
}

.related-title {
  font-size: 12px;
  color: var(--text-color-tertiary);
  margin: 0 0 var(--spacing-sm);
  font-weight: 500;
}

.related-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.related-btn {
  border-radius: var(--radius-full) !important;
  padding: 0 var(--spacing-sm) !important;
  height: 26px !important;
  font-size: 12px !important;
  background: var(--primary-color-bg-light) !important;
  border: 1px solid var(--primary-color-bg) !important;
  color: var(--primary-color) !important;
  transition: all 0.25s ease !important;
}

.related-btn:hover {
  background: var(--primary-color) !important;
  border-color: var(--primary-color) !important;
  color: white !important;
}

/* 加载气泡 */
.loading-bubble {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  background: var(--bg-color);
  color: var(--primary-color);
  border: 1px solid var(--border-color-light);
  box-shadow: var(--shadow-sm);
}

.loading-dots {
  display: flex;
  gap: 4px;
}

.loading-dots span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--primary-color);
  animation: dotBounce 1.4s infinite ease-in-out;
}

.loading-dots span:nth-child(1) {
  animation-delay: -0.32s;
}

.loading-dots span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes dotBounce {
  0%, 80%, 100% {
    transform: scale(0.6);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

/* 输入区域 */
.input-section {
  padding: var(--spacing-lg) var(--spacing-2xl) var(--spacing-xl);
  background: var(--bg-color);
  border-top: 1px solid var(--border-color-light);
}

.input-container {
  background: var(--bg-color-light);
  border-radius: var(--radius-xl);
  padding: var(--spacing-md);
  border: 1px solid var(--border-color-light);
  transition: all 0.3s ease;
}

.input-container:focus-within {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(22, 119, 255, 0.1);
  background: var(--bg-color);
}

.input-textarea :deep(.el-textarea__inner) {
  border: none !important;
  background: transparent !important;
  resize: none !important;
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-color-primary);
}

.input-textarea :deep(.el-textarea__inner:focus) {
  box-shadow: none !important;
}

.input-textarea :deep(.el-input__count) {
  color: var(--text-color-tertiary);
  font-size: 12px;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: var(--spacing-sm);
  gap: var(--spacing-md);
}

.clear-btn {
  border-radius: var(--radius-full) !important;
  padding: 0 var(--spacing-lg) !important;
  transition: all 0.25s ease !important;
}

.clear-btn:hover {
  color: var(--error-color) !important;
  border-color: var(--error-color) !important;
  background: var(--error-color-bg) !important;
}

.send-btn {
  background: linear-gradient(135deg, var(--accent-color) 0%, var(--accent-color-light) 100%) !important;
  border: none !important;
  border-radius: var(--radius-full) !important;
  padding: 0 var(--spacing-xl) !important;
  font-weight: 600;
  transition: all 0.3s ease !important;
}

.send-btn:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 6px 16px rgba(255, 125, 0, 0.35) !important;
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none !important;
  box-shadow: none !important;
}

/* 滚动条样式 */
.messages-container::-webkit-scrollbar {
  width: 6px;
}

.messages-container::-webkit-scrollbar-track {
  background: transparent;
}

.messages-container::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 3px;
}

.messages-container::-webkit-scrollbar-thumb:hover {
  background: var(--text-color-tertiary);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .ai-qa-view {
    padding: var(--spacing-md);
  }

  .qa-container {
    height: calc(100vh - 100px);
    min-height: auto;
    border-radius: var(--radius-xl);
  }

  .header-section {
    padding: var(--spacing-lg);
  }

  .title {
    font-size: 18px;
  }

  .subtitle {
    font-size: 12px;
  }

  .ai-avatar {
    width: 44px;
    height: 44px;
  }

  .messages-container {
    padding: var(--spacing-lg);
  }

  .message-content {
    max-width: 90%;
  }

  .message-avatar {
    width: 36px;
    height: 36px;
    font-size: 16px;
  }

  .welcome-icon {
    width: 80px;
    height: 80px;
  }

  .welcome-icon :deep(.el-icon) {
    font-size: 40px;
  }

  .welcome-message h2 {
    font-size: 20px;
  }

  .input-section {
    padding: var(--spacing-md);
  }

  .input-actions {
    flex-direction: column-reverse;
    align-items: stretch;
  }

  .input-actions .el-button {
    width: 100%;
  }
}
</style>
