<template>
  <div class="process-container page-animate">
    <el-card class="process-card">
      <template #header>
        <div class="card-header">
          <span>交易流程管理</span>
          <el-tag :type="statusTagType">{{ statusText }}</el-tag>
        </div>
      </template>

      <!-- 流程进度内容 -->
      <div class="progress-content">
          <!-- 流程进度条 -->
          <el-steps :active="activeStep" finish-status="success" align-center class="process-steps">
            <el-step 
              v-for="(stage, index) in stages" 
              :key="stage.code"
              :title="stage.name"
              :status="getStepStatus(stage)"
            />
          </el-steps>

          <!-- 当前阶段操作区 -->
          <el-card class="current-stage-card" v-if="currentStageInfo">
            <template #header>
              <div class="stage-header">
                <span>当前阶段：{{ currentStageInfo.name }}</span>
                <el-tag :type="stageActionType">{{ stageActionText }}</el-tag>
              </div>
            </template>

            <!-- 阶段需要的操作 -->
            <div class="stage-actions">
              <!-- 需要上传附件 -->
              <div v-if="needUploadAttachment" class="action-section">
                <h4>📎 需要上传的材料</h4>
                <p class="action-tip">{{ attachmentTip }}</p>
                <el-button type="primary" @click="showUploadDialog" icon="Upload">上传材料</el-button>
                
                <!-- 已上传的附件列表 -->
                <div v-if="currentStageAttachments.length > 0" class="uploaded-files">
                  <el-divider>已上传材料</el-divider>
                  <el-tag 
                    v-for="file in currentStageAttachments" 
                    :key="file.id"
                    closable
                    @close="deleteAttachment(file.id)"
                    style="margin-right: 10px; margin-bottom: 10px;"
                  >
                    {{ file.fileName }}
                  </el-tag>
                </div>
              </div>

              <!-- 需要支付 -->
              <div v-if="needPayment" class="action-section">
                <h4>💰 需要支付</h4>
                <p class="action-tip">{{ paymentTip }}</p>
                <el-button 
                  v-if="canInitiatePayment" 
                  type="danger" 
                  @click="showPaymentDialog" 
                  icon="CreditCard"
                >
                  发起支付
                </el-button>
                <el-alert 
                  v-else 
                  type="info" 
                  :closable="false"
                  show-icon
                >
                  {{ paymentWaitText }}
                </el-alert>
                
                <!-- 当前阶段的支付记录 -->
                <div v-if="currentStagePayments.length > 0" class="payment-records">
                  <el-divider>支付记录</el-divider>
                  <el-table :data="currentStagePayments" style="width: 100%">
                    <el-table-column prop="paymentTypeName" label="类型" width="100" />
                    <el-table-column prop="amount" label="金额" width="120">
                      <template #default="scope">
                        <span style="color: #F56C6C; font-weight: bold;">￥{{ scope.row.amount }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column prop="paymentMethodName" label="支付方式" width="100" />
                    <el-table-column label="状态" width="100">
                      <template #default="scope">
                        <el-tag :type="getPaymentStatusType(scope.row.paymentStatus)">
                          {{ scope.row.paymentStatusName }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column label="操作" width="120">
                      <template #default="scope">
                        <el-button 
                          v-if="scope.row.paymentStatus === 0 && canPay"
                          link 
                          type="primary" 
                          size="small" 
                          @click="handlePay(scope.row.id)"
                        >
                          立即支付
                        </el-button>
                        <span v-else-if="scope.row.paymentStatus === 1" style="color: #67C23A">已完成</span>
                        <span v-else style="color: #909399">{{ scope.row.paymentStatusName }}</span>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              </div>

              <!-- 推进到下一阶段按钮 -->
              <div class="stage-progress-btn">
                <el-button 
                  type="success" 
                  size="large"
                  @click="handleNextStage"
                  :disabled="!canProgress"
                >
                  {{ progressButtonText }}
                </el-button>
                <p class="progress-tip" v-if="progressTip">{{ progressTip }}</p>
              </div>
            </div>
          </el-card>

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
              type="danger" 
              @click="handleCancel"
              :disabled="appointment?.status === 3"
            >
              取消交易
            </el-button>
          </div>
        </div>
        <!-- 流程进度内容结束 -->
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
        <el-form-item label="选择图片" required>
          <el-upload
            class="upload-demo"
            :auto-upload="false"
            :on-change="handleFileChange"
            :limit="1"
            accept="image/*"
            list-type="picture-card"
            :file-list="fileList"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">支持 jpg、png、gif 格式图片</div>
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
        <el-button type="primary" @click="confirmUpload" :loading="uploading">确认上传</el-button>
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
import { Plus } from '@element-plus/icons-vue'
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

// 文件上传相关
const fileList = ref<any[]>([])
const currentFile = ref<File | null>(null)
const uploading = ref(false)

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

// 当前用户角色（0=租客/买家, 1=房东, 2=管理员）
const currentUserRole = computed(() => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (!userInfoStr) return 0
  const userInfo = JSON.parse(userInfoStr)
  return userInfo.role || 0
})

// 当前阶段信息
const currentStageInfo = computed(() => {
  if (!appointment.value?.currentStage) return null
  return stages.value.find(s => s.code === appointment.value.currentStage)
})

// 当前阶段需要的操作配置
const stageConfig = computed(() => {
  const stage = appointment.value?.currentStage
  const type = appointment.value?.transactionType // 0=租房, 1=买房
  const role = currentUserRole.value // 0=租客/买家, 1=房东, 2=管理员
  
  console.log('🔍 stageConfig计算:', { stage, type, role })
  
  // 获取当前阶段的附件和支付信息
  const stageAttachments = currentStageAttachments.value
  const stagePayments = currentStagePayments.value
  const hasAttachment = stageAttachments.length > 0
  const hasCompletedPayment = stagePayments.some((p: any) => p.paymentStatus === 1)
  
  console.log('🔍 当前阶段数据:', { 
    stageAttachments: stageAttachments.length, 
    stagePayments: stagePayments.length, 
    hasAttachment, 
    hasCompletedPayment 
  })
  
  const configs: any = {
    // 租房流程
    '0': {
      'viewing': { 
        needAttachment: true,
        attachmentTip: '可上传看房相关照片、笔记等材料（可选）',
        attachmentRequired: false,
        needPayment: false,
        canProgress: true,
        progressText: '确认看房'
      },
      'viewing_confirmed': { 
        needAttachment: true,
        attachmentTip: '可上传看房确认相关材料（可选）',
        attachmentRequired: false,
        needPayment: false,
        canProgress: true,
        progressText: '进入意向谈价'
      },
      'intention': { 
        needAttachment: true,
        attachmentTip: '建议上传身份证照片（正反面）',
        attachmentRequired: false,
        needPayment: false,
        canProgress: true,
        progressText: '进入合同签订'
      },
      'sign_contract': { 
        needAttachment: true,
        attachmentTip: role === 1 ? '请上传租赁合同（必须）' : '等待房东上传租赁合同',
        attachmentRequired: true, // 合同阶段必须上传附件
        needPayment: false,
        canProgress: role === 1 ? hasAttachment : true,
        progressText: role === 1 ? '合同已上传，进入备案' : '确认合同，进入备案',
        progressTip: role === 1 && !hasAttachment ? '请先上传租赁合同' : ''
      },
      'contract_record': { 
        needAttachment: true,
        attachmentTip: '可上传备案相关材料',
        attachmentRequired: false,
        needPayment: false,
        canProgress: true,
        progressText: '备案完成，进入交付'
      },
      'handover': { 
        needAttachment: true,
        attachmentTip: '可上传房屋交接照片、水电表读数等',
        attachmentRequired: false,
        needPayment: true, // 交付入住阶段需要支付
        paymentTip: '需支付首月租金、押金等费用',
        canInitiatePayment: role === 1, // 只有房东能发起支付
        canPay: role === 0, // 只有租客能支付
        paymentWaitText: role === 1 ? '等待租客支付' : '请等待房东发起支付',
        canProgress: hasCompletedPayment, // 必须完成支付才能推进
        progressText: '确认交付完成',
        progressTip: stagePayments.length === 0 ? '请先发起租金和押金支付' : 
                     !hasCompletedPayment ? '等待支付完成' : ''
      },
      'in_lease': { 
        needAttachment: true,
        attachmentTip: '可上传租期相关材料',
        attachmentRequired: false,
        needPayment: false,
        canProgress: true,
        progressText: '进入续租/退房'
      },
      'renewal': { 
        needAttachment: true,
        attachmentTip: '可上传续租合同或退房材料',
        attachmentRequired: false,
        needPayment: false,
        canProgress: true,
        progressText: '完成流程'
      },
      'completed': {
        needAttachment: true,
        attachmentTip: '可上传退房照片、交接单等',
        attachmentRequired: false,
        needPayment: false,
        canProgress: false,
        progressText: '流程已完成'
      }
    },
    // 买房流程
    '1': {
      'viewing': { 
        needAttachment: true,
        attachmentTip: '可上传看房相关照片、笔记等材料（可选）',
        attachmentRequired: false,
        needPayment: false,
        canProgress: true,
        progressText: '确认看房'
      },
      'viewing_confirmed': { 
        needAttachment: true,
        attachmentTip: '可上传看房确认相关材料（可选）',
        attachmentRequired: false,
        needPayment: false,
        canProgress: true,
        progressText: '进入意向谈价'
      },
      'intention': { 
        needAttachment: true,
        attachmentTip: '建议上传身份证、购房资质证明',
        attachmentRequired: false,
        needPayment: false,
        canProgress: true,
        progressText: '进入签订意向书'
      },
      'sign_intention': { 
        needAttachment: true,
        attachmentTip: '可上传意向书',
        attachmentRequired: false,
        needPayment: false,
        canProgress: true,
        progressText: '进入支付定金'
      },
      'pay_deposit': { 
        needAttachment: true,
        attachmentTip: '可上传支付凭证',
        attachmentRequired: false,
        needPayment: true,
        paymentTip: '需支付购房定金',
        canInitiatePayment: role === 1, // 只有房东能发起
        canPay: role === 0, // 只有买家能支付
        paymentWaitText: role === 1 ? '等待买家支付定金' : '请等待房东发起支付',
        canProgress: hasCompletedPayment,
        progressText: '定金已收，进入签订合同',
        progressTip: !hasCompletedPayment ? '等待支付完成' : ''
      },
      'sign_contract': { 
        needAttachment: true,
        attachmentTip: '请双方上传签字的购房合同（必须）',
        attachmentRequired: true, // 合同阶段必须上传
        needPayment: false,
        canProgress: hasAttachment,
        progressText: '合同已签署，进入首付',
        progressTip: !hasAttachment ? '请先上传购房合同' : ''
      },
      'pay_down_payment': { 
        needAttachment: true,
        attachmentTip: '可上传支付凭证、贷款审批材料',
        attachmentRequired: false,
        needPayment: true,
        paymentTip: '需支付首付款',
        canInitiatePayment: role === 1,
        canPay: role === 0,
        paymentWaitText: role === 1 ? '等待买家支付首付' : '请等待房东发起支付',
        canProgress: hasCompletedPayment,
        progressText: '首付已收，进入网签',
        progressTip: !hasCompletedPayment ? '等待支付完成' : ''
      },
      'online_sign': {
        needAttachment: true,
        attachmentTip: '可上传网签材料',
        attachmentRequired: false,
        needPayment: false,
        canProgress: true,
        progressText: '网签完成，进入贷款审批'
      },
      'loan_approval': {
        needAttachment: true,
        attachmentTip: '可上传贷款审批材料',
        attachmentRequired: false,
        needPayment: false,
        canProgress: true,
        progressText: '审批通过，进入过户'
      },
      'transfer': {
        needAttachment: true,
        attachmentTip: '可上传过户材料、产权证照片',
        attachmentRequired: false,
        needPayment: false,
        canProgress: true,
        progressText: '过户完成，进入交房'
      },
      'handover': {
        needAttachment: true,
        attachmentTip: '可上传房屋交接照片、钥匙交接单等',
        attachmentRequired: false,
        needPayment: true, // 交房阶段需要支付
        paymentTip: '需支付尾款及交房相关费用',
        canInitiatePayment: role === 1, // 只有房东能发起
        canPay: role === 0, // 只有买家能支付
        paymentWaitText: role === 1 ? '等待买家支付尾款' : '请等待房东发起支付',
        canProgress: hasCompletedPayment, // 必须完成支付才能推进
        progressText: '确认交房完成',
        progressTip: stagePayments.length === 0 ? '请先发起尾款支付' : 
                     !hasCompletedPayment ? '等待支付完成' : ''
      },
      'completed': {
        needAttachment: true,
        attachmentTip: '流程已完成',
        attachmentRequired: false,
        needPayment: false,
        canProgress: false,
        progressText: '流程已完成'
      }
    }
  }
  
  return configs[type]?.[stage] || { 
    needAttachment: true,  // 默认都可以上传附件
    attachmentTip: '可上传相关材料（可选）',
    attachmentRequired: false,
    needPayment: false,
    canProgress: true,
    progressText: '推进到下一阶段'
  }
})

// 是否需要上传附件（所有阶段都需要，但不一定必须）
const needUploadAttachment = computed(() => {
  const result = stageConfig.value.needAttachment
  console.log('🔍 needUploadAttachment:', result, '当前阶段:', appointment.value?.currentStage)
  return result
})
const attachmentTip = computed(() => stageConfig.value.attachmentTip || '')
const attachmentRequired = computed(() => stageConfig.value.attachmentRequired || false)

// 是否需要支付
const needPayment = computed(() => {
  const result = stageConfig.value.needPayment
  console.log('🔍 needPayment:', result, '当前阶段:', appointment.value?.currentStage, 'stageConfig:', stageConfig.value)
  return result
})
const paymentTip = computed(() => stageConfig.value.paymentTip || '')
const canInitiatePayment = computed(() => stageConfig.value.canInitiatePayment || false)
const canPay = computed(() => stageConfig.value.canPay || false)
const paymentWaitText = computed(() => stageConfig.value.paymentWaitText || '')

// 是否可以推进
const canProgress = computed(() => stageConfig.value.canProgress !== false && canOperate.value)
const progressButtonText = computed(() => stageConfig.value.progressText || '推进到下一阶段')
const progressTip = computed(() => stageConfig.value.progressTip || '')

// 阶段操作类型标签
const stageActionType = computed(() => {
  if (needUploadAttachment.value) return 'warning'
  if (needPayment.value) return 'danger'
  return 'success'
})

const stageActionText = computed(() => {
  if (needUploadAttachment.value) return '需要上传材料'
  if (needPayment.value) return '需要支付'
  return '进行中'
})

// 当前阶段的附件
const currentStageAttachments = computed(() => {
  return attachments.value.filter(
    (a: any) => a.stageCode === appointment.value?.currentStage
  )
})

// 当前阶段的支付记录
const currentStagePayments = computed(() => {
  return payments.value.filter(
    (p: any) => p.stageCode === appointment.value?.currentStage
  )
})

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

// 文件选择处理
const handleFileChange = (file: any) => {
  currentFile.value = file.raw
  fileList.value = [file]
}

// 上传附件
const showUploadDialog = () => {
  uploadForm.value = {
    attachmentType: 3,
    fileName: '',
    remark: ''
  }
  fileList.value = []
  currentFile.value = null
  uploadDialog.value = true
}

const confirmUpload = async () => {
  if (!currentFile.value) {
    ElMessage.warning('请选择要上传的图片')
    return
  }
  
  try {
    uploading.value = true
    const userInfoStr = localStorage.getItem('userInfo')
    if (!userInfoStr) {
      ElMessage.error('用户信息缺失，请重新登录')
      return
    }
    
    const userInfo = JSON.parse(userInfoStr)
    
    // 转换图片为Base64
    const reader = new FileReader()
    reader.readAsDataURL(currentFile.value)
    
    reader.onload = async () => {
      const base64Data = reader.result as string
      
      const res: any = await request.post('/transaction-process/attachment/upload', {
        appointmentId: appointmentId.value,
        stageCode: appointment.value.currentStage,
        fileName: currentFile.value!.name,
        attachmentType: uploadForm.value.attachmentType,
        uploaderId: userInfo.id,
        uploaderName: userInfo.realName || userInfo.username,
        remark: uploadForm.value.remark,
        fileSize: currentFile.value!.size,
        fileType: currentFile.value!.type,
        fileData: base64Data
      })
      
      if (res.success) {
        ElMessage.success('附件上传成功')
        uploadDialog.value = false
        uploading.value = false
        await loadAttachments()
      } else {
        ElMessage.error(res.message || '上传失败')
        uploading.value = false
      }
    }
    
    reader.onerror = () => {
      ElMessage.error('文件读取失败')
      uploading.value = false
    }
  } catch (error: any) {
    ElMessage.error(error.message || '上传失败')
    uploading.value = false
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

.current-stage-card {
  margin: 30px 0;
}

.stage-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: bold;
}

.stage-actions {
  padding: 20px 0;
}

.action-section {
  margin-bottom: 30px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.action-section h4 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #303133;
}

.action-tip {
  margin: 10px 0;
  color: #606266;
  font-size: 14px;
}

.uploaded-files {
  margin-top: 15px;
}

.payment-records {
  margin-top: 15px;
}

.stage-progress-btn {
  text-align: center;
  padding: 30px 0 10px;
}

.progress-tip {
  margin-top: 10px;
  color: #909399;
  font-size: 13px;
}

.attachments-section,
.payments-section {
  padding: 20px;
}

.upload-demo {
  display: flex;
  justify-content: center;
}

.upload-tip {
  margin-top: 10px;
  font-size: 12px;
  color: #909399;
  text-align: center;
}
</style>
