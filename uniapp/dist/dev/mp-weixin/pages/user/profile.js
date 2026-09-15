"use strict";
const common_vendor = require("../../common/vendor.js");
const api_user = require("../../api/user.js");
require("../../api/request.js");
const _sfc_main = {
  data() {
    return {
      userInfo: {},
      isLoggedIn: false,
      favoriteCount: 0,
      propertyCount: 0,
      showSettings: false,
      showAbout: false
    };
  },
  onShow() {
    this.checkLoginStatus();
    if (this.isLoggedIn) {
      this.loadUserData();
    }
  },
  methods: {
    // 获取用户名首字母
    getUserInitial() {
      if (this.userInfo.nickname && this.userInfo.nickname.length > 0) {
        return this.userInfo.nickname.charAt(0);
      }
      return "用";
    },
    // 检查登录状态
    checkLoginStatus() {
      const token = common_vendor.index.getStorageSync("token");
      const userInfo = common_vendor.index.getStorageSync("userInfo");
      this.isLoggedIn = !!token;
      if (userInfo) {
        this.userInfo = userInfo;
      }
    },
    // 加载用户数据
    async loadUserData() {
      try {
        const userResponse = await api_user.userApi.getUserInfo();
        if (userResponse.code === 200 && userResponse.data) {
          this.userInfo = {
            id: userResponse.data.id,
            username: userResponse.data.username,
            nickname: userResponse.data.realName || userResponse.data.username,
            phone: userResponse.data.phone,
            email: userResponse.data.email,
            role: userResponse.data.role,
            avatar: userResponse.data.avatar
          };
          common_vendor.index.setStorageSync("userInfo", this.userInfo);
        }
      } catch (error) {
        console.error("加载用户数据失败:", error);
      }
    },
    // 选择头像
    chooseAvatar() {
      if (!this.isLoggedIn) {
        this.goToLogin();
        return;
      }
      common_vendor.index.chooseImage({
        count: 1,
        sizeType: ["compressed"],
        sourceType: ["album", "camera"],
        success: (res) => {
          console.log("选择头像:", res.tempFilePaths[0]);
        }
      });
    },
    // 跳转页面
    goToPage(url) {
      if (!this.isLoggedIn && (url.includes("favorites") || url.includes("landlord"))) {
        this.goToLogin();
        return;
      }
      common_vendor.index.navigateTo({
        url
      });
    },
    // 跳转登录页
    goToLogin() {
      common_vendor.index.navigateTo({
        url: "/pages/user/login"
      });
    },
    // 退出登录
    handleLogout() {
      common_vendor.index.showModal({
        title: "提示",
        content: "确定要退出登录吗？",
        success: (res) => {
          if (res.confirm) {
            common_vendor.index.removeStorageSync("token");
            common_vendor.index.removeStorageSync("userInfo");
            this.isLoggedIn = false;
            this.userInfo = {};
            this.favoriteCount = 0;
            this.propertyCount = 0;
            common_vendor.index.showToast({
              title: "已退出登录",
              icon: "success"
            });
          }
        }
      });
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.t($options.getUserInitial()),
    b: common_vendor.o((...args) => $options.chooseAvatar && $options.chooseAvatar(...args)),
    c: common_vendor.t($data.userInfo.nickname || "未登录"),
    d: common_vendor.t($data.userInfo.description || "这个人很懒，什么都没留下"),
    e: $data.favoriteCount > 0
  }, $data.favoriteCount > 0 ? {
    f: common_vendor.t($data.favoriteCount)
  } : {}, {
    g: common_vendor.o(($event) => $options.goToPage("/pages/favorites/index")),
    h: $data.propertyCount > 0
  }, $data.propertyCount > 0 ? {
    i: common_vendor.t($data.propertyCount)
  } : {}, {
    j: common_vendor.o(($event) => $options.goToPage("/pages/landlord/manage")),
    k: common_vendor.o(($event) => $options.goToPage("/pages/landlord/publish")),
    l: common_vendor.o(($event) => $options.goToPage("/pages/ai/recommend")),
    m: common_vendor.o(($event) => $options.goToPage("/pages/ai/qa")),
    n: common_vendor.o(($event) => $data.showSettings = true),
    o: common_vendor.o(($event) => $data.showAbout = true),
    p: !$data.isLoggedIn
  }, !$data.isLoggedIn ? {
    q: common_vendor.o((...args) => $options.goToLogin && $options.goToLogin(...args))
  } : {
    r: common_vendor.o((...args) => $options.handleLogout && $options.handleLogout(...args))
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-f6b4f04d"], ["__file", "C:/Users/34588/Desktop/ai租房test/ai-intelligent-rental-housing/code/ai租房系统/uniapp/src/pages/user/profile.vue"]]);
wx.createPage(MiniProgramPage);
