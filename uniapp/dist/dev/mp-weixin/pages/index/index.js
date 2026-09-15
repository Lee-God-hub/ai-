"use strict";
const common_vendor = require("../../common/vendor.js");
const api_ai = require("../../api/ai.js");
const api_property = require("../../api/property.js");
require("../../api/request.js");
const _sfc_main = {
  data() {
    return {
      // 轮播图数据
      bannerList: [
        {
          image: "https://cdn.uviewui.com/uview/swiper/swiper1.png",
          title: "智能找房，AI推荐"
        },
        {
          image: "https://cdn.uviewui.com/uview/swiper/swiper2.png",
          title: "优质房源，一键发布"
        },
        {
          image: "https://cdn.uviewui.com/uview/swiper/swiper3.png",
          title: "专业问答，贴心服务"
        }
      ],
      // 推荐房源
      recommendList: [],
      recommendLoading: false,
      // 热门房源
      hotProperties: [],
      hotLoading: false
    };
  },
  onLoad() {
    this.loadRecommendations();
    this.loadHotProperties();
  },
  onPullDownRefresh() {
    this.refreshData();
  },
  methods: {
    // 刷新数据
    async refreshData() {
      await Promise.all([
        this.loadRecommendations(),
        this.loadHotProperties()
      ]);
      common_vendor.index.stopPullDownRefresh();
    },
    // 加载AI推荐
    async loadRecommendations() {
      this.recommendLoading = true;
      try {
        const response = await api_ai.aiApi.getRecommendations(6);
        if (response.code === 200 && response.data) {
          this.recommendList = response.data;
        }
      } catch (error) {
        console.error("加载推荐失败:", error);
      } finally {
        this.recommendLoading = false;
      }
    },
    // 加载热门房源
    async loadHotProperties() {
      this.hotLoading = true;
      try {
        const response = await api_property.propertyApi.getHotProperties(8);
        if (response.code === 200 && response.data) {
          this.hotProperties = response.data;
        }
      } catch (error) {
        console.error("加载热门房源失败:", error);
      } finally {
        this.hotLoading = false;
      }
    },
    // 轮播图点击
    onBannerClick(index) {
      console.log("点击轮播图:", index);
    },
    // 跳转搜索页面
    goToSearch() {
      common_vendor.index.navigateTo({
        url: "/pages/search/search"
      });
    },
    // 跳转页面
    goToPage(url) {
      common_vendor.index.navigateTo({
        url
      });
    },
    // 跳转房源详情
    goToDetail(id) {
      common_vendor.index.navigateTo({
        url: `/pages/property/detail?id=${id}`
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
    a: common_vendor.o((...args) => $options.goToSearch && $options.goToSearch(...args)),
    b: common_vendor.f($data.bannerList, (banner, index, i0) => {
      return {
        a: banner.image,
        b: index
      };
    }),
    c: common_vendor.o(($event) => $options.goToPage("/pages/search/search")),
    d: common_vendor.o(($event) => $options.goToPage("/pages/ai/recommend")),
    e: common_vendor.o(($event) => $options.goToPage("/pages/ai/qa")),
    f: common_vendor.o(($event) => $options.goToPage("/pages/landlord/publish")),
    g: common_vendor.o(($event) => $options.goToPage("/pages/ai/recommend")),
    h: $data.recommendLoading
  }, $data.recommendLoading ? {} : {
    i: common_vendor.f($data.recommendList, (item, k0, i0) => {
      return {
        a: $options.getFirstImage(item.property.images),
        b: common_vendor.t(item.property.title),
        c: common_vendor.t($options.formatPrice(item.property.price)),
        d: common_vendor.t(item.recommendScore),
        e: item.property.id,
        f: common_vendor.o(($event) => $options.goToDetail(item.property.id), item.property.id)
      };
    })
  }, {
    j: common_vendor.o(($event) => $options.goToPage("/pages/search/search")),
    k: $data.hotLoading
  }, $data.hotLoading ? {} : {
    l: common_vendor.f($data.hotProperties, (property, k0, i0) => {
      return common_vendor.e({
        a: $options.getFirstImage(property.images),
        b: common_vendor.t(property.title),
        c: common_vendor.t($options.formatPrice(property.price)),
        d: common_vendor.t(property.area),
        e: property.bedrooms
      }, property.bedrooms ? {
        f: common_vendor.t(property.bedrooms)
      } : {}, {
        g: property.bathrooms
      }, property.bathrooms ? {
        h: common_vendor.t(property.bathrooms)
      } : {}, {
        i: common_vendor.t(property.city),
        j: common_vendor.t(property.district),
        k: property.id,
        l: common_vendor.o(($event) => $options.goToDetail(property.id), property.id)
      });
    })
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-83a5a03c"], ["__file", "C:/Users/34588/Desktop/ai租房test/ai-intelligent-rental-housing/code/ai租房系统/uniapp/src/pages/index/index.vue"]]);
wx.createPage(MiniProgramPage);
