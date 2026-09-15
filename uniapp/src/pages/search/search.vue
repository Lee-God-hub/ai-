<template>
	<view class="search-page">
		<!-- 筛选条件 -->
		<view class="filter-section animate-fade-in-up animate-delay-1">
			<view class="filter-row">
				<input 
					v-model="filters.city"
					placeholder="城市"
					class="filter-input"
				/>
				<input 
					v-model="filters.district"
					placeholder="区域"
					class="filter-input"
				/>
			</view>
			
			<view class="filter-row">
				<text class="filter-label">房源类型</text>
				<scroll-view scroll-x class="filter-scroll" show-scrollbar="false">
					<view class="filter-list">
						<view 
							v-for="(filter, index) in propertyTypeTabs"
							:key="'pt-' + index"
							:class="['filter-item', { active: filters.propertyType === filter.value }]"
							@click="filters.propertyType = filter.value; refreshData()"
						>
							<text>{{ filter.name }}</text>
						</view>
					</view>
				</scroll-view>
			</view>
			
			<view class="filter-row">
				<text class="filter-label">交易类型</text>
				<scroll-view scroll-x class="filter-scroll" show-scrollbar="false">
					<view class="filter-list">
						<view 
							v-for="(filter, index) in transactionTypeTabs"
							:key="'tt-' + index"
							:class="['filter-item', { active: filters.transactionType === filter.value }]"
							@click="filters.transactionType = filter.value; refreshData()"
						>
							<text>{{ filter.name }}</text>
						</view>
					</view>
				</scroll-view>
			</view>
			
			<view class="filter-actions">
				<button class="filter-btn filter-btn-search" @click="handleSearch">搜索</button>
				<button class="filter-btn filter-btn-reset" @click="resetFilters">重置</button>
			</view>
		</view>
		
		<!-- 房源列表 -->
		<view class="property-list">
			<!-- 加载状态 -->
			<view v-if="loading && properties.length === 0" class="loading-container">
				<text class="loading-text">搜索中...</text>
			</view>
			
			<!-- 空状态 -->
			<view v-else-if="properties.length === 0 && !loading" class="empty-container animate-fade-in-up">
				<text class="empty-icon">🔍</text>
				<text class="empty-text">暂无相关房源</text>
			</view>
			
			<!-- 房源卡片 -->
			<view v-else class="property-cards">
				<view 
					v-for="(property, index) in properties"
					:key="property.id"
					:class="['property-card', 'card-touch', 'animate-fade-in-up', 'animate-delay-' + (Math.min(index + 1, 5))]"
					@click="goToDetail(property.id)"
				>
					<image 
						:src="getFirstImage(property.images)"
						class="property-image"
						mode="aspectFill"
						@error="onImageError($event, property)"
					></image>
					
					<view class="property-info">
						<view class="property-title">{{ property.title }}</view>
						<view class="property-price">¥{{ formatPrice(property.price, property.transactionType) }}<text class="price-unit" v-if="property.transactionType !== 1">/月</text></view>
						<view class="property-details">
							<text>{{ property.area }}㎡</text>
							<text>{{ property.rooms || property.bedrooms || 0 }}室{{ property.halls || 1 }}厅</text>
						</view>
						<view class="property-address">
							<text class="location-icon">📍</text>
							<text>{{ property.city }} {{ property.district }}</text>
						</view>
						<text v-if="property.source" class="property-source">{{ property.source }}</text>
					</view>
					
					<view class="property-actions">
						<text 
							:class="['favorite-icon', { favorited: property.isFavorite }]"
							@click.stop="toggleFavorite(property)"
						>
							{{ property.isFavorite ? '❤️' : '🤍' }}
						</text>
					</view>
				</view>
			</view>
		</view>
		
		<!-- 加载更多 -->
		<view v-if="hasMore && properties.length > 0" class="load-more animate-fade-in-up">
			<text class="load-more-text" @click="loadMore">
				{{ loadMoreStatus === 'loading' ? '加载中...' : '加载更多' }}
			</text>
		</view>
		
		<CustomTabbar :current="1" />
	</view>
</template>

<script>
	import { propertyApi } from '@/api/property.js'
	import { aiApi } from '@/api/ai.js'
	import { crawlerProperties } from '@/mock/crawlerData.js'
	
	export default {
		data() {
			return {
				properties: [],
				loading: false,
				hasMore: true,
				loadMoreStatus: 'loadmore',
				currentPage: 1,
				pageSize: 10,
				usingMockData: false,
				
				// 筛选条件
				filters: {
					city: '',
					district: '',
					propertyType: null,
					transactionType: null
				},
				
				// 房源类型选项
				propertyTypeTabs: [
					{ name: '全部', value: null },
					{ name: '住宅', value: 0 },
					{ name: '公寓', value: 1 },
					{ name: '别墅', value: 2 }
				],
				
				// 交易类型选项
				transactionTypeTabs: [
					{ name: '全部', value: null },
					{ name: '出租', value: 0 },
					{ name: '出售', value: 1 }
				]
			}
		},
		
		onLoad() {
			this.loadProperties()
		},
		
		onPullDownRefresh() {
			this.refreshData()
		},
		
		onReachBottom() {
			if (this.hasMore && !this.loading) {
				this.loadMore()
			}
		},
		
		methods: {
			// 搜索房源
			handleSearch() {
				this.currentPage = 1
				this.properties = []
				this.hasMore = true
				this.usingMockData = false
				this.loadProperties()
			},
			
			// 加载房源列表
			async loadProperties() {
				if (this.loading) return
				
				this.loading = true
				this.loadMoreStatus = 'loading'
				
				try {
					const params = {
						pageNum: this.currentPage,
						pageSize: this.pageSize
					}
					
					// 添加筛选条件
					if (this.filters.city) {
						params.city = this.filters.city
					}
					if (this.filters.district) {
						params.district = this.filters.district
					}
					if (this.filters.propertyType !== null) {
						params.propertyType = this.filters.propertyType
					}
					if (this.filters.transactionType !== null) {
						params.transactionType = this.filters.transactionType
					}
					
					const response = await propertyApi.searchProperties(params)
					
					if (response.code === 200 && response.data && response.data.length > 0) {
						const list = response.data
						this.usingMockData = false
						
						if (this.currentPage === 1) {
							this.properties = list
						} else {
							this.properties.push(...list)
						}
						
						this.hasMore = list.length === this.pageSize
						this.loadMoreStatus = this.hasMore ? 'loadmore' : 'nomore'
						return
					}
				} catch (error) {
					console.log('API搜索失败，使用示例数据:', error)
				} finally {
					if (!this.usingMockData) {
						this.loading = false
					}
				}
				
				// 使用示例数据降级
				this.usingMockData = true
				let filteredList = [...crawlerProperties]
				
				// 城市筛选
				if (this.filters.city) {
					const city = this.filters.city.toLowerCase()
					filteredList = filteredList.filter(p => 
						p.city.toLowerCase().includes(city)
					)
				}
				
				// 区域筛选
				if (this.filters.district) {
					const district = this.filters.district.toLowerCase()
					filteredList = filteredList.filter(p => 
						p.district.toLowerCase().includes(district)
					)
				}
				
				// 房源类型筛选
				if (this.filters.propertyType !== null) {
					filteredList = filteredList.filter(p => 
						p.propertyType === this.filters.propertyType
					)
				}
				
				// 交易类型筛选
				if (this.filters.transactionType !== null) {
					filteredList = filteredList.filter(p => 
						p.transactionType === this.filters.transactionType
					)
				}
				
				// 简单分页
				const start = (this.currentPage - 1) * this.pageSize
				const pageList = filteredList.slice(start, start + this.pageSize)
				
				if (this.currentPage === 1) {
					this.properties = pageList
				} else {
					this.properties.push(...pageList)
				}
				
				this.hasMore = start + pageList.length < filteredList.length
				this.loadMoreStatus = this.hasMore ? 'loadmore' : 'nomore'
				this.loading = false
				
				if (this.currentPage === 1) {
					uni.stopPullDownRefresh()
				}
			},
			
			// 刷新数据
			refreshData() {
				this.currentPage = 1
				this.properties = []
				this.hasMore = true
				this.usingMockData = false
				this.loadProperties()
			},
			
			// 加载更多
			loadMore() {
				if (this.hasMore && !this.loading) {
					this.currentPage++
					this.loadProperties()
				}
			},
			
			// 重置筛选条件
			resetFilters() {
				this.filters = {
					city: '',
					district: '',
					propertyType: null,
					transactionType: null
				}
				this.refreshData()
			},
			
			// 跳转房源详情
			async goToDetail(id) {
				if (!this.usingMockData) {
					try {
						await aiApi.recordUserBehavior(id, 'view')
					} catch (error) {
						console.error('记录浏览行为失败:', error)
					}
				}
				
				uni.navigateTo({
					url: `/pages/property/detail?id=${id}`
				})
			},
			
			// 切换收藏状态
			async toggleFavorite(property) {
				try {
					if (property.isFavorite) {
						if (!this.usingMockData) {
							await propertyApi.unfavoriteProperty(property.id)
							await aiApi.recordUserBehavior(property.id, 'unfavorite')
						}
						property.isFavorite = false
						uni.showToast({
							title: '已取消收藏',
							icon: 'success'
						})
					} else {
						if (!this.usingMockData) {
							await propertyApi.favoriteProperty(property.id)
							await aiApi.recordUserBehavior(property.id, 'favorite')
						}
						property.isFavorite = true
						uni.showToast({
							title: '收藏成功',
							icon: 'success'
						})
					}
				} catch (error) {
					console.error('收藏操作失败:', error)
					uni.showToast({
						title: '操作失败',
						icon: 'none'
					})
				}
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
			onImageError(e, property) {
				property.images = ['/static/placeholder.png']
				property.coverImage = '/static/placeholder.png'
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
			}
		}
	}
</script>

<style lang="scss" scoped>
	.search-page {
		background: $bg-color-page;
		min-height: 100vh;
		padding-bottom: calc(110rpx + env(safe-area-inset-bottom));
	}
	
	.filter-section {
		background: $bg-color;
		border-bottom: 1rpx solid $border-color-light;
		padding: $spacing-sm $spacing-base;
	}
	
	.filter-row {
		display: flex;
		gap: $spacing-sm;
		margin-bottom: $spacing-sm;
		
		&:last-child {
			margin-bottom: 0;
		}
	}
	
	.filter-input {
		flex: 1;
		background: $bg-color-light;
		border-radius: $radius-lg;
		padding: $spacing-xs $spacing-base;
		border: 1rpx solid $border-color;
		font-size: $uni-font-size-sm;
	}
	
	.filter-label {
		font-size: 26rpx;
		color: $text-color-regular;
		font-weight: 500;
		flex-shrink: 0;
		padding-right: $spacing-xs;
		line-height: 56rpx;
	}
	
	.filter-scroll {
		white-space: nowrap;
	}
	
	.filter-list {
		display: flex;
		gap: $spacing-sm;
	}
	
	.filter-item {
		padding: $spacing-xs $spacing-base;
		border-radius: $radius-full;
		font-size: 26rpx;
		color: $text-color-regular;
		white-space: nowrap;
		border: 1rpx solid $border-color;
		background: $bg-color;
		transition: all $transition-fast;
		font-weight: 400;
		
		&:active {
			transform: scale(0.96);
		}
	}
	
	.filter-item.active {
		background: $primary-color;
		color: $text-color-inverse;
		border-color: $primary-color;
		font-weight: 500;
		box-shadow: 0 4rpx 12rpx rgba($primary-color, 0.25);
	}
	
	.filter-actions {
		display: flex;
		gap: $spacing-sm;
		padding-top: $spacing-xs;
	}
	
	.filter-btn {
		flex: 1;
		height: 80rpx;
		border-radius: $radius-lg;
		font-size: $uni-font-size-base;
		font-weight: 500;
		border: none;
		transition: all $transition-fast;
		
		&:active {
			transform: scale(0.98);
		}
	}
	
	.filter-btn-search {
		background: $primary-gradient;
		color: $text-color-inverse;
		box-shadow: 0 4rpx 12rpx rgba($primary-color, 0.3);
	}
	
	.filter-btn-reset {
		background: $bg-color-light;
		color: $text-color-regular;
		border: 1rpx solid $border-color;
	}
	
	.property-list {
		padding: $spacing-sm $spacing-base;
	}
	
	.loading-container {
		display: flex;
		justify-content: center;
		padding: 100rpx $spacing-base;
	}
	
	.loading-text {
		font-size: 26rpx;
		color: $text-color-secondary;
	}
	
	.empty-container {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 120rpx $spacing-base;
		gap: $spacing-sm;
	}
	
	.empty-icon {
		font-size: 100rpx;
		opacity: 0.6;
	}
	
	.empty-text {
		font-size: $uni-font-size-base;
		color: $text-color-secondary;
	}
	
	.property-cards {
		display: flex;
		flex-direction: column;
		gap: $spacing-sm;
	}
	
	.property-card {
		background: $bg-color;
		border-radius: $radius-xl;
		padding: $spacing-sm;
		display: flex;
		gap: $spacing-sm;
		box-shadow: $shadow-sm;
		border: 1rpx solid $border-color-light;
		transition: all $transition-base;
		
		&:active {
			transform: translateY(2rpx);
			box-shadow: $shadow-base;
		}
	}
	
	.property-image {
		width: 200rpx;
		height: 150rpx;
		border-radius: $radius-lg;
		flex-shrink: 0;
		background: $bg-color-light;
		overflow: hidden;
	}
	
	.property-info {
		flex: 1;
		display: flex;
		flex-direction: column;
		gap: 8rpx;
		min-width: 0;
	}
	
	.property-title {
		font-size: 28rpx;
		font-weight: 600;
		color: $text-color-primary;
		line-height: 1.4;
		overflow: hidden;
		text-overflow: ellipsis;
		display: -webkit-box;
		-webkit-line-clamp: 2;
		-webkit-box-orient: vertical;
	}
	
	.property-price {
		font-size: 34rpx;
		font-weight: 700;
		color: $price-color;
	}
	
	.price-unit {
		font-size: 24rpx;
		font-weight: normal;
		color: $text-color-secondary;
		margin-left: 4rpx;
	}
	
	.property-source {
		display: inline-block;
		background: rgba($secondary-color, 0.1);
		color: $secondary-color;
		font-size: 20rpx;
		padding: 4rpx 12rpx;
		border-radius: $radius-full;
		align-self: flex-start;
		font-weight: 500;
	}
	
	.property-details {
		font-size: 24rpx;
		color: $text-color-secondary;
		
		text {
			margin-right: $spacing-sm;
		}
	}
	
	.property-address {
		display: flex;
		align-items: center;
		gap: 6rpx;
		font-size: 24rpx;
		color: $text-color-regular;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}
	
	.location-icon {
		font-size: 20rpx;
		flex-shrink: 0;
	}
	
	.property-actions {
		display: flex;
		flex-direction: column;
		justify-content: flex-start;
		align-items: center;
		padding-top: 4rpx;
	}
	
	.favorite-icon {
		font-size: 44rpx;
		transition: all $transition-fast;
		
		&:active {
			transform: scale(0.9);
		}
	}
	
	.favorite-icon.favorited {
		color: $error-color;
	}
	
	.load-more {
		padding: $spacing-base;
		text-align: center;
	}
	
	.load-more-text {
		color: $primary-color;
		font-size: $uni-font-size-base;
		font-weight: 500;
		
		&:active {
			opacity: 0.8;
		}
	}
</style>