/**
 * 工具函数库
 */

/**
 * 格式化价格
 * @param {number} price 价格
 * @returns {string} 格式化后的价格
 */
export function formatPrice(price, transactionType = null) {
  if (!price) return '0'
  
  let displayPrice = price
  
  if (transactionType === 0 && price > 10000) {
    displayPrice = Math.round(price / 12)
  }
  
  if (displayPrice >= 10000) {
    return (displayPrice / 10000).toFixed(1) + '万'
  }
  return Math.round(displayPrice).toLocaleString()
}

/**
 * 格式化时间
 * @param {string|Date} time 时间
 * @param {string} format 格式
 * @returns {string} 格式化后的时间
 */
export function formatTime(time, format = 'YYYY-MM-DD HH:mm:ss') {
  const date = new Date(time)
  
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  
  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 获取相对时间
 * @param {string|Date} time 时间
 * @returns {string} 相对时间描述
 */
export function getRelativeTime(time) {
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) { // 1分钟内
    return '刚刚'
  } else if (diff < 3600000) { // 1小时内
    return Math.floor(diff / 60000) + '分钟前'
  } else if (diff < 86400000) { // 1天内
    return Math.floor(diff / 3600000) + '小时前'
  } else if (diff < 2592000000) { // 30天内
    return Math.floor(diff / 86400000) + '天前'
  } else {
    return formatTime(date, 'YYYY-MM-DD')
  }
}

/**
 * 获取第一张图片
 * @param {string} images 图片字符串
 * @returns {string} 第一张图片URL
 */
export function getFirstImage(images) {
  if (!images) return '/static/placeholder.png'
  const imageList = images.split(',')
  return imageList[0] || '/static/placeholder.png'
}

/**
 * 防抖函数
 * @param {Function} func 要防抖的函数
 * @param {number} wait 等待时间
 * @returns {Function} 防抖后的函数
 */
export function debounce(func, wait) {
  let timeout
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout)
      func(...args)
    }
    clearTimeout(timeout)
    timeout = setTimeout(later, wait)
  }
}

/**
 * 节流函数
 * @param {Function} func 要节流的函数
 * @param {number} limit 时间限制
 * @returns {Function} 节流后的函数
 */
export function throttle(func, limit) {
  let inThrottle
  return function(...args) {
    if (!inThrottle) {
      func.apply(this, args)
      inThrottle = true
      setTimeout(() => inThrottle = false, limit)
    }
  }
}

/**
 * 深拷贝
 * @param {any} obj 要拷贝的对象
 * @returns {any} 拷贝后的对象
 */
export function deepClone(obj) {
  if (obj === null || typeof obj !== 'object') return obj
  if (obj instanceof Date) return new Date(obj.getTime())
  if (obj instanceof Array) return obj.map(item => deepClone(item))
  if (typeof obj === 'object') {
    const clonedObj = {}
    for (const key in obj) {
      if (obj.hasOwnProperty(key)) {
        clonedObj[key] = deepClone(obj[key])
      }
    }
    return clonedObj
  }
}

/**
 * 生成唯一ID
 * @returns {string} 唯一ID
 */
export function generateId() {
  return Date.now().toString(36) + Math.random().toString(36).substr(2)
}

/**
 * 验证手机号
 * @param {string} phone 手机号
 * @returns {boolean} 是否有效
 */
export function validatePhone(phone) {
  return /^1[3-9]\d{9}$/.test(phone)
}

/**
 * 验证邮箱
 * @param {string} email 邮箱
 * @returns {boolean} 是否有效
 */
export function validateEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

/**
 * 获取房源类型文本
 * @param {number} type 房源类型
 * @returns {string} 类型文本
 */
export function getPropertyTypeText(type) {
  const types = {
    0: '住宅',
    1: '公寓',
    2: '别墅'
  }
  return types[type] || '住宅'
}

/**
 * 获取交易类型文本
 * @param {number} type 交易类型
 * @returns {string} 类型文本
 */
export function getTransactionTypeText(type) {
  return type === 1 ? '出售' : '出租'
}

/**
 * 存储数据到本地
 * @param {string} key 键名
 * @param {any} data 数据
 */
export function setStorage(key, data) {
  try {
    uni.setStorageSync(key, data)
  } catch (error) {
    console.error('存储数据失败:', error)
  }
}

/**
 * 从本地获取数据
 * @param {string} key 键名
 * @param {any} defaultValue 默认值
 * @returns {any} 数据
 */
export function getStorage(key, defaultValue = null) {
  try {
    return uni.getStorageSync(key) || defaultValue
  } catch (error) {
    console.error('获取数据失败:', error)
    return defaultValue
  }
}

/**
 * 删除本地数据
 * @param {string} key 键名
 */
export function removeStorage(key) {
  try {
    uni.removeStorageSync(key)
  } catch (error) {
    console.error('删除数据失败:', error)
  }
}

/**
 * 显示Toast提示
 * @param {string} title 提示内容
 * @param {string} icon 图标类型
 * @param {number} duration 持续时间
 */
export function showToast(title, icon = 'none', duration = 2000) {
  uni.showToast({
    title,
    icon,
    duration
  })
}

/**
 * 显示加载提示
 * @param {string} title 提示内容
 */
export function showLoading(title = '加载中...') {
  uni.showLoading({
    title,
    mask: true
  })
}

/**
 * 隐藏加载提示
 */
export function hideLoading() {
  uni.hideLoading()
}

/**
 * 显示确认对话框
 * @param {string} content 内容
 * @param {string} title 标题
 * @returns {Promise<boolean>} 是否确认
 */
export function showConfirm(content, title = '提示') {
  return new Promise((resolve) => {
    uni.showModal({
      title,
      content,
      success: (res) => {
        resolve(res.confirm)
      }
    })
  })
}

/**
 * 跳转页面
 * @param {string} url 页面路径
 * @param {object} options 选项
 */
export function navigateTo(url, options = {}) {
  uni.navigateTo({
    url,
    ...options
  })
}

/**
 * 返回上一页
 * @param {number} delta 返回层数
 */
export function navigateBack(delta = 1) {
  uni.navigateBack({
    delta
  })
}

/**
 * 切换到Tab页面
 * @param {string} url 页面路径
 */
export function switchTab(url) {
  uni.switchTab({
    url
  })
}

/**
 * 重定向到页面
 * @param {string} url 页面路径
 */
export function redirectTo(url) {
  uni.redirectTo({
    url
  })
}

/**
 * 重新启动到页面
 * @param {string} url 页面路径
 */
export function reLaunch(url) {
  uni.reLaunch({
    url
  })
}