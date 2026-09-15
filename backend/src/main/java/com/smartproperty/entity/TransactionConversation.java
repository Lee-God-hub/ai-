package com.smartproperty.entity;

import java.time.LocalDateTime;

/**
 * 交易流程对话实体类
 */
public class TransactionConversation {
    
    private Long id;
    private Long appointmentId;
    private Long senderId;
    private String senderName;
    private Integer senderRole; // 0-租客/买家, 1-房东, 2-管理员
    private String messageContent;
    private Integer messageType; // 1-文本, 2-图片, 3-文件
    private Integer isRead; // 0-未读, 1-已读
    private LocalDateTime createTime;

    public TransactionConversation() {
        this.messageType = 1;
        this.isRead = 0;
        this.createTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public Integer getSenderRole() {
        return senderRole;
    }

    public void setSenderRole(Integer senderRole) {
        this.senderRole = senderRole;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public Integer getMessageType() {
        return messageType;
    }

    public void setMessageType(Integer messageType) {
        this.messageType = messageType;
    }

    public Integer getIsRead() {
        return isRead;
    }

    public void setIsRead(Integer isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
