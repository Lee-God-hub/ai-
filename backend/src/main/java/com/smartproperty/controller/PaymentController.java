package com.smartproperty.controller;

import com.smartproperty.entity.PaymentRecord;
import com.smartproperty.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 付款记录Controller
 */
@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * 创建付款记录
     */
    @PostMapping("/create")
    public Map<String, Object> createPayment(@RequestBody PaymentRecord payment) {
        try {
            PaymentRecord created = paymentService.createPayment(payment);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", created);
            result.put("message", "付款记录创建成功");
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }

    /**
     * 确认支付
     */
    @PostMapping("/confirm/{id}")
    public Map<String, Object> confirmPayment(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            String transactionNo = request.getOrDefault("transactionNo", "").toString();
            paymentService.confirmPayment(id, transactionNo);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "支付确认成功");
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }

    /**
     * 获取付款列表
     */
    @GetMapping("/list/{appointmentId}")
    public Map<String, Object> getPayments(@PathVariable Long appointmentId) {
        try {
            List<PaymentRecord> payments = paymentService.getPayments(appointmentId);
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

    /**
     * 我的付款记录
     */
    @GetMapping("/my-list")
    public Map<String, Object> getMyPayments(@RequestParam Long payerId) {
        try {
            List<PaymentRecord> payments = paymentService.getMyPayments(payerId);
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
}
