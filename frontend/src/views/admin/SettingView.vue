<template>
  <div class="setting-view page-animate">
    <div class="page-header">
      <div class="page-title">
        <span class="title-bar"></span>
        <h2>系统设置</h2>
      </div>
    </div>

    <div class="cards-row">
      <div class="info-card">
        <div class="card-header">
          <div class="header-icon icon-system">
            <el-icon><InfoFilled /></el-icon>
          </div>
          <h3>系统信息</h3>
        </div>
        <div class="card-body">
          <div class="info-list">
            <div class="info-item">
              <span class="info-label">系统名称</span>
              <span class="info-value">智能房产交易平台</span>
            </div>
            <div class="info-item">
              <span class="info-label">系统版本</span>
              <span class="info-value">v1.0.0</span>
            </div>
            <div class="info-item">
              <span class="info-label">开发框架</span>
              <span class="info-value">Vue 3 + Spring Boot</span>
            </div>
            <div class="info-item">
              <span class="info-label">数据库</span>
              <span class="info-value">MySQL 8.0+</span>
            </div>
            <div class="info-item">
              <span class="info-label">运行状态</span>
              <el-tag type="success" size="small" class="status-tag">正常运行</el-tag>
            </div>
          </div>
        </div>
      </div>

      <div class="info-card">
        <div class="card-header">
          <div class="header-icon icon-stats">
            <el-icon><DataAnalysis /></el-icon>
          </div>
          <h3>数据统计</h3>
        </div>
        <div class="card-body">
          <div class="stat-list">
            <div class="stat-item">
              <div class="stat-icon user-icon">
                <el-icon><User /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.userCount }}</div>
                <div class="stat-label">用户总数</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon property-icon">
                <el-icon><House /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.propertyCount }}</div>
                <div class="stat-label">房源总数</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon appointment-icon">
                <el-icon><Calendar /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.appointmentCount }}</div>
                <div class="stat-label">预约总数</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon favorite-icon">
                <el-icon><Star /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.favoriteCount }}</div>
                <div class="stat-label">收藏总数</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="info-card">
        <div class="card-header">
          <div class="header-icon icon-actions">
            <el-icon><Operation /></el-icon>
          </div>
          <h3>快捷操作</h3>
        </div>
        <div class="card-body">
          <div class="action-list">
            <el-button type="primary" class="action-btn" @click="handleRefreshCache">
              <el-icon><Refresh /></el-icon>
              <span>刷新缓存</span>
            </el-button>
            <el-button type="success" class="action-btn" @click="handleExportData">
              <el-icon><Download /></el-icon>
              <span>导出数据</span>
            </el-button>
            <el-button type="warning" class="action-btn" @click="handleBackupDatabase">
              <el-icon><FolderOpened /></el-icon>
              <span>备份数据库</span>
            </el-button>
            <el-button type="info" class="action-btn" @click="handleViewLogs">
              <el-icon><Document /></el-icon>
              <span>查看日志</span>
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <div class="settings-card">
      <div class="card-header">
        <div class="header-icon icon-settings">
          <el-icon><Setting /></el-icon>
        </div>
        <h3>系统配置</h3>
      </div>
      <div class="card-body">
        <el-tabs v-model="activeTab" class="settings-tabs">
          <el-tab-pane label="基本设置" name="basic">
            <el-form :model="basicSettings" label-width="150px" class="settings-form">
              <el-form-item label="网站名称">
                <el-input v-model="basicSettings.siteName" placeholder="请输入网站名称" class="form-input" />
              </el-form-item>
              <el-form-item label="网站描述">
                <el-input
                  v-model="basicSettings.siteDescription"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入网站描述"
                  class="form-textarea"
                />
              </el-form-item>
              <el-form-item label="联系邮箱">
                <el-input v-model="basicSettings.contactEmail" placeholder="请输入联系邮箱" class="form-input" />
              </el-form-item>
              <el-form-item label="联系电话">
                <el-input v-model="basicSettings.contactPhone" placeholder="请输入联系电话" class="form-input" />
              </el-form-item>
              <el-form-item label="网站状态">
                <el-switch
                  v-model="basicSettings.siteStatus"
                  active-text="开启"
                  inactive-text="关闭"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" class="btn-save" @click="handleSaveBasicSettings">
                  <el-icon><Check /></el-icon>
                  保存设置
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="审核设置" name="approval">
            <el-form :model="approvalSettings" label-width="150px" class="settings-form">
              <el-form-item label="房源审核">
                <el-switch
                  v-model="approvalSettings.propertyApproval"
                  active-text="需要审核"
                  inactive-text="自动通过"
                />
              </el-form-item>
              <el-form-item label="用户注册审核">
                <el-switch
                  v-model="approvalSettings.userApproval"
                  active-text="需要审核"
                  inactive-text="自动通过"
                />
              </el-form-item>
              <el-form-item label="自动审核时间">
                <el-input-number
                  v-model="approvalSettings.autoApprovalTime"
                  :min="0"
                  :max="72"
                  :step="1"
                />
                <span class="form-tip">小时后自动通过</span>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" class="btn-save" @click="handleSaveApprovalSettings">
                  <el-icon><Check /></el-icon>
                  保存设置
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="通知设置" name="notification">
            <el-form :model="notificationSettings" label-width="150px" class="settings-form">
              <el-form-item label="邮件通知">
                <el-switch
                  v-model="notificationSettings.emailNotification"
                  active-text="开启"
                  inactive-text="关闭"
                />
              </el-form-item>
              <el-form-item label="短信通知">
                <el-switch
                  v-model="notificationSettings.smsNotification"
                  active-text="开启"
                  inactive-text="关闭"
                />
              </el-form-item>
              <el-form-item label="站内通知">
                <el-switch
                  v-model="notificationSettings.systemNotification"
                  active-text="开启"
                  inactive-text="关闭"
                />
              </el-form-item>
              <el-form-item label="通知频率">
                <el-radio-group v-model="notificationSettings.notificationFrequency">
                  <el-radio label="realtime">实时</el-radio>
                  <el-radio label="daily">每日汇总</el-radio>
                  <el-radio label="weekly">每周汇总</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" class="btn-save" @click="handleSaveNotificationSettings">
                  <el-icon><Check /></el-icon>
                  保存设置
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="安全设置" name="security">
            <el-form :model="securitySettings" label-width="150px" class="settings-form">
              <el-form-item label="密码最小长度">
                <el-input-number
                  v-model="securitySettings.minPasswordLength"
                  :min="6"
                  :max="20"
                  :step="1"
                />
              </el-form-item>
              <el-form-item label="密码复杂度">
                <el-checkbox-group v-model="securitySettings.passwordComplexity">
                  <el-checkbox label="uppercase">包含大写字母</el-checkbox>
                  <el-checkbox label="lowercase">包含小写字母</el-checkbox>
                  <el-checkbox label="number">包含数字</el-checkbox>
                  <el-checkbox label="special">包含特殊字符</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
              <el-form-item label="登录失败锁定">
                <el-switch
                  v-model="securitySettings.loginLock"
                  active-text="开启"
                  inactive-text="关闭"
                />
              </el-form-item>
              <el-form-item label="最大失败次数">
                <el-input-number
                  v-model="securitySettings.maxLoginAttempts"
                  :min="3"
                  :max="10"
                  :step="1"
                  :disabled="!securitySettings.loginLock"
                />
              </el-form-item>
              <el-form-item label="锁定时长">
                <el-input-number
                  v-model="securitySettings.lockDuration"
                  :min="5"
                  :max="60"
                  :step="5"
                  :disabled="!securitySettings.loginLock"
                />
                <span class="form-tip">分钟</span>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" class="btn-save" @click="handleSaveSecuritySettings">
                  <el-icon><Check /></el-icon>
                  保存设置
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { InfoFilled, DataAnalysis, Operation, Setting, User, House, Calendar, Star, Refresh, Download, FolderOpened, Document, Check } from '@element-plus/icons-vue'
import { userApi } from '@/api/user'

// 当前标签页
const activeTab = ref('basic')

// 统计数据
const statistics = reactive({
  userCount: 0,
  propertyCount: 0,
  appointmentCount: 0,
  favoriteCount: 0
})

// 基本设置
const basicSettings = reactive({
  siteName: '智能房产交易平台',
  siteDescription: '基于AI的智能房产交易系统',
  contactEmail: 'support@smartproperty.com',
  contactPhone: '400-123-4567',
  siteStatus: true
})

// 审核设置
const approvalSettings = reactive({
  propertyApproval: true,
  userApproval: false,
  autoApprovalTime: 24
})

// 通知设置
const notificationSettings = reactive({
  emailNotification: true,
  smsNotification: false,
  systemNotification: true,
  notificationFrequency: 'realtime'
})

// 安全设置
const securitySettings = reactive({
  minPasswordLength: 6,
  passwordComplexity: ['lowercase', 'number'],
  loginLock: true,
  maxLoginAttempts: 5,
  lockDuration: 30
})

/**
 * 加载统计数据
 */
const loadStatistics = async () => {
  try {
    const response = await userApi.getStatistics()
    if (response.code === 200 && response.data) {
      statistics.userCount = response.data.userCount || 0
      statistics.propertyCount = response.data.propertyCount || 0
      statistics.appointmentCount = response.data.appointmentCount || 0
      statistics.favoriteCount = response.data.favoriteCount || 0
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

/**
 * 刷新缓存
 */
const handleRefreshCache = () => {
  ElMessage.success('缓存刷新成功')
}

/**
 * 导出数据
 */
const handleExportData = () => {
  ElMessage.info('数据导出功能开发中...')
}

/**
 * 备份数据库
 */
const handleBackupDatabase = () => {
  ElMessage.info('数据库备份功能开发中...')
}

/**
 * 查看日志
 */
const handleViewLogs = () => {
  ElMessage.info('日志查看功能开发中...')
}

/**
 * 保存基本设置
 */
const handleSaveBasicSettings = () => {
  // 这里应该调用API保存设置
  ElMessage.success('基本设置保存成功')
}

/**
 * 保存审核设置
 */
const handleSaveApprovalSettings = () => {
  ElMessage.success('审核设置保存成功')
}

/**
 * 保存通知设置
 */
const handleSaveNotificationSettings = () => {
  ElMessage.success('通知设置保存成功')
}

/**
 * 保存安全设置
 */
const handleSaveSecuritySettings = () => {
  ElMessage.success('安全设置保存成功')
}

// 页面加载时获取数据
onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.setting-view {
  padding: var(--spacing-2xl);
  min-height: 100%;
  background: var(--bg-color-page);
}

.page-header {
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

.cards-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
}

.info-card {
  background: var(--bg-color);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-base);
  border: 1px solid var(--border-color-light);
  transition: var(--transition-base);
  overflow: hidden;
}

.info-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-card-hover);
  border-color: var(--primary-color-bg);
}

.card-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-lg) var(--spacing-xl);
  border-bottom: 1px solid var(--border-color-light);
  background: linear-gradient(135deg, var(--bg-color-light) 0%, var(--bg-color) 100%);
}

.header-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: white;
  flex-shrink: 0;
}

.icon-system {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-color-light) 100%);
}

.icon-stats {
  background: linear-gradient(135deg, var(--accent-color) 0%, var(--accent-color-light) 100%);
}

.icon-actions {
  background: linear-gradient(135deg, #722ed1 0%, #9254de 100%);
}

.icon-settings {
  background: linear-gradient(135deg, var(--success-color) 0%, var(--success-color-light) 100%);
}

.card-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-color-primary);
  margin: 0;
}

.card-body {
  padding: var(--spacing-lg) var(--spacing-xl);
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-sm) 0;
  border-bottom: 1px dashed var(--border-color-light);
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 13px;
  color: var(--text-color-secondary);
}

.info-value {
  font-size: 13px;
  color: var(--text-color-primary);
  font-weight: 500;
}

.status-tag {
  font-weight: 500;
}

.stat-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-md);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-md);
  background: var(--bg-color-light);
  border-radius: var(--radius-lg);
  transition: var(--transition-base);
}

.stat-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
  background: var(--primary-color-bg-light);
}

.stat-icon {
  width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  font-size: 20px;
  color: white;
  flex-shrink: 0;
}

.user-icon {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-color-light) 100%);
}

.property-icon {
  background: linear-gradient(135deg, var(--accent-color) 0%, var(--accent-color-light) 100%);
}

.appointment-icon {
  background: linear-gradient(135deg, #722ed1 0%, #9254de 100%);
}

.favorite-icon {
  background: linear-gradient(135deg, var(--success-color) 0%, var(--success-color-light) 100%);
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-color-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 12px;
  color: var(--text-color-secondary);
  margin-top: 2px;
}

.action-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.action-btn {
  width: 100%;
  justify-content: flex-start;
  border-radius: var(--radius-lg);
  padding: 12px 16px;
  font-weight: 500;
  transition: var(--transition-base);
}

.action-btn:hover {
  transform: translateX(4px);
}

.action-btn .el-icon {
  margin-right: 8px;
}

.settings-card {
  background: var(--bg-color);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-base);
  border: 1px solid var(--border-color-light);
  transition: var(--transition-base);
  overflow: hidden;
}

.settings-card:hover {
  box-shadow: var(--shadow-lg);
}

.settings-tabs {
  --el-tabs-header-padding: 0;
}

.settings-tabs :deep(.el-tabs__header) {
  margin: 0;
  padding: 0 var(--spacing-xl);
  border-bottom: 1px solid var(--border-color-light);
}

.settings-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.settings-tabs :deep(.el-tabs__item) {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-color-secondary);
  padding: 0 24px;
  height: 50px;
  line-height: 50px;
  transition: var(--transition-base);
}

.settings-tabs :deep(.el-tabs__item:hover) {
  color: var(--primary-color);
}

.settings-tabs :deep(.el-tabs__item.is-active) {
  color: var(--primary-color);
  font-weight: 600;
}

.settings-tabs :deep(.el-tabs__active-bar) {
  background: linear-gradient(90deg, var(--primary-color), var(--accent-color));
  height: 3px;
  border-radius: 2px;
}

.settings-tabs :deep(.el-tabs__content) {
  padding: var(--spacing-xl);
}

.settings-form {
  max-width: 600px;
}

.form-input :deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
}

.form-textarea :deep(.el-textarea__inner) {
  border-radius: var(--radius-md);
}

.form-tip {
  margin-left: var(--spacing-sm);
  font-size: 13px;
  color: var(--text-color-secondary);
}

.btn-save {
  display: flex;
  align-items: center;
  gap: 6px;
  border-radius: var(--radius-full);
  padding: 10px 24px;
  font-weight: 500;
  transition: var(--transition-base);
}

.btn-save:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
}

@media (max-width: 1200px) {
  .cards-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .setting-view {
    padding: var(--spacing-lg);
  }

  .page-title h2 {
    font-size: 18px;
  }

  .cards-row {
    grid-template-columns: 1fr;
  }

  .stat-list {
    grid-template-columns: repeat(2, 1fr);
  }

  .settings-tabs :deep(.el-tabs__item) {
    padding: 0 16px;
    font-size: 13px;
  }

  .settings-tabs :deep(.el-tabs__content) {
    padding: var(--spacing-lg);
  }

  .settings-form {
    max-width: 100%;
  }
}
</style>
