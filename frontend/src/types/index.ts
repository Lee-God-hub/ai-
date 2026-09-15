// 通用类型定义

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp?: number
}

export interface PageResult<T> {
  total: number
  records: T[]
}

export interface Property {
  id?: number
  landlordId?: number
  title: string
  description?: string
  price: number
  priceType?: number
  area: number
  bedrooms?: number
  bathrooms?: number
  orientation?: string
  floor?: string
  address: string
  city: string
  district?: string
  propertyType?: number
  transactionType?: number
  images?: string
  status?: number
  viewCount?: number
  rejectReason?: string
  createTime?: string
  updateTime?: string
}

export interface Favorite {
  id?: number
  userId?: number
  propertyId: number
  createTime?: string
  property?: Property
}

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
  property?: Property
  user?: UserInfo
}

export interface Message {
  id?: number
  userId?: number
  landlordId?: number
  propertyId: number
  content: string
  reply?: string
  isRead?: number
  senderRole?: number
  conversationId?: string
  createTime?: string
  updateTime?: string
  property?: Property
  user?: UserInfo
}

export interface UserInfo {
  id?: number
  username: string
  realName?: string
  phone: string
  email?: string
  role: number
  status?: number
  avatar?: string
  createTime?: string
}

export interface LoginRequest {
  username: string
  password: string
}

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

// 导出AI相关类型
export * from './ai'
