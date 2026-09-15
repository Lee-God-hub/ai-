import { request } from './request'
import type { ApiResponse, PageResult, Property } from '@/types'

export const propertyApi = {
  // 获取房源列表（用户浏览，带筛选）
  getPropertyList(params: {
    pageNum?: number
    pageSize?: number
    city?: string
    district?: string
    propertyType?: number
    transactionType?: number
    minPrice?: number
    maxPrice?: number
  }) {
    return request.get<Property[]>('/property/list', params)
  },

  // 获取房东的房源列表
  getLandlordPropertyList() {
    return request.get<Property[]>('/property/my-list')
  },

  // 获取管理员房源列表（所有房源）
  getAdminPropertyList() {
    return request.get<Property[]>('/property/admin-list')
  },

  // 获取待审核列表（管理员）
  getPendingList() {
    return request.get<Property[]>('/property/pending')
  },

  // 根据ID获取房源详情
  getPropertyById(id: number) {
    return request.get<Property>(`/property/${id}`)
  },

  // 获取房源详情（别名方法）
  getDetail(id: number) {
    return request.get<Property>(`/property/${id}`)
  },

  // 发布房源（房东）
  createProperty(data: Property) {
    return request.post<number>('/property/publish', data)
  },

  // 更新房源
  updateProperty(data: Property) {
    return request.put<string>('/property/update', data)
  },

  // 删除房源
  deleteProperty(id: number) {
    return request.delete<string>(`/property/delete/${id}`)
  },

  // 审核通过（管理员）
  approveProperty(id: number) {
    return request.post<string>(`/property/approve/${id}`)
  },

  // 审核驳回（管理员）
  rejectProperty(id: number, reason: string) {
    return request.post<string>(`/property/reject/${id}?reason=${encodeURIComponent(reason)}`)
  },

  // 上架房源（房东）
  publishProperty(id: number) {
    return request.post<string>(`/property/online/${id}`)
  },

  // 下架房源（房东）
  offShelfProperty(id: number) {
    return request.post<string>(`/property/offline/${id}`)
  }
}
