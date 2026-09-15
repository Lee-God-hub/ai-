package com.smartproperty.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import java.time.LocalDateTime;

/**
 * 预约看房实体类
 *
 * @author 毕业设计项目
 */
public class Appointment {

    /** 预约ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 房源ID */
    private Long propertyId;

    /** 房东ID */
    private Long landlordId;

    /** 预约看房时间 */
    private LocalDateTime appointmentTime;

    /** 联系电话 */
    private String contactPhone;

    /** 备注信息 */
    private String remark;

    /** 状态：0-待确认，1-已确认，2-已完成，3-已取消 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 交易类型：0-租房，1-买房 */
    private Integer transactionType;

    /** 当前阶段 */
    private String currentStage;

    /** 阶段状态：0-进行中，1-已完成，2-已拒绝 */
    private Integer stageStatus;

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

    public Long getLandlordId() { return landlordId; }
    public void setLandlordId(Long landlordId) { this.landlordId = landlordId; }

    public LocalDateTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalDateTime appointmentTime) { this.appointmentTime = appointmentTime; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Property getProperty() { return property; }
    public void setProperty(Property property) { this.property = property; }

    public Integer getTransactionType() { return transactionType; }
    public void setTransactionType(Integer transactionType) { this.transactionType = transactionType; }

    public String getCurrentStage() { return currentStage; }
    public void setCurrentStage(String currentStage) { this.currentStage = currentStage; }

    public Integer getStageStatus() { return stageStatus; }
    public void setStageStatus(Integer stageStatus) { this.stageStatus = stageStatus; }
}
