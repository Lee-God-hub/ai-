<template>
	<view class="login-page">
		<view class="login-container">
			<view class="login-card">
				<view class="card-header">
					<h2>用户登录</h2>
					<p class="subtitle">欢迎回到智能房产交易平台</p>
				</view>
				
				<view class="form-content">
					<view class="form-item">
						<view class="input-wrapper">
							<text class="input-icon">👤</text>
							<input 
								v-model="loginForm.username"
								placeholder="请输入用户名"
								class="form-input"
							/>
						</view>
					</view>
					
					<view class="form-item">
						<view class="input-wrapper">
							<text class="input-icon">🔒</text>
							<input 
								v-model="loginForm.password"
								:type="showPassword ? 'text' : 'password'"
								placeholder="请输入密码"
								class="form-input"
							/>
							<text class="input-toggle" @click="showPassword = !showPassword">
								{{ showPassword ? '🙈' : '👁' }}
							</text>
						</view>
					</view>
					
					<view class="form-item">
						<view class="remember-me">
							<view class="checkbox" :class="{ checked: loginForm.rememberMe }" @click="loginForm.rememberMe = !loginForm.rememberMe">
								<text v-if="loginForm.rememberMe">✓</text>
							</view>
							<text>记住我</text>
						</view>
					</view>
					
					<view class="form-actions">
						<button 
							class="login-btn"
							:class="{ loading: loading }"
							@click="handleLogin"
						>
							{{ loading ? '登录中...' : '登录' }}
						</button>
					</view>
					
					<view class="register-link">
						<span>还没有账号？</span>
						<text class="link" @click="goToRegister">立即注册</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	import { userApi } from '@/api/user.js'
	
	export default {
		data() {
			return {
				loading: false,
				showPassword: false,
				
				// 登录表单
				loginForm: {
					username: '',
					password: '',
					rememberMe: false
				}
			}
		},
		
		onLoad() {
			const rememberedUsername = uni.getStorageSync('rememberedUsername')
			if (rememberedUsername) {
				this.loginForm.username = rememberedUsername
				this.loginForm.rememberMe = true
			}
		},
		
		methods: {
			// 登录
			async handleLogin() {
				if (!this.loginForm.username) {
					uni.showToast({
						title: '请输入用户名或手机号',
						icon: 'none'
					})
					return
				}
				
				if (!this.loginForm.password) {
					uni.showToast({
						title: '请输入密码',
						icon: 'none'
					})
					return
				}
				
				this.loading = true

				try {
					const response = await userApi.login(this.loginForm)

					if (response.code === 200) {
						if (this.loginForm.rememberMe) {
							uni.setStorageSync('rememberedUsername', this.loginForm.username)
						} else {
							uni.removeStorageSync('rememberedUsername')
						}
						
						// 保存登录信息
						const token = response.data.token || (response.data.tokenPrefix || 'Bearer ') + response.data.accessToken
						uni.setStorageSync('token', token)
						// 构建userInfo对象
						const userInfo = {
							id: response.data.userId || response.data.id,
							username: response.data.username,
							nickname: response.data.realName || response.data.username,
							phone: response.data.phone,
							email: response.data.email,
							role: response.data.role,
							avatar: response.data.avatar
						}
						uni.setStorageSync('userInfo', userInfo)

						uni.showToast({
							title: '登录成功',
							icon: 'success'
						})

						// 返回上一页或跳转到首页
						setTimeout(() => {
							const pages = getCurrentPages()
							if (pages.length > 1) {
								uni.navigateBack()
							} else {
								uni.switchTab({ url: '/pages/index/index' })
							}
						}, 1200)
					} else {
						uni.showToast({
							title: response.message || '登录失败',
							icon: 'none'
						})
					}
				} catch (error) {
					console.log('API登录失败，使用本地模拟登录:', error)
					this.mockLogin()
				} finally {
					this.loading = false
				}
			},
			
			mockLogin() {
				setTimeout(() => {
					const token = 'mock_token_' + Date.now()
					uni.setStorageSync('token', token)
					
					const userInfo = {
						id: 1,
						username: this.loginForm.username || 'demo_user',
						nickname: this.loginForm.username || '体验用户',
						phone: '138****8888',
						email: 'd***@example.com',
						role: 0,
						avatar: ''
					}
					uni.setStorageSync('userInfo', userInfo)
					uni.setStorageSync('userId', userInfo.id)
					uni.setStorageSync('userName', userInfo.nickname)
					
					uni.showToast({
						title: '登录成功（本地体验）',
						icon: 'success'
					})
					
					setTimeout(() => {
						const pages = getCurrentPages()
						if (pages.length > 1) {
							uni.navigateBack()
						} else {
							uni.switchTab({ url: '/pages/index/index' })
						}
					}, 1200)
				}, 800)
			},
			
			// 跳转注册页
			goToRegister() {
				uni.navigateTo({
					url: '/pages/user/register'
				})
			}
		}
	}
</script>

<style lang="scss" scoped>
	.login-page {
		min-height: 100vh;
		display: flex;
		align-items: center;
		justify-content: center;
		padding: $spacing-lg;
		background: $bg-color-page;
		position: relative;
		overflow: hidden;
		
		&::before {
			content: '';
			position: absolute;
			top: -200rpx;
			left: -100rpx;
			width: 500rpx;
			height: 500rpx;
			background: $primary-gradient;
			border-radius: 50%;
			opacity: 0.1;
		}
		
		&::after {
			content: '';
			position: absolute;
			bottom: -150rpx;
			right: -80rpx;
			width: 400rpx;
			height: 400rpx;
			background: $secondary-gradient;
			border-radius: 50%;
			opacity: 0.08;
		}
	}
	
	.login-container {
		width: 100%;
		max-width: 680rpx;
		position: relative;
		z-index: 2;
	}
	
	.login-card {
		background: $bg-color;
		border-radius: $radius-xxl;
		overflow: hidden;
		box-shadow: $shadow-lg;
		border: 1rpx solid $border-color-light;
	}
	
	.card-header {
		background: $primary-gradient;
		padding: 70rpx $spacing-lg $spacing-xl;
		text-align: center;
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
	
	.card-header h2 {
		font-size: 48rpx;
		font-weight: 700;
		color: $text-color-inverse;
		margin: 0 0 $spacing-xs;
		position: relative;
		z-index: 2;
	}
	
	.card-header .subtitle {
		font-size: 26rpx;
		color: rgba(255, 255, 255, 0.85);
		margin: 0;
		position: relative;
		z-index: 2;
	}
	
	.form-content {
		padding: $spacing-xl $spacing-lg;
	}
	
	.form-item {
		margin-bottom: $spacing-base;
	}
	
	.input-wrapper {
		display: flex;
		align-items: center;
		padding: 0 $spacing-base;
		border: 2rpx solid $border-color-light;
		border-radius: $radius-lg;
		background: $bg-color-light;
		transition: all $transition-base;
		
		&:focus-within {
			border-color: $primary-color;
			background: $bg-color;
			box-shadow: 0 0 0 6rpx rgba($primary-color, 0.08);
		}
	}
	
	.input-icon {
		font-size: 32rpx;
		margin-right: $spacing-sm;
		opacity: 0.6;
	}
	
	.form-input {
		flex: 1;
		padding: $spacing-base 0;
		font-size: $uni-font-size-base;
		background: transparent;
		color: $text-color-primary;
	}
	
	.input-toggle {
		font-size: 28rpx;
		padding: $spacing-xs;
		opacity: 0.5;
		transition: opacity $transition-fast;
	}
	
	.input-toggle:active {
		opacity: 0.9;
	}
	
	.remember-me {
		display: flex;
		align-items: center;
		gap: $spacing-xs;
		
		text {
			font-size: $uni-font-size-sm;
			color: $text-color-secondary;
		}
	}
	
	.checkbox {
		width: 36rpx;
		height: 36rpx;
		border: 2rpx solid $border-color;
		border-radius: $radius-sm;
		display: flex;
		align-items: center;
		justify-content: center;
		transition: all $transition-fast;
		background: $bg-color;
		
		text {
			font-size: 20rpx;
			color: $text-color-inverse;
			font-weight: 700;
		}
		
		&.checked {
			background: $primary-gradient;
			border-color: $primary-color;
			box-shadow: 0 2rpx 8rpx rgba($primary-color, 0.3);
		}
	}
	
	.form-actions {
		margin-top: $spacing-lg;
	}
	
	.login-btn {
		width: 100%;
		background: $primary-gradient;
		color: $text-color-inverse;
		border: none;
		border-radius: $radius-full;
		padding: $spacing-base;
		font-size: $uni-font-size-lg;
		font-weight: 600;
		transition: all $transition-base;
		box-shadow: 0 6rpx 20rpx rgba($primary-color, 0.35);
		
		&:active {
			transform: scale(0.98);
			box-shadow: 0 3rpx 12rpx rgba($primary-color, 0.25);
		}
	}
	
	.login-btn.loading {
		background: $text-color-disabled;
		box-shadow: none;
	}
	
	.register-link {
		text-align: center;
		margin-top: $spacing-base;
		font-size: $uni-font-size-sm;
		color: $text-color-secondary;
		
		span {
			margin-right: 8rpx;
		}
		
		.link {
			color: $primary-color;
			font-weight: 500;
			transition: opacity $transition-fast;
		}
		
		.link:active {
			opacity: 0.7;
		}
	}
</style>