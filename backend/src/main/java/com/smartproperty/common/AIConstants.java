package com.smartproperty.common;

/**
 * AI功能常量定义
 * 定义AI问答、文案生成、智能推荐等功能使用的常量
 * 
 * @author 毕业设计项目
 */
public class AIConstants {

    // ==================== 问题类型常量 ====================
    
    /**
     * 问题类型：买房相关
     */
    public static final String QUESTION_TYPE_BUY = "buy";
    
    /**
     * 问题类型：租房相关
     */
    public static final String QUESTION_TYPE_RENT = "rent";
    
    /**
     * 问题类型：户型相关
     */
    public static final String QUESTION_TYPE_LAYOUT = "layout";
    
    /**
     * 问题类型：政策相关
     */
    public static final String QUESTION_TYPE_POLICY = "policy";
    
    /**
     * 问题类型：装修相关
     */
    public static final String QUESTION_TYPE_DECORATION = "decoration";
    
    /**
     * 问题类型：地段相关
     */
    public static final String QUESTION_TYPE_LOCATION = "location";
    
    /**
     * 问题类型：投资相关
     */
    public static final String QUESTION_TYPE_INVESTMENT = "investment";
    
    /**
     * 问题类型：贷款相关
     */
    public static final String QUESTION_TYPE_LOAN = "loan";
    
    /**
     * 问题类型：物业相关
     */
    public static final String QUESTION_TYPE_PROPERTY_MANAGEMENT = "property_management";
    
    /**
     * 问题类型：通用问题
     */
    public static final String QUESTION_TYPE_GENERAL = "general";

    // ==================== 文案模板类型常量 ====================
    
    /**
     * 文案版本：简短版
     */
    public static final String CONTENT_VERSION_SHORT = "short";
    
    /**
     * 文案版本：详细版
     */
    public static final String CONTENT_VERSION_DETAILED = "detailed";

    // ==================== 推荐来源常量 ====================
    
    /**
     * 推荐来源：AI智能推荐
     */
    public static final String RECOMMEND_SOURCE_AI = "recommendation";
    
    /**
     * 推荐来源：热门房源
     */
    public static final String RECOMMEND_SOURCE_HOT = "hot";
    
    /**
     * 推荐来源：相似房源
     */
    public static final String RECOMMEND_SOURCE_SIMILAR = "similar";
    
    /**
     * 推荐来源：最新房源
     */
    public static final String RECOMMEND_SOURCE_NEW = "new";

    // ==================== 用户行为类型常量 ====================
    
    /**
     * 用户行为：浏览房源
     */
    public static final String BEHAVIOR_TYPE_VIEW = "view";
    
    /**
     * 用户行为：收藏房源
     */
    public static final String BEHAVIOR_TYPE_FAVORITE = "favorite";
    
    /**
     * 用户行为：预约看房
     */
    public static final String BEHAVIOR_TYPE_APPOINTMENT = "appointment";
    
    /**
     * 用户行为：取消收藏
     */
    public static final String BEHAVIOR_TYPE_UNFAVORITE = "unfavorite";

    // ==================== 推荐算法参数常量 ====================
    
    /**
     * 推荐算法版本
     */
    public static final String ALGORITHM_VERSION = "v1.0";
    
    /**
     * 默认推荐数量
     */
    public static final int DEFAULT_RECOMMEND_LIMIT = 10;
    
    /**
     * 最大推荐数量
     */
    public static final int MAX_RECOMMEND_LIMIT = 50;
    
    /**
     * 推荐评分权重：城市匹配
     */
    public static final double WEIGHT_CITY = 0.20;
    
    /**
     * 推荐评分权重：区域匹配
     */
    public static final double WEIGHT_DISTRICT = 0.15;
    
    /**
     * 推荐评分权重：房源类型匹配
     */
    public static final double WEIGHT_PROPERTY_TYPE = 0.15;
    
    /**
     * 推荐评分权重：交易类型匹配
     */
    public static final double WEIGHT_TRANSACTION_TYPE = 0.10;
    
    /**
     * 推荐评分权重：价格匹配
     */
    public static final double WEIGHT_PRICE = 0.20;
    
    /**
     * 推荐评分权重：面积匹配
     */
    public static final double WEIGHT_AREA = 0.10;
    
    /**
     * 推荐评分权重：浏览次数
     */
    public static final double WEIGHT_VIEW_COUNT = 0.10;

    // ==================== AI响应时间模拟参数 ====================
    
    /**
     * AI问答最小响应时间（毫秒）
     */
    public static final int QA_MIN_DELAY_MS = 200;
    
    /**
     * AI问答最大响应时间（毫秒）
     */
    public static final int QA_MAX_DELAY_MS = 800;
    
    /**
     * 文案生成最小响应时间（毫秒）
     */
    public static final int CONTENT_MIN_DELAY_MS = 500;
    
    /**
     * 文案生成最大响应时间（毫秒）
     */
    public static final int CONTENT_MAX_DELAY_MS = 1500;

    // ==================== 房源类型常量 ====================
    
    /**
     * 房源类型：住宅
     */
    public static final int PROPERTY_TYPE_RESIDENCE = 1;
    
    /**
     * 房源类型：公寓
     */
    public static final int PROPERTY_TYPE_APARTMENT = 2;
    
    /**
     * 房源类型：别墅
     */
    public static final int PROPERTY_TYPE_VILLA = 3;
    
    /**
     * 房源类型：商铺
     */
    public static final int PROPERTY_TYPE_SHOP = 4;
    
    /**
     * 房源类型：写字楼
     */
    public static final int PROPERTY_TYPE_OFFICE = 5;

    // ==================== 交易类型常量 ====================
    
    /**
     * 交易类型：出售
     */
    public static final int TRANSACTION_TYPE_SALE = 1;
    
    /**
     * 交易类型：出租
     */
    public static final int TRANSACTION_TYPE_RENT = 2;

    // ==================== 私有构造函数 ====================
    
    /**
     * 私有构造函数，防止实例化
     */
    private AIConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
