import request from './request.js'

/**
 * 房源相关API
 * 对应后端 PropertyController (@RequestMapping("/property"))
 *         FavoriteController (@RequestMapping("/favorite"))
 *         AppointmentController (@RequestMapping("/appointment"))
 */
export const propertyApi = {
  // ==================== 房源基本操作 ====================

  /**
   * 获取房源列表（带筛选条件）
   * @param {Object} params 查询参数
   * @returns {Promise} 房源列表
   */
  getPropertyList(params = {}) {
    return request.get('/property/list', params)
  },

  /**
   * 获取房源详情
   * @param {number} id 房源ID
   * @returns {Promise} 房源详情
   */
  getPropertyDetail(id) {
    return request.get(`/property/${id}`)
  },

  /**
   * 搜索房源（复用list接口带筛选参数）
   * @param {Object} params 搜索参数
   * @returns {Promise} 搜索结果
   */
  searchProperties(params) {
    return request.get('/property/list', params)
  },

  /**
   * 获取热门房源（复用list接口）
   * @param {number} limit 数量限制
   * @returns {Promise} 热门房源列表
   */
  getHotProperties(limit = 10) {
    return request.get('/property/list', { pageSize: limit })
  },

  /**
   * 发布房源（房东）
   * @param {Object} data 房源数据
   * @returns {Promise} 发布结果
   */
  publishProperty(data) {
    return request.post('/property/publish', data, {
      loadingText: '发布中...'
    })
  },

  /**
   * 更新房源（房东）
   * @param {Object} data 更新数据（需包含id）
   * @returns {Promise} 更新结果
   */
  updateProperty(data) {
    return request.put('/property/update', data)
  },

  /**
   * 删除房源（房东）
   * @param {number} id 房源ID
   * @returns {Promise} 删除结果
   */
  deleteProperty(id) {
    return request.delete(`/property/delete/${id}`)
  },

  /**
   * 上架房源（房东）
   * @param {number} id 房源ID
   * @returns {Promise} 上架结果
   */
  onlineProperty(id) {
    return request.post(`/property/online/${id}`)
  },

  /**
   * 下架房源（房东）
   * @param {number} id 房源ID
   * @returns {Promise} 下架结果
   */
  offlineProperty(id) {
    return request.post(`/property/offline/${id}`)
  },

  /**
   * 获取房东的房源列表
   * @returns {Promise} 房源列表
   */
  getMyList() {
    return request.get('/property/my-list')
  },

  // ==================== 收藏相关操作 ====================

  /**
   * 收藏房源
   * @param {number} propertyId 房源ID
   * @returns {Promise} 收藏结果
   */
  favoriteProperty(propertyId) {
    return request.post(`/favorite/add/${propertyId}`, {}, {
      loading: false
    })
  },

  /**
   * 取消收藏
   * @param {number} propertyId 房源ID
   * @returns {Promise} 取消收藏结果
   */
  unfavoriteProperty(propertyId) {
    return request.delete(`/favorite/remove/${propertyId}`)
  },

  /**
   * 检查是否已收藏
   * @param {number} propertyId 房源ID
   * @returns {Promise} 是否已收藏
   */
  checkFavorite(propertyId) {
    return request.get(`/favorite/check/${propertyId}`, {}, {
      loading: false
    })
  },

  /**
   * 获取收藏列表
   * @returns {Promise} 收藏列表（带房源信息）
   */
  getFavoriteList() {
    return request.get('/favorite/my-list')
  },

  // ==================== 预约相关操作 ====================

  /**
   * 预约看房
   * @param {Object} data 预约数据
   * @returns {Promise} 预约结果
   */
  makeAppointment(data) {
    return request.post('/appointment/create', data, {
      loadingText: '预约中...'
    })
  },

  /**
   * 取消预约
   * @param {number} id 预约ID
   * @returns {Promise} 取消结果
   */
  cancelAppointment(id) {
    return request.post(`/appointment/cancel/${id}`)
  },

  /**
   * 完成看房
   * @param {number} id 预约ID
   * @returns {Promise} 完成结果
   */
  completeAppointment(id) {
    return request.post(`/appointment/complete/${id}`)
  },

  /**
   * 获取我的预约列表
   * @returns {Promise} 预约列表
   */
  getMyAppointments() {
    return request.get('/appointment/my-list')
  },

  // ==================== 房东预约管理 ====================

  /**
   * 获取房东预约列表
   * @returns {Promise} 预约列表
   */
  getLandlordAppointments() {
    return request.get('/appointment/landlord-list')
  },

  /**
   * 房东确认预约
   * @param {number} id 预约ID
   * @returns {Promise} 确认结果
   */
  confirmAppointment(id) {
    return request.post(`/appointment/confirm/${id}`)
  }
}

export default propertyApi
