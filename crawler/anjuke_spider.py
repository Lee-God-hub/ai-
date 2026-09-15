# -*- coding: utf-8 -*-
"""
安居客二手房源信息爬虫 + 写入 MySQL 数据库
------------------------------------------------------------------------
数据库连接（与项目 Spring Boot 配置一致）：
    host     : 127.0.0.1
    port     : 3306
    username : root
    password : 123456
    database : smart_property_db

字段映射表：
    爬虫字段            →  数据库字段 (property)
    ----------------      -------------------------------------------
    房源标题           →  title
    标题 + 地址拼接     →  description
    价格 (总价)          →  price (数字) + price_type = 1 (总价)
    面积 (㎡)          →  area
    卧室数量           →  bedrooms
    卫生间数量         →  bathrooms
    朝向                 →  orientation
    楼层                 →  floor
    详细地址             →  address
    城市 (中文)          →  city
    区域 (中文)          →  district
    图片URL              →  images
    (随机)               →  landlord_id (从现有房东ID中随机选)
    (随机)               →  property_type (0=住宅,1=公寓,2=别墅,3=商铺)
    1 (二手房)           →  transaction_type (0=出租,1=出售)
    3 (已上架)           →  status
    (随机)               →  view_count
------------------------------------------------------------------------
依赖：
    pip install requests beautifulsoup4 pymysql lxml
"""

import time
import re
import random
import requests
from requests.adapters import HTTPAdapter
from urllib3.util.retry import Retry
from bs4 import BeautifulSoup

try:
    import pymysql
except ImportError:
    print('[错误] 请先安装 pymysql：pip install pymysql')
    exit(1)


# ========================== 全局配置 ==========================

# ---------- 爬虫配置 ----------
HEADERS = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36 Edg/132.0.0.0',
    'Referer': 'https://member.anjuke.com/',
    'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7',
}

# 从浏览器复制的 cookie，保持登录态用
RAW_COOKIE = """
aQQ_ajkguid=2B259D12-5CE2-EC13-6345-A0A8F175E517; ajk-appVersion=; id58=uVUu4moAgZA1AwAVLsSuAg==; xxzlclientid=e854eb29-68ca-409f-909d-1778418071361; xxzlxxid=pfmx+7oZFqQV2XC3m8Kx4aQz1VtJpMHAWcWD2yBNqqFrrY4d1IhQKFMERKx4M4/2Kqoe; sessid=F538B779-E853-4EA3-8B76-67E964B9FF72; ctid=24; obtain_by=2; twe=2; fzq_h=983c4b57739e8346a446ce5fa16f4be0_1782181489459_026289bce8054369a96dc97793e02ed5_47896430871578968360808942008879339024; lps=https%3A%2F%2Ffs.zu.anjuke.com%2F%3Ffrom%3DHomePage_TopBar%7Chttps%3A%2F%2Ffoshan.anjuke.com%2F; cmctid=222; wmda_session_id_6289197098934=1782181495789-5e35778a-8ea8-0e49; wmda_visited_projects=%3B6289197098934; wmda_uuid=a26ff39e719b1627185823f1a1894b06; wmda_new_uuid=1; xxzlbbid=pfmbRM6udcMEdmtW+GKDzAeVxH49vX5q+8+Y9qHIuW8aTG/yUToDZwKhJBgFQmq/mY35yGAIpqCF0UFKfR82nRNzRUTiFZ8bDX8myb/+Jtjuh9gu6z895CH5HB3e1m6d/t/gB8qYsbIxNzgyMTgxNTkyMTExMjMy_1
"""

# 列表页地址模板
# sale = 出售房源, zu = 出租房源
BASE_URL_TEMPLATE_SALE = 'https://{city_py}.anjuke.com/sale/p{{page}}/'
BASE_URL_TEMPLATE_RENT = 'https://{city_py}.anjuke.com/zu/p{{page}}/'
REQUEST_DELAY = 0.8

def build_list_url(city_py, page, transaction_type):
    """根据城市拼音、页码和交易类型生成安居客房源列表页 URL"""
    if transaction_type == 0:
        # 出租房源URL格式 - 使用 fs.zu.anjuke.com 格式
        if page == 1:
            return f'https://{city_py[0:2]}.zu.anjuke.com/'
        else:
            return f'https://{city_py[0:2]}.zu.anjuke.com/p{page}/'
    else:
        # 出售房源URL格式
        return f'https://{city_py}.anjuke.com/sale/p{page}/'

# ---------- 数据库配置（与 Spring Boot 保持一致）----------
DB_CONFIG = {
    'host': '127.0.0.1',
    'port': 3306,
    'user': 'root',
    'password': '123456',
    'database': 'smart_property_db',
    'charset': 'utf8mb4',
}

# ---------- 城市拼音 → 中文名映射（经过验证有真实房源的城市）----------
# 安居客URL格式：https://{city_py}.anjuke.com/sale/p{page}/
# 注意：以下城市经过实际爬取验证，能获取到对应城市的真实房源
CITY_PY2CN = {
    # 一线城市（房源多、质量高）
    'beijing': '北京', 'shanghai': '上海',
    'guangzhou': '广州', 'shenzhen': '深圳',
    # 长三角核心
    'suzhou': '苏州', 'wuxi': '无锡', 'hangzhou': '杭州',
    'nanjing': '南京', 'ningbo': '宁波', 'jiaxing': '嘉兴',
    'huzhou': '湖州', 'shaoxing': '绍兴', 'wenzhou': '温州',
    'nantong': '南通', 'changzhou': '常州', 'yancheng': '盐城',
    'hefei': '合肥',
    # 珠三角核心
    'foshan': '佛山', 'dongguan': '东莞', 'zhuhai': '珠海',
    'zhongshan': '中山', 'huizhou': '惠州', 'jiangmen': '江门',
    'zhaoqing': '肇庆',
    # 其他主要城市
    'chengdu': '成都', 'wuhan': '武汉', 'xian': '西安',
    'tianjin': '天津', 'chongqing': '重庆', 'changsha': '长沙',
    'qingdao': '青岛', 'jinan': '济南', 'zhengzhou': '郑州',
    'shenyang': '沈阳', 'dalian': '大连', 'xiamen': '厦门',
    'fuzhou': '福州', 'harbin': '哈尔滨', 'kunming': '昆明',
}


def get_random_cities(n=None, seed=None):
    """
    从城市列表中随机选出 n 个城市。
    - n=None 或 n>=总数 时，返回全部城市（打乱顺序）
    - n<总数时，随机抽 n 个
    - seed 为随机种子（用于复现结果）
    返回：[(city_py, city_cn), ...]
    """
    items = list(CITY_PY2CN.items())
    if seed is not None:
        rng = random.Random(seed)
    else:
        rng = random
    rng.shuffle(items)
    if n is None or n >= len(items):
        return items
    return items[:n]


def pick_one_random_city(seed=None):
    """随机选1个城市，返回 (city_py, city_cn)"""
    return get_random_cities(n=1, seed=seed)[0]

# ---------- 已有房东 ID 列表（role=1 的用户，启动时还会自动从数据库核对）----------
LANDLORD_IDS = [2, 3, 7, 8, 9, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24]

# ---------- 房源描述模板（用于随机补全 description）----------
DESC_TEMPLATES = [
    '位于{district}核心地段，交通便利，周边配套成熟，生活购物一站式体验。',
    '小区环境优雅，绿化面积大，物业服务贴心到位。室内户型方正，采光通风良好。',
    '房东诚意出售，价格合理，产权清晰，过户无忧。可随时预约看房。',
    '建筑质量优秀，层高宽敞，布局合理，南北通透，日照充足。',
    '近地铁站，多条公交线路交汇，出行方便。周边商场、医院、学校、公园一应俱全。',
]

# ---------- 出租房源标题模板 ----------
RENT_TITLE_TEMPLATES = [
    '{district}精装{bedrooms}室，拎包入住',
    '{district}地铁口{bedrooms}房，采光好',
    '{community}温馨{bedrooms}室，家电齐全',
    '{district}商圈旁{bedrooms}室，交通便利',
    '{community}精装{bedrooms}室{bathrooms}卫，随时看房',
    '{district}电梯{bedrooms}室，干净整洁',
    '{community}朝南{bedrooms}室，阳光充足',
    '{district}近学校{bedrooms}室，适合家庭',
]

# ---------- 小区名称模板 ----------
COMMUNITY_NAMES = [
    '花园小区', '阳光花园', '丽景苑', '翠湖花园', '明珠小区',
    '锦绣花园', '怡馨园', '幸福里', '金域蓝湾', '星河湾',
    '恒大华府', '碧桂园', '万科城', '保利花园', '中海锦城',
    '雅居乐', '富力城', '绿城花园', '融创中心', '华润广场',
]


# ========================== 工具函数 ==========================

def parse_cookie(raw):
    cookies = {}
    for part in raw.strip().split(';'):
        part = part.strip()
        if not part or '=' not in part:
            continue
        key, value = part.split('=', 1)
        cookies[key.strip()] = value.strip()
    return cookies


COOKIES = parse_cookie(RAW_COOKIE)


def create_session():
    session = requests.Session()
    retry_strategy = Retry(
        total=3, backoff_factor=1,
        status_forcelist=[500, 502, 503, 504],
        allowed_methods=frozenset(['GET', 'POST']),
    )
    adapter = HTTPAdapter(max_retries=retry_strategy)
    session.mount('https://', adapter)
    session.mount('http://', adapter)
    return session


def parse_layout(layout_text):
    bedrooms, bathrooms = 0, 0
    if not layout_text:
        return bedrooms, bathrooms
    m = re.search(r'(\d+)\s*室', layout_text)
    if m: bedrooms = int(m.group(1))
    m = re.search(r'(\d+)\s*卫', layout_text)
    if m: bathrooms = int(m.group(1))
    return bedrooms, bathrooms


def parse_price(price_text):
    """'300万' → 3000000, '58.5万' → 585000"""
    if not price_text: return None
    text = price_text.strip().replace(',', '')
    m = re.search(r'([\d.]+)\s*万', text)
    if m: return int(float(m.group(1)) * 10000)
    m = re.search(r'([\d.]+)', text)
    if m: return int(float(m.group(1)))
    return None


def parse_area(area_text):
    """'128㎡' → 128.0"""
    if not area_text: return None
    m = re.search(r'([\d.]+)', area_text)
    if m: return float(m.group(1))
    return None


def extract_city_and_region(community_name, address, page_url):
    city, region = '', ''
    m = re.search(r'https?://([^./]+)\.anjuke\.com', page_url or '')
    if m:
        city_py = m.group(1).lower()
        city = CITY_PY2CN.get(city_py, city_py)
    for text in [community_name, address]:
        if not text: continue
        m = re.search(r'([\u4e00-\u9fa5]{2,5}区)', text)
        if m:
            region = m.group(1)
            break
    return city, region


# ============================================================
# 图片素材库（兜底用）— 只有当详情页完全抓不到房源图时才使用
# ============================================================
FALLBACK_PROPERTY_IMAGES = [
    'https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=800&auto=format&fit=crop',
    'https://images.unsplash.com/photo-1512917774080-9991f1c4c750?w=800&auto=format&fit=crop',
    'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?w=800&auto=format&fit=crop',
    'https://images.unsplash.com/photo-1600566753190-17f0baa2a6c3?w=800&auto=format&fit=crop',
    'https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?w=800&auto=format&fit=crop',
    'https://images.unsplash.com/photo-1560185127-6ed189bf02f4?w=800&auto=format&fit=crop',
    'https://images.unsplash.com/photo-1586023492125-27b2c045efd7?w=800&auto=format&fit=crop',
    'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?w=800&auto=format&fit=crop',
    'https://images.unsplash.com/photo-1505691938895-1758d7feb511?w=800&auto=format&fit=crop',
    'https://images.unsplash.com/photo-1540518614846-7eded433c457?w=800&auto=format&fit=crop',
    'https://images.unsplash.com/photo-1570129477492-45c003edd2be?w=800&auto=format&fit=crop',
    'https://images.unsplash.com/photo-1502005229762-cf1b2da7c5d6?w=800&auto=format&fit=crop',
    'https://images.unsplash.com/photo-1505843513577-22bb7d21e455?w=800&auto=format&fit=crop',
    'https://images.unsplash.com/photo-1568605114967-8130f3a36994?w=800&auto=format&fit=crop',
]


# ============================================================
# 图片黑名单 —— 只过滤"明显不是房源实图"的URL关键词
# 注意：58cdn / ajkimg / anjuke 这些是安居客真实房源图的CDN域名，
#       不再整体拉黑，只过滤这些域名下的特定路径/关键词。
# ============================================================
BAD_IMAGE_KEYWORDS = [
    # --- 占位图 / 默认图 ---
    'default', 'nopic', 'no_pic', 'no-image', 'noimage',
    'placeholder', 'holder', 'blank', 'empty',
    # --- 平台Logo / 标签徽章 / 图标（URL带尺寸标记：_w128_h48 是标签）---
    'logo', 'badge', 'icon', 'tag', 'mark', 'watermark', 'stamp',
    'w40_h', '_w80_', '_w128_', '_w144_', '_w160_', 'w100_',
    '_h48', '_h80', '_h100', '_h120',
    # --- 缩略图 / 小图 ---
    'thumb', 'thumbnail', 'small.', 'small_', 'mini', 'tiny',
    'width=40', 'width=80', 'width=100',
    # --- 户型图 / 小区图 / 配套图 / 交通图 URL 关键词 ---
    'huxing', 'huxingtu', 'huxing_img', 'huxingmap',
    'community', 'xiaoqu', 'xiaoqumap', 'xiaoqu_pic',
    'map', 'jiaotong', 'traffic', 'peitao', 'support',
    'zhoubian', 'around', 'shanhushi',
]

# 户型图关键词（用于从特定容器中识别户型图）
FLOORPLAN_CONTAINER_KEYWORDS = [
    'huxing', 'huxingtu', '户型', 'floorplan', 'floor-plan', 'huxingpic',
    'xiaoqupic', 'community', '小区图',
]


def clean_image_url(src):
    """清洗图片URL：补全协议、去参数"""
    if not src: return ''
    src = src.strip()
    if src.startswith('//'):
        src = 'https:' + src
    # 去掉末尾的URL参数（对ajkimg.com上的图片可以去除裁剪参数）
    # 但 ajkimg.com/xxx.jpg!w800h600 这种带!的不要裁剪
    if '?' in src and src.endswith(('.jpg', '.jpeg', '.png', '.webp', '.gif')):
        src = src.split('?')[0]
    return src


def _is_good_image_url(src):
    """
    判断一张图片URL是否是"可用的房源图"。
    过滤掉：平台徽章/标签/占位图/缩略图/纯户型图URL
    但保留：58cdn / ajkimg / anjuke 下的真实房源图
    """
    if not src or not src.strip():
        return False
    s = src.strip().lower()

    # 必须是图片格式
    if not ('.jpg' in s or '.jpeg' in s or '.png' in s or '.webp' in s or '.gif' in s):
        return False

    # 黑名单过滤：URL中包含明显的"非房源图"关键词
    for kw in BAD_IMAGE_KEYWORDS:
        if kw in s:
            return False

    # 额外检测：URL中包含 明显尺寸标记且很小（如 w40_h40 / w80_h80）
    # 格式通常是：xxx.jpg!w800h600 或 xxx.jpg_w128_h48 或 xxx.jpg?w=40
    small_dim_patterns = [r'w[1-9]\d_h[1-9]\d', r'w[1-2]\d{2}_h[1-2]\d{2}']
    for p in small_dim_patterns:
        if re.search(p, s):
            dim_match = re.search(r'w(\d+)_h(\d+)', s)
            if dim_match:
                w, h = int(dim_match.group(1)), int(dim_match.group(2))
                if w <= 200 or h <= 200:
                    return False

    return True


def _extract_images_from_container(container):
    """
    从一个HTML容器节点中提取所有img的图片URL（优先懒加载属性）
    """
    urls = []
    for img in container.find_all('img'):
        src = (img.get('data-src') or img.get('data-original') or
               img.get('data-lazy-src') or img.get('data-img') or
               img.get('data-pic') or img.get('src') or '')
        if src:
            urls.append(clean_image_url(src))
    # 容器自己的data属性
    for attr in ['data-src', 'data-original', 'data-img', 'data-image', 'data-pic']:
        val = container.get(attr, '')
        if val and val.strip():
            urls.append(clean_image_url(val.strip()))
    return urls


def _class_matches_any(class_attr, keywords):
    """检查元素的class属性是否包含任一关键词（大小写不敏感）"""
    if not class_attr:
        return False
    if isinstance(class_attr, list):
        class_str = ' '.join(class_attr)
    else:
        class_str = str(class_attr)
    class_lower = class_str.lower()
    for kw in keywords:
        if kw.lower() in class_lower:
            return True
    return False


def parse_detail_page(session, detail_url):
    """
    从详情页解析：楼层 + 图片（分"实图"和"户型图"两组）
    返回: (floor, real_images, floorplan_images)
        - real_images: 房源实图，放前面做封面
        - floorplan_images: 户型图/小区图，放后面
    存储到数据库时 images = [实图...] + [户型图...]，第一张永远是实图。
    """
    floor = ''
    real_images, floorplan_images = [], []
    if not detail_url:
        return floor, real_images, floorplan_images

    try:
        resp = session.get(detail_url, headers=HEADERS, cookies=COOKIES, timeout=20)
        resp.raise_for_status()
    except Exception as e:
        print(f'    [详情页] 请求失败: {e}')
        return floor, real_images, floorplan_images

    html = resp.text
    soup = BeautifulSoup(html, 'html.parser')

    # --- 楼层 ---
    for cls_name in ['maininfo-model-weak', 'house-floor', 'floor', 'comm-floor', 'basic-item']:
        nodes = soup.find_all(class_=cls_name)
        for node in nodes:
            text = node.get_text(' ', strip=True)
            if '层' in text and len(text) <= 40 and '价格' not in text and '元' not in text:
                floor = text
                break
        if floor: break
    if not floor:
        for text_node in soup.find_all(string=True):
            text = str(text_node).strip()
            if re.search(r'\(共\d+层\)|[低中高]层|共\d+层', text) and len(text) <= 40:
                floor = text
                break

    # ============= 图片提取 =============
    # 策略：
    #   1. 先从"房源实图"容器抓 → 放到 real_images
    #   2. 再从"户型图/小区图"容器抓 → 放到 floorplan_images
    #   3. 所有URL走 _is_good_image_url() 过滤，去掉标签/小图/占位图
    #
    # 页面上的常见容器class：
    #   - 顶部轮播/大图区：carousel, swiper, gallery, banner, slide,
    #                       big-pic, big-image, pic-container, photo-box,
    #                       property-photo, house-photo
    #   - 房源相册/实图区：pic-list, image-list, photo-list, img-list,
    #                       pics, photos, images
    #   - 户型图/小区图：huxing, 户型, community, 小区, xiaoqu,
    #                   map, 交通, 配套, 周边

    seen = set()   # 去重

    # --- [容器1] 顶部轮播 / 大图 区 —— 这是房源实图 ---
    real_container_classes = [
        'carousel', 'swiper', 'gallery', 'banner', 'slide',
        'big-pic', 'big-image', 'pic-container', 'photo-box',
        'property-photo', 'house-photo', 'property-image',
        'pic-list', 'image-list', 'photo-list', 'img-list',
        'pics', 'photos', 'images', 'album', 'house-img',
    ]
    for cls in real_container_classes:
        for container in soup.find_all(class_=re.compile(cls, re.I)):
            # 跳过明显是户型图的容器（根据父/自身class或id判断）
            container_text = str(container.get('class', '')) + ' ' + str(container.get('id', ''))
            if _class_matches_any(container.get('class'), FLOORPLAN_CONTAINER_KEYWORDS):
                continue
            if 'huxing' in container_text.lower() or '户型' in container_text:
                continue
            # 提取图片
            for url in _extract_images_from_container(container):
                if url and url not in seen and _is_good_image_url(url):
                    seen.add(url)
                    real_images.append(url)
            if len(real_images) >= 8:
                break
        if len(real_images) >= 8:
            break

    # --- [容器2] 户型图 / 小区图 容器 —— 放到 floorplan_images ---
    for kw in FLOORPLAN_CONTAINER_KEYWORDS:
        for container in soup.find_all(class_=re.compile(kw, re.I)):
            for url in _extract_images_from_container(container):
                if url and url not in seen and _is_good_image_url(url):
                    seen.add(url)
                    floorplan_images.append(url)
        # 同时按包含文本过滤一些包含"户型图"、"小区图"的块
        for node in soup.find_all(['div', 'section'], class_=True):
            node_text = node.get_text(' ', strip=True)[:200]
            if ('户型' in node_text or '小区图' in node_text or '配套图' in node_text or
                '交通图' in node_text):
                for url in _extract_images_from_container(node):
                    if url and url not in seen and _is_good_image_url(url):
                        seen.add(url)
                        floorplan_images.append(url)

    # --- [容器3] 兜底：从页面所有img标签抓（如果前面没抓到足够的实图）---
    if len(real_images) < 4:
        for img in soup.find_all('img'):
            src = (img.get('data-src') or img.get('data-original') or
                   img.get('data-lazy-src') or img.get('data-img') or
                   img.get('data-pic') or img.get('src') or '')
            clean = clean_image_url(src)
            if clean and clean not in seen and _is_good_image_url(clean):
                # 检查它的父容器：如果父容器明显是户型图/小区图容器，放到户型图组
                parent = img.find_parent(class_=True)
                is_floorplan = False
                if parent:
                    if _class_matches_any(parent.get('class'), FLOORPLAN_CONTAINER_KEYWORDS):
                        is_floorplan = True
                    parent_text = parent.get_text(' ', strip=True)[:200]
                    if ('户型' in parent_text or '小区图' in parent_text or
                        '配套图' in parent_text or '交通图' in parent_text):
                        is_floorplan = True
                seen.add(clean)
                if is_floorplan:
                    floorplan_images.append(clean)
                else:
                    real_images.append(clean)
            if len(real_images) >= 8:
                break

    # --- [容器4] 从script标签里的JSON提取图片URL（现代安居客页面用JS渲染）---
    if len(real_images) < 4:
        # 匹配 https://xxx.ajkimg.com/xxx.jpg 或 https://xxx.58cdn.com.cn/xxx.jpg
        pic_pattern = re.compile(
            r'(https?://[^\s"\'<>{}]+\.(?:ajkimg|anjuke|58cdn|alicdn|aliyuncs)\.com[^\s"\'<>{}]*\.(?:jpg|jpeg|png|webp))',
            re.I
        )
        for m in pic_pattern.finditer(html):
            clean = clean_image_url(m.group(1))
            if clean and clean not in seen and _is_good_image_url(clean):
                seen.add(clean)
                real_images.append(clean)
            if len(real_images) >= 10:
                break

    print(f'    [图片] 实图 {len(real_images)} 张 + 户型图 {len(floorplan_images)} 张')
    return floor, real_images, floorplan_images


def build_images_for_property(real_images, floorplan_images=None, fallback_seed=0):
    """
    组装最终存储到数据库的图片列表。
    严格规则：
    1. 只存真实房源实图（real_images），户型图不存
    2. 必须至少有 1 张实图才能返回有效列表，否则返回空列表
    3. 最多取前 6 张实图
    """
    if not real_images:
        return []

    # 只取房源实图，最多6张
    final = list(real_images[:6])

    return final


def generate_rental_property_data(city_cn, landlord_ids, index):
    """
    生成模拟出租房源数据（当爬取真实出租房源失败时使用）
    """
    bedrooms = random.randint(1, 4)
    bathrooms = random.randint(1, min(2, bedrooms))
    area = random.randint(40, 150)
    rent_per_month = random.randint(1500, 8000)
    price = rent_per_month * 12  # 年租金
    district = random.choice(['禅城区', '南海区', '顺德区', '三水区', '高明区'])
    community = random.choice(COMMUNITY_NAMES)
    
    # 生成标题
    title_template = random.choice(RENT_TITLE_TEMPLATES)
    title = title_template.format(
        district=district,
        community=community,
        bedrooms=bedrooms,
        bathrooms=bathrooms
    )
    
    # 生成描述
    desc_template = random.choice(DESC_TEMPLATES)
    description = (f'{title}。'
        + desc_template.format(district=district or city_cn)
        + f'建筑面积约 {area:.2f}㎡，{bedrooms}室{bathrooms}卫，'
        + f'租金 {rent_per_month}元/月。')
    
    # 生成楼层
    floor_num = random.randint(3, 30)
    total_floor = random.randint(10, 40)
    floor = f'{floor_num}层/共{total_floor}层'
    
    # 朝向
    orientation = random.choice(['朝南', '南北通透', '朝东', '朝西', '朝北'])
    
    # 地址
    address = f'{community}，{district}街道'
    
    # 随机图片（从备用图片库选取）
    image_count = random.randint(3, 6)
    shuffled_images = FALLBACK_PROPERTY_IMAGES.copy()
    random.shuffle(shuffled_images)
    images_str = ','.join(shuffled_images[:image_count])
    
    return {
        'landlord_id': random.choice(landlord_ids),
        'title': title[:200],
        'description': description[:1500],
        'price': price,
        'price_type': 2,  # 月租
        'area': area,
        'bedrooms': bedrooms,
        'bathrooms': bathrooms,
        'orientation': orientation[:20],
        'floor': floor[:50],
        'address': address[:500],
        'city': city_cn[:100],
        'district': district[:100],
        'property_type': random.choice([0, 0, 0, 0, 1, 2, 3]),
        'transaction_type': 0,  # 出租
        'images': images_str[:2000],
        'status': 3,
        'view_count': random.randint(0, 500),
    }


# ========================== 数据库写入 ==========================

def get_db_connection():
    return pymysql.connect(
        host=DB_CONFIG['host'],
        port=DB_CONFIG['port'],
        user=DB_CONFIG['user'],
        password=DB_CONFIG['password'],
        database=DB_CONFIG['database'],
        charset=DB_CONFIG['charset'],
        cursorclass=pymysql.cursors.DictCursor,
    )


def verify_landlords(conn):
    """启动时检查数据库中真实存在的房东ID，避免外键错误"""
    with conn.cursor() as cursor:
        cursor.execute("SELECT id FROM user WHERE role = 1 AND status = 1 ORDER BY id")
        return [row['id'] for row in cursor.fetchall()]


INSERT_SQL = """
INSERT INTO property (
    landlord_id, title, description, price, price_type,
    area, bedrooms, bathrooms, orientation, floor, address,
    city, district, property_type, transaction_type, images,
    status, view_count, create_time, update_time
) VALUES (
    %s, %s, %s, %s, %s,
    %s, %s, %s, %s, %s, %s,
    %s, %s, %s, %s,
    %s, %s, %s, NOW(), NOW()
)
"""


def insert_property(conn, data):
    values = (
        data['landlord_id'], data['title'], data['description'],
        data['price'], data['price_type'], data['area'],
        data['bedrooms'], data['bathrooms'], data['orientation'],
        data['floor'], data['address'], data['city'], data['district'],
        data['property_type'], data['transaction_type'], data['images'],
        data['status'], data['view_count'],
    )
    try:
        with conn.cursor() as cursor:
            cursor.execute(INSERT_SQL, values)
        conn.commit()
        return True, cursor.lastrowid
    except Exception as e:
        conn.rollback()
        return False, str(e)


# ========================== 主程序 ==========================

def main():
    print('=' * 70)
    print(' 安居客房源爬虫 → 写入 smart_property_db.property 表')
    print('=' * 70)

    # 连接数据库
    try:
        conn = get_db_connection()
    except Exception as e:
        print(f'[错误] 无法连接数据库: {e}')
        print(f'       配置: {DB_CONFIG}')
        return

    valid_landlord_ids = verify_landlords(conn)
    if not valid_landlord_ids:
        print('[警告] 数据库中没有有效的房东用户，使用默认 ID=2')
        valid_landlord_ids = [2]
    print(f'[配置] 可用房东ID: {valid_landlord_ids}')
    print()

    # 抓取数量 & 城市数量 & 交易类型（支持命令行参数）
    # 用法：python anjuke_spider.py [房源总数] [随机城市数] [交易类型]
    #   交易类型: 0=出租, 1=出售, 2=全部(各一半)
    #   例：python anjuke_spider.py 20 5 0   → 爬20条出租房源，随机5个城市
    #   例：python anjuke_spider.py 20 5 1   → 爬20条出售房源，随机5个城市
    #   例：python anjuke_spider.py 20 5 2   → 爬20条房源(10条出租+10条出售)，随机5个城市
    #   例：python anjuke_spider.py 15 0     → 爬15条出售房源，只爬佛山（默认）
    #   例：python anjuke_spider.py          → 无参数，默认：20条出售房源，5个城市
    import sys
    args = sys.argv[1:]

    if len(args) >= 1:
        try:
            house_count = int(args[0])
        except ValueError:
            house_count = 20
    else:
        house_count = 20  # 默认：20条房源

    if len(args) >= 2:
        try:
            city_count = int(args[1])
        except ValueError:
            city_count = 5
    else:
        city_count = 5  # 默认：5个随机城市

    if len(args) >= 3:
        try:
            transaction_type_param = int(args[2])
        except ValueError:
            transaction_type_param = 1
    else:
        transaction_type_param = 1  # 默认：出售房源

    if house_count <= 0: return

    # ---------- 选择城市 ----------
    if city_count == 0:
        cities = [('foshan', '佛山')]
        print('[模式] 单城市（佛山）')
    else:
        cities = get_random_cities(n=city_count)
        print(f'[模式] 随机 {len(cities)} 个城市: ' + '、'.join(f'{cn}({py})' for py, cn in cities))

    # ---------- 确定交易类型 ----------
    if transaction_type_param == 0:
        transaction_types = [0]
        print('[模式] 只爬取出租房源')
    elif transaction_type_param == 1:
        transaction_types = [1]
        print('[模式] 只爬取出售房源')
    else:
        transaction_types = [0, 1]
        print('[模式] 爬取全部房源（出租+出售）')

    # ---------- 初始化 ----------
    session = create_session()
    per_city_target = (house_count + len(cities) - 1) // len(cities)
    page_count_per_city = (per_city_target // 30) + (1 if per_city_target % 30 else 0) + 1
    collected, inserted, failed = 0, 0, 0

    print(f'目标房源 {house_count} 条 (每个城市约 {per_city_target} 条，{page_count_per_city} 页)\n')

    try:
        # ---------- 外层：遍历每个城市和交易类型 ----------
        for city_idx, (city_py, city_cn) in enumerate(cities, 1):
            if collected >= house_count: break

            city_target = min(per_city_target, house_count - collected)
            city_collected = 0  # 当前城市已爬数量，用于均衡分配

            for trans_type in transaction_types:
                if collected >= house_count: break
                if city_collected >= city_target: break

                trans_label = '出租' if trans_type == 0 else '出售'
                print(f'\n===== [城市{city_idx}/{len(cities)}] {city_cn} ({city_py}) - {trans_label}，目标 {city_target} 条 =====')

                # ---------- 内层：遍历该城市的页面 ----------
                page_has_data = False
                for current_page in range(1, page_count_per_city + 1):
                    if collected >= house_count: break
                    if city_collected >= city_target: break
                    print(f'  [第{current_page}页]...')

                    list_url = build_list_url(city_py, current_page, trans_type)
                    try:
                        resp = session.get(list_url, headers=HEADERS, cookies=COOKIES, timeout=15)
                        resp.raise_for_status()
                    except Exception as e:
                        print(f'  [警告] 列表页请求失败: {e}，继续下一页')
                        continue

                    soup = BeautifulSoup(resp.text, 'html.parser')

                    # 找房源卡片：多种选择器兜底（适配出售和出租页面）
                    cards = []
                    
                    # 出售页面的选择器
                    if trans_type == 1:
                        cards = soup.find_all('div', class_='property')
                        if not cards:
                            cards = soup.find_all('li', class_='property')
                        if not cards:
                            cards = soup.find_all('a', href=re.compile(r'/sale/\d+\.html|/prop/view/'))
                    else:
                        # 出租页面的选择器 - 使用 zu-itemmod
                        cards = soup.find_all('div', class_='zu-itemmod')
                        if not cards:
                            cards = soup.find_all('div', class_='list-item')
                        if not cards:
                            cards = soup.find_all('li', class_='zu-item')
                        if not cards:
                            cards = soup.find_all('a', href=re.compile(r'/zu/\d+\.html|/prop/view/'))
                    
                    if not cards:
                        if current_page == 1 and trans_type == 0:
                            print('  [提示] 出租页面没解析到房源，将生成模拟数据')
                        else:
                            print('  [提示] 页面没解析到房源，可能被反爬，停止。')
                        break
                    print(f'  本页找到 {len(cards)} 张房源卡片')
                    page_has_data = True

                    for card in cards:
                        if collected >= house_count: break
                        if city_collected >= city_target: break

                        # 出租页面(zu-itemmod)和出售页面(property)结构不同，分开处理
                        if trans_type == 0:
                            # ===== 出租页面解析 (zu-itemmod) =====
                            # --- 1. 标题
                            title_node = card.find('h3')
                            title = title_node.get_text(' ', strip=True).strip() if title_node else ''
                            if not title:
                                title = '未命名房源'

                            # --- 2. 户型、面积、朝向
                            details_items = card.find_all(class_='details-item')
                            layout_text = area_text = orientation = floor = ''
                            community_name = ''
                            for item in details_items:
                                text = item.get_text(' ', strip=True)
                                if '室' in text and '厅' in text:
                                    layout_text = text
                                elif '平米' in text or '㎡' in text:
                                    area_text = text
                                elif re.search(r'[东南西北中上下]+', text):
                                    m = re.search(r'([东南西北中上下]+)', text)
                                    if m:
                                        orientation = m.group(1)
                                elif '层' in text:
                                    floor = text
                                else:
                                    community_name = text

                            bedrooms, bathrooms = parse_layout(layout_text)

                            # --- 3. 价格
                            price_node = card.find(class_='zu-price')
                            price_full = price_node.get_text(' ', strip=True).strip() if price_node else ''
                            
                            if '元/月' in price_full or price_full.isdigit():
                                m = re.search(r'([\d.]+)', price_full)
                                if m:
                                    price = int(float(m.group(1)) * 12)
                                else:
                                    price = random.randint(18000, 120000)
                            else:
                                price = random.randint(18000, 120000)

                            # --- 4. 地址
                            address = community_name
                            district = ''

                            # --- 5. 详情页链接
                            detail_url = card.get('link', '')
                            if not detail_url:
                                a_tag = card.find('a', class_='img')
                                if a_tag:
                                    detail_url = a_tag.get('href', '')

                            # --- 6. 图片（从列表页直接提取）
                            img_tag = card.find('img', class_='thumbnail')
                            real_images = []
                            if img_tag:
                                img_url = img_tag.get('lazy_src') or img_tag.get('src', '')
                                if img_url and 'pic1.ajkimg.com' in img_url:
                                    real_images.append(img_url)

                        else:
                            # ===== 出售页面解析 (property) =====
                            # --- 1. 标题
                            title = ''
                            title_selectors = ['property-content-title-name', 'house-title', 'title', 'prop-title', 'info-title', 'list-title']
                            for selector in title_selectors:
                                title_node = card.find(class_=selector)
                                if title_node:
                                    title = title_node.get_text(' ', strip=True).strip()
                                    break
                            if not title:
                                all_text = card.get_text(' ', strip=True).split('\n')
                                for t in all_text:
                                    t = t.strip()
                                    if 4 <= len(t) <= 50 and '㎡' not in t and '万' not in t and '元/月' not in t:
                                        title = t
                                        break
                            title = title.strip() or '未命名房源'

                            # --- 2. 户型、面积、朝向
                            info_p_list = card.find_all(class_='property-content-info-text')
                            if not info_p_list:
                                info_p_list = card.find_all(class_='house-info-item')
                            if not info_p_list:
                                info_p_list = card.find_all('p', class_=re.compile('info|detail|desc'))

                            info_texts = [p.get_text(' ', strip=True) for p in info_p_list]
                            if not info_texts:
                                card_text = card.get_text(' ', strip=True)
                                info_texts = [card_text]

                            layout_text = area_text = orientation = floor = ''
                            for t in info_texts:
                                if ('室' in t or '房' in t) and '室' in t:
                                    layout_text = t
                                elif '㎡' in t or '平米' in t:
                                    area_text = t
                                elif re.search(r'[东南西北中上下]+', t) and '㎡' not in t and '室' not in t:
                                    m = re.search(r'([东南西北中上下]+)', t)
                                    if m:
                                        orientation = m.group(1)
                            bedrooms, bathrooms = parse_layout(layout_text)

                            # --- 3. 价格
                            total_price_node = card.find(class_='property-price-total-num')
                            if not total_price_node:
                                total_price_node = card.find(class_='price')
                            if not total_price_node:
                                total_price_node = card.find(class_='house-price')

                            total_price_text = total_price_node.get_text(strip=True) if total_price_node else ''
                            total_unit_node = card.find(class_='property-price-total-text')
                            if not total_unit_node:
                                total_unit_node = card.find(class_='unit')
                            total_unit = total_unit_node.get_text(strip=True) if total_unit_node else ''
                            price_full = f'{total_price_text}{total_unit}'.strip()

                            if not price_full:
                                card_text = card.get_text(' ', strip=True)
                                m = re.search(r'(\d+(?:\.\d+)?)\s*(万|元/月|元/㎡|元)', card_text)
                                if m:
                                    price_full = m.group(0)

                            price = parse_price(price_full)

                            # --- 4. 小区名 & 地址
                            community_node = card.find(class_='property-content-info-comm-name')
                            if not community_node:
                                community_node = card.find(class_='community-name')
                            community_name = community_node.get_text(' ', strip=True) if community_node else ''

                            address_node = card.find(class_='property-content-info-comm-address')
                            if not address_node:
                                address_node = card.find(class_='address')
                            address = address_node.get_text(' ', strip=True) if address_node else ''

                            # --- 5. 详情页链接
                            detail_url = ''
                            outer_a = card.find_parent('a')
                            if outer_a and outer_a.get('href'):
                                detail_url = outer_a.get('href', '').strip()
                            else:
                                inner_a = card.find('a', href=True)
                                if inner_a:
                                    detail_url = inner_a.get('href', '').strip()

                            # --- 6. 图片（需要从详情页获取）
                            real_images = []

                        # --- 统一处理：城市/区域 ---
                        city_extracted, district = extract_city_and_region(community_name, address, list_url)

                        # --- 统一处理：从详情页获取更多图片（如果列表页没有足够图片）---
                        floor_from_detail = ''
                        floorplan_images = []
                        if trans_type == 0 and not real_images:
                            # 出租页面列表页可能有图片，先检查
                            pass
                        
                        if detail_url and (not real_images or len(real_images) < 2):
                            if 'anjuke.com/fangyuan/' in detail_url or 'anjuke.com/prop/view/' in detail_url or '/sale/' in detail_url:
                                print(f'  → 详情页: {title[:30]}...')
                                floor_from_detail, real_images, floorplan_images = parse_detail_page(session, detail_url)

                        # 使用详情页的楼层信息（如果列表页没有）
                        if floor_from_detail and not floor:
                            floor = floor_from_detail

                        # --- 7. 封面+详情图 ---
                        images_final = build_images_for_property(real_images, floorplan_images, fallback_seed=collected)

                        # 关键检查：没有实图的房源不入库
                        if not images_final:
                            print(f'    [SKIP] 无实图，跳过: {title[:28]}')
                            continue

                        images_str = ','.join(images_final)

                        collected += 1
                        city_collected += 1

                        # ========================== 组装数据库数据 ==========================
                        if trans_type == 0:
                            if price is None or price <= 0:
                                price = random.randint(18000, 120000)
                            price_type = 2
                        else:
                            if price is None or price <= 0:
                                price = random.randint(500000, 8000000)
                            price_type = 1

                        area_num = parse_area(area_text)
                        if area_num is None or area_num <= 0:
                            area_num = random.randint(40, 200)
                        if bedrooms <= 0: bedrooms = random.randint(1, 4)
                        if bathrooms <= 0: bathrooms = random.randint(1, 3)
                        if not orientation:
                            orientation = random.choice(['朝南', '南北通透', '朝东', '朝西', '朝北'])
                        if not floor:
                            floor = f'{random.randint(3, 30)}层/共{random.randint(10, 40)}层'
                        if not city_extracted: 
                            city_extracted = city_cn
                        if not district:
                            district = random.choice(['禅城区', '南海区', '顺德区', '三水区', '高明区'])

                        landlord_id = random.choice(valid_landlord_ids)
                        property_type = random.choice([0, 0, 0, 0, 1, 2, 3])
                        transaction_type = trans_type
                        status = 3
                        view_count = random.randint(0, 500)
                        desc_template = random.choice(DESC_TEMPLATES)

                        if trans_type == 0:
                            description = (f'{title}。'
                                + desc_template.format(district=district or city_extracted)
                                + f'建筑面积约 {area_num:.2f}㎡，{bedrooms}室{bathrooms}卫，'
                                + f'租金 {price / 12:.0f}元/月。')
                        else:
                            description = (f'{title}。'
                                + desc_template.format(district=district or city_extracted)
                                + f'建筑面积约 {area_num:.2f}㎡，{bedrooms}室{bathrooms}卫，'
                                + f'总价 {price / 10000:.1f}万元。')

                        row = {
                            'landlord_id': landlord_id, 'title': title[:200],
                            'description': description[:1500],
                            'price': price, 'price_type': price_type, 'area': area_num,
                            'bedrooms': bedrooms, 'bathrooms': bathrooms,
                            'orientation': orientation[:20], 'floor': floor[:50],
                            'address': address[:500],
                            'city': city_extracted[:100], 'district': district[:100],
                            'property_type': property_type,
                            'transaction_type': transaction_type,
                            'images': images_str[:2000] if images_str else '',
                            'status': status, 'view_count': view_count,
                        }

                        ok, info = insert_property(conn, row)
                        if ok:
                            inserted += 1
                            if trans_type == 0:
                                print(f'    [OK] 已入库 (id={info}): {title[:28]}  {price/12:.0f}元/月  ({len(real_images)}张实图)')
                            else:
                                print(f'    [OK] 已入库 (id={info}): {title[:28]}  {price/10000:.1f}万  ({len(real_images)}张实图)')
                        else:
                            failed += 1
                            print(f'    [FAIL] 写入失败: {info[:80]}')

                        time.sleep(REQUEST_DELAY)
                    time.sleep(1.0)

                # 如果是出租房源且没有爬取到数据，生成模拟数据
                if trans_type == 0 and not page_has_data and city_collected < city_target:
                    print(f'  [生成模拟数据] 为 {city_cn} 生成 {city_target - city_collected} 条出租房源...')
                    remaining = city_target - city_collected
                    for i in range(remaining):
                        if collected >= house_count:
                            break
                        rental_data = generate_rental_property_data(city_cn, valid_landlord_ids, collected)
                        ok, info = insert_property(conn, rental_data)
                        if ok:
                            inserted += 1
                            collected += 1
                            city_collected += 1
                            print(f'    [OK] 模拟数据入库 (id={info}): {rental_data["title"][:28]}  {rental_data["price"]/12:.0f}元/月')
                        else:
                            failed += 1
                            print(f'    [FAIL] 写入失败: {info[:80]}')

    except KeyboardInterrupt:
        print('\n[中断] 用户手动停止。已保存当前结果。')
    except Exception as e:
        print(f'\n[异常] 运行出错: {e}')
    finally:
        conn.close()

    print()
    print('=' * 70)
    print(f' 结束：爬虫采集 {collected} 条，成功写入 {inserted} 条，失败 {failed} 条')
    print('=' * 70)


if __name__ == '__main__':
    main()
