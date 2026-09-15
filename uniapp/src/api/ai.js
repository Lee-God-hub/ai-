import request from './request.js'

/**
 * AI功能API模块
 * 封装所有AI相关的API请求，包括智能问答、文案生成、智能推荐
 */
export const aiApi = {
  // ==================== AI问答相关接口 ====================

  /**
   * 提问AI并获取回答
   * @param {Object} data 问答请求数据
   * @param {string} data.question 用户问题
   * @param {string} data.questionType 问题类型（可选）
   * @returns {Promise} AI回答结果
   */
  askQuestion(data) {
    return request.post('/ai/qa/ask', data, {
      loadingText: 'AI正在思考中...',
      timeout: 20000
    })
  },

  /**
   * 获取用户的问答历史记录
   * @param {number} limit 查询数量限制，默认20条
   * @returns {Promise} 问答历史列表
   */
  getQAHistory(limit = 20) {
    return request.get('/ai/qa/history', { limit }, {
      loading: false
    })
  },

  // ==================== 文案生成相关接口 ====================

  /**
   * 生成房源宣传文案
   * @param {Object} data 文案生成请求数据
   * @returns {Promise} 生成的文案内容
   */
  generateContent(data) {
    return request.post('/ai/content/generate', data, {
      loadingText: 'AI正在生成文案...',
      timeout: 25000
    })
  },

  /**
   * 标记文案为已应用
   * @param {number} generationId 文案生成记录ID
   * @param {number} propertyId 房源ID
   * @returns {Promise} 标记结果
   */
  markContentAsApplied(generationId, propertyId) {
    return request.post(`/ai/content/apply?generationId=${generationId}&propertyId=${propertyId}`, {})
  },

  /**
   * 获取房东的文案生成历史
   * @param {number} limit 查询数量限制，默认10条
   * @returns {Promise} 文案生成历史列表
   */
  getContentHistory(limit = 10) {
    return request.get('/ai/content/history', { limit })
  },

  // ==================== 智能推荐相关接口 ====================

  /**
   * 获取智能推荐房源列表
   * @param {number} limit 推荐数量限制，默认10条
   * @returns {Promise} 推荐房源列表
   */
  getRecommendations(limit = 10) {
    return request.get('/ai/recommend/properties', { limit }, {
      loadingText: 'AI正在推荐房源...'
    })
  },

  /**
   * 记录用户点击推荐房源的行为
   * @param {Object} data 点击记录请求数据
   * @param {number} data.propertyId 房源ID
   * @param {string} data.source 推荐来源
   * @returns {Promise} 记录结果
   */
  recordRecommendClick(data) {
    return request.post('/ai/recommend/click', data, {
      loading: false
    })
  },

  /**
   * 记录用户行为日志
   * @param {number} propertyId 房源ID
   * @param {string} behaviorType 行为类型（view/favorite/appointment/unfavorite）
   * @returns {Promise} 记录结果
   */
  recordUserBehavior(propertyId, behaviorType) {
    return request.post(`/ai/behavior/record?propertyId=${propertyId}&behaviorType=${behaviorType}`, {}, {
      loading: false
    })
  },

  /**
   * AI服务健康检查
   * @returns {Promise} 服务状态
   */
  healthCheck() {
    return request.get('/ai/health', {}, {
      loading: false
    })
  }
}

export default aiApi