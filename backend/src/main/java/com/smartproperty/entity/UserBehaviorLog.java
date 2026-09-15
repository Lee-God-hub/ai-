package com.smartproperty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 用户行为日志实体类
 * 用于记录用户的浏览、收藏、预约等行为，为推荐算法提供数据支持
 *
 * @author 毕业设计项目
 */
@TableName("user_behavior_log")
public class UserBehaviorLog {

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
     * 房源ID，关联property表
     */
    private Long propertyId;

    /**
     * 行为类型
     * view: 浏览
     * favorite: 收藏
     * appointment: 预约
     * unfavorite: 取消收藏
     */
    private String behaviorType;

    /**
     * 行为详情（JSON格式）
     * 可以存储额外信息，如浏览时长、预约时间等
     */
    private String behaviorDetail;

    /**
     * 行为发生时间（创建时间）
     */
    private LocalDateTime createTime;

    // ===== 构造方法 =====

    public UserBehaviorLog() {
    }

    public UserBehaviorLog(Long userId, Long propertyId, String behaviorType) {
        this.userId = userId;
        this.propertyId = propertyId;
        this.behaviorType = behaviorType;
    }

    public UserBehaviorLog(Long userId, Long propertyId, String behaviorType, String behaviorDetail) {
        this.userId = userId;
        this.propertyId = propertyId;
        this.behaviorType = behaviorType;
        this.behaviorDetail = behaviorDetail;
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

    public String getBehaviorType() {
        return behaviorType;
    }

    public void setBehaviorType(String behaviorType) {
        this.behaviorType = behaviorType;
    }

    public String getBehaviorDetail() {
        return behaviorDetail;
    }

    public void setBehaviorDetail(String behaviorDetail) {
        this.behaviorDetail = behaviorDetail;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "UserBehaviorLog{" +
                "id=" + id +
                ", userId=" + userId +
                ", propertyId=" + propertyId +
                ", behaviorType='" + behaviorType + '\'' +
                ", behaviorDetail='" + behaviorDetail + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
