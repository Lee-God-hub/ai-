/**
 * 用户信息接口
 * 定义用户数据的类型结构
 */
export interface UserInfo {
  id: number
  username: string
  realName?: string
  phone: string
  email?: string
  role: number
  status?: number
  avatar?: string
  lastLoginTime?: string
  createTime?: string
  updateTime?: string
  remark?: string
}

/**
 * 登录响应接口
 * 包含JWT token和用户信息
 */
export interface LoginResponse {
  token: string
  userId: number
  username: string
  realName?: string
  phone: string
  email?: string
  role: number
  avatar?: string
}

/**
 * 用户注册请求数据接口
 */
export interface RegisterRequest {
  username: string
  password: string
  phone: string
  realName?: string
  email?: string
  role?: number
}

/**
 * 用户登录请求数据接口
 */
export interface LoginRequest {
  username: string
  password: string
}

/**
 * API响应数据接口
 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

/**
 * 分页查询参数接口
 */
export interface PageParams {
  pageNum: number
  pageSize: number
}

/**
 * 分页响应数据接口
 */
export interface PageResponse<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
  pages: number
}

/**
 * 检查用户名响应数据接口
 */
export interface CheckUsernameResponse {
  exists: boolean
}

/**
 * 检查手机号响应数据接口
 */
export interface CheckPhoneResponse {
  exists: boolean
}

/**
 * 房源接口
 */
export interface Property {
  id?: number
  landlordId?: number
  title: string
  description?: string
  price: number
  priceType: number
  area: number
  bedrooms?: number
  bathrooms?: number
  address: string
  city: string
  district?: string
  propertyType: number
  transactionType: number
  images?: string
  status?: number
  viewCount?: number
  rejectReason?: string
  createTime?: string
  updateTime?: string
  landlord?: UserInfo
}

/**
 * 收藏接口
 */
export interface Favorite {
  id?: number
  userId?: number
  propertyId: number
  createTime?: string
  user?: UserInfo
  property?: Property
}

/**
 * 预约接口
 */
export interface Appointment {
  id?: number
  userId?: number
  propertyId: number
  landlordId?: number
  appointmentTime: string
  contactPhone: string
  remark?: string
  status?: number
  createTime?: string
  updateTime?: string
  user?: UserInfo
  property?: Property
}