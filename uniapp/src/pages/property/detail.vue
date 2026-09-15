<template>
	<view class="detail-page">
		<!-- 加载状态 -->
		<view v-if="loading" class="loading-container">
			<text class="loading-text">加载中...</text>
		</view>
		
		<!-- 房源详情 -->
		<view v-else-if="property" class="property-detail">
			<!-- 图片轮播 -->
			<view class="image-section animate-fade-in-up">
				<swiper class="image-swiper" indicator-dots="true" autoplay="false">
					<swiper-item v-for="(image, index) in imageList" :key="index">
						<image 
							v-if="!image.isPlaceholder && image.url" 
							:src="image.url" 
							class="swiper-image" 
							mode="aspectFill"
							@error="onImageError($event, index)"
						></image>
						<view v-else class="swiper-placeholder">
							<text class="placeholder-icon">🏠</text>
							<text class="placeholder-text">房源图片</text>
						</view>
					</swiper-item>
				</swiper>
				
				<!-- 收藏按钮 -->
				<view class="favorite-btn card-touch" @click="toggleFavorite">
					<text class="favorite-icon">{{ property.isFavorite ? '❤️' : '🤍' }}</text>
				</view>
				
				<!-- 数据来源标签 -->
				<view v-if="usingMockData || property.source" class="source-tag">
					<text class="source-text">{{ property.source || '示例数据' }}</text>
				</view>
			</view>
			
			<!-- 基本信息 -->
			<view class="basic-info card animate-fade-in-up animate-delay-1">
				<view class="property-title">{{ property.title }}</view>
				<view class="property-price animate-elastic-in animate-delay-2">
					<text class="price">¥{{ formatPrice(property.price, property.transactionType) }}</text>
					<text class="price-unit" v-if="property.transactionType !== 1">/月</text>
				</view>
				
				<view class="property-tags animate-fade-in-up animate-delay-2">
					<text class="tag primary-tag">{{ getPropertyTypeText(property.propertyType) }}</text>
					<text class="tag success-tag">{{ getTransactionTypeText(property.transactionType) }}</text>
					<text class="tag info-tag">{{ property.area }}㎡</text>
					<text 
						v-if="displayRooms > 0"
						class="tag info-tag"
					>{{ displayRooms }}室{{ displayHalls }}厅</text>
					<text 
						v-if="displayBathrooms > 0"
						class="tag info-tag"
					>{{ displayBathrooms }}卫</text>
					<text v-if="property.floor || property.totalFloor" class="tag warning-tag">{{ property.floor || '-' }}/{{ property.totalFloor || '-' }}层</text>
				</view>
				
				<view class="property-address animate-fade-in-up animate-delay-3">
					<text class="location-icon">📍</text>
					<text>{{ property.city }}{{ property.district }} {{ property.address }}</text>
				</view>
			</view>
			
			<!-- 房源描述 -->
			<view class="description-section card animate-fade-in-up animate-delay-2">
				<view class="section-title">房源描述</view>
				<view class="description-content">
					{{ property.description || '暂无描述' }}
				</view>
			</view>
			
			<!-- 配套设施 -->
			<view v-if="property.facilities" class="facilities-section card animate-fade-in-up animate-delay-3">
				<view class="section-title">配套设施</view>
				<view class="facilities-list">
					<text 
						v-for="facility in facilitiesList"
						:key="facility"
						class="facility-tag"
					>{{ facility }}</text>
				</view>
			</view>
			
			<!-- 房东信息 -->
			<view class="landlord-section card animate-fade-in-up animate-delay-4">
				<view class="section-title">房东信息</view>
				<view class="landlord-info">
					<view class="landlord-avatar">
						<text class="avatar-text">{{ getLandlordInitial() }}</text>
					</view>
					<view class="landlord-details">
						<view class="landlord-name">{{ getLandlordName() }}</view>
						<view class="landlord-desc">{{ getLandlordDesc() }}</view>
					</view>
					<button class="contact-btn btn-shimmer card-touch" @click="contactLandlord">
						联系房东
					</button>
				</view>
			</view>
		</view>
		
		<!-- 底部操作栏 -->
		<view class="bottom-actions animate-fade-in-up">
			<view class="action-buttons">
				<button 
					class="appointment-btn btn-shimmer card-touch"
					@click="makeAppointment"
					:disabled="!property"
				>
					预约看房
				</button>
				<button 
					class="consult-btn btn-shimmer card-touch"
					@click="showMessageModal"
					:disabled="!property"
				>
					立即咨询
				</button>
			</view>
		</view>
		
		<!-- 预约看房弹窗 -->
		<view v-if="showAppointment" class="appointment-modal" @click="closeAppointmentModal">
			<view class="appointment-modal-content" @click.stop>
				<view class="modal-header">
					<text class="modal-title">预约看房</text>
					<view class="modal-close" @click="closeAppointmentModal">×</view>
				</view>
				<view class="appointment-form">
					<view class="form-item">
						<text class="form-label">* 预约时间</text>
						<view class="picker-input" @click="showDatePicker">
							<text>{{ appointmentForm.appointmentTime || '请选择预约时间' }}</text>
							<text class="picker-arrow">▾</text>
						</view>
					</view>
					<view class="form-item">
						<text class="form-label">* 预约时段</text>
						<view class="picker-input" @click="showTimePicker">
							<text>{{ appointmentForm.timeIndex >= 0 ? timeSlots[appointmentForm.timeIndex] : '请选择时段' }}</text>
							<text class="picker-arrow">▾</text>
						</view>
					</view>
					<view class="form-item phone-item">
						<text class="form-label">* 联系电话</text>
						<input 
							v-model="appointmentForm.phone" 
							class="form-input" 
							placeholder="请输入联系电话"
							type="text"
						/>
					</view>
					<view class="form-item">
						<text class="form-label">备注信息</text>
						<textarea 
							v-model="appointmentForm.remark" 
							class="form-textarea" 
							placeholder="请输入备注信息（选填）"
							:maxlength="200"
						></textarea>
						<text class="input-count">{{ appointmentForm.remark.length }}/200</text>
					</view>
				</view>
				<view class="modal-footer">
					<button class="btn-cancel" @click="closeAppointmentModal">取消</button>
					<button class="btn-submit" @click="submitAppointment">提交预约</button>
				</view>
			</view>
		</view>
		
		<!-- 发消息弹窗 -->
		<view v-if="showMessage" class="message-modal" @click="closeMessageModal">
			<view class="message-modal-content" @click.stop>
				<view class="modal-header">
					<text class="modal-title">与房东对话</text>
					<view class="modal-close" @click="closeMessageModal">×</view>
				</view>
				<scroll-view 
					class="conversation-messages" 
					scroll-y 
					:scroll-top="scrollTop"
				>
					<view 
						v-for="msg in conversationMessages" 
						:key="msg.id"
						:class="['message-item', msg.userId === currentUserId ? 'message-self' : 'message-other']"
					>
						<view class="message-header">
							<text :class="['sender-tag', msg.userId === currentUserId ? 'sender-self' : 'sender-other']">
								{{ msg.userId === currentUserId ? '我' : '房东' }}
							</text>
							<text class="message-time">{{ formatTime(msg.createTime) }}</text>
						</view>
						<view class="message-content">
							{{ msg.content }}
						</view>
					</view>
					<view v-if="conversationMessages.length === 0" class="no-messages">
						暂无对话记录，发送第一条消息吧！
					</view>
				</scroll-view>
				<view class="conversation-input">
					<textarea 
						v-model="messageContent"
						class="message-textarea"
						placeholder="输入消息内容..."
						:maxlength="500"
						:adjust-position="true"
					></textarea>
					<view class="input-footer">
						<text class="input-count">{{ messageContent.length }}/500</text>
						<button class="btn-send" @click="sendMessage" :disabled="!messageContent.trim()">发送消息</button>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	import { propertyApi } from '@/api/property.js'
	import { aiApi } from '@/api/ai.js'
	import { messageApi } from '@/api/user.js'
	import { crawlerProperties } from '@/mock/crawlerData.js'
	
	export default {
		data() {
			return {
				propertyId: null,
				property: null,
				loading: false,
				usingMockData: false,
				showMessage: false,
				messageContent: '',
				conversationMessages: [],
				scrollTop: 0,
				currentUserId: null,
				showAppointment: false,
				appointmentForm: {
					appointmentTime: '',
					timeIndex: -1,
					phone: '',
					remark: ''
				},
				timeSlots: ['09:00-11:00', '14:00-16:00', '16:00-18:00', '19:00-21:00']
			}
		},
		
		computed: {
			// 图片列表
			imageList() {
				if (!this.property) {
					return [{ url: '', isPlaceholder: true }]
				}
				let images = this.property.images
				if (!images || (typeof images === 'string' && images.trim() === '')) {
					return [{ url: '', isPlaceholder: true }]
				}
				let imageUrls = []
				if (Array.isArray(images)) {
					imageUrls = images.filter(url => url && url.trim())
				} else {
					imageUrls = images.split(',').filter(url => url && url.trim()).map(url => url.trim())
				}
				if (imageUrls.length === 0) {
					return [{ url: '', isPlaceholder: true }]
				}
				return imageUrls.map(url => ({ url, isPlaceholder: false }))
			},
			
			// 配套设施列表
			facilitiesList() {
				if (!this.property) return []
				let facilities = this.property.facilities || this.property.amenities
				if (!facilities) return []
				if (Array.isArray(facilities)) return facilities
				return facilities.split(',').filter(f => f.trim())
			},
			
			// 显示的房间数
			displayRooms() {
				if (!this.property) return 0
				return this.property.rooms || this.property.bedrooms || 0
			},
			
			// 显示的厅数
			displayHalls() {
				if (!this.property) return 1
				return this.property.halls || this.property.livingRooms || 1
			},
			
			// 显示的卫生间数
			displayBathrooms() {
				if (!this.property) return 1
				return this.property.bathrooms || this.property.bathRoomNum || 1
			}
		},
		
		onLoad(options) {
			if (options.id) {
				this.propertyId = parseInt(options.id)
				this.loadPropertyDetail()
			}
		},
		
		methods: {
			// 加载房源详情
			async loadPropertyDetail() {
				this.loading = true
				
				try {
					const response = await propertyApi.getPropertyDetail(this.propertyId)
					
					if (response.code === 200 && response.data) {
						this.property = response.data
						// 设置默认值
						if (!this.property.isFavorite) {
							this.property.isFavorite = false
						}
						// 检查是否已收藏
						try {
							const favRes = await propertyApi.checkFavorite(this.propertyId)
							if (favRes.code === 200) {
								this.property.isFavorite = favRes.data === true
							}
						} catch (e) {
							// 未登录时忽略收藏检查
						}
						// 记录浏览行为
						this.recordViewBehavior()
					} else {
						this.loadMockPropertyDetail()
					}
				} catch (error) {
					console.error('加载房源详情失败，使用模拟数据:', error)
					this.loadMockPropertyDetail()
				} finally {
					this.loading = false
				}
			},
			
			// 加载模拟房源详情
			loadMockPropertyDetail() {
				this.usingMockData = true
				let foundProperty = crawlerProperties.find(p => p.id === this.propertyId)
				if (!foundProperty) {
					foundProperty = crawlerProperties[0]
					this.propertyId = foundProperty.id
				}
				
				this.property = JSON.parse(JSON.stringify(foundProperty))
				this.property.isFavorite = false
				this.property.priceType = 0
				this.property.propertyType = this.property.propertyType || 1
				this.property.transactionType = 0
				if (!this.property.landlord) {
					this.property.landlord = {
						nickname: (this.property.creatorName || '房东') + (this.property.creatorPhone ? ' ' + this.property.creatorPhone.substring(0, 3) + '****' + this.property.creatorPhone.substring(7) : ''),
						phone: this.property.creatorPhone || '138****8888',
						description: '真实房源，欢迎看房'
					}
				}
				
				try {
					const favorites = uni.getStorageSync('favorites')
					if (favorites) {
						const favList = JSON.parse(favorites)
						this.property.isFavorite = favList.some(f => f.id === this.propertyId)
					}
				} catch (e) {
					console.error(e)
				}
			},
			
			// 图片加载错误处理
			onImageError(e, index) {
				if (this.imageList[index]) {
					this.imageList[index].isPlaceholder = true
					this.$forceUpdate()
				}
			},
			
			// 记录浏览行为
			async recordViewBehavior() {
				try {
					await aiApi.recordUserBehavior(this.propertyId, 'view')
				} catch (error) {
					console.error('记录浏览行为失败:', error)
				}
			},
			
			// 切换收藏状态
			async toggleFavorite() {
				try {
					try {
						if (this.property.isFavorite) {
							await propertyApi.unfavoriteProperty(this.propertyId)
							await aiApi.recordUserBehavior(this.propertyId, 'unfavorite')
						} else {
							await propertyApi.favoriteProperty(this.propertyId)
							await aiApi.recordUserBehavior(this.propertyId, 'favorite')
						}
					} catch (apiError) {
						console.error('API收藏失败，使用本地存储:', apiError)
					}
					
					// 本地更新收藏状态
					this.property.isFavorite = !this.property.isFavorite
					this.saveFavoriteToLocal()
					
					uni.showToast({
						title: this.property.isFavorite ? '收藏成功' : '已取消收藏',
						icon: 'success'
					})
				} catch (error) {
					console.error('收藏操作失败:', error)
					uni.showToast({
						title: '操作失败',
						icon: 'none'
					})
				}
			},
			
			// 保存收藏到本地存储
			saveFavoriteToLocal() {
				try {
					let favorites = []
					const stored = uni.getStorageSync('favorites')
					if (stored) {
						favorites = JSON.parse(stored)
					}
					
					let coverImage = ''
					if (Array.isArray(this.property.images) && this.property.images.length > 0) {
						coverImage = this.property.images[0]
					} else if (typeof this.property.images === 'string' && this.property.images) {
						coverImage = this.property.images.split(',')[0]
					}
					
					if (this.property.isFavorite) {
						const exists = favorites.some(f => f.id === this.propertyId)
						if (!exists) {
							favorites.unshift({
								id: this.property.id,
								propertyId: this.property.id,
								title: this.property.title,
								price: this.property.price,
								area: this.property.area,
								rooms: this.property.rooms || this.property.bedrooms,
								halls: this.property.halls || 1,
								bedrooms: this.property.bedrooms || this.property.rooms,
								city: this.property.city,
								district: this.property.district,
								address: this.property.address,
								source: this.property.source || '示例数据',
								coverImage: coverImage,
								createTime: new Date().toISOString()
							})
						}
					} else {
						favorites = favorites.filter(f => f.id !== this.propertyId)
					}
					
					uni.setStorageSync('favorites', JSON.stringify(favorites))
				} catch (e) {
					console.error('保存收藏失败:', e)
				}
			},
			
			// 预约看房
			makeAppointment() {
				if (!this.property) return
				const token = uni.getStorageSync('token')
				if (!token) {
					uni.showModal({
						title: '提示',
						content: '需要登录后才能预约看房',
						confirmText: '去登录',
						success: (res) => {
							if (res.confirm) uni.navigateTo({ url: '/pages/user/login' })
						}
					})
					return
				}
				this.showAppointment = true
				this.appointmentForm = {
					appointmentTime: '',
					timeIndex: -1,
					phone: '',
					remark: ''
				}
			},
			
			// 关闭预约弹窗
			closeAppointmentModal() {
				this.showAppointment = false
			},
			
			// 显示日期选择器
			showDatePicker() {
				const today = new Date()
				const dates = []
				for (let i = 0; i < 30; i++) {
					const date = new Date(today)
					date.setDate(today.getDate() + i)
					const year = date.getFullYear()
					const month = String(date.getMonth() + 1).padStart(2, '0')
					const day = String(date.getDate()).padStart(2, '0')
					dates.push(`${year}-${month}-${day}`)
				}
				
				uni.showActionSheet({
					itemList: dates,
					success: (res) => {
						this.appointmentForm.appointmentTime = dates[res.tapIndex]
					}
				})
			},
			
			// 显示时段选择器
			showTimePicker() {
				uni.showActionSheet({
					itemList: this.timeSlots,
					success: (res) => {
						this.appointmentForm.timeIndex = res.tapIndex
					}
				})
			},
			
			// 提交预约
			async submitAppointment() {
				if (!this.appointmentForm.appointmentTime) {
					uni.showToast({ title: '请选择预约时间', icon: 'none' })
					return
				}
				if (this.appointmentForm.timeIndex < 0) {
					uni.showToast({ title: '请选择预约时段', icon: 'none' })
					return
				}
				if (!this.appointmentForm.phone || !/^1[3-9]\d{9}$/.test(this.appointmentForm.phone)) {
					uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
					return
				}
				
				const appointmentTime = this.appointmentForm.appointmentTime + 'T' + this.timeSlots[this.appointmentForm.timeIndex].split('-')[0] + ':00'
				
				try {
					const result = await propertyApi.makeAppointment({
						propertyId: this.property.id,
						appointmentTime: appointmentTime,
						contactPhone: this.appointmentForm.phone,
						remark: this.appointmentForm.remark
					})
					
					if (result.code === 200) {
						try { await aiApi.recordUserBehavior(this.propertyId, 'appointment') } catch (e) {}
						this.saveAppointmentToLocal(appointmentTime)
						uni.showToast({ title: '预约成功，等待房东确认', icon: 'success' })
						this.closeAppointmentModal()
					} else {
						uni.showToast({ title: result.message || '预约失败', icon: 'none' })
					}
				} catch (apiError) {
					console.error('预约失败:', apiError)
					uni.showToast({ title: apiError?.message || apiError?.errMsg || '预约失败，请检查网络', icon: 'none' })
				}
			},
			
			// 保存预约到本地
			saveAppointmentToLocal(appointmentTime) {
				try {
					let appointments = []
					const stored = uni.getStorageSync('appointments')
					if (stored) {
						appointments = JSON.parse(stored)
					}
					appointments.unshift({
						id: Date.now(),
						propertyId: this.property.id,
						propertyTitle: this.property.title,
						appointmentTime: appointmentTime,
						contactPhone: this.appointmentForm.phone,
						remark: this.appointmentForm.remark,
						status: 0,
						createTime: new Date().toISOString()
					})
					uni.setStorageSync('appointments', JSON.stringify(appointments))
				} catch (e) {
					console.error('保存预约失败:', e)
				}
			},
			
			// 联系房东（拨打电话）
			contactLandlord() {
				if (!this.property || !this.property.landlord || !this.property.landlord.phone) {
					uni.showToast({
						title: '暂无联系方式',
						icon: 'none'
					})
					return
				}
				
				uni.makePhoneCall({
					phoneNumber: this.property.landlord.phone
				})
			},
			
			// 格式化时间
			formatTime(time) {
				if (!time) return '-'
				return new Date(time).toLocaleString('zh-CN')
			},
			
			// 获取当前用户ID
			getCurrentUserId() {
				try {
					const userInfoStr = uni.getStorageSync('userInfo')
					if (!userInfoStr) return null
					const userInfo = typeof userInfoStr === 'object' ? userInfoStr : JSON.parse(userInfoStr)
					return userInfo.id || null
				} catch (e) {
					console.error('解析用户信息失败:', e)
					return null
				}
			},
			
			// 加载对话消息
			async loadConversationMessages() {
				try {
					if (!this.property || !this.property.id) {
						console.error('房源信息不存在')
						return
					}
					const res = await messageApi.getConversationMessages(this.property.id)
					if (res && res.code === 200 && res.data) {
						this.conversationMessages = res.data
						setTimeout(() => {
							this.scrollTop = 9999
						}, 100)
					}
				} catch (error) {
					console.error('加载消息失败:', error)
				}
			},
			
			// 显示发消息弹窗 - 改为跳转到独立聊天页面
			showMessageModal() {
				const token = uni.getStorageSync('token')
				if (!token) {
					uni.showModal({
						title: '提示',
						content: '需要登录后才能发送消息',
						confirmText: '去登录',
						success: (res) => {
							if (res.confirm) uni.navigateTo({ url: '/pages/user/login' })
						}
					})
					return
				}
				
				if (!this.property || !this.property.id) {
					uni.showToast({
						title: '房源信息异常',
						icon: 'none'
					})
					return
				}
				
				this.updateConversationListForFirstMessage()
				
				uni.navigateTo({
					url: `/pages/user/chat?propertyId=${this.property.id}&propertyTitle=${encodeURIComponent(this.property.title || '房源咨询')}&propertyPrice=${this.property.price || ''}`
				})
			},
			
			// 更新消息列表（首次发消息时添加会话）
			updateConversationListForFirstMessage() {
				try {
					let conversations = []
					const stored = uni.getStorageSync('conversations')
					if (stored) {
						conversations = JSON.parse(stored)
					}
					
					const propertyId = this.property.id
					const title = this.property.title || '房源信息'
					
					const exists = conversations.some(c => c.propertyId === propertyId)
					
					if (!exists) {
						conversations.unshift({
							propertyId: propertyId,
							propertyTitle: title,
							lastMessageContent: '开始聊天吧',
							lastMessageTime: new Date().toISOString(),
							isRead: 1,
							unreadCount: 0
						})
						uni.setStorageSync('conversations', JSON.stringify(conversations))
					}
				} catch (e) {
					console.error('更新对话列表失败:', e)
				}
			},
			
			// 关闭发消息弹窗
			closeMessageModal() {
				this.showMessage = false
				this.messageContent = ''
				this.conversationMessages = []
			},
			
			// 发送消息
			async sendMessage() {
				if (!this.messageContent.trim()) {
					uni.showToast({
						title: '请输入消息内容',
						icon: 'none'
					})
					return
				}
				
				if (!this.property || !this.property.id) {
					uni.showToast({
						title: '房源信息异常',
						icon: 'none'
					})
					return
				}
				
				try {
					const result = await messageApi.sendConversationMessage({
						propertyId: this.property.id,
						content: this.messageContent.trim()
					})
					
					if (result && result.code === 200) {
						this.messageContent = ''
						await this.loadConversationMessages()
						this.updateConversationList()
						uni.showToast({
							title: '消息发送成功',
							icon: 'success'
						})
					} else {
						uni.showToast({
							title: result && result.message || '发送失败',
							icon: 'none'
						})
					}
				} catch (error) {
					console.error('发送消息失败:', error)
					uni.showToast({
						title: '发送失败，请重试',
						icon: 'none'
					})
				}
			},
			
			// 更新消息列表
			updateConversationList() {
				try {
					const conversations = JSON.parse(uni.getStorageSync('conversations') || '[]')
					const propertyId = this.property.id
					const title = this.property.title || '房源信息'
					
					let exists = false
					for (let i = 0; i < conversations.length; i++) {
						if (conversations[i].propertyId === propertyId) {
							conversations[i].lastMessageContent = this.messageContent.trim()
							conversations[i].lastMessageTime = new Date().toISOString()
							conversations[i].isRead = 0
							conversations[i].unreadCount++
							exists = true
							break
						}
					}
					
					if (!exists) {
						conversations.unshift({
							propertyId: propertyId,
							propertyTitle: title,
							lastMessageContent: this.messageContent.trim(),
							lastMessageTime: new Date().toISOString(),
							isRead: 0,
							unreadCount: 1
						})
					}
					
					uni.setStorageSync('conversations', JSON.stringify(conversations))
				} catch (e) {
					console.error('更新对话列表失败:', e)
				}
			},
			
			// 获取房东名字首字母
			getLandlordInitial() {
				if (this.property && this.property.landlord && this.property.landlord.nickname) {
					return this.property.landlord.nickname.charAt(0)
				}
				return '房'
			},
			
			// 获取房东名字
			getLandlordName() {
				if (this.property && this.property.landlord && this.property.landlord.nickname) {
					return this.property.landlord.nickname
				}
				return '房东'
			},
			
			// 获取房东描述
			getLandlordDesc() {
				if (this.property && this.property.landlord && this.property.landlord.description) {
					return this.property.landlord.description
				}
				return '暂无介绍'
			},
			
			// 获取房源类型文本
			getPropertyTypeText(type) {
				if (typeof type === 'string') {
					return type
				}
				const types = {
					0: '住宅',
					1: '公寓',
					2: '别墅'
				}
				return types[type] || '住宅'
			},
			
			// 获取交易类型文本
			getTransactionTypeText(type) {
				return type === 1 ? '出售' : '出租'
			},
			
			// 格式化价格
			formatPrice(price) {
				if (!price) return '0'
				if (price >= 10000) {
					return (price / 10000).toFixed(1) + '万'
				}
				return price.toLocaleString()
			}
		}
	}
</script>

<style lang="scss" scoped>
	.detail-page {
		background: $bg-color-page;
		min-height: 100vh;
		padding-bottom: calc(120rpx + env(safe-area-inset-bottom));
	}
	
	.loading-container {
		display: flex;
		justify-content: center;
		padding: 100rpx $spacing-base;
	}
	
	.loading-text {
		font-size: 26rpx;
		color: $text-color-secondary;
	}
	
	.image-section {
		position: relative;
	}
	
	.image-swiper {
		height: 500rpx;
	}
	
	.swiper-image {
		width: 100%;
		height: 100%;
	}
	
	.swiper-placeholder {
		width: 100%;
		height: 100%;
		background: $primary-gradient;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: $spacing-sm;
		position: relative;
		overflow: hidden;
		
		&::before {
			content: '';
			position: absolute;
			top: -30%;
			right: -10%;
			width: 300rpx;
			height: 300rpx;
			background: rgba(255, 255, 255, 0.1);
			border-radius: 50%;
		}
	}
	
	.placeholder-icon {
		font-size: 120rpx;
		position: relative;
		z-index: 2;
	}
	
	.placeholder-text {
		font-size: 28rpx;
		color: rgba(255, 255, 255, 0.9);
		position: relative;
		z-index: 2;
	}
	
	.favorite-btn {
		position: absolute;
		top: $spacing-lg;
		right: $spacing-lg;
		width: 80rpx;
		height: 80rpx;
		background: rgba(0, 0, 0, 0.4);
		backdrop-filter: blur(10rpx);
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		z-index: 10;
		transition: all $transition-fast;
		
		&:active {
			transform: scale(0.9);
		}
	}
	
	.favorite-icon {
		font-size: 40rpx;
	}
	
	.card {
		background: $bg-color;
		margin: $spacing-sm $spacing-base;
		padding: $spacing-base;
		border-radius: $radius-xl;
		box-shadow: $shadow-sm;
		border: 1rpx solid $border-color-light;
	}
	
	.basic-info {
		margin-top: -$spacing-lg;
		position: relative;
		z-index: 5;
		border-top-left-radius: $radius-2xl;
		border-top-right-radius: $radius-2xl;
	}
	
	.property-title {
		font-size: 36rpx;
		font-weight: 700;
		color: $text-color-primary;
		margin-bottom: $spacing-sm;
		line-height: 1.4;
	}
	
	.property-price {
		margin-bottom: $spacing-sm;
		display: flex;
		align-items: baseline;
	}
	
	.price {
		font-size: 52rpx;
		font-weight: 700;
		color: $price-color;
	}
	
	.price-unit {
		font-size: 28rpx;
		color: $text-color-secondary;
		margin-left: $spacing-xs;
	}
	
	.property-tags {
		display: flex;
		flex-wrap: wrap;
		gap: $spacing-xs;
		margin-bottom: $spacing-sm;
	}
	
	.tag {
		padding: 6rpx 16rpx;
		border-radius: $radius-full;
		font-size: 22rpx;
		font-weight: 500;
	}
	
	.primary-tag {
		background: rgba($primary-color, 0.1);
		color: $primary-color;
	}
	
	.success-tag {
		background: rgba($success-color, 0.1);
		color: $success-color;
	}
	
	.info-tag {
		background: $info-color-light;
		color: $text-color-regular;
	}
	
	.warning-tag {
		background: rgba($warning-color, 0.1);
		color: $warning-color;
	}
	
	.source-tag {
		position: absolute;
		bottom: $spacing-base;
		left: $spacing-base;
		background: rgba(0, 0, 0, 0.6);
		backdrop-filter: blur(10rpx);
		padding: 8rpx 16rpx;
		border-radius: $radius-full;
		z-index: 10;
	}
	
	.source-text {
		font-size: 22rpx;
		color: #fff;
		font-weight: 500;
	}
	
	.property-address {
		display: flex;
		align-items: center;
		gap: 10rpx;
		font-size: 28rpx;
		color: $text-color-regular;
	}
	
	.location-icon {
		font-size: 24rpx;
		flex-shrink: 0;
	}
	
	.section-title {
		font-size: 32rpx;
		font-weight: 600;
		color: $text-color-primary;
		margin-bottom: $spacing-sm;
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
	
	.description-content {
		font-size: 28rpx;
		color: $text-color-regular;
		line-height: 1.7;
	}
	
	.config-grid {
		display: grid;
		grid-template-columns: repeat(2, 1fr);
		gap: $spacing-sm;
	}
	
	.config-item {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: $spacing-sm 0;
		border-bottom: 1rpx solid $border-color-light;
		
		&:nth-last-child(-n+2) {
			border-bottom: none;
		}
	}
	
	.config-label {
		font-size: 26rpx;
		color: $text-color-secondary;
	}
	
	.config-value {
		font-size: 26rpx;
		color: $text-color-primary;
		font-weight: 500;
	}
	
	.facilities-list {
		display: flex;
		flex-wrap: wrap;
		gap: $spacing-xs;
	}
	
	.facility-tag {
		padding: 8rpx 20rpx;
		background: $bg-color-light;
		color: $text-color-regular;
		border-radius: $radius-full;
		font-size: 24rpx;
		border: 1rpx solid $border-color-light;
	}
	
	.landlord-info {
		display: flex;
		align-items: center;
		gap: $spacing-sm;
	}
	
	.landlord-avatar {
		width: 80rpx;
		height: 80rpx;
		border-radius: 50%;
		background: $primary-gradient;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 4rpx 12rpx rgba($primary-color, 0.25);
	}
	
	.avatar-text {
		font-size: 28rpx;
		color: $text-color-inverse;
		font-weight: 600;
	}
	
	.landlord-details {
		flex: 1;
	}
	
	.landlord-name {
		font-size: 30rpx;
		font-weight: 600;
		color: $text-color-primary;
		margin-bottom: 6rpx;
	}
	
	.landlord-desc {
		font-size: 24rpx;
		color: $text-color-secondary;
	}
	
	.contact-btn {
		background: $primary-gradient;
		color: $text-color-inverse;
		border: none;
		border-radius: $radius-full;
		padding: $spacing-xs $spacing-base;
		font-size: 24rpx;
		font-weight: 500;
		transition: all $transition-fast;
		box-shadow: 0 4rpx 12rpx rgba($primary-color, 0.25);
		
		&:active {
			transform: scale(0.96);
		}
	}
	
	.bottom-actions {
		position: fixed;
		bottom: 0;
		left: 0;
		right: 0;
		background: $bg-color;
		padding: $spacing-sm $spacing-base;
		padding-bottom: calc($spacing-sm + env(safe-area-inset-bottom));
		border-top: 1rpx solid $border-color-light;
		z-index: 100;
		box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.04);
	}
	
	.action-buttons {
		display: flex;
		gap: $spacing-sm;
	}
	
	.appointment-btn, .consult-btn {
		flex: 1;
		padding: $spacing-sm;
		border-radius: $radius-full;
		font-size: $uni-font-size-base;
		font-weight: 500;
		border: none;
		transition: all $transition-fast;
	}
	
	.appointment-btn {
		background: $bg-color-light;
		color: $text-color-regular;
		border: 2rpx solid $border-color;
		
		&:active {
			background: $bg-color-hover;
		}
	}
	
	.consult-btn {
		background: $main-gradient;
		color: $text-color-inverse;
		box-shadow: 0 4rpx 16rpx rgba($primary-color, 0.25);
		
		&:active {
			transform: scale(0.98);
			box-shadow: 0 2rpx 8rpx rgba($primary-color, 0.2);
		}
	}
	
	/* 预约看房弹窗 */
	.appointment-modal {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background: rgba(0, 0, 0, 0.5);
		display: flex;
		align-items: flex-start;
		justify-content: center;
		z-index: 999;
		padding: $spacing-lg;
		padding-top: 150rpx;
		animation: fadeIn $transition-base ease;
	}
	
	.appointment-modal-content {
		width: 100%;
		max-width: 600rpx;
		background: $bg-color;
		border-radius: $radius-2xl;
		overflow: hidden;
		position: relative;
		z-index: 200;
		animation: slideUp $transition-base ease;
	}
	
	.appointment-form {
		padding: $spacing-base;
	}
	
	.form-item {
		margin-bottom: $spacing-base;
	}
	
	.form-label {
		display: block;
		font-size: $uni-font-size-base;
		color: $text-color-regular;
		margin-bottom: $spacing-xs;
		font-weight: 500;
	}
	
	.picker-input {
		display: flex;
		justify-content: space-between;
		align-items: center;
		height: 88rpx;
		padding: 0 $spacing-base;
		background: $bg-color-light;
		border-radius: $radius-lg;
		font-size: $uni-font-size-base;
		color: $text-color-primary;
		border: 2rpx solid transparent;
		transition: all $transition-base;
		
		&:active {
			border-color: $primary-color;
			background: $bg-color;
			box-shadow: 0 0 0 6rpx rgba($primary-color, 0.08);
		}
	}
	
	.picker-arrow {
		color: $text-color-placeholder;
		font-size: 24rpx;
	}
	
	.phone-item {
		position: relative;
		z-index: 100;
	}
	
	.form-input {
		width: 100%;
		height: 88rpx;
		padding: 0 $spacing-base;
		background: $bg-color-light;
		border-radius: $radius-lg;
		font-size: $uni-font-size-base;
		box-sizing: border-box;
		border: 2rpx solid transparent;
		transition: all $transition-base;
		display: flex;
		align-items: center;
		
		&:focus {
			border-color: $primary-color;
			background: $bg-color;
			box-shadow: 0 0 0 6rpx rgba($primary-color, 0.08);
		}
	}
	
	.form-textarea {
		width: 100%;
		height: 160rpx;
		padding: $spacing-sm $spacing-base;
		background: $bg-color-light;
		border-radius: $radius-lg;
		font-size: $uni-font-size-base;
		box-sizing: border-box;
		border: 2rpx solid transparent;
		transition: all $transition-base;
		resize: none;
		
		&:focus {
			border-color: $primary-color;
			background: $bg-color;
			box-shadow: 0 0 0 6rpx rgba($primary-color, 0.08);
		}
	}
	
	.input-count {
		display: block;
		text-align: right;
		font-size: $uni-font-size-sm;
		color: $text-color-placeholder;
		margin-top: $spacing-xs;
	}
	
	.modal-footer {
		display: flex;
		gap: $spacing-sm;
		padding: $spacing-sm $spacing-base;
		border-top: 1rpx solid $border-color-light;
	}
	
	.btn-cancel, .btn-submit {
		flex: 1;
		padding: $spacing-sm;
		border-radius: $radius-full;
		font-size: $uni-font-size-base;
		border: none;
		font-weight: 500;
		transition: all $transition-fast;
	}
	
	.btn-cancel {
		background: $bg-color-light;
		color: $text-color-regular;
		border: 2rpx solid $border-color;
	}
	
	.btn-submit {
		background: $primary-gradient;
		color: $text-color-inverse;
		box-shadow: 0 4rpx 12rpx rgba($primary-color, 0.25);
		
		&:active {
			transform: scale(0.98);
		}
	}
	
	/* 发消息弹窗 */
	.message-modal {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background: rgba(0, 0, 0, 0.5);
		display: flex;
		align-items: center;
		justify-content: center;
		z-index: 1000;
		padding: $spacing-lg;
		animation: fadeIn $transition-base ease;
	}
	
	.message-modal-content {
		width: 100%;
		max-width: 650rpx;
		background: $bg-color;
		border-radius: $radius-2xl;
		overflow: hidden;
		height: 700rpx;
		display: flex;
		flex-direction: column;
		animation: slideUp $transition-base ease;
	}
	
	.modal-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: $spacing-base;
		border-bottom: 1rpx solid $border-color-light;
		flex-shrink: 0;
		background: linear-gradient(135deg, rgba($primary-color, 0.05), rgba($secondary-color, 0.05));
	}
	
	.modal-title {
		font-size: 32rpx;
		font-weight: 600;
		color: $text-color-primary;
	}
	
	.modal-close {
		width: 60rpx;
		height: 60rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 48rpx;
		color: $text-color-secondary;
		transition: all $transition-fast;
		
		&:active {
			transform: rotate(90deg);
		}
	}
	
	.conversation-messages {
		flex: 1;
		overflow-y: auto;
		padding: $spacing-sm;
		background: $bg-color-page;
	}
	
	.message-item {
		margin-bottom: $spacing-sm;
		display: flex;
		flex-direction: column;
	}
	
	.message-self {
		align-items: flex-end;
	}
	
	.message-other {
		align-items: flex-start;
	}
	
	.message-header {
		display: flex;
		align-items: center;
		gap: 10rpx;
		margin-bottom: 6rpx;
	}
	
	.sender-tag {
		padding: 4rpx 14rpx;
		border-radius: $radius-full;
		font-size: 22rpx;
		font-weight: 500;
	}
	
	.sender-self {
		background: $primary-color;
		color: $text-color-inverse;
	}
	
	.sender-other {
		background: $secondary-color;
		color: $text-color-inverse;
	}
	
	.message-time {
		font-size: 22rpx;
		color: $text-color-placeholder;
	}
	
	.message-content {
		max-width: 70%;
		padding: $spacing-sm $spacing-base;
		border-radius: 24rpx;
		font-size: $uni-font-size-base;
		word-wrap: break-word;
		line-height: 1.6;
	}
	
	.message-self .message-content {
		background: $primary-gradient;
		color: $text-color-inverse;
		border-bottom-right-radius: 8rpx;
		box-shadow: 0 4rpx 12rpx rgba($primary-color, 0.2);
	}
	
	.message-other .message-content {
		background: $bg-color;
		color: $text-color-primary;
		border-bottom-left-radius: 8rpx;
		box-shadow: $shadow-sm;
		border: 1rpx solid $border-color-light;
	}
	
	.no-messages {
		text-align: center;
		padding: 100rpx $spacing-sm;
		color: $text-color-secondary;
		font-size: $uni-font-size-base;
	}
	
	.conversation-input {
		border-top: 1rpx solid $border-color-light;
		padding: $spacing-sm;
		flex-shrink: 0;
		background: $bg-color;
	}
	
	.message-textarea {
		width: 100%;
		height: 120rpx;
		padding: $spacing-sm;
		border: 2rpx solid transparent;
		border-radius: $radius-lg;
		font-size: $uni-font-size-base;
		background: $bg-color-light;
		box-sizing: border-box;
		transition: all $transition-base;
		resize: none;
		
		&:focus {
			border-color: $primary-color;
			background: $bg-color;
			box-shadow: 0 0 0 6rpx rgba($primary-color, 0.08);
		}
	}
	
	.input-footer {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-top: $spacing-xs;
	}
	
	.input-count {
		font-size: $uni-font-size-sm;
		color: $text-color-placeholder;
	}
	
	.btn-send {
		background: $primary-gradient;
		color: $text-color-inverse;
		border: none;
		border-radius: $radius-full;
		padding: $spacing-xs $spacing-base;
		font-size: $uni-font-size-base;
		font-weight: 500;
		transition: all $transition-fast;
		box-shadow: 0 4rpx 12rpx rgba($primary-color, 0.25);
		
		&:active {
			transform: scale(0.96);
		}
	}
	
	.btn-send[disabled] {
		background: $text-color-disabled;
		box-shadow: none;
	}
	
	@keyframes fadeIn {
		from { opacity: 0; }
		to { opacity: 1; }
	}
	
	@keyframes slideUp {
		from {
			opacity: 0;
			transform: translateY(40rpx);
		}
		to {
			opacity: 1;
			transform: translateY(0);
		}
	}
</style>