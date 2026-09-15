<template>
	<view class="recommend-page">
		<!-- 页面头部 -->
		<view class="page-header">
			<view class="header-content">
				<view class="title-section">
					<text class="title-icon">⭐</text>
					<text class="page-title">AI智能推荐</text>
				</view>
				<text class="header-tag">个性化推荐</text>
			</view>
			<view class="header-desc">基于您的浏览偏好，为您推荐合适的房源</view>
		</view>
		
		<!-- 推荐列表 -->
		<view class="recommend-content">
			<!-- 加载状态 -->
			<view v-if="loading && recommendations.length === 0" class="loading-container">
				<text class="loading-icon">⏳</text>
				<text class="loading-text">AI正在为您推荐房源...</text>
			</view>
			
			<!-- 空状态 -->
			<view v-else-if="recommendations.length === 0" class="empty-container">
				<text class="empty-icon">📋</text>
				<text class="empty-text">暂无推荐房源</text>
				<view class="empty-tip">
					<text>浏览更多房源后，AI将为您推荐更合适的房源</text>
					<button class="browse-btn" @click="goToBrowse">
						去浏览房源
					</button>
				</view>
			</view>
			
			<!-- 推荐房源列表 -->
			<view v-else class="property-list">
				<view 
					v-for="(item, index) in recommendations"
					:key="item.property.id"
					class="property-card"
					@click="goToDetail(item.property.id)"
				>
					<!-- 房源图片 -->
					<view class="property-image">
						<image 
							:src="getFirstImage(item.property.images)"
							mode="aspectFill"
							class="image"
							@error="onImageError($event, index)"
						></image>
						
						<!-- 推荐标签 -->
						<view class="recommend-badge">
							<text class="badge-icon">⭐</text>
							<text>AI推荐</text>
						</view>
						
						
						
						<!-- 数据来源标签 -->
						<view v-if="usingMockData || item.property.source" class="source-tag">
							<text>{{ item.property.source || '示例' }}</text>
						</view>
					</view>
					
					<!-- 房源信息 -->
					<view class="property-info">
						<view class="property-title">{{ item.property.title }}</view>
						
						<view class="property-price">
							<text class="price">¥{{ formatPrice(item.property.price, item.property.transactionType) }}</text>
							<text class="price-unit" v-if="item.property.transactionType !== 1">/月</text>
						</view>
						
						<view class="property-details">
							<text class="detail-tag">{{ item.property.area }}㎡</text>
							<text class="detail-tag">{{ item.property.rooms || item.property.bedrooms || 2 }}室{{ item.property.halls || 1 }}厅</text>
							<text 
								v-if="item.property.bathrooms || item.property.bathRoomNum"
								class="detail-tag"
							>{{ item.property.bathrooms || item.property.bathRoomNum }}卫</text>
						</view>
						
						<view class="property-address">
							<text class="location-icon">📍</text>
							<text>{{ item.property.city }} {{ item.property.district }} {{ item.property.address }}</text>
						</view>

						<!-- 推荐理由 -->
						<view class="recommend-reason" v-if="item.recommendReason">
							<text class="reason-icon">💡</text>
							<text>{{ item.recommendReason }}</text>
						</view>
					</view>
				</view>
			</view>
		</view>
		
		<!-- 底部提示 -->
		<view v-if="recommendations.length > 0" class="bottom-tip">
			<text>已为您推荐 {{ recommendations.length }} 套房源</text>
		</view>
	</view>
</template>

<script>
	import { aiApi } from '@/api/ai.js'
	import { crawlerProperties } from '@/mock/crawlerData.js'
	
	export default {
		data() {
			return {
				recommendations: [],
				loading: false,
				usingMockData: false
			}
		},
		
		onLoad() {
			this.loadRecommendations()
		},
		
		onPullDownRefresh() {
			this.loadRecommendations().then(() => {
				uni.stopPullDownRefresh()
			})
		},
		
		methods: {
			// 加载推荐房源
			async loadRecommendations() {
				this.loading = true
				
				try {
					const response = await aiApi.getRecommendations(10)
					
					if (response.code === 200 && response.data && response.data.length > 0) {
						this.recommendations = response.data
						this.usingMockData = false
						this.loading = false
						return
					}
				} catch (error) {
					console.log('API推荐加载失败，使用示例数据:', error)
				} finally {
					if (!this.usingMockData) {
						this.loading = false
					}
				}
				
				// 使用示例数据降级
				this.usingMockData = true
				const shuffled = [...crawlerProperties].sort(() => Math.random() - 0.5)
				const count = Math.min(4, shuffled.length)
				this.recommendations = shuffled.slice(0, count).map((p, index) => ({
					property: p,
					score: 90 + Math.floor(Math.random() * 10),
					recommendScore: 90 + Math.floor(Math.random() * 10),
					recommendReason: this.getRandomRecommendReason(p)
				}))
				this.loading = false
			},
			
			// 获取随机推荐理由
			getRandomRecommendReason(property) {
				const reasons = [
					`${property.area}㎡${property.rooms || property.bedrooms || 2}室，户型方正，采光良好`,
					`位于${property.district}核心区域，交通便利，周边配套完善`,
					`价格实惠，性价比高，适合刚需租房人群`,
					`精装修，家具家电齐全，拎包即可入住`,
					`小区环境优美，物业管理规范，居住舒适度高`
				]
				return reasons[Math.floor(Math.random() * reasons.length)]
			},
			
			// 跳转到房源详情
			async goToDetail(id) {
				// 记录点击行为
				try {
					await aiApi.recordRecommendClick({
						propertyId: id,
						source: 'recommendation'
					})
				} catch (error) {
					console.error('记录点击失败:', error)
				}
				
				// 跳转到详情页
				uni.navigateTo({
					url: `/pages/property/detail?id=${id}`
				})
			},
			
			// 去浏览房源
			goToBrowse() {
				uni.switchTab({
					url: '/pages/search/search'
				})
			},
			
			// 获取第一张图片
			getFirstImage(images) {
				if (!images) return '/static/placeholder.png'
				if (Array.isArray(images)) {
					return images[0] || '/static/placeholder.png'
				}
				const imageList = images.split(',')
				return imageList[0] || '/static/placeholder.png'
			},
			
			// 图片加载错误
			onImageError(e, index) {
				if (this.recommendations[index] && this.recommendations[index].property) {
					this.recommendations[index].property.images = ['/static/placeholder.png']
					this.recommendations[index].property.coverImage = '/static/placeholder.png'
					this.$forceUpdate()
				}
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
	.recommend-page {
		background: $bg-color-page;
		min-height: 100vh;
		padding-bottom: $spacing-lg;
	}
	
	.page-header {
		background: $primary-gradient;
		padding: 60rpx $spacing-base $spacing-xl;
		position: relative;
		overflow: hidden;
		
		&::before {
			content: '';
			position: absolute;
			top: -80rpx;
			right: -60rpx;
			width: 280rpx;
			height: 280rpx;
			background: rgba(255, 255, 255, 0.1);
			border-radius: 50%;
		}
		
		&::after {
			content: '';
			position: absolute;
			bottom: -60rpx;
			left: -40rpx;
			width: 200rpx;
			height: 200rpx;
			background: rgba(255, 255, 255, 0.06);
			border-radius: 50%;
		}
	}
	
	.header-content {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: $spacing-sm;
		position: relative;
		z-index: 2;
	}
	
	.title-section {
		display: flex;
		align-items: center;
		gap: $spacing-xs;
	}
	
	.title-icon {
		font-size: 32rpx;
	}
	
	.page-title {
		font-size: 40rpx;
		font-weight: 700;
		color: $text-color-inverse;
	}
	
	.header-tag {
		background: $secondary-gradient;
		color: $text-color-inverse;
		padding: $spacing-xs $spacing-sm;
		border-radius: $radius-full;
		font-size: 22rpx;
		font-weight: 500;
		box-shadow: 0 4rpx 12rpx rgba($secondary-color, 0.3);
	}
	
	.header-desc {
		font-size: $uni-font-size-base;
		color: rgba(255, 255, 255, 0.85);
		line-height: 1.6;
		position: relative;
		z-index: 2;
	}
	
	.recommend-content {
		padding: 0 $spacing-base;
	}
	
	.loading-container {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 120rpx $spacing-lg;
		gap: $spacing-base;
		background: $bg-color;
		border-radius: $radius-xl;
		box-shadow: $shadow-sm;
		border: 1rpx solid $border-color-light;
	}
	
	.loading-icon {
		font-size: 64rpx;
	}
	
	.loading-text {
		font-size: $uni-font-size-base;
		color: $text-color-secondary;
	}
	
	.empty-container {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 120rpx $spacing-lg;
		gap: $spacing-sm;
		background: $bg-color;
		border-radius: $radius-xl;
		box-shadow: $shadow-sm;
		border: 1rpx solid $border-color-light;
	}
	
	.empty-icon {
		font-size: 88rpx;
		opacity: 0.6;
	}
	
	.empty-text {
		font-size: 30rpx;
		color: $text-color-primary;
		font-weight: 600;
	}
	
	.empty-tip {
		text-align: center;
		font-size: $uni-font-size-base;
		color: $text-color-secondary;
		line-height: 1.6;
	}
	
	.browse-btn {
		background: $primary-gradient;
		color: $text-color-inverse;
		border: none;
		border-radius: $radius-full;
		padding: $spacing-sm $spacing-lg;
		font-size: $uni-font-size-base;
		margin-top: $spacing-sm;
		font-weight: 500;
		box-shadow: 0 4rpx 12rpx rgba($primary-color, 0.3);
		transition: all $transition-base;
		
		&:active {
			transform: scale(0.95);
		}
	}
	
	.browse-btn::after {
		border: none;
	}
	
	.property-list {
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
		height: 400rpx;
	}
	
	.image {
		width: 100%;
		height: 100%;
	}
	
	.recommend-badge {
		position: absolute;
		top: $spacing-base;
		left: $spacing-base;
		background: $primary-gradient;
		color: $text-color-inverse;
		padding: 8rpx 16rpx;
		border-radius: $radius-full;
		font-size: 22rpx;
		display: flex;
		align-items: center;
		gap: 8rpx;
		font-weight: 500;
		box-shadow: 0 4rpx 12rpx rgba($primary-color, 0.3);
	}
	
	.badge-icon {
		font-size: 20rpx;
	}
	
	.source-tag {
		position: absolute;
		bottom: $spacing-base;
		left: $spacing-base;
		background: rgba(0, 0, 0, 0.55);
		color: $text-color-inverse;
		padding: 6rpx 14rpx;
		border-radius: $radius-sm;
		font-size: 20rpx;
	}
	
	.property-info {
		padding: $spacing-base;
	}
	
	.property-title {
		font-size: 30rpx;
		font-weight: 600;
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
		font-size: 40rpx;
		font-weight: 700;
		color: $price-color;
	}
	
	.price-unit {
		font-size: 26rpx;
		color: $text-color-secondary;
		margin-left: 8rpx;
	}
	
	.property-details {
		display: flex;
		gap: $spacing-xs;
		margin-bottom: $spacing-sm;
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
		color: $text-color-secondary;
		margin-bottom: $spacing-sm;
	}
	
	.location-icon {
		font-size: 22rpx;
	}
	
	.recommend-reason {
		display: flex;
		align-items: flex-start;
		gap: $spacing-xs;
		padding: $spacing-sm;
		background: linear-gradient(135deg, rgba($secondary-color, 0.08) 0%, rgba($primary-color, 0.05) 100%);
		border-radius: $radius-lg;
		font-size: 24rpx;
		color: $text-color-secondary;
		line-height: 1.6;
		border-left: 4rpx solid $secondary-color;
	}
	
	.reason-icon {
		font-size: 24rpx;
		margin-top: 2rpx;
		flex-shrink: 0;
	}
	
	.bottom-tip {
		text-align: center;
		padding: $spacing-lg $spacing-base;
		font-size: 24rpx;
		color: $text-color-placeholder;
	}
</style>