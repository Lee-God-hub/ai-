"use strict";
Object.defineProperty(exports, Symbol.toStringTag, { value: "Module" });
const common_vendor = require("./common/vendor.js");
if (!Math) {
  "./pages/index/index.js";
  "./pages/search/search.js";
  "./pages/property/detail.js";
  "./pages/ai/qa.js";
  "./pages/ai/recommend.js";
  "./pages/user/profile.js";
  "./pages/user/login.js";
  "./pages/user/register.js";
  "./pages/landlord/publish.js";
  "./pages/landlord/manage.js";
  "./pages/favorites/index.js";
  "./pages/test/test.js";
}
const _sfc_main = {
  onLaunch: function() {
    console.log("App Launch");
    this.checkLogin();
  },
  onShow: function() {
    console.log("App Show");
  },
  onHide: function() {
    console.log("App Hide");
  },
  methods: {
    // 检查登录状态
    checkLogin() {
      const token = common_vendor.index.getStorageSync("token");
      if (token) {
        this.validateToken(token);
      }
    },
    // 验证token
    validateToken(token) {
      console.log("验证token:", token);
    }
  }
};
const App = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__file", "C:/Users/34588/Desktop/ai租房test/ai-intelligent-rental-housing/code/ai租房系统/uniapp/src/App.vue"]]);
function createApp() {
  const app = common_vendor.createSSRApp(App);
  return {
    app
  };
}
createApp().app.mount("#app");
exports.createApp = createApp;
