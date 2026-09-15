<template>
  <div class="profile-view page-animate">
    <div class="page-header">
      <div class="page-title">
        <span class="title-bar"></span>
        <h1>个人中心</h1>
      </div>
    </div>

    <!-- 用户信息卡片 -->
    <div class="profile-card">
      <div class="profile-header">
        <div class="profile-bg"></div>
        <div class="profile-content">
          <div class="avatar-wrap">
            <div class="avatar">
              <el-icon :size="48"><User /></el-icon>
            </div>
            <div class="avatar-badge" :class="'role-' + userStore.userRole">
              {{ roleBadge }}
            </div>
          </div>
          <div class="user-info">
            <h2 class="username">{{ userStore.username }}</h2>
            <p class="user-role">
              <el-tag :class="['role-tag', 'role-tag-' + userStore.userRole]">{{ roleText }}</el-tag>
            </p>
          </div>
        </div>
      </div>

      <div class="profile-body">
        <!-- 基本信息 -->
        <div class="info-section">
          <h3 class="section-title">
            <span class="section-bar"></span>
            基本信息
          </h3>
          <div class="info-grid">
            <div class="info-card">
              <div class="info-icon user-icon">
                <el-icon><User /></el-icon>
              </div>
              <div class="info-content">
                <div class="info-label">用户名</div>
                <div class="info-value">{{ userStore.username }}</div>
              </div>
            </div>
            <div class="info-card">
              <div class="info-icon id-icon">
                <el-icon><CreditCard /></el-icon>
              </div>
              <div class="info-content">
                <div class="info-label">用户ID</div>
                <div class="info-value">{{ userStore.userId }}</div>
              </div>
            </div>
            <div class="info-card">
              <div class="info-icon role-icon">
                <el-icon><Medal /></el-icon>
              </div>
              <div class="info-content">
                <div class="info-label">用户角色</div>
                <div class="info-value">{{ roleText }}</div>
              </div>
            </div>
            <div class="info-card">
              <div class="info-icon time-icon">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="info-content">
                <div class="info-label">注册时间</div>
                <div class="info-value">--</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 快捷入口 -->
        <div class="quick-section">
          <h3 class="section-title">
            <span class="section-bar"></span>
            快捷入口
          </h3>
          <div class="quick-grid">
            <div class="quick-item" @click="goToFavorites">
              <div class="quick-icon fav-icon">
                <el-icon><Star /></el-icon>
              </div>
              <div class="quick-text">
                <div class="quick-title">我的收藏</div>
                <div class="quick-desc">查看收藏的房源</div>
              </div>
              <el-icon class="quick-arrow"><ArrowRight /></el-icon>
            </div>
            <div class="quick-item" @click="goToAppointments">
              <div class="quick-icon apt-icon">
                <el-icon><Calendar /></el-icon>
              </div>
              <div class="quick-text">
                <div class="quick-title">我的预约</div>
                <div class="quick-desc">管理预约看房</div>
              </div>
              <el-icon class="quick-arrow"><ArrowRight /></el-icon>
            </div>
            <div class="quick-item" @click="goToAI">
              <div class="quick-icon ai-icon">
                <el-icon><ChatDotRound /></el-icon>
              </div>
              <div class="quick-text">
                <div class="quick-title">AI问答</div>
                <div class="quick-desc">智能房产咨询</div>
              </div>
              <el-icon class="quick-arrow"><ArrowRight /></el-icon>
            </div>
            <div class="quick-item" @click="goToHome">
              <div class="quick-icon home-icon">
                <el-icon><House /></el-icon>
              </div>
              <div class="quick-text">
                <div class="quick-title">返回首页</div>
                <div class="quick-desc">浏览更多房源</div>
              </div>
              <el-icon class="quick-arrow"><ArrowRight /></el-icon>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  User,
  CreditCard,
  Medal,
  Clock,
  Star,
  Calendar,
  ChatDotRound,
  House,
  ArrowRight
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const roleText = computed(() => {
  const roleMap: Record<number, string> = {
    0: '普通用户',
    1: '经纪人',
    2: '管理员'
  }
  return roleMap[userStore.userRole] || '未知'
})

const roleBadge = computed(() => {
  const badgeMap: Record<number, string> = {
    0: 'VIP',
    1: 'BROKER',
    2: 'ADMIN'
  }
  return badgeMap[userStore.userRole] || 'USER'
})

const goToFavorites = () => {
  router.push('/favorites').catch(() => {})
}

const goToAppointments = () => {
  router.push('/appointments').catch(() => {})
}

const goToAI = () => {
  router.push('/ai-qa').catch(() => {})
}

const goToHome = () => {
  router.push('/').catch(() => {})
}
</script>

<style scoped>
.profile-view {
  padding: var(--spacing-xl) var(--spacing-2xl);
  min-height: 100%;
  background: var(--bg-color-page);
}

.page-header {
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

.profile-card {
  background: var(--bg-color);
  border-radius: var(--radius-2xl);
  overflow: hidden;
  box-shadow: var(--shadow-base);
  border: 1px solid var(--border-color-light);
}

.profile-header {
  position: relative;
  overflow: hidden;
}

.profile-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-color-dark) 50%, var(--primary-color-darker) 100%);
}

.profile-bg::before {
  content: '';
  position: absolute;
  top: -80px;
  right: -80px;
  width: 280px;
  height: 280px;
  background: radial-gradient(circle, rgba(255, 125, 0, 0.3) 0%, transparent 70%);
  border-radius: 50%;
}

.profile-bg::after {
  content: '';
  position: absolute;
  bottom: -60px;
  left: 40px;
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(64, 150, 255, 0.35) 0%, transparent 70%);
  border-radius: 50%;
}

.profile-content {
  position: relative;
  z-index: 1;
  padding: var(--spacing-2xl) var(--spacing-2xl) var(--spacing-3xl);
  display: flex;
  align-items: center;
  gap: var(--spacing-xl);
}

.avatar-wrap {
  position: relative;
  flex-shrink: 0;
}

.avatar {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--accent-color) 0%, var(--accent-color-light) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 8px 24px rgba(255, 125, 0, 0.35);
  border: 4px solid rgba(255, 255, 255, 0.3);
}

.avatar-badge {
  position: absolute;
  bottom: 0;
  right: 0;
  padding: 3px 10px;
  border-radius: var(--radius-full);
  font-size: 10px;
  font-weight: 700;
  color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  letter-spacing: 0.5px;
}

.avatar-badge.role-0 {
  background: linear-gradient(135deg, #ffd700 0%, #ffb700 100%);
}

.avatar-badge.role-1 {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-color-light) 100%);
}

.avatar-badge.role-2 {
  background: linear-gradient(135deg, #722ed1 0%, #9254de 100%);
}

.user-info {
  flex: 1;
}

.username {
  font-size: 28px;
  font-weight: 700;
  color: white;
  margin: 0 0 var(--spacing-sm);
  letter-spacing: 0.5px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.user-role {
  margin: 0;
}

.role-tag {
  border: none !important;
  background: rgba(255, 255, 255, 0.2) !important;
  color: white !important;
  backdrop-filter: blur(10px);
  font-weight: 500;
  padding: 0 var(--spacing-md) !important;
  height: 28px !important;
  line-height: 26px !important;
}

.profile-body {
  padding: var(--spacing-2xl);
}

.info-section {
  margin-bottom: var(--spacing-2xl);
}

.info-section:last-child {
  margin-bottom: 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-color-primary);
  margin: 0 0 var(--spacing-lg);
}

.section-bar {
  width: 4px;
  height: 20px;
  background: linear-gradient(180deg, var(--primary-color) 0%, var(--accent-color) 100%);
  border-radius: 2px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-md);
}

.info-card {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
  background: var(--bg-color-light);
  border-radius: var(--radius-lg);
  transition: var(--transition-base);
  border: 1px solid var(--border-color-light);
}

.info-card:hover {
  background: var(--primary-color-bg-light);
  border-color: var(--primary-color-bg);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.1);
}

.info-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 22px;
}

.user-icon {
  background: var(--primary-color-bg);
  color: var(--primary-color);
}

.id-icon {
  background: var(--accent-color-bg);
  color: var(--accent-color);
}

.role-icon {
  background: var(--success-color-bg);
  color: var(--success-color);
}

.time-icon {
  background: var(--warning-color-bg);
  color: var(--warning-color);
}

.info-content {
  min-width: 0;
  flex: 1;
}

.info-label {
  font-size: 12px;
  color: var(--text-color-tertiary);
  margin-bottom: 4px;
}

.info-value {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.quick-section {
  padding-top: var(--spacing-xl);
  border-top: 1px solid var(--border-color-light);
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-md);
}

.quick-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
  background: var(--bg-color-light);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: var(--transition-base);
  border: 1px solid var(--border-color-light);
  position: relative;
  overflow: hidden;
}

.quick-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, var(--primary-color-bg-light) 0%, transparent 50%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.quick-item:hover {
  border-color: var(--primary-color-bg);
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(22, 119, 255, 0.12);
}

.quick-item:hover::before {
  opacity: 1;
}

.quick-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 22px;
  position: relative;
  z-index: 1;
  transition: transform 0.3s ease;
}

.quick-item:hover .quick-icon {
  transform: scale(1.1);
}

.fav-icon {
  background: linear-gradient(135deg, var(--accent-color) 0%, var(--accent-color-light) 100%);
  color: white;
}

.apt-icon {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-color-light) 100%);
  color: white;
}

.ai-icon {
  background: linear-gradient(135deg, #722ed1 0%, #9254de 100%);
  color: white;
}

.home-icon {
  background: linear-gradient(135deg, var(--success-color) 0%, var(--success-color-light) 100%);
  color: white;
}

.quick-text {
  flex: 1;
  min-width: 0;
  position: relative;
  z-index: 1;
}

.quick-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-color-primary);
  margin-bottom: 4px;
}

.quick-desc {
  font-size: 12px;
  color: var(--text-color-tertiary);
}

.quick-arrow {
  color: var(--text-color-tertiary);
  font-size: 16px;
  position: relative;
  z-index: 1;
  transition: all 0.3s ease;
}

.quick-item:hover .quick-arrow {
  color: var(--primary-color);
  transform: translateX(4px);
}

/* 响应式设计 */
@media (max-width: 900px) {
  .info-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .quick-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .profile-view {
    padding: var(--spacing-md);
  }

  .page-title h1 {
    font-size: 20px;
  }

  .profile-content {
    padding: var(--spacing-xl);
    flex-direction: column;
    text-align: center;
  }

  .username {
    font-size: 22px;
  }

  .profile-body {
    padding: var(--spacing-lg);
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .quick-grid {
    grid-template-columns: 1fr;
  }
}
</style>
