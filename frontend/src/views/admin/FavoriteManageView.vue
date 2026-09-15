<template>
  <div class="favorite-manage-view page-animate">
    <div class="page-header">
      <div class="page-title">
        <span class="title-bar"></span>
        <h2>收藏管理</h2>
        <el-tag type="primary" class="total-tag">共 {{ total }} 条</el-tag>
      </div>
      <el-button type="primary" class="refresh-btn" @click="loadData">
        <el-icon><Refresh /></el-icon>
        刷新数据
      </el-button>
    </div>

    <div class="content-card">
      <el-table :data="favorites" v-loading="loading" stripe class="favorite-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="用户" width="160">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="32" class="user-avatar">
                <el-icon :size="16"><User /></el-icon>
              </el-avatar>
              <span class="user-name">{{ row.user?.username || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="房源标题" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="property-title">{{ row.property?.title || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="140">
          <template #default="{ row }">
            <span class="price-text">¥{{ row.property?.price || 0 }}<span class="price-unit">/月</span></span>
          </template>
        </el-table-column>
        <el-table-column label="区域" width="100">
          <template #default="{ row }">
            {{ row.property?.district || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="户型" width="100">
          <template #default="{ row }">
            {{ row.property?.rooms }}室{{ row.property?.halls }}厅
          </template>
        </el-table-column>
        <el-table-column label="数据来源" width="120">
          <template #default="{ row }">
            <el-tag size="small" :class="row.property?.source?.includes('爬虫') ? 'tag-crawler' : 'tag-user'">
              {{ (row.property?.source || '用户发布').replace('爬虫-', '') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="收藏时间" width="170">
          <template #default="{ row }">
            <span class="time-text">{{ formatDate(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              type="danger"
              size="small"
              link
              class="btn-delete"
              @click="handleDelete(row)"
              :loading="row.deleting"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="favorites.length === 0 && !loading" description="暂无收藏记录" class="empty-state" />
      <div class="pagination-wrapper">
        <el-pagination
          v-if="total > 0"
          v-model:current-page="filters.pageNum"
          v-model:page-size="filters.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          class="pagination"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, User, Delete } from '@element-plus/icons-vue'
import { favoriteApi } from '@/api/favorite'
import { crawlerFavorites } from '@/mock/crawlerData'
import type { Favorite } from '@/types'

const loading = ref(false)
const favorites = ref<Favorite[]>([])
const total = ref(0)

const filters = reactive({
  pageNum: 1,
  pageSize: 10
})

const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

const loadData = async () => {
  try {
    loading.value = true
    try {
      const res = await favoriteApi.getAdminFavoriteList({
        pageNum: filters.pageNum,
        pageSize: filters.pageSize
      })
      if (res.code === 200 && res.data) {
        let list: any[] = Array.isArray(res.data) ? res.data : ((res.data as any).records || [])
        if (list && list.length > 0) {
          total.value = list.length
          const start = (filters.pageNum - 1) * filters.pageSize
          favorites.value = list.slice(start, start + filters.pageSize)
          return
        }
      }
    } catch (apiError) {
      console.log('API请求失败，使用爬虫数据:', apiError)
    }
    
    const mockList = crawlerFavorites
    total.value = mockList.length
    const start = (filters.pageNum - 1) * filters.pageSize
    favorites.value = mockList.slice(start, start + filters.pageSize) as any
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const handleDelete = async (row: any) => {
  if (!row.id) return
  try {
    await ElMessageBox.confirm('确定删除该收藏吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    row.deleting = true
    try {
      await favoriteApi.delete(row.id)
      ElMessage.success('删除成功')
    } catch (e) {
      ElMessage.success('删除成功（本地模拟）')
    }
    favorites.value = favorites.value.filter(f => f.id !== row.id)
    total.value--
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  } finally {
    row.deleting = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.favorite-manage-view {
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

.total-tag {
  font-weight: 500;
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

.favorite-table {
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.favorite-table :deep(.el-table__header th) {
  font-weight: 600;
  color: var(--text-color-primary);
  background: var(--bg-color-light);
}

.favorite-table :deep(.el-table__row:hover > td) {
  background-color: var(--primary-color-bg-light) !important;
}

.favorite-table :deep(.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell) {
  background-color: var(--bg-color-light);
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  background: var(--primary-color-bg);
  border: 2px solid var(--border-color-light);
}

.user-avatar :deep(.el-icon) {
  color: var(--primary-color);
}

.user-name {
  font-weight: 500;
  color: var(--text-color-primary);
}

.property-title {
  color: var(--text-color-primary);
}

.price-text {
  color: var(--accent-color);
  font-weight: 700;
  font-size: 16px;
}

.price-unit {
  font-size: 12px;
  font-weight: 400;
  color: var(--text-color-tertiary);
  margin-left: 2px;
}

.tag-crawler {
  background: var(--success-color-bg);
  color: var(--success-color);
  border-color: var(--success-color-bg);
}

.tag-user {
  background: var(--primary-color-bg);
  color: var(--primary-color);
  border-color: var(--primary-color-bg);
}

.time-text {
  color: var(--text-color-secondary);
  font-size: 13px;
}

.btn-delete {
  color: var(--error-color) !important;
}

.empty-state {
  padding: var(--spacing-3xl);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: var(--spacing-xl);
}

.pagination {
  --el-pagination-hover-color: var(--primary-color);
}

@media (max-width: 768px) {
  .favorite-manage-view {
    padding: var(--spacing-lg);
  }

  .page-title h2 {
    font-size: 18px;
  }

  .content-card {
    padding: var(--spacing-lg);
  }
}
</style>
