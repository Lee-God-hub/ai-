<template>
	<view class="landlord-messages-page">
		<!-- 页面头部 -->
		<view class="header-section">
			<view class="header-title">
				<text class="title-text">我的消息</text>
			</view>
			<view class="header-stats" v-if="messages.length > 0">
				<view class="stat-item stat-warning">
					<text class="stat-value">{{ unreadCount }}</text>
					<text class="stat-label">未读</text>
				</view>
				<view class="stat-item stat-info">
					<text class="stat-value">{{ messages.length }}</text>
					<text class="stat-label">共 {{ messages.length }} 条</text>
				</view>
			</view>
		</view>
		
		<!-- 消息列表 -->
		<scroll-view 
			scroll-y 
			class="list-scroll"
			:refresher-enabled="true"
			:refresher-triggered="refreshing"
			@refresherrefresh="onRefresh"
		>
			<view class="list-container">
				<view v-if="loading && messages.length === 0" class="loading-state">
					<text class="loading-text">加载中...</text>
				</view>
				
				<view v-else-if="messages.length === 0" class="empty-state">
					<view class="empty-icon">💬</view>
					<text class="empty-title">暂无消息</text>
					<text class="empty-desc">暂时没有租客留言</text>
				</view>
				
				<view v-else class="message-list">
					<view 
						v-for="item in messages" 
						:key="item.id"
						class="message-card"
						:class="{ 'unread': item.isRead !== 1 }"
					>
						<!-- 卡片头部 -->
						<view class="card-header">
							<view class="user-info">
								<view class="user-avatar">
									<text>{{ getAvatarText(item.user?.username, item.user?.realName) }}</text>
								</view>
								<view class="user-detail">
									<text class="user-name">{{ item.user?.realName || item.user?.username || '未知用户' }}</text>
									<text class="message-time">{{ formatDateTime(item.createTime) }}</text>
								</view>
							</view>
							<view class="status-badge" :class="item.isRead === 1 ? 'read' : 'unread'">
								<text>{{ item.isRead === 1 ? '已读' : '未读' }}</text>
							</view>
						</view>
						
						<!-- 房源信息 -->
						<view class="property-section" @click="goToPropertyDetail(item.propertyId)">
							<text class="property-label">房源：</text>
							<text class="property-title">{{ item.property?.title || '未知房源' }}</text>
						</view>
						
						<!-- 消息内容 -->
						<view class="content-section">
							<text class="content-text" :class="{ 'unread-content': item.isRead !== 1 }">{{ item.content }}</text>
						</view>
						
						<!-- 操作按钮 -->
						<view class="card-actions">
							<button 
								class="action-btn btn-primary"
								@click="handleReply(item)"
							>
								回复
							</button>
							
							<button 
								v-if="item.isRead !== 1"
								class="action-btn btn-success"
								@click="handleRead(item)"
							>
								已读
							</button>
							
							<button 
								class="action-btn btn-danger"
								@click="handleDelete(item)"
							>
								删除
							</button>
						</view>
					</view>
				</view>
			</view>
		</scroll-view>
	</view>
</template>

<script>
	import { messageApi } from '@/api/user.js'
	
	export default {
		data() {
			return {
				messages: [],
				loading: false,
				refreshing: false
			}
		},
		
		computed: {
			unreadCount() {
				return this.messages.filter(item => item.isRead !== 1).length
			}
		},
		
		onShow() {
			this.loadMessages()
		},
		
		methods: {
			// 加载消息列表
			async loadMessages(showLoading = true) {
				if (showLoading) {
					this.loading = true
				}
				
				try {
					const result = await messageApi.getLandlordMessageList({
						pageNum: 1,
						pageSize: 100
					})
					if (result.code === 200 && result.data) {
						this.messages = result.data.records || result.data || []
					} else {
						uni.showToast({ title: result.message || '加载失败', icon: 'none' })
					}
				} catch (error) {
					console.error('加载消息列表失败:', error)
					uni.showToast({ title: '加载消息列表失败', icon: 'none' })
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
			
			// 标记已读
			async handleRead(item) {
				try {
					const result = await messageApi.markMessageAsRead(item.id)
					if (result.code === 200) {
						uni.showToast({ title: '标记成功', icon: 'success' })
						this.loadMessages()
					} else {
						uni.showToast({ title: result.message || '操作失败', icon: 'none' })
					}
				} catch (error) {
					console.error('标记已读失败:', error)
					uni.showToast({ title: '标记已读失败', icon: 'none' })
				}
			},
			
			// 回复 - 进入对话页
			handleReply(item) {
				if (!item.propertyId || !item.userId) {
					uni.showToast({ title: '消息信息不完整，无法进入对话', icon: 'none' })
					return
				}
				uni.navigateTo({
					url: `/pages/landlord/conversation?propertyId=${item.propertyId}&userId=${item.userId}`
				})
			},
			
			// 删除消息
			handleDelete(item) {
				uni.showModal({
					title: '删除消息',
					content: '确定要删除这条留言吗？',
					confirmColor: '#ff4d4f',
					success: async (res) => {
						if (res.confirm) {
							try {
								const result = await messageApi.deleteMessage(item.id)
								if (result.code === 200) {
									uni.showToast({ title: '删除成功', icon: 'success' })
									this.loadMessages()
								} else {
									uni.showToast({ title: result.message || '删除失败', icon: 'none' })
								}
							} catch (error) {
								console.error('删除消息失败:', error)
								uni.showToast({ title: '删除消息失败', icon: 'none' })
							}
						}
					}
				})
			},
			
			// 查看房源详情
			goToPropertyDetail(propertyId) {
				uni.navigateTo({
					url: `/pages/property/detail?id=${propertyId}`
				})
			},
			
			// 格式化日期时间
			formatDateTime(date) {
				if (!date) return '-'
				const d = new Date(date)
				if (isNaN(d.getTime())) return date
				return d.toLocaleString('zh-CN', {
					year: 'numeric',
					month: '2-digit',
					day: '2-digit',
					hour: '2-digit',
					minute: '2-digit'
				})
			},
			
			// 获取头像文字
			getAvatarText(username, realName) {
				const name = realName || username || 'U'
				return name.charAt(0).toUpperCase()
			}
		}
	}
</script>

<style lang="scss" scoped>
	.landlord-messages-page {
		min-height: 100vh;
		background: $bg-color-page;
		display: flex;
		flex-direction: column;
	}
	
	.header-section {
		background: $primary-gradient;
		padding: 40rpx $spacing-base 60rpx;
		position: relative;
		overflow: hidden;
		
		&::before {
			content: '';
			position: absolute;
			top: -60rpx;
			right: -40rpx;
			width: 200rpx;
			height: 200rpx;
			background: rgba(255, 255, 255, 0.1);
			border-radius: 50%;
		}
	}
	
	.header-title {
		display: flex;
		align-items: baseline;
		gap: $spacing-sm;
		position: relative;
		z-index: 1;
		margin-bottom: $spacing-base;
	}
	
	.title-text {
		font-size: 40rpx;
		font-weight: 700;
		color: $text-color-inverse;
	}
	
	.header-stats {
		display: flex;
		gap: $spacing-base;
		position: relative;
		z-index: 1;
	}
	
	.stat-item {
		background: rgba(255, 255, 255, 0.18);
		backdrop-filter: blur(10rpx);
		padding: 16rpx 28rpx;
		border-radius: $radius-xl;
		display: flex;
		flex-direction: column;
		min-width: 120rpx;
	}
	
	.stat-value {
		font-size: 36rpx;
		font-weight: 700;
		color: $text-color-inverse;
	}
	
	.stat-label {
		font-size: 22rpx;
		color: rgba(255, 255, 255, 0.85);
		margin-top: 4rpx;
	}
	
	.list-scroll {
		flex: 1;
		height: 0;
		padding: $spacing-base;
		margin-top: -30rpx;
		border-radius: $radius-xl $radius-xl 0 0;
		background: $bg-color-page;
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
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 120rpx $spacing-base;
	}
	
	.empty-icon {
		font-size: 120rpx;
		margin-bottom: $spacing-base;
	}
	
	.empty-title {
		font-size: $uni-font-size-lg;
		color: $text-color-primary;
		font-weight: 600;
		margin-bottom: $spacing-sm;
	}
	
	.empty-desc {
		font-size: $uni-font-size-base;
		color: $text-color-secondary;
	}
	
	.message-list {
		display: flex;
		flex-direction: column;
		gap: $spacing-base;
	}
	
	.message-card {
		background: $bg-color;
		border-radius: $radius-xl;
		padding: $spacing-base;
		box-shadow: $shadow-sm;
		border: 1rpx solid $border-color-light;
		transition: all $transition-base;
		
		&:active {
			transform: scale(0.99);
			box-shadow: $shadow-base;
		}
		
		&.unread {
			border-color: rgba($warning-color, 0.4);
			box-shadow: 0 4rpx 16rpx rgba($warning-color, 0.08);
		}
	}
	
	.card-header {
		display: flex;
		justify-content: space-between;
		align-items: flex-start;
		margin-bottom: $spacing-sm;
	}
	
	.user-info {
		display: flex;
		align-items: center;
		gap: $spacing-sm;
		flex: 1;
		min-width: 0;
	}
	
	.user-avatar {
		width: 72rpx;
		height: 72rpx;
		border-radius: 50%;
		background: $primary-gradient;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
		
		text {
			font-size: 30rpx;
			font-weight: 600;
			color: $text-color-inverse;
		}
	}
	
	.user-detail {
		flex: 1;
		min-width: 0;
		display: flex;
		flex-direction: column;
	}
	
	.user-name {
		font-size: $uni-font-size-base;
		color: $text-color-primary;
		font-weight: 600;
	}
	
	.message-time {
		font-size: 22rpx;
		color: $text-color-secondary;
		margin-top: 4rpx;
	}
	
	.status-badge {
		flex-shrink: 0;
		padding: 6rpx 16rpx;
		border-radius: $radius-full;
		font-size: 22rpx;
		font-weight: 500;
		margin-left: $spacing-sm;
		
		&.read {
			background: $success-color-light;
			color: $success-color;
		}
		
		&.unread {
			background: $warning-color-light;
			color: $warning-color;
		}
	}
	
	.property-section {
		display: flex;
		align-items: center;
		padding: $spacing-sm $spacing-base;
		background: $bg-color-light;
		border-radius: $radius-lg;
		margin-bottom: $spacing-sm;
	}
	
	.property-label {
		font-size: 24rpx;
		color: $text-color-secondary;
		flex-shrink: 0;
	}
	
	.property-title {
		font-size: 24rpx;
		color: $primary-color;
		font-weight: 500;
		flex: 1;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
		margin-left: 8rpx;
	}
	
	.content-section {
		padding: $spacing-sm 0;
		margin-bottom: $spacing-sm;
	}
	
	.content-text {
		font-size: $uni-font-size-base;
		color: $text-color-secondary;
		line-height: 1.5;
		word-break: break-all;
		
		&.unread-content {
			color: $text-color-primary;
			font-weight: 500;
		}
	}
	
	.card-actions {
		display: flex;
		gap: $spacing-sm;
	}
	
	.action-btn {
		flex: 1;
		font-size: 24rpx;
		padding: 16rpx 0;
		border-radius: $radius-full;
		font-weight: 500;
		transition: all $transition-base;
		display: flex;
		align-items: center;
		justify-content: center;
		line-height: 1.2;
		
		&:active {
			transform: scale(0.97);
		}
		
		&::after {
			border: none;
		}
	}
	
	.btn-primary {
		background: $primary-gradient;
		color: $text-color-inverse;
		border: none;
		box-shadow: 0 6rpx 20rpx rgba($primary-color, 0.35);
		
		&:active {
			box-shadow: 0 4rpx 12rpx rgba($primary-color, 0.25);
		}
	}
	
	.btn-success {
		background: $success-color-light;
		color: $success-color;
		border: 1rpx solid rgba($success-color, 0.3);
		
		&:active {
			background: rgba($success-color, 0.2);
		}
	}
	
	.btn-danger {
		background: $error-color-light;
		color: $error-color;
		border: 1rpx solid rgba($error-color, 0.3);
		
		&:active {
			background: rgba($error-color, 0.2);
		}
	}
</style>
