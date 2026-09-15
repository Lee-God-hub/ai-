package com.smartproperty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartproperty.entity.Contract;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * 合同管理Mapper接口
 */
@Mapper
public interface ContractMapper extends BaseMapper<Contract> {

    @Select("SELECT * FROM contract WHERE appointment_id = #{appointmentId}")
    Contract selectByAppointmentId(Long appointmentId);

    @Select("SELECT * FROM contract WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Contract> selectByUserId(Long userId);

    @Select("SELECT * FROM contract WHERE landlord_id = #{landlordId} ORDER BY create_time DESC")
    List<Contract> selectByLandlordId(Long landlordId);

    @Select("SELECT * FROM contract WHERE contract_no = #{contractNo}")
    Contract selectByContractNo(String contractNo);
}
