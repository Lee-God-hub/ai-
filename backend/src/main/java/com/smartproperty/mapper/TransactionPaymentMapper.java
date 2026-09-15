package com.smartproperty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartproperty.entity.TransactionPayment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 流程支付记录Mapper
 */
@Mapper
public interface TransactionPaymentMapper extends BaseMapper<TransactionPayment> {
    
    /**
     * 根据预约ID查询支付记录列表
     */
    @Select("SELECT * FROM transaction_payment WHERE appointment_id = #{appointmentId} ORDER BY create_time DESC")
    List<TransactionPayment> selectByAppointmentId(Long appointmentId);
    
    /**
     * 根据预约ID和阶段查询支付记录
     */
    @Select("SELECT * FROM transaction_payment WHERE appointment_id = #{appointmentId} AND stage_code = #{stageCode} ORDER BY create_time DESC")
    List<TransactionPayment> selectByAppointmentAndStage(Long appointmentId, String stageCode);
}
