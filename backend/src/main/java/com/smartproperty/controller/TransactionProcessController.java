package com.smartproperty.controller;

import com.smartproperty.common.Result;
import com.smartproperty.entity.TransactionAttachment;
import com.smartproperty.entity.TransactionConversation;
import com.smartproperty.entity.TransactionPayment;
import com.smartproperty.entity.TransactionProcessLog;
import com.smartproperty.entity.TransactionStatistics;
import com.smartproperty.mapper.TransactionAttachmentMapper;
import com.smartproperty.mapper.TransactionConversationMapper;
import com.smartproperty.mapper.TransactionPaymentMapper;
import com.smartproperty.mapper.TransactionStatisticsMapper;
import com.smartproperty.service.TransactionProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 交易流程管理Controller
 */
@RestController
@RequestMapping("/transaction-process")
public class TransactionProcessController {

    @Autowired
    private TransactionProcessService processService;
    
    @Autowired
    private TransactionAttachmentMapper attachmentMapper;
    
    @Autowired
    private TransactionPaymentMapper paymentMapper;
    
    @Autowired
    private TransactionStatisticsMapper statisticsMapper;
    
    @Autowired
    private TransactionConversationMapper conversationMapper;

    /**
     * 获取流程详情
     */
    @GetMapping("/{appointmentId}")
    public Map<String, Object> getProcessDetail(@PathVariable Long appointmentId) {
        try {
            Map<String, Object> detail = processService.getProcessDetail(appointmentId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", detail);
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }

    /**
     * 推进到下一阶段
     */
    @PostMapping("/next-stage")
    public Map<String, Object> nextStage(@RequestBody Map<String, Object> request) {
        try {
            // 参数校验
            if (request.get("appointmentId") == null) {
                throw new IllegalArgumentException("预约ID不能为空");
            }
            if (request.get("operatorId") == null) {
                throw new IllegalArgumentException("操作人ID不能为空");
            }
            if (request.get("operatorName") == null) {
                throw new IllegalArgumentException("操作人姓名不能为空");
            }
            
            Long appointmentId = Long.parseLong(request.get("appointmentId").toString());
            Long operatorId = Long.parseLong(request.get("operatorId").toString());
            String operatorName = request.get("operatorName").toString();
            String remark = request.getOrDefault("remark", "").toString();
            
            processService.nextStage(appointmentId, operatorId, operatorName, remark);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "流程推进成功");
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }

    /**
     * 获取流程日志
     */
    @GetMapping("/logs/{appointmentId}")
    public Map<String, Object> getProcessLogs(@PathVariable Long appointmentId) {
        try {
            List<TransactionProcessLog> logs = processService.getProcessLogs(appointmentId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", logs);
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }

    /**
     * 取消交易
     */
    @PostMapping("/cancel/{appointmentId}")
    public Map<String, Object> cancelTransaction(
            @PathVariable Long appointmentId,
            @RequestBody Map<String, Object> request) {
        try {
            // 参数校验
            if (request.get("operatorId") == null) {
                throw new IllegalArgumentException("操作人ID不能为空");
            }
            if (request.get("operatorName") == null) {
                throw new IllegalArgumentException("操作人姓名不能为空");
            }
            if (request.get("reason") == null || request.get("reason").toString().trim().isEmpty()) {
                throw new IllegalArgumentException("取消原因不能为空");
            }
            
            Long operatorId = Long.parseLong(request.get("operatorId").toString());
            String operatorName = request.get("operatorName").toString();
            String reason = request.get("reason").toString();
            
            processService.cancelTransaction(appointmentId, operatorId, operatorName, reason);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "交易已取消");
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }

    /**
     * 初始化流程
     */
    @PostMapping("/init")
    public Map<String, Object> initProcess(@RequestBody Map<String, Object> request) {
        try {
            Long appointmentId = Long.parseLong(request.get("appointmentId").toString());
            Integer transactionType = Integer.parseInt(request.get("transactionType").toString());
            
            processService.initProcess(appointmentId, transactionType);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "流程初始化成功");
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }
    
    // ==================== 附件管理接口 ====================
    
    /**
     * 上传附件（接收Base64图片）
     */
    @PostMapping("/attachment/upload")
    public Map<String, Object> uploadAttachment(@RequestBody Map<String, Object> request) {
        try {
            // 参数校验
            if (request.get("appointmentId") == null) {
                throw new IllegalArgumentException("预约ID不能为空");
            }
            if (request.get("stageCode") == null) {
                throw new IllegalArgumentException("阶段代码不能为空");
            }
            if (request.get("fileName") == null) {
                throw new IllegalArgumentException("文件名不能为空");
            }
            if (request.get("fileData") == null) {
                throw new IllegalArgumentException("文件数据不能为空");
            }
            
            Long appointmentId = Long.parseLong(request.get("appointmentId").toString());
            String stageCode = request.get("stageCode").toString();
            String fileName = request.get("fileName").toString();
            Integer attachmentType = Integer.parseInt(request.getOrDefault("attachmentType", 3).toString());
            Long uploaderId = Long.parseLong(request.get("uploaderId").toString());
            String uploaderName = request.get("uploaderName").toString();
            String remark = request.getOrDefault("remark", "").toString();
            String fileData = request.get("fileData").toString();
            
            Long fileSize = Long.parseLong(request.getOrDefault("fileSize", 1024000).toString());
            String fileType = request.getOrDefault("fileType", "image/jpeg").toString();
            
            // 保存图片到磁盘（简化版：直接存储Base64）
            String filePath = "/uploads/attachments/" + appointmentId + "/" + UUID.randomUUID().toString() + "_" + fileName;
            
            // 实际项目中应该：
            // 1. 解码Base64
            // 2. 保存到磁盘或OSS
            // 3. 返回访问URL
            // 这里为了演示，直接存储路径
            
            // 保存附件记录
            TransactionAttachment attachment = new TransactionAttachment();
            attachment.setAppointmentId(appointmentId);
            attachment.setStageCode(stageCode);
            attachment.setFileName(fileName);
            attachment.setFilePath(filePath);
            attachment.setFileSize(fileSize);
            attachment.setFileType(fileType);
            attachment.setAttachmentType(attachmentType);
            attachment.setUploaderId(uploaderId);
            attachment.setUploaderName(uploaderName);
            attachment.setUploadTime(LocalDateTime.now());
            attachment.setRemark(remark);
            
            attachmentMapper.insert(attachment);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "附件上传成功");
            result.put("data", attachment);
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }
    
    /**
     * 获取附件列表
     */
    @GetMapping("/attachment/{appointmentId}")
    public Map<String, Object> getAttachments(@PathVariable Long appointmentId) {
        try {
            List<TransactionAttachment> attachments = attachmentMapper.selectByAppointmentId(appointmentId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", attachments);
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }
    
    /**
     * 删除附件
     */
    @DeleteMapping("/attachment/{attachmentId}")
    public Map<String, Object> deleteAttachment(@PathVariable Long attachmentId) {
        try {
            attachmentMapper.deleteById(attachmentId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "附件删除成功");
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }
    
    // ==================== 支付管理接口 ====================
    
    /**
     * 创建支付订单
     */
    @PostMapping("/payment/create")
    public Map<String, Object> createPayment(@RequestBody Map<String, Object> request) {
        try {
            // 参数校验
            if (request.get("appointmentId") == null) {
                throw new IllegalArgumentException("预约ID不能为空");
            }
            if (request.get("amount") == null) {
                throw new IllegalArgumentException("支付金额不能为空");
            }
            
            Long appointmentId = Long.parseLong(request.get("appointmentId").toString());
            String stageCode = request.get("stageCode").toString();
            Integer paymentType = Integer.parseInt(request.get("paymentType").toString());
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            Integer paymentMethod = Integer.parseInt(request.get("paymentMethod").toString());
            Long payerId = Long.parseLong(request.get("payerId").toString());
            String payerName = request.get("payerName").toString();
            String remark = request.getOrDefault("remark", "").toString();
            
            // 创建支付记录
            TransactionPayment payment = new TransactionPayment();
            payment.setAppointmentId(appointmentId);
            payment.setStageCode(stageCode);
            payment.setPaymentType(paymentType);
            payment.setAmount(amount);
            payment.setPaymentMethod(paymentMethod);
            payment.setPaymentStatus(0); // 待支付
            payment.setPayerId(payerId);
            payment.setPayerName(payerName);
            payment.setCreateTime(LocalDateTime.now());
            payment.setRemark(remark);
            
            paymentMapper.insert(payment);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "支付订单创建成功");
            result.put("data", payment);
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }
    
    /**
     * 模拟支付（直接标记为已支付）
     */
    @PostMapping("/payment/pay/{paymentId}")
    public Map<String, Object> payOrder(@PathVariable Long paymentId) {
        try {
            TransactionPayment payment = paymentMapper.selectById(paymentId);
            if (payment == null) {
                throw new RuntimeException("支付订单不存在");
            }
            
            // 模拟支付成功，生成交易流水号
            payment.setPaymentStatus(1); // 已支付
            payment.setTransactionNo("TX" + System.currentTimeMillis());
            payment.setPaymentTime(LocalDateTime.now());
            paymentMapper.updateById(payment);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "支付成功");
            result.put("data", payment);
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }
    
    /**
     * 获取支付记录列表
     */
    @GetMapping("/payment/{appointmentId}")
    public Map<String, Object> getPayments(@PathVariable Long appointmentId) {
        try {
            List<TransactionPayment> payments = paymentMapper.selectByAppointmentId(appointmentId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", payments);
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }
    
    // ==================== 数据统计接口 ====================
    
    /**
     * 获取统计数据
     */
    @GetMapping("/statistics")
    public Map<String, Object> getStatistics(
            @RequestParam(required = false, defaultValue = "7") Integer days) {
        try {
            List<TransactionStatistics> statistics = statisticsMapper.selectRecentDays(days);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", statistics);
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }
    
    // ==================== 对话管理接口 ====================
    
    /**
     * 获取对话列表
     */
    @GetMapping("/conversation/{appointmentId}")
    public Result<List<TransactionConversation>> getConversations(@PathVariable Long appointmentId) {
        try {
            List<TransactionConversation> conversations = conversationMapper.selectByAppointmentId(appointmentId);
            return Result.success(conversations);
        } catch (Exception e) {
            return Result.error("获取对话失败: " + e.getMessage());
        }
    }
    
    /**
     * 发送消息
     */
    @PostMapping("/conversation/send")
    public Result<TransactionConversation> sendMessage(@RequestBody Map<String, Object> request) {
        try {
            // 参数校验
            if (request.get("appointmentId") == null) {
                return Result.error("预约ID不能为空");
            }
            if (request.get("messageContent") == null || request.get("messageContent").toString().trim().isEmpty()) {
                return Result.error("消息内容不能为空");
            }
            
            Long appointmentId = Long.parseLong(request.get("appointmentId").toString());
            Long senderId = Long.parseLong(request.get("senderId").toString());
            String senderName = request.get("senderName").toString();
            Integer senderRole = Integer.parseInt(request.get("senderRole").toString());
            String messageContent = request.get("messageContent").toString();
            
            // 创建对话记录
            TransactionConversation conversation = new TransactionConversation();
            conversation.setAppointmentId(appointmentId);
            conversation.setSenderId(senderId);
            conversation.setSenderName(senderName);
            conversation.setSenderRole(senderRole);
            conversation.setMessageContent(messageContent);
            
            conversationMapper.insert(conversation);
            
            return Result.success("消息发送成功", conversation);
        } catch (Exception e) {
            return Result.error("发送消息失败: " + e.getMessage());
        }
    }
    
    /**
     * 标记所有消息为已读
     */
    @PostMapping("/conversation/mark-read/{appointmentId}")
    public Result<String> markAllAsRead(
            @PathVariable Long appointmentId,
            @RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            
            conversationMapper.markAllAsRead(appointmentId, userId);
            
            return Result.success("已标记为已读", "");
        } catch (Exception e) {
            return Result.error("标记失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取未读消息数量
     */
    @GetMapping("/conversation/unread-count/{appointmentId}")
    public Result<Integer> getUnreadCount(
            @PathVariable Long appointmentId,
            @RequestParam Long userId) {
        try {
            int count = conversationMapper.getUnreadCount(appointmentId, userId);
            return Result.success(count);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
}
