<template>
	<view class="chat-page">
		<!-- 房源信息卡片 -->
		<view class="property-card" @click="goToProperty" v-if="propertyInfo">
			<image 
				:src="getPropertyImage()" 
				class="property-image" 
				mode="aspectFill"
				@error="onImageError"
			></image>
			<view class="property-info">
				<text class="property-title">{{ propertyInfo.title }}</text>
				<view class="property-meta">
					<text class="property-price">¥{{ propertyInfo.price }}/月</text>
					<text class="property-rooms" v-if="propertyInfo.rooms || propertyInfo.bedrooms">{{ propertyInfo.rooms || propertyInfo.bedrooms }}室{{ propertyInfo.halls || 1 }}厅</text>
					<text class="property-area" v-if="propertyInfo.area">{{ propertyInfo.area }}㎡</text>
				</view>
			</view>
			<text class="arrow-icon">›</text>
		</view>
		
		<!-- 消息列表 -->
		<view class="message-list-wrapper">
			<scroll-view 
				class="message-list" 
				scroll-y="true" 
				:scroll-into-view="scrollToView"
				:scroll-with-animation="true"
			>
				<view class="message-container">
					<view 
						v-for="(message, index) in messageList" 
						:key="message.id"
						:id="'msg-' + message.id"
						:class="['message-item', message.isSelf ? 'message-self' : 'message-other']"
					>
						<!-- 时间分隔 -->
						<view class="time-separator" v-if="shouldShowTime(index)">
							<text class="time-text">{{ formatMessageTime(message.sendTime) }}</text>
						</view>
						
						<!-- 消息气泡 -->
						<view class="message-bubble-wrap">
							<view class="avatar" v-if="!message.isSelf">
								<text class="avatar-text">{{ otherInitial }}</text>
							</view>
							<view class="message-bubble">
								<text class="message-content">{{ message.content }}</text>
							</view>
							<view class="avatar self-avatar" v-if="message.isSelf">
								<text class="avatar-text">我</text>
							</view>
						</view>
					</view>
				</view>
			</scroll-view>
		</view>
		
		<!-- 快捷回复 -->
		<view class="quick-replies" v-if="quickReplies.length > 0 && !inputFocused">
			<scroll-view class="quick-reply-scroll" scroll-x="true" show-scrollbar="false">
				<view class="quick-reply-list">
					<view 
						class="quick-reply-item" 
						v-for="(reply, index) in quickReplies" 
						:key="index"
						@click="useQuickReply(reply)"
					>
						<text>{{ reply }}</text>
					</view>
				</view>
			</scroll-view>
		</view>
		
		<!-- 输入区域 -->
		<view class="input-section" :style="{ paddingBottom: `calc(20rpx + ${keyboardHeight}px + env(safe-area-inset-bottom))` }">
			<view class="input-container">
				<textarea 
					class="message-input" 
					v-model="inputContent"
					placeholder="输入消息..."
					:adjust-position="false"
					:auto-height="true"
					:maxlength="500"
					:cursor-spacing="0"
					@focus="onInputFocus"
					@blur="onInputBlur"
				></textarea>
				<button 
					class="send-btn" 
					@click="sendMessage"
					:disabled="!inputContent.trim()"
				>
					发送
				</button>
			</view>
		</view>
	</view>
</template>

<script>
	import { crawlerProperties } from '@/mock/crawlerData.js'
	import { messageApi } from '@/api/user.js'
	
	export default {
		data() {
			return {
				propertyId: null,
				propertyTitle: '',
				propertyPrice: '',
				propertyInfo: {
					title: '',
					price: '',
					image: '',
					rooms: 0,
					halls: 1,
					bedrooms: 0,
					area: 0,
					images: []
				},
				messageList: [],
				inputContent: '',
				scrollToView: '',
				otherUserName: '房东',
				otherInitial: '房',
				keyboardHeight: 0,
				inputFocused: false,
				currentUserId: null,
				quickReplies: [
					'这个房子还在吗？',
					'什么时候可以看房？',
					'可以便宜点吗？',
					'周边交通怎么样？',
					'物业费多少？',
					'可以养宠物吗？'
				]
			}
		},
		
		onLoad(options) {
			this.propertyId = options.propertyId
			this.propertyTitle = options.propertyTitle || '房源咨询'
			this.propertyPrice = options.propertyPrice || ''
			this.currentUserId = this.getCurrentUserId()
			
			uni.setNavigationBarTitle({
				title: this.propertyTitle.length > 10 ? this.propertyTitle.substring(0, 10) + '...' : this.propertyTitle
			})
			
			this.initPropertyInfo()
			this.loadMessages()
			this.markAsRead()
		},
		
		onShow() {
			this.loadMessages()
		},
		
		onUnload() {
			this.updateConversationList()
		},
		
		methods: {
			initPropertyInfo() {
				this.propertyInfo = {
					title: this.propertyTitle,
					price: this.propertyPrice || '',
					image: '',
					rooms: 0,
					halls: 1,
					bedrooms: 0,
					area: 0,
					images: []
				}
				
				let found = false
				
				// 从爬虫示例数据查找
				const crawlerProp = crawlerProperties.find(p => p.id == this.propertyId)
				if (crawlerProp) {
					this.propertyInfo = {
						title: crawlerProp.title || this.propertyTitle,
						price: crawlerProp.price || this.propertyPrice || '',
						image: this.getFirstImage(crawlerProp.images),
						rooms: crawlerProp.rooms || crawlerProp.bedrooms || 0,
						halls: crawlerProp.halls || 1,
						bedrooms: crawlerProp.bedrooms || 0,
						area: crawlerProp.area || 0,
						images: crawlerProp.images || []
					}
					found = true
				}
				
				// 从本地存储的房源列表查找
				if (!found) {
					const properties = uni.getStorageSync('propertyList')
					if (properties) {
						try {
							const list = JSON.parse(properties)
							const prop = list.find(p => p.id == this.propertyId)
							if (prop) {
								this.propertyInfo = {
									title: prop.title || this.propertyTitle,
									price: prop.price || this.propertyPrice || '',
							image: this.getFirstImage(prop.images || prop.image || prop.coverImage),
							rooms: prop.rooms || prop.bedrooms || 0,
							halls: prop.halls || 1,
							bedrooms: prop.bedrooms || 0,
							area: prop.area || 0,
							images: prop.images || []
						}
						found = true
					}
				} catch (e) {
					console.error('解析房源信息失败', e)
				}
			}
			}
			
			// 从收藏列表查找
			if (!found) {
				const favorites = uni.getStorageSync('favorites')
				if (favorites) {
					try {
						const list = JSON.parse(favorites)
						const fav = list.find(f => (f.propertyId == this.propertyId || f.id == this.propertyId))
						const prop = fav.property || fav
						if (prop) {
							this.propertyInfo = {
								title: prop.title || this.propertyTitle,
								price: prop.price || this.propertyPrice || '',
									image: this.getFirstImage(prop.images || prop.image || prop.coverImage),
									rooms: prop.rooms || prop.bedrooms || 0,
									halls: prop.halls || 1,
									bedrooms: prop.bedrooms || 0,
									area: prop.area || 0,
									images: prop.images || []
								}
								found = true
							}
						} catch (e) {
							console.error('解析收藏房源信息失败', e)
						}
					}
				}
				
				// 从我的房源查找
				if (!found) {
					const myProperties = uni.getStorageSync('myProperties')
					if (myProperties) {
						try {
							const list = JSON.parse(myProperties)
							const prop = list.find(p => p.id == this.propertyId)
							if (prop) {
								this.propertyInfo = {
									title: prop.title || this.propertyTitle,
									price: prop.price || this.propertyPrice || '',
									image: this.getFirstImage(prop.images || prop.image || prop.coverImage),
									rooms: prop.rooms || prop.bedrooms || 0,
									halls: prop.halls || 1,
									bedrooms: prop.bedrooms || 0,
									area: prop.area || 0,
									images: prop.images || []
								}
								found = true
							}
						} catch (e) {
							console.error('解析我的房源信息失败', e)
						}
					}
				}
				
				this.otherInitial = this.propertyTitle.charAt(0)
			},
			
			getFirstImage(images) {
				if (!images) return '/static/placeholder.png'
				if (Array.isArray(images)) {
					return images[0] || '/static/placeholder.png'
				}
				if (typeof images === 'string') {
					const imageList = images.split(',')
					return imageList[0] || '/static/placeholder.png'
				}
				return '/static/placeholder.png'
			},
			
			getPropertyImage() {
				return this.propertyInfo.image || '/static/placeholder.png'
			},
			
			onImageError() {
				this.propertyInfo.image = '/static/placeholder.png'
			},
			
			useQuickReply(reply) {
				this.inputContent = reply
			},
			
			getCurrentUserId() {
				const userInfo = uni.getStorageSync('userInfo')
				if (!userInfo) return null
				try {
					const info = typeof userInfo === 'string' ? JSON.parse(userInfo) : userInfo
					return info.id || null
				} catch (e) {
					return null
				}
			},
			
			async loadMessages() {
				if (!this.propertyId) return
				
				try {
					const res = await messageApi.getConversationMessages(this.propertyId)
					if (res.code === 200 && res.data) {
						this.messageList = res.data.map(msg => ({
							id: msg.id,
							content: msg.content,
							sendTime: msg.createTime,
							isSelf: msg.senderRole === 0 || (msg.senderRole === undefined && msg.userId === this.currentUserId),
							senderRole: msg.senderRole
						}))
					} else {
						this.messageList = []
					}
				} catch (error) {
					console.error('加载消息失败:', error)
					this.messageList = []
				}
				
				this.$nextTick(() => {
					this.scrollToBottom()
				})
			},
			
			updateConversationList() {
				let conversations = uni.getStorageSync('conversations')
				if (!conversations) return
				
				try {
					conversations = JSON.parse(conversations)
					const convIndex = conversations.findIndex(c => c.propertyId == this.propertyId)
					
					if (convIndex >= 0) {
						const lastMsg = this.messageList[this.messageList.length - 1]
						conversations[convIndex].lastMessageContent = lastMsg ? lastMsg.content : ''
						conversations[convIndex].lastMessageTime = lastMsg ? lastMsg.sendTime : new Date().toISOString()
						conversations[convIndex].isRead = 1
						conversations[convIndex].unreadCount = 0
						if (this.propertyPrice) {
							conversations[convIndex].propertyPrice = this.propertyPrice
						}
						uni.setStorageSync('conversations', JSON.stringify(conversations))
					}
				} catch (e) {
					console.error('更新会话列表失败', e)
				}
			},
			
			markAsRead() {
				let conversations = uni.getStorageSync('conversations')
				if (!conversations) return
				
				try {
					conversations = JSON.parse(conversations)
					const convIndex = conversations.findIndex(c => c.propertyId == this.propertyId)
					if (convIndex >= 0) {
						conversations[convIndex].isRead = 1
						conversations[convIndex].unreadCount = 0
						uni.setStorageSync('conversations', JSON.stringify(conversations))
					}
				} catch (e) {
					console.error(e)
				}
			},
			
			async sendMessage() {
				const content = this.inputContent.trim()
				if (!content) return
				
				this.inputContent = ''
				
				try {
					const res = await messageApi.sendConversationMessage({
						propertyId: this.propertyId,
						content: content
					})
					
					if (res.code === 200) {
						await this.loadMessages()
						this.updateConversationList()
					} else {
						uni.showToast({
							title: res.message || '发送失败',
							icon: 'none'
						})
					}
				} catch (error) {
					console.error('发送消息失败:', error)
					uni.showToast({
						title: error.message || '发送失败',
						icon: 'none'
					})
				}
			},
			
			scrollToBottom() {
				if (this.messageList.length > 0) {
					this.scrollToView = 'msg-' + this.messageList[this.messageList.length - 1].id
				}
			},
			
			shouldShowTime(index) {
				if (index === 0) return true
				const current = new Date(this.messageList[index].sendTime)
				const prev = new Date(this.messageList[index - 1].sendTime)
				return (current.getTime() - prev.getTime()) > 5 * 60 * 1000
			},
			
			formatMessageTime(time) {
				if (!time) return ''
				const date = new Date(time)
				const now = new Date()
				const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
				const msgDate = new Date(date.getFullYear(), date.getMonth(), date.getDate())
				const diffDays = Math.floor((today - msgDate) / (24 * 60 * 60 * 1000))
				
				const hours = date.getHours().toString().padStart(2, '0')
				const minutes = date.getMinutes().toString().padStart(2, '0')
				const timeStr = `${hours}:${minutes}`
				
				if (diffDays === 0) {
					return timeStr
				} else if (diffDays === 1) {
					return '昨天 ' + timeStr
				} else if (diffDays < 7) {
					const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
					return weekdays[date.getDay()] + ' ' + timeStr
				} else {
					const month = (date.getMonth() + 1).toString().padStart(2, '0')
					const day = date.getDate().toString().padStart(2, '0')
					return `${month}-${day} ${timeStr}`
				}
			},
			
			goToProperty() {
				uni.navigateTo({
					url: `/pages/property/detail?id=${this.propertyId}`
				})
			},
			
			onInputFocus(e) {
				this.inputFocused = true
				this.keyboardHeight = e.detail.height || 0
				setTimeout(() => {
					this.scrollToBottom()
				}, 300)
			},
			
			onInputBlur() {
				this.inputFocused = false
				this.keyboardHeight = 0
			}
		}
	}
</script>

<style lang="scss" scoped>
	page {
		height: 100%;
		background: $bg-color-page;
	}
	
	.chat-page {
		display: flex;
		flex-direction: column;
		height: 100vh;
		background: $bg-color-page;
	}
	
	.property-card {
		display: flex;
		align-items: center;
		gap: $spacing-sm;
		background: $bg-color;
		padding: $spacing-sm $spacing-base;
		margin: $spacing-sm;
		border-radius: $radius-xl;
		box-shadow: $shadow-xs;
		border: 1rpx solid $border-color-light;
		flex-shrink: 0;
		transition: all $transition-base;
		
		&:active {
			transform: scale(0.98);
			box-shadow: $shadow-sm;
		}
	}
	
	.property-image {
		width: 96rpx;
		height: 96rpx;
		border-radius: $radius-lg;
		background: $bg-color-light;
		flex-shrink: 0;
	}
	
	.property-info {
		flex: 1;
		min-width: 0;
	}
	
	.property-title {
		display: block;
		font-size: $uni-font-size-base;
		color: $text-color-primary;
		font-weight: 600;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
		margin-bottom: $spacing-xs;
	}
	
	.property-meta {
		display: flex;
		align-items: center;
		gap: $spacing-xs;
		flex-wrap: wrap;
	}
	
	.property-price {
		font-size: $uni-font-size-base;
		color: $price-color;
		font-weight: 700;
	}
	
	.property-rooms,
	.property-area {
		font-size: 22rpx;
		color: $text-color-secondary;
		background: rgba($primary-color, 0.06);
		padding: 4rpx 12rpx;
		border-radius: $radius-sm;
	}
	
	.arrow-icon {
		font-size: 44rpx;
		color: $text-color-placeholder;
		flex-shrink: 0;
		font-weight: 300;
	}
	
	.quick-replies {
		background: $bg-color;
		padding: $spacing-sm 0;
		flex-shrink: 0;
		border-top: 1rpx solid $border-color-light;
	}
	
	.quick-reply-scroll {
		white-space: nowrap;
	}
	
	.quick-reply-list {
		display: inline-flex;
		gap: $spacing-sm;
		padding: 0 $spacing-base;
	}
	
	.quick-reply-item {
		display: inline-flex;
		align-items: center;
		padding: $spacing-xs $spacing-sm;
		background: rgba($primary-color, 0.06);
		border-radius: $radius-full;
		font-size: $uni-font-size-sm;
		color: $primary-color;
		flex-shrink: 0;
		transition: all $transition-base;
		
		&:active {
			background: $primary-color;
			color: $text-color-inverse;
			transform: scale(0.95);
		}
	}
	
	.message-list-wrapper {
		flex: 1;
		height: 0;
		overflow: hidden;
	}
	
	.message-list {
		height: 100%;
	}
	
	.message-container {
		padding: $spacing-base;
		padding-bottom: $spacing-lg;
	}
	
	.message-item {
		margin-bottom: $spacing-base;
		animation: fadeSlideIn 0.3s ease-out;
	}
	
	@keyframes fadeSlideIn {
		from {
			opacity: 0;
			transform: translateY(10rpx);
		}
		to {
			opacity: 1;
			transform: translateY(0);
		}
	}
	
	.time-separator {
		text-align: center;
		margin-bottom: $spacing-base;
	}
	
	.time-text {
		font-size: 22rpx;
		color: $text-color-placeholder;
		background: rgba(0, 0, 0, 0.06);
		padding: 6rpx 20rpx;
		border-radius: $radius-full;
	}
	
	.message-bubble-wrap {
		display: flex;
		align-items: flex-end;
		gap: $spacing-sm;
	}
	
	.message-self .message-bubble-wrap {
		flex-direction: row-reverse;
	}
	
	.avatar {
		width: 76rpx;
		height: 76rpx;
		border-radius: 50%;
		background: $primary-gradient;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
		box-shadow: $shadow-xs;
	}
	
	.self-avatar {
		background: $secondary-gradient;
	}
	
	.avatar-text {
		font-size: 28rpx;
		color: $text-color-inverse;
		font-weight: 600;
	}
	
	.message-bubble {
		max-width: 72%;
		padding: $spacing-sm $spacing-base;
		border-radius: $radius-xl;
		background: $bg-color;
		box-shadow: $shadow-xs;
		border: 1rpx solid $border-color-light;
	}
	
	.message-self .message-bubble {
		background: $primary-gradient;
		border-bottom-right-radius: $radius-sm;
		border: none;
		box-shadow: 0 4rpx 12rpx rgba($primary-color, 0.25);
	}
	
	.message-other .message-bubble {
		background: $bg-color;
		border-bottom-left-radius: $radius-sm;
	}
	
	.message-content {
		font-size: $uni-font-size-base;
		line-height: 1.6;
		word-wrap: break-word;
		word-break: break-all;
	}
	
	.message-self .message-content {
		color: $text-color-inverse;
	}
	
	.message-other .message-content {
		color: $text-color-primary;
	}
	
	.input-section {
		background: $bg-color;
		border-top: 1rpx solid $border-color-light;
		padding: $spacing-sm;
		padding-bottom: calc(#{$spacing-sm} + env(safe-area-inset-bottom));
		flex-shrink: 0;
		box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.04);
	}
	
	.input-container {
		display: flex;
		align-items: flex-end;
		gap: $spacing-sm;
	}
	
	.message-input {
		flex: 1;
		min-height: 76rpx;
		max-height: 200rpx;
		padding: $spacing-sm $spacing-base;
		background: $bg-color-light;
		border-radius: $radius-full;
		font-size: $uni-font-size-base;
		line-height: 1.5;
		box-sizing: border-box;
		transition: all $transition-base;
		border: 2rpx solid transparent;
		
		&:focus {
			background: $bg-color;
			border-color: $primary-color;
			box-shadow: 0 0 0 6rpx rgba($primary-color, 0.08);
		}
	}
	
	.send-btn {
		flex-shrink: 0;
		height: 76rpx;
		line-height: 76rpx;
		padding: 0 36rpx;
		background: $primary-gradient;
		color: $text-color-inverse;
		border-radius: $radius-full;
		font-size: $uni-font-size-base;
		font-weight: 600;
		border: none;
		margin: 0;
		transition: all $transition-base;
		box-shadow: 0 4rpx 12rpx rgba($primary-color, 0.3);
		
		&:active {
			transform: scale(0.95);
			box-shadow: 0 2rpx 8rpx rgba($primary-color, 0.25);
		}
	}
	
	.send-btn[disabled] {
		background: $bg-color-light;
		color: $text-color-placeholder;
		box-shadow: none;
	}
	
	.send-btn::after {
		border: none;
	}
</style>
