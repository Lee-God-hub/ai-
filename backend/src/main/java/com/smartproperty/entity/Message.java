package com.smartproperty.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import java.time.LocalDateTime;

public class Message {

    private Long id;
    private Long userId;
    private Long landlordId;
    private Long propertyId;
    private String content;
    private String reply;
    private Integer isRead;
    private String conversationId;  // 对话ID
    private Integer senderRole;  // 发送人角色：0-租客/买家，1-房东
    private LocalDateTime lastMessageTime;  // 最后消息时间
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /** 发送用户（关联查询用，非数据库字段） */
    @TableField(exist = false)
    private User user;

    /** 关联房源（关联查询用，非数据库字段） */
    @TableField(exist = false)
    private Property property;

    public Message() {
        this.isRead = 0;
        this.senderRole = 0;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.lastMessageTime = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getLandlordId() { return landlordId; }
    public void setLandlordId(Long landlordId) { this.landlordId = landlordId; }

    public Long getPropertyId() { return propertyId; }
    public void setPropertyId(Long propertyId) { this.propertyId = propertyId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }

    public Integer getIsRead() { return isRead; }
    public void setIsRead(Integer isRead) { this.isRead = isRead; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public Integer getSenderRole() { return senderRole; }
    public void setSenderRole(Integer senderRole) { this.senderRole = senderRole; }

    public LocalDateTime getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(LocalDateTime lastMessageTime) { this.lastMessageTime = lastMessageTime; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Property getProperty() { return property; }
    public void setProperty(Property property) { this.property = property; }
}
