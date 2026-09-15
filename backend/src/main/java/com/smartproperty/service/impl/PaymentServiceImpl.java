package com.smartproperty.service.impl;

import com.smartproperty.entity.PaymentRecord;
import com.smartproperty.mapper.PaymentRecordMapper;
import com.smartproperty.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 付款记录Service实现类
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRecordMapper paymentRecordMapper;

    @Override
    @Transactional
    public PaymentRecord createPayment(PaymentRecord payment) {
        payment.setPaymentStatus(0); // 待支付
        payment.setCreateTime(LocalDateTime.now());
        paymentRecordMapper.insert(payment);
        return payment;
    }

    @Override
    @Transactional
    public void confirmPayment(Long id, String transactionNo) {
        PaymentRecord payment = paymentRecordMapper.selectById(id);
        if (payment == null) {
            throw new RuntimeException("付款记录不存在");
        }

        payment.setPaymentStatus(1); // 已支付
        payment.setPaymentTime(LocalDateTime.now());
        payment.setTransactionNo(transactionNo);
        paymentRecordMapper.updateById(payment);
    }

    @Override
    public List<PaymentRecord> getPayments(Long appointmentId) {
        return paymentRecordMapper.selectByAppointmentId(appointmentId);
    }

    @Override
    public List<PaymentRecord> getMyPayments(Long payerId) {
        return paymentRecordMapper.selectByPayerId(payerId);
    }
}
