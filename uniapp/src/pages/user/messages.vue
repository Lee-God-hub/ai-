<template>
	<view class="messages-page">
		<view class="page-header">
			<text class="page-title">我的对话</text>
		</view>
		
		<view class="conversation-list">
			<view 
				v-for="conversation in conversations" 
				:key="conversation.propertyId"
				class="conversation-item"
				@click="openConversation(conversation)"
			>
				<view class="conversation-info">
					<view class="property-title">
						<text class="property-icon">🏠</text>
						<text>{{ conversation.propertyTitle || '房源信息' }}</text>
					</view>
					<text class="last-message">{{ conversation.lastMessageContent || '暂无消息' }}</text>
				</view>
				<view class="conversation-meta">
					<text class="message-time">{{ formatTime(conversation.lastMessageTime) }}</text>
					<view class="badge-wrap">
						<view class="msg-badge" v-if="conversation.unreadCount > 0">{{ conversation.unreadCount }}</view>
						<view :class="['read-tag', conversation.isRead ? 'read' : 'unread']">
							{{ conversation.isRead ? '已读' : '未读' }}
						</view>
					</view>
				</view>
			</view>
			
			<view class="empty-state" v-if="conversations.length === 0">
				<text class="empty-icon">💬</text>
				<text class="empty-text">暂无对话，访问房源详情页可以向房东留言</text>
			</view>
		</view>
		
		<CustomTabbar :current="3" />
	</view>
</template>

<script>
	import { messageApi } from '@/api/user.js'
	
	export default {
		data() {
			return {
				conversations: []
			}
		},
		
		onShow() {
			this.loadConversations()
		},
		
		methods: {
			async loadConversations() {
				try {
					const res = await messageApi.getUserMessageList({
						pageNum: 1,
						pageSize: 100
					})
					
					if (res.code === 200 && res.data && res.data.records) {
						const grouped = new Map()
						
						res.data.records.forEach(msg => {
							const key = msg.propertyId
							if (!grouped.has(key)) {
								grouped.set(key, {
									propertyId: msg.propertyId,
									propertyTitle: msg.property?.title || '房源信息',
									lastMessageContent: msg.content,
									lastMessageTime: msg.createTime,
									isRead: msg.isRead === 1 ? 1 : 0,
									unreadCount: msg.isRead === 1 ? 0 : 1
								})
							} else {
								const existing = grouped.get(key)
								if (new Date(msg.createTime) > new Date(existing.lastMessageTime)) {
									existing.lastMessageContent = msg.content
									existing.lastMessageTime = msg.createTime
								}
								if (msg.isRead !== 1) {
									existing.unreadCount++
								}
							}
						})
						
						this.conversations = Array.from(grouped.values()).sort((a, b) =>
							new Date(b.lastMessageTime) - new Date(a.lastMessageTime)
						)
						uni.setStorageSync('conversations', JSON.stringify(this.conversations))
					} else {
						this.conversations = []
					}
				} catch (error) {
					console.error('加载对话列表失败:', error)
					const storedConversations = uni.getStorageSync('conversations')
					if (storedConversations) {
						try {
							this.conversations = JSON.parse(storedConversations)
						} catch (e) {
							this.conversations = []
						}
					} else {
						this.conversations = []
					}
				}
			},
			
			formatTime(time) {
				if (!time) return '-'
				const date = new Date(time)
				const now = new Date()
				const diff = now.getTime() - date.getTime()
				const days = Math.floor(diff / (1000 * 60 * 60 * 24))
				
				if (days === 0) {
					const hours = date.getHours().toString().padStart(2, '0')
					const minutes = date.getMinutes().toString().padStart(2, '0')
					return `${hours}:${minutes}`
				} else if (days === 1) {
					return '昨天'
				} else if (days < 7) {
					return `${days}天前`
				} else {
					const month = (date.getMonth() + 1).toString().padStart(2, '0')
					const day = date.getDate().toString().padStart(2, '0')
					return `${month}-${day}`
				}
			},
			
			openConversation(conversation) {
				const priceParam = conversation.propertyPrice ? `&propertyPrice=${conversation.propertyPrice}` : ''
				uni.navigateTo({
					url: `/pages/user/chat?propertyId=${conversation.propertyId}&propertyTitle=${encodeURIComponent(conversation.propertyTitle)}${priceParam}`
				})
			}
		}
	}
</script>

<style lang="scss" scoped>
	.messages-page {
		min-height: 100vh;
		background: $bg-color-page;
		padding-bottom: calc(120rpx + env(safe-area-inset-bottom));
	}
	
	.page-header {
		background: $primary-gradient;
		padding: 60rpx $spacing-base $spacing-lg;
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
		
		&::after {
			content: '';
			position: absolute;
			bottom: -40rpx;
			left: -30rpx;
			width: 140rpx;
			height: 140rpx;
			background: rgba(255, 255, 255, 0.06);
			border-radius: 50%;
		}
	}
	
	.page-title {
		font-size: 40rpx;
		font-weight: 700;
		color: $text-color-inverse;
		position: relative;
		z-index: 2;
	}
	
	.conversation-list {
		padding: $spacing-base;
		margin-top: -20rpx;
		position: relative;
		z-index: 3;
	}
	
	.conversation-item {
		display: flex;
		justify-content: space-between;
		align-items: center;
		background: $bg-color;
		padding: $spacing-base;
		margin-bottom: $spacing-sm;
		border-radius: $radius-xl;
		box-shadow: $shadow-sm;
		border: 1rpx solid $border-color-light;
		transition: all $transition-base;
		
		&:active {
			transform: translateY(2rpx);
			box-shadow: $shadow-xs;
		}
	}
	
	.conversation-info {
		flex: 1;
		min-width: 0;
	}
	
	.property-title {
		display: flex;
		align-items: center;
		gap: $spacing-xs;
		font-size: 30rpx;
		font-weight: 600;
		color: $text-color-primary;
		margin-bottom: $spacing-xs;
	}
	
	.property-icon {
		font-size: 28rpx;
	}
	
	.last-message {
		font-size: 26rpx;
		color: $text-color-secondary;
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
		display: block;
	}
	
	.conversation-meta {
		display: flex;
		flex-direction: column;
		align-items: flex-end;
		gap: $spacing-xs;
		margin-left: $spacing-sm;
		flex-shrink: 0;
	}
	
	.message-time {
		font-size: 22rpx;
		color: $text-color-placeholder;
	}
	
	.badge-wrap {
		display: flex;
		flex-direction: column;
		align-items: flex-end;
		gap: 6rpx;
	}
	
	.msg-badge {
		min-width: 36rpx;
		height: 36rpx;
		border-radius: 50%;
		background: $secondary-gradient;
		color: $text-color-inverse;
		font-size: 22rpx;
		font-weight: 500;
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 0 10rpx;
		box-shadow: 0 2rpx 8rpx rgba($secondary-color, 0.35);
	}
	
	.read-tag {
		font-size: 20rpx;
		padding: 6rpx 14rpx;
		border-radius: $radius-full;
		font-weight: 500;
	}
	
	.read-tag.read {
		background: rgba($success-color, 0.1);
		color: $success-color;
	}
	
	.read-tag.unread {
		background: rgba($warning-color, 0.1);
		color: $warning-color;
	}
	
	.empty-state {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 120rpx $spacing-lg;
		background: $bg-color;
		border-radius: $radius-xl;
		box-shadow: $shadow-sm;
		border: 1rpx solid $border-color-light;
	}
	
	.empty-icon {
		font-size: 100rpx;
		margin-bottom: $spacing-base;
		opacity: 0.6;
	}
	
	.empty-text {
		font-size: $uni-font-size-base;
		color: $text-color-secondary;
		text-align: center;
		line-height: 1.6;
	}
</style>