package com.smartproperty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartproperty.entity.TransactionAttachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 流程附件Mapper
 */
@Mapper
public interface TransactionAttachmentMapper extends BaseMapper<TransactionAttachment> {
    
    /**
     * 根据预约ID查询附件列表
     */
    @Select("SELECT * FROM transaction_attachment WHERE appointment_id = #{appointmentId} ORDER BY upload_time DESC")
    List<TransactionAttachment> selectByAppointmentId(Long appointmentId);
    
    /**
     * 根据预约ID和阶段查询附件列表
     */
    @Select("SELECT * FROM transaction_attachment WHERE appointment_id = #{appointmentId} AND stage_code = #{stageCode} ORDER BY upload_time DESC")
    List<TransactionAttachment> selectByAppointmentAndStage(Long appointmentId, String stageCode);
}
