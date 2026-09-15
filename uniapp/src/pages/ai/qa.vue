<template>
	<view class="qa-page">
		<!-- 消息列表 -->
		<scroll-view 
			scroll-y 
			class="messages-container"
			:scroll-top="scrollTop"
			:scroll-with-animation="true"
		>
			<!-- 欢迎消息 -->
			<view v-if="messages.length === 0" class="welcome-section animate-fade-in-up">
				<view class="welcome-avatar">
					<text class="avatar-icon">🤖</text>
				</view>
				<view class="welcome-title animate-fade-in-up animate-delay-1">您好！我是AI房产助手</view>
				<view class="welcome-desc animate-fade-in-up animate-delay-2">我可以帮您解答关于房产的各种问题</view>
				
				<view class="quick-questions animate-fade-in-up animate-delay-3">
					<view class="quick-title">快速提问：</view>
					<view class="question-tags">
						<view 
							v-for="(question, index) in quickQuestions"
							:key="index"
							:class="['question-tag', 'card-touch', 'animate-fade-in-up', 'animate-delay-' + (index + 1)]"
							@click="sendQuickQuestion(question)"
						>
							{{ question }}
						</view>
					</view>
				</view>
			</view>
			
			<!-- 对话消息 -->
			<view class="messages-list">
				<view 
					v-for="(message, index) in messages"
					:key="index"
					:class="['message-item', message.type, 'animate-fade-in-up']"
				>
					<!-- 用户消息 -->
					<view v-if="message.type === 'user'" class="message-content user-message">
						<view class="message-bubble user-bubble">
							<text class="message-text">{{ message.content }}</text>
							<view class="message-time">{{ message.time }}</view>
						</view>
						<view class="message-avatar">
							<text class="avatar-text">👤</text>
						</view>
					</view>
					
					<!-- AI消息 -->
					<view v-else class="message-content ai-message">
						<view class="message-avatar">
							<text class="avatar-text">🤖</text>
						</view>
						<view class="message-bubble ai-bubble">
							<text class="message-text">{{ message.content }}</text>
							<view class="message-time">{{ message.time }}</view>
							
							<!-- 相关问题推荐 -->
							<view v-if="message.relatedQuestions && message.relatedQuestions.length > 0" class="related-questions">
								<view class="related-title">相关问题：</view>
								<view class="related-tags">
									<view 
										v-for="(q, idx) in message.relatedQuestions"
										:key="idx"
										class="related-tag card-touch"
										@click="sendQuickQuestion(q)"
									>
										{{ q }}
									</view>
								</view>
							</view>
						</view>
					</view>
				</view>
			</view>
			
			<!-- 加载中 -->
			<view v-if="loading" class="message-item ai animate-fade-in-up">
				<view class="message-content ai-message">
					<view class="message-avatar">
						<text class="avatar-text">🤖</text>
					</view>
					<view class="message-bubble ai-bubble loading-bubble">
						<text class="loading-icon animate-spin">⏳</text>
						<text class="loading-text">AI正在思考中...</text>
					</view>
				</view>
			</view>
		</scroll-view>
		
		<!-- 输入区域 -->
		<view class="input-section animate-fade-in-up">
			<view class="input-container">
				<textarea 
					v-model="inputQuestion"
					placeholder="请输入您的问题..."
					:maxlength="500"
					:disabled="loading"
					class="input-field"
					auto-height
				></textarea>
				<view class="input-actions">
					<button 
						class="send-btn btn-shimmer card-touch"
						:class="{ disabled: !inputQuestion.trim() || loading }"
						@click="handleSend"
					>
						{{ loading ? '发送中' : '发送' }}
					</button>
				</view>
			</view>
		</view>
		
		<CustomTabbar :current="2" />
	</view>
</template>

<script>
	import { aiApi } from '@/api/ai.js'
	
	export default {
		data() {
			return {
				messages: [],
				inputQuestion: '',
				loading: false,
				scrollTop: 0,
				quickQuestions: [
					'买房需要注意什么？',
					'租房如何避免被骗？',
					'三室两厅适合几口人住？',
					'最新的房产政策有哪些？'
				]
			}
		},
		
		onLoad() {
			this.loadMessages()
		},

		onShow() {
			this.loadMessages()
		},

		methods: {
			// 发送消息
			async handleSend() {
				const question = this.inputQuestion.trim()
				
				if (!question) {
					uni.showToast({
						title: '请输入问题',
						icon: 'none'
					})
					return
				}
				
				if (question.length < 2) {
					uni.showToast({
						title: '问题内容太短',
						icon: 'none'
					})
					return
				}
				
				// 添加用户消息
				this.messages.push({
					type: 'user',
					content: question,
					time: this.formatTime(new Date())
				})
				this.saveMessages()
				
				// 清空输入框
				this.inputQuestion = ''
				
				// 滚动到底部
				this.$nextTick(() => {
					this.scrollToBottom()
				})
				
				// 调用AI接口
				this.loading = true
				
				try {
					const response = await aiApi.askQuestion({ question })
					
					if (response.code === 200 && response.data) {
						const aiResponse = response.data
						
						this.messages.push({
							type: 'ai',
							content: aiResponse.answer,
							time: this.formatTime(new Date(aiResponse.answerTime || Date.now())),
							relatedQuestions: aiResponse.relatedQuestions || []
						})
						this.saveMessages()
						
						this.$nextTick(() => {
							this.scrollToBottom()
						})
					} else {
						this.addMockAnswer(question)
					}
				} catch (error) {
					console.log('AI API调用失败，使用本地回复:', error)
					this.addMockAnswer(question)
				} finally {
					this.loading = false
				}
			},
			
			// 添加模拟回答
			addMockAnswer(question) {
				setTimeout(() => {
					let answer = ''
					let relatedQuestions = []
					const q = question.toLowerCase()
					
					if (q.includes('买房') || q.includes('购房') || q.includes('注意')) {
						answer = '买房需要注意以下几点：\n1. 核实房屋产权是否清晰，有无抵押或查封\n2. 查看房屋质量，包括墙体、水电、防水等\n3. 了解周边配套设施，如学校、医院、交通\n4. 确认小区物业情况和居住环境\n5. 仔细阅读购房合同条款，明确违约责任\n6. 核实房屋面积是否与产权证一致'
						relatedQuestions = ['二手房交易流程是什么？', '如何判断房屋产权是否清晰？', '房贷首付比例是多少？']
					} else if (q.includes('租房') || q.includes('被骗') || q.includes('防骗')) {
						answer = '租房避免被骗的建议：\n1. 一定要实地看房，确认房屋真实存在\n2. 核实房东身份和房产证信息\n3. 选择正规中介机构，避免私下交易\n4. 签订正规租房合同，明确押金和租金条款\n5. 不要轻易支付大额定金或预付款\n6. 交接时仔细检查房屋设施并拍照留证'
						relatedQuestions = ['租房押金一般交多少？', '签订租房合同注意事项', '合租需要注意什么？']
					} else if (q.includes('室') || q.includes('厅') || q.includes('几口人') || q.includes('适合')) {
						answer = '三室两厅是比较常见的户型，建筑面积一般在90-140㎡左右，适合3-5口人居住：\n• 适合夫妻+孩子+老人的三代同堂家庭\n• 也适合二孩家庭，孩子可以各有独立房间\n• 客厅和餐厅独立分开，活动空间充足\n• 如果是小家庭，还可以预留一间作为书房或客房'
						relatedQuestions = ['两居室适合几口人住？', '如何选择合适的户型？', '小户型怎么装修显大？']
					} else if (q.includes('政策') || q.includes('最新') || q.includes('限购')) {
						answer = '当前房产政策要点（供参考）：\n1. 很多城市实行限购限贷政策，购房前需了解当地资格\n2. 首套房和二套房首付比例不同，首套一般20%-30%\n3. 公积金贷款利率通常低于商业贷款\n4. 部分城市对人才购房有补贴政策\n5. 租房市场也在规范，保障租客权益\n\n具体政策请咨询当地住建部门或房产中介。'
						relatedQuestions = ['首套房认定标准是什么？', '公积金贷款条件是什么？', '人才购房补贴怎么申请？']
					} else if (q.includes('价格') || q.includes('多少钱') || q.includes('首付')) {
						answer = '房价和首付受多种因素影响：\n• 不同城市、不同区域价格差异很大\n• 一线城市房价较高，二三线城市相对亲民\n• 首套房首付一般为20%-30%\n• 二套房首付比例通常更高，40%-70%不等\n• 建议根据自身经济能力选择合适的房源\n\n您可以在本平台搜索目标区域房源，了解具体价格。'
						relatedQuestions = ['如何计算月供？', '公积金贷款额度怎么算？', '买二手房要交哪些税？']
					} else if (q.includes('装修') || q.includes('家具') || q.includes('拎包')) {
						answer = '关于租房/买房装修建议：\n1. 租房尽量选择带家具家电的房源，拎包入住更方便\n2. 自住装修建议选择环保材料，注重实用性\n3. 小户型建议多做收纳空间，选择浅色系装修显大\n4. 水电改造是隐蔽工程，一定要做好质量把关\n5. 家具选择尺寸合适的，避免空间拥挤'
						relatedQuestions = ['精装修房验房注意事项', '出租房简单改造技巧', '智能家居有必要装吗？']
					} else {
						answer = '感谢您的提问！我是AI房产助手，可以为您提供以下方面的咨询：\n• 买房/租房注意事项\n• 户型选择建议\n• 房产政策解读\n• 价格和贷款相关问题\n• 装修和家居建议\n\n您可以点击下方快速问题，或直接描述您的问题~'
						relatedQuestions = this.quickQuestions
					}
					
					this.messages.push({
						type: 'ai',
						content: answer,
						time: this.formatTime(new Date()),
						relatedQuestions: relatedQuestions
					})
					this.saveMessages()
					
					this.$nextTick(() => {
						this.scrollToBottom()
					})
				}, 800)
			},
			
			// 发送快速问题
			sendQuickQuestion(question) {
				this.inputQuestion = question
				this.handleSend()
			},
			
			// 滚动到底部
			scrollToBottom() {
				const query = uni.createSelectorQuery().in(this)
				query.select('.messages-list').boundingClientRect(data => {
					if (data) {
						this.scrollTop = data.height
					}
				}).exec()
			},
			
			// 格式化时间
			formatTime(date) {
				const hours = date.getHours().toString().padStart(2, '0')
				const minutes = date.getMinutes().toString().padStart(2, '0')
				return `${hours}:${minutes}`
			},

			// 加载本地历史消息
			loadMessages() {
				try {
					const savedMessages = uni.getStorageSync('ai_chat_messages')
					if (savedMessages) {
						this.messages = JSON.parse(savedMessages)
						this.$nextTick(() => {
							this.scrollToBottom()
						})
					}
				} catch (error) {
					console.error('加载历史消息失败:', error)
					this.messages = []
				}
			},

			// 保存消息到本地
			saveMessages() {
				try {
					uni.setStorageSync('ai_chat_messages', JSON.stringify(this.messages))
				} catch (error) {
					console.error('保存消息失败:', error)
				}
			}
		}
	}
</script>

<style lang="scss" scoped>
	.qa-page {
		height: 100vh;
		display: flex;
		flex-direction: column;
		background: $bg-color-page;
	}
	
	.messages-container {
		flex: 1;
		padding: $spacing-base;
		padding-bottom: calc(110rpx + 120rpx + env(safe-area-inset-bottom));
		overflow-y: auto;
	}
	
	.welcome-section {
		text-align: center;
		padding: 80rpx $spacing-lg;
		background: $bg-color;
		border-radius: $radius-xl;
		margin-top: $spacing-base;
		box-shadow: $shadow-sm;
		border: 1rpx solid $border-color-light;
	}
	
	.welcome-avatar {
		width: 120rpx;
		height: 120rpx;
		margin: 0 auto $spacing-base;
		background: $primary-gradient;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 8rpx 24rpx rgba($primary-color, 0.3);
	}
	
	.welcome-title {
		font-size: 38rpx;
		font-weight: 700;
		color: $text-color-primary;
		margin-bottom: $spacing-xs;
	}
	
	.welcome-desc {
		font-size: $uni-font-size-base;
		color: $text-color-secondary;
		margin-bottom: $spacing-lg;
		line-height: 1.6;
	}
	
	.quick-questions {
		text-align: left;
	}
	
	.quick-title {
		font-size: $uni-font-size-sm;
		color: $text-color-regular;
		margin-bottom: $spacing-sm;
		font-weight: 500;
	}
	
	.question-tags {
		display: flex;
		flex-wrap: wrap;
		gap: $spacing-sm;
	}
	
	.messages-list {
		padding-bottom: $spacing-base;
	}
	
	.message-item {
		margin-bottom: $spacing-base;
		animation: fadeSlideIn 0.3s ease-out;
	}
	
	@keyframes fadeSlideIn {
		from {
			opacity: 0;
			transform: translateY(10rpx);
		}
		to {
			opacity: 1;
			transform: translateY(0);
		}
	}
	
	.message-content {
		display: flex;
		gap: $spacing-sm;
		max-width: 82%;
	}
	
	.user-message {
		margin-left: auto;
		flex-direction: row-reverse;
	}
	
	.ai-message {
		margin-right: auto;
	}
	
	.message-bubble {
		padding: $spacing-sm $spacing-base;
		border-radius: $radius-lg;
		word-wrap: break-word;
		word-break: break-word;
		line-height: 1.6;
	}
	
	.user-bubble {
		background: $primary-gradient;
		color: $text-color-inverse;
		box-shadow: 0 4rpx 16rpx rgba($primary-color, 0.3);
		border-bottom-right-radius: $radius-sm;
	}
	
	.ai-bubble {
		background: $bg-color;
		color: $text-color-primary;
		box-shadow: $shadow-sm;
		border: 1rpx solid $border-color-light;
		border-bottom-left-radius: $radius-sm;
	}
	
	.loading-bubble {
		display: flex;
		align-items: center;
		gap: $spacing-sm;
		background: $bg-color;
		color: $primary-color;
	}
	
	.message-text {
		font-size: 28rpx;
		white-space: pre-wrap;
	}
	
	.message-time {
		font-size: 20rpx;
		color: rgba(255, 255, 255, 0.75);
		margin-top: $spacing-xs;
		display: block;
		text-align: right;
	}
	
	.ai-bubble .message-time {
		color: $text-color-placeholder;
		text-align: left;
	}
	
	.loading-text {
		font-size: 26rpx;
		color: $primary-color;
	}
	
	.related-questions {
		margin-top: $spacing-sm;
		padding-top: $spacing-sm;
		border-top: 1rpx solid $border-color-light;
	}
	
	.related-title {
		font-size: 24rpx;
		color: $text-color-regular;
		margin-bottom: $spacing-xs;
		font-weight: 500;
	}
	
	.related-tags {
		display: flex;
		flex-wrap: wrap;
		gap: $spacing-xs;
	}
	
	.input-section {
		position: fixed;
		bottom: calc(110rpx + env(safe-area-inset-bottom));
		left: 0;
		right: 0;
		background: $bg-color;
		border-top: 1rpx solid $border-color-light;
		padding: $spacing-sm $spacing-base;
		box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.05);
		z-index: 100;
	}
	
	/* 隐藏自定义Tabbar占位，让输入框紧贴导航栏 */
	::v-deep .tabbar-placeholder {
		height: 0 !important;
	}
	
	.input-container {
		display: flex;
		align-items: flex-end;
		gap: $spacing-sm;
		background: $bg-color-light;
		border-radius: $radius-lg;
		padding: $spacing-sm;
		border: 2rpx solid transparent;
		transition: all $transition-base;
		
		&:focus-within {
			border-color: $primary-color;
			background: $bg-color;
			box-shadow: 0 0 0 6rpx rgba($primary-color, 0.08);
		}
	}
	
	.input-field {
		flex: 1;
		min-height: 72rpx;
		max-height: 200rpx;
		background: transparent;
		border: none;
		outline: none;
		font-size: 28rpx;
		line-height: 1.5;
		resize: none;
		color: $text-color-primary;
		padding: 0 $spacing-xs;
	}
	
	.input-actions {
		flex-shrink: 0;
	}
	
	.avatar-icon {
		font-size: 56rpx;
	}
	
	.avatar-text {
		font-size: 32rpx;
		width: 72rpx;
		height: 72rpx;
		border-radius: 50%;
		background: $primary-gradient;
		color: $text-color-inverse;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
		box-shadow: 0 4rpx 12rpx rgba($primary-color, 0.25);
	}
	
	.user-message .avatar-text {
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.25);
	}
	
	.question-tag {
		padding: 14rpx 24rpx;
		background: rgba($primary-color, 0.06);
		color: $primary-color;
		border-radius: $radius-full;
		font-size: 26rpx;
		font-weight: 500;
		border: 1rpx solid rgba($primary-color, 0.15);
		transition: all $transition-base;
		
		&:active {
			transform: scale(0.96);
			background: rgba($primary-color, 0.12);
		}
	}
	
	.related-tag {
		padding: 8rpx 18rpx;
		background: rgba($secondary-color, 0.06);
		color: $secondary-color;
		border-radius: $radius-full;
		font-size: 22rpx;
		font-weight: 500;
		border: 1rpx solid rgba($secondary-color, 0.15);
		transition: all $transition-base;
		
		&:active {
			transform: scale(0.96);
			background: rgba($secondary-color, 0.12);
		}
	}
	
	.send-btn {
		background: $primary-gradient;
		color: $text-color-inverse;
		border: none;
		border-radius: $radius-lg;
		padding: 18rpx 32rpx;
		font-size: 26rpx;
		font-weight: 500;
		box-shadow: 0 4rpx 12rpx rgba($primary-color, 0.3);
		transition: all $transition-base;
		
		&:active {
			transform: scale(0.96);
		}
	}
	
	.send-btn.disabled {
		background: $text-color-disabled;
		color: $text-color-inverse;
		box-shadow: none;
	}
	
	.loading-icon {
		font-size: 24rpx;
		animation: spin 1s linear infinite;
	}
	
	@keyframes spin {
		from {
			transform: rotate(0deg);
		}
		to {
			transform: rotate(360deg);
		}
	}
</style>