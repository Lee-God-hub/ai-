"use strict";
const common_vendor = require("../common/vendor.js");
const api_request = require("./request.js");
const userApi = {
  /**
   * 用户登录
   * @param {Object} data 登录数据
   * @param {string} data.username 用户名
   * @param {string} data.password 密码
   * @returns {Promise} 登录结果（包含token和用户信息）
   */
  login(data) {
    return api_request.request.post("/user/login", data, {
      loadingText: "登录中..."
    });
  },
  /**
   * 用户注册
   * @param {Object} data 注册数据
   * @returns {Promise} 注册结果
   */
  register(data) {
    return api_request.request.post("/user/register", data, {
      loadingText: "注册中..."
    });
  },
  /**
   * 退出登录（客户端清除token即可）
   */
  logout() {
    common_vendor.index.removeStorageSync("token");
    common_vendor.index.removeStorageSync("userInfo");
    return Promise.resolve({ code: 200, message: "退出成功" });
  },
  /**
   * 获取当前登录用户信息
   * @returns {Promise} 用户信息
   */
  getUserInfo() {
    return api_request.request.get("/user/current", {}, {
      loading: false
    });
  },
  /**
   * 更新用户信息
   * @param {number} id 用户ID
   * @param {Object} data 用户数据
   * @returns {Promise} 更新结果
   */
  updateUserInfo(id, data) {
    return api_request.request.put(`/user/${id}`, data);
  },
  /**
   * 检查用户名是否已存在
   * @param {string} username 用户名
   * @returns {Promise} 检查结果
   */
  checkUsername(username) {
    return api_request.request.get("/user/check-username", { username }, {
      loading: false
    });
  },
  /**
   * 检查手机号是否已存在
   * @param {string} phone 手机号
   * @returns {Promise} 检查结果
   */
  checkPhone(phone) {
    return api_request.request.get("/user/check-phone", { phone }, {
      loading: false
    });
  },
  /**
   * 健康检查
   * @returns {Promise} 服务状态
   */
  healthCheck() {
    return api_request.request.get("/user/health", {}, {
      loading: false
    });
  }
};
exports.userApi = userApi;
