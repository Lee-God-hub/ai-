<template>
	<view class="manage-page">
		<!-- 页面头部 -->
		<view class="page-header">
			<view class="header-content">
				<view class="title-section">
					<text class="title-icon">🏠</text>
					<text class="page-title">房源管理</text>
				</view>
				<button class="publish-btn" @click="goToPublish">
					发布房源
				</button>
			</view>
		</view>
		
		<!-- 统计信息 -->
		<view class="stats-section">
			<view class="stats-grid">
				<view class="stat-item">
					<view class="stat-number">{{ totalCount }}</view>
					<view class="stat-label">总房源</view>
				</view>
				<view class="stat-item">
					<view class="stat-number">{{ onlineCount }}</view>
					<view class="stat-label">在线</view>
				</view>
				<view class="stat-item">
					<view class="stat-number">{{ offlineCount }}</view>
					<view class="stat-label">下线</view>
				</view>
				<view class="stat-item">
					<view class="stat-number">{{ viewCount }}</view>
					<view class="stat-label">总浏览</view>
				</view>
			</view>
		</view>
		
		<!-- 房源列表 -->
		<view class="property-list">
			<!-- 加载状态 -->
			<view v-if="loading && properties.length === 0" class="loading-container">
				<text class="loading-text">加载中...</text>
			</view>
			
			<!-- 空状态 -->
			<view v-else-if="properties.length === 0" class="empty-container">
				<text class="empty-icon">🏠</text>
				<text class="empty-text">暂无房源</text>
				<view class="empty-tip">
					<text>快去发布您的第一套房源吧</text>
					<button class="publish-btn-empty" @click="goToPublish">
						立即发布
					</button>
				</view>
			</view>
			
			<!-- 房源卡片 -->
			<view v-else class="property-cards">
				<view 
					v-for="property in properties"
					:key="property.id"
					class="property-card"
				>
					<!-- 房源图片 -->
					<view class="property-image" @click="goToDetail(property.id)">
						<image 
							:src="getFirstImage(property.images)"
							mode="aspectFill"
							class="image"
							@error="onImageError($event, property)"
						></image>
						
						<!-- 状态标签 -->
						<view :class="['status-badge', property.status === 1 ? 'online' : 'offline']">
							<text>{{ property.status === 1 ? '在线' : '下线' }}</text>
						</view>
						<view v-if="usingMockData" class="mock-badge">
							<text>示例</text>
						</view>
					</view>
					
					<!-- 房源信息 -->
					<view class="property-info" @click="goToDetail(property.id)">
						<view class="property-title">{{ property.title }}</view>
						
						<view class="property-price">
							<text class="price">¥{{ formatPrice(property.price) }}</text>
							<text class="price-unit" v-if="property.priceType === 0">/月</text>
						</view>
						
						<view class="property-details">
							<text class="detail-tag">{{ property.area }}㎡</text>
							<text class="detail-tag">
								{{ property.rooms || property.bedrooms || 0 }}室{{ property.halls || 1 }}厅
							</text>
							<text 
								v-if="property.bathrooms"
								class="detail-tag"
							>{{ property.bathrooms }}卫</text>
						</view>
						
						<view v-if="property.district || property.address" class="property-address">
							<text class="address-text">{{ property.city || '' }}{{ property.district || '' }} {{ property.address || '' }}</text>
						</view>
						
						<view class="property-stats">
							<view class="stat-item">
								<text class="stat-icon">👁️</text>
								<text>{{ property.viewCount || 0 }}</text>
							</view>
							<view class="stat-item">
								<text class="stat-icon">❤️</text>
								<text>{{ property.favoriteCount || 0 }}</text>
							</view>
							<view class="stat-item">
								<text class="stat-icon">📅</text>
								<text>{{ formatDate(property.createTime) }}</text>
							</view>
						</view>
					</view>
					
					<!-- 操作按钮 -->
					<view class="property-actions">
						<button class="action-btn edit-btn" @click="editProperty(property)">
							编辑
						</button>
						<button 
							:class="['action-btn', property.status === 1 ? 'offline-btn' : 'online-btn']"
							@click="toggleStatus(property)"
						>
							{{ property.status === 1 ? '下线' : '上线' }}
						</button>
						<button class="action-btn delete-btn" @click="deleteProperty(property)">
							删除
						</button>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	import { propertyApi } from '@/api/property.js'
	import { crawlerProperties } from '@/mock/crawlerData.js'
	
	export default {
		data() {
			return {
				properties: [],
				loading: false,
				usingMockData: false,
				
				totalCount: 0,
				onlineCount: 0,
				offlineCount: 0,
				viewCount: 0
			}
		},
		
		onLoad() {
			// 清除旧的缓存数据，避免使用旧的AI图片URL
			uni.removeStorageSync('myProperties')
			this.loadProperties()
			this.loadStats()
		},
		
		onShow() {
			// 从发布页面返回时刷新列表
			this.refreshData()
		},
		
		onPullDownRefresh() {
			this.refreshData()
		},
		
		methods: {
			async loadProperties() {
				this.loading = true
				
				try {
					const response = await propertyApi.getMyList()
					
					if (response.code === 200 && response.data && response.data.length > 0) {
						this.properties = response.data
						this.usingMockData = false
						this.calculateStats()
						return
					}
				} catch (error) {
					console.log('API加载我的房源失败，使用示例数据:', error)
				}
				
				this.usingMockData = true
				const myProperties = uni.getStorageSync('myProperties')
				if (myProperties) {
					try {
						this.properties = JSON.parse(myProperties)
					} catch (e) {
						this.properties = []
					}
				}
				
				if (this.properties.length === 0) {
					this.properties = crawlerProperties.slice(0, 3).map((p, index) => ({
						...p,
						id: 'my_' + p.id,
						status: index === 0 ? 1 : (index === 1 ? 1 : 0),
						viewCount: Math.floor(Math.random() * 500) + 50,
						favoriteCount: Math.floor(Math.random() * 50) + 5,
						createTime: new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000).toISOString()
					}))
				}
				
				this.calculateStats()
				this.loading = false
				uni.stopPullDownRefresh()
			},
			
			calculateStats() {
				this.totalCount = this.properties.length
				this.onlineCount = this.properties.filter(p => p.status === 1).length
				this.offlineCount = this.properties.filter(p => p.status === 0).length
				this.viewCount = this.properties.reduce((sum, p) => sum + (p.viewCount || 0), 0)
			},
			
			async loadStats() {
				this.calculateStats()
			},
			
			refreshData() {
				this.properties = []
				this.loadProperties()
			},
			
			// 跳转发布页面
			goToPublish() {
				uni.navigateTo({
					url: '/pages/landlord/publish'
				})
			},
			
			// 跳转房源详情
			goToDetail(id) {
				uni.navigateTo({
					url: `/pages/property/detail?id=${id}`
				})
			},
			
			// 编辑房源
			editProperty(property) {
				uni.navigateTo({
					url: `/pages/landlord/publish?id=${property.id}&mode=edit`
				})
			},
			
			async toggleStatus(property) {
				const action = property.status === 1 ? '下架' : '上架'
				
				uni.showModal({
					title: '提示',
					content: `确定要${action}这套房源吗？`,
					success: async (res) => {
						if (res.confirm) {
							if (this.usingMockData) {
								const newStatus = property.status === 1 ? 0 : 1
								property.status = newStatus
								if (newStatus === 1) {
									this.onlineCount++
									this.offlineCount--
								} else {
									this.onlineCount--
									this.offlineCount++
								}
								this.saveMockProperties()
								uni.showToast({ title: `${action}成功`, icon: 'success' })
								return
							}
							
							try {
								let response
								if (property.status === 1) {
									response = await propertyApi.offlineProperty(property.id)
								} else {
									response = await propertyApi.onlineProperty(property.id)
								}
								
								if (response.code === 200) {
									const newStatus = property.status === 1 ? 0 : 1
									property.status = newStatus
									this.calculateStats()
									uni.showToast({ title: `${action}成功`, icon: 'success' })
								} else {
									uni.showToast({ title: response.message || `${action}失败`, icon: 'none' })
								}
							} catch (error) {
								console.error(`${action}房源失败:`, error)
								const newStatus = property.status === 1 ? 0 : 1
								property.status = newStatus
								this.calculateStats()
								this.saveMockProperties()
								uni.showToast({ title: `${action}成功（本地）`, icon: 'success' })
							}
						}
					}
				})
			},
			
			deleteProperty(property) {
				uni.showModal({
					title: '提示',
					content: '确定要删除这套房源吗？删除后无法恢复。',
					success: async (res) => {
						if (res.confirm) {
							if (this.usingMockData) {
								const index = this.properties.findIndex(p => p.id === property.id)
								if (index > -1) {
									this.properties.splice(index, 1)
								}
								this.calculateStats()
								this.saveMockProperties()
								uni.showToast({ title: '删除成功', icon: 'success' })
								return
							}
							
							try {
								const response = await propertyApi.deleteProperty(property.id)
								
								if (response.code === 200) {
									const index = this.properties.findIndex(p => p.id === property.id)
									if (index > -1) {
										this.properties.splice(index, 1)
									}
									this.calculateStats()
									uni.showToast({ title: '删除成功', icon: 'success' })
								} else {
									uni.showToast({ title: response.message || '删除失败', icon: 'none' })
								}
							} catch (error) {
								console.error('删除房源失败:', error)
								const index = this.properties.findIndex(p => p.id === property.id)
								if (index > -1) {
									this.properties.splice(index, 1)
								}
								this.calculateStats()
								this.saveMockProperties()
								uni.showToast({ title: '删除成功（本地）', icon: 'success' })
							}
						}
					}
				})
			},
			
			saveMockProperties() {
				if (this.usingMockData) {
					uni.setStorageSync('myProperties', JSON.stringify(this.properties))
				}
			},
			
			getFirstImage(images) {
				if (!images) return '/static/placeholder.png'
				if (Array.isArray(images)) {
					return images[0] || '/static/placeholder.png'
				}
				const imageList = images.split(',')
				return imageList[0] || '/static/placeholder.png'
			},
			
			onImageError(e, property) {
				property.images = ['/static/placeholder.png']
				property.coverImage = '/static/placeholder.png'
			},
			
			// 格式化价格
			formatPrice(price) {
				if (!price) return '0'
				if (price >= 10000) {
					return (price / 10000).toFixed(1) + '万'
				}
				return price.toLocaleString()
			},
			
			// 格式化日期
			formatDate(date) {
				const d = new Date(date)
				return `${d.getMonth() + 1}/${d.getDate()}`
			}
		}
	}
</script>

<style lang="scss" scoped>
	.manage-page {
		background: $bg-color-page;
		min-height: 100vh;
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
			top: -80rpx;
			right: -50rpx;
			width: 240rpx;
			height: 240rpx;
			background: rgba(255, 255, 255, 0.1);
			border-radius: 50%;
		}
		
		&::after {
			content: '';
			position: absolute;
			bottom: -50rpx;
			left: -30rpx;
			width: 160rpx;
			height: 160rpx;
			background: rgba(255, 255, 255, 0.06);
			border-radius: 50%;
		}
	}
	
	.header-content {
		display: flex;
		justify-content: space-between;
		align-items: center;
		position: relative;
		z-index: 2;
	}
	
	.title-section {
		display: flex;
		align-items: center;
		gap: $spacing-sm;
	}
	
	.title-icon {
		font-size: 32rpx;
	}
	
	.page-title {
		font-size: 40rpx;
		font-weight: 700;
		color: $text-color-inverse;
	}
	
	.publish-btn {
		background: $secondary-gradient;
		color: $text-color-inverse;
		border: none;
		border-radius: $radius-full;
		padding: 14rpx 28rpx;
		font-size: 26rpx;
		font-weight: 500;
		box-shadow: 0 4rpx 12rpx rgba($secondary-color, 0.35);
		transition: all $transition-base;
		
		&:active {
			transform: scale(0.96);
		}
	}
	
	.stats-section {
		padding: 0 $spacing-base;
		margin-top: -20rpx;
		position: relative;
		z-index: 3;
	}
	
	.stats-grid {
		display: grid;
		grid-template-columns: repeat(4, 1fr);
		gap: $spacing-xs;
		background: $bg-color;
		border-radius: $radius-xl;
		padding: $spacing-base $spacing-xs;
		box-shadow: $shadow-base;
		border: 1rpx solid $border-color-light;
	}
	
	.stats-grid .stat-item {
		text-align: center;
	}
	
	.stat-number {
		font-size: 36rpx;
		font-weight: 700;
		color: $primary-color;
		margin-bottom: 6rpx;
	}
	
	.stat-label {
		font-size: 22rpx;
		color: $text-color-secondary;
	}
	
	.property-list {
		padding: $spacing-base;
	}
	
	.loading-container {
		display: flex;
		justify-content: center;
		padding: 120rpx $spacing-base;
	}
	
	.loading-text {
		font-size: $uni-font-size-base;
		color: $text-color-secondary;
	}
	
	.empty-container {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 100rpx $spacing-base;
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
		font-size: $uni-font-size-lg;
		color: $text-color-primary;
		font-weight: 600;
		margin-bottom: $spacing-xs;
	}
	
	.empty-tip {
		text-align: center;
		font-size: $uni-font-size-base;
		color: $text-color-secondary;
		line-height: 1.6;
	}
	
	.publish-btn-empty {
		background: $primary-gradient;
		color: $text-color-inverse;
		border: none;
		border-radius: $radius-full;
		padding: 18rpx 48rpx;
		font-size: $uni-font-size-base;
		font-weight: 500;
		margin-top: $spacing-lg;
		box-shadow: 0 6rpx 20rpx rgba($primary-color, 0.35);
		transition: all $transition-base;
		
		&:active {
			transform: scale(0.96);
		}
	}
	
	.property-cards {
		display: flex;
		flex-direction: column;
		gap: $spacing-base;
	}
	
	.property-card {
		background: $bg-color;
		border-radius: $radius-xl;
		overflow: hidden;
		box-shadow: $shadow-sm;
		border: 1rpx solid $border-color-light;
		transition: all $transition-base;
		
		&:active {
			transform: translateY(4rpx);
			box-shadow: $shadow-xs;
		}
	}
	
	.property-image {
		position: relative;
		width: 100%;
		height: 360rpx;
		background: $bg-color-light;
	}
	
	.image {
		width: 100%;
		height: 100%;
	}
	
	.status-badge {
		position: absolute;
		top: $spacing-base;
		left: $spacing-base;
		padding: 8rpx 20rpx;
		border-radius: $radius-full;
		font-size: 22rpx;
		font-weight: 500;
		color: $text-color-inverse;
		z-index: 2;
		box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.15);
		
		&.online {
			background: $success-color;
		}
		
		&.offline {
			background: $info-color;
		}
	}
	
	.mock-badge {
		position: absolute;
		top: $spacing-base;
		right: $spacing-base;
		padding: 6rpx 16rpx;
		border-radius: $radius-sm;
		font-size: 20rpx;
		background: $warning-color;
		color: $text-color-inverse;
		z-index: 2;
		font-weight: 500;
	}
	
	.property-info {
		padding: $spacing-base;
	}
	
	.property-title {
		font-size: 30rpx;
		font-weight: 600;
		color: $text-color-primary;
		margin-bottom: $spacing-xs;
		line-height: 1.4;
		overflow: hidden;
		text-overflow: ellipsis;
		display: -webkit-box;
		-webkit-line-clamp: 1;
		-webkit-box-orient: vertical;
	}
	
	.property-price {
		margin-bottom: $spacing-xs;
		display: flex;
		align-items: baseline;
	}
	
	.price {
		font-size: 36rpx;
		font-weight: 700;
		color: $secondary-color;
	}
	
	.price-unit {
		font-size: 24rpx;
		color: $text-color-secondary;
		margin-left: 8rpx;
	}
	
	.property-details {
		display: flex;
		gap: 12rpx;
		margin-bottom: $spacing-xs;
		flex-wrap: wrap;
	}
	
	.detail-tag {
		background: rgba($primary-color, 0.06);
		color: $primary-color;
		padding: 6rpx 14rpx;
		border-radius: $radius-sm;
		font-size: 22rpx;
		font-weight: 500;
	}
	
	.property-address {
		margin-bottom: $spacing-sm;
	}
	
	.address-text {
		font-size: 24rpx;
		color: $text-color-secondary;
		line-height: 1.4;
	}
	
	.property-stats {
		display: flex;
		gap: $spacing-lg;
		padding-top: $spacing-sm;
		border-top: 1rpx solid $border-color-light;
	}
	
	.property-stats .stat-item {
		display: flex;
		align-items: center;
		gap: 6rpx;
		font-size: 22rpx;
		color: $text-color-secondary;
	}
	
	.stat-icon {
		font-size: 22rpx;
	}
	
	.property-actions {
		display: flex;
		gap: $spacing-xs;
		padding: $spacing-sm $spacing-base;
		border-top: 1rpx solid $border-color-light;
		background: $bg-color-light;
	}
	
	.action-btn {
		flex: 1;
		padding: 16rpx;
		border: none;
		border-radius: $radius-lg;
		font-size: 24rpx;
		font-weight: 500;
		transition: all $transition-base;
		
		&:active {
			transform: scale(0.96);
		}
	}
	
	.edit-btn {
		background: $bg-color;
		color: $primary-color;
		border: 1rpx solid $primary-color;
	}
	
	.online-btn {
		background: $success-color;
		color: $text-color-inverse;
		box-shadow: 0 2rpx 8rpx rgba($success-color, 0.3);
	}
	
	.offline-btn {
		background: $warning-color;
		color: $text-color-inverse;
		box-shadow: 0 2rpx 8rpx rgba($warning-color, 0.3);
	}
	
	.delete-btn {
		background: $bg-color;
		color: $error-color;
		border: 1rpx solid rgba($error-color, 0.3);
	}
</style>