"use strict";
const common_vendor = require("../../common/vendor.js");
const api_user = require("../../api/user.js");
require("../../api/request.js");
const _sfc_main = {
  data() {
    return {
      loginType: 0,
      // 0: 密码登录, 1: 验证码登录
      loginTabs: [
        { name: "密码登录" },
        { name: "验证码登录" }
      ],
      loading: false,
      // 密码登录表单
      passwordForm: {
        username: "",
        password: ""
      },
      // 手机登录表单
      phoneForm: {
        phone: "",
        verifyCode: ""
      },
      // 验证码倒计时
      codeCountdown: 0,
      codeTimer: null,
      // 忘记密码
      showForgotPassword: false
    };
  },
  computed: {
    canSendCode() {
      return /^1[3-9]\d{9}$/.test(this.phoneForm.phone);
    }
  },
  onUnload() {
    if (this.codeTimer) {
      clearInterval(this.codeTimer);
    }
  },
  methods: {
    // 密码登录
    async handlePasswordLogin() {
      if (!this.passwordForm.username) {
        common_vendor.index.showToast({
          title: "请输入用户名或手机号",
          icon: "none"
        });
        return;
      }
      if (!this.passwordForm.password) {
        common_vendor.index.showToast({
          title: "请输入密码",
          icon: "none"
        });
        return;
      }
      this.loading = true;
      try {
        const response = await api_user.userApi.login(this.passwordForm);
        if (response.code === 200) {
          common_vendor.index.setStorageSync("token", response.data.token);
          const userInfo = {
            id: response.data.userId,
            username: response.data.username,
            nickname: response.data.realName || response.data.username,
            phone: response.data.phone,
            email: response.data.email,
            role: response.data.role,
            avatar: response.data.avatar
          };
          common_vendor.index.setStorageSync("userInfo", userInfo);
          common_vendor.index.showToast({
            title: "登录成功",
            icon: "success"
          });
          setTimeout(() => {
            common_vendor.index.navigateBack() || common_vendor.index.switchTab({ url: "/pages/index/index" });
          }, 1500);
        } else {
          common_vendor.index.showToast({
            title: response.message || "登录失败",
            icon: "none"
          });
        }
      } catch (error) {
        console.error("登录失败:", error);
        common_vendor.index.showToast({
          title: "登录失败，请稍后重试",
          icon: "none"
        });
      } finally {
        this.loading = false;
      }
    },
    // 手机验证码登录（暂未开放）
    async handlePhoneLogin() {
      common_vendor.index.showToast({
        title: "验证码登录功能开发中，请使用密码登录",
        icon: "none"
      });
    },
    // 发送验证码
    async sendVerifyCode() {
      if (!this.canSendCode) {
        common_vendor.index.showToast({
          title: "请输入正确的手机号",
          icon: "none"
        });
        return;
      }
      try {
        const response = await api_user.userApi.sendVerifyCode(this.phoneForm.phone);
        if (response.code === 200) {
          common_vendor.index.showToast({
            title: "验证码已发送",
            icon: "success"
          });
          this.startCountdown();
        } else {
          common_vendor.index.showToast({
            title: response.message || "发送失败",
            icon: "none"
          });
        }
      } catch (error) {
        console.error("发送验证码失败:", error);
        common_vendor.index.showToast({
          title: "发送失败，请稍后重试",
          icon: "none"
        });
      }
    },
    // 开始倒计时
    startCountdown() {
      this.codeCountdown = 60;
      this.codeTimer = setInterval(() => {
        this.codeCountdown--;
        if (this.codeCountdown <= 0) {
          clearInterval(this.codeTimer);
          this.codeTimer = null;
        }
      }, 1e3);
    },
    // 跳转注册页
    goToRegister() {
      common_vendor.index.navigateTo({
        url: "/pages/user/register"
      });
    },
    // 微信登录
    wechatLogin() {
      common_vendor.index.getUserProfile({
        desc: "用于完善用户资料",
        success: (res) => {
          console.log("微信用户信息:", res.userInfo);
          common_vendor.index.showToast({
            title: "微信登录功能开发中",
            icon: "none"
          });
        }
      });
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.f($data.loginTabs, (tab, index, i0) => {
      return {
        a: common_vendor.t(tab.name),
        b: index,
        c: common_vendor.n({
          active: $data.loginType === index
        }),
        d: common_vendor.o(($event) => $data.loginType = index, index)
      };
    }),
    b: $data.loginType === 0
  }, $data.loginType === 0 ? {
    c: $data.passwordForm.username,
    d: common_vendor.o(($event) => $data.passwordForm.username = $event.detail.value),
    e: $data.passwordForm.password,
    f: common_vendor.o(($event) => $data.passwordForm.password = $event.detail.value),
    g: common_vendor.t($data.loading ? "登录中..." : "登录"),
    h: $data.loading ? 1 : "",
    i: common_vendor.o((...args) => $options.handlePasswordLogin && $options.handlePasswordLogin(...args))
  } : {
    j: $data.phoneForm.phone,
    k: common_vendor.o(($event) => $data.phoneForm.phone = $event.detail.value),
    l: $data.phoneForm.verifyCode,
    m: common_vendor.o(($event) => $data.phoneForm.verifyCode = $event.detail.value),
    n: common_vendor.t($data.codeCountdown > 0 ? `${$data.codeCountdown}s` : "获取验证码"),
    o: !$options.canSendCode || $data.codeCountdown > 0,
    p: common_vendor.o((...args) => $options.sendVerifyCode && $options.sendVerifyCode(...args)),
    q: common_vendor.t($data.loading ? "登录中..." : "登录"),
    r: $data.loading ? 1 : "",
    s: common_vendor.o((...args) => $options.handlePhoneLogin && $options.handlePhoneLogin(...args))
  }, {
    t: common_vendor.o((...args) => $options.goToRegister && $options.goToRegister(...args)),
    v: common_vendor.o(($event) => $data.showForgotPassword = true),
    w: common_vendor.o((...args) => $options.wechatLogin && $options.wechatLogin(...args))
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-ebed24a8"], ["__file", "C:/Users/34588/Desktop/ai租房test/ai-intelligent-rental-housing/code/ai租房系统/uniapp/src/pages/user/login.vue"]]);
wx.createPage(MiniProgramPage);
