# 安居客房源爬虫使用说明

## 目录结构

```
crawler/
├── anjuke_spider.py    # 核心爬虫脚本（单次爬取）
├── auto_crawler.py     # 自动调度爬虫（定时重复执行）
└── README.md           # 使用说明文档
```

## 环境要求

- Python 3.7+
- 依赖库：
  ```bash
  pip install requests beautifulsoup4 pymysql lxml
  ```

## 数据库配置

爬虫默认连接本地 MySQL 数据库：
- **host**: 127.0.0.1
- **port**: 3306
- **username**: root
- **password**: 123456
- **database**: smart_property_db

如需修改配置，请编辑 `anjuke_spider.py` 中的 `DB_CONFIG` 变量。

## 核心爬虫脚本：anjuke_spider.py

### 基本用法

```bash
# 爬取 20 条出售房源（默认），随机 5 个城市
python anjuke_spider.py

# 爬取指定数量的房源
python anjuke_spider.py [房源总数] [城市数] [交易类型]
```

### 参数说明

| 参数 | 说明 | 默认值 |
|------|------|--------|
| 房源总数 | 要爬取的房源总条数 | 20 |
| 城市数 | 随机选择的城市数量，0 表示只爬佛山 | 5 |
| 交易类型 | 0=出租, 1=出售, 2=全部 | 1 |

### 使用示例

```bash
# 爬取 10 条出租房源，只爬佛山
python anjuke_spider.py 10 0 0

# 爬取 50 条出售房源，随机 10 个城市
python anjuke_spider.py 50 10 1

# 爬取 30 条房源（出租+出售各半），随机 3 个城市
python anjuke_spider.py 30 3 2
```

## 自动调度爬虫：auto_crawler.py

### 基本用法

```bash
# 默认：每 60 秒执行一次，20 个城市，每城市 20 条，随机交易类型
python auto_crawler.py

# 自定义参数
python auto_crawler.py [间隔秒数] [城市数] [每城市条数] [交易类型]
```

### 参数说明

| 参数 | 说明 | 默认值 |
|------|------|--------|
| 间隔秒数 | 每次爬取之间的等待时间 | 60 |
| 城市数 | 随机选择的城市数量 | 20 |
| 每城市条数 | 每个城市爬取的房源数量 | 20 |
| 交易类型 | 0=出租, 1=出售, 2=随机 | 2 |

### 使用示例

```bash
# 每 30 秒爬一次，10 个城市，每城市 5 条，随机交易类型
python auto_crawler.py 30 10 5

# 每 120 秒爬一次，5 个城市，每城市 10 条，只爬出租
python auto_crawler.py 120 5 10 0

# 每 60 秒爬一次，20 个城市，每城市 20 条，只爬出售
python auto_crawler.py 60 20 20 1
```

### 停止方法

按 `Ctrl+C` 停止自动调度器。

## Cookie 配置

爬虫使用安居客网站的 Cookie 来绕过反爬机制。Cookie 在 `anjuke_spider.py` 的 `RAW_COOKIE` 变量中配置。

### 获取 Cookie 的方法

1. 打开浏览器，访问 [佛山出租页面](https://fs.zu.anjuke.com/)
2. 登录账号（如有需要）
3. 打开浏览器开发者工具（F12）
4. 在 "Application" 或 "存储" 标签中找到 Cookie
5. 复制所有 Cookie 值，替换 `RAW_COOKIE` 变量中的内容

## 支持的城市

爬虫支持以下城市的房源爬取：

**一线城市**
- 北京、上海、广州、深圳

**长三角核心**
- 苏州、无锡、杭州、南京、宁波、嘉兴、湖州、绍兴、温州、南通、常州、盐城、合肥

**珠三角核心**
- 佛山、东莞、珠海、中山、惠州、江门、肇庆

**其他主要城市**
- 成都、武汉、西安、天津、重庆、长沙、青岛、济南、郑州、沈阳、大连、厦门、福州、哈尔滨、昆明

## 爬取结果

爬取的房源数据会写入 MySQL 数据库的 `property` 表，包含以下字段：

| 字段 | 说明 |
|------|------|
| title | 房源标题 |
| description | 房源描述 |
| price | 价格（出售为总价，出租为年租金） |
| price_type | 价格类型（1=总价, 2=月租） |
| area | 面积（㎡） |
| bedrooms | 卧室数量 |
| bathrooms | 卫生间数量 |
| orientation | 朝向 |
| floor | 楼层 |
| address | 详细地址 |
| city | 城市 |
| district | 区域 |
| images | 图片URL（逗号分隔） |
| transaction_type | 交易类型（0=出租, 1=出售） |
| status | 状态（3=已上架） |
| view_count | 浏览次数 |

## 注意事项

1. **反爬机制**：安居客有反爬机制，建议：
   - 设置合理的请求间隔（建议 ≥ 30 秒）
   - 使用有效的 Cookie
   - 不要过于频繁地爬取

2. **Cookie 有效期**：Cookie 可能会过期，如遇到爬取失败，需要重新获取。

3. **数据完整性**：部分房源可能缺少图片或详细信息，爬虫会自动跳过无实图的房源。

4. **数据库连接**：确保 MySQL 服务已启动，且数据库 `smart_property_db` 已创建。

## 故障排除

**问题1：连接数据库失败**
- 检查 MySQL 服务是否启动
- 检查数据库配置是否正确
- 确保数据库 `smart_property_db` 已创建

**问题2：爬取不到房源**
- 检查 Cookie 是否有效
- 尝试更换 Cookie
- 检查网络连接

**问题3：房源图片为空**
- 网站可能更新了页面结构
- 检查房源详情页是否能正常访问

## 许可证

本项目仅供学习和研究使用，请遵守网站的使用条款和相关法律法规。
