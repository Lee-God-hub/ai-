package com.smartproperty.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import java.time.LocalDateTime;

/**
 * 收藏实体类
 *
 * @author 毕业设计项目
 */
public class Favorite {

    /** 收藏ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 房源ID */
    private Long propertyId;

    /** 收藏时间 */
    private LocalDateTime createTime;

    /** 用户信息（关联查询用，非数据库字段） */
    @TableField(exist = false)
    private User user;

    /** 房源信息（关联查询用，非数据库字段） */
    @TableField(exist = false)
    private Property property;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getPropertyId() { return propertyId; }
    public void setPropertyId(Long propertyId) { this.propertyId = propertyId; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Property getProperty() { return property; }
    public void setProperty(Property property) { this.property = property; }
}
