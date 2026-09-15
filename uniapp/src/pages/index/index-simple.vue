<template>
	<view class="index-page">
		<!-- 顶部标题 -->
		<view class="header">
			<text class="app-title">智能房产交易平台</text>
		</view>
		
		<!-- 搜索栏 -->
		<view class="search-section">
			<view class="search-box" @click="goToSearch">
				<text class="search-text">搜索房源、地区...</text>
				<text class="search-icon">🔍</text>
			</view>
		</view>
		
		<!-- 快捷功能 -->
		<view class="quick-section">
			<text class="section-title">快捷功能</text>
			<view class="quick-grid">
				<view class="quick-item" @click="goToPage('/pages/search/search')">
					<text class="quick-icon">🔍</text>
					<text class="quick-text">找房源</text>
				</view>
				<view class="quick-item" @click="goToPage('/pages/ai/recommend')">
					<text class="quick-icon">⭐</text>
					<text class="quick-text">AI推荐</text>
				</view>
				<view class="quick-item" @click="goToPage('/pages/ai/qa')">
					<text class="quick-icon">💬</text>
					<text class="quick-text">AI问答</text>
				</view>
				<view class="quick-item" @click="goToPage('/pages/user/profile')">
					<text class="quick-icon">👤</text>
					<text class="quick-text">个人中心</text>
				</view>
			</view>
		</view>
		
		<!-- 推荐房源 -->
		<view class="recommend-section">
			<text class="section-title">推荐房源</text>
			<view class="property-list">
				<view 
					v-for="property in mockProperties"
					:key="property.id"
					class="property-item"
					@click="goToDetail(property.id)"
				>
					<view class="property-info">
						<text class="property-title">{{ property.title }}</text>
						<text class="property-price">¥{{ property.price }}{{ property.transactionType !== 1 ? '/月' : '' }}</text>
						<text class="property-area">{{ property.area }}㎡</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				mockProperties: [
					{
						id: 1,
						title: '精装三室两厅，南北通透',
						price: 8500,
						area: 120,
						transactionType: 0
					},
					{
						id: 2,
						title: '豪华公寓，配套齐全',
						price: 12000,
						area: 85,
						transactionType: 0
					}
				]
			}
		},
		
		methods: {
			goToSearch() {
				uni.navigateTo({
					url: '/pages/search/search'
				})
			},
			
			goToPage(url) {
				uni.navigateTo({
					url: url
				})
			},
			
			goToDetail(id) {
				uni.navigateTo({
					url: `/pages/property/detail?id=${id}`
				})
			}
		}
	}
</script>

<style lang="scss" scoped>
	.index-page {
		background: $bg-color-page;
		min-height: 100vh;
		padding-bottom: calc(120rpx + env(safe-area-inset-bottom));
	}
	
	.header {
		background: $primary-gradient;
		padding: 60rpx $spacing-base $spacing-xl;
		text-align: left;
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
	
	.app-title {
		font-size: 44rpx;
		font-weight: 700;
		color: $text-color-inverse;
		position: relative;
		z-index: 2;
	}
	
	.search-section {
		padding: 0 $spacing-base;
		margin-top: -30rpx;
		position: relative;
		z-index: 3;
	}
	
	.search-box {
		display: flex;
		justify-content: space-between;
		align-items: center;
		background: $bg-color;
		border-radius: $radius-full;
		padding: $spacing-sm $spacing-base;
		border: none;
		box-shadow: $shadow-sm;
		transition: all $transition-base;
		
		&:active {
			transform: scale(0.98);
			box-shadow: $shadow-xs;
		}
	}
	
	.search-text {
		color: $text-color-placeholder;
		font-size: $uni-font-size-base;
	}
	
	.search-icon {
		font-size: 36rpx;
	}
	
	.quick-section {
		background: $bg-color;
		margin: $spacing-base;
		border-radius: $radius-xl;
		padding: $spacing-base;
		box-shadow: $shadow-xs;
		border: 1rpx solid $border-color-light;
	}
	
	.section-title {
		font-size: 32rpx;
		font-weight: 600;
		color: $text-color-primary;
		margin-bottom: $spacing-base;
		display: block;
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
	
	.quick-grid {
		display: grid;
		grid-template-columns: repeat(4, 1fr);
		gap: $spacing-sm;
	}
	
	.quick-item {
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: $spacing-xs;
		padding: $spacing-sm;
		border-radius: $radius-lg;
		transition: all $transition-base;
		
		&:active {
			background: rgba($primary-color, 0.06);
			transform: scale(0.95);
		}
	}
	
	.quick-icon {
		font-size: 48rpx;
		width: 88rpx;
		height: 88rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		background: linear-gradient(135deg, rgba($primary-color, 0.1) 0%, rgba($secondary-color, 0.1) 100%);
		border-radius: 50%;
	}
	
	.quick-text {
		font-size: 24rpx;
		color: $text-color-secondary;
		font-weight: 500;
	}
	
	.recommend-section {
		background: $bg-color;
		margin: 0 $spacing-base $spacing-base;
		border-radius: $radius-xl;
		padding: $spacing-base;
		box-shadow: $shadow-xs;
		border: 1rpx solid $border-color-light;
	}
	
	.property-list {
		display: flex;
		flex-direction: column;
		gap: $spacing-sm;
	}
	
	.property-item {
		background: linear-gradient(135deg, rgba($primary-color, 0.04) 0%, rgba($secondary-color, 0.03) 100%);
		border-radius: $radius-lg;
		padding: $spacing-base;
		transition: all $transition-base;
		border-left: 4rpx solid $primary-color;
		
		&:active {
			transform: translateX(4rpx);
		}
	}
	
	.property-info {
		display: flex;
		flex-direction: column;
		gap: 8rpx;
	}
	
	.property-title {
		font-size: 28rpx;
		font-weight: 600;
		color: $text-color-primary;
	}
	
	.property-price {
		font-size: 36rpx;
		font-weight: 700;
		color: $price-color;
	}
	
	.property-area {
		font-size: 24rpx;
		color: $text-color-secondary;
	}
</style>