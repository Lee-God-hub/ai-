<template>
  <!-- 用户注册页面 -->
  <div class="register-view page-animate">
    <div class="register-container">
      <!-- 注册表单 -->
      <el-card class="register-card" shadow="always">
        <template #header>
          <div class="card-header">
            <div class="logo-wrapper">
              <svg class="logo-icon" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 2L2 7v10l10 5 10-5V7L12 2zm0 2.18l7 3.5v7.64l-7 3.5-7-3.5V7.68l7-3.5z"/>
                <path d="M12 6.5L7 9v6l5 2.5L17 15V9l-5-2.5z" opacity="0.6"/>
              </svg>
            </div>
            <h2>用户注册</h2>
            <p class="subtitle">创建您的智能房产交易平台账号</p>
          </div>
        </template>
        
        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          label-width="100px"
          class="register-form"
          @keyup.enter="handleRegister"
        >
          <!-- 用户名输入 -->
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="registerForm.username"
              placeholder="请输入用户名（3-20位字母、数字、下划线）"
              prefix-icon="User"
              clearable
              @blur="checkUsernameAvailability"
            />
            <div class="form-tips" v-if="usernameStatus">
              <el-icon :color="usernameStatus.color"><CircleCheckFilled v-if="usernameStatus.valid" /><CircleCloseFilled v-else /></el-icon>
              <span :style="{ color: usernameStatus.color }">{{ usernameStatus.message }}</span>
            </div>
          </el-form-item>
          
          <!-- 密码输入 -->
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码（6-20位）"
              prefix-icon="Lock"
              show-password
              clearable
              @input="checkPasswordStrength"
            />
            <div class="password-strength" v-if="registerForm.password">
              <div class="strength-bar">
                <div 
                  class="strength-fill" 
                  :class="passwordStrength.level"
                  :style="{ width: passwordStrength.percentage + '%' }"
                ></div>
              </div>
              <span class="strength-text" :style="{ color: passwordStrength.color }">
                密码强度：{{ passwordStrength.text }}
              </span>
            </div>
          </el-form-item>
          
          <!-- 确认密码 -->
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              prefix-icon="Lock"
              show-password
              clearable
            />
          </el-form-item>
          
          <!-- 真实姓名 -->
          <el-form-item label="真实姓名" prop="realName">
            <el-input
              v-model="registerForm.realName"
              placeholder="请输入真实姓名（选填）"
              prefix-icon="UserFilled"
              clearable
            />
          </el-form-item>
          
          <!-- 手机号码 -->
          <el-form-item label="手机号码" prop="phone">
            <el-input
              v-model="registerForm.phone"
              placeholder="请输入手机号码"
              prefix-icon="Iphone"
              clearable
              @blur="checkPhoneAvailability"
            />
            <div class="form-tips" v-if="phoneStatus">
              <el-icon :color="phoneStatus.color"><CircleCheckFilled v-if="phoneStatus.valid" /><CircleCloseFilled v-else /></el-icon>
              <span :style="{ color: phoneStatus.color }">{{ phoneStatus.message }}</span>
            </div>
          </el-form-item>
          
          <!-- 电子邮箱 -->
          <el-form-item label="电子邮箱" prop="email">
            <el-input
              v-model="registerForm.email"
              placeholder="请输入电子邮箱（选填）"
              prefix-icon="Message"
              clearable
            />
          </el-form-item>
          
          <!-- 用户角色选择 -->
          <el-form-item label="注册身份" prop="role">
            <el-radio-group v-model="registerForm.role">
              <el-radio :label="0">
                <div class="role-option">
                  <el-icon><User /></el-icon>
                  <div class="role-info">
                    <span class="role-name">普通用户</span>
                    <span class="role-desc">浏览房源、收藏、预约看房</span>
                  </div>
                </div>
              </el-radio>
              <el-radio :label="1">
                <div class="role-option">
                  <el-icon><House /></el-icon>
                  <div class="role-info">
                    <span class="role-name">房东</span>
                    <span class="role-desc">发布房源、管理房源、处理预约</span>
                  </div>
                </div>
              </el-radio>
            </el-radio-group>
          </el-form-item>
          
          <!-- 用户协议 -->
          <el-form-item>
            <el-checkbox v-model="registerForm.agreed" :checked="registerForm.agreed">
              我已阅读并同意
              <el-link type="primary" @click="showUserAgreement">《用户协议》</el-link>
              和
              <el-link type="primary" @click="showPrivacyPolicy">《隐私政策》</el-link>
            </el-checkbox>
          </el-form-item>
          
          <!-- 注册按钮 -->
          <el-form-item>
            <el-button
              type="primary"
              class="register-button"
              :loading="loading"
              @click="handleRegister"
            >
              {{ loading ? '注册中...' : '立即注册' }}
            </el-button>
          </el-form-item>
          
          <!-- 分割线 -->
          <div class="divider">
            <div class="divider-line"></div>
            <span class="divider-text">已有账号</span>
            <div class="divider-line"></div>
          </div>

          <!-- 登录链接 -->
          <div class="login-link">
            <el-link type="primary" @click="goToLogin">立即登录</el-link>
          </div>
        </el-form>
      </el-card>
      
      <!-- 注册优势 -->
      <div class="register-advantages">
        <h3>注册成为平台用户，享受以下权益：</h3>
        <div class="advantages-list">
          <div class="advantage-item">
            <el-icon class="advantage-icon" color="#409EFF"><Search /></el-icon>
            <div class="advantage-content">
              <h4>智能房产搜索</h4>
              <p>基于AI算法的智能推荐，快速找到心仪房源</p>
            </div>
          </div>
          <div class="advantage-item">
            <el-icon class="advantage-icon" color="#67C23A"><Star /></el-icon>
            <div class="advantage-content">
              <h4>收藏房源</h4>
              <p>收藏感兴趣的房源，方便随时查看</p>
            </div>
          </div>
          <div class="advantage-item">
            <el-icon class="advantage-icon" color="#E6A23C"><Bell /></el-icon>
            <div class="advantage-content">
              <h4>价格提醒</h4>
              <p>设置价格提醒，不错过任何优惠机会</p>
            </div>
          </div>
          <div class="advantage-item">
            <el-icon class="advantage-icon" color="#F56C6C"><Service /></el-icon>
            <div class="advantage-content">
              <h4>专属客服</h4>
              <p>享受一对一专业房产咨询服务</p>
            </div>
          </div>
        </div>
        
        <div class="security-notice">
          <el-icon color="#67C23A"><Lock /></el-icon>
          <div class="notice-content">
            <h4>安全保障</h4>
            <p>您的个人信息将受到严格保护，我们承诺不会泄露给第三方</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import type { RegisterRequest } from '@/types/user'

// 路由实例
const router = useRouter()

// 用户状态管理
const userStore = useUserStore()

// 表单引用
const registerFormRef = ref<FormInstance>()

// 注册表单数据
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: '',
  email: '',
  role: 0, // 默认普通用户
  agreed: false
})

// 用户名检查状态
const usernameStatus = ref<{
  valid: boolean
  message: string
  color: string
} | null>(null)

// 手机号检查状态
const phoneStatus = ref<{
  valid: boolean
  message: string
  color: string
} | null>(null)

// 密码强度状态
const passwordStrength = ref({
  level: 'weak',
  percentage: 0,
  text: '弱',
  color: '#F56C6C'
})

// 加载状态
const loading = ref(false)

/**
 * 验证确认密码
 */
const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 表单验证规则
const registerRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度3-20个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/, message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择注册身份', trigger: 'change' }
  ]
}

/**
 * 检查密码强度
 */
const checkPasswordStrength = () => {
  const password = registerForm.password
  if (!password) {
    passwordStrength.value = {
      level: 'weak',
      percentage: 0,
      text: '弱',
      color: '#F56C6C'
    }
    return
  }
  
  let strength = 0
  
  // 长度检查
  if (password.length >= 6) strength += 20
  if (password.length >= 8) strength += 10
  if (password.length >= 12) strength += 10
  
  // 包含小写字母
  if (/[a-z]/.test(password)) strength += 15
  
  // 包含大写字母
  if (/[A-Z]/.test(password)) strength += 15
  
  // 包含数字
  if (/\d/.test(password)) strength += 15
  
  // 包含特殊字符
  if (/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)) strength += 15
  
  // 根据强度设置显示
  if (strength < 40) {
    passwordStrength.value = {
      level: 'weak',
      percentage: strength,
      text: '弱',
      color: '#F56C6C'
    }
  } else if (strength < 70) {
    passwordStrength.value = {
      level: 'medium',
      percentage: strength,
      text: '中',
      color: '#E6A23C'
    }
  } else {
    passwordStrength.value = {
      level: 'strong',
      percentage: strength,
      text: '强',
      color: '#67C23A'
    }
  }
}

/**
 * 检查用户名是否可用
 */
const checkUsernameAvailability = async () => {
  if (!registerForm.username || registerForm.username.length < 3) {
    usernameStatus.value = null
    return
  }
  
  try {
    const result = await userStore.checkUsername(registerForm.username)
    usernameStatus.value = {
      valid: result.available,
      message: result.message,
      color: result.available ? '#67C23A' : '#F56C6C'
    }
  } catch (error) {
    console.error('检查用户名失败:', error)
  }
}

/**
 * 检查手机号是否可用
 */
const checkPhoneAvailability = async () => {
  if (!registerForm.phone || !/^1[3-9]\d{9}$/.test(registerForm.phone)) {
    phoneStatus.value = null
    return
  }
  
  try {
    const result = await userStore.checkPhone(registerForm.phone)
    phoneStatus.value = {
      valid: result.available,
      message: result.message,
      color: result.available ? '#67C23A' : '#F56C6C'
    }
  } catch (error) {
    console.error('检查手机号失败:', error)
  }
}

/**
 * 处理注册
 */
const handleRegister = async () => {
  // 表单验证
  if (!registerFormRef.value) return
  
  try {
    const valid = await registerFormRef.value.validate()
    if (!valid) return
  } catch (error) {
    console.error('表单验证失败:', error)
    return
  }
  
  // 检查用户协议
  if (!registerForm.agreed) {
    ElMessage.warning('请阅读并同意用户协议和隐私政策')
    return
  }
  
  // 检查用户名和手机号是否可用
  if (usernameStatus.value && !usernameStatus.value.valid) {
    ElMessage.error('用户名不可用，请修改后重试')
    return
  }
  
  if (phoneStatus.value && !phoneStatus.value.valid) {
    ElMessage.error('手机号不可用，请修改后重试')
    return
  }
  
  // 设置加载状态
  loading.value = true
  
  try {
    // 准备注册数据
    const registerData = {
      username: registerForm.username,
      password: registerForm.password,
      phone: registerForm.phone,
      realName: registerForm.realName || undefined,
      email: registerForm.email || undefined,
      role: registerForm.role
    }
    
    // 调用注册API
    const result = await userStore.register(registerData)
    
    if (result.success) {
      ElMessage.success({
        message: '注册成功！即将跳转到登录页面...',
        duration: 2000
      })
      
      // 延迟跳转到登录页面
      setTimeout(() => {
        router.push({
          path: '/login',
          query: { username: registerForm.username }
        })
      }, 2000)
    } else {
      ElMessage.error(result.message || '注册失败，请稍后重试')
    }
  } catch (error: any) {
    console.error('注册失败:', error)
    ElMessage.error(error.message || '注册失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

/**
 * 跳转到登录页面
 */
const goToLogin = () => {
  router.push('/login')
}

/**
 * 显示用户协议
 */
const showUserAgreement = () => {
  ElMessage.info('用户协议内容正在开发中...')
}

/**
 * 显示隐私政策
 */
const showPrivacyPolicy = () => {
  ElMessage.info('隐私政策内容正在开发中...')
}
</script>

<style scoped>
.register-view {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  background: linear-gradient(135deg, #e6f4ff 0%, #f0f7ff 30%, #fff7e6 70%, #f5f7fa 100%);
  position: relative;
  overflow: hidden;
}

.register-view::before {
  content: '';
  position: absolute;
  top: -200px;
  right: -200px;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(22, 119, 255, 0.08) 0%, transparent 70%);
  border-radius: 50%;
}

.register-view::after {
  content: '';
  position: absolute;
  bottom: -150px;
  left: -150px;
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(255, 125, 0, 0.06) 0%, transparent 70%);
  border-radius: 50%;
}

.register-container {
  display: flex;
  max-width: 1100px;
  width: 100%;
  gap: 32px;
  align-items: flex-start;
  position: relative;
  z-index: 1;
}

.register-card {
  flex: 1;
  max-width: 560px;
  border-radius: var(--radius-2xl);
  overflow: hidden;
  box-shadow: var(--shadow-xl);
  border: 1px solid rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  background: rgba(255, 255, 255, 0.95);
}

.register-card :deep(.el-card__header) {
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
  padding: 32px 32px 28px;
  position: relative;
  overflow: hidden;
}

.register-card :deep(.el-card__header)::before {
  content: '';
  position: absolute;
  top: -50px;
  right: -50px;
  width: 150px;
  height: 150px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
}

.register-card :deep(.el-card__header)::after {
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
  width: 56px;
  height: 56px;
  margin: 0 auto 12px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
}

.logo-icon {
  width: 32px;
  height: 32px;
  color: white;
}

.card-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: white;
  margin-bottom: 6px;
  letter-spacing: 1px;
}

.card-header .subtitle {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.9);
}

.register-form {
  padding: 28px 28px 8px;
}

.register-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.register-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--text-color-regular);
}

.register-form :deep(.el-input__wrapper) {
  height: 42px;
  padding: 0 14px;
  border-radius: var(--radius-lg);
  background: var(--bg-color-light);
  border: 1px solid var(--border-color-light);
  box-shadow: none;
  transition: all 0.25s ease;
}

.register-form :deep(.el-input__wrapper:hover) {
  border-color: var(--primary-color-light);
  background: var(--bg-color);
  box-shadow: 0 0 0 3px var(--primary-color-bg);
}

.register-form :deep(.el-input__wrapper.is-focus) {
  border-color: var(--primary-color);
  background: var(--bg-color);
  box-shadow: 0 0 0 3px var(--primary-color-bg);
}

.form-tips {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 6px;
  font-size: 12px;
}

.form-tips .el-icon {
  font-size: 14px;
}

.password-strength {
  margin-top: 8px;
}

.strength-bar {
  width: 100%;
  height: 6px;
  background-color: var(--border-color-light);
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 6px;
}

.strength-fill {
  height: 100%;
  transition: all 0.3s ease;
  border-radius: 3px;
}

.strength-fill.weak { background-color: var(--error-color); }
.strength-fill.medium { background-color: var(--warning-color); }
.strength-fill.strong { background-color: var(--success-color); }

.strength-text {
  font-size: 12px;
  font-weight: 500;
}

.role-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: var(--radius-lg);
  transition: all 0.3s;
  border: 2px solid var(--border-color-light);
  background: var(--bg-color-light);
}

.register-form :deep(.el-radio__input.is-checked + .el-radio__label .role-option) {
  border-color: var(--primary-color);
  background: var(--primary-color-bg-light);
}

.role-option .el-icon {
  font-size: 24px;
  color: var(--primary-color);
}

.role-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.role-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-color-primary);
}

.role-desc {
  font-size: 12px;
  color: var(--text-color-secondary);
}

.register-form :deep(.el-radio) {
  width: 100%;
  margin-right: 0;
  margin-bottom: 12px;
}

.register-form :deep(.el-radio__label) {
  width: 100%;
  padding-left: 0;
}

.register-form :deep(.el-radio__input) {
  display: none;
}

.register-button {
  width: 100%;
  height: 46px;
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

.register-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 125, 0, 0.4);
  background: linear-gradient(135deg, #ff9a2e 0%, #ffb86b 100%);
}

.register-button:active {
  transform: translateY(0);
}

.divider {
  display: flex;
  align-items: center;
  margin: 20px 0 16px;
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

.login-link {
  text-align: center;
  font-size: 14px;
  color: var(--text-color-secondary);
}

.login-link .el-link {
  margin-left: 6px;
  font-weight: 500;
}

.register-advantages {
  flex: 1;
  background: rgba(255, 255, 255, 0.9);
  padding: 32px;
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-lg);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.8);
}

.register-advantages h3 {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-color-primary);
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.register-advantages h3::before {
  content: '';
  width: 4px;
  height: 20px;
  background: linear-gradient(180deg, var(--primary-color), var(--accent-color));
  border-radius: 2px;
}

.advantages-list {
  margin-bottom: 24px;
}

.advantage-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 14px;
  padding: 16px;
  background-color: var(--bg-color-light);
  border-radius: var(--radius-lg);
  transition: all 0.3s;
  border: 1px solid transparent;
}

.advantage-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  background: var(--bg-color);
  border-color: var(--primary-color-bg);
}

.advantage-icon {
  margin-right: 14px;
  flex-shrink: 0;
  font-size: 24px;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: var(--primary-color-bg);
  color: var(--primary-color);
}

.advantage-content h4 {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-color-primary);
  margin-bottom: 4px;
}

.advantage-content p {
  font-size: 13px;
  color: var(--text-color-secondary);
  line-height: 1.5;
}

.security-notice {
  display: flex;
  align-items: flex-start;
  padding: 18px;
  background: var(--primary-color-bg-light);
  border-radius: var(--radius-lg);
  border-left: 4px solid var(--primary-color);
}

.security-notice .el-icon {
  margin-right: 14px;
  font-size: 22px;
  flex-shrink: 0;
  color: var(--primary-color);
}

.notice-content h4 {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-color-primary);
  margin-bottom: 6px;
}

.notice-content p {
  font-size: 13px;
  color: var(--text-color-secondary);
  line-height: 1.5;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .register-container {
    flex-direction: column;
    gap: 24px;
    align-items: center;
  }

  .register-card,
  .register-advantages {
    max-width: 100%;
    width: 100%;
  }

  .register-view {
    padding: 20px;
  }
}
</style>