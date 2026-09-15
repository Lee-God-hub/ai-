package com.smartproperty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartproperty.entity.TransactionProcessLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * 交易流程日志Mapper接口
 */
@Mapper
public interface TransactionProcessLogMapper extends BaseMapper<TransactionProcessLog> {

    @Select("SELECT * FROM transaction_process_log WHERE appointment_id = #{appointmentId} ORDER BY create_time ASC")
    List<TransactionProcessLog> selectByAppointmentId(Long appointmentId);

    @Select("SELECT * FROM transaction_process_log WHERE appointment_id = #{appointmentId} AND stage_code = #{stageCode}")
    TransactionProcessLog selectByAppointmentAndStage(Long appointmentId, String stageCode);
}
