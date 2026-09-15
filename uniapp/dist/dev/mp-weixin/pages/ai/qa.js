"use strict";
const common_vendor = require("../../common/vendor.js");
const api_ai = require("../../api/ai.js");
require("../../api/request.js");
const _sfc_main = {
  data() {
    return {
      messages: [],
      inputQuestion: "",
      loading: false,
      scrollTop: 0,
      quickQuestions: [
        "买房需要注意什么？",
        "租房如何避免被骗？",
        "三室两厅适合几口人住？",
        "最新的房产政策有哪些？"
      ]
    };
  },
  onLoad() {
  },
  methods: {
    // 发送消息
    async handleSend() {
      const question = this.inputQuestion.trim();
      if (!question) {
        common_vendor.index.showToast({
          title: "请输入问题",
          icon: "none"
        });
        return;
      }
      if (question.length < 2) {
        common_vendor.index.showToast({
          title: "问题内容太短",
          icon: "none"
        });
        return;
      }
      this.messages.push({
        type: "user",
        content: question,
        time: this.formatTime(/* @__PURE__ */ new Date())
      });
      this.inputQuestion = "";
      this.$nextTick(() => {
        this.scrollToBottom();
      });
      this.loading = true;
      try {
        const response = await api_ai.aiApi.askQuestion({ question });
        if (response.code === 200 && response.data) {
          const aiResponse = response.data;
          this.messages.push({
            type: "ai",
            content: aiResponse.answer,
            time: this.formatTime(new Date(aiResponse.answerTime)),
            relatedQuestions: aiResponse.relatedQuestions || []
          });
          this.$nextTick(() => {
            this.scrollToBottom();
          });
        } else {
          common_vendor.index.showToast({
            title: response.message || "AI回答失败",
            icon: "none"
          });
        }
      } catch (error) {
        console.error("AI问答失败:", error);
        common_vendor.index.showToast({
          title: "AI服务异常，请稍后重试",
          icon: "none"
        });
        this.messages.push({
          type: "ai",
          content: "抱歉，我暂时无法回答您的问题，请稍后再试。",
          time: this.formatTime(/* @__PURE__ */ new Date())
        });
        this.$nextTick(() => {
          this.scrollToBottom();
        });
      } finally {
        this.loading = false;
      }
    },
    // 发送快速问题
    sendQuickQuestion(question) {
      this.inputQuestion = question;
      this.handleSend();
    },
    // 滚动到底部
    scrollToBottom() {
      const query = common_vendor.index.createSelectorQuery().in(this);
      query.select(".messages-list").boundingClientRect((data) => {
        if (data) {
          this.scrollTop = data.height;
        }
      }).exec();
    },
    // 格式化时间
    formatTime(date) {
      const hours = date.getHours().toString().padStart(2, "0");
      const minutes = date.getMinutes().toString().padStart(2, "0");
      return `${hours}:${minutes}`;
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: $data.messages.length === 0
  }, $data.messages.length === 0 ? {
    b: common_vendor.f($data.quickQuestions, (question, index, i0) => {
      return {
        a: common_vendor.t(question),
        b: index,
        c: common_vendor.o(($event) => $options.sendQuickQuestion(question), index)
      };
    })
  } : {}, {
    c: common_vendor.f($data.messages, (message, index, i0) => {
      return common_vendor.e({
        a: message.type === "user"
      }, message.type === "user" ? {
        b: common_vendor.t(message.content),
        c: common_vendor.t(message.time)
      } : common_vendor.e({
        d: common_vendor.t(message.content),
        e: common_vendor.t(message.time),
        f: message.relatedQuestions && message.relatedQuestions.length > 0
      }, message.relatedQuestions && message.relatedQuestions.length > 0 ? {
        g: common_vendor.f(message.relatedQuestions, (q, idx, i1) => {
          return {
            a: common_vendor.t(q),
            b: idx,
            c: common_vendor.o(($event) => $options.sendQuickQuestion(q), idx)
          };
        })
      } : {}), {
        h: index,
        i: common_vendor.n(message.type)
      });
    }),
    d: $data.loading
  }, $data.loading ? {} : {}, {
    e: $data.scrollTop,
    f: $data.loading,
    g: $data.inputQuestion,
    h: common_vendor.o(($event) => $data.inputQuestion = $event.detail.value),
    i: common_vendor.t($data.loading ? "发送中" : "发送"),
    j: !$data.inputQuestion.trim() || $data.loading ? 1 : "",
    k: common_vendor.o((...args) => $options.handleSend && $options.handleSend(...args))
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-6c808026"], ["__file", "C:/Users/34588/Desktop/ai租房test/ai-intelligent-rental-housing/code/ai租房系统/uniapp/src/pages/ai/qa.vue"]]);
wx.createPage(MiniProgramPage);
