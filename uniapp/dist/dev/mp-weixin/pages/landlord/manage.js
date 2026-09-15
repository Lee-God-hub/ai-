"use strict";
const common_vendor = require("../../common/vendor.js");
const api_property = require("../../api/property.js");
require("../../api/request.js");
const _sfc_main = {
  data() {
    return {
      properties: [],
      loading: false,
      // 统计数据
      totalCount: 0,
      onlineCount: 0,
      offlineCount: 0,
      viewCount: 0
    };
  },
  onLoad() {
    this.loadProperties();
    this.loadStats();
  },
  onShow() {
    this.refreshData();
  },
  onPullDownRefresh() {
    this.refreshData();
  },
  methods: {
    // 加载房源列表
    async loadProperties() {
      this.loading = true;
      try {
        const response = await api_property.propertyApi.getMyList();
        if (response.code === 200 && response.data) {
          this.properties = response.data;
        } else {
          this.properties = [];
        }
      } catch (error) {
        console.error("加载房源列表失败:", error);
      } finally {
        this.loading = false;
        common_vendor.index.stopPullDownRefresh();
      }
    },
    // 加载统计数据（从房源列表计算）
    async loadStats() {
      try {
        this.totalCount = this.properties.length;
        this.onlineCount = this.properties.filter((p) => p.status === 1).length;
        this.offlineCount = this.properties.filter((p) => p.status === 0).length;
        this.viewCount = this.properties.reduce((sum, p) => sum + (p.viewCount || 0), 0);
      } catch (error) {
        console.error("加载统计数据失败:", error);
      }
    },
    // 刷新数据
    refreshData() {
      this.properties = [];
      this.loadProperties();
      this.loadStats();
    },
    // 跳转发布页面
    goToPublish() {
      common_vendor.index.navigateTo({
        url: "/pages/landlord/publish"
      });
    },
    // 跳转房源详情
    goToDetail(id) {
      common_vendor.index.navigateTo({
        url: `/pages/property/detail?id=${id}`
      });
    },
    // 编辑房源
    editProperty(property) {
      common_vendor.index.navigateTo({
        url: `/pages/landlord/publish?id=${property.id}&mode=edit`
      });
    },
    // 切换房源状态
    async toggleStatus(property) {
      const action = property.status === 1 ? "下架" : "上架";
      common_vendor.index.showModal({
        title: "提示",
        content: `确定要${action}这套房源吗？`,
        success: async (res) => {
          if (res.confirm) {
            try {
              let response;
              if (property.status === 1) {
                response = await api_property.propertyApi.offlineProperty(property.id);
              } else {
                response = await api_property.propertyApi.onlineProperty(property.id);
              }
              if (response.code === 200) {
                const newStatus = property.status === 1 ? 0 : 1;
                property.status = newStatus;
                if (newStatus === 1) {
                  this.onlineCount++;
                  this.offlineCount--;
                } else {
                  this.onlineCount--;
                  this.offlineCount++;
                }
                common_vendor.index.showToast({
                  title: `${action}成功`,
                  icon: "success"
                });
              } else {
                common_vendor.index.showToast({
                  title: response.message || `${action}失败`,
                  icon: "none"
                });
              }
            } catch (error) {
              console.error(`${action}房源失败:`, error);
              common_vendor.index.showToast({
                title: `${action}失败`,
                icon: "none"
              });
            }
          }
        }
      });
    },
    // 删除房源
    deleteProperty(property) {
      common_vendor.index.showModal({
        title: "提示",
        content: "确定要删除这套房源吗？删除后无法恢复。",
        success: async (res) => {
          if (res.confirm) {
            try {
              const response = await api_property.propertyApi.deleteProperty(property.id);
              if (response.code === 200) {
                const index = this.properties.findIndex((p) => p.id === property.id);
                if (index > -1) {
                  this.properties.splice(index, 1);
                }
                this.totalCount--;
                if (property.status === 1) {
                  this.onlineCount--;
                } else {
                  this.offlineCount--;
                }
                common_vendor.index.showToast({
                  title: "删除成功",
                  icon: "success"
                });
              } else {
                common_vendor.index.showToast({
                  title: response.message || "删除失败",
                  icon: "none"
                });
              }
            } catch (error) {
              console.error("删除房源失败:", error);
              common_vendor.index.showToast({
                title: "删除失败",
                icon: "none"
              });
            }
          }
        }
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
    // 格式化日期
    formatDate(date) {
      const d = new Date(date);
      return `${d.getMonth() + 1}/${d.getDate()}`;
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.o((...args) => $options.goToPublish && $options.goToPublish(...args)),
    b: common_vendor.t($data.totalCount),
    c: common_vendor.t($data.onlineCount),
    d: common_vendor.t($data.offlineCount),
    e: common_vendor.t($data.viewCount),
    f: $data.loading && $data.properties.length === 0
  }, $data.loading && $data.properties.length === 0 ? {} : $data.properties.length === 0 ? {
    h: common_vendor.o((...args) => $options.goToPublish && $options.goToPublish(...args))
  } : {
    i: common_vendor.f($data.properties, (property, k0, i0) => {
      return common_vendor.e({
        a: $options.getFirstImage(property.images),
        b: common_vendor.t(property.status === 1 ? "在线" : "下线"),
        c: common_vendor.n(property.status === 1 ? "online" : "offline"),
        d: common_vendor.o(($event) => $options.goToDetail(property.id), property.id),
        e: common_vendor.t(property.title),
        f: common_vendor.t($options.formatPrice(property.price)),
        g: property.priceType === 0
      }, property.priceType === 0 ? {} : {}, {
        h: common_vendor.t(property.area),
        i: property.bedrooms
      }, property.bedrooms ? {
        j: common_vendor.t(property.bedrooms)
      } : {}, {
        k: property.bathrooms
      }, property.bathrooms ? {
        l: common_vendor.t(property.bathrooms)
      } : {}, {
        m: common_vendor.t(property.viewCount || 0),
        n: common_vendor.t(property.favoriteCount || 0),
        o: common_vendor.t($options.formatDate(property.createTime)),
        p: common_vendor.o(($event) => $options.goToDetail(property.id), property.id),
        q: common_vendor.o(($event) => $options.editProperty(property), property.id),
        r: common_vendor.t(property.status === 1 ? "下线" : "上线"),
        s: common_vendor.n(property.status === 1 ? "offline-btn" : "online-btn"),
        t: common_vendor.o(($event) => $options.toggleStatus(property), property.id),
        v: common_vendor.o(($event) => $options.deleteProperty(property), property.id),
        w: property.id
      });
    })
  }, {
    g: $data.properties.length === 0
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-8d22bdbb"], ["__file", "C:/Users/34588/Desktop/ai租房test/ai-intelligent-rental-housing/code/ai租房系统/uniapp/src/pages/landlord/manage.vue"]]);
wx.createPage(MiniProgramPage);
