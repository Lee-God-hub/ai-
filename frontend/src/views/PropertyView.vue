<template>
  <div class="property-view page-animate">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>房源浏览</span>
        </div>
      </template>
      <el-empty description="房源功能开发中..." />
    </el-card>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.property-view {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
