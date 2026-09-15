/**
 * 常量定义
 */

// 房源类型（与数据库保持一致）
export const PROPERTY_TYPES = {
  RESIDENTIAL: 0,    // 住宅
  APARTMENT: 1,      // 公寓
  VILLA: 2           // 别墅
}

// 房源类型列表（用于下拉选择）
export const PROPERTY_TYPE_LIST = [
  { label: '住宅', value: PROPERTY_TYPES.RESIDENTIAL },
  { label: '公寓', value: PROPERTY_TYPES.APARTMENT },
  { label: '别墅', value: PROPERTY_TYPES.VILLA }
]

// 房源类型文本映射
export const PROPERTY_TYPE_TEXTS = {
  [PROPERTY_TYPES.RESIDENTIAL]: '住宅',
  [PROPERTY_TYPES.APARTMENT]: '公寓',
  [PROPERTY_TYPES.VILLA]: '别墅'
}

// 交易类型
export const TRANSACTION_TYPES = {
  RENT: 0,    // 出租
  SALE: 1     // 出售
}

// 交易类型列表（用于下拉选择）
export const TRANSACTION_TYPE_LIST = [
  { label: '出租', value: TRANSACTION_TYPES.RENT },
  { label: '出售', value: TRANSACTION_TYPES.SALE }
]

// 交易类型文本映射
export const TRANSACTION_TYPE_TEXTS = {
  [TRANSACTION_TYPES.RENT]: '出租',
  [TRANSACTION_TYPES.SALE]: '出售'
}

// 用户类型
export const USER_TYPES = {
  TENANT: 0,     // 普通用户/租客
  LANDLORD: 1    // 房东
}

// 用户类型文本映射
export const USER_TYPE_TEXTS = {
  [USER_TYPES.TENANT]: '普通用户',
  [USER_TYPES.LANDLORD]: '房东'
}

// 房源状态
export const PROPERTY_STATUS = {
  OFFLINE: 0,    // 下线
  ONLINE: 1      // 在线
}

// 房源状态文本映射
export const PROPERTY_STATUS_TEXTS = {
  [PROPERTY_STATUS.OFFLINE]: '下线',
  [PROPERTY_STATUS.ONLINE]: '在线'
}

// 用户行为类型
export const BEHAVIOR_TYPES = {
  VIEW: 'view',              // 浏览
  FAVORITE: 'favorite',      // 收藏
  UNFAVORITE: 'unfavorite',  // 取消收藏
  APPOINTMENT: 'appointment'  // 预约看房
}

// 问题类型
export const QUESTION_TYPES = {
  BUY: 'buy',        // 购房相关
  RENT: 'rent',      // 租房相关
  LAYOUT: 'layout',  // 户型相关
  POLICY: 'policy',  // 政策相关
  GENERAL: 'general' // 一般问题
}

// API响应状态码
export const API_CODES = {
  SUCCESS: 200,           // 成功
  BAD_REQUEST: 400,       // 请求错误
  UNAUTHORIZED: 401,      // 未授权
  FORBIDDEN: 403,         // 禁止访问
  NOT_FOUND: 404,         // 未找到
  SERVER_ERROR: 500       // 服务器错误
}

// 存储键名
export const STORAGE_KEYS = {
  TOKEN: 'token',
  USER_INFO: 'userInfo',
  APP_SETTINGS: 'appSettings',
  SEARCH_HISTORY: 'searchHistory',
  BROWSE_HISTORY: 'browseHistory'
}

// 页面路径
export const PAGE_PATHS = {
  INDEX: '/pages/index/index',
  SEARCH: '/pages/search/search',
  PROPERTY_DETAIL: '/pages/property/detail',
  AI_QA: '/pages/ai/qa',
  AI_RECOMMEND: '/pages/ai/recommend',
  USER_PROFILE: '/pages/user/profile',
  USER_LOGIN: '/pages/user/login',
  USER_REGISTER: '/pages/user/register',
  FAVORITES: '/pages/favorites/index',
  LANDLORD_PUBLISH: '/pages/landlord/publish',
  LANDLORD_MANAGE: '/pages/landlord/manage'
}

// 默认配置
export const DEFAULT_CONFIG = {
  PAGE_SIZE: 10,           // 默认分页大小
  MAX_IMAGES: 9,           // 最大图片数量
  MAX_TITLE_LENGTH: 50,    // 最大标题长度
  MAX_DESC_LENGTH: 500,    // 最大描述长度
  CODE_COUNTDOWN: 60,      // 验证码倒计时秒数
  REQUEST_TIMEOUT: 10000   // 请求超时时间
}

// 正则表达式
export const REGEX_PATTERNS = {
  PHONE: /^1[3-9]\d{9}$/,                    // 手机号
  EMAIL: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,       // 邮箱
  PASSWORD: /^.{6,}$/,                       // 密码（至少6位）
  VERIFY_CODE: /^\d{6}$/,                    // 验证码（6位数字）
  PRICE: /^\d+(\.\d{1,2})?$/,               // 价格（支持小数点后两位）
  AREA: /^\d+(\.\d{1,2})?$/                 // 面积（支持小数点后两位）
}

// 错误消息
export const ERROR_MESSAGES = {
  NETWORK_ERROR: '网络连接失败，请检查网络',
  REQUEST_TIMEOUT: '请求超时，请稍后重试',
  SERVER_ERROR: '服务器异常，请稍后重试',
  UNAUTHORIZED: '登录已过期，请重新登录',
  FORBIDDEN: '没有权限访问',
  NOT_FOUND: '请求的资源不存在',
  VALIDATION_ERROR: '数据验证失败',
  UPLOAD_ERROR: '上传失败，请重试'
}

// 成功消息
export const SUCCESS_MESSAGES = {
  LOGIN_SUCCESS: '登录成功',
  REGISTER_SUCCESS: '注册成功',
  LOGOUT_SUCCESS: '已退出登录',
  PUBLISH_SUCCESS: '发布成功',
  UPDATE_SUCCESS: '更新成功',
  DELETE_SUCCESS: '删除成功',
  FAVORITE_SUCCESS: '收藏成功',
  UNFAVORITE_SUCCESS: '已取消收藏',
  APPOINTMENT_SUCCESS: '预约成功',
  UPLOAD_SUCCESS: '上传成功',
  COPY_SUCCESS: '已复制到剪贴板'
}

// 配套设施选项
export const FACILITIES_OPTIONS = [
  '地铁', '公交', '学校', '医院', '商场', '公园',
  '停车位', '电梯', '阳台', '空调', '冰箱', '洗衣机',
  '热水器', '宽带', '有线电视', '燃气', '暖气', '物业'
]

// 装修情况选项
export const DECORATION_OPTIONS = [
  '毛坯', '简装', '精装', '豪装'
]

// 朝向选项
export const ORIENTATION_OPTIONS = [
  '东', '南', '西', '北', '东南', '东北', '西南', '西北', '南北通透'
]

// AI文案生成版本
export const AI_CONTENT_VERSIONS = {
  SHORT: 'short',      // 简短版
  DETAILED: 'detailed' // 详细版
}

// 推荐来源
export const RECOMMEND_SOURCES = {
  RECOMMENDATION: 'recommendation',  // AI推荐
  HOT: 'hot',                       // 热门推荐
  SIMILAR: 'similar'                // 相似推荐
}