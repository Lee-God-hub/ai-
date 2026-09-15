<template>
	<view class="index-page">
		<!-- 搜索栏 -->
		<view class="search-bar animate-fade-in-up">
			<view class="search-input" @click="goToSearch">
				<text class="search-placeholder">搜索房源、地区...</text>
				<text class="search-icon">🔍</text>
			</view>
		</view>
		
		<!-- 轮播图 -->
		<view class="banner-section animate-fade-in-up animate-delay-1">
			<swiper class="banner-swiper" indicator-dots="true" autoplay="true" interval="3000" duration="500">
				<swiper-item v-for="(banner, index) in bannerList" :key="'b'+index">
					<view :class="['banner-card', 'banner-'+index]">
						<text class="banner-title">{{ banner.title }}</text>
						<text class="banner-sub">{{ banner.sub }}</text>
					</view>
				</swiper-item>
			</swiper>
		</view>
		
		<!-- 快捷功能 -->
		<view class="quick-actions card animate-fade-in-up animate-delay-2">
			<view class="section-title">快捷功能</view>
			<view class="actions-grid">
				<view class="action-item animate-fade-in-up animate-delay-1" @click="goToPage({ tab: true, url: '/pages/search/search' })">
					<text class="action-icon">🔍</text>
					<text>找房源</text>
				</view>
				<view class="action-item animate-fade-in-up animate-delay-2" @click="goToPage({ tab: false, url: '/pages/ai/recommend' })">
					<text class="action-icon">⭐</text>
					<text>AI推荐</text>
				</view>
				<view class="action-item animate-fade-in-up animate-delay-3" @click="goToPage({ tab: true, url: '/pages/ai/qa' })">
					<text class="action-icon">💬</text>
					<text>AI问答</text>
				</view>
				<view class="action-item animate-fade-in-up animate-delay-4" @click="goToPage({ tab: false, url: '/pages/landlord/publish' })">
					<text class="action-icon">➕</text>
					<text>发布房源</text>
				</view>
			</view>
		</view>
		
		<!-- AI智能推荐 -->
		<view class="recommend-section card animate-fade-in-up animate-delay-3">
			<view class="section-header">
				<view class="section-title">
					<text class="title-icon">⭐</text>
					<text>AI智能推荐</text>
				</view>
				<view class="more-btn" @click="goToPage('/pages/ai/recommend')">
					<text>更多</text>
					<text class="arrow-icon">→</text>
				</view>
			</view>
			
			<view v-if="recommendLoading" class="loading-container">
				<text class="loading-text">AI正在为您推荐...</text>
			</view>
			
			<scroll-view v-else scroll-x class="recommend-scroll" show-scrollbar="false">
				<view class="recommend-list">
					<view 
						v-for="(item, index) in recommendList" 
						:key="item.property.id"
						:class="['recommend-item', 'card-touch', 'animate-fade-in-up', 'animate-delay-' + (index + 1)]"
						@click="goToDetail(item.property.id)"
					>
						<image 
						:src="getFirstImage(item.property.images)" 
						class="property-image"
						mode="aspectFill"
						@error="onImageError($event, item.property)"
					></image>
					<view class="property-info">
						<view class="property-title">{{ item.property.title }}</view>
						<view class="property-price">¥{{ formatPrice(item.property.price, item.property.transactionType) }}<text class="price-unit" v-if="item.property.transactionType !== 1">/月</text></view>
						<view class="property-tags">
							<text class="tag ai-tag">AI推荐</text>
							<text v-if="item.property.source" class="tag source-tag">{{ item.property.source }}</text>
						</view>
					</view>
				</view>
			</view>
		</scroll-view>
		</view>
		
		<!-- 热门房源 -->
		<view class="hot-properties card animate-fade-in-up animate-delay-4">
			<view class="section-header">
				<view class="section-title">
					<text class="title-icon">🔥</text>
					<text>热门房源</text>
				</view>
				<view class="more-btn" @click="goToPage('/pages/search/search')">
					<text>更多</text>
					<text class="arrow-icon">→</text>
				</view>
			</view>
			
			<view v-if="hotLoading" class="loading-container">
				<text class="loading-text">加载中...</text>
			</view>
			
			<view v-else class="property-list">
				<view 
					v-for="(property, index) in hotProperties" 
					:key="property.id"
					:class="['property-card', 'card-touch', 'animate-fade-in-up', 'animate-delay-' + (index + 1)]"
					@click="goToDetail(property.id)"
				>
					<image 
						:src="getFirstImage(property.images)" 
						class="card-image"
						mode="aspectFill"
						@error="onImageError($event, property)"
					></image>
					<view class="card-content">
						<view class="card-title">{{ property.title }}</view>
						<view class="card-price">¥{{ formatPrice(property.price, property.transactionType) }}<text class="price-unit" v-if="property.transactionType !== 1">/月</text></view>
						<view class="card-info">
							<text>{{ property.area }}㎡</text>
							<text>{{ property.rooms || property.bedrooms || 0 }}室{{ property.halls || 1 }}厅</text>
						</view>
						<view class="card-location">
							<text class="location-icon">📍</text>
							<text>{{ property.city }} {{ property.district }}</text>
						</view>
						<text v-if="property.source" class="card-source">{{ property.source }}</text>
					</view>
				</view>
			</view>
		</view>
		
		<CustomTabbar :current="0" />
	</view>
</template>

<script>
	import { aiApi } from '@/api/ai.js'
	import { propertyApi } from '@/api/property.js'
	import { crawlerProperties } from '@/mock/crawlerData.js'
	
	export default {
		data() {
			return {
				// 轮播图数据
				bannerList: [
					{ title: '智慧房产 · 美好生活', sub: 'AI赋能，找房更简单高效' },
					{ title: '优质房源 · 一键发布', sub: '房东直租，信息透明可靠' },
					{ title: '专业问答 · 贴心服务', sub: 'AI助手，房产问题随时问' }
				],

				// 推荐房源
				recommendList: [],
				recommendLoading: false,

				// 热门房源
				hotProperties: [],
				hotLoading: false
			}
		},
		
		onLoad() {
			this.loadRecommendations()
			this.loadHotProperties()
		},
		
		onPullDownRefresh() {
			this.refreshData()
		},
		
		methods: {
			// 刷新数据
			async refreshData() {
				await Promise.all([
					this.loadRecommendations(),
					this.loadHotProperties()
				])
				uni.stopPullDownRefresh()
			},
			
			// 加载AI推荐
			async loadRecommendations() {
				this.recommendLoading = true
				try {
					const response = await aiApi.getRecommendations(6)
					if (response.code === 200 && response.data && response.data.length > 0) {
						this.recommendList = response.data
						return
					}
				} catch (error) {
					console.log('API推荐加载失败，使用示例数据:', error)
				} finally {
					this.recommendLoading = false
				}
				// 使用示例数据降级
				this.recommendList = crawlerProperties.slice(0, 6).map(p => ({
					property: p,
					recommendScore: 90 + Math.floor(Math.random() * 10)
				}))
			},
			
			// 加载热门房源
			async loadHotProperties() {
				this.hotLoading = true
				try {
					const response = await propertyApi.getHotProperties(8)
					if (response.code === 200 && response.data && response.data.length > 0) {
						this.hotProperties = response.data
						return
					}
				} catch (error) {
					console.log('API热门房源加载失败，使用示例数据:', error)
				} finally {
					this.hotLoading = false
				}
				// 使用示例数据降级
				this.hotProperties = crawlerProperties.slice(0, 6)
			},
			
			// 轮播图点击
			onBannerClick(index) {
				console.log('点击轮播图:', index)
			},

			// 跳转搜索页面
			goToSearch() {
				uni.switchTab({
					url: '/pages/search/search'
				})
			},

			// 跳转页面（区分 tabBar 和 普通页面）
			goToPage(opts) {
				if (typeof opts === 'string') {
					opts = { tab: false, url: opts }
				}
				if (opts.tab) {
					uni.switchTab({ url: opts.url })
				} else {
					uni.navigateTo({
						url: opts.url,
						fail() {
							uni.reLaunch({ url: opts.url })
						}
					})
				}
			},
			
			// 跳转房源详情
			goToDetail(id) {
				uni.navigateTo({
					url: `/pages/property/detail?id=${id}`
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
				if (item.property) {
					item.property.images = ['/static/placeholder.png']
					item.property.coverImage = '/static/placeholder.png'
				} else {
					item.images = ['/static/placeholder.png']
					item.coverImage = '/static/placeholder.png'
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
	.index-page {
		background: $bg-color-page;
		min-height: 100vh;
		padding-bottom: calc(110rpx + env(safe-area-inset-bottom));
	}
	
	.search-bar {
		padding: $spacing-sm $spacing-base;
		background: $bg-color;
		position: sticky;
		top: 0;
		z-index: 10;
	}
	
	.search-input {
		display: flex;
		align-items: center;
		justify-content: space-between;
		background: $bg-color-light;
		border-radius: $radius-full;
		padding: $spacing-sm $spacing-base;
		border: 2rpx solid transparent;
		transition: all $transition-base;
		
		&:active {
			border-color: $primary-color;
			background: $bg-color;
			box-shadow: 0 0 0 6rpx rgba($primary-color, 0.08);
		}
	}
	
	.search-placeholder {
		color: $text-color-secondary;
		font-size: $uni-font-size-base;
		flex: 1;
	}
	
	.search-icon {
		font-size: 32rpx;
		color: $primary-color;
	}
	
	.banner-section {
		padding: $spacing-sm $spacing-base;
	}

	.banner-swiper {
		height: 320rpx;
		border-radius: $radius-xl;
		overflow: hidden;
		box-shadow: $shadow-base;
	}

	.banner-card {
		width: 100%;
		height: 100%;
		display: flex;
		flex-direction: column;
		justify-content: center;
		align-items: center;
		color: $text-color-inverse;
		position: relative;
		overflow: hidden;
		
		&::before {
			content: '';
			position: absolute;
			top: -50%;
			right: -20%;
			width: 300rpx;
			height: 300rpx;
			background: rgba(255, 255, 255, 0.1);
			border-radius: 50%;
		}
		
		&::after {
			content: '';
			position: absolute;
			bottom: -30%;
			left: -10%;
			width: 200rpx;
			height: 200rpx;
			background: rgba(255, 255, 255, 0.08);
			border-radius: 50%;
		}
	}

	.banner-0 { background: $primary-gradient; }
	.banner-1 { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
	.banner-2 { background: $secondary-gradient; }

	.banner-title {
		font-size: 40rpx;
		font-weight: 700;
		margin-bottom: $spacing-xs;
		position: relative;
		z-index: 2;
	}

	.banner-sub {
		font-size: $uni-font-size-sm;
		opacity: 0.9;
		position: relative;
		z-index: 2;
	}
	
	.action-icon {
		font-size: 48rpx;
		margin-bottom: $spacing-xs;
	}
	
	.title-icon {
		font-size: 28rpx;
		margin-right: $spacing-xs;
	}
	
	.arrow-icon {
		font-size: 20rpx;
		color: $text-color-secondary;
	}
	
	.tag {
		padding: 6rpx 14rpx;
		border-radius: $radius-full;
		font-size: 20rpx;
		font-weight: 500;
	}
	
	.ai-tag {
		background: rgba($primary-color, 0.1);
		color: $primary-color;
	}
	
	.source-tag {
		background: rgba($secondary-color, 0.1);
		color: $secondary-color;
	}
	
	.price-unit {
		font-size: 22rpx;
		font-weight: normal;
		color: $text-color-secondary;
		margin-left: 4rpx;
	}
	
	.location-icon {
		font-size: 20rpx;
	}
	
	.section-title {
		font-size: $uni-font-size-lg;
		font-weight: 600;
		color: $text-color-primary;
		display: flex;
		align-items: center;
		gap: $spacing-xs;
		margin-bottom: 0;
	}
	
	.section-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: $spacing-sm;
	}
	
	.more-btn {
		display: flex;
		align-items: center;
		gap: 6rpx;
		color: $text-color-secondary;
		font-size: $uni-font-size-sm;
		transition: color $transition-fast;
		
		&:active {
			color: $primary-color;
		}
	}
	
	.quick-actions {
		margin: $spacing-sm $spacing-base;
		border-radius: $radius-xl;
		background: $bg-color;
		padding: $spacing-base;
		box-shadow: $shadow-sm;
		
		.section-title {
			margin-bottom: $spacing-sm;
		}
	}
	
	.actions-grid {
		display: grid;
		grid-template-columns: repeat(4, 1fr);
		gap: $spacing-sm;
	}
	
	.action-item {
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: $spacing-xs;
		padding: $spacing-sm $spacing-xs;
		border-radius: $radius-lg;
		transition: all $transition-fast;
		
		&:active {
			background: $bg-color-light;
			transform: scale(0.96);
		}
		
		text {
			font-size: $uni-font-size-sm;
			color: $text-color-regular;
		}
	}
	
	.loading-container {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: $spacing-xl $spacing-base;
		gap: $spacing-sm;
	}
	
	.loading-text {
		font-size: $uni-font-size-sm;
		color: $text-color-secondary;
	}
	
	.recommend-section {
		margin: $spacing-sm $spacing-base;
		border-radius: $radius-xl;
		background: $bg-color;
		padding: $spacing-base;
		box-shadow: $shadow-sm;
		overflow: hidden;
	}
	
	.recommend-scroll {
		white-space: nowrap;
		margin: 0 (-$spacing-base);
		padding: 0 $spacing-base;
	}
	
	.recommend-list {
		display: flex;
		gap: $spacing-sm;
		padding-bottom: $spacing-xs;
	}
	
	.recommend-item {
		width: 280rpx;
		background: $bg-color;
		border-radius: $radius-lg;
		overflow: hidden;
		box-shadow: $shadow-xs;
		flex-shrink: 0;
		border: 1rpx solid $border-color-light;
		transition: all $transition-base;
		
		&:active {
			transform: translateY(4rpx);
			box-shadow: $shadow-sm;
		}
	}
	
	.property-image {
		width: 100%;
		height: 160rpx;
		background: $bg-color-light;
	}
	
	.property-info {
		padding: $spacing-sm;
	}
	
	.property-title {
		font-size: 26rpx;
		color: $text-color-primary;
		margin-bottom: 6rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
		font-weight: 500;
	}
	
	.property-price {
		font-size: 30rpx;
		font-weight: 700;
		color: $price-color;
		margin-bottom: $spacing-xs;
	}
	
	.property-tags {
		display: flex;
		gap: 6rpx;
		flex-wrap: wrap;
	}
	
	.hot-properties {
		margin: $spacing-sm $spacing-base;
		border-radius: $radius-xl;
		background: $bg-color;
		padding: $spacing-base;
		box-shadow: $shadow-sm;
	}
	
	.property-list {
		display: grid;
		grid-template-columns: repeat(2, 1fr);
		gap: $spacing-sm;
	}
	
	.property-card {
		position: relative;
		background: $bg-color;
		border-radius: $radius-lg;
		overflow: hidden;
		box-shadow: $shadow-xs;
		border: 1rpx solid $border-color-light;
		transition: all $transition-base;
		
		&:active {
			transform: translateY(4rpx);
			box-shadow: $shadow-sm;
		}
	}
	
	.card-image {
		width: 100%;
		height: 200rpx;
		background: $bg-color-light;
	}
	
	.card-content {
		padding: $spacing-sm;
	}
	
	.card-title {
		font-size: 26rpx;
		color: $text-color-primary;
		margin-bottom: 6rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
		font-weight: 500;
	}
	
	.card-price {
		font-size: 30rpx;
		font-weight: 700;
		color: $price-color;
		margin-bottom: 6rpx;
	}
	
	.card-info {
		font-size: 22rpx;
		color: $text-color-secondary;
		margin-bottom: 6rpx;
		
		text {
			margin-right: $spacing-xs;
		}
	}
	
	.card-location {
		display: flex;
		align-items: center;
		gap: 6rpx;
		font-size: 22rpx;
		color: $text-color-secondary;
	}
	
	.card-source {
		position: absolute;
		top: $spacing-sm;
		right: $spacing-sm;
		background: rgba($secondary-color, 0.95);
		color: #fff;
		font-size: 18rpx;
		padding: 4rpx 10rpx;
		border-radius: $radius-full;
		font-weight: 500;
		box-shadow: 0 2rpx 8rpx rgba($secondary-color, 0.3);
	}
</style>