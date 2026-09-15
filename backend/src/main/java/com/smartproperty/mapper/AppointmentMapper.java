package com.smartproperty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartproperty.entity.Appointment;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 预约看房Mapper接口
 *
 * @author 毕业设计项目
 */
@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {

    @Update("UPDATE appointment SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Select("SELECT * FROM appointment WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Appointment> selectByUserId(Long userId);

    @Select("SELECT * FROM appointment WHERE landlord_id = #{landlordId} ORDER BY create_time DESC")
    List<Appointment> selectByLandlordId(Long landlordId);

    @Select("SELECT * FROM appointment WHERE property_id = #{propertyId} ORDER BY create_time DESC")
    List<Appointment> selectByPropertyId(Long propertyId);

    @Select("SELECT COUNT(*) FROM appointment WHERE user_id = #{userId}")
    int countByUserId(Long userId);

    @Select("SELECT COUNT(*) FROM appointment WHERE landlord_id = #{landlordId}")
    int countByLandlordId(Long landlordId);
}
