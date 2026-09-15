<template>
	<view class="profile-page">
		<!-- 用户信息卡片 -->
		<view class="user-card animate-fade-in-up">
			<view class="user-info">
				<view class="user-avatar" @click="chooseAvatar">
					<text class="avatar-text">{{ getUserInitial() }}</text>
				</view>
				<view class="user-details">
					<view class="user-name">{{ userInfo.nickname || '未登录' }}</view>
					<view class="user-desc">{{ userInfo.description || '这个人很懒，什么都没留下' }}</view>
				</view>
				<text class="arrow-icon">→</text>
			</view>
		</view>
		
		<!-- 统计数据 -->
		<view class="stats-section animate-fade-in-up animate-delay-1">
			<view class="stats-card card-touch">
				<view class="stat-item animate-fade-in-up animate-delay-1" @click="goToPage('/pages/favorites/index')">
					<text class="stat-number">{{ favoriteCount }}</text>
					<text class="stat-label">收藏</text>
				</view>
				<view class="stat-divider"></view>
				<view class="stat-item animate-fade-in-up animate-delay-2" @click="goToPage(isLandlord ? '/pages/landlord/appointments' : '/pages/appointment/list')">
					<text class="stat-number">{{ appointmentCount }}</text>
					<text class="stat-label">预约</text>
				</view>
				<view class="stat-divider"></view>
				<view class="stat-item animate-fade-in-up animate-delay-3" @click="goToPage(isLandlord ? '/pages/landlord/messages' : '/pages/user/messages')">
					<text class="stat-number">{{ messageCount }}</text>
					<text class="stat-label">消息</text>
				</view>
				<view class="stat-divider"></view>
				<view class="stat-item animate-fade-in-up animate-delay-4" @click="goToPage('/pages/landlord/manage')">
					<text class="stat-number">{{ propertyCount }}</text>
					<text class="stat-label">房源</text>
				</view>
			</view>
		</view>
		
		<!-- 功能菜单 -->
		<view class="menu-section">
			<!-- 房源管理 -->
			<view class="menu-group animate-fade-in-up animate-delay-2">
				<view class="menu-item animate-fade-in-up animate-delay-1" @click="goToPage('/pages/favorites/index')">
					<view class="menu-left">
						<text class="menu-icon">❤️</text>
						<text>我的收藏</text>
					</view>
					<view class="menu-right">
						<text class="count-badge" v-if="favoriteCount > 0">{{ favoriteCount }}</text>
						<text class="arrow-icon">→</text>
					</view>
				</view>
				
				<view class="menu-item animate-fade-in-up animate-delay-2" @click="goToPage('/pages/appointment/list')">
					<view class="menu-left">
						<text class="menu-icon">📅</text>
						<text>预约记录</text>
					</view>
					<view class="menu-right">
						<text class="count-badge" v-if="appointmentCount > 0">{{ appointmentCount }}</text>
						<text class="arrow-icon">→</text>
					</view>
				</view>
				
				<view class="menu-item animate-fade-in-up animate-delay-3" @click="goToPage('/pages/user/messages')" v-if="!isLandlord">
					<view class="menu-left">
						<text class="menu-icon">💬</text>
						<text>消息中心</text>
					</view>
					<view class="menu-right">
						<text class="count-badge" v-if="unreadMessageCount > 0">{{ unreadMessageCount }}</text>
						<text class="arrow-icon">→</text>
					</view>
				</view>
				
				<!-- 房东专属菜单 -->
				<view class="menu-item animate-fade-in-up animate-delay-2" @click="goToPage('/pages/landlord/appointments')" v-if="isLandlord">
					<view class="menu-left">
						<text class="menu-icon">📋</text>
						<text>预约管理</text>
					</view>
					<view class="menu-right">
						<text class="count-badge" v-if="appointmentCount > 0">{{ appointmentCount }}</text>
						<text class="arrow-icon">→</text>
					</view>
				</view>
				
				<view class="menu-item animate-fade-in-up animate-delay-3" @click="goToPage('/pages/landlord/messages')" v-if="isLandlord">
					<view class="menu-left">
						<text class="menu-icon">💬</text>
						<text>我的消息</text>
					</view>
					<view class="menu-right">
						<text class="count-badge" v-if="unreadMessageCount > 0">{{ unreadMessageCount }}</text>
						<text class="arrow-icon">→</text>
					</view>
				</view>
				
				<view class="menu-item animate-fade-in-up animate-delay-4" @click="goToPage('/pages/landlord/manage')">
					<view class="menu-left">
						<text class="menu-icon">🏠</text>
						<text>我的房源</text>
					</view>
					<view class="menu-right">
						<text class="count-badge" v-if="propertyCount > 0">{{ propertyCount }}</text>
						<text class="arrow-icon">→</text>
					</view>
				</view>
				
				<view class="menu-item animate-fade-in-up animate-delay-5" @click="goToPage('/pages/landlord/publish')">
					<view class="menu-left">
						<text class="menu-icon">➕</text>
						<text>发布房源</text>
					</view>
					<view class="menu-right">
						<text class="arrow-icon">→</text>
					</view>
				</view>
			</view>
			
			<!-- AI功能 -->
			<view class="menu-group animate-fade-in-up animate-delay-3">
				<view class="menu-item animate-fade-in-up animate-delay-1" @click="goToPage('/pages/ai/recommend')">
					<view class="menu-left">
						<text class="menu-icon">⭐</text>
						<text>AI推荐</text>
					</view>
					<view class="menu-right">
						<text class="feature-tag">智能</text>
						<text class="arrow-icon">→</text>
					</view>
				</view>
				
				<view class="menu-item animate-fade-in-up animate-delay-2" @click="goToPage('/pages/ai/qa')">
					<view class="menu-left">
						<text class="menu-icon">🤖</text>
						<text>AI问答</text>
					</view>
					<view class="menu-right">
						<text class="feature-tag">助手</text>
						<text class="arrow-icon">→</text>
					</view>
				</view>
			</view>
			
			<!-- 其他功能 -->
			<view class="menu-group animate-fade-in-up animate-delay-4">
				<view class="menu-item animate-fade-in-up animate-delay-1" @click="showSettings = true">
					<view class="menu-left">
						<text class="menu-icon">⚙️</text>
						<text>设置</text>
					</view>
					<view class="menu-right">
						<text class="arrow-icon">→</text>
					</view>
				</view>
				
				<view class="menu-item animate-fade-in-up animate-delay-2" @click="showAbout = true">
					<view class="menu-left">
						<text class="menu-icon">ℹ️</text>
						<text>关于我们</text>
					</view>
					<view class="menu-right">
						<text class="arrow-icon">→</text>
					</view>
				</view>
			</view>
		</view>
		
		<!-- 登录/退出按钮 -->
		<view class="action-section animate-fade-in-up animate-delay-5">
			<button 
				v-if="!isLoggedIn"
				class="login-btn btn-shimmer card-touch"
				@click="goToLogin"
			>
				立即登录
			</button>
			<button 
				v-else
				class="logout-btn btn-shimmer card-touch"
				@click="handleLogout"
			>
				退出登录
			</button>
		</view>
		
		<CustomTabbar :current="4" />
	</view>
</template>

<script>
	import { userApi } from '@/api/user.js'
	
	export default {
		data() {
			return {
				userInfo: {},
				isLoggedIn: false,
				favoriteCount: 0,
				propertyCount: 0,
				appointmentCount: 0,
				messageCount: 0,
				unreadMessageCount: 0,
				showSettings: false,
				showAbout: false
			}
		},
		
		onShow() {
			this.checkLoginStatus()
			this.loadLocalStats()
			if (this.isLoggedIn) {
				this.loadUserData()
			} else {
				this.goToLogin()
			}
		},
		
		computed: {
			isLandlord() {
				return this.userInfo.role === 1
			}
		},
		
		methods: {
			// 获取用户名首字母
			getUserInitial() {
				if (this.userInfo.nickname && this.userInfo.nickname.length > 0) {
					return this.userInfo.nickname.charAt(0)
				}
				return '用'
			},
			
			// 检查登录状态
			checkLoginStatus() {
				const token = uni.getStorageSync('token')
				let userInfo = uni.getStorageSync('userInfo')
				
				this.isLoggedIn = !!token
				if (userInfo) {
					if (typeof userInfo === 'string') {
						try {
							userInfo = JSON.parse(userInfo)
						} catch (e) {
							userInfo = {}
						}
					}
					this.userInfo = userInfo
				}
			},
			
			// 从本地存储加载统计数据
			loadLocalStats() {
				try {
					const favorites = uni.getStorageSync('favorites')
					this.favoriteCount = favorites ? JSON.parse(favorites).length : 0
					
					const appointments = uni.getStorageSync('appointments')
					this.appointmentCount = appointments ? JSON.parse(appointments).length : 0
					
					const conversations = uni.getStorageSync('conversations')
					if (conversations) {
						const convList = JSON.parse(conversations)
						this.messageCount = convList.length
						this.unreadMessageCount = convList.filter(c => c.unreadCount > 0 || c.isRead === 0).length
					} else {
						this.messageCount = 0
						this.unreadMessageCount = 0
					}
					
					const myProperties = uni.getStorageSync('myProperties')
					this.propertyCount = myProperties ? JSON.parse(myProperties).length : 0
				} catch (e) {
					console.error('加载本地统计数据失败:', e)
				}
			},
			
			// 加载用户数据
			async loadUserData() {
				try {
					// 获取用户信息
					const userResponse = await userApi.getUserInfo()
					if (userResponse.code === 200 && userResponse.data) {
						this.userInfo = {
							id: userResponse.data.id,
							username: userResponse.data.username,
							nickname: userResponse.data.realName || userResponse.data.username,
							phone: userResponse.data.phone,
							email: userResponse.data.email,
							role: userResponse.data.role,
							avatar: userResponse.data.avatar
						}
						uni.setStorageSync('userInfo', this.userInfo)
					}
				} catch (error) {
					console.error('加载用户数据失败:', error)
				}
			},
			
			// 选择头像
			chooseAvatar() {
				if (!this.isLoggedIn) {
					this.goToLogin()
					return
				}
				
				uni.chooseImage({
					count: 1,
					sizeType: ['compressed'],
					sourceType: ['album', 'camera'],
					success: (res) => {
						console.log('选择头像:', res.tempFilePaths[0])
						// 这里可以上传头像
					}
				})
			},
			
			// 跳转页面
			goToPage(url) {
				if (!this.isLoggedIn && (url.includes('favorites') || url.includes('landlord') || url.includes('messages') || url.includes('transaction') || url.includes('appointment'))) {
				this.goToLogin()
				return
			}
				
				// tabBar页面使用switchTab跳转
				const tabBarPages = ['/pages/index/index', '/pages/search/search', '/pages/ai/qa', '/pages/user/messages', '/pages/user/profile']
				if (tabBarPages.includes(url)) {
					uni.switchTab({
						url: url
					})
				} else {
					uni.navigateTo({
						url: url
					})
				}
			},
			
			// 跳转登录页
			goToLogin() {
				uni.navigateTo({
					url: '/pages/user/login'
				})
			},
			
			// 退出登录
			handleLogout() {
				uni.showModal({
					title: '提示',
					content: '确定要退出登录吗？',
					success: (res) => {
						if (res.confirm) {
							// 清除本地数据
							uni.removeStorageSync('token')
							uni.removeStorageSync('userInfo')
							
							// 重置状态
							this.isLoggedIn = false
							this.userInfo = {}
							this.loadLocalStats()
							
							uni.showToast({
								title: '已退出登录',
								icon: 'success'
							})
						}
					}
				})
			}
		}
	}
</script>

<style lang="scss" scoped>
	.profile-page {
		background: $bg-color-page;
		min-height: 100vh;
		padding-bottom: calc(120rpx + env(safe-area-inset-bottom));
	}
	
	.user-card {
		background: $primary-gradient;
		padding: 60rpx $spacing-base 80rpx;
		margin-bottom: -30rpx;
		position: relative;
		overflow: hidden;
		
		&::before {
			content: '';
			position: absolute;
			top: -100rpx;
			right: -60rpx;
			width: 300rpx;
			height: 300rpx;
			background: rgba(255, 255, 255, 0.1);
			border-radius: 50%;
		}
		
		&::after {
			content: '';
			position: absolute;
			bottom: -80rpx;
			left: -40rpx;
			width: 220rpx;
			height: 220rpx;
			background: rgba(255, 255, 255, 0.06);
			border-radius: 50%;
		}
	}
	
	.user-info {
		display: flex;
		align-items: center;
		gap: $spacing-base;
		position: relative;
		z-index: 2;
	}
	
	.user-avatar {
		width: 120rpx;
		height: 120rpx;
		border-radius: 50%;
		background: rgba(255, 255, 255, 0.25);
		display: flex;
		align-items: center;
		justify-content: center;
		border: 4rpx solid rgba(255, 255, 255, 0.3);
		box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.15);
		transition: all $transition-base;
		
		&:active {
			transform: scale(0.95);
		}
	}
	
	.avatar-text {
		font-size: 44rpx;
		color: $text-color-inverse;
		font-weight: 700;
	}
	
	.user-details {
		flex: 1;
		min-width: 0;
	}
	
	.user-name {
		font-size: 36rpx;
		font-weight: 700;
		color: $text-color-inverse;
		margin-bottom: 8rpx;
	}
	
	.user-desc {
		font-size: $uni-font-size-sm;
		color: rgba(255, 255, 255, 0.8);
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}
	
	.arrow-icon {
		font-size: 24rpx;
		color: rgba(255, 255, 255, 0.7);
	}
	
	.stats-section {
		padding: 0 $spacing-base;
		position: relative;
		z-index: 3;
	}
	
	.stats-card {
		background: $bg-color;
		border-radius: $radius-xl;
		padding: $spacing-base 0;
		display: flex;
		align-items: center;
		box-shadow: $shadow-base;
		border: 1rpx solid $border-color-light;
	}
	
	.stat-item {
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 8rpx;
		transition: all $transition-base;
		
		&:active {
			transform: scale(0.95);
		}
	}
	
	.stat-number {
		font-size: 40rpx;
		font-weight: 700;
		color: $primary-color;
	}
	
	.stat-label {
		font-size: $uni-font-size-sm;
		color: $text-color-secondary;
	}
	
	.stat-divider {
		width: 1rpx;
		height: 60rpx;
		background: $border-color-light;
	}
	
	.menu-section {
		padding: $spacing-base;
		position: relative;
		z-index: 1;
	}
	
	.menu-group {
		background: $bg-color;
		border-radius: $radius-xl;
		margin-bottom: $spacing-base;
		overflow: hidden;
		box-shadow: $shadow-sm;
		border: 1rpx solid $border-color-light;
	}
	
	.menu-item {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: $spacing-base $spacing-base;
		border-bottom: 1rpx solid $border-color-light;
		transition: all $transition-base;
		
		&:active {
			background: $bg-color-light;
		}
		
		&:last-child {
			border-bottom: none;
		}
	}
	
	.menu-left {
		display: flex;
		align-items: center;
		gap: $spacing-sm;
		
		text {
			font-size: $uni-font-size-base;
			color: $text-color-primary;
			font-weight: 500;
		}
	}
	
	.menu-icon {
		font-size: 32rpx;
		width: 48rpx;
		text-align: center;
	}
	
	.menu-right {
		display: flex;
		align-items: center;
		gap: $spacing-xs;
		
		.arrow-icon {
			font-size: 22rpx;
			color: $text-color-placeholder;
		}
	}
	
	.count-badge {
		background: $secondary-gradient;
		color: $text-color-inverse;
		font-size: 22rpx;
		padding: 4rpx 16rpx;
		border-radius: $radius-full;
		min-width: 32rpx;
		text-align: center;
		font-weight: 500;
		box-shadow: 0 2rpx 8rpx rgba($secondary-color, 0.3);
	}
	
	.feature-tag {
		background: rgba($primary-color, 0.1);
		color: $primary-color;
		font-size: 22rpx;
		padding: 6rpx 16rpx;
		border-radius: $radius-full;
		font-weight: 500;
	}
	
	.action-section {
		padding: $spacing-lg $spacing-base;
	}
	
	.login-btn, .logout-btn {
		width: 100%;
		padding: 28rpx;
		border-radius: $radius-full;
		font-size: $uni-font-size-lg;
		font-weight: 500;
		border: none;
		transition: all $transition-base;
	}
	
	.login-btn {
		background: $primary-gradient;
		color: $text-color-inverse;
		box-shadow: 0 6rpx 20rpx rgba($primary-color, 0.35);
		
		&:active {
			transform: scale(0.98);
			box-shadow: 0 3rpx 12rpx rgba($primary-color, 0.25);
		}
	}
	
	.logout-btn {
		background: $bg-color;
		color: $error-color;
		border: 2rpx solid rgba($error-color, 0.3);
		box-shadow: $shadow-sm;
		
		&:active {
			transform: scale(0.98);
			background: rgba($error-color, 0.05);
		}
	}
</style>