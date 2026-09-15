# 基于AI的智能房产交易平台 - 完整使用文档

![版本](https://img.shields.io/badge/version-1.0.0-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.9-green)
![Vue](https://img.shields.io/badge/Vue-3.x-brightgreen)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)

---

## 📖 目录

- [项目简介](#项目简介)
- [功能特性](#功能特性)
- [技术架构](#技术架构)
- [环境要求](#环境要求)
- [快速开始](#快速开始)
- [详细配置](#详细配置)
- [API文档](#api文档)
- [常见问题](#常见问题)
- [测试账号](#测试账号)

---

## 📝 项目简介

这是一个现代化的智能房产交易平台，整合了AI技术，为用户提供智能化的房源搜索、推荐、问答等服务。系统支持租房和买房两种交易模式，提供完整的交易流程管理。

### 🎯 适用场景

- **毕业设计项目**：完整的前后端分离架构，集成AI技术
- **学习参考**：Spring Boot + Vue 3 + AI集成的完整示例
- **二次开发**：可扩展的架构设计，便于定制化开发

---

## ✨ 功能特性

### 🏠 核心业务功能

#### 1. 用户管理
- ✅ 用户注册/登录（支持邮箱/手机号）
- ✅ 用户信息管理
- ✅ 多角色权限（租客、房东、管理员）
- ✅ 密码修改与找回

#### 2. 房源管理
- ✅ 房源发布（房东）
- ✅ 房源浏览（列表/卡片/地图视图）
- ✅ 房源详情展示（图片轮播、VR看房）
- ✅ 多维度搜索筛选
- ✅ 房源收藏功能
- ✅ 房源审核（管理员）

#### 3. 预约看房
- ✅ 在线预约看房
- ✅ 预约状态管理（待确认/已确认/已完成/已取消）
- ✅ 预约提醒通知
- ✅ 预约历史查询

#### 4. 交易流程管理 ⭐

**租房流程（9个阶段）**
1. 预约看房
2. 看房确认
3. 意向谈价
4. 签订合同
5. 合同备案
6. 交付入住
7. 租期中
8. 续租/退房
9. 已完成

**买房流程（12个阶段）**
1. 预约看房
2. 看房确认
3. 意向谈价
4. 签订意向书
5. 支付定金
6. 签订购房合同
7. 支付首付款
8. 网签备案
9. 贷款审批
10. 产权过户
11. 交房验收
12. 已完成

**流程功能**
- ✅ 可视化进度条展示
- ✅ 流程操作日志记录
- ✅ 阶段推进功能
- ✅ 交易取消功能
- ✅ 合同管理
- ✅ 支付记录管理

### 🤖 AI智能功能

#### 1. AI智能问答
- ✅ 房产相关问题智能解答
- ✅ 多轮对话支持
- ✅ 对话历史保存
- ✅ 基于DeepSeek V4模型

#### 2. AI文案生成
- ✅ 自动生成房源推广文案
- ✅ 简短版/详细版可选
- ✅ 文案历史管理
- ✅ 一键复制分享

#### 3. 智能推荐
- ✅ 基于用户行为分析
- ✅ 多维度评分推荐
- ✅ 个性化推荐列表
- ✅ 实时推荐更新

### 📱 多端支持

- ✅ **Web端**：响应式设计，支持PC和平板
- ✅ **移动端**：UniApp开发，支持微信小程序/H5/App

---

## 🏗️ 技术架构

### 后端技术栈

```
Spring Boot 3.1.9
├── Spring Security        # 安全认证
├── MyBatis-Plus 3.5.5    # ORM框架
├── MySQL 8.0             # 数据库
├── JWT                   # Token认证
├── Lombok                # 代码简化
└── Validation            # 参数校验
```

### 前端技术栈

```
Vue 3
├── TypeScript            # 类型系统
├── Vite 4                # 构建工具
├── Element Plus          # UI组件库
├── Pinia                 # 状态管理
├── Vue Router 4          # 路由管理
└── Axios                 # HTTP客户端
```

### 移动端技术栈

```
UniApp
├── Vue 3
├── uni-ui                # UI组件
├── uView Plus            # UI框架
└── Vuex                  # 状态管理
```

### AI服务

```
火山方舟（Volcengine）
└── DeepSeek V4 Pro       # 大语言模型
```

---

## 💻 环境要求

### 必需软件

| 软件 | 版本要求 | 说明 |
|------|---------|------|
| **JDK** | 17+ | Java开发环境 |
| **Node.js** | 16+ | 前端运行环境 |
| **MySQL** | 8.0+ | 数据库 |
| **Maven** | 3.6+ | 后端构建工具 |

### 推荐IDE

- **后端开发**：IntelliJ IDEA 2023+
- **前端开发**：Visual Studio Code
- **数据库管理**：Navicat Premium / MySQL Workbench

---

## 🚀 快速开始

### 步骤1：克隆项目

```bash
# 假设项目已在本地
cd c:\Users\34588\Desktop\ai租房test\ai-intelligent-rental-housing\code\ai租房系统
```

### 步骤2：配置数据库

#### 2.1 创建数据库

```bash
# 打开MySQL命令行
mysql -u root -p
```

```sql
-- 创建数据库
CREATE DATABASE smart_property CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE smart_property;
```

#### 2.2 导入数据表

```bash
# 在项目根目录执行
mysql -u root -p smart_property < database/smart_property_db.sql
```

**验证导入成功**：
```sql
-- 查看所有表
SHOW TABLES;

-- 应该看到以下表：
-- user, property, appointment, favorite, message等
```

#### 2.3 修改数据库配置

编辑文件：`backend/src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/smart_property?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 你的MySQL密码    # ⚠️ 修改这里
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 步骤3：启动后端服务

#### 方式1：使用IDE（推荐）

1. 使用IntelliJ IDEA打开 `backend` 目录
2. 等待Maven依赖下载完成
3. 找到 `SmartPropertyApplication.java` 主类
4. 右键点击 → Run 'SmartPropertyApplication'

#### 方式2：使用命令行

```bash
# 进入后端目录
cd backend

# 使用Maven启动
mvn spring-boot:run

# 或者先编译再运行
mvn clean package -DskipTests
java -jar target/smart-property-platform-1.0.0.jar
```

**启动成功标志**：

```
==========================================
智能房产交易平台启动成功！
后端服务地址：http://localhost:8080
==========================================
```


### 步骤4：启动前端服务

**打开新的终端窗口**：

```bash
# 进入前端目录
cd frontend

# 安装依赖（首次运行）
npm install

# 启动开发服务器
npm run dev
```

**启动成功标志**：

```
VITE v4.x.x  ready in xxx ms

➜  Local:   http://localhost:5173/
➜  Network: use --host to expose
➜  press h to show help
```

### 步骤5：访问系统

打开浏览器访问：**http://localhost:5173**

---

## ⚙️ 详细配置

### 后端配置文件说明

文件位置：`backend/src/main/resources/application.yml`

```yaml
# 服务器配置
server:
  port: 8080                      # 后端服务端口
  servlet:
    context-path: /               # 应用上下文路径

# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/smart_property
    username: root                # 数据库用户名
    password: 123456              # 数据库密码（⚠️必改）
    driver-class-name: com.mysql.cj.jdbc.Driver

  # 文件上传配置
  servlet:
    multipart:
      max-file-size: 10MB        # 单个文件最大大小
      max-request-size: 50MB     # 请求最大大小

# MyBatis-Plus配置
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true  # 驼峰命名转换
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # SQL日志
  global-config:
    db-config:
      logic-delete-field: deleted  # 逻辑删除字段
      logic-delete-value: 1
      logic-not-delete-value: 0

# 日志配置
logging:
  level:
    com.smartproperty: INFO      # 项目日志级别
  file:
    name: logs/smart-property.log
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{50} - %msg%n"

# AI功能配置（可选）
smart-property:
  ai:
    enabled: true                 # 是否启用AI功能
    zhipu:
      api-key: your-api-key       # 火山方舟API密钥
      model: deepseek-v4-pro-260425
    qa:
      enabled: true
      max-history: 20             # 最大历史记录数
    content:
      enabled: true
      max-length: 500             # 最大文案长度
    recommendation:
      enabled: true
      default-limit: 10           # 默认推荐数量
```

### 前端配置文件说明

文件位置：`frontend/src/config/index.ts`（或环境变量文件）

```typescript
// API基础地址配置
export const API_BASE_URL = 'http://localhost:8080/api'

// 文件上传地址
export const UPLOAD_URL = 'http://localhost:8080/api/upload'

// 分页配置
export const PAGE_SIZE = 10

// 图片预览配置
export const IMAGE_PREVIEW_SIZE = 5
```

### AI功能配置（可选）

#### 获取API密钥

1. 访问火山方舟官网：https://www.volcengine.com/
2. 注册并登录账号
3. 进入"模型推理"服务
4. 创建API密钥
5. 复制密钥到配置文件

#### 配置示例

```yaml
smart-property:
  ai:
    enabled: true
    zhipu:
      api-key: ark-xxxxxxxxxxxxxxxxxxxxx
      model: deepseek-v4-pro-260425
```

**注意**：如果不配置AI功能，系统仍可正常运行，只是AI相关功能不可用。

---

## 📂 项目结构

```
ai租房系统/
├── backend/                          # 后端项目（Spring Boot）
│   ├── src/main/
│   │   ├── java/com/smartproperty/
│   │   │   ├── controller/          # 控制器层（API接口）
│   │   │   │   ├── UserController.java
│   │   │   │   ├── PropertyController.java
│   │   │   │   ├── AppointmentController.java
│   │   │   │   ├── TransactionProcessController.java
│   │   │   │   └── AIController.java
│   │   │   ├── service/             # 业务逻辑层
│   │   │   │   ├── impl/            # 实现类
│   │   │   │   └── ...
│   │   │   ├── mapper/              # 数据访问层
│   │   │   ├── entity/              # 实体类
│   │   │   ├── dto/                 # 数据传输对象
│   │   │   ├── enums/               # 枚举类
│   │   │   ├── common/              # 公共类
│   │   │   └── config/              # 配置类
│   │   └── resources/
│   │       ├── mapper/              # MyBatis XML映射文件
│   │       ├── application.yml      # 主配置文件
│   │       └── logback-spring.xml   # 日志配置
│   ├── logs/                         # 日志目录
│   └── pom.xml                       # Maven配置文件
│
├── frontend/                         # 前端项目（Vue 3）
│   ├── src/
│   │   ├── views/                   # 页面组件
│   │   │   ├── Home.vue             # 首页
│   │   │   ├── PropertyList.vue     # 房源列表
│   │   │   ├── PropertyDetail.vue   # 房源详情
│   │   │   ├── Appointment.vue      # 预约管理
│   │   │   ├── TransactionProcess.vue  # 流程管理
│   │   │   └── AIAssistant.vue      # AI助手
│   │   ├── components/              # 通用组件
│   │   ├── router/                  # 路由配置
│   │   ├── store/                   # 状态管理
│   │   ├── api/                     # API接口封装
│   │   ├── assets/                  # 静态资源
│   │   └── utils/                   # 工具函数
│   ├── public/                       # 公共静态文件
│   ├── package.json                  # NPM配置
│   └── vite.config.ts                # Vite配置
│
├── uniapp/                           # 移动端项目（UniApp）
│   ├── src/
│   │   ├── pages/                   # 页面
│   │   ├── components/              # 组件
│   │   ├── static/                  # 静态资源
│   │   └── api/                     # API接口
│   └── package.json
│
├── database/                         # 数据库脚本
│   └── smart_property_db.sql         # 数据库初始化脚本
│
└── README.md                         # 本文档
```

---

## 🔌 API文档

### 基础信息

- **基础路径**：`http://localhost:8080/api`
- **认证方式**：JWT Token（在Header中携带）
- **响应格式**：统一JSON格式

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

### 用户相关接口

#### 1. 用户登录
```http
POST /api/user/login
Content-Type: application/json

{
  "username": "user1",
  "password": "123456"
}
```

#### 2. 用户注册
```http
POST /api/user/register
Content-Type: application/json

{
  "username": "newuser",
  "password": "123456",
  "email": "user@example.com",
  "phone": "13800138000"
}
```

#### 3. 获取用户信息
```http
GET /api/user/info
Authorization: Bearer <token>
```

### 房源相关接口

#### 1. 房源列表
```http
GET /api/property/list?page=1&size=10&city=北京&district=朝阳区
```

#### 2. 房源详情
```http
GET /api/property/{id}
```

#### 3. 发布房源
```http
POST /api/property
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "朝阳区三居室",
  "price": 8000,
  "area": 120,
  "bedrooms": 3,
  "bathrooms": 2,
  "address": "北京市朝阳区...",
  ...
}
```

### 预约相关接口

#### 1. 创建预约
```http
POST /api/appointment
Authorization: Bearer <token>
Content-Type: application/json

{
  "propertyId": 1,
  "appointmentTime": "2026-06-20 14:00:00",
  "contactPhone": "13800138000",
  "remark": "想周末看房"
}
```

#### 2. 预约列表
```http
GET /api/appointment/user?status=0
Authorization: Bearer <token>
```

### 流程相关接口

#### 1. 初始化流程
```http
POST /api/transaction-process/init
Authorization: Bearer <token>
Content-Type: application/json

{
  "appointmentId": 1,
  "transactionType": 0
}
```

#### 2. 获取流程详情
```http
GET /api/transaction-process/{id}
Authorization: Bearer <token>
```

#### 3. 推进流程
```http
POST /api/transaction-process/next-stage
Authorization: Bearer <token>
Content-Type: application/json

{
  "processId": 1,
  "operationNote": "双方已确认看房"
}
```

### AI相关接口

#### 1. AI问答
```http
POST /api/ai/qa
Authorization: Bearer <token>
Content-Type: application/json

{
  "question": "买房需要注意什么？",
  "conversationId": "uuid"
}
```

#### 2. 生成文案
```http
POST /api/ai/generate-content
Authorization: Bearer <token>
Content-Type: application/json

{
  "propertyId": 1,
  "contentType": "short"
}
```

#### 3. 智能推荐
```http
GET /api/ai/recommend?limit=10
Authorization: Bearer <token>
```

---

## ❓ 常见问题

### Q1: 后端启动失败 - 端口被占用

**错误信息**：
```
Web server failed to start. Port 8080 was already in use.
```

**解决方法**：

Windows:
```bash
# 查找占用端口的进程
netstat -ano | findstr ":8080"

# 结束进程
taskkill /F /PID <进程ID>
```

Linux/Mac:
```bash
# 查找占用端口的进程
lsof -i :8080

# 结束进程
kill -9 <进程ID>
```

或修改端口号：
```yaml
# application.yml
server:
  port: 8081  # 修改为其他端口
```

### Q2: 数据库连接失败

**错误信息**：
```
Access denied for user 'root'@'localhost'
```

**解决方法**：

1. 检查MySQL服务是否启动
```bash
# Windows
net start MySQL80

# Linux
systemctl start mysql
```

2. 验证用户名密码
```bash
mysql -u root -p
# 输入密码测试是否能登录
```

3. 修改配置文件
```yaml
spring:
  datasource:
    username: root
    password: 你的实际密码
```

### Q3: 前端启动失败 - 依赖安装错误

**解决方法**：

```bash
# 清除缓存
rm -rf node_modules package-lock.json

# 重新安装
npm install

# 如果还是失败，尝试使用国内镜像
npm config set registry https://registry.npmmirror.com
npm install
```

### Q4: 前端无法访问后端API

**问题原因**：跨域或后端未启动

**解决方法**：

1. 确认后端已启动（访问 http://localhost:8080）
2. 检查前端API配置是否正确
3. 后端已配置CORS，如仍有问题检查：

```java
// WebConfig.java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
        .allowedOrigins("http://localhost:5173")
        .allowedMethods("*")
        .allowCredentials(true);
}
```

### Q5: AI功能不可用

**原因**：未配置API密钥或网络问题

**解决方法**：

1. 检查配置文件中的API密钥
```yaml
smart-property:
  ai:
    enabled: true
    zhipu:
      api-key: your-actual-key  # 检查是否正确
```

2. 查看后端日志
```bash
tail -f backend/logs/smart-property.log
```

3. 如暂时不需要AI功能，可以禁用：
```yaml
smart-property:
  ai:
    enabled: false
```

### Q6: 流程页面无法加载

**解决方法**：

1. 确认已导入数据库脚本
2. 检查是否有Mapper XML文件
3. 从预约页面启动流程
4. 查看后端日志排查问题

---

## 👥 测试账号

系统已预置测试账号供开发使用：

### 普通用户账号
```
用户名：user1
密码：123456
角色：租客
权限：浏览房源、预约看房、查看流程
```

### 房东账号
```
用户名：landlord1
密码：123456
角色：房东
权限：发布房源、管理预约、管理流程、查看收益
```

### 管理员账号
```
用户名：admin
密码：123456
角色：管理员
权限：系统管理、用户管理、房源审核、数据统计
```

---

## 🛠️ 开发调试

### 后端调试

#### 启用详细日志
```yaml
logging:
  level:
    com.smartproperty: DEBUG
    org.springframework.web: DEBUG
```

#### 查看SQL日志
```yaml
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

#### 使用IDE断点调试
1. 在关键代码行打断点
2. 以Debug模式启动应用
3. 通过前端触发接口
4. 在断点处查看变量值

### 前端调试

#### 浏览器开发者工具
- 按F12打开DevTools
- Network标签：查看API请求
- Console标签：查看JavaScript日志
- Vue DevTools：查看组件状态

#### 查看API请求
```javascript
// 在api文件中添加请求/响应拦截器
axios.interceptors.request.use(config => {
  console.log('请求：', config)
  return config
})

axios.interceptors.response.use(response => {
  console.log('响应：', response)
  return response
})
```

---

## 📦 打包部署

### 前端打包

```bash
cd frontend

# 构建生产版本
npm run build

# 生成的文件在 dist/ 目录
# 将dist目录部署到Nginx或其他Web服务器
```

**Nginx配置示例**：
```nginx
server {
    listen 80;
    server_name yourdomain.com;
    
    location / {
        root /path/to/dist;
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 后端打包

```bash
cd backend

# 打包项目
mvn clean package -DskipTests

# 运行JAR文件
java -jar target/smart-property-platform-1.0.0.jar

# 后台运行
nohup java -jar target/smart-property-platform-1.0.0.jar > app.log 2>&1 &
```

---

## 📚 相关文档

- [Spring Boot官方文档](https://spring.io/projects/spring-boot)
- [Vue 3官方文档](https://vuejs.org/)
- [Element Plus文档](https://element-plus.org/)
- [MyBatis-Plus文档](https://baomidou.com/)
- [UniApp文档](https://uniapp.dcloud.io/)

---

## 📮 技术支持

### 问题反馈

如遇到问题，请提供以下信息：
1. 错误信息截图
2. 后端日志（`backend/logs/smart-property.log`）
3. 浏览器控制台截图
4. 详细的操作步骤

---

## 📄 许可证

本项目仅供学习和研究使用。

---

## 🎉 快速命令参考

```bash
# 数据库初始化
mysql -u root -p smart_property < database/smart_property_db.sql

# 启动后端
cd backend && mvn spring-boot:run

# 启动前端
cd frontend && npm run dev

# 后端打包
cd backend && mvn clean package -DskipTests

# 前端打包
cd frontend && npm run build

# 查看后端日志
tail -f backend/logs/smart-property.log
```

---

**最后更新**：2026-06-17  
**版本**：v1.0.0

**祝你使用愉快！如有问题，请参考文档或查看日志排查。🎉**
