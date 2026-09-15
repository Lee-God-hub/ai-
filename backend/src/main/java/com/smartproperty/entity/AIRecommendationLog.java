package com.smartproperty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI推荐日志实体类
 * 用于记录系统为用户推荐的房源及用户的点击行为
 *
 * @author 毕业设计项目
 */
@TableName("ai_recommendation_log")
public class AIRecommendationLog {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID，关联user表
     */
    private Long userId;

    /**
     * 推荐的房源ID，关联property表
     */
    private Long propertyId;

    /**
     * 推荐分数（0-100）
     * 分数越高表示越匹配用户偏好
     */
    private BigDecimal recommendScore;

    /**
     * 推荐理由
     * 例如："基于您的浏览历史"、"相似房源推荐"、"热门房源"等
     */
    private String recommendReason;

    /**
     * 是否点击
     * 0: 未点击
     * 1: 已点击
     */
    private Integer isClicked;

    /**
     * 点击时间
     * 用户点击推荐房源时记录
     */
    private LocalDateTime clickTime;

    /**
     * 推荐时间（创建时间）
     */
    private LocalDateTime createTime;

    // ===== 构造方法 =====

    public AIRecommendationLog() {
    }

    public AIRecommendationLog(Long userId, Long propertyId, BigDecimal recommendScore, String recommendReason) {
        this.userId = userId;
        this.propertyId = propertyId;
        this.recommendScore = recommendScore;
        this.recommendReason = recommendReason;
        this.isClicked = 0;
    }

    // ===== Getter / Setter =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
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

    public Integer getIsClicked() {
        return isClicked;
    }

    public void setIsClicked(Integer isClicked) {
        this.isClicked = isClicked;
    }

    public LocalDateTime getClickTime() {
        return clickTime;
    }

    public void setClickTime(LocalDateTime clickTime) {
        this.clickTime = clickTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "AIRecommendationLog{" +
                "id=" + id +
                ", userId=" + userId +
                ", propertyId=" + propertyId +
                ", recommendScore=" + recommendScore +
                ", recommendReason='" + recommendReason + '\'' +
                ", isClicked=" + isClicked +
                ", clickTime=" + clickTime +
                ", createTime=" + createTime +
                '}';
    }
}
