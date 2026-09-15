<template>
	<view class="landlord-conversation-page">
		<!-- 页面头部 -->
		<view class="header-section">
			<view class="header-content">
				<text class="back-icon" @click="goBack">←</text>
				<view class="header-info">
					<text class="header-title">对话详情</text>
					<text class="header-subtitle" v-if="tenantName">租客：{{ tenantName }}</text>
				</view>
			</view>
			<view class="property-info" v-if="propertyTitle">
				<text class="property-label">房源：</text>
				<text class="property-title-text">{{ propertyTitle }}</text>
			</view>
		</view>
		
		<!-- 消息列表 -->
		<scroll-view 
			scroll-y 
			class="message-scroll"
			:scroll-top="scrollTop"
			:refresher-enabled="true"
			:refresher-triggered="refreshing"
			@refresherrefresh="onRefresh"
		>
			<view class="message-container">
				<view v-if="loading && messages.length === 0" class="loading-state">
					<text class="loading-text">加载中...</text>
				</view>
				
				<view v-else-if="messages.length === 0" class="empty-state">
					<text class="empty-text">暂无对话记录，发送第一条消息吧</text>
				</view>
				
				<view v-else class="message-list">
					<view 
						v-for="(msg, index) in messages" 
						:key="msg.id || index"
						class="message-item"
						:class="isSelf(msg) ? 'message-self' : 'message-other'"
					>
						<view class="message-bubble">
							<view class="message-header">
								<text class="sender-name">{{ isSelf(msg) ? '我' : '租客' }}</text>
								<text class="message-time">{{ formatTime(msg.createTime) }}</text>
							</view>
							<text class="message-content">{{ msg.content }}</text>
						</view>
					</view>
				</view>
			</view>
		</scroll-view>
		
		<!-- 输入区域 -->
		<view class="input-section">
			<view class="input-box">
				<textarea 
					class="message-input"
					v-model="newMessage"
					placeholder="输入回复内容..."
					placeholder-class="input-placeholder"
					:maxlength="500"
					:auto-height="true"
					confirm-type="send"
					@confirm="sendMessage"
				/>
				<text class="char-count">{{ newMessage.length }}/500</text>
			</view>
			<button 
				class="send-btn"
				:class="{ 'send-disabled': !newMessage.trim() }"
				@click="sendMessage"
				:disabled="!newMessage.trim() || sending"
			>
				发送
			</button>
		</view>
		
		<!-- 底部安全区占位 -->
		<view class="safe-area-placeholder"></view>
	</view>
</template>

<script>
	import { messageApi } from '@/api/user.js'
	
	export default {
		data() {
			return {
				propertyId: null,
				userId: null,
				propertyTitle: '',
				tenantName: '',
				messages: [],
				newMessage: '',
				loading: false,
				refreshing: false,
				sending: false,
				scrollTop: 0
			}
		},
		
		onLoad(options) {
			this.propertyId = Number(options.propertyId)
			this.userId = Number(options.userId)
			
			if (!this.propertyId || !this.userId) {
				uni.showToast({ title: '参数错误', icon: 'none' })
				setTimeout(() => {
					uni.navigateBack()
				}, 1500)
				return
			}
			
			this.loadMessages()
		},
		
		onShow() {
			if (this.propertyId && this.userId) {
				this.loadMessages(false)
			}
		},
		
		methods: {
			// 加载对话消息
			async loadMessages(showLoading = true) {
				if (showLoading) {
					this.loading = true
				}
				
				try {
					const result = await messageApi.getLandlordConversationMessages(this.propertyId, this.userId)
					if (result.code === 200 && result.data) {
						this.messages = result.data
						
						if (this.messages.length > 0) {
							const firstMsg = this.messages[0]
							this.propertyTitle = firstMsg.property?.title || '房源对话'
							this.tenantName = firstMsg.user?.realName || firstMsg.user?.username || '租客'
						}
						
						this.scrollToBottom()
					} else {
						uni.showToast({ title: result.message || '加载失败', icon: 'none' })
					}
				} catch (error) {
					console.error('加载消息失败:', error)
					uni.showToast({ title: '加载消息失败', icon: 'none' })
				} finally {
					this.loading = false
					this.refreshing = false
				}
			},
			
			// 下拉刷新
			onRefresh() {
				this.refreshing = true
				this.loadMessages(false)
			},
			
			// 发送消息
			async sendMessage() {
				const content = this.newMessage.trim()
				if (!content || this.sending) return
				
				this.sending = true
				
				try {
					const result = await messageApi.landlordSendConversationMessage({
						propertyId: this.propertyId,
						userId: this.userId,
						content: content
					})
					
					if (result.code === 200) {
						this.newMessage = ''
						await this.loadMessages(false)
						uni.showToast({ title: '发送成功', icon: 'success' })
					} else {
						uni.showToast({ title: result.message || '发送失败', icon: 'none' })
					}
				} catch (error) {
					console.error('发送消息失败:', error)
					uni.showToast({ title: '发送消息失败', icon: 'none' })
				} finally {
					this.sending = false
				}
			},
			
			// 判断是否为自己发送的消息
			isSelf(msg) {
				return msg.senderRole === 1
			},
			
			// 滚动到底部
			scrollToBottom() {
				setTimeout(() => {
					const query = uni.createSelectorQuery().in(this)
					query.select('.message-container').boundingClientRect()
					query.select('.message-scroll').boundingClientRect()
					query.exec((res) => {
						if (res && res[0] && res[1]) {
							const containerHeight = res[0].height
							const scrollHeight = res[1].height
							if (containerHeight > scrollHeight) {
								this.scrollTop = containerHeight - scrollHeight + 100
							}
						}
					})
				}, 100)
			},
			
			// 格式化时间
			formatTime(time) {
				if (!time) return '-'
				const d = new Date(time)
				if (isNaN(d.getTime())) return time
				return d.toLocaleString('zh-CN', {
					month: '2-digit',
					day: '2-digit',
					hour: '2-digit',
					minute: '2-digit'
				})
			},
			
			// 返回上一页
			goBack() {
				uni.navigateBack()
			}
		}
	}
</script>

<style lang="scss" scoped>
	.landlord-conversation-page {
		min-height: 100vh;
		background: $bg-color-page;
		display: flex;
		flex-direction: column;
	}
	
	.header-section {
		background: $primary-gradient;
		padding: 40rpx $spacing-base;
		padding-top: calc(40rpx + var(--status-bar-height));
		position: relative;
		z-index: 10;
	}
	
	.header-content {
		display: flex;
		align-items: center;
		gap: $spacing-base;
	}
	
	.back-icon {
		font-size: 48rpx;
		color: $text-color-inverse;
		padding: 0 12rpx;
		line-height: 1;
	}
	
	.header-info {
		flex: 1;
		display: flex;
		flex-direction: column;
	}
	
	.header-title {
		font-size: 36rpx;
		font-weight: 700;
		color: $text-color-inverse;
	}
	
	.header-subtitle {
		font-size: 24rpx;
		color: rgba(255, 255, 255, 0.85);
		margin-top: 4rpx;
	}
	
	.property-info {
		display: flex;
		align-items: center;
		margin-top: $spacing-sm;
		padding: 12rpx $spacing-base;
		background: rgba(255, 255, 255, 0.15);
		border-radius: $radius-lg;
	}
	
	.property-label {
		font-size: 24rpx;
		color: rgba(255, 255, 255, 0.8);
		flex-shrink: 0;
	}
	
	.property-title-text {
		font-size: 24rpx;
		color: $text-color-inverse;
		font-weight: 500;
		flex: 1;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
		margin-left: 8rpx;
	}
	
	.message-scroll {
		flex: 1;
		height: 0;
		padding: $spacing-base;
	}
	
	.message-container {
		min-height: 100%;
	}
	
	.loading-state {
		display: flex;
		justify-content: center;
		align-items: center;
		padding: 80rpx 0;
	}
	
	.loading-text {
		font-size: $uni-font-size-base;
		color: $text-color-secondary;
	}
	
	.empty-state {
		display: flex;
		justify-content: center;
		align-items: center;
		padding: 120rpx $spacing-base;
	}
	
	.empty-text {
		font-size: $uni-font-size-base;
		color: $text-color-secondary;
	}
	
	.message-list {
		display: flex;
		flex-direction: column;
		gap: $spacing-base;
	}
	
	.message-item {
		display: flex;
		margin-bottom: $spacing-sm;
	}
	
	.message-self {
		justify-content: flex-end;
		
		.message-bubble {
			background: $primary-gradient;
			border-bottom-right-radius: 8rpx;
		}
		
		.sender-name,
		.message-time {
			color: rgba(255, 255, 255, 0.85);
		}
		
		.message-content {
			color: $text-color-inverse;
		}
	}
	
	.message-other {
		justify-content: flex-start;
		
		.message-bubble {
			background: $bg-color;
			border-bottom-left-radius: 8rpx;
			border: 1rpx solid $border-color-light;
		}
	}
	
	.message-bubble {
		max-width: 75%;
		padding: $spacing-sm $spacing-base;
		border-radius: $radius-xl;
		box-shadow: $shadow-sm;
	}
	
	.message-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 8rpx;
		gap: $spacing-sm;
	}
	
	.sender-name {
		font-size: 22rpx;
		font-weight: 600;
	}
	
	.message-time {
		font-size: 20rpx;
		color: $text-color-secondary;
		flex-shrink: 0;
	}
	
	.message-content {
		font-size: $uni-font-size-base;
		line-height: 1.5;
		word-break: break-all;
	}
	
	.input-section {
		background: $bg-color;
		padding: $spacing-base;
		border-top: 1rpx solid $border-color-light;
		display: flex;
		gap: $spacing-sm;
		align-items: flex-end;
	}
	
	.input-box {
		flex: 1;
		background: $bg-color-light;
		border-radius: $radius-lg;
		padding: 16rpx $spacing-base;
		border: 1rpx solid $border-color-light;
		position: relative;
	}
	
	.message-input {
		width: 100%;
		min-height: 60rpx;
		max-height: 200rpx;
		font-size: $uni-font-size-base;
		color: $text-color-primary;
		line-height: 1.5;
	}
	
	.input-placeholder {
		color: $text-color-placeholder;
	}
	
	.char-count {
		font-size: 20rpx;
		color: $text-color-secondary;
		text-align: right;
		margin-top: 4rpx;
	}
	
	.send-btn {
		width: 120rpx;
		padding: 20rpx 0;
		background: $primary-gradient;
		color: $text-color-inverse;
		font-size: $uni-font-size-base;
		font-weight: 600;
		border-radius: $radius-lg;
		border: none;
		box-shadow: 0 6rpx 20rpx rgba($primary-color, 0.35);
		transition: all $transition-base;
		display: flex;
		align-items: center;
		justify-content: center;
		line-height: 1.2;
		
		&:active {
			transform: scale(0.97);
		}
		
		&.send-disabled {
			background: $border-color;
			color: $text-color-placeholder;
			box-shadow: none;
		}
		
		&::after {
			border: none;
		}
	}
	
	.safe-area-placeholder {
		height: env(safe-area-inset-bottom);
	}
</style>
