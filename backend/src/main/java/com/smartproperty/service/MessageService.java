package com.smartproperty.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartproperty.dto.PageResult;
import com.smartproperty.entity.Message;
import com.smartproperty.entity.Property;
import com.smartproperty.entity.User;
import com.smartproperty.mapper.MessageMapper;
import com.smartproperty.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService extends ServiceImpl<MessageMapper, Message> {

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private UserService userService;

    public PageResult<Message> getUserMessagePage(Integer pageNum, Integer pageSize) {
        Long userId = UserContext.getUserId();
        Page<Message> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(Message::getUserId, userId);
        wrapper.orderByDesc(Message::getCreateTime);

        Page<Message> result = this.page(page, wrapper);
        List<Message> records = result.getRecords().stream()
                .peek(this::enrichMessageInfo)
                .collect(Collectors.toList());
        return new PageResult<>(result.getTotal(), records);
    }

    public PageResult<Message> getLandlordMessagePage(Integer pageNum, Integer pageSize) {
        Long landlordId = UserContext.getUserId();
        Page<Message> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(Message::getLandlordId, landlordId);
        wrapper.orderByDesc(Message::getCreateTime);

        Page<Message> result = this.page(page, wrapper);
        List<Message> records = result.getRecords().stream()
                .peek(this::enrichMessageInfo)
                .collect(Collectors.toList());
        return new PageResult<>(result.getTotal(), records);
    }

    public PageResult<Message> getAdminMessagePage(Integer pageNum, Integer pageSize) {
        Page<Message> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();

        wrapper.orderByDesc(Message::getCreateTime);

        Page<Message> result = this.page(page, wrapper);
        List<Message> records = result.getRecords().stream()
                .peek(this::enrichMessageInfo)
                .collect(Collectors.toList());
        return new PageResult<>(result.getTotal(), records);
    }

    /**
     *  enrich message with user and property info
     */
    private void enrichMessageInfo(Message message) {
        if (message == null) {
            return;
        }
        if (message.getUserId() != null) {
            User user = userService.getById(message.getUserId());
            if (user != null) {
                user.setPassword(null);
                message.setUser(user);
            }
        }
        if (message.getPropertyId() != null) {
            Property property = propertyService.getById(message.getPropertyId());
            message.setProperty(property);
        }
    }

    public boolean createMessage(Message message) {
        Long userId = UserContext.getUserId();
        
        Property property = propertyService.getById(message.getPropertyId());
        if (property == null) {
            throw new RuntimeException("房源不存在");
        }
        
        message.setUserId(userId);
        message.setLandlordId(property.getLandlordId());
        message.setIsRead(0);
        return this.save(message);
    }

    public boolean replyMessage(Long id, String reply) {
        Long landlordId = UserContext.getUserId();
        
        Message message = this.getById(id);
        if (message == null) {
            throw new RuntimeException("留言不存在");
        }
        
        if (!message.getLandlordId().equals(landlordId)) {
            throw new RuntimeException("无权回复");
        }
        
        message.setReply(reply);
        message.setIsRead(1);
        return this.updateById(message);
    }

    public boolean markAsRead(Long id) {
        Message message = this.getById(id);
        if (message == null) {
            throw new RuntimeException("留言不存在");
        }
        
        message.setIsRead(1);
        return this.updateById(message);
    }

    public boolean deleteMessage(Long id) {
        return this.removeById(id);
    }

    /**
     * 房东在对话中发送消息
     */
    public Message landlordSendConversationMessage(Long propertyId, Long userId, String content) {
        Long landlordId = UserContext.getUserId();
        
        Property property = propertyService.getById(propertyId);
        if (property == null) {
            throw new RuntimeException("房源不存在");
        }
        
        if (!property.getLandlordId().equals(landlordId)) {
            throw new RuntimeException("无权操作此房源");
        }
        
        // 生成对话ID
        String conversationId = "prop_" + propertyId + "_user_" + userId;
        
        Message message = new Message();
        message.setPropertyId(propertyId);
        message.setUserId(userId);
        message.setLandlordId(landlordId);
        message.setContent(content);
        message.setConversationId(conversationId);
        message.setSenderRole(1);  // 1表示房东
        message.setIsRead(0);
        
        this.save(message);
        return message;
    }

    /**
     * 获取与某个房源的对话消息列表
     */
    public List<Message> getConversationMessages(Long propertyId) {
        Long userId = UserContext.getUserId();
        return getConversationMessagesByUser(propertyId, userId);
    }

    /**
     * 房东获取与某个用户的对话消息列表
     */
    public List<Message> getLandlordConversationMessages(Long propertyId, Long userId) {
        Long landlordId = UserContext.getUserId();

        Property property = propertyService.getById(propertyId);
        if (property == null) {
            throw new RuntimeException("房源不存在");
        }

        if (!property.getLandlordId().equals(landlordId)) {
            throw new RuntimeException("无权操作此房源");
        }

        return getConversationMessagesByUser(propertyId, userId);
    }

    private List<Message> getConversationMessagesByUser(Long propertyId, Long userId) {
        String conversationId = "prop_" + propertyId + "_user_" + userId;

        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getConversationId, conversationId);
        wrapper.orderByAsc(Message::getCreateTime);

        List<Message> messages = this.list(wrapper);
        messages.forEach(this::enrichMessageInfo);
        return messages;
    }

    /**
     * 发送对话消息
     */
    public Message sendConversationMessage(Long propertyId, String content) {
        Long userId = UserContext.getUserId();
        
        Property property = propertyService.getById(propertyId);
        if (property == null) {
            throw new RuntimeException("房源不存在");
        }
        
        // 生成对话ID
        String conversationId = "prop_" + propertyId + "_user_" + userId;
        
        Message message = new Message();
        message.setPropertyId(propertyId);
        message.setUserId(userId);
        message.setLandlordId(property.getLandlordId());
        message.setContent(content);
        message.setConversationId(conversationId);
        message.setSenderRole(0);  // 0表示租客/买家
        message.setIsRead(0);
        
        this.save(message);
        return message;
    }
}
