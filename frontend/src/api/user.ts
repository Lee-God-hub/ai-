import { request } from './request'
import type { PageResult } from '@/types'
import type {
  UserInfo,
  LoginResponse,
  RegisterRequest,
  LoginRequest,
  ApiResponse,
  Property,
  Favorite,
  Appointment
} from '@/types/user'

/**
 * 用户API模块
 * 封装所有与用户相关的API请求，支持JWT认证
 */
export const userApi = {
  /**
   * 用户注册
   * @param data 注册数据
   * @returns 注册结果
   */
  register(data: RegisterRequest): Promise<ApiResponse> {
    return request.post('/user/register', data)
  },

  /**
   * 用户登录
   * @param data 登录数据
   * @returns 登录结果（包含JWT token）
   */
  login(data: LoginRequest): Promise<ApiResponse<LoginResponse>> {
    return request.post('/user/login', data)
  },

  /**
   * 获取当前登录用户信息
   * @returns 用户信息
   */
  getCurrentUser(): Promise<ApiResponse<UserInfo>> {
    return request.get('/user/current')
  },

  /**
   * 根据ID获取用户信息
   * @param id 用户ID
   * @returns 用户信息
   */
  getUserById(id: number): Promise<ApiResponse<UserInfo>> {
    return request.get(`/user/${id}`)
  },

  /**
   * 更新用户信息
   * @param id 用户ID
   * @param data 用户数据
   * @returns 更新结果
   */
  updateUser(id: number, data: Partial<UserInfo>): Promise<ApiResponse> {
    return request.put(`/user/${id}`, data)
  },

  /**
   * 删除用户
   * @param id 用户ID
   * @returns 删除结果
   */
  deleteUser(id: number): Promise<ApiResponse> {
    return request.delete(`/user/${id}`)
  },

  /**
   * 获取所有用户
   * @returns 用户列表
   */
  getAllUsers(): Promise<ApiResponse<UserInfo[]>> {
    return request.get('/user/all')
  },

  /**
   * 检查用户名是否可用
   * @param username 用户名
   * @returns 检查结果
   */
  checkUsername(username: string): Promise<ApiResponse<any>> {
    return request.get('/user/check-username', { username })
  },

  /**
   * 检查手机号是否可用
   * @param phone 手机号
   * @returns 检查结果
   */
  checkPhone(phone: string): Promise<ApiResponse<any>> {
    return request.get('/user/check-phone', { phone })
  },

  /**
   * 健康检查
   * @returns 健康状态
   */
  healthCheck(): Promise<ApiResponse> {
    return request.get('/user/health')
  },

  /**
   * 获取系统统计数据（管理员）
   * @returns 统计数据
   */
  getStatistics(): Promise<ApiResponse<any>> {
    return request.get('/user/statistics')
  }
}

/**
 * 房源API模块
 */
export const propertyApi = {
  publish(data: Property): Promise<ApiResponse<number>> {
    return request.post('/property/publish', data)
  },
  update(data: Property): Promise<ApiResponse> {
    return request.put('/property/update', data)
  },
  adminUpdate(data: Property): Promise<ApiResponse> {
    return request.put('/property/admin-update', data)
  },
  delete(id: number): Promise<ApiResponse> {
    return request.delete(`/property/delete/${id}`)
  },
  online(id: number): Promise<ApiResponse> {
    return request.post(`/property/online/${id}`)
  },
  offline(id: number): Promise<ApiResponse> {
    return request.post(`/property/offline/${id}`)
  },
  approve(id: number): Promise<ApiResponse> {
    return request.post(`/property/approve/${id}`)
  },
  reject(id: number, reason: string): Promise<ApiResponse> {
    return request.post(`/property/reject/${id}?reason=${encodeURIComponent(reason)}`)
  },
  getDetail(id: number): Promise<ApiResponse<Property>> {
    return request.get(`/property/${id}`)
  },
  getMyList(): Promise<ApiResponse<Property[]>> {
    return request.get('/property/my-list')
  },
  getPendingList(): Promise<ApiResponse<Property[]>> {
    return request.get('/property/pending')
  },
  getAdminList(params: {
    pageNum?: number
    pageSize?: number
    keyword?: string
    status?: number | null
  }): Promise<ApiResponse<PageResult<Property>>> {
    return request.get('/property/admin-list', params)
  },
  getAdminStatistics(): Promise<ApiResponse<Record<string, number>>> {
    return request.get('/property/admin-statistics')
  },
  getList(params: any): Promise<ApiResponse<Property[]>> {
    return request.get('/property/list', params)
  }
}

/**
 * 收藏API模块
 */
export const favoriteApi = {
  add(propertyId: number): Promise<ApiResponse> {
    return request.post(`/favorite/add/${propertyId}`)
  },
  remove(propertyId: number): Promise<ApiResponse> {
    return request.delete(`/favorite/remove/${propertyId}`)
  },
  check(propertyId: number): Promise<ApiResponse<boolean>> {
    return request.get(`/favorite/check/${propertyId}`)
  },
  getMyList(): Promise<ApiResponse<Favorite[]>> {
    return request.get('/favorite/my-list')
  }
}

/**
 * 预约API模块
 */
export const appointmentApi = {
  create(data: Appointment): Promise<ApiResponse<number>> {
    return request.post('/appointment/create', data)
  },
  cancel(id: number): Promise<ApiResponse> {
    return request.post(`/appointment/cancel/${id}`)
  },
  confirm(id: number): Promise<ApiResponse> {
    return request.post(`/appointment/confirm/${id}`)
  },
  complete(id: number): Promise<ApiResponse> {
    return request.post(`/appointment/complete/${id}`)
  },
  getMyList(): Promise<ApiResponse<Appointment[]>> {
    return request.get('/appointment/my-list')
  },
  getLandlordList(): Promise<ApiResponse<Appointment[]>> {
    return request.get('/appointment/landlord-list')
  }
}

export default userApi