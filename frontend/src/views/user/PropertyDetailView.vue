<template>
  <div class="property-detail-view page-animate">
    <el-card v-loading="loading">
      <!-- 返回按钮 -->
      <div class="back-button">
        <el-button @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回列表
        </el-button>
      </div>

      <div v-if="property" class="property-content">
        <!-- 房源标题和基本信息 -->
        <div class="property-header animate-fade-in-up">
          <h1 class="property-title">{{ property.title }}</h1>
          <div class="property-tags">
            <el-tag type="primary">{{ getPropertyTypeText(property.propertyType) }}</el-tag>
            <el-tag type="success">{{ getTransactionTypeText(property.transactionType) }}</el-tag>
            <el-tag type="info">{{ getStatusText(property.status) }}</el-tag>
          </div>
        </div>

        <!-- 房源图片 -->
        <div class="property-images animate-fade-in-up animate-delay-1">
          <el-carousel height="400px" v-if="imageList.length > 0">
            <el-carousel-item v-for="(image, index) in imageList" :key="index">
              <el-image
                :src="image"
                fit="cover"
                style="width: 100%; height: 100%"
              >
                <template #error>
                  <div class="image-slot">
                    <el-icon><Picture /></el-icon>
                    <span>暂无图片</span>
                  </div>
                </template>
              </el-image>
            </el-carousel-item>
          </el-carousel>
          <div v-else class="no-image">
            <el-icon><Picture /></el-icon>
            <span>暂无图片</span>
          </div>
        </div>

        <!-- 价格和操作 -->
        <div class="price-action-bar animate-elastic-in animate-delay-2">
          <div class="price-section">
            <span class="price">¥{{ formatPrice(property.price, property.transactionType) }}</span>
            <span class="price-unit">{{ property.transactionType !== 1 ? '/月' : '' }}</span>
          </div>
          <div class="action-buttons">
            <el-button
              type="primary"
              size="large"
              class="btn-shimmer btn-press"
              @click="handleFavorite"
              :loading="favoriteLoading"
            >
              <el-icon><Star /></el-icon>
              {{ isFavorited ? '已收藏' : '收藏' }}
            </el-button>
            <el-button
              type="success"
              size="large"
              class="btn-shimmer btn-press"
              @click="showAppointmentDialog"
            >
              <el-icon><Calendar /></el-icon>
              预约看房
            </el-button>
            <el-button
              size="large"
              class="btn-shimmer btn-press"
              @click="handleContact"
            >
              <el-icon><Phone /></el-icon>
              联系房东
            </el-button>
            <el-button
              type="warning"
              size="large"
              class="btn-shimmer btn-press"
              @click="showMessageDialog"
            >
              <el-icon><MessageSquare /></el-icon>
              发消息
            </el-button>
          </div>
        </div>

        <!-- 房源详细信息 -->
        <el-row :gutter="20">
          <el-col :span="16">
            <!-- 基本信息 -->
            <el-card class="info-card animate-fade-in-up" style="animation-delay: 0.3s">
              <template #header>
                <span class="card-title">基本信息</span>
              </template>
              <el-descriptions :column="2" border>
                <el-descriptions-item label="房源面积">
                  {{ property.area }}㎡
                </el-descriptions-item>
                <el-descriptions-item label="户型">
                  {{ property.bedrooms }}室{{ property.bathrooms }}卫
                </el-descriptions-item>
                <el-descriptions-item label="房源类型">
                  {{ getPropertyTypeText(property.propertyType) }}
                </el-descriptions-item>
                <el-descriptions-item label="交易类型">
                  {{ getTransactionTypeText(property.transactionType) }}
                </el-descriptions-item>
                <el-descriptions-item label="所在城市">
                  {{ property.city }}
                </el-descriptions-item>
                <el-descriptions-item label="所在区域">
                  {{ property.district }}
                </el-descriptions-item>
                <el-descriptions-item label="详细地址" :span="2">
                  <el-icon><Location /></el-icon>
                  {{ property.address }}
                </el-descriptions-item>
                <el-descriptions-item label="浏览次数">
                  {{ property.viewCount || 0 }} 次
                </el-descriptions-item>
                <el-descriptions-item label="发布时间">
                  {{ formatDate(property.createTime) }}
                </el-descriptions-item>
              </el-descriptions>
            </el-card>

            <!-- 房源描述 -->
            <el-card class="info-card animate-fade-in-up" style="animation-delay: 0.4s">
              <template #header>
                <span class="card-title">房源描述</span>
              </template>
              <div class="description">
                {{ property.description || '暂无描述' }}
              </div>
            </el-card>
          </el-col>

          <el-col :span="8">
            <!-- 房东信息 -->
            <el-card class="info-card animate-fade-in-up" style="animation-delay: 0.35s">
              <template #header>
                <span class="card-title">房东信息</span>
              </template>
              <div class="landlord-info">
                <el-avatar :size="80" :src="property.landlord?.avatar">
                  <el-icon><UserFilled /></el-icon>
                </el-avatar>
                <div class="landlord-details">
                  <div class="landlord-name">
                    {{ property.landlord?.realName || property.landlord?.username }}
                  </div>
                  <div class="landlord-phone">
                    <el-icon><Phone /></el-icon>
                    {{ property.landlord?.phone || '未提供' }}
                  </div>
                </div>
              </div>
            </el-card>

            <!-- 温馨提示 -->
            <el-card class="info-card animate-fade-in-up" style="animation-delay: 0.45s">
              <template #header>
                <span class="card-title">温馨提示</span>
              </template>
              <div class="tips">
                <el-alert
                  title="看房提示"
                  type="info"
                  :closable="false"
                  show-icon
                >
                  <ul>
                    <li>请提前预约看房时间</li>
                    <li>看房时注意人身安全</li>
                    <li>仔细检查房屋设施</li>
                    <li>谨防虚假房源信息</li>
                  </ul>
                </el-alert>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <el-empty v-else-if="!loading" description="房源不存在或已下架" />
    </el-card>

    <!-- 消息对话弹窗 -->
    <el-dialog
      v-model="messageDialogVisible"
      title="与房东对话"
      width="700px"
      :close-on-click-modal="false"
    >
      <div class="conversation-container">
        <div class="conversation-messages" ref="messagesContainer">
          <div 
            v-for="msg in conversationMessages" 
            :key="msg.id"
            :class="['message-item', msg.userId === getCurrentUserId() ? 'message-self' : 'message-other']"
          >
            <div class="message-header">
              <span class="sender-name">
                <el-tag :type="msg.userId === getCurrentUserId() ? 'success' : 'warning'" size="small">
                  {{ msg.userId === getCurrentUserId() ? '我' : '房东' }}
                </el-tag>
              </span>
              <span class="message-time">{{ formatMessageTime(msg.createTime) }}</span>
            </div>
            <div class="message-content">
              {{ msg.content }}
            </div>
          </div>
          <div v-if="conversationMessages.length === 0" class="no-messages">
            暂无对话记录，发送第一条消息吧！
          </div>
        </div>
        
        <div class="conversation-input">
          <el-input
            v-model="newMessageContent"
            type="textarea"
            :rows="3"
            placeholder="输入消息内容..."
            maxlength="500"
            show-word-limit
            @keydown.enter.prevent="sendMessage"
          />
          <div class="input-actions">
            <span class="input-tip">Enter 快速发送</span>
            <el-button type="primary" @click="sendMessage" :disabled="!newMessageContent.trim()">
              发送消息
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 预约看房对话框 -->
    <el-dialog
      v-model="appointmentDialogVisible"
      title="预约看房"
      width="500px"
    >
      <el-form
        ref="appointmentFormRef"
        :model="appointmentForm"
        :rules="appointmentRules"
        label-width="100px"
      >
        <el-form-item label="预约时间" prop="appointmentTime">
          <el-date-picker
            v-model="appointmentForm.appointmentTime"
            type="datetime"
            placeholder="选择预约时间"
            style="width: 100%"
            :disabled-date="disabledDate"
            :disabled-hours="disabledHours"
          />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input
            v-model="appointmentForm.contactPhone"
            placeholder="请输入联系电话"
          />
        </el-form-item>
        <el-form-item label="备注信息" prop="remark">
          <el-input
            v-model="appointmentForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息（选填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="appointmentDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="submitAppointment"
          :loading="appointmentLoading"
        >
          提交预约
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { propertyApi } from '@/api/property'
import { favoriteApi } from '@/api/favorite'
import { appointmentApi } from '@/api/appointment'
import { aiApi } from '@/api/ai'
import { messageApi } from '@/api/message'
import { useUserStore } from '@/stores/user'
import { PROPERTY_TYPE_TEXTS, TRANSACTION_TYPE_TEXTS, PROPERTY_STATUS_TEXTS } from '@/utils/constants'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 数据
const loading = ref(false)
const favoriteLoading = ref(false)
const appointmentLoading = ref(false)
const property = ref<any>(null)
const isFavorited = ref(false)

// 对话框
const appointmentDialogVisible = ref(false)
const messageDialogVisible = ref(false)

// 预约表单
const appointmentFormRef = ref<FormInstance>()
const appointmentForm = reactive({
  propertyId: 0,
  appointmentTime: '',
  contactPhone: '',
  remark: ''
})

// 消息对话
const messagesContainer = ref<HTMLElement | null>(null)
const conversationMessages = ref<any[]>([])
const newMessageContent = ref('')

const appointmentRules: FormRules = {
  appointmentTime: [
    { required: true, message: '请选择预约时间', trigger: 'change' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 图片列表
const imageList = computed(() => {
  if (!property.value?.images) return []
  return property.value.images.split(',').filter((img: string) => img.trim())
})

/**
 * 加载房源详情
 */
const loadPropertyDetail = async () => {
  try {
    loading.value = true
    const id = Number(route.params.id)

    const response = await propertyApi.getDetail(id)
    if (response.code === 200 && response.data) {
      property.value = response.data

      // 记录用户浏览行为（用于AI推荐）
      if (userStore.isLoggedIn) {
        try {
          await aiApi.recordUserBehavior(id, 'view')
        } catch (err) {
          console.debug('记录浏览行为失败（不影响展示）', err)
        }
      }

      // 检查是否已收藏
      if (userStore.isLoggedIn) {
        checkFavoriteStatus()
      }
    } else {
      ElMessage.error('房源不存在')
    }
  } catch (error: any) {
    console.error('加载房源详情失败:', error)
    ElMessage.error(error.message || '加载房源详情失败')
  } finally {
    loading.value = false
  }
}

/**
 * 检查收藏状态
 */
const checkFavoriteStatus = async () => {
  try {
    const response = await favoriteApi.check(property.value.id)
    if (response.code === 200) {
      isFavorited.value = response.data
    }
  } catch (error) {
    console.error('检查收藏状态失败:', error)
  }
}

/**
 * 收藏/取消收藏
 */
const handleFavorite = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    favoriteLoading.value = true

    if (isFavorited.value) {
      await favoriteApi.remove(property.value.id)
      ElMessage.success('取消收藏成功')
      isFavorited.value = false
      // 记录取消收藏行为（用于AI推荐）
      try {
        await aiApi.recordUserBehavior(property.value.id, 'unfavorite')
      } catch (err) {
        console.debug('记录取消收藏行为失败（不影响展示）', err)
      }
    } else {
      await favoriteApi.add(property.value.id)
      ElMessage.success('收藏成功')
      isFavorited.value = true
      // 记录收藏行为（用于AI推荐）
      try {
        await aiApi.recordUserBehavior(property.value.id, 'favorite')
      } catch (err) {
        console.debug('记录收藏行为失败（不影响展示）', err)
      }
    }
  } catch (error: any) {
    console.error('操作失败:', error)
    ElMessage.error(error.message || '操作失败')
  } finally {
    favoriteLoading.value = false
  }
}

/**
 * 显示预约对话框
 */
const showAppointmentDialog = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  appointmentForm.propertyId = property.value.id
  appointmentForm.appointmentTime = ''
  appointmentForm.contactPhone = userStore.userInfo?.phone || ''
  appointmentForm.remark = ''
  appointmentDialogVisible.value = true
}

/**
 * 提交预约
 */
const submitAppointment = async () => {
  if (!appointmentFormRef.value) return

  try {
    const valid = await appointmentFormRef.value.validate()
    if (!valid) return

    appointmentLoading.value = true
    await appointmentApi.create(appointmentForm)
    ElMessage.success('预约成功，请等待房东确认')
    appointmentDialogVisible.value = false

    // 记录预约行为（用于AI推荐）
    try {
      await aiApi.recordUserBehavior(property.value.id, 'appointment')
    } catch (err) {
      console.debug('记录预约行为失败（不影响展示）', err)
    }
  } catch (error: any) {
    console.error('预约失败:', error)
    ElMessage.error(error.message || '预约失败')
  } finally {
    appointmentLoading.value = false
  }
}

/**
 * 联系房东
 */
const handleContact = () => {
  if (!property.value.landlord?.phone) {
    ElMessage.warning('房东未提供联系方式')
    return
  }

  ElMessageBox.alert(
    `房东电话：${property.value.landlord.phone}`,
    '联系房东',
    {
      confirmButtonText: '确定'
    }
  )
}

/**
 * 获取当前用户ID
 */
const getCurrentUserId = () => {
  const userInfo = userStore.userInfo
  return userInfo?.id || null
}

/**
 * 格式化消息时间
 */
const formatMessageTime = (time: string) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

/**
 * 滚动到消息底部
 */
const scrollToMessagesBottom = () => {
  setTimeout(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  }, 100)
}

/**
 * 加载对话消息
 */
const loadConversationMessages = async () => {
  try {
    const res = await messageApi.getConversationMessages(property.value.id)
    if (res.code === 200 && res.data) {
      conversationMessages.value = res.data
      scrollToMessagesBottom()
    }
  } catch (error) {
    console.error('加载消息失败:', error)
  }
}

/**
 * 显示消息对话对话框
 */
const showMessageDialog = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  newMessageContent.value = ''
  messageDialogVisible.value = true
  
  // 延迟加载消息，等待DOM渲染
  setTimeout(() => {
    loadConversationMessages()
  }, 100)
}

/**
 * 发送消息
 */
const sendMessage = async () => {
  if (!newMessageContent.value.trim()) {
    return
  }
  
  try {
    const res = await messageApi.sendConversationMessage({
      propertyId: property.value.id,
      content: newMessageContent.value
    })
    
    if (res.code === 200) {
      newMessageContent.value = ''
      await loadConversationMessages()
      ElMessage.success('消息发送成功')
    } else {
      ElMessage.error(res.message || '发送失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '发送失败')
  }
}

/**
 * 返回列表
 */
const goBack = () => {
  router.back()
}

/**
 * 禁用过去的日期
 */
const disabledDate = (time: Date) => {
  return time.getTime() < Date.now() - 8.64e7 // 禁用今天之前的日期
}

/**
 * 禁用过去的小时
 */
const disabledHours = () => {
  const hours: number[] = []
  const now = new Date()
  const selectedDate = new Date(appointmentForm.appointmentTime)
  
  // 如果选择的是今天，禁用已过去的小时
  if (selectedDate.toDateString() === now.toDateString()) {
    for (let i = 0; i < now.getHours(); i++) {
      hours.push(i)
    }
  }
  
  return hours
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
  return new Date(date).toLocaleDateString('zh-CN')
}

/**
 * 获取房源类型文本
 */
const getPropertyTypeText = (type: number) => {
  return PROPERTY_TYPE_TEXTS[type] || '未知'
}

/**
 * 获取交易类型文本
 */
const getTransactionTypeText = (type: number) => {
  return TRANSACTION_TYPE_TEXTS[type] || '未知'
}

/**
 * 获取状态文本
 */
const getStatusText = (status: number) => {
  return PROPERTY_STATUS_TEXTS[status] || '未知'
}

// 页面加载时获取数据
onMounted(() => {
  loadPropertyDetail()
})
</script>

<style scoped>
.property-detail-view {
  padding: var(--spacing-lg);
  max-width: 1400px;
  margin: 0 auto;
}

.back-button {
  margin-bottom: var(--spacing-xl);
}

.back-button :deep(.el-button) {
  border-radius: var(--radius-lg);
  padding: 10px 20px;
  transition: all 0.2s ease;
}

.property-content {
  padding: var(--spacing-lg);
}

.property-header {
  margin-bottom: var(--spacing-2xl);
}

.property-title {
  font-size: 30px;
  font-weight: 700;
  color: var(--text-color-primary);
  margin: 0 0 16px 0;
  line-height: 1.3;
}

.property-tags {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.property-tags :deep(.el-tag) {
  border-radius: var(--radius-full);
  padding: 0 14px;
  height: 28px;
  line-height: 26px;
  font-weight: 500;
  border: none;
}

.property-images {
  margin-bottom: var(--spacing-2xl);
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: var(--shadow-base);
}

.property-images :deep(.el-carousel) {
  border-radius: var(--radius-xl);
}

.property-images :deep(.el-carousel__item) {
  border-radius: var(--radius-xl);
  overflow: hidden;
}

.property-images :deep(.el-carousel__indicator) {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.no-image {
  height: 400px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: var(--bg-color-light);
  color: var(--text-color-tertiary);
  font-size: 48px;
  border-radius: var(--radius-xl);
}

.no-image span {
  font-size: 16px;
  margin-top: 12px;
}

.image-slot {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background-color: var(--bg-color-light);
  color: var(--text-color-tertiary);
  font-size: 48px;
}

.image-slot span {
  font-size: 16px;
  margin-top: 12px;
}

.price-action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32px 36px;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-color-dark) 100%);
  border-radius: var(--radius-xl);
  margin-bottom: var(--spacing-2xl);
  box-shadow: var(--shadow-lg);
  position: relative;
  overflow: hidden;
}

.price-action-bar::before {
  content: '';
  position: absolute;
  top: -60px;
  right: -60px;
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(255, 125, 0, 0.25) 0%, transparent 70%);
  border-radius: 50%;
}

.price-section {
  color: white;
  position: relative;
  z-index: 1;
}

.price {
  font-size: 44px;
  font-weight: 700;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.price-unit {
  font-size: 20px;
  margin-left: 8px;
  opacity: 0.9;
}

.action-buttons {
  display: flex;
  gap: 12px;
  position: relative;
  z-index: 1;
}

.action-buttons :deep(.el-button) {
  border-radius: var(--radius-lg);
  padding: 0 24px;
  height: 44px;
  font-weight: 600;
  transition: all 0.25s ease;
}

.action-buttons :deep(.el-button--primary) {
  background: white;
  border-color: white;
  color: var(--primary-color);
}

.action-buttons :deep(.el-button--primary:hover) {
  background: rgba(255, 255, 255, 0.9);
  border-color: rgba(255, 255, 255, 0.9);
  color: var(--primary-color);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.action-buttons :deep(.el-button--success) {
  background: linear-gradient(135deg, var(--accent-color) 0%, var(--accent-color-light) 100%);
  border-color: transparent;
  color: white;
}

.action-buttons :deep(.el-button--success:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 125, 0, 0.4);
}

.info-card {
  margin-bottom: var(--spacing-lg);
  border-radius: var(--radius-xl);
  border: 1px solid var(--border-color-light);
  box-shadow: var(--shadow-sm);
  transition: box-shadow 0.2s ease;
}

.info-card:hover {
  box-shadow: var(--shadow-base);
}

.info-card :deep(.el-card__header) {
  padding: 18px 24px;
  border-bottom: 1px solid var(--border-color-lighter);
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-color-primary);
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-title::before {
  content: '';
  width: 4px;
  height: 18px;
  background: linear-gradient(180deg, var(--primary-color) 0%, var(--accent-color) 100%);
  border-radius: 2px;
}

.info-card :deep(.el-card__body) {
  padding: 24px;
}

.info-card :deep(.el-descriptions) {
  --el-descriptions-item-label-color: var(--text-color-secondary);
  --el-descriptions-text-color: var(--text-color-primary);
}

.info-card :deep(.el-descriptions__label) {
  font-weight: 500;
}

.info-card :deep(.el-descriptions__body) {
  border-radius: var(--radius-md);
  overflow: hidden;
}

.description {
  line-height: 1.8;
  color: var(--text-color-secondary);
  white-space: pre-wrap;
  font-size: 15px;
}

.landlord-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  padding: var(--spacing-lg) 0;
}

.landlord-info :deep(.el-avatar) {
  border: 3px solid var(--primary-color-bg);
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.15);
}

.landlord-details {
  text-align: center;
}

.landlord-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-color-primary);
  margin-bottom: 10px;
}

.landlord-phone {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: var(--text-color-secondary);
  font-size: 14px;
}

.landlord-phone .el-icon {
  color: var(--primary-color);
}

.tips :deep(.el-alert) {
  border-radius: var(--radius-lg);
  border: none;
  background: var(--primary-color-bg-light);
}

.tips :deep(.el-alert__title) {
  color: var(--primary-color);
  font-weight: 600;
}

.tips ul {
  margin: 12px 0 0 0;
  padding-left: 20px;
  line-height: 2;
}

.tips li {
  color: var(--text-color-secondary);
  font-size: 14px;
}

/* 消息对话样式 */
.conversation-container {
  display: flex;
  flex-direction: column;
  height: 500px;
}

.conversation-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: var(--bg-color-page);
  border-radius: var(--radius-lg);
  margin-bottom: var(--spacing-lg);
}

.message-item {
  margin-bottom: var(--spacing-lg);
  display: flex;
  flex-direction: column;
  animation: fadeInUp 0.3s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
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
  margin-bottom: 8px;
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
  color: var(--text-color-tertiary);
  font-size: 11px;
}

.message-content {
  max-width: 65%;
  padding: 14px 18px;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  word-wrap: break-word;
  white-space: pre-wrap;
  line-height: 1.6;
  font-size: 14px;
}

.message-self .message-content {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-color-light) 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

.message-other .message-content {
  background: var(--bg-color);
  color: var(--text-color-primary);
  border-bottom-left-radius: 4px;
  border: 1px solid var(--border-color-light);
}

.no-messages {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-color-tertiary);
  font-size: 14px;
}

.conversation-input {
  border-top: 1px solid var(--border-color-lighter);
  padding-top: var(--spacing-md);
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: var(--spacing-md);
}

.input-tip {
  font-size: 12px;
  color: var(--text-color-tertiary);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .property-detail-view {
    padding: var(--spacing-md);
  }

  .property-content {
    padding: var(--spacing-md);
  }

  .property-title {
    font-size: 22px;
  }

  .price-action-bar {
    flex-direction: column;
    gap: 20px;
    padding: 24px;
  }

  .price {
    font-size: 36px;
  }

  .action-buttons {
    flex-direction: column;
    width: 100%;
  }

  .action-buttons .el-button {
    width: 100%;
  }

  .property-images {
    height: 250px;
  }

  .message-content {
    max-width: 80%;
  }
}
</style>
