<template>
  <view class="process-page">
    <view class="header-section">
      <view class="status-badge" :class="statusClass">
        {{ statusText }}
      </view>
      <text class="current-stage">当前阶段：{{ currentStageName }}</text>
    </view>

    <view class="progress-section">
      <view class="progress-title">流程进度</view>
      <view class="stages-container">
        <view 
          v-for="(stage, index) in stages" 
          :key="stage.code"
          class="stage-item"
          :class="{ 
            'active': index === activeStepIndex,
            'completed': stage.status === 1,
            'failed': stage.status === 2
          }"
        >
          <view class="stage-dot">
            <text class="icon" v-if="stage.status === 1">✓</text>
            <text class="icon" v-else-if="stage.status === 2">✕</text>
          </view>
          <text class="stage-name">{{ stage.name }}</text>
          <view v-if="index < stages.length - 1" class="stage-line"></view>
        </view>
      </view>
    </view>

    <view class="action-section" v-if="appointment?.status === 0">
      <view class="action-card waiting-card">
        <text class="action-title">等待确认</text>
        <text class="action-tip">预约已提交，等待房东确认后即可继续推进交易流程</text>
      </view>
    </view>

    <view class="action-section" v-else-if="canOperate">
      <view class="action-card">
        <text class="action-title">待办事项</text>
        <text class="action-tip">{{ currentStageTip }}</text>
        <button class="btn-primary" @click="handleNextStage">
          推进到下一阶段
        </button>
      </view>
    </view>

    <view class="log-section">
      <view class="log-title">流程记录</view>
      <view 
        class="log-item" 
        v-for="log in logs" 
        :key="log.id"
      >
        <view class="log-time">{{ formatTime(log.createTime) }}</view>
        <view class="log-content">
          <text class="stage-name">{{ log.stageName }}</text>
          <text class="remark" v-if="log.remark">{{ log.remark }}</text>
          <text class="operator">操作人：{{ log.operatorName || '系统' }}</text>
        </view>
      </view>
    </view>

    <view class="bottom-actions" v-if="appointment?.status !== 3">
      <button class="btn-cancel" @click="handleCancel">取消交易</button>
    </view>

    <view class="popup-mask" v-if="showNextStagePopup" @click="closeNextStage">
      <view class="popup-content" @click.stop>
        <view class="popup-title">推进流程</view>
        <textarea 
          v-model="nextStageRemark" 
          placeholder="请输入备注信息（可选）"
          class="popup-textarea"
        />
        <view class="popup-buttons">
          <button class="btn-cancel-popup" @click="closeNextStage">取消</button>
          <button class="btn-confirm" @click="confirmNextStage">确认推进</button>
        </view>
      </view>
    </view>

    <view class="popup-mask" v-if="showCancelPopup" @click="closeCancelPopup">
      <view class="popup-content" @click.stop>
        <view class="popup-title">取消交易</view>
        <textarea 
          v-model="cancelReason" 
          placeholder="请输入取消原因"
          class="popup-textarea"
        />
        <view class="popup-buttons">
          <button class="btn-cancel-popup" @click="closeCancelPopup">取消</button>
          <button class="btn-danger" @click="confirmCancel">确认取消</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { crawlerProperties } from '@/mock/crawlerData.js'

export default {
  data() {
    return {
      appointmentId: 0,
      appointment: null,
      stages: [],
      logs: [],
      nextStageRemark: '',
      cancelReason: '',
      showNextStagePopup: false,
      showCancelPopup: false
    }
  },
  
  computed: {
    activeStepIndex() {
      return this.stages.findIndex(s => s.code === this.appointment?.currentStage)
    },
    
    currentStageName() {
      if (this.appointment?.status === 0) {
        return '待房东确认'
      }
      const stage = this.stages[this.activeStepIndex]
      return stage?.name || '未知阶段'
    },
    
    statusText() {
      const statusMap = {
        0: '待确认',
        1: '进行中',
        2: '已完成',
        3: '已取消'
      }
      return statusMap[this.appointment?.status] || '未知'
    },
    
    statusClass() {
      const classMap = {
        0: 'status-warning',
        1: 'status-success',
        2: 'status-info',
        3: 'status-danger'
      }
      return classMap[this.appointment?.status] || ''
    },
    
    canOperate() {
      return this.appointment?.status !== 0 &&
             this.appointment?.status !== 3 && 
             this.appointment?.currentStage !== 'completed'
    },
    
    currentStageTip() {
      if (this.appointment?.status === 0) {
        return '预约待房东确认，确认后可继续推进流程'
      }
      return '请确认当前阶段已完成，可推进到下一阶段'
    }
  },
  
  onLoad(options) {
    this.appointmentId = Number(options.id) || 1
    this.loadProcessDetail()
  },
  
  methods: {
    formatTime(time) {
      if (!time) return '-'
      const date = new Date(time)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
    },
    
    async loadProcessDetail() {
      uni.showLoading({ title: '加载中...' })
      
      try {
        const res = await uni.request({
          url: `/api/transaction-process/${this.appointmentId}`,
          method: 'GET'
        })
        
        if (res.data && res.data.success) {
          this.appointment = res.data.data.appointment
          this.stages = res.data.data.stages
          this.logs = res.data.data.logs
          uni.hideLoading()
          return
        }
      } catch (error) {
        console.log('API加载失败，使用示例数据')
      }
      
      this.loadMockData()
      uni.hideLoading()
    },
    
    loadMockData() {
      const prop = crawlerProperties[0]
      this.appointment = {
        id: this.appointmentId,
        propertyId: prop.id,
        propertyTitle: prop.title,
        status: 0,
        currentStage: 'booking',
        createTime: new Date(Date.now() - 86400000 * 3).toISOString()
      }
      
      this.stages = [
        { code: 'booking', name: '预约看房', status: 0 },
        { code: 'viewing', name: '实地看房', status: 0 },
        { code: 'negotiation', name: '洽谈价格', status: 0 },
        { code: 'contract', name: '签订合同', status: 0 },
        { code: 'payment', name: '支付租金', status: 0 },
        { code: 'completed', name: '交易完成', status: 0 }
      ]
      
      this.logs = [
        {
          id: 1,
          stageName: '预约看房',
          remark: '用户提交预约申请，等待房东确认',
          operatorName: '租客',
          createTime: new Date(Date.now() - 86400000 * 3).toISOString()
        }
      ]
    },
    
    handleNextStage() {
      this.nextStageRemark = ''
      this.showNextStagePopup = true
    },
    
    closeNextStage() {
      this.showNextStagePopup = false
    },
    
    async confirmNextStage() {
      if (this.appointment?.status === 0) {
        uni.showToast({
          title: '等待房东确认预约',
          icon: 'none'
        })
        this.showNextStagePopup = false
        return
      }
      
      const userId = uni.getStorageSync('userId') || '1'
      const userName = uni.getStorageSync('userName') || '用户'
      
      try {
        const res = await uni.request({
          url: '/api/transaction-process/next-stage',
          method: 'POST',
          data: {
            appointmentId: this.appointmentId,
            operatorId: userId,
            operatorName: userName,
            remark: this.nextStageRemark
          }
        })
        
        if (res.data && res.data.success) {
          uni.showToast({
            title: '推进成功',
            icon: 'success'
          })
          this.showNextStagePopup = false
          await this.loadProcessDetail()
          return
        }
      } catch (error) {
        console.log('API调用失败，使用本地模拟')
      }
      
      this.mockNextStage(userName)
      this.showNextStagePopup = false
    },
    
    mockNextStage(userName) {
      const stageCodes = ['booking', 'viewing', 'negotiation', 'contract', 'payment', 'completed']
      const stageNames = ['预约看房', '实地看房', '洽谈价格', '签订合同', '支付租金', '交易完成']
      const currentIndex = stageCodes.indexOf(this.appointment.currentStage)
      
      if (currentIndex >= 0 && currentIndex < stageCodes.length - 1) {
        this.stages[currentIndex].status = 1
        this.appointment.currentStage = stageCodes[currentIndex + 1]
        
        this.logs.unshift({
          id: Date.now(),
          stageName: stageNames[currentIndex + 1],
          remark: this.nextStageRemark || '阶段已完成，进入下一阶段',
          operatorName: userName,
          createTime: new Date().toISOString()
        })
        
        if (currentIndex + 1 === stageCodes.length - 1) {
          this.appointment.status = 2
        }
        
        uni.showToast({
          title: '推进成功',
          icon: 'success'
        })
      }
    },
    
    handleCancel() {
      this.cancelReason = ''
      this.showCancelPopup = true
    },
    
    closeCancelPopup() {
      this.showCancelPopup = false
    },
    
    async confirmCancel() {
      if (!this.cancelReason) {
        uni.showToast({
          title: '请输入取消原因',
          icon: 'none'
        })
        return
      }
      
      const userId = uni.getStorageSync('userId') || '1'
      const userName = uni.getStorageSync('userName') || '用户'
      
      try {
        const res = await uni.request({
          url: `/api/transaction-process/cancel/${this.appointmentId}`,
          method: 'POST',
          data: {
            operatorId: userId,
            operatorName: userName,
            reason: this.cancelReason
          }
        })
        
        if (res.data && res.data.success) {
          uni.showToast({
            title: '已取消',
            icon: 'success'
          })
          this.showCancelPopup = false
          await this.loadProcessDetail()
          return
        }
      } catch (error) {
        console.log('API调用失败，使用本地模拟')
      }
      
      this.mockCancel(userName)
      this.showCancelPopup = false
    },
    
    mockCancel(userName) {
      this.appointment.status = 3
      this.appointment.currentStage = 'cancelled'
      
      this.logs.unshift({
        id: Date.now(),
        stageName: '交易取消',
        remark: `取消原因：${this.cancelReason}`,
        operatorName: userName,
        createTime: new Date().toISOString()
      })
      
      uni.showToast({
        title: '已取消',
        icon: 'success'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.process-page {
  min-height: 100vh;
  background: $bg-color-page;
  padding-bottom: calc(140rpx + env(safe-area-inset-bottom));
}

.header-section {
  background: $primary-gradient;
  padding: 60rpx $spacing-base $spacing-xl;
  margin-bottom: 0;
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: -80rpx;
    right: -60rpx;
    width: 280rpx;
    height: 280rpx;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 50%;
  }
  
  &::after {
    content: '';
    position: absolute;
    bottom: -60rpx;
    left: -40rpx;
    width: 200rpx;
    height: 200rpx;
    background: rgba(255, 255, 255, 0.06);
    border-radius: 50%;
  }
  
  .status-badge {
    display: inline-block;
    padding: $spacing-xs $spacing-sm;
    border-radius: $radius-full;
    font-size: 24rpx;
    margin-bottom: $spacing-sm;
    font-weight: 500;
    position: relative;
    z-index: 2;
    
    &.status-warning {
      background: rgba(255, 255, 255, 0.2);
      color: $text-color-inverse;
    }
    
    &.status-success {
      background: rgba(255, 255, 255, 0.2);
      color: $text-color-inverse;
    }
    
    &.status-info {
      background: rgba(255, 255, 255, 0.2);
      color: $text-color-inverse;
    }
    
    &.status-danger {
      background: rgba(255, 255, 255, 0.2);
      color: $text-color-inverse;
    }
  }
  
  .current-stage {
    display: block;
    font-size: 40rpx;
    font-weight: 700;
    color: $text-color-inverse;
    position: relative;
    z-index: 2;
  }
}

.progress-section {
  background: $bg-color;
  padding: $spacing-base;
  margin: -30rpx $spacing-base $spacing-base;
  border-radius: $radius-xl;
  box-shadow: $shadow-sm;
  border: 1rpx solid $border-color-light;
  position: relative;
  z-index: 3;
  
  .progress-title {
    font-size: 32rpx;
    font-weight: 600;
    margin-bottom: $spacing-base;
    color: $text-color-primary;
    position: relative;
    padding-left: $spacing-sm;
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 6rpx;
      height: 28rpx;
      background: $primary-gradient;
      border-radius: $radius-sm;
    }
  }
  
  .stages-container {
    display: flex;
    flex-direction: column;
    gap: 0;
  }
  
  .stage-item {
    display: flex;
    align-items: flex-start;
    position: relative;
    padding-left: 64rpx;
    min-height: 80rpx;
    transition: all $transition-base;
    
    .stage-dot {
      position: absolute;
      left: 0;
      top: 0;
      width: 44rpx;
      height: 44rpx;
      border-radius: 50%;
      background: $border-color-light;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all $transition-base;
      
      .icon {
        color: $text-color-inverse;
        font-size: 24rpx;
        font-weight: bold;
      }
    }
    
    .stage-name {
      font-size: 28rpx;
      line-height: 44rpx;
      color: $text-color-secondary;
      font-weight: 500;
    }
    
    .stage-line {
      position: absolute;
      left: 21rpx;
      top: 44rpx;
      width: 2rpx;
      height: 40rpx;
      background: $border-color-light;
      transition: all $transition-base;
    }
    
    &.active .stage-dot {
      background: $primary-gradient;
      box-shadow: 0 0 0 8rpx rgba($primary-color, 0.15);
    }
    
    &.active .stage-name {
      color: $primary-color;
      font-weight: 600;
    }
    
    &.completed .stage-dot {
      background: $success-color;
    }
    
    &.completed .stage-name {
      color: $text-color-primary;
    }
    
    &.completed .stage-line {
      background: $success-color;
    }
    
    &.failed .stage-dot {
      background: $error-color;
    }
    
    &:last-child .stage-line {
      display: none;
    }
  }
}

.action-section {
  padding: 0 $spacing-base $spacing-base;
  
  .action-card {
    background: linear-gradient(135deg, rgba($secondary-color, 0.08) 0%, rgba($primary-color, 0.05) 100%);
    border-radius: $radius-xl;
    padding: $spacing-base;
    border-left: 6rpx solid $secondary-color;
    
    .action-title {
      display: block;
      font-size: 30rpx;
      font-weight: 600;
      margin-bottom: $spacing-xs;
      color: $text-color-primary;
    }
    
    .action-tip {
      display: block;
      color: $text-color-secondary;
      font-size: 26rpx;
      margin-bottom: $spacing-base;
      line-height: 1.6;
    }
    
    .btn-primary {
      width: 100%;
      background: $primary-gradient;
      color: $text-color-inverse;
      border: none;
      border-radius: $radius-full;
      height: 88rpx;
      line-height: 88rpx;
      font-size: $uni-font-size-lg;
      font-weight: 600;
      margin: 0;
      box-shadow: 0 4rpx 12rpx rgba($primary-color, 0.3);
      transition: all $transition-base;
      
      &:active {
        transform: scale(0.98);
        box-shadow: 0 2rpx 8rpx rgba($primary-color, 0.25);
      }
      
      &::after {
        border: none;
      }
    }
    
    &.waiting-card {
      background: linear-gradient(135deg, rgba($warning-color, 0.08) 0%, rgba($warning-color, 0.03) 100%);
      border-left-color: $warning-color;
    }
  }
}

.log-section {
  background: $bg-color;
  padding: $spacing-base;
  margin: 0 $spacing-base $spacing-base;
  border-radius: $radius-xl;
  box-shadow: $shadow-xs;
  border: 1rpx solid $border-color-light;
  
  .log-title {
    font-size: 32rpx;
    font-weight: 600;
    margin-bottom: $spacing-base;
    color: $text-color-primary;
    position: relative;
    padding-left: $spacing-sm;
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 6rpx;
      height: 28rpx;
      background: $primary-gradient;
      border-radius: $radius-sm;
    }
  }
  
  .log-item {
    position: relative;
    padding-left: 40rpx;
    padding-bottom: $spacing-base;
    
    &:before {
      content: '';
      position: absolute;
      left: 0;
      top: 8rpx;
      width: 16rpx;
      height: 16rpx;
      background: $primary-gradient;
      border-radius: 50%;
      box-shadow: 0 0 0 4rpx rgba($primary-color, 0.1);
    }
    
    &:after {
      content: '';
      position: absolute;
      left: 7rpx;
      top: 24rpx;
      width: 2rpx;
      height: calc(100% - 24rpx);
      background: $border-color-light;
    }
    
    &:last-child:after {
      display: none;
    }
    
    .log-time {
      color: $text-color-placeholder;
      font-size: 24rpx;
      margin-bottom: 8rpx;
    }
    
    .log-content {
      display: flex;
      flex-direction: column;
      gap: 8rpx;
      
      .stage-name {
        font-size: 28rpx;
        font-weight: 600;
        color: $text-color-primary;
      }
      
      .remark {
        font-size: 26rpx;
        color: $text-color-secondary;
        line-height: 1.6;
      }
      
      .operator {
        font-size: 22rpx;
        color: $text-color-placeholder;
      }
    }
  }
}

.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: $bg-color;
  padding: $spacing-sm $spacing-base;
  padding-bottom: calc(#{$spacing-sm} + env(safe-area-inset-bottom));
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.06);
  border-top: 1rpx solid $border-color-light;
  
  .btn-cancel {
    width: 100%;
    background: $bg-color;
    color: $error-color;
    border: 2rpx solid $error-color;
    border-radius: $radius-full;
    height: 88rpx;
    line-height: 84rpx;
    font-size: $uni-font-size-lg;
    font-weight: 500;
    margin: 0;
    transition: all $transition-base;
    
    &:active {
      background: $error-color;
      color: $text-color-inverse;
      transform: scale(0.98);
    }
    
    &::after {
      border: none;
    }
  }
}

.popup-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
  display: flex;
  align-items: flex-end;
  animation: fadeIn 0.25s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.popup-content {
  width: 100%;
  background: $bg-color;
  padding: $spacing-lg;
  padding-bottom: calc(#{$spacing-lg} + env(safe-area-inset-bottom));
  border-radius: $radius-xl $radius-xl 0 0;
  animation: slideUp 0.3s ease-out;
  box-shadow: 0 -8rpx 32rpx rgba(0, 0, 0, 0.12);
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}

.popup-content {
  .popup-title {
    font-size: 36rpx;
    font-weight: 600;
    margin-bottom: $spacing-base;
    text-align: center;
    color: $text-color-primary;
  }
  
  .popup-textarea {
    width: 100%;
    min-height: 200rpx;
    padding: $spacing-sm;
    border: 2rpx solid $border-color-light;
    border-radius: $radius-lg;
    font-size: $uni-font-size-base;
    margin-bottom: $spacing-base;
    box-sizing: border-box;
    background: $bg-color-page;
    transition: all $transition-base;
    
    &:focus {
      border-color: $primary-color;
      background: $bg-color;
      box-shadow: 0 0 0 6rpx rgba($primary-color, 0.08);
    }
  }
  
  .popup-buttons {
    display: flex;
    gap: $spacing-sm;
    
    button {
      flex: 1;
      height: 88rpx;
      line-height: 88rpx;
      border-radius: $radius-full;
      font-size: $uni-font-size-lg;
      font-weight: 500;
      border: none;
      margin: 0;
      transition: all $transition-base;
      
      &:active {
        transform: scale(0.98);
      }
      
      &::after {
        border: none;
      }
    }
    
    .btn-cancel-popup {
      background: $bg-color-light;
      color: $text-color-regular;
    }
    
    .btn-confirm {
      background: $primary-gradient;
      color: $text-color-inverse;
      box-shadow: 0 4rpx 12rpx rgba($primary-color, 0.3);
    }
    
    .btn-danger {
      background: $error-color;
      color: $text-color-inverse;
      box-shadow: 0 4rpx 12rpx rgba($error-color, 0.3);
    }
  }
}
</style>