package com.smartproperty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartproperty.entity.PaymentRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * 付款记录Mapper接口
 */
@Mapper
public interface PaymentRecordMapper extends BaseMapper<PaymentRecord> {

    @Select("SELECT * FROM payment_record WHERE appointment_id = #{appointmentId} ORDER BY create_time DESC")
    List<PaymentRecord> selectByAppointmentId(Long appointmentId);

    @Select("SELECT * FROM payment_record WHERE contract_id = #{contractId} ORDER BY create_time DESC")
    List<PaymentRecord> selectByContractId(Long contractId);

    @Select("SELECT * FROM payment_record WHERE payer_id = #{payerId} ORDER BY create_time DESC")
    List<PaymentRecord> selectByPayerId(Long payerId);
}
