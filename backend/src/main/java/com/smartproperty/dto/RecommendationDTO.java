package com.smartproperty.dto;

import com.smartproperty.entity.Property;

import java.math.BigDecimal;

/**
 * 推荐房源DTO
 * 用于返回推荐的房源信息及推荐理由
 *
 * @author 毕业设计项目
 */
public class RecommendationDTO {

    /**
     * 房源信息
     */
    private Property property;

    /**
     * 推荐分数（0-100）
     * 分数越高表示越匹配用户偏好
     */
    private BigDecimal recommendScore;

    /**
     * 推荐理由
     * 如："基于您的浏览历史"、"相似房源推荐"、"热门房源"等
     */
    private String recommendReason;

    /**
     * 匹配标签
     * 如：["价格匹配", "户型相似", "地段优越"]
     */
    private String[] matchTags;

    // ===== 构造方法 =====

    public RecommendationDTO() {
    }

    public RecommendationDTO(Property property, BigDecimal recommendScore, String recommendReason) {
        this.property = property;
        this.recommendScore = recommendScore;
        this.recommendReason = recommendReason;
    }

    // ===== Getter / Setter =====

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public BigDecimal getRecommendScore() {
        return recommendScore;
    }

    public void setRecommendScore(BigDecimal recommendScore) {
        this.recommendScore = recommendScore;
    }

    public String getRecommendReason() {
        return recommendReason;
    }

    public void setRecommendReason(String recommendReason) {
        this.recommendReason = recommendReason;
    }

    public String[] getMatchTags() {
        return matchTags;
    }

    public void setMatchTags(String[] matchTags) {
        this.matchTags = matchTags;
    }

    @Override
    public String toString() {
        return "RecommendationDTO{" +
                "property=" + property +
                ", recommendScore=" + recommendScore +
                ", recommendReason='" + recommendReason + '\'' +
                '}';
    }
}
