<template>
  <!-- 用户登录页面 -->
  <div class="login-view page-animate">
    <div class="login-container">
      <!-- 登录表单 -->
      <el-card class="login-card" shadow="always">
        <template #header>
          <div class="card-header">
            <div class="logo-wrapper">
              <svg class="logo-icon" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 2L2 7v10l10 5 10-5V7L12 2zm0 2.18l7 3.5v7.64l-7 3.5-7-3.5V7.68l7-3.5z"/>
                <path d="M12 6.5L7 9v6l5 2.5L17 15V9l-5-2.5z" opacity="0.6"/>
              </svg>
            </div>
            <h2>用户登录</h2>
            <p class="subtitle">欢迎回到智能房产交易平台</p>
          </div>
        </template>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          label-width="0"
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <!-- 用户名输入 -->
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
              clearable
              size="large"
            />
          </el-form-item>

          <!-- 密码输入 -->
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
              clearable
              size="large"
            />
          </el-form-item>

          <!-- 记住我 & 忘记密码 -->
          <div class="remember-row">
            <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
            <span class="forgot-link" @click="goToRegister">忘记密码？</span>
          </div>

          <!-- 登录按钮 -->
          <el-form-item>
            <el-button
              type="primary"
              class="login-button"
              :loading="loading"
              @click="handleLogin"
              size="large"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>

          <!-- 分割线 -->
          <div class="divider">
            <div class="divider-line"></div>
            <span class="divider-text">还没有账号</span>
            <div class="divider-line"></div>
          </div>

          <!-- 注册链接 -->
          <div class="register-link">
            <el-link type="primary" @click="goToRegister">立即注册</el-link>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import type { LoginRequest } from '@/types/user'

// 路由实例
const router = useRouter()

// 用户状态管理
const userStore = useUserStore()

// 表单引用
const loginFormRef = ref<FormInstance>()

// 登录表单数据
const loginForm = reactive<LoginRequest & { rememberMe: boolean }>({
  username: '',
  password: '',
  rememberMe: false
})

// 加载状态
const loading = ref(false)

// 表单验证规则
const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20个字符', trigger: 'blur' }
  ]
}

/**
 * 处理登录
 */
const handleLogin = async () => {
  // 表单验证
  if (!loginFormRef.value) return

  const valid = await loginFormRef.value.validate()
  if (!valid) return

  // 设置加载状态
  loading.value = true

  try {
    // 调用登录API
    const result = await userStore.login(loginForm.username, loginForm.password)

    if (result.success) {
      ElMessage.success('登录成功')

      // 如果选择了记住我，保存用户名到localStorage
      if (loginForm.rememberMe) {
        localStorage.setItem('rememberedUsername', loginForm.username)
      } else {
        localStorage.removeItem('rememberedUsername')
      }

      // 延迟后刷新页面，确保状态完全同步
      setTimeout(() => {
        window.location.href = '/'
      }, 500)
    } else {
      ElMessage.error(result.message || '登录失败')
    }
  } catch (error: any) {
    console.error('登录失败:', error)
    ElMessage.error(error.message || '登录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

/**
 * 跳转到注册页面
 */
const goToRegister = () => {
  router.push('/register')
}

// 页面加载时检查是否有记住的用户名
const rememberedUsername = localStorage.getItem('rememberedUsername')
if (rememberedUsername) {
  loginForm.username = rememberedUsername
  loginForm.rememberMe = true
}
</script>

<style scoped>
.login-view {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: linear-gradient(135deg, #e6f4ff 0%, #f0f7ff 30%, #fff7e6 70%, #f5f7fa 100%);
  position: relative;
  overflow: hidden;
}

.login-view::before {
  content: '';
  position: absolute;
  top: -200px;
  right: -200px;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(22, 119, 255, 0.08) 0%, transparent 70%);
  border-radius: 50%;
}

.login-view::after {
  content: '';
  position: absolute;
  bottom: -150px;
  left: -150px;
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(255, 125, 0, 0.06) 0%, transparent 70%);
  border-radius: 50%;
}

.login-container {
  display: flex;
  max-width: 960px;
  width: 100%;
  justify-content: center;
  position: relative;
  z-index: 1;
}

.login-card {
  width: 100%;
  max-width: 440px;
  border-radius: var(--radius-2xl);
  overflow: hidden;
  box-shadow: var(--shadow-xl);
  border: 1px solid rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  background: rgba(255, 255, 255, 0.95);
}

.login-card :deep(.el-card__header) {
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
  padding: 36px 32px 32px;
  position: relative;
  overflow: hidden;
}

.login-card :deep(.el-card__header)::before {
  content: '';
  position: absolute;
  top: -50px;
  right: -50px;
  width: 150px;
  height: 150px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
}

.login-card :deep(.el-card__header)::after {
  content: '';
  position: absolute;
  bottom: -30px;
  left: 30px;
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 50%;
}

.card-header {
  text-align: center;
  position: relative;
  z-index: 1;
}

.logo-wrapper {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
}

.logo-icon {
  width: 36px;
  height: 36px;
  color: white;
}

.card-header h2 {
  font-size: 26px;
  font-weight: 700;
  color: white;
  margin-bottom: 8px;
  letter-spacing: 1px;
}

.card-header .subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
}

.login-form {
  padding: 32px 32px 8px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 22px;
}

.login-form :deep(.el-input__wrapper) {
  height: 46px;
  padding: 0 16px;
  border-radius: var(--radius-lg);
  background: var(--bg-color-light);
  border: 1px solid var(--border-color-light);
  box-shadow: none;
  transition: all 0.25s ease;
}

.login-form :deep(.el-input__wrapper:hover) {
  border-color: var(--primary-color-light);
  background: var(--bg-color);
  box-shadow: 0 0 0 3px var(--primary-color-bg);
}

.login-form :deep(.el-input__wrapper.is-focus) {
  border-color: var(--primary-color);
  background: var(--bg-color);
  box-shadow: 0 0 0 3px var(--primary-color-bg);
}

.login-form :deep(.el-input__inner) {
  font-size: 14px;
}

.login-form :deep(.el-input__prefix) {
  color: var(--text-color-tertiary);
}

.form-tips {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  font-size: 12px;
}

.form-tips .el-icon {
  font-size: 14px;
}

.remember-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.forgot-link {
  font-size: 13px;
  color: var(--text-color-secondary);
  cursor: pointer;
  transition: color 0.2s;
}

.forgot-link:hover {
  color: var(--primary-color);
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--radius-full);
  background: linear-gradient(135deg, #ff7d00 0%, #ff9a2e 100%);
  border: none;
  color: white;
  box-shadow: 0 4px 12px rgba(255, 125, 0, 0.3);
  transition: all 0.3s ease;
  letter-spacing: 1px;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 125, 0, 0.4);
  background: linear-gradient(135deg, #ff9a2e 0%, #ffb86b 100%);
}

.login-button:active {
  transform: translateY(0);
}

.divider {
  display: flex;
  align-items: center;
  margin: 24px 0 20px;
}

.divider-line {
  flex: 1;
  height: 1px;
  background: var(--border-color);
}

.divider-text {
  padding: 0 16px;
  font-size: 13px;
  color: var(--text-color-tertiary);
}

.register-link {
  text-align: center;
  font-size: 14px;
  color: var(--text-color-secondary);
}

.register-link .el-link {
  margin-left: 6px;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-view {
    padding: 16px;
  }

  .login-card :deep(.el-card__header) {
    padding: 28px 24px 24px;
  }

  .login-form {
    padding: 24px 20px 8px;
  }

  .card-header h2 {
    font-size: 22px;
  }
}
</style>