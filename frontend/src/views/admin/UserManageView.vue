<template>
  <div class="user-manage-view page-animate">
    <div class="page-header">
      <div class="page-title">
        <span class="title-bar"></span>
        <h2>用户管理</h2>
      </div>
      <el-tag type="primary" class="total-tag">
        <el-icon><User /></el-icon>
        共 {{ total }} 个用户
      </el-tag>
    </div>

    <div class="stats-section">
      <div class="stat-card">
        <div class="stat-icon icon-total">
          <el-icon><UserFilled /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ totalUsers }}</div>
          <div class="stat-label">用户总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon icon-normal">
          <el-icon><Avatar /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ normalUsers }}</div>
          <div class="stat-label">普通用户</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon icon-landlord">
          <el-icon><OfficeBuilding /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ landlordUsers }}</div>
          <div class="stat-label">房东用户</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon icon-active">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ activeUsers }}</div>
          <div class="stat-label">正常状态</div>
        </div>
      </div>
    </div>

    <div class="content-card">
      <div class="filter-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索用户名、手机号、真实姓名"
          clearable
          class="search-input"
          @clear="loadUsers"
          @keyup.enter="loadUsers"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-select
          v-model="filterRole"
          placeholder="用户角色"
          clearable
          class="filter-select"
          @change="loadUsers"
        >
          <el-option label="全部角色" :value="null" />
          <el-option label="普通用户" :value="0" />
          <el-option label="房东" :value="1" />
          <el-option label="管理员" :value="2" />
        </el-select>

        <el-select
          v-model="filterStatus"
          placeholder="用户状态"
          clearable
          class="filter-select"
          @change="loadUsers"
        >
          <el-option label="全部状态" :value="null" />
          <el-option label="正常" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>

        <el-button type="primary" class="btn-search" @click="loadUsers">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>

        <el-button class="btn-reset" @click="handleReset">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="users"
        stripe
        class="user-table"
      >
        <el-table-column prop="id" label="ID" width="80" />
        
        <el-table-column prop="username" label="用户名" min-width="160">
          <template #default="{ row }">
            <div class="user-info">
              <div class="user-avatar">
                <el-avatar :size="40" :src="row.avatar">
                  <el-icon :size="20"><UserFilled /></el-icon>
                </el-avatar>
              </div>
              <div class="user-name-info">
                <div class="user-name">{{ row.username }}</div>
                <div class="user-phone">{{ row.phone }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="realName" label="真实姓名" min-width="100" />

        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />

        <el-table-column prop="role" label="角色" width="110">
          <template #default="{ row }">
            <el-tag :class="getRoleClass(row.role)" size="small">
              {{ getRoleText(row.role) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
              class="status-switch"
            />
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="注册时间" width="170">
          <template #default="{ row }">
            <span class="time-text">{{ formatDate(row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              link
              class="btn-view"
              @click="handleView(row)"
            >
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button
              type="warning"
              size="small"
              link
              class="btn-edit"
              @click="handleEdit(row)"
            >
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button
              type="danger"
              size="small"
              link
              class="btn-delete"
              @click="handleDelete(row)"
              :disabled="row.role === 2"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-if="total > 0"
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          class="pagination"
          @size-change="loadUsers"
          @current-change="loadUsers"
        />
      </div>
    </div>

    <!-- 用户详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="用户详情"
      width="600px"
      class="detail-dialog"
    >
      <el-descriptions :column="2" border v-if="currentUser" class="detail-descriptions">
        <el-descriptions-item label="用户ID">
          {{ currentUser.id }}
        </el-descriptions-item>
        <el-descriptions-item label="用户名">
          {{ currentUser.username }}
        </el-descriptions-item>
        <el-descriptions-item label="真实姓名">
          {{ currentUser.realName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="手机号">
          {{ currentUser.phone }}
        </el-descriptions-item>
        <el-descriptions-item label="邮箱" :span="2">
          {{ currentUser.email || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag :class="getRoleClass(currentUser.role)" size="small">
            {{ getRoleText(currentUser.role) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :class="currentUser.status === 1 ? 'status-active' : 'status-disabled'" size="small">
            {{ currentUser.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间" :span="2">
          {{ formatDate(currentUser.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="最后登录" :span="2">
          {{ formatDate(currentUser.lastLoginTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          {{ currentUser.remark || '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 编辑用户对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑用户"
      width="600px"
      class="edit-dialog"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
        class="edit-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editForm.username" disabled class="disabled-input" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="editForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="editForm.role" style="width: 100%">
            <el-option label="普通用户" :value="0" />
            <el-option label="房东" :value="1" />
            <el-option label="管理员" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="editForm.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="editForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false" class="btn-cancel">取消</el-button>
        <el-button type="primary" class="btn-save" @click="handleSaveEdit" :loading="saving">
          <el-icon><Check /></el-icon>
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  User,
  UserFilled,
  Search,
  Refresh,
  View,
  Edit,
  Delete,
  Avatar,
  OfficeBuilding,
  CircleCheck,
  Check
} from '@element-plus/icons-vue'
import { userApi } from '@/api/user'

const loading = ref(false)
const saving = ref(false)
const users = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const filterRole = ref<number | null>(null)
const filterStatus = ref<number | null>(null)

const viewDialogVisible = ref(false)
const editDialogVisible = ref(false)
const currentUser = ref<any>(null)

const allUsers = ref<any[]>([])

const totalUsers = computed(() => allUsers.value.length)
const normalUsers = computed(() => allUsers.value.filter(u => u.role === 0).length)
const landlordUsers = computed(() => allUsers.value.filter(u => u.role === 1).length)
const activeUsers = computed(() => allUsers.value.filter(u => u.status === 1).length)

const editFormRef = ref<FormInstance>()
const editForm = reactive({
  id: 0,
  username: '',
  realName: '',
  phone: '',
  email: '',
  role: 0,
  status: 1,
  remark: ''
})

const editRules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/, message: '请输入正确的邮箱', trigger: 'blur' }
  ]
}

const loadUsers = async () => {
  try {
    loading.value = true
    const response = await userApi.getAllUsers()
    
    if (response.code === 200 && response.data) {
      allUsers.value = response.data
      let list = [...response.data]
      
      if (searchKeyword.value) {
        const keyword = searchKeyword.value.toLowerCase()
        list = list.filter((item: any) =>
          item.username.toLowerCase().includes(keyword) ||
          item.phone.includes(keyword) ||
          (item.realName && item.realName.toLowerCase().includes(keyword))
        )
      }
      
      if (filterRole.value !== null) {
        list = list.filter((item: any) => item.role === filterRole.value)
      }
      
      if (filterStatus.value !== null) {
        list = list.filter((item: any) => item.status === filterStatus.value)
      }
      
      total.value = list.length
      
      const start = (pageNum.value - 1) * pageSize.value
      const end = start + pageSize.value
      users.value = list.slice(start, end)
    }
  } catch (error: any) {
    console.error('加载用户列表失败:', error)
    ElMessage.error(error.message || '加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  searchKeyword.value = ''
  filterRole.value = null
  filterStatus.value = null
  pageNum.value = 1
  loadUsers()
}

const handleView = (row: any) => {
  currentUser.value = row
  viewDialogVisible.value = true
}

const handleEdit = (row: any) => {
  Object.assign(editForm, {
    id: row.id,
    username: row.username,
    realName: row.realName || '',
    phone: row.phone,
    email: row.email || '',
    role: row.role,
    status: row.status,
    remark: row.remark || ''
  })
  editDialogVisible.value = true
}

const handleSaveEdit = async () => {
  if (!editFormRef.value) return
  
  try {
    const valid = await editFormRef.value.validate()
    if (!valid) return
    
    saving.value = true
    await userApi.updateUser(editForm.id, editForm)
    ElMessage.success('保存成功')
    editDialogVisible.value = false
    loadUsers()
  } catch (error: any) {
    console.error('保存失败:', error)
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const handleStatusChange = async (row: any) => {
  try {
    await userApi.updateUser(row.id, { status: row.status })
    ElMessage.success('状态更新成功')
  } catch (error: any) {
    console.error('状态更新失败:', error)
    ElMessage.error(error.message || '状态更新失败')
    row.status = row.status === 1 ? 0 : 1
  }
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户"${row.username}"吗？此操作不可恢复！`,
      '删除用户',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await userApi.deleteUser(row.id)
    ElMessage.success('删除成功')
    loadUsers()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

const getRoleText = (role: number) => {
  const roleMap: Record<number, string> = {
    0: '普通用户',
    1: '房东',
    2: '管理员'
  }
  return roleMap[role] || '未知'
}

const getRoleClass = (role: number) => {
  const classMap: Record<number, string> = {
    0: 'role-normal',
    1: 'role-landlord',
    2: 'role-admin'
  }
  return classMap[role] || 'role-unknown'
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-manage-view {
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
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  font-weight: 500;
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

.icon-normal {
  background: linear-gradient(135deg, #722ed1 0%, #9254de 100%);
}

.icon-landlord {
  background: linear-gradient(135deg, var(--accent-color) 0%, var(--accent-color-light) 100%);
}

.icon-active {
  background: linear-gradient(135deg, var(--success-color) 0%, var(--success-color-light) 100%);
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
  flex: 1;
  min-width: 240px;
  max-width: 360px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: var(--radius-full);
}

.filter-select {
  width: 150px;
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

.btn-reset {
  display: flex;
  align-items: center;
  gap: 6px;
  border-radius: var(--radius-full);
  padding: 10px 20px;
  font-weight: 500;
  transition: var(--transition-base);
}

.btn-reset:hover {
  color: var(--primary-color);
  border-color: var(--primary-color);
}

.user-table {
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.user-table :deep(.el-table__header th) {
  font-weight: 600;
  color: var(--text-color-primary);
  background: var(--bg-color-light);
}

.user-table :deep(.el-table__row:hover > td) {
  background-color: var(--primary-color-bg-light) !important;
}

.user-table :deep(.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell) {
  background-color: var(--bg-color-light);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  flex-shrink: 0;
}

.user-avatar :deep(.el-avatar) {
  border: 2px solid var(--border-color-light);
  background: var(--primary-color-bg);
}

.user-name-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-weight: 600;
  color: var(--text-color-primary);
  font-size: 14px;
}

.user-phone {
  font-size: 12px;
  color: var(--text-color-tertiary);
}

.role-normal {
  background: var(--primary-color-bg);
  color: var(--primary-color);
  border-color: var(--primary-color-bg);
}

.role-landlord {
  background: var(--accent-color-bg);
  color: var(--accent-color);
  border-color: var(--accent-color-bg);
}

.role-admin {
  background: var(--error-color-bg);
  color: var(--error-color);
  border-color: var(--error-color-bg);
}

.role-unknown {
  background: var(--bg-color-light);
  color: var(--text-color-tertiary);
  border-color: var(--bg-color-light);
}

.status-active {
  background: var(--success-color-bg);
  color: var(--success-color);
  border-color: var(--success-color-bg);
}

.status-disabled {
  background: var(--error-color-bg);
  color: var(--error-color);
  border-color: var(--error-color-bg);
}

.time-text {
  color: var(--text-color-secondary);
  font-size: 13px;
}

.btn-view {
  color: var(--primary-color) !important;
}

.btn-edit {
  color: var(--warning-color) !important;
}

.btn-delete {
  color: var(--error-color) !important;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: var(--spacing-xl);
}

.pagination {
  --el-pagination-hover-color: var(--primary-color);
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

.edit-dialog :deep(.el-dialog__body) {
  padding: var(--spacing-xl);
}

.edit-form :deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
}

.edit-form :deep(.el-textarea__inner) {
  border-radius: var(--radius-md);
}

.disabled-input :deep(.el-input__wrapper) {
  background: var(--bg-color-light);
}

.btn-cancel {
  border-radius: var(--radius-full);
  padding: 8px 20px;
}

.btn-save {
  display: flex;
  align-items: center;
  gap: 6px;
  border-radius: var(--radius-full);
  padding: 8px 20px;
  font-weight: 500;
}

@media (max-width: 1024px) {
  .stats-section {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .user-manage-view {
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
  .btn-search,
  .btn-reset {
    width: 100%;
    max-width: 100%;
  }
}
</style>
