import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo, LoginResponse } from '@/types/user'
import { userApi } from '@/api/user'

/**
 * 用户状态管理Store
 * 管理用户登录状态、用户信息、JWT token等，支持角色权限控制
 */
export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string | null>(localStorage.getItem('token'))
  const userInfo = ref<UserInfo | null>(null)

  // 从localStorage初始化用户信息
  const storedUserInfo = localStorage.getItem('userInfo')
  if (storedUserInfo) {
    try {
      userInfo.value = JSON.parse(storedUserInfo)
    } catch (error) {
      console.error('解析用户信息失败:', error)
      localStorage.removeItem('userInfo')
    }
  }

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value?.username || '')
  const userId = computed(() => userInfo.value?.id || null)
  const userRole = computed(() => userInfo.value?.role || 0)

  /**
   * 用户登录
   * @param username 用户名
   * @param password 密码
   * @returns 登录结果
   */
  const login = async (username: string, password: string) => {
    try {
      const response = await userApi.login({ username, password })

      if (response.code === 200 && response.data) {
        const loginData: LoginResponse = response.data

        // 保存JWT token
        token.value = loginData.token
        localStorage.setItem('token', loginData.token)

        // 保存用户信息
        userInfo.value = {
          id: loginData.userId,
          username: loginData.username,
          realName: loginData.realName,
          phone: loginData.phone,
          email: loginData.email,
          role: loginData.role,
          avatar: loginData.avatar
        }
        localStorage.setItem('userInfo', JSON.stringify(userInfo.value))

        return {
          success: true,
          message: '登录成功',
          data: loginData
        }
      } else {
        return {
          success: false,
          message: response.message || '登录失败'
        }
      }
    } catch (error: any) {
      console.error('登录失败:', error)
      return {
        success: false,
        message: error.message || '网络错误，请稍后重试'
      }
    }
  }

  /**
   * 用户注册
   * @param userData 用户注册数据
   * @returns 注册结果
   */
  const register = async (userData: {
    username: string
    password: string
    phone: string
    realName?: string
    email?: string
  }) => {
    try {
      const response = await userApi.register(userData)

      if (response.code === 200) {
        return {
          success: true,
          message: '注册成功'
        }
      } else {
        return {
          success: false,
          message: response.message || '注册失败'
        }
      }
    } catch (error: any) {
      console.error('注册失败:', error)
      return {
        success: false,
        message: error.message || '网络错误，请稍后重试'
      }
    }
  }

  /**
   * 退出登录
   */
  const logout = () => {
    token.value = null
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  /**
   * 更新用户信息
   * @param newUserInfo 新的用户信息
   */
  const updateUserInfo = (newUserInfo: UserInfo) => {
    userInfo.value = { ...userInfo.value, ...newUserInfo }
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  /**
   * 检查用户名是否可用
   * @param username 用户名
   * @returns 检查结果
   */
  const checkUsername = async (username: string) => {
    try {
      const response = await userApi.checkUsername(username)
      return {
        available: !response.data?.exists,
        message: response.data?.exists ? '用户名已存在' : '用户名可用'
      }
    } catch (error: any) {
      console.error('检查用户名失败:', error)
      return {
        available: false,
        message: '检查失败，请稍后重试'
      }
    }
  }

  /**
   * 检查手机号是否可用
   * @param phone 手机号
   * @returns 检查结果
   */
  const checkPhone = async (phone: string) => {
    try {
      const response = await userApi.checkPhone(phone)
      return {
        available: !response.data?.exists,
        message: response.data?.exists ? '手机号已存在' : '手机号可用'
      }
    } catch (error: any) {
      console.error('检查手机号失败:', error)
      return {
        available: false,
        message: '检查失败，请稍后重试'
      }
    }
  }

  /**
   * 获取用户信息
   * @param userId 用户ID
   * @returns 用户信息
   */
  const getUserInfo = async (userId: number) => {
    try {
      const response = await userApi.getUserById(userId)
      if (response.code === 200 && response.data) {
        return {
          success: true,
          data: response.data
        }
      } else {
        return {
          success: false,
          message: response.message || '获取用户信息失败'
        }
      }
    } catch (error: any) {
      console.error('获取用户信息失败:', error)
      return {
        success: false,
        message: error.message || '网络错误，请稍后重试'
      }
    }
  }

  return {
    // 状态
    token,
    userInfo,

    // 计算属性
    isLoggedIn,
    username,
    userId,
    userRole,

    // 方法
    login,
    register,
    logout,
    updateUserInfo,
    checkUsername,
    checkPhone,
    getUserInfo
  }
})