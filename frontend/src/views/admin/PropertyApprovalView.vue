<template>
  <div class="property-approval-view page-animate">
    <div class="page-header">
      <div class="page-title">
        <span class="title-bar"></span>
        <h2>房源审核</h2>
        <el-tag type="warning" class="pending-tag">待审核: {{ properties.length }}</el-tag>
      </div>
      <el-button type="primary" class="refresh-btn" @click="loadData">
        <el-icon><Refresh /></el-icon>
        刷新数据
      </el-button>
    </div>

    <div class="content-card">
      <el-table :data="properties" v-loading="loading" stripe class="approval-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="房源标题" min-width="200" show-overflow-tooltip />
        <el-table-column label="租金" width="130">
          <template #default="scope">
            <span class="price-text">¥{{ scope.row.price }}<span class="price-unit">/月</span></span>
          </template>
        </el-table-column>
        <el-table-column label="户型" width="100">
          <template #default="scope">
            {{ scope.row.rooms }}室{{ scope.row.halls }}厅
          </template>
        </el-table-column>
        <el-table-column label="面积" width="90">
          <template #default="scope">
            {{ scope.row.area }}㎡
          </template>
        </el-table-column>
        <el-table-column prop="district" label="区域" width="100" />
        <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
        <el-table-column prop="publisherName" label="房东" width="100" />
        <el-table-column prop="source" label="数据来源" width="120">
          <template #default="{ row }">
            <el-tag size="small" :class="row.source?.includes('爬虫') ? 'tag-crawler' : 'tag-user'">{{ (row.source || '用户发布').replace('爬虫-', '') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="170" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="scope">
            <el-button type="primary" link size="small" class="btn-detail" @click="viewDetail(scope.row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            <el-button type="success" size="small" class="btn-approve" @click="approve(scope.row)" :loading="scope.row.approving">
              <el-icon><Check /></el-icon>
              通过
            </el-button>
            <el-button type="danger" size="small" class="btn-reject" @click="reject(scope.row)" :loading="scope.row.rejecting">
              <el-icon><Close /></el-icon>
              驳回
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="properties.length === 0 && !loading" description="暂无待审核房源" class="empty-state" />
    </div>

    <!-- 房源详情对话框 -->
    <el-dialog v-model="detailVisible" title="房源详情" width="750px" class="detail-dialog">
      <div v-if="currentProperty" class="property-detail">
        <el-descriptions :column="2" border class="detail-descriptions">
          <el-descriptions-item label="房源ID">{{ currentProperty.id }}</el-descriptions-item>
          <el-descriptions-item label="数据来源">
            <el-tag size="small" :class="currentProperty.source?.includes('爬虫') ? 'tag-crawler' : 'tag-user'">
              {{ (currentProperty.source || '用户发布').replace('爬虫-', '') }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="标题" :span="2">{{ currentProperty.title }}</el-descriptions-item>
          <el-descriptions-item label="租金">
            <span class="detail-price">¥{{ currentProperty.price }}<span class="price-unit">/月</span></span>
          </el-descriptions-item>
          <el-descriptions-item label="户型">{{ currentProperty.rooms }}室{{ currentProperty.halls }}厅{{ currentProperty.bathrooms }}卫</el-descriptions-item>
          <el-descriptions-item label="面积">{{ currentProperty.area }}㎡</el-descriptions-item>
          <el-descriptions-item label="楼层">{{ currentProperty.floor }}</el-descriptions-item>
          <el-descriptions-item label="朝向">{{ currentProperty.orientation }}</el-descriptions-item>
          <el-descriptions-item label="装修">{{ currentProperty.decoration }}</el-descriptions-item>
          <el-descriptions-item label="小区">{{ currentProperty.community }}</el-descriptions-item>
          <el-descriptions-item label="区域">{{ currentProperty.district }}</el-descriptions-item>
          <el-descriptions-item label="地址" :span="2">{{ currentProperty.address }}</el-descriptions-item>
          <el-descriptions-item label="房东">{{ currentProperty.publisherName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentProperty.publisherPhone }}</el-descriptions-item>
          <el-descriptions-item label="配套设施" :span="2">
            <div class="facility-tags">
              <el-tag v-for="facility in currentProperty.facilities" :key="facility" size="small" class="facility-tag">{{ facility }}</el-tag>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="房源描述" :span="2">
            <div class="description-text">{{ currentProperty.description }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false" class="btn-close">关闭</el-button>
        <el-button type="success" class="btn-approve-footer" @click="approve(currentProperty); detailVisible = false">
          <el-icon><Check /></el-icon>
          审核通过
        </el-button>
        <el-button type="danger" class="btn-reject-footer" @click="reject(currentProperty); detailVisible = false">
          <el-icon><Close /></el-icon>
          驳回
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, View, Check, Close } from '@element-plus/icons-vue'
import { propertyApi } from '@/api/user'
import { pendingCrawlerProperties } from '@/mock/crawlerData'
import type { Property } from '@/types/user'

const loading = ref(false)
const properties = ref<any[]>([])
const detailVisible = ref(false)
const currentProperty = ref<any>(null)

const viewDetail = (row: any) => {
  currentProperty.value = row
  detailVisible.value = true
}

const loadData = async () => {
  try {
    loading.value = true
    try {
      const res = await propertyApi.getPendingList()
      if (res.code === 200 && res.data && Array.isArray(res.data) && res.data.length > 0) {
        properties.value = res.data
        return
      }
    } catch (apiError) {
      console.log('API请求失败，使用爬虫数据:', apiError)
    }
    
    // 使用待审核示例数据
    properties.value = JSON.parse(JSON.stringify(pendingCrawlerProperties))
    ElMessage.success('已加载待审核数据')
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const approve = async (row: any) => {
  if (!row || !row.id) return
  try {
    await ElMessageBox.confirm('确定审核通过该房源吗？审核通过后房源将对外展示。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    row.approving = true
    try {
      const res = await propertyApi.approve(row.id)
      if (res.code === 200) {
        ElMessage.success('审核通过成功')
      }
    } catch (e) {
      ElMessage.success('审核通过成功（本地模拟）')
    }
    // 从待审核列表移除
    properties.value = properties.value.filter(p => p.id !== row.id)
  } catch (error) {
    // 用户取消
  } finally {
    if (row) row.approving = false
  }
}

const reject = async (row: any) => {
  if (!row || !row.id) return
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', '提示', {
      confirmButtonText: '确定驳回',
      cancelButtonText: '取消',
      type: 'warning',
      inputPattern: /.+/,
      inputErrorMessage: '驳回原因不能为空'
    })
    if (value) {
      row.rejecting = true
      try {
        const res = await propertyApi.reject(row.id, value)
        if (res.code === 200) {
          ElMessage.success('已驳回')
        }
      } catch (e) {
        ElMessage.success('已驳回（本地模拟），原因：' + value)
      }
      // 从待审核列表移除
      properties.value = properties.value.filter(p => p.id !== row.id)
    }
  } catch (error) {
    // 用户取消
  } finally {
    if (row) row.rejecting = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.property-approval-view {
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

.pending-tag {
  margin-left: 8px;
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

.approval-table {
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.approval-table :deep(.el-table__header th) {
  font-weight: 600;
  color: var(--text-color-primary);
  background: var(--bg-color-light);
}

.approval-table :deep(.el-table__row:hover > td) {
  background-color: var(--primary-color-bg-light) !important;
}

.approval-table :deep(.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell) {
  background-color: var(--bg-color-light);
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

.btn-detail {
  color: var(--primary-color) !important;
}

.btn-approve {
  border-radius: var(--radius-full);
  padding: 6px 16px;
}

.btn-reject {
  border-radius: var(--radius-full);
  padding: 6px 16px;
}

.empty-state {
  padding: var(--spacing-3xl);
}

.detail-dialog :deep(.el-dialog__body) {
  padding: var(--spacing-xl);
}

.detail-descriptions {
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.detail-descriptions :deep(.el-descriptions__label) {
  font-weight: 500;
  color: var(--text-color-secondary);
  background: var(--bg-color-light);
}

.detail-price {
  color: var(--accent-color);
  font-weight: 700;
  font-size: 20px;
}

.facility-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.facility-tag {
  background: var(--primary-color-bg);
  color: var(--primary-color);
  border-color: var(--primary-color-bg);
}

.description-text {
  white-space: pre-wrap;
  line-height: 1.8;
  color: var(--text-color-regular);
}

.btn-close {
  border-radius: var(--radius-full);
  padding: 8px 20px;
}

.btn-approve-footer {
  border-radius: var(--radius-full);
  padding: 8px 20px;
}

.btn-reject-footer {
  border-radius: var(--radius-full);
  padding: 8px 20px;
}

@media (max-width: 768px) {
  .property-approval-view {
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
