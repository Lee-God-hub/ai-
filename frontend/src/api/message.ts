import { request } from './request'
import type { ApiResponse, PageResult, Message } from '@/types'

export const messageApi = {
  getUserMessageList(params: {
    pageNum?: number
    pageSize?: number
  }) {
    return request.get<PageResult<Message>>('/message/user/list', params)
  },

  getLandlordMessageList(params: {
    pageNum?: number
    pageSize?: number
  }) {
    return request.get<PageResult<Message>>('/message/landlord/list', params)
  },

  getAdminMessageList(params: {
    pageNum?: number
    pageSize?: number
  }) {
    return request.get<PageResult<Message>>('/message/admin/list', params)
  },

  getMessageById(id: number) {
    return request.get<Message>(`/message/${id}`)
  },

  createMessage(data: Message) {
    return request.post<string>('/message/create', data)
  },

  replyMessage(id: number, reply: string) {
    return request.post<string>(`/message/reply/${id}`, { reply })
  },

  markAsRead(id: number) {
    return request.post<string>(`/message/read/${id}`)
  },

  deleteMessage(id: number) {
    return request.delete<string>(`/message/${id}`)
  },

  // 对话相关API
  getConversationMessages(propertyId: number) {
    return request.get<Message[]>(`/message/conversation/${propertyId}`)
  },

  sendConversationMessage(data: {
    propertyId: number
    content: string
  }) {
    return request.post<Message>('/message/conversation/send', data)
  },

  // 房东在对话中发送消息
  landlordSendConversationMessage(data: {
    propertyId: number
    userId: number
    content: string
  }) {
    return request.post<Message>('/message/landlord/conversation/send', data)
  },

  // 房东获取与某个用户的对话消息列表
  getLandlordConversationMessages(propertyId: number, userId: number) {
    return request.get<Message[]>(`/message/landlord/conversation/${propertyId}/${userId}`)
  }
}
