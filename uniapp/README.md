# 智能房产交易平台 - Uniapp移动端

基于 Uniapp + Vue3 + uView Plus 开发的智能房产交易移动端应用，支持微信小程序、App、H5 多端运行。

## 项目特色

### 🤖 AI智能功能
- **智能推荐**：基于用户行为的个性化房源推荐
- **AI问答助手**：房产相关问题智能解答
- **文案生成**：房东一键生成优质房源宣传文案

### 📱 移动端优化
- 响应式设计，适配各种屏幕尺寸
- 流畅的交互体验和动画效果
- 原生级别的性能表现

### 🔧 技术栈
- **前端框架**：Uniapp + Vue3
- **UI组件库**：uView Plus
- **状态管理**：Vuex/Pinia
- **网络请求**：封装的 uni.request
- **多端支持**：微信小程序、App、H5

## 功能模块

### 🏠 房源管理
- 房源搜索与筛选
- 房源详情展示
- 收藏与预约看房
- 房源发布与管理

### 👤 用户系统
- 用户注册与登录
- 个人信息管理
- 收藏列表管理
- 设置与偏好

### 🎯 AI功能
- 智能房源推荐
- AI问答助手
- 智能文案生成
- 用户行为分析

## 项目结构

```
uniapp/
├── api/                    # API接口
│   ├── request.js         # 请求封装
│   ├── ai.js             # AI相关接口
│   ├── property.js       # 房源相关接口
│   └── user.js           # 用户相关接口
├── pages/                 # 页面文件
│   ├── index/            # 首页
│   ├── search/           # 搜索页面
│   ├── property/         # 房源相关页面
│   ├── ai/               # AI功能页面
│   └── user/             # 用户相关页面
├── static/               # 静态资源
├── components/           # 自定义组件
├── utils/                # 工具函数
├── App.vue              # 应用入口
├── main.js              # 主入口文件
├── manifest.json        # 应用配置
├── pages.json           # 页面配置
└── package.json         # 依赖配置
```

## 快速开始

### 环境要求
- Node.js 14+
- HBuilderX 3.6+ 或 VS Code + uni-app 插件
- 微信开发者工具（小程序开发）
- Android Studio（App开发）
- Xcode（iOS开发，仅Mac）

### 安装依赖
```bash
# 进入项目目录
cd uniapp

# 安装依赖
npm install

# 或使用 yarn
yarn install
```

### 开发运行

#### H5端
```bash
npm run dev:h5
```

#### 微信小程序
```bash
npm run dev:mp-weixin
```

#### App端
```bash
npm run dev:app-plus
```

### 生产构建

#### H5端
```bash
npm run build:h5
```

#### 微信小程序
```bash
npm run build:mp-weixin
```

#### App端
```bash
npm run build:app-plus
```

## 配置说明

### API配置
在 `api/request.js` 中配置后端接口地址：

```javascript
const config = {
  dev: {
    baseURL: 'http://localhost:8080'  // 开发环境
  },
  prod: {
    baseURL: 'https://your-api-domain.com'  // 生产环境
  }
}
```

### 微信小程序配置
1. 在 `manifest.json` 中配置小程序 appid
2. 配置服务器域名白名单
3. 配置相关权限

### App配置
1. 在 `manifest.json` 中配置App相关信息
2. 配置图标和启动页
3. 配置权限和模块

## 主要页面

### 首页 (pages/index/index.vue)
- 搜索栏
- 轮播图
- 快捷功能入口
- AI智能推荐
- 热门房源

### 搜索页面 (pages/search/search.vue)
- 房源搜索
- 筛选条件
- 房源列表
- 收藏功能

### 房源详情 (pages/property/detail.vue)
- 房源图片轮播
- 详细信息展示
- 预约看房
- 相似推荐

### AI问答 (pages/ai/qa.vue)
- 智能对话界面
- 快速提问
- 历史记录
- 相关问题推荐

### AI推荐 (pages/ai/recommend.vue)
- 个性化推荐列表
- 推荐理由展示
- 推荐评分
- 换一批功能

### 个人中心 (pages/user/profile.vue)
- 用户信息展示
- 功能菜单
- 设置选项
- 登录/退出

## API接口

### AI相关接口
- `POST /api/ai/qa/ask` - AI问答
- `GET /api/ai/qa/history` - 问答历史
- `POST /api/ai/content/generate` - 文案生成
- `GET /api/ai/recommend/properties` - 智能推荐
- `POST /api/ai/recommend/click` - 记录推荐点击
- `POST /api/ai/behavior/record` - 记录用户行为

### 房源相关接口
- `GET /api/properties` - 房源列表
- `GET /api/properties/:id` - 房源详情
- `GET /api/properties/search` - 搜索房源
- `GET /api/properties/hot` - 热门房源
- `POST /api/properties` - 发布房源
- `POST /api/properties/favorite` - 收藏房源
- `POST /api/properties/appointment` - 预约看房

### 用户相关接口
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/phone-login` - 手机登录
- `GET /api/user/info` - 获取用户信息
- `PUT /api/user/info` - 更新用户信息

## 注意事项

1. **跨域配置**：H5端开发时需要配置代理解决跨域问题
2. **权限申请**：App端需要申请相应的系统权限
3. **图片资源**：建议使用CDN存储图片资源
4. **性能优化**：合理使用图片懒加载和分页加载
5. **兼容性**：注意不同端的API兼容性差异

## 部署说明

### H5部署
1. 执行 `npm run build:h5`
2. 将 `dist/build/h5` 目录部署到Web服务器

### 微信小程序部署
1. 执行 `npm run build:mp-weixin`
2. 使用微信开发者工具打开 `dist/build/mp-weixin` 目录
3. 上传代码并提交审核

### App部署
1. 执行 `npm run build:app-plus`
2. 使用HBuilderX打包生成安装包
3. 发布到应用商店

## 开发规范

### 代码规范
- 使用 ESLint 进行代码检查
- 遵循 Vue3 Composition API 规范
- 统一的命名规范和注释规范

### 组件规范
- 组件名使用 PascalCase
- Props 定义要完整
- 事件命名使用 kebab-case

### 样式规范
- 使用 SCSS 预处理器
- 遵循 BEM 命名规范
- 合理使用全局样式变量

## 更新日志

### v1.0.0 (2024-01-01)
- 🎉 项目初始化
- ✨ 完成基础功能开发
- 🤖 集成AI智能功能
- 📱 适配多端运行

## 技术支持

如有问题，请联系开发团队或提交 Issue。

## 许可证

MIT License