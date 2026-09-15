package com.smartproperty.service;

import com.smartproperty.entity.PaymentRecord;
import java.util.List;

/**
 * 付款记录Service接口
 */
public interface PaymentService {

    /**
     * 创建付款记录
     */
    PaymentRecord createPayment(PaymentRecord payment);

    /**
     * 确认支付
     */
    void confirmPayment(Long id, String transactionNo);

    /**
     * 获取预约的付款列表
     */
    List<PaymentRecord> getPayments(Long appointmentId);

    /**
     * 获取用户的付款记录
     */
    List<PaymentRecord> getMyPayments(Long payerId);
}
