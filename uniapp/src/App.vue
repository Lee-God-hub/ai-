<script>
	export default {
		onLaunch: function() {
			console.log('App Launch')
			this.checkLogin()
		},
		onShow: function() {
			console.log('App Show')
		},
		onHide: function() {
			console.log('App Hide')
		},
		methods: {
			checkLogin() {
				const token = uni.getStorageSync('token')
				if (token) {
					this.validateToken(token)
				}
			},
			
			validateToken(token) {
				console.log('验证token:', token)
			}
		}
	}
</script>

<style lang="scss">
	/* #ifdef H5 */
	uni-tabbar {
		display: none !important;
	}
	/* #endif */
	
	page {
		background-color: $bg-color-page;
		font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue', sans-serif;
		color: $text-color-primary;
		font-size: $uni-font-size-base;
		-webkit-font-smoothing: antialiased;
		-moz-osx-font-smoothing: grayscale;
	}
	
	/* 隐藏所有滚动条（主要针对H5横向滚动条） */
	::-webkit-scrollbar {
		display: none;
		width: 0 !important;
		height: 0 !important;
		-webkit-appearance: none;
		background: transparent;
	}
	
	.container {
		padding: $spacing-base;
	}
	
	.card {
		background: $bg-color;
		border-radius: $radius-xl;
		padding: $spacing-base;
		margin-bottom: $spacing-sm;
		box-shadow: $shadow-sm;
		transition: box-shadow $transition-base, transform $transition-base;
		
		&:active {
			box-shadow: $shadow-base;
			transform: translateY(2rpx);
		}
	}
	
	.flex {
		display: flex;
	}
	
	.flex-1 {
		flex: 1;
	}
	
	.flex-center {
		display: flex;
		align-items: center;
		justify-content: center;
	}
	
	.flex-between {
		display: flex;
		align-items: center;
		justify-content: space-between;
	}
	
	.flex-column {
		display: flex;
		flex-direction: column;
	}
	
	.text-primary {
		color: $primary-color;
	}
	
	.text-secondary {
		color: $secondary-color;
	}
	
	.text-success {
		color: $success-color;
	}
	
	.text-warning {
		color: $warning-color;
	}
	
	.text-error {
		color: $error-color;
	}
	
	.text-info {
		color: $info-color;
	}
	
	.text-price {
		color: $price-color;
	}
	
	.text-sm {
		font-size: $uni-font-size-sm;
	}
	
	.text-base {
		font-size: $uni-font-size-base;
	}
	
	.text-lg {
		font-size: $uni-font-size-lg;
	}
	
	.text-xl {
		font-size: $uni-font-size-xl;
	}
	
	.text-bold {
		font-weight: 600;
	}
	
	.text-center {
		text-align: center;
	}
	
	.text-ellipsis {
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}
	
	.text-ellipsis-2 {
		overflow: hidden;
		text-overflow: ellipsis;
		display: -webkit-box;
		-webkit-line-clamp: 2;
		-webkit-box-orient: vertical;
	}
	
	.mt-10 { margin-top: 10rpx; }
	.mt-20 { margin-top: 20rpx; }
	.mt-30 { margin-top: 30rpx; }
	.mb-10 { margin-bottom: 10rpx; }
	.mb-20 { margin-bottom: 20rpx; }
	.mb-30 { margin-bottom: 30rpx; }
	.ml-10 { margin-left: 10rpx; }
	.ml-20 { margin-left: 20rpx; }
	.mr-10 { margin-right: 10rpx; }
	.mr-20 { margin-right: 20rpx; }
	.p-10 { padding: 10rpx; }
	.p-20 { padding: 20rpx; }
	.p-30 { padding: 30rpx; }
	.px-20 { padding-left: 20rpx; padding-right: 20rpx; }
	.py-20 { padding-top: 20rpx; padding-bottom: 20rpx; }
	
	.btn-primary {
		background: $primary-gradient;
		color: $text-color-inverse;
		border-radius: $radius-full;
		height: 88rpx;
		line-height: 88rpx;
		text-align: center;
		font-size: $uni-font-size-lg;
		font-weight: 500;
		border: none;
		transition: all $transition-base;
		box-shadow: 0 4rpx 12rpx rgba($primary-color, 0.3);
		
		&:active {
			transform: scale(0.98);
			box-shadow: 0 2rpx 8rpx rgba($primary-color, 0.25);
		}
		
		&.btn-plain {
			background: $bg-color;
			color: $primary-color;
			border: 2rpx solid $primary-color;
			box-shadow: none;
		}
		
		&.btn-disabled {
			background: $text-color-disabled;
			color: $text-color-inverse;
			box-shadow: none;
		}
	}
	
	.btn-secondary {
		background: $secondary-gradient;
		color: $text-color-inverse;
		border-radius: $radius-full;
		height: 88rpx;
		line-height: 88rpx;
		text-align: center;
		font-size: $uni-font-size-lg;
		font-weight: 500;
		border: none;
		transition: all $transition-base;
		box-shadow: 0 4rpx 12rpx rgba($secondary-color, 0.3);
		
		&:active {
			transform: scale(0.98);
			box-shadow: 0 2rpx 8rpx rgba($secondary-color, 0.25);
		}
	}
	
	.btn-gradient {
		background: $main-gradient;
		color: $text-color-inverse;
		border-radius: $radius-full;
		height: 88rpx;
		line-height: 88rpx;
		text-align: center;
		font-size: $uni-font-size-lg;
		font-weight: 500;
		border: none;
		transition: all $transition-base;
		box-shadow: 0 4rpx 12rpx rgba($primary-color, 0.25);
		
		&:active {
			transform: scale(0.98);
			box-shadow: 0 2rpx 8rpx rgba($primary-color, 0.2);
		}
	}
	
	.input-wrap {
		background: $bg-color-light;
		border-radius: $radius-lg;
		border: 2rpx solid transparent;
		padding: 20rpx 24rpx;
		transition: all $transition-base;
		
		&:focus-within {
			border-color: $primary-color;
			background: $bg-color;
			box-shadow: 0 0 0 6rpx rgba($primary-color, 0.08);
		}
	}
	
	.divider {
		height: 1rpx;
		background: $border-color-light;
		margin: $spacing-sm 0;
	}
	
	.tag {
		display: inline-block;
		padding: 6rpx 16rpx;
		border-radius: $radius-full;
		font-size: 22rpx;
		font-weight: 500;
		transition: all $transition-fast;
		
		&.tag-primary {
			background: rgba($primary-color, 0.1);
			color: $primary-color;
		}
		
		&.tag-secondary {
			background: rgba($secondary-color, 0.1);
			color: $secondary-color;
		}
		
		&.tag-success {
			background: $success-color-light;
			color: $success-color;
		}
		
		&.tag-warning {
			background: $warning-color-light;
			color: $warning-color;
		}
		
		&.tag-error {
			background: $error-color-light;
			color: $error-color;
		}
		
		&.tag-outline {
			background: transparent;
			border: 1rpx solid $border-color;
			color: $text-color-regular;
		}
	}
	
	.page-header {
		background: $primary-gradient;
		padding: $spacing-xl $spacing-lg $spacing-2xl;
		color: $text-color-inverse;
		border-bottom-left-radius: $radius-2xl;
		border-bottom-right-radius: $radius-2xl;
	}
	
	.section-title {
		font-size: $uni-font-size-lg;
		font-weight: 600;
		color: $text-color-primary;
		margin-bottom: $spacing-sm;
	}
	
	.safe-area-bottom {
		padding-bottom: env(safe-area-inset-bottom);
	}
	
	@keyframes fadeIn {
		from { opacity: 0; }
		to { opacity: 1; }
	}
	
	@keyframes slideUp {
		from {
			opacity: 0;
			transform: translateY(20rpx);
		}
		to {
			opacity: 1;
			transform: translateY(0);
		}
	}
	
	@keyframes pulse {
		0%, 100% { opacity: 1; }
		50% { opacity: 0.5; }
	}
	
	.animate-fadeIn {
		animation: fadeIn $transition-base ease;
	}
	
	.animate-slideUp {
		animation: slideUp $transition-base ease;
	}
	
	/* ===== 炫酷动态特效 - 移动端动画系统 ===== */
	
	/* 淡入上浮 */
	@keyframes fadeInUp {
		from {
			opacity: 0;
			transform: translateY(30rpx);
		}
		to {
			opacity: 1;
			transform: translateY(0);
		}
	}
	
	/* 弹性缩放入场 */
	@keyframes elasticIn {
		0% {
			opacity: 0;
			transform: scale(0.6);
		}
		50% {
			transform: scale(1.05);
		}
		70% {
			transform: scale(0.98);
		}
		100% {
			opacity: 1;
			transform: scale(1);
		}
	}
	
	/* 流光文字 */
	@keyframes textGradientFlow {
		0% { background-position: 0% 50%; }
		50% { background-position: 100% 50%; }
		100% { background-position: 0% 50%; }
	}
	
	/* 脉冲呼吸发光 */
	@keyframes pulseGlow {
		0%, 100% {
			box-shadow: 0 0 0 0 rgba($primary-color, 0.4);
		}
		50% {
			box-shadow: 0 0 0 16rpx rgba($primary-color, 0);
		}
	}
	
	/* 浮动效果 */
	@keyframes floatUp {
		0%, 100% { transform: translateY(0); }
		50% { transform: translateY(-8rpx); }
	}
	
	/* 旋转加载 */
	@keyframes spin {
		from { transform: rotate(0deg); }
		to { transform: rotate(360deg); }
	}
	
	/* 卡片按压缩放（触摸反馈） */
	@keyframes scalePress {
		0% { transform: scale(1); }
		50% { transform: scale(0.97); }
		100% { transform: scale(1); }
	}
	
	/* 心形弹跳 */
	@keyframes heartBeat {
		0% { transform: scale(1); }
		14% { transform: scale(1.3); }
		28% { transform: scale(1); }
		42% { transform: scale(1.3); }
		70% { transform: scale(1); }
	}
	
	/* 抖动效果 */
	@keyframes shake {
		0%, 100% { transform: translateX(0); }
		10%, 30%, 50%, 70%, 90% { transform: translateX(-4rpx); }
		20%, 40%, 60%, 80% { transform: translateX(4rpx); }
	}
	
	/* 扫光效果 */
	@keyframes shimmer {
		0% { left: -100%; }
		100% { left: 100%; }
	}
	
	/* ===== 动画工具类 ===== */
	
	.animate-fade-in-up {
		animation: fadeInUp 0.5s cubic-bezier(0.4, 0, 0.2, 1) both;
	}
	
	.animate-elastic-in {
		animation: elasticIn 0.6s cubic-bezier(0.34, 1.56, 0.64, 1) both;
	}
	
	.animate-pulse-glow {
		animation: pulseGlow 2s ease-in-out infinite;
	}
	
	.animate-float {
		/* 移除连续动画，改为静态效果 */
	}
	
	.animate-spin {
		animation: spin 1s linear infinite;
	}
	
	.animate-heart-beat {
		animation: heartBeat 1s ease;
	}
	
	/* 延迟类 */
	.animate-delay-1 { animation-delay: 0.1s; }
	.animate-delay-2 { animation-delay: 0.2s; }
	.animate-delay-3 { animation-delay: 0.3s; }
	.animate-delay-4 { animation-delay: 0.4s; }
	.animate-delay-5 { animation-delay: 0.5s; }
	
	/* 渐变文字类 */
	.text-gradient-flow {
		background: linear-gradient(90deg, $primary-color, $secondary-color);
		-webkit-background-clip: text;
		background-clip: text;
		color: transparent;
	}
	
	/* 按钮扫光效果 */
	.btn-shimmer {
		position: relative;
		overflow: hidden;
		
		&::before {
			content: '';
			position: absolute;
			top: 0;
			left: -100%;
			width: 100%;
			height: 100%;
			background: linear-gradient(
				90deg,
				transparent 0%,
				rgba(255, 255, 255, 0.3) 50%,
				transparent 100%
			);
			transition: left 0.5s ease;
		}
		
		&:active::before {
			left: 100%;
		}
	}
	
	/* 卡片触摸反馈 */
	.card-touch {
		transition: transform 0.2s ease, box-shadow 0.2s ease;
		
		&:active {
			transform: scale(0.97);
			box-shadow: $shadow-sm;
		}
	}
	
	/* 图片光晕效果 */
	.img-glow {
		position: relative;
		overflow: hidden;
		
		&::after {
			content: '';
			position: absolute;
			inset: 0;
			border-radius: inherit;
			box-shadow: inset 0 0 40rpx rgba($primary-color, 0);
			transition: box-shadow 0.3s ease;
			pointer-events: none;
		}
	}
	
	/* 减少动画（适配系统设置） */
	@media (prefers-reduced-motion: reduce) {
		*,
		*::before,
		*::after {
			animation-duration: 0.01ms !important;
			animation-iteration-count: 1 !important;
			transition-duration: 0.01ms !important;
		}
	}
</style>
