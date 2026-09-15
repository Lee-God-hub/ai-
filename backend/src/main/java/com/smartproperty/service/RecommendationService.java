package com.smartproperty.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.smartproperty.entity.Property;
import com.smartproperty.entity.UserBehaviorLog;
import com.smartproperty.mapper.PropertyMapper;
import com.smartproperty.mapper.UserBehaviorLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI推荐引擎服务类
 * 基于用户行为数据，使用协同过滤算法进行房源推荐
 * 
 * 推荐策略：
 * 1. 分析用户浏览和收藏历史，提取偏好特征
 * 2. 计算房源与用户偏好的相似度得分
 * 3. 过滤用户已收藏的房源
 * 4. 按得分降序返回推荐结果
 *
 * @author 毕业设计项目
 */
@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    @Autowired
    private PropertyMapper propertyMapper;

    @Autowired
    private UserBehaviorLogMapper behaviorLogMapper;

    /**
     * 为用户推荐房源
     *
     * @param userId 用户ID
     * @param limit  推荐数量限制
     * @return 推荐的房源列表（按推荐分数降序）
     */
    public List<PropertyRecommendation> recommend(Long userId, Integer limit) {
        log.info("开始为用户 {} 生成推荐，数量限制: {}", userId, limit);

        // 1. 获取用户行为历史
        List<UserBehaviorLog> viewHistory = new ArrayList<>();
        List<Long> favoritePropertyIds = new ArrayList<>();
        
        if (userId != null) {
            viewHistory = behaviorLogMapper.selectByUserIdAndType(userId, "view", 50);
            favoritePropertyIds = behaviorLogMapper.selectFavoritePropertyIds(userId);
            log.info("用户浏览历史: {} 条，收藏: {} 个", viewHistory.size(), favoritePropertyIds.size());
        } else {
            log.info("用户未登录，返回热门房源推荐");
        }

        // 2. 分析用户偏好
        UserPreference preference = analyzePreference(viewHistory, favoritePropertyIds);

        // 3. 获取候选房源（状态为已上架的房源）
        QueryWrapper<Property> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 3); // 3-已上架
        queryWrapper.orderByDesc("view_count"); // 按浏览量排序，提高热门房源权重
        queryWrapper.last("LIMIT 200"); // 扩大候选集，增加随机性
        List<Property> candidateProperties = propertyMapper.selectList(queryWrapper);

        log.info("候选房源数量: {}", candidateProperties.size());

        // 4. 随机打乱并抽样
        Collections.shuffle(candidateProperties, new Random());
        int sampleSize = Math.min(60, candidateProperties.size());
        candidateProperties = new ArrayList<>(candidateProperties.subList(0, sampleSize));

        // 5. 计算推荐分数
        List<PropertyRecommendation> recommendations = new ArrayList<>();
        for (Property property : candidateProperties) {
            // 过滤用户已收藏的房源
            if (favoritePropertyIds.contains(property.getId())) {
                continue;
            }

            // 计算相似度得分
            BigDecimal score = calculateSimilarityScore(property, preference);
            String reason = generateRecommendReason(property, preference);

            PropertyRecommendation recommendation = new PropertyRecommendation();
            recommendation.setProperty(property);
            recommendation.setScore(score);
            recommendation.setReason(reason);

            recommendations.add(recommendation);
        }

        // 6. 按分数降序排序（分数相同时随机排序）
        recommendations.sort((r1, r2) -> {
            int scoreCompare = r2.getScore().compareTo(r1.getScore());
            if (scoreCompare == 0) {
                return new Random().nextInt(3) - 1;
            }
            return scoreCompare;
        });

        // 7. 返回Top N
        List<PropertyRecommendation> result = recommendations.stream()
                .limit(limit)
                .collect(Collectors.toList());

        log.info("推荐生成完成，返回 {} 个房源", result.size());

        return result;
    }

    /**
     * 分析用户偏好
     * 从用户的浏览和收藏历史中提取偏好特征
     *
     * @param viewHistory         浏览历史
     * @param favoritePropertyIds 收藏的房源ID列表
     * @return 用户偏好对象
     */
    private UserPreference analyzePreference(List<UserBehaviorLog> viewHistory, List<Long> favoritePropertyIds) {
        UserPreference preference = new UserPreference();

        // 如果没有历史数据，返回默认偏好
        if (viewHistory.isEmpty() && favoritePropertyIds.isEmpty()) {
            log.info("用户无历史数据，返回默认偏好");
            return preference;
        }

        // 收集用户浏览和收藏过的房源
        Set<Long> allPropertyIds = new HashSet<>();
        viewHistory.forEach(log -> allPropertyIds.add(log.getPropertyId()));
        allPropertyIds.addAll(favoritePropertyIds);

        if (allPropertyIds.isEmpty()) {
            return preference;
        }

        // 查询这些房源的详细信息
        List<Property> userProperties = propertyMapper.selectBatchIds(allPropertyIds);

        // 统计各个特征的出现频率
        Map<String, Integer> cityCount = new HashMap<>();
        Map<String, Integer> districtCount = new HashMap<>();
        Map<Integer, Integer> propertyTypeCount = new HashMap<>();
        Map<Integer, Integer> transactionTypeCount = new HashMap<>();
        List<BigDecimal> prices = new ArrayList<>();
        List<BigDecimal> areas = new ArrayList<>();

        for (Property property : userProperties) {
            // 城市偏好
            if (property.getCity() != null) {
                cityCount.put(property.getCity(), cityCount.getOrDefault(property.getCity(), 0) + 1);
            }

            // 区域偏好
            if (property.getDistrict() != null) {
                districtCount.put(property.getDistrict(), districtCount.getOrDefault(property.getDistrict(), 0) + 1);
            }

            // 房源类型偏好
            if (property.getPropertyType() != null) {
                propertyTypeCount.put(property.getPropertyType(),
                        propertyTypeCount.getOrDefault(property.getPropertyType(), 0) + 1);
            }

            // 交易类型偏好
            if (property.getTransactionType() != null) {
                transactionTypeCount.put(property.getTransactionType(),
                        transactionTypeCount.getOrDefault(property.getTransactionType(), 0) + 1);
            }

            // 价格和面积
            if (property.getPrice() != null) {
                prices.add(property.getPrice());
            }
            if (property.getArea() != null) {
                areas.add(property.getArea());
            }
        }

        // 提取最常见的偏好
        preference.setPreferredCity(getMostFrequent(cityCount));
        preference.setPreferredDistrict(getMostFrequent(districtCount));
        preference.setPreferredPropertyType(getMostFrequent(propertyTypeCount));
        preference.setPreferredTransactionType(getMostFrequent(transactionTypeCount));

        // 计算价格和面积的平均值作为偏好
        if (!prices.isEmpty()) {
            BigDecimal avgPrice = prices.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(new BigDecimal(prices.size()), 2, RoundingMode.HALF_UP);
            preference.setPreferredPrice(avgPrice);
        }

        if (!areas.isEmpty()) {
            BigDecimal avgArea = areas.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(new BigDecimal(areas.size()), 2, RoundingMode.HALF_UP);
            preference.setPreferredArea(avgArea);
        }

        log.info("用户偏好分析完成: 城市={}, 区域={}, 类型={}, 交易类型={}, 价格={}, 面积={}",
                preference.getPreferredCity(), preference.getPreferredDistrict(),
                preference.getPreferredPropertyType(), preference.getPreferredTransactionType(),
                preference.getPreferredPrice(), preference.getPreferredArea());

        return preference;
    }

    /**
     * 计算房源与用户偏好的相似度得分
     * 得分范围：0-100
     *
     * @param property   房源
     * @param preference 用户偏好
     * @return 相似度得分
     */
    private BigDecimal calculateSimilarityScore(Property property, UserPreference preference) {
        BigDecimal score = BigDecimal.ZERO;

        // 城市匹配（权重：20分）
        if (preference.getPreferredCity() != null && preference.getPreferredCity().equals(property.getCity())) {
            score = score.add(new BigDecimal("20"));
        }

        // 区域匹配（权重：15分）
        if (preference.getPreferredDistrict() != null && preference.getPreferredDistrict().equals(property.getDistrict())) {
            score = score.add(new BigDecimal("15"));
        }

        // 房源类型匹配（权重：15分）
        if (preference.getPreferredPropertyType() != null &&
                preference.getPreferredPropertyType().equals(property.getPropertyType())) {
            score = score.add(new BigDecimal("15"));
        }

        // 交易类型匹配（权重：10分）
        if (preference.getPreferredTransactionType() != null &&
                preference.getPreferredTransactionType().equals(property.getTransactionType())) {
            score = score.add(new BigDecimal("10"));
        }

        // 价格相似度（权重：20分）
        if (preference.getPreferredPrice() != null && property.getPrice() != null) {
            BigDecimal priceDiff = preference.getPreferredPrice().subtract(property.getPrice()).abs();
            BigDecimal priceRatio = priceDiff.divide(preference.getPreferredPrice(), 4, RoundingMode.HALF_UP);

            // 价格差异越小，得分越高
            if (priceRatio.compareTo(new BigDecimal("0.2")) <= 0) {
                score = score.add(new BigDecimal("20"));
            } else if (priceRatio.compareTo(new BigDecimal("0.5")) <= 0) {
                score = score.add(new BigDecimal("10"));
            } else if (priceRatio.compareTo(new BigDecimal("1.0")) <= 0) {
                score = score.add(new BigDecimal("5"));
            }
        }

        // 面积相似度（权重：10分）
        if (preference.getPreferredArea() != null && property.getArea() != null) {
            BigDecimal areaDiff = preference.getPreferredArea().subtract(property.getArea()).abs();
            BigDecimal areaRatio = areaDiff.divide(preference.getPreferredArea(), 4, RoundingMode.HALF_UP);

            // 面积差异越小，得分越高
            if (areaRatio.compareTo(new BigDecimal("0.2")) <= 0) {
                score = score.add(new BigDecimal("10"));
            } else if (areaRatio.compareTo(new BigDecimal("0.5")) <= 0) {
                score = score.add(new BigDecimal("5"));
            }
        }

        // 浏览量加成（权重：10分）
        // 热门房源获得额外加分
        if (property.getViewCount() != null && property.getViewCount() > 0) {
            int viewBonus = Math.min(property.getViewCount() / 10, 10);
            score = score.add(new BigDecimal(viewBonus));
        }

        // 添加随机扰动，使相同得分的房源排序不同
        double randomFactor = new Random().nextDouble() * 10 - 5;
        score = score.add(new BigDecimal(String.valueOf(randomFactor)));

        return score;
    }

    /**
     * 生成推荐理由
     *
     * @param property   房源
     * @param preference 用户偏好
     * @return 推荐理由
     */
    private String generateRecommendReason(Property property, UserPreference preference) {
        List<String> reasons = new ArrayList<>();

        if (preference.getPreferredCity() != null && preference.getPreferredCity().equals(property.getCity())) {
            reasons.add("位于您偏好的城市");
        }

        if (preference.getPreferredDistrict() != null && preference.getPreferredDistrict().equals(property.getDistrict())) {
            reasons.add("位于您偏好的区域");
        }

        if (preference.getPreferredPropertyType() != null &&
                preference.getPreferredPropertyType().equals(property.getPropertyType())) {
            reasons.add("房源类型符合您的偏好");
        }

        if (preference.getPreferredPrice() != null && property.getPrice() != null) {
            BigDecimal priceDiff = preference.getPreferredPrice().subtract(property.getPrice()).abs();
            BigDecimal priceRatio = priceDiff.divide(preference.getPreferredPrice(), 4, RoundingMode.HALF_UP);
            if (priceRatio.compareTo(new BigDecimal("0.2")) <= 0) {
                reasons.add("价格符合您的预算");
            }
        }

        if (property.getViewCount() != null && property.getViewCount() > 50) {
            reasons.add("热门房源");
        }

        if (reasons.isEmpty()) {
            return "为您推荐的优质房源";
        }

        return String.join("，", reasons);
    }

    /**
     * 获取Map中出现频率最高的key
     *
     * @param countMap 统计Map
     * @param <T>      key类型
     * @return 出现频率最高的key
     */
    private <T> T getMostFrequent(Map<T, Integer> countMap) {
        if (countMap.isEmpty()) {
            return null;
        }

        return countMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * 用户偏好内部类
     * 存储从用户行为中提取的偏好特征
     */
    public static class UserPreference {
        private String preferredCity;
        private String preferredDistrict;
        private Integer preferredPropertyType;
        private Integer preferredTransactionType;
        private BigDecimal preferredPrice;
        private BigDecimal preferredArea;

        // Getters and Setters
        public String getPreferredCity() {
            return preferredCity;
        }

        public void setPreferredCity(String preferredCity) {
            this.preferredCity = preferredCity;
        }

        public String getPreferredDistrict() {
            return preferredDistrict;
        }

        public void setPreferredDistrict(String preferredDistrict) {
            this.preferredDistrict = preferredDistrict;
        }

        public Integer getPreferredPropertyType() {
            return preferredPropertyType;
        }

        public void setPreferredPropertyType(Integer preferredPropertyType) {
            this.preferredPropertyType = preferredPropertyType;
        }

        public Integer getPreferredTransactionType() {
            return preferredTransactionType;
        }

        public void setPreferredTransactionType(Integer preferredTransactionType) {
            this.preferredTransactionType = preferredTransactionType;
        }

        public BigDecimal getPreferredPrice() {
            return preferredPrice;
        }

        public void setPreferredPrice(BigDecimal preferredPrice) {
            this.preferredPrice = preferredPrice;
        }

        public BigDecimal getPreferredArea() {
            return preferredArea;
        }

        public void setPreferredArea(BigDecimal preferredArea) {
            this.preferredArea = preferredArea;
        }
    }

    /**
     * 房源推荐结果内部类
     * 包含房源信息、推荐分数和推荐理由
     */
    public static class PropertyRecommendation {
        private Property property;
        private BigDecimal score;
        private String reason;

        public Property getProperty() {
            return property;
        }

        public void setProperty(Property property) {
            this.property = property;
        }

        public BigDecimal getScore() {
            return score;
        }

        public void setScore(BigDecimal score) {
            this.score = score;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
