package com.smartproperty.controller;

import com.smartproperty.common.Result;
import com.smartproperty.dto.PageResult;
import com.smartproperty.entity.Message;
import com.smartproperty.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/user/list")
    public Result<PageResult<Message>> getUserMessageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            PageResult<Message> result = messageService.getUserMessagePage(pageNum, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询留言列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/landlord/list")
    public Result<PageResult<Message>> getLandlordMessageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            PageResult<Message> result = messageService.getLandlordMessagePage(pageNum, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询留言列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/admin/list")
    public Result<PageResult<Message>> getAdminMessageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            PageResult<Message> result = messageService.getAdminMessagePage(pageNum, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询留言列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<Message> getMessageById(@PathVariable Long id) {
        try {
            Message message = messageService.getById(id);
            if (message == null) {
                return Result.notFound("留言不存在");
            }
            return Result.success(message);
        } catch (Exception e) {
            return Result.error("查询留言失败: " + e.getMessage());
        }
    }

    @PostMapping("/create")
    public Result<String> createMessage(@RequestBody Message message) {
        try {
            boolean success = messageService.createMessage(message);
            if (success) {
                return Result.success("发送成功", "");
            } else {
                return Result.error("发送失败");
            }
        } catch (Exception e) {
            return Result.error("发送失败: " + e.getMessage());
        }
    }

    @PostMapping("/reply/{id}")
    public Result<String> replyMessage(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String reply = body.get("reply");
            boolean success = messageService.replyMessage(id, reply);
            if (success) {
                return Result.success("回复成功", "");
            } else {
                return Result.error("回复失败");
            }
        } catch (Exception e) {
            return Result.error("回复失败: " + e.getMessage());
        }
    }

    @PostMapping("/read/{id}")
    public Result<String> markAsRead(@PathVariable Long id) {
        try {
            boolean success = messageService.markAsRead(id);
            if (success) {
                return Result.success("标记成功", "");
            } else {
                return Result.error("标记失败");
            }
        } catch (Exception e) {
            return Result.error("标记失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteMessage(@PathVariable Long id) {
        try {
            boolean success = messageService.deleteMessage(id);
            if (success) {
                return Result.success("删除成功", "");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    @GetMapping("/conversation/{propertyId}")
    public Result<List<Message>> getConversationMessages(@PathVariable Long propertyId) {
        try {
            List<Message> messages = messageService.getConversationMessages(propertyId);
            return Result.success(messages);
        } catch (Exception e) {
            return Result.error("查询对话失败: " + e.getMessage());
        }
    }

    @PostMapping("/conversation/send")
    public Result<Message> sendConversationMessage(@RequestBody Map<String, Object> body) {
        try {
            Long propertyId = Long.parseLong(body.get("propertyId").toString());
            String content = body.get("content").toString();
            
            if (content == null || content.trim().isEmpty()) {
                return Result.error("消息内容不能为空");
            }
            
            Message message = messageService.sendConversationMessage(propertyId, content);
            return Result.success(message);
        } catch (Exception e) {
            return Result.error("发送消息失败: " + e.getMessage());
        }
    }

    @GetMapping("/landlord/conversation/{propertyId}/{userId}")
    public Result<List<Message>> getLandlordConversationMessages(
            @PathVariable Long propertyId,
            @PathVariable Long userId) {
        try {
            List<Message> messages = messageService.getLandlordConversationMessages(propertyId, userId);
            return Result.success(messages);
        } catch (Exception e) {
            return Result.error("查询对话失败: " + e.getMessage());
        }
    }

    @PostMapping("/landlord/conversation/send")
    public Result<Message> landlordSendConversationMessage(@RequestBody Map<String, Object> body) {
        try {
            Long propertyId = Long.parseLong(body.get("propertyId").toString());
            Long userId = Long.parseLong(body.get("userId").toString());
            String content = body.get("content").toString();
            
            if (content == null || content.trim().isEmpty()) {
                return Result.error("消息内容不能为空");
            }
            
            Message message = messageService.landlordSendConversationMessage(propertyId, userId, content);
            return Result.success(message);
        } catch (Exception e) {
            return Result.error("发送消息失败: " + e.getMessage());
        }
    }
}
