<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" :class="['aside', { collapsed: isCollapse }]">
      <div class="logo" @click="$router.push('/')">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" width="28" height="28" fill="currentColor">
            <path d="M12 2L2 7v10l10 5 10-5V7L12 2zm0 2.18l7 3.5v7.64l-7 3.5-7-3.5V7.68l7-3.5z"/>
            <path d="M12 6.5L7 9v6l5 2.5L17 15V9l-5-2.5z" opacity="0.6"/>
          </svg>
        </div>
        <transition name="fade">
          <span v-if="!isCollapse" class="logo-text">智能房产</span>
        </transition>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :unique-opened="true"
        class="sidebar-menu"
        :collapse-transition="false"
        @select="handleMenuSelect"
      >
        <!-- 管理员菜单 -->
        <template v-if="userRole === 2">
          <el-menu-item index="/">
            <el-icon><HomeFilled /></el-icon>
            <template #title>首页概览</template>
          </el-menu-item>
          <el-menu-item index="/admin/approval">
            <el-icon><Document /></el-icon>
            <template #title>房源审核</template>
          </el-menu-item>
          <el-menu-item index="/admin/property">
            <el-icon><OfficeBuilding /></el-icon>
            <template #title>房源管理</template>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><UserFilled /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
          <el-menu-item index="/admin/appointments">
            <el-icon><Calendar /></el-icon>
            <template #title>预约管理</template>
          </el-menu-item>
          <el-menu-item index="/admin/messages">
            <el-icon><ChatDotRound /></el-icon>
            <template #title>留言管理</template>
          </el-menu-item>
          <el-menu-item index="/admin/favorites">
            <el-icon><Star /></el-icon>
            <template #title>收藏管理</template>
          </el-menu-item>
          <el-menu-item index="/admin/setting">
            <el-icon><Setting /></el-icon>
            <template #title>系统设置</template>
          </el-menu-item>
        </template>
        <!-- 房东菜单 -->
        <template v-else-if="userRole === 1">
          <el-menu-item index="/">
            <el-icon><HomeFilled /></el-icon>
            <template #title>首页概览</template>
          </el-menu-item>
          <el-menu-item index="/ai-qa">
            <el-icon><ChatDotRound /></el-icon>
            <template #title>AI 助手</template>
          </el-menu-item>
          <el-menu-item index="/landlord/publish">
            <el-icon><CirclePlus /></el-icon>
            <template #title>发布房源</template>
          </el-menu-item>
          <el-menu-item index="/landlord/my-properties">
            <el-icon><OfficeBuilding /></el-icon>
            <template #title>我的房源</template>
          </el-menu-item>
          <el-menu-item index="/landlord/appointments">
            <el-icon><Calendar /></el-icon>
            <template #title>预约管理</template>
          </el-menu-item>
          <el-menu-item index="/landlord/messages">
            <el-icon><ChatDotRound /></el-icon>
            <template #title>我的消息</template>
          </el-menu-item>
          <el-menu-item index="/profile">
            <el-icon><UserFilled /></el-icon>
            <template #title>个人中心</template>
          </el-menu-item>
        </template>
        <!-- 普通用户菜单 -->
        <template v-else>
          <el-menu-item index="/">
            <el-icon><HomeFilled /></el-icon>
            <template #title>首页概览</template>
          </el-menu-item>
          <el-menu-item index="/ai-qa">
            <el-icon><ChatDotRound /></el-icon>
            <template #title>AI 助手</template>
          </el-menu-item>
          <el-menu-item index="/property">
            <el-icon><OfficeBuilding /></el-icon>
            <template #title>房源浏览</template>
          </el-menu-item>
          <el-menu-item index="/favorites">
            <el-icon><Star /></el-icon>
            <template #title>我的收藏</template>
          </el-menu-item>
          <el-menu-item index="/appointments">
            <el-icon><Calendar /></el-icon>
            <template #title>预约记录</template>
          </el-menu-item>
          <el-menu-item index="/messages">
            <el-icon><ChatDotRound /></el-icon>
            <template #title>我的消息</template>
          </el-menu-item>
          <el-menu-item index="/profile">
            <el-icon><UserFilled /></el-icon>
            <template #title>个人中心</template>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <!-- 主内容区域 -->
    <el-container class="main-container">
      <!-- 顶部导航栏 -->
      <el-header class="header">
        <div class="header-left">
          <div class="collapse-btn" :class="{ collapsed: isCollapse }" @click.stop="toggleCollapse" title="折叠/展开菜单">
            <el-icon>
              <Fold v-if="!isCollapse" />
              <Expand v-else />
            </el-icon>
          </div>
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentPageTitle && route.path !== '/'">{{ currentPageTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <div class="role-badge" :class="'role-' + userRole">
            {{ roleText }}
          </div>
          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-avatar-wrapper">
              <el-avatar :size="32" class="user-avatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span class="user-name">{{ username }}</span>
              <el-icon class="arrow-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><UserFilled /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区域 -->
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Fold,
  Expand,
  User,
  ArrowDown,
  HomeFilled,
  OfficeBuilding,
  UserFilled,
  Setting,
  Document,
  CirclePlus,
  Calendar,
  Star,
  ChatDotRound,
  SwitchButton
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const savedCollapse = localStorage.getItem('sidebarCollapse')
const isCollapse = ref(savedCollapse ? JSON.parse(savedCollapse) : false)

// 用户信息
const username = computed(() => userStore.username)
const userRole = computed(() => userStore.userRole)

// 角色文本
const roleText = computed(() => {
  const map: Record<number, string> = {
    0: '用户',
    1: '房东',
    2: '管理员'
  }
  return map[userStore.userRole] || '未知'
})

// 激活的菜单
const activeMenu = computed(() => route.path)

// 页面标题映射
const pageTitleMap: Record<string, string> = {
  '/': '首页概览',
  '/ai-qa': 'AI 助手',
  '/property': '房源浏览',
  '/favorites': '我的收藏',
  '/appointments': '预约记录',
  '/messages': '我的消息',
  '/profile': '个人中心',
  '/landlord/publish': '发布房源',
  '/landlord/my-properties': '我的房源',
  '/landlord/appointments': '预约管理',
  '/landlord/messages': '我的消息',
  '/landlord/messages/conversation': '对话详情',
  '/admin/approval': '房源审核',
  '/admin/property': '房源管理',
  '/admin/property/edit': '编辑房源',
  '/admin/users': '用户管理',
  '/admin/appointments': '预约管理',
  '/admin/messages': '留言管理',
  '/admin/favorites': '收藏管理',
  '/admin/setting': '系统设置'
}

// 当前页面标题
const currentPageTitle = computed(() => {
  const path = route.path
  if (pageTitleMap[path]) {
    return pageTitleMap[path]
  }
  if (path.startsWith('/property/')) return '房源详情'
  if (path.startsWith('/messages/')) return '消息详情'
  if (path.startsWith('/landlord/messages/conversation/')) return '对话详情'
  if (path.startsWith('/admin/property/edit/')) return '编辑房源'
  if (path.startsWith('/landlord/edit-property/')) return '编辑房源'
  return ''
})

// 切换侧边栏
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
  localStorage.setItem('sidebarCollapse', JSON.stringify(isCollapse.value))
}

// 菜单选择处理（防止点击菜单项时菜单展开）
const handleMenuSelect = (index: string) => {
  router.push(index).catch(() => {})
}

// 处理下拉菜单命令
const handleCommand = async (command: string) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      userStore.logout()
      ElMessage.success('退出登录成功')
      router.push('/login')
    } catch {
      // 用户取消
    }
  } else if (command === 'profile') {
    if ([0, 1].includes(userStore.userRole)) {
      router.push('/profile')
    } else {
      ElMessage.warning('管理员暂不支持个人中心')
    }
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.aside {
  background: var(--bg-color);
  border-right: 1px solid var(--border-color-light);
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow-x: hidden;
  overflow-y: auto;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.03);
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  cursor: pointer;
  border-bottom: 1px solid var(--border-color-light);
  padding: 0 12px;
  position: relative;
  background: linear-gradient(135deg, var(--primary-color-bg-light) 0%, var(--bg-color) 100%);
}

.logo-icon {
  color: var(--primary-color);
  flex-shrink: 0;
  display: flex;
  align-items: center;
  width: 36px;
  height: 36px;
  background: var(--primary-color);
  border-radius: 10px;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
}

.logo-icon svg {
  width: 22px;
  height: 22px;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--accent-color) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  white-space: nowrap;
  letter-spacing: 1px;
}

/* 侧边栏菜单样式 */
.sidebar-menu {
  border-right: none !important;
  padding: 12px 6px;
  background: transparent;
  width: 100%;
}

.sidebar-menu :deep(.el-menu-item) {
  height: 46px;
  line-height: 46px;
  margin-bottom: 6px;
  border-radius: var(--radius-lg);
  color: var(--text-color-secondary);
  font-size: 14px;
  font-weight: 500;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 0 8px !important;
  display: flex;
  justify-content: center;
  align-items: center;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background-color: var(--primary-color-bg-light);
  color: var(--primary-color);
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-color-light) 100%);
  color: white;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
}

.sidebar-menu :deep(.el-menu-item.is-active .el-icon) {
  color: white;
}

.sidebar-menu :deep(.el-menu-item .el-icon) {
  font-size: 18px;
  margin-right: 8px;
  color: var(--text-color-tertiary);
  transition: color 0.25s;
  flex-shrink: 0;
}

.sidebar-menu :deep(.el-menu-item:hover .el-icon) {
  color: var(--primary-color);
}

.sidebar-menu :deep(.el-menu-item:not(.is-active):hover) {
  background-color: var(--primary-color-bg-light) !important;
}

.aside.collapsed .sidebar-menu {
  padding: 12px 4px;
}

.aside.collapsed .sidebar-menu :deep(.el-menu-item) {
  padding: 0 !important;
  width: 48px;
  margin: 0 auto 6px;
}

.aside.collapsed .sidebar-menu :deep(.el-menu-item .el-icon) {
  margin-right: 0;
  font-size: 20px;
}

/* 顶部导航栏 */
.main-container {
  display: flex;
  flex-direction: column;
  background: var(--bg-color-page);
}

.header {
  background: var(--bg-color);
  border-bottom: 1px solid var(--border-color-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  height: 64px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
  position: relative;
  z-index: 10;
}

.header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--border-color), transparent);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.collapse-btn {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: var(--primary-color-bg);
  color: var(--primary-color);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid transparent;
  box-shadow: 0 2px 8px rgba(22, 119, 255, 0.12);
  user-select: none;
}

.collapse-btn .el-icon {
  font-size: 18px;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.collapse-btn:hover {
  background: var(--primary-color);
  color: #fff;
  transform: scale(1.08);
  box-shadow: 0 4px 14px rgba(22, 119, 255, 0.28);
  border-color: var(--primary-color);
}

.collapse-btn:hover .el-icon {
  transform: rotate(180deg);
}

.collapse-btn.collapsed:hover .el-icon {
  transform: rotate(-180deg);
}

.breadcrumb {
  font-size: 14px;
}

.breadcrumb :deep(.el-breadcrumb__inner) {
  color: var(--text-color-secondary);
}

.breadcrumb :deep(.el-breadcrumb__inner.is-link) {
  color: var(--text-color-regular);
  transition: color 0.2s;
}

.breadcrumb :deep(.el-breadcrumb__inner.is-link:hover) {
  color: var(--primary-color);
}

.breadcrumb :deep(.el-breadcrumb__separator) {
  color: var(--text-color-tertiary);
}

/* 角色徽章 */
.role-badge {
  padding: 6px 14px;
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.role-0 {
  background: var(--primary-color-bg);
  color: var(--primary-color);
  border: 1px solid var(--primary-color-bg);
}

.role-1 {
  background: var(--accent-color-bg);
  color: var(--accent-color);
  border: 1px solid var(--accent-color-bg);
}

.role-2 {
  background: var(--error-color-bg);
  color: var(--error-color);
  border: 1px solid var(--error-color-bg);
}

/* 用户头像区域 */
.user-avatar-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 12px 6px 6px;
  border-radius: var(--radius-full);
  transition: var(--transition-base);
  border: 1px solid transparent;
}

.user-avatar-wrapper:hover {
  background: var(--bg-color-light);
  border-color: var(--border-color-light);
}

.user-avatar {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-color-light) 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(22, 119, 255, 0.3);
}

.user-name {
  font-size: 14px;
  color: var(--text-color-regular);
  font-weight: 500;
}

.arrow-icon {
  font-size: 12px;
  color: var(--text-color-tertiary);
  transition: transform 0.2s;
}

.user-avatar-wrapper:hover .arrow-icon {
  color: var(--primary-color);
}

/* 内容区域 */
.main {
  background-color: var(--bg-color-page);
  padding: 24px;
  overflow-y: auto;
}

/* 动画 */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.25s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

/* 滚动条美化 */
.aside::-webkit-scrollbar {
  width: 4px;
}

.aside::-webkit-scrollbar-thumb {
  background: transparent;
  border-radius: 2px;
}

.aside:hover::-webkit-scrollbar-thumb {
  background: var(--border-color);
}

.main::-webkit-scrollbar {
  width: 8px;
}

.main::-webkit-scrollbar-track {
  background: transparent;
}

.main::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 4px;
}

.main::-webkit-scrollbar-thumb:hover {
  background: var(--text-color-tertiary);
}
</style>
