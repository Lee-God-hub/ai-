"use strict";
const common_vendor = require("../../common/vendor.js");
const api_property = require("../../api/property.js");
const api_ai = require("../../api/ai.js");
require("../../api/request.js");
const _sfc_main = {
  data() {
    return {
      favorites: [],
      loading: false,
      totalCount: 0
    };
  },
  onLoad() {
    this.loadFavorites();
  },
  onPullDownRefresh() {
    this.refreshData();
  },
  methods: {
    // 加载收藏列表
    async loadFavorites() {
      this.loading = true;
      try {
        const response = await api_property.propertyApi.getFavoriteList();
        if (response.code === 200 && response.data) {
          this.favorites = response.data;
          this.totalCount = this.favorites.length;
        } else {
          this.favorites = [];
          this.totalCount = 0;
        }
      } catch (error) {
        console.error("加载收藏列表失败:", error);
      } finally {
        this.loading = false;
        common_vendor.index.stopPullDownRefresh();
      }
    },
    // 刷新数据
    refreshData() {
      this.favorites = [];
      this.loadFavorites();
    },
    // 跳转房源详情
    async goToDetail(id) {
      try {
        await api_ai.aiApi.recordUserBehavior(id, "view");
      } catch (error) {
        console.error("记录浏览行为失败:", error);
      }
      common_vendor.index.navigateTo({
        url: `/pages/property/detail?id=${id}`
      });
    },
    // 取消收藏
    async removeFavorite(item) {
      common_vendor.index.showModal({
        title: "提示",
        content: "确定要取消收藏这套房源吗？",
        success: async (res) => {
          if (res.confirm) {
            try {
              await api_property.propertyApi.unfavoriteProperty(item.property.id);
              await api_ai.aiApi.recordUserBehavior(item.property.id, "unfavorite");
              const index = this.favorites.findIndex((f) => f.id === item.id);
              if (index > -1) {
                this.favorites.splice(index, 1);
                this.totalCount--;
              }
              common_vendor.index.showToast({
                title: "已取消收藏",
                icon: "success"
              });
            } catch (error) {
              console.error("取消收藏失败:", error);
              common_vendor.index.showToast({
                title: "操作失败",
                icon: "none"
              });
            }
          }
        }
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
    },
    // 格式化时间
    formatTime(time) {
      const date = new Date(time);
      const now = /* @__PURE__ */ new Date();
      const diff = now - date;
      if (diff < 6e4) {
        return "刚刚";
      } else if (diff < 36e5) {
        return Math.floor(diff / 6e4) + "分钟前";
      } else if (diff < 864e5) {
        return Math.floor(diff / 36e5) + "小时前";
      } else if (diff < 2592e6) {
        return Math.floor(diff / 864e5) + "天前";
      } else {
        return date.toLocaleDateString();
      }
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.t($data.totalCount),
    b: $data.loading && $data.favorites.length === 0
  }, $data.loading && $data.favorites.length === 0 ? {} : $data.favorites.length === 0 ? {
    d: common_vendor.o((...args) => $options.goToBrowse && $options.goToBrowse(...args))
  } : {
    e: common_vendor.f($data.favorites, (item, k0, i0) => {
      return common_vendor.e({
        a: $options.getFirstImage(item.property.images),
        b: common_vendor.t($options.formatTime(item.createTime)),
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
        m: common_vendor.o(($event) => $options.removeFavorite(item), item.id),
        n: item.id,
        o: common_vendor.o(($event) => $options.goToDetail(item.property.id), item.id)
      });
    })
  }, {
    c: $data.favorites.length === 0
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-b2f01737"], ["__file", "C:/Users/34588/Desktop/ai租房test/ai-intelligent-rental-housing/code/ai租房系统/uniapp/src/pages/favorites/index.vue"]]);
wx.createPage(MiniProgramPage);
