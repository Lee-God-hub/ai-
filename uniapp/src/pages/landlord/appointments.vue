<template>
	<view class="landlord-appointments-page">
		<!-- 页面头部 -->
		<view class="header-section">
			<view class="header-title">
				<text class="title-text">预约管理</text>
			</view>
			<view class="header-stats" v-if="appointments.length > 0">
				<view class="stat-item stat-warning">
					<text class="stat-value">{{ pendingCount }}</text>
					<text class="stat-label">待确认</text>
				</view>
				<view class="stat-item stat-info">
					<text class="stat-value">{{ appointments.length }}</text>
					<text class="stat-label">共 {{ appointments.length }} 条</text>
				</view>
			</view>
		</view>
		
		<!-- 筛选 -->
		<view class="filter-section">
			<scroll-view scroll-x class="filter-scroll" show-scrollbar="false">
				<view 
					v-for="item in statusFilters" 
					:key="item.value"
					class="filter-item"
					:class="{ 'filter-active': currentFilter === item.value }"
					@click="changeFilter(item.value)"
				>
					<text class="filter-text">{{ item.label }}</text>
					<text class="filter-count" v-if="item.count > 0">{{ item.count }}</text>
				</view>
			</scroll-view>
		</view>
		
		<!-- 搜索 -->
		<view class="search-section">
			<view class="search-box">
				<text class="search-icon">🔍</text>
				<input 
					class="search-input"
					v-model="searchKeyword"
					placeholder="搜索房源或用户"
					placeholder-class="search-placeholder"
					confirm-type="search"
					@confirm="loadAppointments"
				/>
				<text class="clear-icon" v-if="searchKeyword" @click="clearSearch">×</text>
			</view>
		</view>
		
		<!-- 预约列表 -->
		<scroll-view 
			scroll-y 
			class="list-scroll"
			:refresher-enabled="true"
			:refresher-triggered="refreshing"
			@refresherrefresh="onRefresh"
		>
			<view class="list-container">
				<view v-if="loading && appointments.length === 0" class="loading-state">
					<text class="loading-text">加载中...</text>
				</view>
				
				<view v-else-if="filteredAppointments.length === 0" class="empty-state">
					<view class="empty-icon">📋</view>
					<text class="empty-title">暂无预约</text>
					<text class="empty-desc">暂时没有租客预约您的房源</text>
				</view>
				
				<view v-else class="appointment-list">
					<view 
						v-for="item in filteredAppointments" 
						:key="item.id"
						class="appointment-card"
					>
						<!-- 卡片头部 -->
						<view class="card-header">
							<view class="status-badge" :class="'status-' + item.status">
								<text>{{ getStatusText(item.status) }}</text>
							</view>
							<text class="appointment-id">ID: {{ item.id }}</text>
						</view>
						
						<!-- 房源信息 -->
						<view class="property-section" @click="goToPropertyDetail(item.propertyId)">
							<image 
								class="property-image"
								:src="getFirstImage(item.property?.images)"
								mode="aspectFill"
							></image>
							<view class="property-detail">
								<text class="property-title">{{ item.property?.title || '未知房源' }}</text>
								<text class="property-address">{{ item.property?.address || '-' }}</text>
								<view class="property-price">
									<text class="price-value">¥{{ formatPrice(item.property?.price, item.property?.transactionType) }}</text>
									<text class="price-unit">{{ item.property?.transactionType !== 1 ? '/月' : '' }}</text>
								</view>
							</view>
						</view>
						
						<!-- 用户信息 -->
						<view class="user-section">
							<view class="user-avatar">
								<text>{{ getAvatarText(item.user?.username, item.user?.realName) }}</text>
							</view>
							<view class="user-info">
								<text class="user-name">{{ item.user?.realName || item.user?.username || '未知用户' }}</text>
								<text class="user-phone">📞 {{ item.contactPhone || '-' }}</text>
							</view>
						</view>
						
						<!-- 预约信息 -->
						<view class="appointment-info">
							<view class="info-row">
								<text class="info-label">预约时间</text>
								<text class="info-value">{{ formatDateTime(item.appointmentTime) }}</text>
							</view>
							<view class="info-row">
								<text class="info-label">提交时间</text>
								<text class="info-value">{{ formatDateTime(item.createTime) }}</text>
							</view>
							<view class="info-row" v-if="item.remark">
								<text class="info-label">备注</text>
								<text class="info-value remark">{{ item.remark }}</text>
							</view>
						</view>
						
						<!-- 操作按钮 -->
						<view class="card-actions">
							<button 
								class="action-btn btn-outline"
								@click="goToPropertyDetail(item.propertyId)"
							>
								查看房源
							</button>
							
							<button 
								v-if="item.transactionType !== null && item.transactionType !== undefined"
								class="action-btn btn-outline-primary"
								@click="viewProcess(item)"
							>
								查看流程
							</button>
							
							<button 
								v-if="item.status === 0"
								class="action-btn btn-success"
								@click="handleConfirm(item)"
							>
								确认预约
							</button>
							
							<button 
								v-if="item.status === 0"
								class="action-btn btn-danger"
								@click="handleReject(item)"
							>
								拒绝
							</button>
							
							<button 
								v-if="item.status === 1"
								class="action-btn btn-primary"
								@click="handleComplete(item)"
							>
								完成看房
							</button>
						</view>
					</view>
				</view>
			</view>
		</scroll-view>
	</view>
</template>

<script>
	import { propertyApi } from '@/api/property.js'
	
	export default {
		data() {
			return {
				appointments: [],
				loading: false,
				refreshing: false,
				searchKeyword: '',
				currentFilter: null,
				rejectItem: null,
				rejectReason: ''
			}
		},
		
		computed: {
			statusFilters() {
				const pendingCount = this.appointments.filter(item => item.status === 0).length
				const confirmedCount = this.appointments.filter(item => item.status === 1).length
				const completedCount = this.appointments.filter(item => item.status === 2).length
				const cancelledCount = this.appointments.filter(item => item.status === 3).length
				
				return [
					{ label: '全部', value: null, count: 0 },
					{ label: '待确认', value: 0, count: pendingCount },
					{ label: '已确认', value: 1, count: confirmedCount },
					{ label: '已完成', value: 2, count: completedCount },
					{ label: '已取消', value: 3, count: cancelledCount }
				]
			},
			
			pendingCount() {
				return this.appointments.filter(item => item.status === 0).length
			},
			
			filteredAppointments() {
				let list = [...this.appointments]
				
				if (this.currentFilter !== null) {
					list = list.filter(item => item.status === this.currentFilter)
				}
				
				if (this.searchKeyword.trim()) {
					const keyword = this.searchKeyword.trim().toLowerCase()
					list = list.filter(item =>
						(item.property?.title || '').toLowerCase().includes(keyword) ||
						(item.user?.username || '').toLowerCase().includes(keyword) ||
						(item.user?.realName || '').toLowerCase().includes(keyword) ||
						(item.contactPhone || '').includes(keyword)
					)
				}
				
				list.sort((a, b) => {
					return new Date(b.createTime).getTime() - new Date(a.createTime).getTime()
				})
				
				return list
			}
		},
		
		onShow() {
			this.loadAppointments()
		},
		
		methods: {
			// 加载预约列表
			async loadAppointments(showLoading = true) {
				if (showLoading) {
					this.loading = true
				}
				
				try {
					const result = await propertyApi.getLandlordAppointments()
					if (result.code === 200 && result.data) {
						this.appointments = result.data
					} else {
						uni.showToast({ title: result.message || '加载失败', icon: 'none' })
					}
				} catch (error) {
					console.error('加载预约列表失败:', error)
					uni.showToast({ title: '加载预约列表失败', icon: 'none' })
				} finally {
					this.loading = false
					this.refreshing = false
				}
			},
			
			// 下拉刷新
			onRefresh() {
				this.refreshing = true
				this.loadAppointments(false)
			},
			
			// 切换筛选
			changeFilter(value) {
				this.currentFilter = value
			},
			
			// 清空搜索
			clearSearch() {
				this.searchKeyword = ''
				this.loadAppointments()
			},
			
			// 确认预约
			handleConfirm(item) {
				uni.showModal({
					title: '确认预约',
					content: '确定要确认这个预约吗？确认后租客可继续推进交易流程',
					confirmColor: '#07c160',
					success: async (res) => {
						if (res.confirm) {
							try {
								const result = await propertyApi.confirmAppointment(item.id)
								if (result.code === 200) {
									uni.showToast({ title: '预约已确认', icon: 'success' })
									this.loadAppointments()
								} else {
									uni.showToast({ title: result.message || '确认失败', icon: 'none' })
								}
							} catch (error) {
								console.error('确认预约失败:', error)
								uni.showToast({ title: '确认预约失败', icon: 'none' })
							}
						}
					}
				})
			},
			
			// 拒绝预约
			handleReject(item) {
				this.rejectItem = item
				this.rejectReason = ''
				uni.showModal({
					title: '拒绝预约',
					content: '确定要拒绝这个预约吗？',
					editable: true,
					placeholderText: '请输入拒绝原因（选填）',
					confirmColor: '#ff4d4f',
					success: async (res) => {
						if (res.confirm) {
							try {
								const result = await propertyApi.cancelAppointment(item.id)
								if (result.code === 200) {
									uni.showToast({ title: '已拒绝', icon: 'success' })
									this.loadAppointments()
								} else {
									uni.showToast({ title: result.message || '操作失败', icon: 'none' })
								}
							} catch (error) {
								console.error('拒绝预约失败:', error)
								uni.showToast({ title: '拒绝预约失败', icon: 'none' })
							}
						}
					}
				})
			},
			
			// 完成看房
			handleComplete(item) {
				uni.showModal({
					title: '完成看房',
					content: '确认已完成看房吗？',
					confirmColor: '#1677ff',
					success: async (res) => {
						if (res.confirm) {
							try {
								const result = await propertyApi.completeAppointment(item.id)
								if (result.code === 200) {
									uni.showToast({ title: '操作成功', icon: 'success' })
									this.loadAppointments()
								} else {
									uni.showToast({ title: result.message || '操作失败', icon: 'none' })
								}
							} catch (error) {
								console.error('完成看房失败:', error)
								uni.showToast({ title: '完成看房失败', icon: 'none' })
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
			
			// 查看交易流程
			viewProcess(item) {
				uni.navigateTo({
					url: `/pages/transaction/process?id=${item.id}`
				})
			},
			
			// 获取第一张图片
			getFirstImage(images) {
				if (!images) return '/static/placeholder.png'
				const imageList = images.split(',')
				return imageList[0] || '/static/placeholder.png'
			},
			
			// 格式化价格
			formatPrice(price, transactionType) {
				if (!price) return '0'
				
				let displayPrice = price
				if (transactionType === 0 && price > 10000) {
					displayPrice = Math.round(price / 12)
				}
				
				if (displayPrice >= 10000) {
					return (displayPrice / 10000).toFixed(1) + '万'
				}
				return Math.round(displayPrice).toLocaleString()
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
			
			// 获取状态文本
			getStatusText(status) {
				const statusMap = {
					0: '待确认',
					1: '已确认',
					2: '已完成',
					3: '已取消'
				}
				return statusMap[status] || '未知'
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
	.landlord-appointments-page {
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
	
	.filter-section {
		background: $bg-color;
		padding: $spacing-base 0;
		margin-top: -30rpx;
		border-radius: $radius-xl $radius-xl 0 0;
		position: relative;
		z-index: 2;
	}
	
	.filter-scroll {
		white-space: nowrap;
		padding: 0 $spacing-base;
	}
	
	.filter-item {
		display: inline-flex;
		align-items: center;
		gap: 8rpx;
		padding: 12rpx 24rpx;
		margin-right: $spacing-sm;
		background: $bg-color-light;
		border-radius: $radius-full;
		border: 1rpx solid $border-color-light;
		transition: all $transition-base;
		
		&:active {
			transform: scale(0.95);
		}
		
		&.filter-active {
			background: $primary-gradient;
			border-color: transparent;
			
			.filter-text {
				color: $text-color-inverse;
			}
			
			.filter-count {
				background: rgba(255, 255, 255, 0.25);
				color: $text-color-inverse;
			}
		}
	}
	
	.filter-text {
		font-size: $uni-font-size-base;
		color: $text-color-regular;
		font-weight: 500;
	}
	
	.filter-count {
		font-size: 22rpx;
		color: $text-color-secondary;
		background: $border-color-light;
		padding: 2rpx 12rpx;
		border-radius: $radius-full;
		min-width: 28rpx;
		text-align: center;
	}
	
	.search-section {
		background: $bg-color;
		padding: 0 $spacing-base $spacing-base;
	}
	
	.search-box {
		display: flex;
		align-items: center;
		background: $bg-color-light;
		border-radius: $radius-full;
		padding: 16rpx $spacing-base;
		border: 1rpx solid $border-color-light;
	}
	
	.search-icon {
		font-size: 28rpx;
		margin-right: $spacing-sm;
	}
	
	.search-input {
		flex: 1;
		font-size: $uni-font-size-base;
		color: $text-color-primary;
		height: 40rpx;
	}
	
	.search-placeholder {
		color: $text-color-placeholder;
	}
	
	.clear-icon {
		font-size: 36rpx;
		color: $text-color-secondary;
		padding: 0 8rpx;
	}
	
	.list-scroll {
		flex: 1;
		height: 0;
		padding: $spacing-base;
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
	
	.appointment-list {
		display: flex;
		flex-direction: column;
		gap: $spacing-base;
	}
	
	.appointment-card {
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
	}
	
	.card-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: $spacing-sm;
	}
	
	.status-badge {
		padding: 6rpx 16rpx;
		border-radius: $radius-full;
		font-size: 22rpx;
		font-weight: 500;
		
		&.status-0 {
			background: $warning-color-light;
			color: $warning-color;
		}
		
		&.status-1 {
			background: $success-color-light;
			color: $success-color;
		}
		
		&.status-2 {
			background: $info-color-light;
			color: $info-color;
		}
		
		&.status-3 {
			background: $error-color-light;
			color: $error-color;
		}
	}
	
	.appointment-id {
		font-size: 22rpx;
		color: $text-color-secondary;
	}
	
	.property-section {
		display: flex;
		gap: $spacing-sm;
		padding-bottom: $spacing-base;
		border-bottom: 1rpx solid $border-color-light;
		margin-bottom: $spacing-base;
	}
	
	.property-image {
		width: 140rpx;
		height: 100rpx;
		border-radius: $radius-base;
		flex-shrink: 0;
		background: $bg-color-light;
	}
	
	.property-detail {
		flex: 1;
		min-width: 0;
		display: flex;
		flex-direction: column;
		justify-content: space-between;
	}
	
	.property-title {
		font-size: $uni-font-size-base;
		color: $text-color-primary;
		font-weight: 600;
		line-height: 1.4;
		overflow: hidden;
		text-overflow: ellipsis;
		display: -webkit-box;
		-webkit-line-clamp: 2;
		-webkit-box-orient: vertical;
	}
	
	.property-address {
		font-size: 22rpx;
		color: $text-color-secondary;
		margin-top: 6rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}
	
	.property-price {
		display: flex;
		align-items: baseline;
		margin-top: 6rpx;
	}
	
	.price-value {
		font-size: 30rpx;
		font-weight: 700;
		color: $price-color;
	}
	
	.price-unit {
		font-size: 22rpx;
		color: $text-color-secondary;
		margin-left: 4rpx;
	}
	
	.user-section {
		display: flex;
		align-items: center;
		gap: $spacing-sm;
		margin-bottom: $spacing-base;
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
	
	.user-info {
		flex: 1;
		display: flex;
		flex-direction: column;
	}
	
	.user-name {
		font-size: $uni-font-size-base;
		color: $text-color-primary;
		font-weight: 600;
	}
	
	.user-phone {
		font-size: 24rpx;
		color: $text-color-secondary;
		margin-top: 4rpx;
	}
	
	.appointment-info {
		background: $bg-color-light;
		border-radius: $radius-lg;
		padding: $spacing-sm $spacing-base;
		margin-bottom: $spacing-base;
	}
	
	.info-row {
		display: flex;
		justify-content: space-between;
		align-items: flex-start;
		padding: 10rpx 0;
		border-bottom: 1rpx dashed $border-color-light;
		
		&:last-child {
			border-bottom: none;
		}
	}
	
	.info-label {
		font-size: 24rpx;
		color: $text-color-secondary;
		flex-shrink: 0;
		margin-right: $spacing-sm;
	}
	
	.info-value {
		font-size: 24rpx;
		color: $text-color-primary;
		font-weight: 500;
		text-align: right;
		flex: 1;
		word-break: break-all;
		
		&.remark {
			color: $text-color-regular;
		}
	}
	
	.card-actions {
		display: flex;
		flex-wrap: wrap;
		gap: $spacing-sm;
	}
	
	.action-btn {
		flex: 1;
		min-width: 140rpx;
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
	
	.btn-outline {
		background: $bg-color;
		color: $text-color-regular;
		border: 1rpx solid $border-color;
		
		&:active {
			background: $bg-color-light;
		}
	}
	
	.btn-outline-primary {
		background: rgba($primary-color, 0.06);
		color: $primary-color;
		border: 1rpx solid rgba($primary-color, 0.3);
		
		&:active {
			background: rgba($primary-color, 0.12);
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
	
	.btn-primary {
		background: $primary-gradient;
		color: $text-color-inverse;
		border: none;
		box-shadow: 0 6rpx 20rpx rgba($primary-color, 0.35);
		
		&:active {
			box-shadow: 0 4rpx 12rpx rgba($primary-color, 0.25);
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
