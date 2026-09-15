"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  name: "TestPage",
  methods: {
    showToast() {
      common_vendor.index.showToast({
        title: "功能正常！",
        icon: "success",
        duration: 2e3
      });
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return {
    a: common_vendor.o((...args) => $options.showToast && $options.showToast(...args))
  };
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__file", "C:/Users/34588/Desktop/ai租房test/ai-intelligent-rental-housing/code/ai租房系统/uniapp/src/pages/test/test.vue"]]);
wx.createPage(MiniProgramPage);
