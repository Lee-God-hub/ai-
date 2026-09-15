<template>
	<view class="appointment-list-page">
		<!-- 页面头部 -->
		<view class="header-section">
			<view class="header-title">
				<text class="title-text">我的预约</text>
				<text class="total-text" v-if="appointments.length > 0">共 {{ appointments.length }} 条</text>
			</view>
		</view>
		
		<!-- 状态筛选 -->
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
		
		<!-- 预约列表 -->
		<scroll-view 
			scroll-y 
			class="list-scroll"
			:refresher-enabled="true"
			:refresher-triggered="refreshing"
			@refresherrefresh="onRefresh"
			@scrolltolower="loadMore"
		>
			<view class="list-container">
				<view v-if="loading && appointments.length === 0" class="loading-state">
					<text class="loading-text">加载中...</text>
				</view>
				
				<view v-else-if="filteredAppointments.length === 0" class="empty-state">
					<view class="empty-icon">📅</view>
					<text class="empty-title">暂无预约记录</text>
					<text class="empty-desc">去浏览房源，预约看房吧</text>
					<button class="empty-btn" @click="goToPropertyList">去浏览房源</button>
				</view>
				
				<view v-else class="appointment-list">
					<view 
						v-for="item in filteredAppointments" 
						:key="item.id"
						class="appointment-card card-touch"
					>
						<!-- 卡片头部 -->
						<view class="card-header">
							<view class="property-info" @click="goToPropertyDetail(item.propertyId)">
								<image 
									class="property-image"
									:src="getFirstImage(item.property?.images)"
									mode="aspectFill"
								></image>
								<view class="property-detail">
									<text class="property-title">{{ item.property?.title || '房源信息' }}</text>
									<text class="property-address">{{ item.property?.address || '-' }}</text>
									<view class="property-price">
										<text class="price-value">¥{{ formatPrice(item.property?.price, item.property?.transactionType) }}</text>
										<text class="price-unit">{{ item.property?.transactionType !== 1 ? '/月' : '' }}</text>
									</view>
								</view>
							</view>
							<view class="status-badge" :class="'status-' + item.status">
								<text>{{ getStatusText(item.status) }}</text>
							</view>
						</view>
						
						<!-- 预约信息 -->
						<view class="appointment-info">
							<view class="info-row">
								<text class="info-label">预约时间</text>
								<text class="info-value">{{ formatDateTime(item.appointmentTime) }}</text>
							</view>
							<view class="info-row">
								<text class="info-label">联系电话</text>
								<text class="info-value">{{ item.contactPhone || '-' }}</text>
							</view>
							<view class="info-row" v-if="item.remark">
								<text class="info-label">备注</text>
								<text class="info-value remark">{{ item.remark }}</text>
							</view>
							<view class="info-row">
								<text class="info-label">提交时间</text>
								<text class="info-value">{{ formatDateTime(item.createTime) }}</text>
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
								class="action-btn btn-warning"
								@click="handleCancel(item)"
							>
								取消预约
							</button>
							
							<button 
								v-if="item.status === 1"
								class="action-btn btn-success"
								@click="handleComplete(item)"
							>
								完成看房
							</button>
							
							<button 
								v-if="item.status === 2 || item.status === 3"
								class="action-btn btn-danger"
								@click="handleDelete(item)"
							>
								删除记录
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
				currentFilter: null
			}
		},
		
		computed: {
			statusFilters() {
				const allCount = this.appointments.length
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
			
			filteredAppointments() {
				let list = [...this.appointments]
				
				if (this.currentFilter !== null) {
					list = list.filter(item => item.status === this.currentFilter)
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
					const result = await propertyApi.getMyAppointments()
					if (result.code === 200 && result.data) {
						this.appointments = result.data
						this.syncLocalCount()
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
			
			// 同步本地计数（用于个人中心展示）
			syncLocalCount() {
				try {
					const localAppointments = this.appointments.map(item => ({
						id: item.id,
						propertyId: item.propertyId,
						appointmentTime: item.appointmentTime,
						status: item.status,
						createTime: item.createTime
					}))
					uni.setStorageSync('appointments', JSON.stringify(localAppointments))
				} catch (e) {
					console.error('同步预约本地计数失败:', e)
				}
			},
			
			// 下拉刷新
			onRefresh() {
				this.refreshing = true
				this.loadAppointments(false)
			},
			
			// 加载更多（后端接口本身不分页，这里预留）
			loadMore() {
				// 当前后端接口一次性返回全部，无需分页加载
			},
			
			// 切换筛选
			changeFilter(value) {
				this.currentFilter = value
			},
			
			// 取消预约
			handleCancel(item) {
				uni.showModal({
					title: '取消预约',
					content: '确定要取消这个预约吗？',
					confirmColor: '#ff9500',
					success: async (res) => {
						if (res.confirm) {
							try {
								const result = await propertyApi.cancelAppointment(item.id)
								if (result.code === 200) {
									uni.showToast({ title: '取消成功', icon: 'success' })
									this.loadAppointments()
								} else {
									uni.showToast({ title: result.message || '取消失败', icon: 'none' })
								}
							} catch (error) {
								console.error('取消预约失败:', error)
								uni.showToast({ title: '取消预约失败', icon: 'none' })
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
					confirmColor: '#07c160',
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
			
			// 删除记录
			handleDelete(item) {
				uni.showModal({
					title: '删除记录',
					content: '确定要删除这条预约记录吗？',
					confirmColor: '#ff4d4f',
					success: async (res) => {
						if (res.confirm) {
							try {
								const result = await propertyApi.cancelAppointment(item.id)
								if (result.code === 200) {
									uni.showToast({ title: '删除成功', icon: 'success' })
									this.loadAppointments()
								} else {
									uni.showToast({ title: result.message || '删除失败', icon: 'none' })
								}
							} catch (error) {
								console.error('删除记录失败:', error)
								uni.showToast({ title: '删除记录失败', icon: 'none' })
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
			
			// 去房源列表
			goToPropertyList() {
				uni.switchTab({
					url: '/pages/index/index'
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
			}
		}
	}
</script>

<style lang="scss" scoped>
	.appointment-list-page {
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
	}
	
	.title-text {
		font-size: 40rpx;
		font-weight: 700;
		color: $text-color-inverse;
	}
	
	.total-text {
		font-size: $uni-font-size-sm;
		color: rgba(255, 255, 255, 0.8);
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
		margin-bottom: $spacing-lg;
	}
	
	.empty-btn {
		background: $primary-gradient;
		color: $text-color-inverse;
		font-size: $uni-font-size-base;
		padding: 20rpx 48rpx;
		border-radius: $radius-full;
		border: none;
		font-weight: 500;
		box-shadow: 0 6rpx 20rpx rgba($primary-color, 0.35);
		transition: all $transition-base;
		
		&:active {
			transform: scale(0.98);
		}
		
		&::after {
			border: none;
		}
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
		align-items: flex-start;
		margin-bottom: $spacing-base;
	}
	
	.property-info {
		display: flex;
		gap: $spacing-sm;
		flex: 1;
		min-width: 0;
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
	
	.status-badge {
		flex-shrink: 0;
		padding: 6rpx 16rpx;
		border-radius: $radius-full;
		font-size: 22rpx;
		font-weight: 500;
		margin-left: $spacing-sm;
		
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
	
	.btn-warning {
		background: $warning-color-light;
		color: $warning-color;
		border: 1rpx solid rgba($warning-color, 0.3);
		
		&:active {
			background: rgba($warning-color, 0.2);
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
