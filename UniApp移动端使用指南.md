# UniApp移动端使用指南

![版本](https://img.shields.io/badge/version-1.0.0-blue)
![UniApp](https://img.shields.io/badge/UniApp-3.0-brightgreen)
![Vue](https://img.shields.io/badge/Vue-3.3-green)

---

## 📖 目录

- [项目简介](#项目简介)
- [支持平台](#支持平台)
- [环境要求](#环境要求)
- [快速开始](#快速开始)
- [配置说明](#配置说明)
- [开发调试](#开发调试)
- [打包发布](#打包发布)
- [常见问题](#常见问题)

---

## 📝 项目简介

这是**智能房产交易平台**的移动端项目，基于UniApp框架开发，支持多端部署（微信小程序、H5、App）。提供完整的房源浏览、预约看房、AI助手等功能。

### 主要功能

- ✅ 房源浏览与搜索
- ✅ 房源详情查看
- ✅ AI智能问答助手
- ✅ AI智能推荐
- ✅ 用户登录注册
- ✅ 个人中心管理
- ✅ 房源收藏功能
- ✅ 房东房源发布与管理

---

## 📱 支持平台

| 平台 | 支持状态 | 说明 |
|------|---------|------|
| **H5** | ✅ 完全支持 | 移动浏览器访问 |
| **微信小程序** | ✅ 完全支持 | 需配置小程序AppID |
| **Android App** | ✅ 完全支持 | 需HBuilderX打包 |
| **iOS App** | ✅ 完全支持 | 需Mac + Xcode |

---

## 💻 环境要求

### 必需软件

| 软件 | 版本要求 | 说明 |
|------|---------|------|
| **Node.js** | 16+ | JavaScript运行环境 |
| **pnpm/npm** | 最新版 | 包管理工具 |
| **HBuilderX** | 3.0+ (可选) | 官方开发IDE |

### 开发方式选择

#### 方式1：命令行开发（推荐）

适合习惯使用VSCode等编辑器的开发者
- 优点：灵活、轻量、支持各种编辑器
- 缺点：打包App需要HBuilderX

#### 方式2：HBuilderX开发

官方推荐的开发工具
- 优点：集成开发、可视化操作、一键打包
- 缺点：功能相对有限

**推荐方案**：日常开发使用命令行，打包App时使用HBuilderX

---

## 🚀 快速开始

### 步骤1：进入项目目录

```bash
cd c:\Users\34588\Desktop\ai租房test\ai-intelligent-rental-housing\code\ai租房系统\uniapp
```

### 步骤2：安装依赖

```bash
# 使用npm安装
npm install


**依赖安装时间**：首次安装约2-5分钟，取决于网络速度

### 步骤3：确保后端服务已启动

UniApp需要连接后端API才能正常工作。

```bash
# 启动后端服务（在backend目录）
cd ..\backend
mvn spring-boot:run
```

验证后端是否启动成功：
- 访问：http://localhost:8080
- 看到服务器信息即表示成功

### 步骤4：启动开发服务器

#### H5开发（推荐用于快速调试）

```bash
# 启动H5开发服务器
npm run dev:h5


**访问地址**：http://localhost:5174

**特点**：
- 热更新快速
- 浏览器调试方便
- 支持Chrome DevTools

#### 微信小程序开发

```bash
# 启动微信小程序开发
npm run dev:mp-weixin

# 或
pnpm dev:mp-weixin
```

编译后的文件位置：`dist/dev/mp-weixin`

**下一步**：
1. 打开**微信开发者工具**
2. 导入项目：选择 `dist/dev/mp-weixin` 目录
3. 输入测试AppID（或使用测试号）
4. 点击编译即可预览

#### App开发

```bash
# 启动App开发
npm run dev:app
```

**下一步**：需要使用HBuilderX打开项目进行调试和打包

---

## ⚙️ 配置说明

### 1. 后端API地址配置

#### 开发环境配置

**H5开发**（使用Vite代理）：  
文件位置：`vite.config.js`

```javascript
export default defineConfig({
  server: {
    port: 5174,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',  // ⚠️ 后端API地址
        changeOrigin: true
      }
    }
  }
})
```

**App/小程序开发**（直接请求）：  
文件位置：`src/api/request.js`

```javascript
// App/小程序开发环境（直接请求后端）
appDev: {
  baseURL: 'http://localhost:8080/api'  // ⚠️ 修改为实际后端地址
}
```

#### 生产环境配置

文件位置：`src/api/request.js`

```javascript
const config = {
  // H5生产环境
  h5Prod: {
    baseURL: 'https://your-api-domain.com/api'  // ⚠️ 修改为生产环境API
  },
  // App/小程序生产环境
  appProd: {
    baseURL: 'https://your-api-domain.com/api'  // ⚠️ 修改为生产环境API
  }
}
```

**修改场景**：
- 后端部署在不同服务器
- 后端使用不同端口
- 正式发布到生产环境

**注意事项**：
- H5开发使用Vite代理，只需配置 `vite.config.js`
- App和小程序无法使用代理，需要直接配置完整URL
- 生产环境必须使用HTTPS协议（小程序强制要求）

### 2. 应用信息配置

文件位置：`src/manifest.json`

```json
{
  "name": "智能房产交易平台",
  "appid": "__UNI__SMART_PROPERTY",
  "description": "基于AI的智能房产交易移动端平台",
  "versionName": "1.0.0",
  "versionCode": "100"
}
```

**关键配置项**：
- `name`：应用名称（显示在手机上）
- `appid`：UniApp应用ID（打包App必需）
- `versionName`：版本号（显示给用户）
- `versionCode`：版本代码（数字，用于更新判断）

### 3. 微信小程序配置

文件位置：`src/manifest.json` → `mp-weixin` 节点

```json
{
  "mp-weixin": {
    "appid": "",                    // ⚠️ 微信小程序AppID
    "setting": {
      "urlCheck": false             // 关闭域名校验（开发用）
    }
  }
}
```

**获取微信小程序AppID**：
1. 访问：https://mp.weixin.qq.com/
2. 登录微信公众平台
3. 进入"开发" → "开发管理" → "开发设置"
4. 复制AppID并填入配置

**测试阶段**：可以使用测试号，无需真实AppID

### 4. 页面配置

文件位置：`src/pages.json`

```json
{
  "pages": [
    {
      "path": "pages/index/index",
      "style": {
        "navigationBarTitleText": "智能房产",
        "enablePullDownRefresh": true    // 启用下拉刷新
      }
    }
  ],
  "tabBar": {
    "list": [
      {
        "pagePath": "pages/index/index",
        "text": "首页",
        "iconPath": "static/tab-home.png",
        "selectedIconPath": "static/tab-home-current.png"
      }
    ]
  }
}
```

**配置说明**：
- `pages`：所有页面路径（第一个为首页）
- `tabBar`：底部导航栏配置
- `globalStyle`：全局样式配置

---

## 🛠️ 开发调试

### H5调试（推荐）

#### 启动方式
```bash
npm run dev:h5
```

#### 调试工具
1. **Chrome DevTools**
   - 打开：F12或右键 → 检查
   - Network：查看API请求
   - Console：查看日志输出
   - Vue DevTools：查看Vue组件状态

2. **移动端模拟**
   - 在DevTools中点击手机图标
   - 选择设备型号（iPhone、Android）
   - 模拟触摸操作

#### 热更新
修改代码后自动刷新，无需手动重启

### 微信小程序调试

#### 启动方式
```bash
npm run dev:mp-weixin
```

#### 调试步骤
1. **编译项目**
   ```bash
   npm run dev:mp-weixin
   ```
   
2. **打开微信开发者工具**
   - 导入项目：`dist/dev/mp-weixin`
   - 选择AppID（或测试号）
   
3. **调试功能**
   - 模拟器：快速预览界面
   - 真机调试：扫码在手机上调试
   - Console：查看日志
   - Network：查看网络请求

#### 真机预览
1. 点击"预览"按钮
2. 用微信扫描二维码
3. 在手机上查看效果

### App调试（使用HBuilderX）

#### 准备工作
1. 下载HBuilderX：https://www.dcloud.io/hbuilderx.html
2. 安装App开发插件
3. 连接手机（开启开发者模式 + USB调试）

#### 调试步骤
1. 用HBuilderX打开项目
2. 连接手机
3. 运行 → 运行到手机或模拟器
4. 选择设备

#### 真机调试技巧
- Android：使用Chrome://inspect查看日志
- iOS：使用Safari Web检查器

### 常用调试命令

```bash
# 查看编译日志
npm run dev:h5 -- --debug

# 清除缓存重新编译
rm -rf node_modules/.vite
npm run dev:h5

# 查看打包体积分析
npm run build:h5 -- --report
```

---

## 📦 打包发布

### H5打包

#### 1. 构建生产版本
```bash
npm run build:h5
```

#### 2. 输出目录
```
dist/build/h5/
├── index.html
├── static/
│   ├── css/
│   └── js/
└── ...
```

#### 3. 部署到服务器

**方式1：使用Nginx**

```nginx
server {
    listen 80;
    server_name mobile.yourdomain.com;
    
    location / {
        root /path/to/dist/build/h5;
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://backend-server:8080;
        proxy_set_header Host $host;
    }
}
```

**方式2：使用云服务**
- 阿里云OSS + CDN
- 腾讯云COS + CDN
- 七牛云

#### 4. 访问测试
```
http://mobile.yourdomain.com
```

### 微信小程序打包

#### 1. 构建生产版本
```bash
npm run build:mp-weixin
```

#### 2. 上传代码

**使用微信开发者工具**：
1. 打开微信开发者工具
2. 导入 `dist/build/mp-weixin` 目录
3. 点击"上传"按钮
4. 填写版本号和项目备注
5. 上传成功

#### 3. 提交审核

1. 登录微信公众平台
2. 版本管理 → 开发版本
3. 选择刚上传的版本
4. 提交审核
5. 填写审核信息

#### 4. 发布上线

审核通过后，点击"发布"即可上线

**审核时间**：通常1-7个工作日

### Android App打包

#### 方式1：云打包（推荐）

1. **使用HBuilderX**
   - 发行 → 原生App-云打包
   - 选择Android平台
   - 填写应用信息
   - 使用DCloud证书（测试用）或自有证书
   - 点击打包

2. **等待打包完成**
   - 通常需要5-15分钟
   - 下载APK文件

3. **测试安装**
   ```bash
   # 安装到手机
   adb install app-release.apk
   ```

#### 方式2：本地打包（高级）

需要配置Android SDK环境，过程较复杂，建议使用云打包。

#### 签名配置（正式发布）

1. **生成密钥库**
   ```bash
   keytool -genkey -v -keystore smart-property.keystore -alias smart-property -keyalg RSA -keysize 2048 -validity 10000
   ```

2. **在HBuilderX中配置**
   - manifest.json → App原生插件配置
   - Android设置 → 使用自有证书
   - 选择密钥库文件

### iOS App打包

#### 必需条件
- Mac电脑
- Apple开发者账号（$99/年）
- Xcode

#### 打包步骤

1. **使用HBuilderX云打包**
   - 发行 → 原生App-云打包
   - 选择iOS平台
   - 上传证书和描述文件
   - 打包

2. **使用Xcode本地打包**
   - 导出iOS工程
   - 用Xcode打开
   - 配置证书
   - Archive打包

**提示**：iOS打包相对复杂，建议参考UniApp官方文档

---

## ❓ 常见问题

### Q1: 安装依赖失败

**错误信息**：
```
npm ERR! network timeout
```

**解决方法**：

```bash
# 切换到国内镜像
npm config set registry https://registry.npmmirror.com

# 重新安装
rm -rf node_modules package-lock.json
npm install

# 或使用pnpm（更快）
npm install -g pnpm
pnpm install
```

### Q2: H5页面无法访问后端API

**错误信息**：
```
Network Error
或
CORS policy
```

**解决方法**：

1. **确认后端已启动**
   ```bash
   curl http://localhost:8080
   ```

2. **检查代理配置**
   ```javascript
   // vite.config.js
   server: {
     proxy: {
       '/api': {
         target: 'http://localhost:8080',
         changeOrigin: true
       }
     }
   }
   ```

3. **重启开发服务器**
   ```bash
   # 停止服务器（Ctrl+C）
   npm run dev:h5
   ```

### Q3: 微信小程序无法发起请求

**错误信息**：
```
request:fail url not in domain list
```

**原因**：微信小程序有域名白名单限制

**解决方法**：

**开发阶段**：
```json
// manifest.json
"mp-weixin": {
  "setting": {
    "urlCheck": false  // 关闭域名校验
  }
}
```

**生产阶段**：
1. 登录微信公众平台
2. 开发管理 → 开发设置 → 服务器域名
3. 添加你的后端域名（必须HTTPS）
   ```
   https://api.yourdomain.com
   ```

### Q4: 页面样式错乱

**可能原因**：
- 不同平台样式差异
- rpx单位使用不当
- 使用了平台不支持的CSS

**解决方法**：

1. **使用条件编译**
   ```vue
   <style>
   /* #ifdef H5 */
   .box { padding: 20px; }
   /* #endif */
   
   /* #ifdef MP-WEIXIN */
   .box { padding: 40rpx; }
   /* #endif */
   </style>
   ```

2. **使用rpx替代px**
   ```css
   /* 不推荐 */
   .box { width: 375px; }
   
   /* 推荐 */
   .box { width: 750rpx; }
   ```

3. **测试多端效果**
   - H5浏览器测试
   - 微信开发者工具测试
   - 真机测试

### Q5: 图片无法显示

**可能原因**：
- 路径错误
- 网络图片协议问题
- 静态资源未正确引入

**解决方法**：

1. **本地图片使用绝对路径**
   ```vue
   <image src="/static/logo.png"></image>
   <!-- 或 -->
   <image src="@/static/logo.png"></image>
   ```

2. **网络图片使用HTTPS**
   ```vue
   <!-- 错误 -->
   <image src="http://example.com/image.jpg"></image>
   
   <!-- 正确 -->
   <image src="https://example.com/image.jpg"></image>
   ```

3. **动态图片路径**
   ```vue
   <template>
     <image :src="imageUrl"></image>
   </template>
   
   <script setup>
   const imageUrl = '/static/logo.png'
   </script>
   ```

### Q6: 打包后体积过大

**解决方法**：

1. **图片优化**
   - 压缩图片（TinyPNG）
   - 使用WebP格式
   - 使用CDN

2. **代码分割**
   ```javascript
   // 使用动态import
   const UserProfile = () => import('./components/UserProfile.vue')
   ```

3. **移除未使用的依赖**
   ```bash
   npm uninstall unused-package
   ```

4. **查看体积分析**
   ```bash
   npm run build:h5 -- --report
   ```

### Q7: 真机调试无法连接

**Android调试**：

1. 检查USB调试已开启
2. 确认驱动已安装
3. 使用命令验证：
   ```bash
   adb devices
   ```

**微信小程序真机调试**：

1. 手机和电脑在同一网络
2. 点击"预览"生成二维码
3. 用微信扫码
4. 允许调试模式

---

## 📱 页面功能说明

### 主要页面

| 页面 | 路径 | 功能 |
|------|------|------|
| 首页 | pages/index/index | 房源推荐、快速搜索 |
| 搜索 | pages/search/search | 房源搜索、筛选 |
| 房源详情 | pages/property/detail | 房源详细信息、预约 |
| AI问答 | pages/ai/qa | 智能问答助手 |
| AI推荐 | pages/ai/recommend | 个性化推荐 |
| 个人中心 | pages/user/profile | 用户信息、设置 |
| 登录注册 | pages/user/login | 用户登录 |
| 发布房源 | pages/landlord/publish | 房东发布房源 |
| 房源管理 | pages/landlord/manage | 房东管理房源 |
| 我的收藏 | pages/favorites/index | 收藏的房源 |

### TabBar导航

- **首页**：房源推荐、快速搜索
- **搜索**：高级筛选、地图找房
- **AI助手**：智能问答、智能推荐
- **我的**：个人中心、我的收藏

---

## 🔧 项目结构

```
uniapp/
├── src/
│   ├── pages/                    # 页面目录
│   │   ├── index/               # 首页
│   │   ├── search/              # 搜索页
│   │   ├── property/            # 房源相关
│   │   ├── ai/                  # AI功能
│   │   ├── user/                # 用户相关
│   │   ├── landlord/            # 房东功能
│   │   └── favorites/           # 收藏
│   ├── components/               # 组件目录
│   ├── static/                   # 静态资源
│   │   ├── images/              # 图片
│   │   └── icons/               # 图标
│   ├── utils/                    # 工具函数
│   │   ├── request.js           # API请求封装
│   │   └── common.js            # 通用函数
│   ├── api/                      # API接口
│   │   ├── property.js          # 房源接口
│   │   ├── user.js              # 用户接口
│   │   └── ai.js                # AI接口
│   ├── store/                    # 状态管理
│   ├── App.vue                   # 应用入口
│   ├── main.js                   # 主文件
│   ├── manifest.json             # 应用配置
│   ├── pages.json                # 页面配置
│   └── uni.scss                  # 全局样式变量
├── dist/                         # 编译输出（自动生成）
│   ├── dev/                     # 开发版本
│   └── build/                   # 生产版本
├── node_modules/                 # 依赖包
├── package.json                  # 项目配置
├── vite.config.js                # Vite配置
└── README.md                     # 项目说明
```

---

## 🎨 开发规范

### 1. 命名规范

**页面文件**：小写+连字符
```
pages/user-profile/user-profile.vue
```

**组件文件**：大驼峰
```
components/PropertyCard.vue
```

**变量命名**：小驼峰
```javascript
const userName = 'John'
const propertyList = []
```

### 2. 样式规范

**使用rpx单位**（响应式）：
```css
.container {
  width: 750rpx;      /* 屏幕宽度 */
  padding: 30rpx;     /* 内边距 */
}
```

**全局样式变量**（uni.scss）：
```scss
$primary-color: #409EFF;
$text-color: #333333;
```

### 3. API调用规范

```javascript
import { getPropertyList } from '@/api/property'

// 使用async/await
async function loadData() {
  try {
    const res = await getPropertyList({ page: 1, size: 10 })
    console.log(res.data)
  } catch (error) {
    console.error('加载失败', error)
  }
}
```

### 4. 条件编译

针对不同平台编写代码：

```vue
<template>
  <!-- H5平台 -->
  <!-- #ifdef H5 -->
  <div class="h5-only">H5专用内容</div>
  <!-- #endif -->
  
  <!-- 微信小程序 -->
  <!-- #ifdef MP-WEIXIN -->
  <view class="weixin-only">微信专用内容</view>
  <!-- #endif -->
  
  <!-- App平台 -->
  <!-- #ifdef APP-PLUS -->
  <view class="app-only">App专用内容</view>
  <!-- #endif -->
</template>

<script>
// #ifdef H5
console.log('H5平台')
// #endif

// #ifdef MP-WEIXIN
console.log('微信小程序')
// #endif
</script>
```

---

## 📚 学习资源

### 官方文档

- [UniApp官方文档](https://uniapp.dcloud.io/)
- [Vue 3文档](https://vuejs.org/)
- [微信小程序开发文档](https://developers.weixin.qq.com/miniprogram/dev/framework/)

### 推荐教程

- UniApp从入门到精通
- Vue 3 + TypeScript实战
- 微信小程序开发指南

### 社区资源

- [DCloud论坛](https://ask.dcloud.net.cn/)
- [UniApp插件市场](https://ext.dcloud.net.cn/)

---

## 🔄 版本更新

### v1.0.0 (2026-06-17)

- ✅ 初始版本发布
- ✅ 支持H5、微信小程序、App
- ✅ 完整的房源浏览功能
- ✅ AI智能助手集成
- ✅ 用户系统完善

---

## 📞 技术支持

如遇到问题，请提供以下信息：

1. **运行平台**：H5/微信小程序/App
2. **错误截图**：控制台错误信息
3. **操作步骤**：详细的复现步骤
4. **设备信息**：手机型号、系统版本

---

## 🎉 快速命令参考

```bash
# 安装依赖
npm install

# H5开发
npm run dev:h5

# 微信小程序开发
npm run dev:mp-weixin

# App开发
npm run dev:app

# H5打包
npm run build:h5

# 微信小程序打包
npm run build:mp-weixin

# 查看所有命令
npm run
```

---

**最后更新**：2026-06-17  
**版本**：v1.0.0

**祝你开发愉快！🎉**
