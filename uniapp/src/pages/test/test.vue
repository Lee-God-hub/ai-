<template>
	<view class="test-page">
		<view class="test-header">
			<text class="test-title">🏠 智能房产平台</text>
		</view>
		<view class="test-content">
			<text class="test-message">项目运行成功！</text>
			<button class="test-button" @click="showToast">点击测试功能</button>
		</view>
	</view>
</template>

<script>
	export default {
		name: 'TestPage',
		methods: {
			showToast() {
				uni.showToast({
					title: '功能正常！',
					icon: 'success',
					duration: 2000
				})
			}
		}
	}
</script>

<style lang="scss" scoped>
	.test-page {
		min-height: 100vh;
		background: $primary-gradient;
		display: flex;
		flex-direction: column;
		justify-content: center;
		align-items: center;
		padding: $spacing-lg;
		position: relative;
		overflow: hidden;
		
		&::before {
			content: '';
			position: absolute;
			top: -120rpx;
			right: -80rpx;
			width: 400rpx;
			height: 400rpx;
			background: rgba(255, 255, 255, 0.1);
			border-radius: 50%;
		}
		
		&::after {
			content: '';
			position: absolute;
			bottom: -100rpx;
			left: -60rpx;
			width: 300rpx;
			height: 300rpx;
			background: rgba(255, 255, 255, 0.06);
			border-radius: 50%;
		}
	}
	
	.test-header {
		margin-bottom: $spacing-xl;
		position: relative;
		z-index: 2;
	}
	
	.test-title {
		font-size: 48rpx;
		font-weight: 700;
		color: $text-color-inverse;
		text-align: center;
	}
	
	.test-content {
		background: $bg-color;
		border-radius: $radius-xl;
		padding: $spacing-xl $spacing-lg;
		text-align: center;
		box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.15);
		position: relative;
		z-index: 2;
		border: 1rpx solid $border-color-light;
	}
	
	.test-message {
		font-size: 32rpx;
		color: $text-color-primary;
		margin-bottom: $spacing-lg;
		display: block;
		font-weight: 500;
	}
	
	.test-button {
		background: $primary-gradient;
		color: $text-color-inverse;
		border: none;
		border-radius: $radius-full;
		padding: $spacing-sm $spacing-lg;
		font-size: $uni-font-size-base;
		font-weight: 600;
		box-shadow: 0 4rpx 12rpx rgba($primary-color, 0.3);
		transition: all $transition-base;
		
		&:active {
			transform: scale(0.95);
			box-shadow: 0 2rpx 8rpx rgba($primary-color, 0.25);
		}
	}
	
	.test-button::after {
		border: none;
	}
</style>