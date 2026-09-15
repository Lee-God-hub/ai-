import { request } from './request'
import type { ApiResponse, PageResult, Favorite } from '@/types'

export const favoriteApi = {
  // 获取我的收藏列表
  getMyList() {
    return request.get<Favorite[]>('/favorite/my-list')
  },

  // 管理员获取收藏列表
  getAdminFavoriteList(params: {
    pageNum?: number
    pageSize?: number
  }) {
    return request.get<Favorite[]>('/favorite/admin/list', params)
  },

  // 检查是否已收藏
  check(propertyId: number) {
    return request.get<boolean>(`/favorite/check/${propertyId}`)
  },

  // 添加收藏
  add(propertyId: number) {
    return request.post<string>(`/favorite/add/${propertyId}`)
  },

  // 取消收藏
  remove(propertyId: number) {
    return request.delete<string>(`/favorite/remove/${propertyId}`)
  },

  // 删除收藏记录（管理员）
  delete(id: number) {
    return request.delete<string>(`/favorite/${id}`)
  }
}
