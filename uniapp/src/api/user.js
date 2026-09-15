import request from './request.js'

/**
 * 用户相关API
 * 对应后端 UserController (@RequestMapping("/user"))
 */
export const userApi = {
  /**
   * 用户登录
   * @param {Object} data 登录数据
   * @param {string} data.username 用户名
   * @param {string} data.password 密码
   * @returns {Promise} 登录结果（包含token和用户信息）
   */
  login(data) {
    return request.post('/user/login', data, {
      loadingText: '登录中...'
    })
  },

  /**
   * 用户注册
   * @param {Object} data 注册数据
   * @returns {Promise} 注册结果
   */
  register(data) {
    return request.post('/user/register', data, {
      loadingText: '注册中...'
    })
  },

  /**
   * 退出登录（客户端清除token即可）
   */
  logout() {
    uni.removeStorageSync('token')
    uni.removeStorageSync('userInfo')
    return Promise.resolve({ code: 200, message: '退出成功' })
  },

  /**
   * 获取当前登录用户信息
   * @returns {Promise} 用户信息
   */
  getUserInfo() {
    return request.get('/user/current', {}, {
      loading: false
    })
  },

  /**
   * 更新用户信息
   * @param {number} id 用户ID
   * @param {Object} data 用户数据
   * @returns {Promise} 更新结果
   */
  updateUserInfo(id, data) {
    return request.put(`/user/${id}`, data)
  },

  /**
   * 检查用户名是否已存在
   * @param {string} username 用户名
   * @returns {Promise} 检查结果
   */
  checkUsername(username) {
    return request.get('/user/check-username', { username }, {
      loading: false
    })
  },

  /**
   * 检查手机号是否已存在
   * @param {string} phone 手机号
   * @returns {Promise} 检查结果
   */
  checkPhone(phone) {
    return request.get('/user/check-phone', { phone }, {
      loading: false
    })
  },

  /**
   * 健康检查
   * @returns {Promise} 服务状态
   */
  healthCheck() {
    return request.get('/user/health', {}, {
      loading: false
    })
  },

  /**
   * 发送对话消息给房东
   * @param {Object} data 消息数据
   * @param {number} data.propertyId 房源ID
   * @param {string} data.content 消息内容
   * @returns {Promise} 发送结果
   */
  sendConversationMessage(data) {
    return request.post('/message/conversation/send', data)
  }
}

export const messageApi = {
  /**
   * 获取房源对话消息列表
   * @param {number} propertyId 房源ID
   * @returns {Promise} 消息列表
   */
  getConversationMessages(propertyId) {
    return request.get(`/message/conversation/${propertyId}`, {}, {
      loading: false
    })
  },

  /**
   * 发送对话消息给房东
   * @param {Object} data 消息数据
   * @param {number} data.propertyId 房源ID
   * @param {string} data.content 消息内容
   * @returns {Promise} 发送结果
   */
  sendConversationMessage(data) {
    return request.post('/message/conversation/send', data)
  },

  /**
   * 获取用户消息列表（用于生成对话列表）
   * @param {Object} params 分页参数
   * @param {number} params.pageNum 页码
   * @param {number} params.pageSize 每页数量
   * @returns {Promise} 消息分页列表
   */
  getUserMessageList(params) {
    return request.get('/message/user/list', params, {
      loading: false
    })
  },

  /**
   * 获取房东消息列表
   * @param {Object} params 分页参数
   * @param {number} params.pageNum 页码
   * @param {number} params.pageSize 每页数量
   * @returns {Promise} 消息分页列表
   */
  getLandlordMessageList(params) {
    return request.get('/message/landlord/list', params, {
      loading: false
    })
  },

  /**
   * 房东标记消息已读
   * @param {number} id 消息ID
   * @returns {Promise} 操作结果
   */
  markMessageAsRead(id) {
    return request.post(`/message/read/${id}`)
  },

  /**
   * 删除消息
   * @param {number} id 消息ID
   * @returns {Promise} 删除结果
   */
  deleteMessage(id) {
    return request.delete(`/message/${id}`)
  },

  /**
   * 房东获取与某个用户的对话消息列表
   * @param {number} propertyId 房源ID
   * @param {number} userId 用户ID
   * @returns {Promise} 消息列表
   */
  getLandlordConversationMessages(propertyId, userId) {
    return request.get(`/message/landlord/conversation/${propertyId}/${userId}`, {}, {
      loading: false
    })
  },

  /**
   * 房东在对话中发送消息
   * @param {Object} data 消息数据
   * @param {number} data.propertyId 房源ID
   * @param {number} data.userId 用户ID
   * @param {string} data.content 消息内容
   * @returns {Promise} 发送结果
   */
  landlordSendConversationMessage(data) {
    return request.post('/message/landlord/conversation/send', data)
  }
}

export default userApi
