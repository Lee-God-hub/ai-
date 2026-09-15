import { request } from './request'
import type { ApiResponse } from '@/types/user'
import type {
  QARequest,
  QAResponse,
  QAHistoryDTO,
  ContentRequest,
  ContentGenerationResponse,
  RecommendationDTO,
  RecommendClickRequest,
  UserBehaviorRequest
} from '@/types/ai'

/**
 * AI功能API模块
 * 封装所有AI相关的API请求，包括智能问答、文案生成、智能推荐
 * 所有接口均需要JWT认证，并根据用户角色进行权限控制
 * 
 * 权限说明：
 * - AI问答：普通用户(0)和房东(1)均可使用
 * - 文案生成：仅房东(1)可使用
 * - 智能推荐：仅普通用户(0)可使用
 */
export const aiApi = {
  // ==================== AI问答相关接口 ====================

  /**
   * 提问AI并获取回答
   * 用户或房东可以提问房产相关问题，获取AI回答
   * 
   * @param data 问答请求数据
   * @returns AI回答结果，包含答案和相关问题推荐
   */
  askQuestion(data: QARequest): Promise<ApiResponse<QAResponse>> {
    return request.post('/ai/qa/ask', data, { timeout: 60000 })
  },

  /**
   * 获取用户的问答历史记录
   * 返回最近的问答记录，用于展示历史对话
   * 
   * @param limit 查询数量限制，默认20条
   * @returns 问答历史列表
   */
  getQAHistory(limit: number = 20): Promise<ApiResponse<QAHistoryDTO[]>> {
    return request.get('/ai/qa/history', { limit })
  },

  // ==================== 文案生成相关接口 ====================

  /**
   * 生成房源宣传文案
   * 房东填写房源基础信息，一键生成优质宣传文案
   * 支持简短版和详细版两种风格
   * 
   * @param data 文案生成请求数据
   * @returns 生成的文案内容
   */
  generateContent(data: ContentRequest): Promise<ApiResponse<ContentGenerationResponse>> {
    return request.post('/ai/content/generate', data, { timeout: 300000 })
  },

  /**
   * 标记文案为已应用
   * 当房东将生成的文案应用到房源发布时调用
   * 
   * @param generationId 文案生成记录ID
   * @param propertyId 房源ID
   * @returns 标记结果
   */
  markContentAsApplied(generationId: number, propertyId: number): Promise<ApiResponse<string>> {
    return request.post(`/ai/content/apply?generationId=${generationId}&propertyId=${propertyId}`)
  },

  /**
   * 获取房东的文案生成历史
   * 返回最近的文案生成记录
   * 
   * @param limit 查询数量限制，默认10条
   * @returns 文案生成历史列表
   */
  getContentHistory(limit: number = 10): Promise<ApiResponse<ContentGenerationResponse[]>> {
    return request.get('/ai/content/history', { limit })
  },

  // ==================== 智能推荐相关接口 ====================

  /**
   * 获取智能推荐房源列表
   * 基于用户浏览历史、收藏偏好等进行个性化推荐
   * 
   * @param limit 推荐数量限制，默认10条
   * @returns 推荐房源列表，包含推荐理由和评分
   */
  getRecommendations(limit: number = 10): Promise<ApiResponse<RecommendationDTO[]>> {
    return request.get('/ai/recommend/properties', { limit })
  },

  /**
   * 记录用户点击推荐房源的行为
   * 用于优化推荐算法和统计点击率
   * 
   * @param data 点击记录请求数据
   * @returns 记录结果
   */
  recordRecommendClick(data: RecommendClickRequest): Promise<ApiResponse<string>> {
    return request.post('/ai/recommend/click', data)
  },

  /**
   * 记录用户行为日志
   * 用于推荐算法的数据支持
   * 
   * @param propertyId 房源ID
   * @param behaviorType 行为类型（view/favorite/appointment/unfavorite）
   * @returns 记录结果
   */
  recordUserBehavior(propertyId: number, behaviorType: string): Promise<ApiResponse<string>> {
    return request.post(`/ai/behavior/record?propertyId=${propertyId}&behaviorType=${behaviorType}`)
  },

  /**
   * AI服务健康检查
   * 用于监控服务状态
   * 
   * @returns 服务状态
   */
  healthCheck(): Promise<ApiResponse<string>> {
    return request.get('/ai/health')
  }
}

export default aiApi
