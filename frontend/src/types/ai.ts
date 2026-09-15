/**
 * AI功能相关TypeScript类型定义
 * 包含AI问答、文案生成、智能推荐的数据类型
 */

import type { Property } from './user'

// ==================== AI问答相关类型 ====================

/**
 * AI问答请求接口
 */
export interface QARequest {
  /** 用户提出的问题 */
  question: string
  /** 问题类型（可选），如：buy, rent, layout, policy, general */
  questionType?: string
}

/**
 * AI问答响应接口
 */
export interface QAResponse {
  /** 问答记录ID */
  qaId: number
  /** 用户提出的问题 */
  question: string
  /** AI生成的回答 */
  answer: string
  /** 问题类型 */
  questionType: string
  /** 相关推荐问题列表 */
  relatedQuestions: string[]
  /** 回答时间 */
  answerTime: string
}

/**
 * 问答历史记录接口
 */
export interface QAHistoryDTO {
  /** 问答记录ID */
  id: number
  /** 用户ID */
  userId: number
  /** 用户提出的问题 */
  question: string
  /** AI生成的回答 */
  answer: string
  /** 问题类型 */
  questionType: string
  /** 创建时间 */
  createTime: string
}

// ==================== 文案生成相关类型 ====================

/**
 * 文案生成请求接口
 */
export interface ContentRequest {
  /** 房源标题 */
  title: string
  /** 房源类型（1:住宅 2:公寓 3:别墅 5:写字楼） */
  propertyType: number
  /** 交易类型（1:出售 2:出租） */
  transactionType: number
  /** 价格 */
  price: number
  /** 面积（平方米） */
  area: number
  /** 卧室数量 */
  bedrooms?: number
  /** 卫生间数量 */
  bathrooms?: number
  /** 地址 */
  address: string
  /** 城市 */
  city: string
  /** 区域 */
  district?: string
  /** 配套设施（逗号分隔） */
  facilities?: string
  /** 装修情况 */
  decoration?: string
  /** 朝向 */
  orientation?: string
  /** 楼层 */
  floor?: string
  /** 生成版本（short:简短版 detailed:详细版） */
  version: string
}

/**
 * 文案生成响应接口
 */
export interface ContentGenerationResponse {
  /** 生成记录ID */
  generationId?: number
  /** 简短版文案（100-200字） */
  shortVersion: string
  /** 详细版文案（300-500字） */
  detailVersion: string
  /** 生成耗时（毫秒） */
  generationTime?: number
}

// ==================== 智能推荐相关类型 ====================

/**
 * 推荐房源DTO接口
 */
export interface RecommendationDTO {
  /** 房源信息 */
  property: Property
  /** 推荐评分（0-100） */
  recommendScore: number
  /** 推荐理由 */
  recommendReason: string
  /** 推荐来源 */
  source?: string
}

/**
 * 推荐点击记录请求接口
 */
export interface RecommendClickRequest {
  /** 房源ID */
  propertyId: number
  /** 推荐来源（recommendation, hot, similar等） */
  source: string
}

/**
 * 用户行为记录请求接口
 */
export interface UserBehaviorRequest {
  /** 房源ID */
  propertyId: number
  /** 行为类型（view, favorite, appointment, unfavorite） */
  behaviorType: 'view' | 'favorite' | 'appointment' | 'unfavorite'
}

// ==================== 推荐日志接口 ====================

/**
 * AI推荐日志接口
 */
export interface AIRecommendationLog {
  /** 日志ID */
  id: number
  /** 用户ID */
  userId: number
  /** 推荐的房源ID列表（逗号分隔） */
  recommendedPropertyIds: string
  /** 推荐算法版本 */
  algorithmVersion: string
  /** 是否点击 */
  isClicked: boolean
  /** 点击的房源ID */
  clickedPropertyId?: number
  /** 创建时间 */
  createTime: string
}

// ==================== 用户行为日志接口 ====================

/**
 * 用户行为日志接口
 */
export interface UserBehaviorLog {
  /** 日志ID */
  id: number
  /** 用户ID */
  userId: number
  /** 房源ID */
  propertyId: number
  /** 行为类型 */
  behaviorType: string
  /** 创建时间 */
  createTime: string
}
