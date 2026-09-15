<template>
  <div class="order-manage-view page-animate">
    <div class="page-header">
      <div class="page-title">
        <span class="title-bar"></span>
        <h2>订单管理</h2>
      </div>
      <el-button type="primary" class="refresh-btn" @click="handleRefresh">
        <el-icon><Refresh /></el-icon>
        刷新数据
      </el-button>
    </div>

    <div class="stats-section">
      <div class="stat-card">
        <div class="stat-icon icon-total">
          <el-icon><Document /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">0</div>
          <div class="stat-label">订单总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon icon-pending">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">0</div>
          <div class="stat-label">待处理</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon icon-completed">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">0</div>
          <div class="stat-label">已完成</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon icon-income">
          <el-icon><Money /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">¥0</div>
          <div class="stat-label">总收入</div>
        </div>
      </div>
    </div>

    <div class="content-card">
      <div class="filter-bar">
        <el-input v-model="searchKeyword" placeholder="搜索订单号/用户/房源" clearable class="search-input" @keyup.enter="handleSearch">
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="statusFilter" placeholder="订单状态" clearable class="filter-select">
          <el-option label="待支付" :value="0" />
          <el-option label="已支付" :value="1" />
          <el-option label="已完成" :value="2" />
          <el-option label="已取消" :value="3" />
        </el-select>
        <el-button type="primary" class="btn-search" @click="handleSearch">
          <el-icon><Search /></el-icon>
          查询
        </el-button>
      </div>

      <div class="empty-placeholder">
        <el-empty description="订单管理功能开发中..." class="empty-state">
          <template #image>
            <div class="empty-icon-wrapper">
              <el-icon :size="80" class="empty-icon"><Document /></el-icon>
            </div>
          </template>
          <template #description>
            <p class="empty-title">订单管理功能即将上线</p>
            <p class="empty-desc">敬请期待，我们正在努力开发中...</p>
          </template>
          <el-button type="primary" class="empty-btn">
            <el-icon><Refresh /></el-icon>
            稍后再来
          </el-button>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Search, Document, Clock, CircleCheck, Money } from '@element-plus/icons-vue'

const searchKeyword = ref('')
const statusFilter = ref<number | null>(null)

const handleRefresh = () => {
  ElMessage.info('数据刷新中...')
}

const handleSearch = () => {
  ElMessage.info('搜索功能开发中...')
}
</script>

<style scoped>
.order-manage-view {
  padding: var(--spacing-2xl);
  min-height: 100%;
  background: var(--bg-color-page);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-xl);
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title h2 {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-color-primary);
  margin: 0;
  letter-spacing: 0.5px;
}

.title-bar {
  width: 5px;
  height: 24px;
  background: linear-gradient(180deg, var(--primary-color) 0%, var(--accent-color) 100%);
  border-radius: 3px;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  border-radius: var(--radius-full);
  padding: 10px 20px;
  font-weight: 500;
  transition: var(--transition-base);
}

.refresh-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
}

.stats-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
}

.stat-card {
  background: var(--bg-color);
  border-radius: var(--radius-xl);
  padding: var(--spacing-xl) var(--spacing-lg);
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  box-shadow: var(--shadow-base);
  border: 1px solid var(--border-color-light);
  transition: var(--transition-base);
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--primary-color), var(--accent-color));
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-card-hover);
  border-color: var(--primary-color-bg);
}

.stat-card:hover::before {
  transform: scaleX(1);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
  flex-shrink: 0;
}

.icon-total {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-color-light) 100%);
}

.icon-pending {
  background: linear-gradient(135deg, var(--warning-color) 0%, var(--warning-color-light) 100%);
}

.icon-completed {
  background: linear-gradient(135deg, var(--success-color) 0%, var(--success-color-light) 100%);
}

.icon-income {
  background: linear-gradient(135deg, var(--accent-color) 0%, var(--accent-color-light) 100%);
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-color-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: var(--text-color-secondary);
  margin-top: 4px;
  font-weight: 500;
}

.content-card {
  background: var(--bg-color);
  border-radius: var(--radius-xl);
  padding: var(--spacing-xl);
  box-shadow: var(--shadow-base);
  border: 1px solid var(--border-color-light);
  transition: var(--transition-base);
}

.content-card:hover {
  box-shadow: var(--shadow-lg);
}

.filter-bar {
  display: flex;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
  flex-wrap: wrap;
  align-items: center;
}

.search-input {
  width: 280px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: var(--radius-full);
  box-shadow: 0 0 0 1px var(--border-color-light) inset;
}

.search-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--primary-color) inset;
}

.search-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--primary-color) inset;
}

.filter-select {
  width: 160px;
}

.filter-select :deep(.el-input__wrapper) {
  border-radius: var(--radius-full);
}

.btn-search {
  display: flex;
  align-items: center;
  gap: 6px;
  border-radius: var(--radius-full);
  padding: 10px 20px;
  font-weight: 500;
  transition: var(--transition-base);
}

.btn-search:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
}

.empty-placeholder {
  padding: var(--spacing-3xl) 0;
}

.empty-state {
  padding: var(--spacing-2xl);
}

.empty-icon-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: var(--spacing-lg);
}

.empty-icon {
  color: var(--text-color-tertiary);
  opacity: 0.6;
}

.empty-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-color-primary);
  margin: 0 0 8px 0;
}

.empty-desc {
  font-size: 14px;
  color: var(--text-color-secondary);
  margin: 0 0 var(--spacing-lg) 0;
}

.empty-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border-radius: var(--radius-full);
  padding: 10px 24px;
  font-weight: 500;
  transition: var(--transition-base);
}

.empty-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
}

@media (max-width: 1200px) {
  .stats-section {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .order-manage-view {
    padding: var(--spacing-lg);
  }

  .page-title h2 {
    font-size: 18px;
  }

  .stats-section {
    grid-template-columns: 1fr;
  }

  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .search-input,
  .filter-select,
  .btn-search {
    width: 100%;
  }

  .empty-title {
    font-size: 16px;
  }

  .stat-number {
    font-size: 24px;
  }
}
</style>
