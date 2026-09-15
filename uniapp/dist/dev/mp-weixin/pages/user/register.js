"use strict";
const common_vendor = require("../../common/vendor.js");
const api_user = require("../../api/user.js");
require("../../api/request.js");
const _sfc_main = {
  data() {
    return {
      loading: false,
      agreeTerms: false,
      // 注册表单
      registerForm: {
        phone: "",
        verifyCode: "",
        nickname: "",
        password: "",
        confirmPassword: "",
        userType: 0
        // 0: 普通用户, 1: 房东
      },
      // 验证码倒计时
      codeCountdown: 0,
      codeTimer: null
    };
  },
  computed: {
    canSendCode() {
      return /^1[3-9]\d{9}$/.test(this.registerForm.phone);
    }
  },
  onUnload() {
    if (this.codeTimer) {
      clearInterval(this.codeTimer);
    }
  },
  methods: {
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
        const response = await api_user.userApi.sendVerifyCode(this.registerForm.phone);
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
    // 注册
    async handleRegister() {
      if (!this.registerForm.phone) {
        common_vendor.index.showToast({
          title: "请输入手机号",
          icon: "none"
        });
        return;
      }
      if (!this.registerForm.nickname) {
        common_vendor.index.showToast({
          title: "请输入用户名",
          icon: "none"
        });
        return;
      }
      if (!this.registerForm.password) {
        common_vendor.index.showToast({
          title: "请输入密码",
          icon: "none"
        });
        return;
      }
      if (this.registerForm.password !== this.registerForm.confirmPassword) {
        common_vendor.index.showToast({
          title: "两次输入的密码不一致",
          icon: "none"
        });
        return;
      }
      if (!this.agreeTerms) {
        common_vendor.index.showToast({
          title: "请先同意用户协议和隐私政策",
          icon: "none"
        });
        return;
      }
      this.loading = true;
      try {
        const registerData = {
          username: this.registerForm.nickname,
          password: this.registerForm.password,
          phone: this.registerForm.phone,
          role: this.registerForm.userType
        };
        const response = await api_user.userApi.register(registerData);
        if (response.code === 200) {
          common_vendor.index.showToast({
            title: "注册成功",
            icon: "success"
          });
          setTimeout(() => {
            common_vendor.index.navigateBack() || common_vendor.index.navigateTo({ url: "/pages/user/login" });
          }, 1500);
        } else {
          common_vendor.index.showToast({
            title: response.message || "注册失败",
            icon: "none"
          });
        }
      } catch (error) {
        console.error("注册失败:", error);
        common_vendor.index.showToast({
          title: "注册失败，请稍后重试",
          icon: "none"
        });
      } finally {
        this.loading = false;
      }
    },
    // 跳转登录页
    goToLogin() {
      common_vendor.index.navigateBack() || common_vendor.index.navigateTo({ url: "/pages/user/login" });
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return {
    a: $data.registerForm.phone,
    b: common_vendor.o(($event) => $data.registerForm.phone = $event.detail.value),
    c: $data.registerForm.verifyCode,
    d: common_vendor.o(($event) => $data.registerForm.verifyCode = $event.detail.value),
    e: common_vendor.t($data.codeCountdown > 0 ? `${$data.codeCountdown}s` : "获取验证码"),
    f: !$options.canSendCode || $data.codeCountdown > 0,
    g: common_vendor.o((...args) => $options.sendVerifyCode && $options.sendVerifyCode(...args)),
    h: $data.registerForm.nickname,
    i: common_vendor.o(($event) => $data.registerForm.nickname = $event.detail.value),
    j: $data.registerForm.password,
    k: common_vendor.o(($event) => $data.registerForm.password = $event.detail.value),
    l: $data.registerForm.confirmPassword,
    m: common_vendor.o(($event) => $data.registerForm.confirmPassword = $event.detail.value),
    n: common_vendor.t($data.registerForm.userType === 0 ? "●" : "○"),
    o: common_vendor.n({
      active: $data.registerForm.userType === 0
    }),
    p: common_vendor.o(($event) => $data.registerForm.userType = 0),
    q: common_vendor.t($data.registerForm.userType === 1 ? "●" : "○"),
    r: common_vendor.n({
      active: $data.registerForm.userType === 1
    }),
    s: common_vendor.o(($event) => $data.registerForm.userType = 1),
    t: common_vendor.t($data.agreeTerms ? "☑️" : "☐"),
    v: common_vendor.o(($event) => $data.agreeTerms = !$data.agreeTerms),
    w: common_vendor.t($data.loading ? "注册中..." : "立即注册"),
    x: $data.loading ? 1 : "",
    y: common_vendor.o((...args) => $options.handleRegister && $options.handleRegister(...args)),
    z: common_vendor.o((...args) => $options.goToLogin && $options.goToLogin(...args))
  };
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-239527a3"], ["__file", "C:/Users/34588/Desktop/ai租房test/ai-intelligent-rental-housing/code/ai租房系统/uniapp/src/pages/user/register.vue"]]);
wx.createPage(MiniProgramPage);
