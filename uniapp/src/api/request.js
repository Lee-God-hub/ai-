// 请求配置
const config = {
  // H5开发环境（使用Vite代理，不需要完整URL）
  h5Dev: {
    baseURL: '/api'
  },
  // H5生产环境
  h5Prod: {
    baseURL: 'https://your-api-domain.com/api'
  },
  // App/小程序开发环境（直接请求后端）
  appDev: {
    baseURL: 'http://localhost:8080/api'
  },
  // App/小程序生产环境
  appProd: {
    baseURL: 'https://your-api-domain.com/api'
  }
}

// 获取当前环境配置
const getConfig = () => {
  // #ifdef H5
  return process.env.NODE_ENV === 'development' ? config.h5Dev : config.h5Prod
  // #endif
  
  // #ifndef H5
  return process.env.NODE_ENV === 'development' ? config.appDev : config.appProd
  // #endif
}

const currentConfig = getConfig()

/**
 * 封装uni.request请求
 */
class Request {
  constructor() {
    this.baseURL = currentConfig.baseURL
    this.timeout = 10000
    this.header = {
      'Content-Type': 'application/json'
    }
  }

  /**
   * 请求拦截器
   */
  interceptors = {
    request: (config) => {
      // 添加token（去掉已有的 Bearer 前缀，避免重复）
      const rawToken = uni.getStorageSync('token') || ''
      const token = rawToken.startsWith('Bearer ') ? rawToken.slice(7) : rawToken
      if (token) {
        config.header.Authorization = `Bearer ${token}`
      }

      // 显示加载提示
      if (config.loading !== false) {
        try {
          uni.showLoading({
            title: config.loadingText || '加载中...',
            mask: true
          })
        } catch (e) {}
      }

      return config
    },

    response: (response) => {
      // 隐藏加载提示
      try { uni.hideLoading() } catch (e) {}

      const { data, statusCode } = response

      // HTTP状态码检查
      if (statusCode !== 200) {
        this.handleError(statusCode, '网络请求失败')
        return Promise.reject(response || {})
      }

      // 业务状态码检查
      if (!data || typeof data !== 'object') {
        uni.showToast({
          title: '返回数据格式错误',
          icon: 'none',
          duration: 2000
        })
        return Promise.reject(data)
      }

      if (data.code === 200) {
        return data
      }

      // 401 未授权
      if (data.code === 401) {
        this.handleUnauthorized()
        return Promise.reject(data)
      }

      // 其他业务错误，不弹全局提示，由调用方决定是否提示
      return Promise.reject(data)
    }
  }

  /**
   * 处理未授权
   */
  handleUnauthorized() {
    uni.removeStorageSync('token')
    uni.removeStorageSync('userInfo')
    
    uni.showModal({
      title: '提示',
      content: '登录已过期，请重新登录',
      showCancel: false,
      success: () => {
        uni.reLaunch({
          url: '/pages/user/login'
        })
      }
    })
  }

  /**
   * 处理错误
   */
  handleError(statusCode, message) {
    let errorMessage = message
    
    switch (statusCode) {
      case 400:
        errorMessage = '请求参数错误'
        break
      case 401:
        errorMessage = '未授权，请登录'
        break
      case 403:
        errorMessage = '拒绝访问'
        break
      case 404:
        errorMessage = '请求地址出错'
        break
      case 408:
        errorMessage = '请求超时'
        break
      case 500:
        errorMessage = '服务器内部错误'
        break
      case 501:
        errorMessage = '服务未实现'
        break
      case 502:
        errorMessage = '网关错误'
        break
      case 503:
        errorMessage = '服务不可用'
        break
      case 504:
        errorMessage = '网关超时'
        break
      case 505:
        errorMessage = 'HTTP版本不受支持'
        break
      default:
        errorMessage = `连接错误${statusCode}`
    }
    
    uni.showToast({
      title: errorMessage,
      icon: 'none',
      duration: 2000
    })
  }

  /**
   * 通用请求方法
   */
  request(options) {
    // 合并配置
    const config = {
      url: this.baseURL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: { ...this.header, ...options.header },
      timeout: options.timeout || this.timeout,
      loading: options.loading,
      loadingText: options.loadingText
    }
    
    // 请求拦截
    const requestConfig = this.interceptors.request(config)
    
    return new Promise((resolve, reject) => {
      uni.request({
        ...requestConfig,
        success: (response) => {
          try {
            const result = this.interceptors.response(response)
            resolve(result)
          } catch (error) {
            reject(error)
          }
        },
        fail: (error) => {
          uni.hideLoading()
          
          // 网络错误处理
          let errorMessage = '网络连接失败'
          if (error.errMsg) {
            if (error.errMsg.includes('timeout')) {
              errorMessage = '请求超时，请检查网络'
            } else if (error.errMsg.includes('fail')) {
              errorMessage = '网络连接失败，请检查网络'
            }
          }
          
          uni.showToast({
            title: errorMessage,
            icon: 'none',
            duration: 2000
          })
          
          reject(error)
        }
      })
    })
  }

  /**
   * GET请求
   */
  get(url, params = {}, options = {}) {
    // 处理查询参数
    if (Object.keys(params).length > 0) {
      const queryString = Object.keys(params)
        .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
        .join('&')
      url += (url.includes('?') ? '&' : '?') + queryString
    }
    
    return this.request({
      url,
      method: 'GET',
      ...options
    })
  }

  /**
   * POST请求
   */
  post(url, data = {}, options = {}) {
    return this.request({
      url,
      method: 'POST',
      data,
      ...options
    })
  }

  /**
   * PUT请求
   */
  put(url, data = {}, options = {}) {
    return this.request({
      url,
      method: 'PUT',
      data,
      ...options
    })
  }

  /**
   * DELETE请求
   */
  delete(url, options = {}) {
    return this.request({
      url,
      method: 'DELETE',
      ...options
    })
  }

  /**
   * 上传文件
   */
  upload(url, filePath, options = {}) {
    const token = uni.getStorageSync('token')
    
    return new Promise((resolve, reject) => {
      uni.uploadFile({
        url: this.baseURL + url,
        filePath,
        name: options.name || 'file',
        formData: options.formData || {},
        header: {
          Authorization: token ? `Bearer ${token}` : '',
          ...options.header
        },
        success: (response) => {
          try {
            const data = JSON.parse(response.data)
            if (data.code === 200) {
              resolve(data)
            } else {
              uni.showToast({
                title: data.message || '上传失败',
                icon: 'none'
              })
              reject(data)
            }
          } catch (error) {
            uni.showToast({
              title: '上传失败',
              icon: 'none'
            })
            reject(error)
          }
        },
        fail: (error) => {
          uni.showToast({
            title: '上传失败',
            icon: 'none'
          })
          reject(error)
        }
      })
    })
  }
}

// 创建实例
const request = new Request()

export default request