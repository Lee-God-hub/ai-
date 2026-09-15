"use strict";
const common_vendor = require("../../common/vendor.js");
const api_property = require("../../api/property.js");
const api_ai = require("../../api/ai.js");
require("../../api/request.js");
const _sfc_main = {
  data() {
    return {
      searchKeyword: "",
      properties: [],
      loading: false,
      hasMore: true,
      loadMoreStatus: "loadmore",
      currentPage: 1,
      pageSize: 10,
      // 筛选条件
      activeTab: 0,
      filterTabs: [
        { name: "全部" },
        { name: "住宅" },
        { name: "公寓" },
        { name: "别墅" },
        { name: "商铺" }
      ]
    };
  },
  onLoad(options) {
    if (options.keyword) {
      this.searchKeyword = options.keyword;
    }
    this.loadProperties();
  },
  onPullDownRefresh() {
    this.refreshData();
  },
  onReachBottom() {
    if (this.hasMore && !this.loading) {
      this.loadMore();
    }
  },
  methods: {
    // 搜索房源
    handleSearch() {
      this.currentPage = 1;
      this.properties = [];
      this.hasMore = true;
      this.loadProperties();
    },
    // 加载房源列表
    async loadProperties() {
      if (this.loading)
        return;
      this.loading = true;
      this.loadMoreStatus = "loading";
      try {
        const params = {
          pageNum: this.currentPage,
          pageSize: this.pageSize
        };
        if (this.searchKeyword) {
          params.city = this.searchKeyword;
        }
        if (this.activeTab > 0) {
          params.propertyType = this.activeTab;
        }
        const response = await api_property.propertyApi.searchProperties(params);
        if (response.code === 200 && response.data) {
          const list = response.data || [];
          if (this.currentPage === 1) {
            this.properties = list;
          } else {
            this.properties.push(...list);
          }
          this.hasMore = list.length === this.pageSize;
          this.loadMoreStatus = this.hasMore ? "loadmore" : "nomore";
        } else {
          this.loadMoreStatus = "loadmore";
        }
      } catch (error) {
        console.error("搜索房源失败:", error);
        this.loadMoreStatus = "loadmore";
      } finally {
        this.loading = false;
        if (this.currentPage === 1) {
          common_vendor.index.stopPullDownRefresh();
        }
      }
    },
    // 刷新数据
    refreshData() {
      this.currentPage = 1;
      this.properties = [];
      this.hasMore = true;
      this.loadProperties();
    },
    // 加载更多
    loadMore() {
      if (this.hasMore && !this.loading) {
        this.currentPage++;
        this.loadProperties();
      }
    },
    // 切换筛选
    handleTabChange(index) {
      this.activeTab = index;
      this.refreshData();
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
    // 切换收藏状态
    async toggleFavorite(property) {
      try {
        if (property.isFavorite) {
          await api_property.propertyApi.unfavoriteProperty(property.id);
          await api_ai.aiApi.recordUserBehavior(property.id, "unfavorite");
          property.isFavorite = false;
          common_vendor.index.showToast({
            title: "已取消收藏",
            icon: "success"
          });
        } else {
          await api_property.propertyApi.favoriteProperty(property.id);
          await api_ai.aiApi.recordUserBehavior(property.id, "favorite");
          property.isFavorite = true;
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
    a: common_vendor.o((...args) => $options.handleSearch && $options.handleSearch(...args)),
    b: $data.searchKeyword,
    c: common_vendor.o(($event) => $data.searchKeyword = $event.detail.value),
    d: common_vendor.o((...args) => $options.handleSearch && $options.handleSearch(...args)),
    e: common_vendor.f($data.filterTabs, (filter, index, i0) => {
      return {
        a: common_vendor.t(filter.name),
        b: index,
        c: common_vendor.n({
          active: $data.activeTab === index
        }),
        d: common_vendor.o(($event) => $options.handleTabChange(index), index)
      };
    }),
    f: $data.loading && $data.properties.length === 0
  }, $data.loading && $data.properties.length === 0 ? {} : $data.properties.length === 0 && !$data.loading ? {} : {
    h: common_vendor.f($data.properties, (property, k0, i0) => {
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
        k: common_vendor.t(property.isFavorite ? "❤️" : "🤍"),
        l: common_vendor.n({
          favorited: property.isFavorite
        }),
        m: common_vendor.o(($event) => $options.toggleFavorite(property), property.id),
        n: property.id,
        o: common_vendor.o(($event) => $options.goToDetail(property.id), property.id)
      });
    })
  }, {
    g: $data.properties.length === 0 && !$data.loading,
    i: $data.hasMore && $data.properties.length > 0
  }, $data.hasMore && $data.properties.length > 0 ? {
    j: common_vendor.t($data.loadMoreStatus === "loading" ? "加载中..." : "加载更多"),
    k: common_vendor.o((...args) => $options.loadMore && $options.loadMore(...args))
  } : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-cdfa925e"], ["__file", "C:/Users/34588/Desktop/ai租房test/ai-intelligent-rental-housing/code/ai租房系统/uniapp/src/pages/search/search.vue"]]);
wx.createPage(MiniProgramPage);
