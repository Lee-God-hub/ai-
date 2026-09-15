<template>
  <router-view v-slot="{ Component, route }">
    <component :is="Component" :key="route.fullPath" />
  </router-view>
</template>

<script setup lang="ts">
// 智能房产交易平台 - 根组件
// 路由视图由 MainLayout 和各视图组件管理
</script>

<style>
/* App 全局覆盖样式 */
#app {
  min-height: 100vh;
}

/* 页面切换过渡动画 */
.page-fade-enter-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.page-fade-leave-active {
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.page-fade-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* 页面内部元素入场动画 */
.page-animate > * {
  animation: pageFadeInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1) both;
}

.page-animate > *:nth-child(1) { animation-delay: 0.05s; }
.page-animate > *:nth-child(2) { animation-delay: 0.1s; }
.page-animate > *:nth-child(3) { animation-delay: 0.15s; }
.page-animate > *:nth-child(4) { animation-delay: 0.2s; }
.page-animate > *:nth-child(5) { animation-delay: 0.25s; }
.page-animate > *:nth-child(6) { animation-delay: 0.3s; }
.page-animate > *:nth-child(7) { animation-delay: 0.35s; }
.page-animate > *:nth-child(8) { animation-delay: 0.4s; }

@keyframes pageFadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
