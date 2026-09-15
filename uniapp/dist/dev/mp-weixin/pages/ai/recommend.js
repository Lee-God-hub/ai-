"use strict";
const common_vendor = require("../../common/vendor.js");
const api_ai = require("../../api/ai.js");
require("../../api/request.js");
const _sfc_main = {
  data() {
    return {
      recommendations: [],
      loading: false
    };
  },
  onLoad() {
    this.loadRecommendations();
  },
  onPullDownRefresh() {
    this.loadRecommendations().then(() => {
      common_vendor.index.stopPullDownRefresh();
    });
  },
  methods: {
    // 加载推荐房源
    async loadRecommendations() {
      this.loading = true;
      try {
        const response = await api_ai.aiApi.getRecommendations(10);
        if (response.code === 200 && response.data) {
          this.recommendations = response.data;
        } else {
          this.recommendations = [];
        }
        if (this.recommendations.length === 0) {
          common_vendor.index.showToast({
            title: "暂无推荐房源",
            icon: "none"
          });
        }
      } catch (error) {
        console.error("加载推荐失败:", error);
        common_vendor.index.showToast({
          title: "AI服务异常，请稍后重试",
          icon: "none"
        });
      } finally {
        this.loading = false;
      }
    },
    // 跳转到房源详情
    async goToDetail(id) {
      try {
        await api_ai.aiApi.recordRecommendClick({
          propertyId: id,
          source: "recommendation"
        });
      } catch (error) {
        console.error("记录点击失败:", error);
      }
      common_vendor.index.navigateTo({
        url: `/pages/property/detail?id=${id}`
      });
    },
    // 去浏览房源
    goToBrowse() {
      common_vendor.index.switchTab({
        url: "/pages/search/search"
      });
    },
    // 获取第一张图片
    getFirstImage(images) {
      if (!images)
        return "/static/placeholder.png";
      const imageList = images.split(",");
      return imageList[0] || "/static/placeholder.png";
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
    a: common_vendor.t($data.loading ? "推荐中..." : "换一批推荐"),
    b: $data.loading ? 1 : "",
    c: common_vendor.o((...args) => $options.loadRecommendations && $options.loadRecommendations(...args)),
    d: $data.loading && $data.recommendations.length === 0
  }, $data.loading && $data.recommendations.length === 0 ? {} : $data.recommendations.length === 0 ? {
    f: common_vendor.o((...args) => $options.goToBrowse && $options.goToBrowse(...args))
  } : {
    g: common_vendor.f($data.recommendations, (item, k0, i0) => {
      return common_vendor.e({
        a: $options.getFirstImage(item.property.images),
        b: common_vendor.t(item.recommendScore),
        c: common_vendor.t(item.property.title),
        d: common_vendor.t($options.formatPrice(item.property.price)),
        e: item.property.priceType === 0
      }, item.property.priceType === 0 ? {} : {}, {
        f: common_vendor.t(item.property.area),
        g: item.property.bedrooms
      }, item.property.bedrooms ? {
        h: common_vendor.t(item.property.bedrooms)
      } : {}, {
        i: item.property.bathrooms
      }, item.property.bathrooms ? {
        j: common_vendor.t(item.property.bathrooms)
      } : {}, {
        k: common_vendor.t(item.property.city),
        l: common_vendor.t(item.property.district),
        m: common_vendor.t(item.recommendReason),
        n: item.property.id,
        o: common_vendor.o(($event) => $options.goToDetail(item.property.id), item.property.id)
      });
    })
  }, {
    e: $data.recommendations.length === 0,
    h: $data.recommendations.length > 0
  }, $data.recommendations.length > 0 ? {
    i: common_vendor.t($data.recommendations.length)
  } : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-ade9e21a"], ["__file", "C:/Users/34588/Desktop/ai租房test/ai-intelligent-rental-housing/code/ai租房系统/uniapp/src/pages/ai/recommend.vue"]]);
wx.createPage(MiniProgramPage);
