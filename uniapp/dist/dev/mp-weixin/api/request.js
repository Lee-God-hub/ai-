"use strict";
const common_vendor = require("../common/vendor.js");
const config = {
  // H5开发环境（使用Vite代理，不需要完整URL）
  h5Dev: {
    baseURL: "/api"
  },
  // H5生产环境
  h5Prod: {
    baseURL: "https://your-api-domain.com/api"
  },
  // App/小程序开发环境（直接请求后端）
  appDev: {
    baseURL: "http://localhost:8080/api"
  },
  // App/小程序生产环境
  appProd: {
    baseURL: "https://your-api-domain.com/api"
  }
};
const getConfig = () => {
  return config.appDev;
};
const currentConfig = getConfig();
class Request {
  constructor() {
    /**
     * 请求拦截器
     */
    this.interceptors = {
      request: (config2) => {
        const token = common_vendor.index.getStorageSync("token");
        if (token) {
          config2.header.Authorization = `Bearer ${token}`;
        }
        if (config2.loading !== false) {
          common_vendor.index.showLoading({
            title: config2.loadingText || "加载中...",
            mask: true
          });
        }
        return config2;
      },
      response: (response) => {
        common_vendor.index.hideLoading();
        const { data, statusCode } = response;
        if (statusCode !== 200) {
          this.handleError(statusCode, "网络请求失败");
          return Promise.reject(response);
        }
        if (data.code !== 200) {
          if (data.code === 401) {
            this.handleUnauthorized();
            return Promise.reject(data);
          }
          if (data.code !== 200) {
            common_vendor.index.showToast({
              title: data.message || "请求失败",
              icon: "none",
              duration: 2e3
            });
          }
          return Promise.reject(data);
        }
        return data;
      }
    };
    this.baseURL = currentConfig.baseURL;
    this.timeout = 1e4;
    this.header = {
      "Content-Type": "application/json"
    };
  }
  /**
   * 处理未授权
   */
  handleUnauthorized() {
    common_vendor.index.removeStorageSync("token");
    common_vendor.index.removeStorageSync("userInfo");
    common_vendor.index.showModal({
      title: "提示",
      content: "登录已过期，请重新登录",
      showCancel: false,
      success: () => {
        common_vendor.index.reLaunch({
          url: "/pages/user/login"
        });
      }
    });
  }
  /**
   * 处理错误
   */
  handleError(statusCode, message) {
    let errorMessage = message;
    switch (statusCode) {
      case 400:
        errorMessage = "请求参数错误";
        break;
      case 401:
        errorMessage = "未授权，请登录";
        break;
      case 403:
        errorMessage = "拒绝访问";
        break;
      case 404:
        errorMessage = "请求地址出错";
        break;
      case 408:
        errorMessage = "请求超时";
        break;
      case 500:
        errorMessage = "服务器内部错误";
        break;
      case 501:
        errorMessage = "服务未实现";
        break;
      case 502:
        errorMessage = "网关错误";
        break;
      case 503:
        errorMessage = "服务不可用";
        break;
      case 504:
        errorMessage = "网关超时";
        break;
      case 505:
        errorMessage = "HTTP版本不受支持";
        break;
      default:
        errorMessage = `连接错误${statusCode}`;
    }
    common_vendor.index.showToast({
      title: errorMessage,
      icon: "none",
      duration: 2e3
    });
  }
  /**
   * 通用请求方法
   */
  request(options) {
    const config2 = {
      url: this.baseURL + options.url,
      method: options.method || "GET",
      data: options.data || {},
      header: { ...this.header, ...options.header },
      timeout: options.timeout || this.timeout,
      loading: options.loading,
      loadingText: options.loadingText
    };
    const requestConfig = this.interceptors.request(config2);
    return new Promise((resolve, reject) => {
      common_vendor.index.request({
        ...requestConfig,
        success: (response) => {
          try {
            const result = this.interceptors.response(response);
            resolve(result);
          } catch (error) {
            reject(error);
          }
        },
        fail: (error) => {
          common_vendor.index.hideLoading();
          let errorMessage = "网络连接失败";
          if (error.errMsg) {
            if (error.errMsg.includes("timeout")) {
              errorMessage = "请求超时，请检查网络";
            } else if (error.errMsg.includes("fail")) {
              errorMessage = "网络连接失败，请检查网络";
            }
          }
          common_vendor.index.showToast({
            title: errorMessage,
            icon: "none",
            duration: 2e3
          });
          reject(error);
        }
      });
    });
  }
  /**
   * GET请求
   */
  get(url, params = {}, options = {}) {
    if (Object.keys(params).length > 0) {
      const queryString = Object.keys(params).map((key) => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`).join("&");
      url += (url.includes("?") ? "&" : "?") + queryString;
    }
    return this.request({
      url,
      method: "GET",
      ...options
    });
  }
  /**
   * POST请求
   */
  post(url, data = {}, options = {}) {
    return this.request({
      url,
      method: "POST",
      data,
      ...options
    });
  }
  /**
   * PUT请求
   */
  put(url, data = {}, options = {}) {
    return this.request({
      url,
      method: "PUT",
      data,
      ...options
    });
  }
  /**
   * DELETE请求
   */
  delete(url, options = {}) {
    return this.request({
      url,
      method: "DELETE",
      ...options
    });
  }
  /**
   * 上传文件
   */
  upload(url, filePath, options = {}) {
    const token = common_vendor.index.getStorageSync("token");
    return new Promise((resolve, reject) => {
      common_vendor.index.uploadFile({
        url: this.baseURL + url,
        filePath,
        name: options.name || "file",
        formData: options.formData || {},
        header: {
          Authorization: token ? `Bearer ${token}` : "",
          ...options.header
        },
        success: (response) => {
          try {
            const data = JSON.parse(response.data);
            if (data.code === 200) {
              resolve(data);
            } else {
              common_vendor.index.showToast({
                title: data.message || "上传失败",
                icon: "none"
              });
              reject(data);
            }
          } catch (error) {
            common_vendor.index.showToast({
              title: "上传失败",
              icon: "none"
            });
            reject(error);
          }
        },
        fail: (error) => {
          common_vendor.index.showToast({
            title: "上传失败",
            icon: "none"
          });
          reject(error);
        }
      });
    });
  }
}
const request = new Request();
exports.request = request;
