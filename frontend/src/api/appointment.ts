import { request } from './request'
import type { ApiResponse, PageResult, Appointment } from '@/types'

export const appointmentApi = {
  // 获取我的预约列表
  getMyList() {
    return request.get<Appointment[]>('/appointment/my-list')
  },

  // 获取房东预约列表
  getLandlordList() {
    return request.get<Appointment[]>('/appointment/landlord-list')
  },

  // 管理员获取预约列表
  getAdminAppointmentList(params: {
    pageNum?: number
    pageSize?: number
  }) {
    return request.get<Appointment[]>('/appointment/admin/list', params)
  },

  // 创建预约
  create(data: Appointment) {
    return request.post<string>('/appointment/create', data)
  },

  // 确认预约
  confirm(id: number) {
    return request.post<string>(`/appointment/confirm/${id}`)
  },

  // 完成预约
  complete(id: number) {
    return request.post<string>(`/appointment/complete/${id}`)
  },

  // 取消预约
  cancel(id: number) {
    return request.post<string>(`/appointment/cancel/${id}`)
  },

  // 删除预约（管理员）
  delete(id: number) {
    return request.delete<string>(`/appointment/${id}`)
  },

  // 交易对话相关
  getConversationMessages(appointmentId: number) {
    return request.get<any[]>(`/transaction-process/conversation/${appointmentId}`)
  },

  sendConversationMessage(data: {
    appointmentId: number
    senderId: number
    senderName: string
    senderRole: number
    messageContent: string
  }) {
    return request.post<any>('/transaction-process/conversation/send', data)
  },

  markConversationAsRead(appointmentId: number, userId: number) {
    return request.post<string>(`/transaction-process/conversation/mark-read/${appointmentId}`, { userId })
  }
}
