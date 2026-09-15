"use strict";
const common_vendor = require("../../common/vendor.js");
const api_property = require("../../api/property.js");
const api_ai = require("../../api/ai.js");
require("../../api/request.js");
const _sfc_main = {
  data() {
    return {
      propertyId: null,
      property: null,
      loading: false
    };
  },
  computed: {
    // 图片列表
    imageList() {
      if (!this.property || !this.property.images)
        return [{ url: "/static/placeholder.png" }];
      return this.property.images.split(",").map((url) => ({ url }));
    },
    // 配套设施列表
    facilitiesList() {
      if (!this.property || !this.property.facilities)
        return [];
      return this.property.facilities.split(",").filter((f) => f.trim());
    }
  },
  onLoad(options) {
    if (options.id) {
      this.propertyId = parseInt(options.id);
      this.loadPropertyDetail();
    }
  },
  methods: {
    // 加载房源详情
    async loadPropertyDetail() {
      this.loading = true;
      try {
        const response = await api_property.propertyApi.getPropertyDetail(this.propertyId);
        if (response.code === 200 && response.data) {
          this.property = response.data;
          if (!this.property.isFavorite) {
            this.property.isFavorite = false;
          }
          try {
            const favRes = await api_property.propertyApi.checkFavorite(this.propertyId);
            if (favRes.code === 200) {
              this.property.isFavorite = favRes.data === true;
            }
          } catch (e) {
          }
          this.recordViewBehavior();
        } else {
          common_vendor.index.showToast({
            title: response.message || "房源不存在",
            icon: "none"
          });
        }
      } catch (error) {
        console.error("加载房源详情失败:", error);
        common_vendor.index.showToast({
          title: "加载失败",
          icon: "none"
        });
      } finally {
        this.loading = false;
      }
    },
    // 记录浏览行为
    async recordViewBehavior() {
      try {
        await api_ai.aiApi.recordUserBehavior(this.propertyId, "view");
      } catch (error) {
        console.error("记录浏览行为失败:", error);
      }
    },
    // 切换收藏状态
    async toggleFavorite() {
      try {
        if (this.property.isFavorite) {
          await api_property.propertyApi.unfavoriteProperty(this.propertyId);
          await api_ai.aiApi.recordUserBehavior(this.propertyId, "unfavorite");
          this.property.isFavorite = false;
          common_vendor.index.showToast({
            title: "已取消收藏",
            icon: "success"
          });
        } else {
          await api_property.propertyApi.favoriteProperty(this.propertyId);
          await api_ai.aiApi.recordUserBehavior(this.propertyId, "favorite");
          this.property.isFavorite = true;
          common_vendor.index.showToast({
            title: "收藏成功",
            icon: "success"
          });
        }
      } catch (error) {
        console.error("收藏操作失败:", error);
        common_vendor.index.showToast({
          title: "操作失败",
          icon: "none"
        });
      }
    },
    // 预约看房
    makeAppointment() {
      common_vendor.index.showModal({
        title: "预约看房",
        content: "请联系房东预约看房时间",
        showCancel: false,
        success: () => {
          this.contactLandlord();
        }
      });
    },
    // 联系房东
    contactLandlord() {
      if (!this.property || !this.property.landlord || !this.property.landlord.phone) {
        common_vendor.index.showToast({
          title: "暂无联系方式",
          icon: "none"
        });
        return;
      }
      common_vendor.index.makePhoneCall({
        phoneNumber: this.property.landlord.phone
      });
    },
    // 获取房东名字首字母
    getLandlordInitial() {
      if (this.property && this.property.landlord && this.property.landlord.nickname) {
        return this.property.landlord.nickname.charAt(0);
      }
      return "房";
    },
    // 获取房东名字
    getLandlordName() {
      if (this.property && this.property.landlord && this.property.landlord.nickname) {
        return this.property.landlord.nickname;
      }
      return "房东";
    },
    // 获取房东描述
    getLandlordDesc() {
      if (this.property && this.property.landlord && this.property.landlord.description) {
        return this.property.landlord.description;
      }
      return "暂无介绍";
    },
    // 获取房源类型文本
    getPropertyTypeText(type) {
      const types = {
        1: "住宅",
        2: "公寓",
        3: "别墅",
        4: "商铺",
        5: "写字楼"
      };
      return types[type] || "未知";
    },
    // 获取交易类型文本
    getTransactionTypeText(type) {
      return type === 1 ? "出售" : "出租";
    },
    // 格式化价格
    formatPrice(price) {
      if (!price)
        return "0";
      if (price >= 1e4) {
        return (price / 1e4).toFixed(1) + "万";
      }
      return price.toLocaleString();
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: $data.loading
  }, $data.loading ? {} : $data.property ? common_vendor.e({
    c: common_vendor.f($options.imageList, (image, index, i0) => {
      return {
        a: image.url,
        b: index
      };
    }),
    d: common_vendor.t($data.property.isFavorite ? "❤️" : "🤍"),
    e: common_vendor.o((...args) => $options.toggleFavorite && $options.toggleFavorite(...args)),
    f: common_vendor.t($data.property.title),
    g: common_vendor.t($options.formatPrice($data.property.price)),
    h: $data.property.priceType === 0
  }, $data.property.priceType === 0 ? {} : {}, {
    i: common_vendor.t($options.getPropertyTypeText($data.property.propertyType)),
    j: common_vendor.t($options.getTransactionTypeText($data.property.transactionType)),
    k: common_vendor.t($data.property.area),
    l: $data.property.bedrooms
  }, $data.property.bedrooms ? {
    m: common_vendor.t($data.property.bedrooms)
  } : {}, {
    n: $data.property.bathrooms
  }, $data.property.bathrooms ? {
    o: common_vendor.t($data.property.bathrooms)
  } : {}, {
    p: common_vendor.t($data.property.address),
    q: common_vendor.t($data.property.description || "暂无描述"),
    r: common_vendor.t($data.property.decoration || "未知"),
    s: common_vendor.t($data.property.orientation || "未知"),
    t: common_vendor.t($data.property.floor || "未知"),
    v: common_vendor.t($data.property.buildYear || "未知"),
    w: $data.property.facilities
  }, $data.property.facilities ? {
    x: common_vendor.f($options.facilitiesList, (facility, k0, i0) => {
      return {
        a: common_vendor.t(facility),
        b: facility
      };
    })
  } : {}, {
    y: common_vendor.t($options.getLandlordInitial()),
    z: common_vendor.t($options.getLandlordName()),
    A: common_vendor.t($options.getLandlordDesc()),
    B: common_vendor.o((...args) => $options.contactLandlord && $options.contactLandlord(...args))
  }) : {}, {
    b: $data.property,
    C: common_vendor.o((...args) => $options.makeAppointment && $options.makeAppointment(...args)),
    D: !$data.property,
    E: common_vendor.o((...args) => $options.contactLandlord && $options.contactLandlord(...args)),
    F: !$data.property
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-142ae9cd"], ["__file", "C:/Users/34588/Desktop/ai租房test/ai-intelligent-rental-housing/code/ai租房系统/uniapp/src/pages/property/detail.vue"]]);
wx.createPage(MiniProgramPage);
