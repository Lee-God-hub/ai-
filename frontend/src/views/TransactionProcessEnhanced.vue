<template>
  <div class="process-container page-animate">
    <el-card class="process-card">
      <template #header>
        <div class="card-header">
          <span>交易流程管理</span>
          <el-tag :type="statusTagType">{{ statusText }}</el-tag>
        </div>
      </template>

      <!-- Tab标签页 -->
      <el-tabs v-model="activeTab" type="border-card">
        <!-- 流程进度标签页 -->
        <el-tab-pane label="流程进度" name="progress">
          <!-- 流程进度条 -->
          <el-steps :active="activeStep" finish-status="success" align-center class="process-steps">
            <el-step 
              v-for="(stage, index) in stages" 
              :key="stage.code"
              :title="stage.name"
              :status="getStepStatus(stage)"
            />
          </el-steps>

          <!-- 流程日志时间轴 -->
          <el-divider>流程记录</el-divider>
          <el-timeline>
            <el-timeline-item 
              v-for="log in logs" 
              :key="log.id"
              :timestamp="formatTime(log.createTime)"
              :type="getTimelineType(log.status)"
            >
              <el-card>
                <h4>{{ log.stageName }}</h4>
                <p v-if="log.remark">{{ log.remark }}</p>
                <p class="operator">操作人：{{ log.operatorName || '系统' }}</p>
              </el-card>
            </el-timeline-item>
          </el-timeline>

          <!-- 操作按钮 -->
          <div class="action-buttons">
            <el-button 
              type="primary" 
              @click="handleNextStage"
              :disabled="!canOperate"
            >
              推进到下一阶段
            </el-button>
            <el-button 
              type="danger" 
              @click="handleCancel"
              :disabled="appointment?.status === 3"
            >
              取消交易
            </el-button>
          </div>
        </el-tab-pane>

        <!-- 附件管理标签页 -->
        <el-tab-pane label="附件管理" name="attachments">
          <div class="attachments-section">
            <el-button type="primary" @click="showUploadDialog" icon="Upload">上传附件</el-button>
            
            <el-table :data="attachments" style="margin-top: 20px">
              <el-table-column prop="fileName" label="文件名" />
              <el-table-column prop="attachmentTypeName" label="类型" width="100" />
              <el-table-column prop="fileSizeFormatted" label="大小" width="100" />
              <el-table-column prop="uploaderName" label="上传人" width="120" />
              <el-table-column prop="uploadTime" label="上传时间" width="180" :formatter="formatTableTime" />
              <el-table-column label="操作" width="150">
                <template #default="scope">
                  <el-button link type="primary" size="small">下载</el-button>
                  <el-button link type="danger" size="small" @click="deleteAttachment(scope.row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <!-- 支付记录标签页 -->
        <el-tab-pane label="支付记录" name="payments">
          <div class="payments-section">
            <el-button type="primary" @click="showPaymentDialog" icon="CreditCard">发起支付</el-button>
            
            <el-table :data="payments" style="margin-top: 20px">
              <el-table-column prop="paymentTypeName" label="支付类型" width="120" />
              <el-table-column prop="amount" label="金额(元)" width="120" />
              <el-table-column prop="paymentMethodName" label="支付方式" width="120" />
              <el-table-column prop="paymentStatusName" label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="getPaymentStatusType(scope.row.paymentStatus)">
                    {{ scope.row.paymentStatusName }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="payerName" label="支付人" width="120" />
              <el-table-column prop="transactionNo" label="流水号" width="150" />
              <el-table-column prop="paymentTime" label="支付时间" width="180" :formatter="formatTableTime" />
              <el-table-column label="操作" width="100">
                <template #default="scope">
                  <el-button 
                    v-if="scope.row.paymentStatus === 0"
                    link 
                    type="primary" 
                    size="small" 
                    @click="handlePay(scope.row.id)"
                  >
                    去支付
                  </el-button>
                  <span v-else style="color: #67C23A">已支付</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <!-- 数据统计标签页 -->
        <el-tab-pane label="数据统计" name="statistics">
          <div class="statistics-section">
            <el-row :gutter="20">
              <el-col :span="6" v-for="stat in summaryStats" :key="stat.label">
                <el-card shadow="hover" class="stat-card">
                  <div class="stat-content">
                    <div class="stat-label">{{ stat.label }}</div>
                    <div class="stat-value">{{ stat.value }}</div>
                  </div>
                </el-card>
              </el-col>
            </el-row>

            <el-divider>近7天趋势</el-divider>
            <div class="chart-placeholder">
              <el-empty description="统计图表展示区域（可集成 ECharts）" />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 推进流程对话框 -->
    <el-dialog v-model="nextStageDialog" title="推进流程" width="500px">
      <el-form :model="nextStageForm" label-width="80px">
        <el-form-item label="备注">
          <el-input 
            v-model="nextStageForm.remark" 
            type="textarea"
            :rows="4"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="nextStageDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmNextStage">确认推进</el-button>
      </template>
    </el-dialog>

    <!-- 取消交易对话框 -->
    <el-dialog v-model="cancelDialog" title="取消交易" width="500px">
      <el-form :model="cancelForm" label-width="80px">
        <el-form-item label="取消原因" required>
          <el-input 
            v-model="cancelForm.reason" 
            type="textarea"
            :rows="4"
            placeholder="请输入取消原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelDialog = false">取消</el-button>
        <el-button type="danger" @click="confirmCancel">确认取消</el-button>
      </template>
    </el-dialog>

    <!-- 上传附件对话框 -->
    <el-dialog v-model="uploadDialog" title="上传附件" width="500px">
      <el-form :model="uploadForm" label-width="90px">
        <el-form-item label="附件类型" required>
          <el-select v-model="uploadForm.attachmentType" placeholder="请选择附件类型">
            <el-option label="合同" :value="1" />
            <el-option label="凭证" :value="2" />
            <el-option label="其他" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="文件名称" required>
          <el-input v-model="uploadForm.fileName" placeholder="请输入文件名称（模拟上传）" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input 
            v-model="uploadForm.remark" 
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmUpload">确认上传</el-button>
      </template>
    </el-dialog>

    <!-- 发起支付对话框 -->
    <el-dialog v-model="paymentDialog" title="发起支付" width="500px">
      <el-form :model="paymentForm" label-width="90px">
        <el-form-item label="支付类型" required>
          <el-select v-model="paymentForm.paymentType" placeholder="请选择支付类型">
            <el-option label="定金" :value="1" />
            <el-option label="首付" :value="2" />
            <el-option label="尾款" :value="3" />
            <el-option label="租金" :value="4" />
            <el-option label="押金" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付金额" required>
          <el-input v-model="paymentForm.amount" placeholder="请输入金额">
            <template #append>元</template>
          </el-input>
        </el-form-item>
        <el-form-item label="支付方式" required>
          <el-select v-model="paymentForm.paymentMethod" placeholder="请选择支付方式">
            <el-option label="微信支付" :value="1" />
            <el-option label="支付宝" :value="2" />
            <el-option label="银行卡" :value="3" />
            <el-option label="现金" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input 
            v-model="paymentForm.remark" 
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="paymentDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmCreatePayment">创建支付订单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import { useRoute } from 'vue-router'

const route = useRoute()
const appointmentId = ref(Number(route.params.id))

// Tab切换
const activeTab = ref('progress')

// 流程数据
const appointment = ref<any>(null)
const stages = ref<any[]>([])
const logs = ref<any[]>([])

// 附件数据
const attachments = ref<any[]>([])

// 支付数据
const payments = ref<any[]>([])

// 统计数据
const statistics = ref<any[]>([])

// 对话框状态
const nextStageDialog = ref(false)
const cancelDialog = ref(false)
const uploadDialog = ref(false)
const paymentDialog = ref(false)

// 表单数据
const nextStageForm = ref({ remark: '' })
const cancelForm = ref({ reason: '' })
const uploadForm = ref({
  attachmentType: 3,
  fileName: '',
  remark: ''
})
const paymentForm = ref({
  paymentType: 1,
  amount: '',
  paymentMethod: 1,
  remark: ''
})

// 计算属性
const activeStep = computed(() => {
  return stages.value.findIndex(s => s.code === appointment.value?.currentStage)
})

const statusText = computed(() => {
  const statusMap: any = {
    0: '待确认',
    1: '已确认',
    2: '已完成',
    3: '已取消'
  }
  return statusMap[appointment.value?.status] || '未知'
})

const statusTagType = computed(() => {
  const typeMap: any = {
    0: 'warning',
    1: 'success',
    2: 'info',
    3: 'danger'
  }
  return typeMap[appointment.value?.status] || 'info'
})

const canOperate = computed(() => {
  return appointment.value?.status !== 3 && 
         appointment.value?.currentStage !== 'completed'
})

const summaryStats = computed(() => [
  { label: '总流程数', value: statistics.value.reduce((sum, s) => sum + s.totalCount, 0) },
  { label: '已完成', value: statistics.value.reduce((sum, s) => sum + s.completedCount, 0) },
  { label: '进行中', value: statistics.value.reduce((sum, s) => sum + s.inProgressCount, 0) },
  { label: '已取消', value: statistics.value.reduce((sum, s) => sum + s.cancelledCount, 0) }
])

// 方法
const getStepStatus = (stage: any) => {
  if (stage.status === 1) return 'finish'
  if (stage.status === 0) return 'process'
  if (stage.status === 2) return 'error'
  return 'wait'
}

const getTimelineType = (status: number) => {
  const typeMap: any = {
    0: 'primary',
    1: 'success',
    2: 'danger',
    3: 'info'
  }
  return typeMap[status] || 'primary'
}

const getPaymentStatusType = (status: number) => {
  const typeMap: any = {
    0: 'warning',
    1: 'success',
    2: 'info'
  }
  return typeMap[status] || 'info'
}

const formatTime = (time: string) => {
  return new Date(time).toLocaleString('zh-CN')
}

const formatTableTime = (row: any, column: any, cellValue: any) => {
  return cellValue ? new Date(cellValue).toLocaleString('zh-CN') : '-'
}

// 加载流程详情
const loadProcessDetail = async () => {
  try {
    const res: any = await request.get(`/transaction-process/${appointmentId.value}`)
    if (res.success) {
      appointment.value = res.data.appointment
      stages.value = res.data.stages
      logs.value = res.data.logs
    } else {
      ElMessage.error(res.message || '加载流程详情失败')
    }
  } catch (error) {
    ElMessage.error('加载流程详情失败')
  }
}

// 加载附件列表
const loadAttachments = async () => {
  try {
    const res: any = await request.get(`/transaction-process/attachment/${appointmentId.value}`)
    if (res.success) {
      attachments.value = res.data.map((item: any) => ({
        ...item,
        attachmentTypeName: ['', '合同', '凭证', '其他'][item.attachmentType],
        fileSizeFormatted: (item.fileSize / 1024).toFixed(2) + ' KB'
      }))
    }
  } catch (error) {
    console.error('加载附件失败:', error)
  }
}

// 加载支付记录
const loadPayments = async () => {
  try {
    const res: any = await request.get(`/transaction-process/payment/${appointmentId.value}`)
    if (res.success) {
      payments.value = res.data.map((item: any) => ({
        ...item,
        paymentTypeName: ['', '定金', '首付', '尾款', '租金', '押金'][item.paymentType],
        paymentMethodName: ['', '微信支付', '支付宝', '银行卡', '现金'][item.paymentMethod],
        paymentStatusName: ['待支付', '已支付', '已退款'][item.paymentStatus]
      }))
    }
  } catch (error) {
    console.error('加载支付记录失败:', error)
  }
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    const res: any = await request.get('/transaction-process/statistics?days=7')
    if (res.success) {
      statistics.value = res.data || []
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 推进到下一阶段
const handleNextStage = () => {
  nextStageForm.value.remark = ''
  nextStageDialog.value = true
}

const confirmNextStage = async () => {
  try {
    const userInfoStr = localStorage.getItem('userInfo')
    if (!userInfoStr) {
      ElMessage.error('用户信息缺失，请重新登录')
      return
    }
    
    const userInfo = JSON.parse(userInfoStr)
    const userId = userInfo.id
    const userName = userInfo.realName || userInfo.username
    
    if (!userId) {
      ElMessage.error('用户ID缺失，请重新登录')
      return
    }
    
    const res: any = await request.post('/transaction-process/next-stage', {
      appointmentId: appointmentId.value,
      operatorId: userId,
      operatorName: userName,
      remark: nextStageForm.value.remark
    })
    
    if (res.success) {
      ElMessage.success('流程推进成功')
      nextStageDialog.value = false
      await loadProcessDetail()
    } else {
      ElMessage.error(res.message || '流程推进失败')
    }
  } catch (error: any) {
    console.error('流程推进失败：', error)
    ElMessage.error(error.message || '操作失败')
  }
}

// 取消交易
const handleCancel = () => {
  cancelForm.value.reason = ''
  cancelDialog.value = true
}

const confirmCancel = async () => {
  if (!cancelForm.value.reason) {
    ElMessage.warning('请输入取消原因')
    return
  }
  
  try {
    const userInfoStr = localStorage.getItem('userInfo')
    if (!userInfoStr) {
      ElMessage.error('用户信息缺失，请重新登录')
      return
    }
    
    const userInfo = JSON.parse(userInfoStr)
    const userId = userInfo.id
    const userName = userInfo.realName || userInfo.username
    
    if (!userId) {
      ElMessage.error('用户ID缺失，请重新登录')
      return
    }
    
    const res: any = await request.post(`/transaction-process/cancel/${appointmentId.value}`, {
      operatorId: userId,
      operatorName: userName,
      reason: cancelForm.value.reason
    })
    
    if (res.success) {
      ElMessage.success('交易已取消')
      cancelDialog.value = false
      await loadProcessDetail()
    } else {
      ElMessage.error(res.message || '取消交易失败')
    }
  } catch (error: any) {
    console.error('取消交易失败：', error)
    ElMessage.error(error.message || '操作失败')
  }
}

// 上传附件
const showUploadDialog = () => {
  uploadForm.value = {
    attachmentType: 3,
    fileName: '',
    remark: ''
  }
  uploadDialog.value = true
}

const confirmUpload = async () => {
  if (!uploadForm.value.fileName) {
    ElMessage.warning('请输入文件名称')
    return
  }
  
  try {
    const userInfoStr = localStorage.getItem('userInfo')
    if (!userInfoStr) {
      ElMessage.error('用户信息缺失，请重新登录')
      return
    }
    
    const userInfo = JSON.parse(userInfoStr)
    
    const res: any = await request.post('/transaction-process/attachment/upload', {
      appointmentId: appointmentId.value,
      stageCode: appointment.value.currentStage,
      fileName: uploadForm.value.fileName,
      attachmentType: uploadForm.value.attachmentType,
      uploaderId: userInfo.id,
      uploaderName: userInfo.realName || userInfo.username,
      remark: uploadForm.value.remark,
      fileSize: 1024000,
      fileType: 'application/pdf'
    })
    
    if (res.success) {
      ElMessage.success('附件上传成功')
      uploadDialog.value = false
      await loadAttachments()
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '上传失败')
  }
}

// 删除附件
const deleteAttachment = async (attachmentId: number) => {
  try {
    const res: any = await request.delete(`/transaction-process/attachment/${attachmentId}`)
    if (res.success) {
      ElMessage.success('附件删除成功')
      await loadAttachments()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

// 发起支付
const showPaymentDialog = () => {
  paymentForm.value = {
    paymentType: 1,
    amount: '',
    paymentMethod: 1,
    remark: ''
  }
  paymentDialog.value = true
}

const confirmCreatePayment = async () => {
  if (!paymentForm.value.amount) {
    ElMessage.warning('请输入支付金额')
    return
  }
  
  try {
    const userInfoStr = localStorage.getItem('userInfo')
    if (!userInfoStr) {
      ElMessage.error('用户信息缺失，请重新登录')
      return
    }
    
    const userInfo = JSON.parse(userInfoStr)
    
    const res: any = await request.post('/transaction-process/payment/create', {
      appointmentId: appointmentId.value,
      stageCode: appointment.value.currentStage,
      paymentType: paymentForm.value.paymentType,
      amount: paymentForm.value.amount,
      paymentMethod: paymentForm.value.paymentMethod,
      payerId: userInfo.id,
      payerName: userInfo.realName || userInfo.username,
      remark: paymentForm.value.remark
    })
    
    if (res.success) {
      ElMessage.success('支付订单创建成功')
      paymentDialog.value = false
      await loadPayments()
    } else {
      ElMessage.error(res.message || '创建失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '创建失败')
  }
}

// 模拟支付
const handlePay = async (paymentId: number) => {
  try {
    const res: any = await request.post(`/transaction-process/payment/pay/${paymentId}`)
    if (res.success) {
      ElMessage.success('支付成功')
      await loadPayments()
    } else {
      ElMessage.error(res.message || '支付失败')
    }
  } catch (error) {
    ElMessage.error('支付失败')
  }
}

// 页面加载时执行
onMounted(async () => {
  await loadProcessDetail()
  await loadAttachments()
  await loadPayments()
  await loadStatistics()
})
</script>

<style scoped>
.process-container {
  padding: 20px;
}

.process-card {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.process-steps {
  margin: 30px 0;
}

.el-timeline {
  margin-top: 20px;
}

.operator {
  margin-top: 10px;
  color: #909399;
  font-size: 14px;
}

.action-buttons {
  margin-top: 30px;
  text-align: center;
}

.action-buttons .el-button {
  margin: 0 10px;
}

.attachments-section,
.payments-section,
.statistics-section {
  padding: 20px;
}

.stat-card {
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-content {
  padding: 20px 0;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
}

.chart-placeholder {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border-radius: 4px;
}
</style>
