<template>
	<view class="favorites-page">
		<!-- 页面头部 -->
		<view class="page-header">
			<view class="header-content">
				<view class="title-section">
					<text class="title-icon">❤️</text>
					<text class="page-title">我的收藏</text>
				</view>
				<text class="count-tag">共{{ totalCount }}套</text>
			</view>
		</view>
		
		<!-- 收藏列表 -->
		<view class="favorites-content">
			<!-- 加载状态 -->
			<view v-if="loading && favorites.length === 0" class="loading-container">
				<text class="loading-text">加载中...</text>
			</view>
			
			<!-- 空状态 -->
			<view v-else-if="favorites.length === 0" class="empty-container">
				<text class="empty-icon">💔</text>
				<text class="empty-text">暂无收藏房源</text>
				<view class="empty-tip">
					<text>快去收藏心仪的房源吧</text>
					<button class="browse-btn" @click="goToBrowse">
						去看房源
					</button>
				</view>
			</view>
			
			<!-- 房源列表 -->
			<view v-else class="property-list">
				<view 
					v-for="item in favorites"
					:key="item.id"
					class="property-card"
					@click="goToDetail(item.property.id)"
				>
					<!-- 房源图片 -->
					<view class="property-image">
						<image 
							:src="getFirstImage(item.property.images)"
							mode="aspectFill"
							class="image"
							@error="onImageError($event, item)"
						></image>
						
						<!-- 收藏时间标签 -->
						<view class="favorite-time">
							<text>{{ formatTime(item.createTime) }}</text>
						</view>
						<text v-if="item.property.source" class="source-tag">{{ item.property.source }}</text>
					</view>
					
					<!-- 房源信息 -->
					<view class="property-info">
						<view class="property-title">{{ item.property.title }}</view>
						
						<view class="property-price">
							<text class="price">¥{{ formatPrice(item.property.price) }}</text>
							<text class="price-unit">/月</text>
						</view>
						
						<view class="property-details">
							<text class="detail-tag">{{ item.property.area }}㎡</text>
							<text class="detail-tag">{{ item.property.rooms || item.property.bedrooms || 0 }}室{{ item.property.halls || 1 }}厅</text>
						</view>
						
						<view class="property-address">
							<text class="location-icon">📍</text>
							<text>{{ item.property.city }} {{ item.property.district }}</text>
						</view>
					</view>
					
					<!-- 操作按钮 -->
					<view class="property-actions">
						<text 
							class="favorite-icon"
							@click.stop="removeFavorite(item)"
						>
							❤️
						</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	import { propertyApi } from '@/api/property.js'
	import { aiApi } from '@/api/ai.js'
	
	export default {
		data() {
			return {
				favorites: [],
				loading: false,
				totalCount: 0
			}
		},
		
		onLoad() {
			this.loadFavorites()
		},
		
		onShow() {
			this.loadFavorites()
		},
		
		onPullDownRefresh() {
			this.refreshData()
		},
		
		methods: {
			// 加载收藏列表
			async loadFavorites() {
				this.loading = true
				
				try {
					const response = await propertyApi.getFavoriteList()
					
					if (response.code === 200 && response.data) {
						this.favorites = response.data
						this.totalCount = this.favorites.length
					} else {
						this.favorites = []
						this.totalCount = 0
					}
				} catch (error) {
					console.error('加载收藏列表失败:', error)
					this.favorites = []
					this.totalCount = 0
				} finally {
					this.loading = false
					uni.stopPullDownRefresh()
				}
			},
			
			// 刷新数据
			refreshData() {
				this.favorites = []
				this.loadFavorites()
			},
			
			// 跳转房源详情
			async goToDetail(id) {
				try {
					await aiApi.recordUserBehavior(id, 'view')
				} catch (error) {
					console.error('记录浏览行为失败:', error)
				}
				
				uni.navigateTo({
					url: `/pages/property/detail?id=${id}`
				})
			},
			
			// 取消收藏
			async removeFavorite(item) {
				uni.showModal({
					title: '提示',
					content: '确定要取消收藏这套房源吗？',
					success: async (res) => {
						if (res.confirm) {
							try {
								await propertyApi.unfavoriteProperty(item.property.id)
								await aiApi.recordUserBehavior(item.property.id, 'unfavorite')
								
								// 从列表中移除
								const index = this.favorites.findIndex(f => f.id === item.id)
								if (index > -1) {
									this.favorites.splice(index, 1)
									this.totalCount--
								}
								
								uni.showToast({
									title: '已取消收藏',
									icon: 'success'
								})
							} catch (error) {
								console.error('取消收藏失败:', error)
								uni.showToast({
									title: '操作失败',
									icon: 'none'
								})
							}
						}
					}
				})
			},
			
			// 去浏览房源
			goToBrowse() {
				uni.switchTab({
					url: '/pages/search/search'
				})
			},
			
			// 获取第一张图片，兼容数组和逗号分隔字符串
			getFirstImage(images) {
				if (!images) return '/static/placeholder.png'
				if (Array.isArray(images)) {
					return images[0] || '/static/placeholder.png'
				}
				const imageList = images.split(',')
				return imageList[0] || '/static/placeholder.png'
			},
			
			// 图片加载错误处理
			onImageError(e, item) {
				if (item && item.property) {
					item.property.images = ['/static/placeholder.png']
					item.property.coverImage = '/static/placeholder.png'
				}
			},
			
			// 格式化价格
			formatPrice(price) {
				if (!price) return '0'
				if (price >= 10000) {
					return (price / 10000).toFixed(1) + '万'
				}
				return price.toLocaleString()
			},
			
			// 格式化时间
			formatTime(time) {
				if (!time) return ''
				const date = new Date(time)
				const now = new Date()
				const diff = now - date
				
				if (diff < 60000) {
					return '刚刚'
				} else if (diff < 3600000) {
					return Math.floor(diff / 60000) + '分钟前'
				} else if (diff < 86400000) {
					return Math.floor(diff / 3600000) + '小时前'
				} else if (diff < 2592000000) {
					return Math.floor(diff / 86400000) + '天前'
				} else {
					return date.toLocaleDateString()
				}
			}
		}
	}
</script>

<style lang="scss" scoped>
	.favorites-page {
		background: $bg-color-page;
		min-height: 100vh;
		padding-bottom: env(safe-area-inset-bottom);
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
			right: -40rpx;
			width: 240rpx;
			height: 240rpx;
			background: rgba(255, 255, 255, 0.1);
			border-radius: 50%;
		}
		
		&::after {
			content: '';
			position: absolute;
			bottom: -60rpx;
			left: -30rpx;
			width: 180rpx;
			height: 180rpx;
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
		gap: 16rpx;
	}
	
	.title-icon {
		font-size: 40rpx;
	}
	
	.page-title {
		font-size: 40rpx;
		font-weight: 700;
		color: $text-color-inverse;
	}
	
	.count-tag {
		background: rgba(255, 255, 255, 0.2);
		color: $text-color-inverse;
		padding: 8rpx 20rpx;
		border-radius: $radius-full;
		font-size: 24rpx;
		font-weight: 500;
		backdrop-filter: blur(10rpx);
	}
	
	.favorites-content {
		padding: $spacing-base;
		margin-top: -30rpx;
		position: relative;
		z-index: 3;
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
		padding: 120rpx $spacing-base;
		background: $bg-color;
		border-radius: $radius-xl;
		box-shadow: $shadow-sm;
	}
	
	.empty-icon {
		font-size: 120rpx;
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
		margin-bottom: $spacing-lg;
	}
	
	.browse-btn {
		background: $primary-gradient;
		color: $text-color-inverse;
		border: none;
		border-radius: $radius-full;
		padding: 20rpx 48rpx;
		font-size: $uni-font-size-base;
		font-weight: 500;
		box-shadow: 0 6rpx 20rpx rgba($primary-color, 0.35);
		transition: all $transition-base;
		
		&:active {
			transform: scale(0.96);
			box-shadow: 0 3rpx 12rpx rgba($primary-color, 0.25);
		}
	}
	
	.property-list {
		display: flex;
		flex-direction: column;
		gap: $spacing-base;
	}
	
	.property-card {
		background: $bg-color;
		border-radius: $radius-xl;
		padding: $spacing-base;
		display: flex;
		gap: $spacing-base;
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
		width: 220rpx;
		height: 170rpx;
		border-radius: $radius-lg;
		overflow: hidden;
		flex-shrink: 0;
		background: $bg-color-light;
	}
	
	.image {
		width: 100%;
		height: 100%;
	}
	
	.favorite-time {
		position: absolute;
		bottom: 12rpx;
		left: 12rpx;
		background: rgba(0, 0, 0, 0.55);
		backdrop-filter: blur(8rpx);
		color: $text-color-inverse;
		padding: 6rpx 12rpx;
		border-radius: $radius-sm;
		font-size: 20rpx;
	}
	
	.source-tag {
		position: absolute;
		top: 12rpx;
		right: 12rpx;
		background: $secondary-gradient;
		color: #fff;
		font-size: 20rpx;
		font-weight: 500;
		padding: 6rpx 14rpx;
		border-radius: $radius-sm;
		box-shadow: 0 4rpx 12rpx rgba($secondary-color, 0.35);
	}
	
	.property-info {
		flex: 1;
		display: flex;
		flex-direction: column;
		gap: 10rpx;
		min-width: 0;
	}
	
	.property-title {
		font-size: 30rpx;
		font-weight: 600;
		color: $text-color-primary;
		line-height: 1.4;
		overflow: hidden;
		text-overflow: ellipsis;
		display: -webkit-box;
		-webkit-line-clamp: 1;
		-webkit-box-orient: vertical;
	}
	
	.property-price {
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
		font-weight: normal;
	}
	
	.property-details {
		display: flex;
		gap: 12rpx;
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
		display: flex;
		align-items: center;
		gap: 8rpx;
		font-size: 24rpx;
		color: $text-color-regular;
		margin-top: auto;
	}
	
	.location-icon {
		font-size: 22rpx;
	}
	
	.property-actions {
		display: flex;
		flex-direction: column;
		justify-content: center;
		align-items: center;
		flex-shrink: 0;
	}
	
	.favorite-icon {
		font-size: 44rpx;
		transition: all $transition-base;
		filter: drop-shadow(0 2rpx 8rpx rgba(255, 77, 79, 0.3));
		
		&:active {
			transform: scale(0.9);
		}
	}
</style>