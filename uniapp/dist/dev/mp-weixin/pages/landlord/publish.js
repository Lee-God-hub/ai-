"use strict";
const common_vendor = require("../../common/vendor.js");
const api_property = require("../../api/property.js");
require("../../api/request.js");
const _sfc_main = {
  data() {
    return {
      publishing: false,
      showAIGenerator: false,
      // 房源表单
      propertyForm: {
        title: "",
        propertyType: 1,
        transactionType: 2,
        price: "",
        area: "",
        bedrooms: 0,
        bathrooms: 0,
        decoration: "",
        orientation: "",
        city: "",
        district: "",
        address: "",
        description: ""
      },
      // 房源类型选项
      propertyTypes: [
        { label: "住宅", value: 1 },
        { label: "公寓", value: 2 },
        { label: "别墅", value: 3 },
        { label: "商铺", value: 4 }
      ]
    };
  },
  methods: {
    // 改变数字
    changeNumber(field, delta) {
      const newValue = this.propertyForm[field] + delta;
      if (newValue >= 0 && newValue <= 10) {
        this.propertyForm[field] = newValue;
      }
    },
    // 发布房源
    async handlePublish() {
      if (!this.propertyForm.title) {
        common_vendor.index.showToast({
          title: "请输入房源标题",
          icon: "none"
        });
        return;
      }
      if (!this.propertyForm.price) {
        common_vendor.index.showToast({
          title: "请输入价格",
          icon: "none"
        });
        return;
      }
      if (!this.propertyForm.area) {
        common_vendor.index.showToast({
          title: "请输入建筑面积",
          icon: "none"
        });
        return;
      }
      if (!this.propertyForm.city) {
        common_vendor.index.showToast({
          title: "请输入城市",
          icon: "none"
        });
        return;
      }
      if (!this.propertyForm.address) {
        common_vendor.index.showToast({
          title: "请输入详细地址",
          icon: "none"
        });
        return;
      }
      this.publishing = true;
      try {
        const publishData = {
          ...this.propertyForm,
          price: parseFloat(this.propertyForm.price),
          area: parseFloat(this.propertyForm.area)
        };
        const response = await api_property.propertyApi.publishProperty(publishData);
        if (response.code === 200) {
          common_vendor.index.showToast({
            title: "发布成功",
            icon: "success"
          });
          setTimeout(() => {
            common_vendor.index.navigateTo({
              url: "/pages/landlord/manage"
            });
          }, 1500);
        } else {
          common_vendor.index.showToast({
            title: response.message || "发布失败",
            icon: "none"
          });
        }
      } catch (error) {
        console.error("发布房源失败:", error);
        common_vendor.index.showToast({
          title: "发布失败，请稍后重试",
          icon: "none"
        });
      } finally {
        this.publishing = false;
      }
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return {
    a: $data.propertyForm.title,
    b: common_vendor.o(($event) => $data.propertyForm.title = $event.detail.value),
    c: common_vendor.f($data.propertyTypes, (type, index, i0) => {
      return {
        a: common_vendor.t($data.propertyForm.propertyType === type.value ? "●" : "○"),
        b: common_vendor.t(type.label),
        c: index,
        d: common_vendor.n({
          active: $data.propertyForm.propertyType === type.value
        }),
        e: common_vendor.o(($event) => $data.propertyForm.propertyType = type.value, index)
      };
    }),
    d: common_vendor.t($data.propertyForm.transactionType === 1 ? "●" : "○"),
    e: common_vendor.n({
      active: $data.propertyForm.transactionType === 1
    }),
    f: common_vendor.o(($event) => $data.propertyForm.transactionType = 1),
    g: common_vendor.t($data.propertyForm.transactionType === 2 ? "●" : "○"),
    h: common_vendor.n({
      active: $data.propertyForm.transactionType === 2
    }),
    i: common_vendor.o(($event) => $data.propertyForm.transactionType = 2),
    j: $data.propertyForm.price,
    k: common_vendor.o(($event) => $data.propertyForm.price = $event.detail.value),
    l: common_vendor.t($data.propertyForm.transactionType === 1 ? "万元" : "元/月"),
    m: $data.propertyForm.area,
    n: common_vendor.o(($event) => $data.propertyForm.area = $event.detail.value),
    o: common_vendor.o(($event) => $options.changeNumber("bedrooms", -1)),
    p: common_vendor.t($data.propertyForm.bedrooms),
    q: common_vendor.o(($event) => $options.changeNumber("bedrooms", 1)),
    r: common_vendor.o(($event) => $options.changeNumber("bathrooms", -1)),
    s: common_vendor.t($data.propertyForm.bathrooms),
    t: common_vendor.o(($event) => $options.changeNumber("bathrooms", 1)),
    v: $data.propertyForm.decoration,
    w: common_vendor.o(($event) => $data.propertyForm.decoration = $event.detail.value),
    x: $data.propertyForm.orientation,
    y: common_vendor.o(($event) => $data.propertyForm.orientation = $event.detail.value),
    z: $data.propertyForm.city,
    A: common_vendor.o(($event) => $data.propertyForm.city = $event.detail.value),
    B: $data.propertyForm.district,
    C: common_vendor.o(($event) => $data.propertyForm.district = $event.detail.value),
    D: $data.propertyForm.address,
    E: common_vendor.o(($event) => $data.propertyForm.address = $event.detail.value),
    F: common_vendor.o(($event) => $data.showAIGenerator = true),
    G: $data.propertyForm.description,
    H: common_vendor.o(($event) => $data.propertyForm.description = $event.detail.value),
    I: common_vendor.t($data.publishing ? "发布中..." : "立即发布"),
    J: $data.publishing ? 1 : "",
    K: common_vendor.o((...args) => $options.handlePublish && $options.handlePublish(...args))
  };
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-7a67bd8b"], ["__file", "C:/Users/34588/Desktop/ai租房test/ai-intelligent-rental-housing/code/ai租房系统/uniapp/src/pages/landlord/publish.vue"]]);
wx.createPage(MiniProgramPage);
