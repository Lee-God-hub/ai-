<template>
	<view class="publish-page">
		<!-- 页面头部 -->
		<view class="page-header">
			<view class="header-content">
				<view class="title-section">
					<text class="title-icon">{{ isEditMode ? '✏️' : '➕' }}</text>
					<text class="page-title">{{ isEditMode ? '编辑房源' : '发布房源' }}</text>
				</view>
				<text class="header-tag">房东专用</text>
			</view>
		</view>
		
		<!-- 发布表单 -->
		<view class="publish-form">
			<!-- 基本信息 -->
			<view class="form-section">
				<view class="section-title">基本信息</view>
				
				<view class="form-item">
					<text class="form-label">房源标题</text>
					<input 
						v-model="propertyForm.title"
						placeholder="请输入房源标题"
						class="form-input"
						maxlength="50"
					/>
				</view>
				
				<view class="form-item">
					<text class="form-label">房源类型</text>
					<view class="radio-group">
						<view 
							v-for="(type, index) in propertyTypes"
							:key="index"
							:class="['radio-item', { active: propertyForm.propertyType === type.value }]"
							@click="propertyForm.propertyType = type.value"
						>
							<text class="radio-icon">{{ propertyForm.propertyType === type.value ? '●' : '○' }}</text>
							<text>{{ type.label }}</text>
						</view>
					</view>
				</view>
				
				<view class="form-item">
					<text class="form-label">交易类型</text>
					<view class="radio-group">
						<view 
							:class="['radio-item', { active: propertyForm.transactionType === 0 }]"
							@click="propertyForm.transactionType = 0"
						>
							<text class="radio-icon">{{ propertyForm.transactionType === 0 ? '●' : '○' }}</text>
							<text>出租</text>
						</view>
						<view 
							:class="['radio-item', { active: propertyForm.transactionType === 1 }]"
							@click="propertyForm.transactionType = 1"
						>
							<text class="radio-icon">{{ propertyForm.transactionType === 1 ? '●' : '○' }}</text>
							<text>出售</text>
						</view>
					</view>
				</view>
				
				<view class="form-item">
					<text class="form-label">价格</text>
					<view class="price-input-container">
						<input 
							v-model="propertyForm.price"
							type="number"
							placeholder="请输入价格"
							class="price-input"
						/>
						<text class="price-unit">{{ propertyForm.transactionType === 1 ? '元' : '元/月' }}</text>
					</view>
				</view>
			</view>
			
			<!-- 房源详情 -->
			<view class="form-section">
				<view class="section-title">房源详情</view>
				
				<view class="form-item">
					<text class="form-label">建筑面积</text>
					<view class="area-input-container">
						<input 
							v-model="propertyForm.area"
							type="number"
							placeholder="请输入建筑面积"
							class="area-input"
						/>
						<text class="area-unit">㎡</text>
					</view>
				</view>
				
				<view class="form-item">
					<text class="form-label">房间配置</text>
					<view class="room-config">
						<view class="config-item">
							<text>卧室</text>
							<view class="number-input">
								<button class="number-btn" @click="changeNumber('rooms', -1)">-</button>
								<text class="number-value">{{ propertyForm.rooms }}</text>
								<button class="number-btn" @click="changeNumber('rooms', 1)">+</button>
							</view>
						</view>
						<view class="config-item">
							<text>客厅</text>
							<view class="number-input">
								<button class="number-btn" @click="changeNumber('halls', -1)">-</button>
								<text class="number-value">{{ propertyForm.halls }}</text>
								<button class="number-btn" @click="changeNumber('halls', 1)">+</button>
							</view>
						</view>
						<view class="config-item">
							<text>卫生间</text>
							<view class="number-input">
								<button class="number-btn" @click="changeNumber('bathrooms', -1)">-</button>
								<text class="number-value">{{ propertyForm.bathrooms }}</text>
								<button class="number-btn" @click="changeNumber('bathrooms', 1)">+</button>
							</view>
						</view>
					</view>
				</view>
				
				<view class="form-item">
					<text class="form-label">装修情况</text>
					<input 
						v-model="propertyForm.decoration"
						placeholder="如：精装修、简装、毛坯等"
						class="form-input"
					/>
				</view>
				
				<view class="form-item">
					<text class="form-label">房屋朝向</text>
					<input 
						v-model="propertyForm.orientation"
						placeholder="如：南北通透、朝南等"
						class="form-input"
					/>
				</view>
			</view>
			
			<!-- 位置信息 -->
			<view class="form-section">
				<view class="section-title">位置信息</view>
				
				<view class="form-item">
					<text class="form-label">所在城市</text>
					<input 
						v-model="propertyForm.city"
						placeholder="请输入城市"
						class="form-input"
					/>
				</view>
				
				<view class="form-item">
					<text class="form-label">所在区域</text>
					<input 
						v-model="propertyForm.district"
						placeholder="请输入区域"
						class="form-input"
					/>
				</view>
				
				<view class="form-item">
					<text class="form-label">详细地址</text>
					<input 
						v-model="propertyForm.address"
						placeholder="请输入详细地址"
						class="form-input"
					/>
				</view>
			</view>
			
			<!-- 房源描述 -->
			<view class="form-section">
				<view class="section-title">
					<text>房源描述</text>
					<button class="ai-btn" @click="showAIGenerator = true">
						<text class="ai-icon">🤖</text>
						<text>AI生成</text>
					</button>
				</view>
				
				<view class="form-item">
					<textarea 
						v-model="propertyForm.description"
						placeholder="请详细描述房源特色、周边配套等信息"
						class="form-textarea"
						maxlength="500"
					></textarea>
				</view>
			</view>
			
			<!-- 发布按钮 -->
			<view class="publish-actions">
				<button 
					class="publish-btn"
					:class="{ loading: publishing }"
					@click="handlePublish"
				>
					{{ publishing ? '发布中...' : '立即发布' }}
				</button>
			</view>
		</view>
		
		<!-- AI 文案生成弹层 -->
		<view v-if="showAIGenerator" class="ai-modal-mask" @click="closeAIGenerator">
			<view class="ai-modal" @click.stop>
				<view class="ai-modal-header">
					<text class="ai-modal-title">🤖 AI 生成宣传文案</text>
					<text class="ai-modal-close" @click="closeAIGenerator">×</text>
				</view>
				
				<view class="ai-modal-body">
					<view v-if="generatingContent" class="ai-loading">
						<text class="ai-loading-icon">✨</text>
						<text>AI 正在根据您填写的房源信息生成文案，请稍候...</text>
					</view>
					
					<view v-else-if="aiGeneratedContent.shortVersion" class="ai-result">
						<view class="ai-result-section">
							<view class="ai-result-title">
								<text>📝 简短版</text>
								<text class="ai-result-btn" @click="applyGeneratedContent('short')">使用此版</text>
							</view>
							<view class="ai-result-text">{{ aiGeneratedContent.shortVersion }}</view>
						</view>
						
						<view class="ai-result-section">
							<view class="ai-result-title">
								<text>📖 详细版</text>
								<text class="ai-result-btn" @click="applyGeneratedContent('detail')">使用此版</text>
							</view>
							<view class="ai-result-text">{{ aiGeneratedContent.detailVersion }}</view>
						</view>
					</view>
					
					<view v-else class="ai-empty">
						<text class="ai-empty-icon">💡</text>
						<text class="ai-empty-text">请先填写房源标题、价格、面积、城市等必填项，然后点击下方"生成"按钮</text>
						<button class="ai-generate-btn" @click="handleGenerateContent" :disabled="generatingContent">
							{{ generatingContent ? '生成中...' : '立即生成' }}
						</button>
					</view>
				</view>
				
				<view class="ai-modal-footer">
					<text class="ai-modal-tip">💡 建议使用详细版文案以获得更好的展示效果</text>
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
				publishing: false,
				showAIGenerator: false,
				generatingContent: false,
				aiGeneratedContent: {
					shortVersion: '',
					detailVersion: '',
					generationId: null
				},
				propertyId: null,
				isEditMode: false,
				loadingDetail: false,
				usingMockData: false,
				
				propertyForm: {
					title: '',
					propertyType: 1,
					transactionType: 0,
					price: '',
					area: '',
					rooms: 1,
					halls: 1,
					bedrooms: 1,
					bathrooms: 1,
					decoration: '',
					orientation: '',
					city: '',
					district: '',
					address: '',
					description: '',
					images: []
				},
				
				// 房源类型选项（与数据库保持一致：0-住宅，1-公寓，2-别墅）
				propertyTypes: [
					{ label: '住宅', value: 0 },
					{ label: '公寓', value: 1 },
					{ label: '别墅', value: 2 }
				]
			}
		},
		
		onLoad(options) {
			if (options && options.id && options.mode === 'edit') {
				const idStr = options.id
				this.propertyId = idStr.startsWith('my_') ? idStr : Number(idStr)
				this.isEditMode = true
				this.loadPropertyDetail(this.propertyId)
			}
		},
		
		methods: {
			async loadPropertyDetail(id) {
				this.loadingDetail = true
				
				if (typeof id === 'string' && id.startsWith('my_')) {
					this.usingMockData = true
					try {
						const myProperties = uni.getStorageSync('myProperties')
						if (myProperties) {
							const properties = JSON.parse(myProperties)
							const property = properties.find(p => p.id === id)
							if (property) {
								this.propertyForm = {
									title: property.title || '',
									propertyType: property.propertyType !== undefined ? property.propertyType : 1,
									transactionType: property.transactionType !== undefined ? property.transactionType : 0,
									price: property.price ? String(property.price) : '',
									area: property.area ? String(property.area) : '',
									rooms: property.rooms || property.bedrooms || 1,
									halls: property.halls || 1,
									bedrooms: property.bedrooms || property.rooms || 1,
									bathrooms: property.bathrooms || 1,
									decoration: property.decoration || '',
									orientation: property.orientation || '',
									city: property.city || '',
									district: property.district || '',
									address: property.address || '',
									description: property.description || '',
									images: Array.isArray(property.images) ? property.images : (property.images ? property.images.split(',') : [])
								}
								uni.showToast({ title: '已加载房源信息', icon: 'success' })
							}
						}
					} catch (e) {
						console.error('加载本地房源失败:', e)
					}
					this.loadingDetail = false
					return
				}
				
				try {
					const response = await propertyApi.getPropertyDetail(id)
					if (response.code === 200 && response.data) {
						const data = response.data
						this.propertyForm = {
							title: data.title || '',
							propertyType: data.propertyType !== undefined ? data.propertyType : 1,
							transactionType: data.transactionType !== undefined ? data.transactionType : 0,
							price: data.price ? String(data.price) : '',
							area: data.area ? String(data.area) : '',
							rooms: data.rooms || data.bedrooms || 1,
							halls: data.halls || 1,
							bedrooms: data.bedrooms || data.rooms || 1,
							bathrooms: data.bathrooms || 1,
							decoration: data.decoration || '',
							orientation: data.orientation || '',
							city: data.city || '',
							district: data.district || '',
							address: data.address || '',
							description: data.description || '',
							images: Array.isArray(data.images) ? data.images : (data.images ? data.images.split(',') : [])
						}
						uni.showToast({ title: '已加载房源信息', icon: 'success' })
					} else {
						uni.showToast({ title: response.message || '加载房源信息失败', icon: 'none' })
					}
				} catch (error) {
					console.log('API加载房源详情失败，尝试本地加载:', error)
					try {
						const myProperties = uni.getStorageSync('myProperties')
						if (myProperties) {
							const properties = JSON.parse(myProperties)
							const property = properties.find(p => p.id === id)
							if (property) {
								this.usingMockData = true
								this.propertyForm = {
									title: property.title || '',
									propertyType: property.propertyType !== undefined ? property.propertyType : 1,
									transactionType: property.transactionType !== undefined ? property.transactionType : 0,
									price: property.price ? String(property.price) : '',
									area: property.area ? String(property.area) : '',
									rooms: property.rooms || property.bedrooms || 1,
									halls: property.halls || 1,
									bedrooms: property.bedrooms || property.rooms || 1,
									bathrooms: property.bathrooms || 1,
									decoration: property.decoration || '',
									orientation: property.orientation || '',
									city: property.city || '',
									district: property.district || '',
									address: property.address || '',
									description: property.description || '',
									images: Array.isArray(property.images) ? property.images : (property.images ? property.images.split(',') : [])
								}
								uni.showToast({ title: '已加载本地房源信息', icon: 'success' })
							}
						}
					} catch (e) {
						uni.showToast({ title: '加载失败，请重试', icon: 'none' })
					}
				} finally {
					this.loadingDetail = false
				}
			},
			
			// 改变数字
			changeNumber(field, delta) {
				const newValue = this.propertyForm[field] + delta
				if (newValue >= 0 && newValue <= 10) {
					this.propertyForm[field] = newValue
				}
			},
			
			// 发布/更新房源
			async handlePublish() {
				// 表单验证
				if (!this.propertyForm.title) {
					uni.showToast({
						title: '请输入房源标题',
						icon: 'none'
					})
					return
				}
				
				if (!this.propertyForm.price) {
					uni.showToast({
						title: '请输入价格',
						icon: 'none'
					})
					return
				}
				
				if (!this.propertyForm.area) {
					uni.showToast({
						title: '请输入建筑面积',
						icon: 'none'
					})
					return
				}
				
				if (!this.propertyForm.city) {
					uni.showToast({
						title: '请输入城市',
						icon: 'none'
					})
					return
				}
				
				if (!this.propertyForm.address) {
					uni.showToast({
						title: '请输入详细地址',
						icon: 'none'
					})
					return
				}
				
				this.publishing = true
				
				const submitData = {
					...this.propertyForm,
					bedrooms: this.propertyForm.rooms || this.propertyForm.bedrooms || 1,
					propertyType: this.propertyForm.propertyType,
					transactionType: this.propertyForm.transactionType,
					priceType: this.propertyForm.transactionType,
					price: parseFloat(this.propertyForm.price),
					area: parseFloat(this.propertyForm.area),
					images: this.propertyForm.images.length > 0 ? this.propertyForm.images : ['https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=800'],
					status: 1,
					viewCount: this.isEditMode ? (this.propertyForm.viewCount || 0) : 0,
					favoriteCount: this.isEditMode ? (this.propertyForm.favoriteCount || 0) : 0,
					createTime: this.isEditMode ? (this.propertyForm.createTime || new Date().toISOString()) : new Date().toISOString()
				}
				
				if (this.usingMockData || this.isEditMode && typeof this.propertyId === 'string' && this.propertyId.startsWith('my_')) {
					setTimeout(() => {
						this.saveMockProperty(submitData)
						this.publishing = false
					}, 800)
					return
				}
				
				try {
					let response
					let successMsg
					
					if (this.isEditMode && this.propertyId) {
						submitData.id = this.propertyId
						response = await propertyApi.updateProperty(submitData)
						successMsg = '更新成功'
					} else {
						response = await propertyApi.publishProperty(submitData)
						successMsg = '发布成功'
					}
					
					if (response.code === 200) {
						uni.showToast({ title: successMsg, icon: 'success' })
						setTimeout(() => { uni.navigateBack() }, 1500)
					} else {
						this.saveMockProperty(submitData)
					}
				} catch (error) {
					console.log('API提交失败，保存到本地:', error)
					this.saveMockProperty(submitData)
				} finally {
					this.publishing = false
				}
			},
			
			saveMockProperty(submitData) {
				try {
					let myProperties = []
					const stored = uni.getStorageSync('myProperties')
					if (stored) {
						myProperties = JSON.parse(stored)
					}
					
					if (this.isEditMode && this.propertyId) {
						const index = myProperties.findIndex(p => p.id === this.propertyId)
						if (index > -1) {
							myProperties[index] = { ...myProperties[index], ...submitData, id: this.propertyId }
						} else {
							myProperties.unshift(submitData)
						}
						uni.showToast({ title: '更新成功（本地）', icon: 'success' })
					} else {
						submitData.id = 'my_' + Date.now()
						myProperties.unshift(submitData)
						uni.showToast({ title: '发布成功（本地）', icon: 'success' })
					}
					
					uni.setStorageSync('myProperties', JSON.stringify(myProperties))
					
					setTimeout(() => { uni.navigateBack() }, 1500)
				} catch (e) {
					console.error('保存本地房源失败:', e)
					uni.showToast({ title: '保存失败', icon: 'none' })
				}
			},
			
			async handleGenerateContent() {
				const form = this.propertyForm
				
				// 基本必填校验
				if (!form.title) {
					uni.showToast({ title: '请先填写房源标题', icon: 'none' })
					return
				}
				if (!form.price) {
					uni.showToast({ title: '请先填写价格', icon: 'none' })
					return
				}
				if (!form.area) {
					uni.showToast({ title: '请先填写建筑面积', icon: 'none' })
					return
				}
				if (!form.city) {
					uni.showToast({ title: '请先填写城市', icon: 'none' })
					return
				}
				
				// propertyType: 0=住宅,1=公寓,2=别墅
				// transactionType: 0=出租,1=出售
				// priceType: 0=月租,1=总价
				const requestData = {
					title: form.title,
					propertyType: form.propertyType,
					transactionType: form.transactionType,
					price: parseFloat(form.price),
					priceType: form.transactionType,
					area: parseFloat(form.area),
					bedrooms: form.bedrooms || 0,
					bathrooms: form.bathrooms || 0,
					city: form.city,
					district: form.district || '',
					address: form.address || '',
					orientation: form.orientation || '',
					decoration: form.decoration || ''
				}
				
				this.generatingContent = true
				
				try {
					const response = await aiApi.generateContent(requestData)
					
					if (response.code === 200 && response.data) {
						this.aiGeneratedContent = {
							shortVersion: response.data.shortVersion || '',
							detailVersion: response.data.detailVersion || '',
							generationId: response.data.generationId
						}
						uni.showToast({ title: '生成成功', icon: 'success' })
					} else {
						this.generateMockContent(requestData)
					}
				} catch (error) {
					console.log('AI API生成失败，使用本地生成:', error)
					this.generateMockContent(requestData)
				} finally {
					this.generatingContent = false
				}
			},
			
			generateMockContent(form) {
				const typeText = ['住宅', '公寓', '别墅'][form.propertyType] || '住宅'
				const transactionText = form.transactionType === 0 ? '出租' : '出售'
				const rooms = form.rooms || form.bedrooms || 1
				const halls = form.halls || 1
				const price = form.price
				const area = form.area
				const city = form.city || ''
				const district = form.district || ''
				const address = form.address || ''
				const decoration = form.decoration || '精装修'
				const orientation = form.orientation || '南北通透'
				
				const shortVersion = `${city}${district}${rooms}室${halls}厅${typeText}${transactionText}，${area}㎡${decoration}，${orientation}，价格${price}元${form.transactionType === 0 ? '/月' : ''}，交通便利，配套齐全，拎包入住！`
				
				const detailVersion = `【房源概况】
${city}${district}${address}，优质${typeText}${transactionText}！
建筑面积${area}㎡，${rooms}室${halls}厅${form.bathrooms || 1}卫，${decoration}，${orientation}，采光极佳。

【房源特色】
✓ 户型方正，空间利用率高
✓ ${decoration}交付，家具家电齐全，拎包入住
✓ ${orientation}，通风采光一流
✓ 小区环境优美，物业管理规范
✓ 周边生活配套成熟，超市、医院、学校一应俱全
✓ 交通便利，出行方便

【价格信息】
${transactionText}价格：${price}元${form.transactionType === 0 ? '/月，押一付三' : '，性价比极高'}
产权清晰，随时看房，欢迎来电咨询！
有意者请尽快联系，好房不等人！`
				
				this.aiGeneratedContent = {
					shortVersion,
					detailVersion,
					generationId: 'mock_' + Date.now()
				}
				uni.showToast({ title: '生成成功', icon: 'success' })
			},
			
			// 使用生成的文案
			applyGeneratedContent(version) {
				if (version === 'short') {
					this.propertyForm.description = this.aiGeneratedContent.shortVersion
				} else {
					this.propertyForm.description = this.aiGeneratedContent.detailVersion
				}
				uni.showToast({ title: '已应用到房源描述', icon: 'success' })
				this.showAIGenerator = false
			},
			
			// 关闭 AI 生成弹层
			closeAIGenerator() {
				this.showAIGenerator = false
			}
		}
	}
</script>

<style lang="scss" scoped>
	.publish-page {
		background: $bg-color-page;
		min-height: 100vh;
		padding-bottom: calc(#{$spacing-xl} + env(safe-area-inset-bottom));
	}
	
	.page-header {
		background: $primary-gradient;
		padding: 60rpx $spacing-base $spacing-lg;
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
		
		&::after {
			content: '';
			position: absolute;
			bottom: -40rpx;
			left: -30rpx;
			width: 140rpx;
			height: 140rpx;
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
	
	.header-tag {
		background: $secondary-gradient;
		color: $text-color-inverse;
		padding: 8rpx 20rpx;
		border-radius: $radius-full;
		font-size: 22rpx;
		font-weight: 500;
		box-shadow: 0 4rpx 12rpx rgba($secondary-color, 0.3);
	}
	
	.publish-form {
		padding: $spacing-base;
		margin-top: -20rpx;
		position: relative;
		z-index: 3;
	}
	
	.form-section {
		background: $bg-color;
		border-radius: $radius-xl;
		padding: $spacing-lg;
		margin-bottom: $spacing-base;
		box-shadow: $shadow-sm;
		border: 1rpx solid $border-color-light;
	}
	
	.section-title {
		font-size: 30rpx;
		font-weight: 600;
		color: $text-color-primary;
		margin-bottom: $spacing-base;
		display: flex;
		justify-content: space-between;
		align-items: center;
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
	
	.ai-btn {
		background: $secondary-gradient;
		color: $text-color-inverse;
		border: none;
		border-radius: $radius-full;
		padding: 10rpx 20rpx;
		font-size: 22rpx;
		font-weight: 500;
		display: flex;
		align-items: center;
		gap: 8rpx;
		box-shadow: 0 4rpx 12rpx rgba($secondary-color, 0.3);
		transition: all $transition-base;
		
		&:active {
			transform: scale(0.96);
		}
	}
	
	.ai-icon {
		font-size: 20rpx;
	}
	
	.form-item {
		margin-bottom: $spacing-base;
		
		&:last-child {
			margin-bottom: 0;
		}
	}
	
	.form-label {
		display: block;
		font-size: 26rpx;
		color: $text-color-primary;
		margin-bottom: $spacing-xs;
		font-weight: 500;
	}
	
	.form-input {
		width: 100%;
		padding: $spacing-base $spacing-base;
		border: 2rpx solid $border-color-light;
		border-radius: $radius-lg;
		font-size: $uni-font-size-base;
		background: $bg-color-light;
		color: $text-color-primary;
		transition: all $transition-base;
		box-sizing: border-box;
		line-height: 40rpx;
		height: 88rpx;
		
		&:focus {
			border-color: $primary-color;
			background: $bg-color;
			box-shadow: 0 0 0 6rpx rgba($primary-color, 0.08);
		}
	}
	
	.form-textarea {
		width: 100%;
		height: 240rpx;
		padding: $spacing-sm $spacing-base;
		border: 2rpx solid $border-color-light;
		border-radius: $radius-lg;
		font-size: $uni-font-size-base;
		background: $bg-color-light;
		color: $text-color-primary;
		resize: none;
		transition: all $transition-base;
		box-sizing: border-box;
		
		&:focus {
			border-color: $primary-color;
			background: $bg-color;
			box-shadow: 0 0 0 6rpx rgba($primary-color, 0.08);
		}
	}
	
	.radio-group {
		display: flex;
		flex-wrap: wrap;
		gap: $spacing-sm;
	}
	
	.radio-item {
		display: flex;
		align-items: center;
		gap: $spacing-xs;
		font-size: 26rpx;
		color: $text-color-regular;
		padding: $spacing-sm $spacing-base;
		border-radius: $radius-lg;
		border: 2rpx solid $border-color-light;
		background: $bg-color-light;
		transition: all $transition-base;
		
		&.active {
			color: $primary-color;
			border-color: $primary-color;
			background: rgba($primary-color, 0.06);
			font-weight: 500;
		}
		
		&:active {
			transform: scale(0.98);
		}
	}
	
	.radio-icon {
		font-size: 24rpx;
		color: $primary-color;
	}
	
	.price-input-container,
	.area-input-container {
		display: flex;
		align-items: center;
		gap: $spacing-xs;
	}
	
	.price-input,
	.area-input {
		flex: 1;
		padding: $spacing-base $spacing-base;
		border: 2rpx solid $border-color-light;
		border-radius: $radius-lg;
		font-size: $uni-font-size-base;
		background: $bg-color-light;
		color: $text-color-primary;
		transition: all $transition-base;
		line-height: 40rpx;
		height: 88rpx;
		box-sizing: border-box;
		
		&:focus {
			border-color: $primary-color;
			background: $bg-color;
			box-shadow: 0 0 0 6rpx rgba($primary-color, 0.08);
		}
	}
	
	.price-unit,
	.area-unit {
		font-size: 24rpx;
		color: $text-color-secondary;
		flex-shrink: 0;
	}
	
	.room-config {
		display: flex;
		justify-content: space-between;
		gap: $spacing-sm;
	}
	
	.config-item {
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: $spacing-xs;
		flex: 1;
		
		text {
			font-size: 24rpx;
			color: $text-color-secondary;
		}
	}
	
	.number-input {
		display: flex;
		align-items: center;
		border: 2rpx solid $border-color-light;
		border-radius: $radius-lg;
		overflow: hidden;
		background: $bg-color-light;
		width: 100%;
	}
	
	.number-btn {
		width: 72rpx;
		height: 72rpx;
		background: $bg-color;
		border: none;
		font-size: 28rpx;
		color: $text-color-regular;
		transition: all $transition-fast;
		
		&:active {
			background: $primary-color;
			color: $text-color-inverse;
		}
	}
	
	.number-value {
		flex: 1;
		text-align: center;
		font-size: 28rpx;
		color: $text-color-primary;
		font-weight: 600;
	}
	
	.publish-actions {
		margin-top: $spacing-lg;
	}
	
	.publish-btn {
		width: 100%;
		background: $primary-gradient;
		color: $text-color-inverse;
		border: none;
		border-radius: $radius-full;
		padding: $spacing-base;
		font-size: $uni-font-size-lg;
		font-weight: 600;
		transition: all $transition-base;
		box-shadow: 0 6rpx 20rpx rgba($primary-color, 0.35);
		
		&:active {
			transform: scale(0.98);
			box-shadow: 0 3rpx 12rpx rgba($primary-color, 0.25);
		}
	}
	
	.publish-btn.loading {
		background: $text-color-disabled;
		box-shadow: none;
	}
	
	.ai-modal-mask {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background: rgba(0, 0, 0, 0.5);
		z-index: 999;
		display: flex;
		align-items: center;
		justify-content: center;
		padding: $spacing-lg;
		animation: fadeIn 0.3s ease-out;
	}
	
	@keyframes fadeIn {
		from { opacity: 0; }
		to { opacity: 1; }
	}
	
	.ai-modal {
		width: 100%;
		max-width: 640rpx;
		background: $bg-color;
		border-radius: $radius-xl;
		overflow: hidden;
		box-shadow: $shadow-lg;
		animation: slideUp 0.3s ease-out;
	}
	
	@keyframes slideUp {
		from {
			opacity: 0;
			transform: translateY(30rpx);
		}
		to {
			opacity: 1;
			transform: translateY(0);
		}
	}
	
	.ai-modal-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: $spacing-base $spacing-lg;
		border-bottom: 1rpx solid $border-color-light;
		background: linear-gradient(135deg, rgba($primary-color, 0.06), rgba($secondary-color, 0.06));
	}
	
	.ai-modal-title {
		font-size: 30rpx;
		font-weight: 600;
		color: $text-color-primary;
	}
	
	.ai-modal-close {
		font-size: 44rpx;
		color: $text-color-secondary;
		line-height: 1;
		padding: 0 10rpx;
		transition: all $transition-fast;
		
		&:active {
			color: $text-color-primary;
		}
	}
	
	.ai-modal-body {
		padding: $spacing-lg;
		max-height: 70vh;
		overflow-y: auto;
	}
	
	.ai-modal-footer {
		padding: $spacing-sm $spacing-lg;
		background: $bg-color-light;
		border-top: 1rpx solid $border-color-light;
	}
	
	.ai-modal-tip {
		font-size: 22rpx;
		color: $text-color-secondary;
	}
	
	.ai-loading {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 80rpx 0;
		gap: $spacing-base;
	}
	
	.ai-loading-icon {
		font-size: 60rpx;
		animation: pulse 1.5s infinite;
	}
	
	@keyframes pulse {
		0%, 100% { transform: scale(1); opacity: 1; }
		50% { transform: scale(1.1); opacity: 0.8; }
	}
	
	.ai-loading text:last-child {
		font-size: 26rpx;
		color: $text-color-secondary;
		text-align: center;
		line-height: 1.6;
	}
	
	.ai-result-section {
		margin-bottom: $spacing-base;
		padding: $spacing-base;
		background: $bg-color-light;
		border-radius: $radius-lg;
		border: 1rpx solid $border-color-light;
		
		&:last-child {
			margin-bottom: 0;
		}
	}
	
	.ai-result-title {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: $spacing-xs;
		font-size: 26rpx;
		font-weight: 600;
		color: $text-color-primary;
	}
	
	.ai-result-btn {
		background: $primary-gradient;
		color: $text-color-inverse;
		font-size: 22rpx;
		padding: 8rpx 20rpx;
		border-radius: $radius-full;
		font-weight: 500;
		box-shadow: 0 2rpx 8rpx rgba($primary-color, 0.25);
		transition: all $transition-base;
		
		&:active {
			transform: scale(0.96);
		}
	}
	
	.ai-result-text {
		font-size: 26rpx;
		color: $text-color-regular;
		line-height: 1.7;
		white-space: pre-wrap;
	}
	
	.ai-empty {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: $spacing-xl 0;
		gap: $spacing-base;
	}
	
	.ai-empty-icon {
		font-size: 80rpx;
		opacity: 0.6;
	}
	
	.ai-empty-text {
		font-size: 26rpx;
		color: $text-color-secondary;
		text-align: center;
		line-height: 1.6;
	}
	
	.ai-generate-btn {
		width: 100%;
		background: $secondary-gradient;
		color: $text-color-inverse;
		border: none;
		border-radius: $radius-full;
		padding: $spacing-base;
		font-size: 28rpx;
		font-weight: 500;
		margin-top: $spacing-sm;
		box-shadow: 0 4rpx 16rpx rgba($secondary-color, 0.3);
		transition: all $transition-base;
		
		&:active {
			transform: scale(0.98);
		}
		
		&:disabled {
			background: $text-color-disabled;
			box-shadow: none;
		}
	}
</style>