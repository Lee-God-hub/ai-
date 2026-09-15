<template>
	<view>
		<!-- 占位元素，撑开底部空间 -->
		<view class="tabbar-placeholder"></view>
		<!-- 实际导航栏 -->
		<view class="custom-tabbar">
			<view 
				v-for="(item, index) in tabList" 
				:key="index"
				class="tab-item"
				:class="{ 'tab-active': currentIndex === index }"
				@click="switchTab(item, index)"
			>
				<view class="tab-icon">
					<text class="icon-emoji">{{ item.icon }}</text>
				</view>
				<text class="tab-text">{{ item.text }}</text>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		name: 'CustomTabbar',
		props: {
			current: {
				type: Number,
				default: -1
			}
		},
		data() {
			return {
				currentIndex: 0,
				tabList: [
					{
						pagePath: '/pages/index/index',
						text: '首页',
						icon: '🏠',
						activeIcon: '🏠'
					},
					{
						pagePath: '/pages/search/search',
						text: '搜索',
						icon: '🔍',
						activeIcon: '🔍'
					},
					{
						pagePath: '/pages/ai/qa',
						text: 'AI助手',
						icon: '🤖',
						activeIcon: '🤖'
					},
					{
						pagePath: '/pages/user/messages',
						text: '消息',
						icon: '💬',
						activeIcon: '💬'
					},
					{
						pagePath: '/pages/user/profile',
						text: '我的',
						icon: '👤',
						activeIcon: '👤'
					}
				]
			}
		},
		created() {
			this.updateCurrentIndex()
		},
		mounted() {
			this.updateCurrentIndex()
			// H5环境下监听hash变化
			// #ifdef H5
			window.addEventListener('hashchange', this.onHashChange)
			// #endif
			// 监听自定义事件
			uni.$on('tabPageShow', this.onTabPageShow)
		},
		watch: {
			current(val) {
				if (val !== -1) {
					this.currentIndex = val
				}
			}
		},
		beforeDestroy() {
			// #ifdef H5
			window.removeEventListener('hashchange', this.onHashChange)
			// #endif
			uni.$off('tabPageShow', this.onTabPageShow)
		},
		methods: {
			onHashChange() {
				this.updateCurrentIndex()
			},
			onTabPageShow(pagePath) {
				const index = this.tabList.findIndex(item => item.pagePath === pagePath)
				if (index !== -1) {
					this.currentIndex = index
				}
			},
			updateCurrentIndex() {
				if (this.current !== -1) {
					this.currentIndex = this.current
					return
				}
				// 通过getCurrentPages获取
				const pages = getCurrentPages()
				if (pages.length > 0) {
					const currentPage = pages[pages.length - 1]
					let route = currentPage.route || currentPage.$page?.fullPath || ''
					if (route && !route.startsWith('/')) {
						route = '/' + route
					}
					const cleanPath = route.split('?')[0].split('#')[0]
					const index = this.tabList.findIndex(item => {
						const itemPath = item.pagePath.split('?')[0]
						return cleanPath === itemPath || cleanPath.endsWith(itemPath)
					})
					if (index !== -1) {
						this.currentIndex = index
						return
					}
				}
				// H5环境通过location获取
				// #ifdef H5
				const hash = window.location.hash || ''
				const hashPath = hash.replace('#', '').split('?')[0]
				const h5Index = this.tabList.findIndex(item => {
					return hashPath === item.pagePath || hashPath.endsWith(item.pagePath)
				})
				if (h5Index !== -1) {
					this.currentIndex = h5Index
				}
				// #endif
			},
			switchTab(item, index) {
				if (this.currentIndex === index) return
				this.currentIndex = index
				uni.switchTab({
					url: item.pagePath
				})
				this.$emit('change', index)
			}
		}
	}
</script>

<style lang="scss" scoped>
	.tabbar-placeholder {
		height: calc(110rpx + env(safe-area-inset-bottom));
	}
	
	.custom-tabbar {
		position: fixed;
		bottom: 0;
		left: 0;
		right: 0;
		height: calc(110rpx + env(safe-area-inset-bottom));
		padding-bottom: env(safe-area-inset-bottom);
		background: $bg-color;
		display: flex;
		align-items: center;
		justify-content: space-around;
		box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.06);
		z-index: 999;
		border-top: 1rpx solid $border-color-light;
	}
	
	.tab-item {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		flex: 1;
		height: 100%;
		gap: 6rpx;
		position: relative;
		transition: all $transition-base;
		
		&:active {
			transform: scale(0.95);
		}
	}
	
	.tab-icon {
		width: 56rpx;
		height: 56rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		border-radius: $radius-lg;
		transition: all $transition-base;
	}
	
	.icon-emoji {
		font-size: 44rpx;
		line-height: 1;
		transition: all $transition-base;
	}
	
	.tab-text {
		font-size: 22rpx;
		color: $text-color-secondary;
		transition: all $transition-base;
		font-weight: 400;
	}
	
	.tab-active {
		.tab-icon {
			background: rgba($primary-color, 0.1);
			transform: translateY(-4rpx);
		}
		
		.icon-emoji {
			transform: scale(1.05);
		}
		
		.tab-text {
			color: $primary-color;
			font-weight: 600;
		}
	}
	
	.tab-item:nth-child(3) {
		.tab-active {
			.tab-icon {
				background: rgba($secondary-color, 0.1);
			}
			
			.tab-text {
				color: $secondary-color;
			}
		}
	}
</style>
