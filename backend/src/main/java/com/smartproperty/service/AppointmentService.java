package com.smartproperty.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartproperty.common.RoleConstants;
import com.smartproperty.dto.PageResult;
import com.smartproperty.entity.Appointment;
import com.smartproperty.entity.Property;
import com.smartproperty.mapper.AppointmentMapper;
import com.smartproperty.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppointmentService extends ServiceImpl<AppointmentMapper, Appointment> {

    @Autowired
    private PropertyService propertyService;

    public PageResult<Appointment> getUserAppointmentPage(Integer pageNum, Integer pageSize) {
        Long userId = UserContext.getUserId();
        Page<Appointment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(Appointment::getUserId, userId);
        wrapper.orderByDesc(Appointment::getCreateTime);
        
        Page<Appointment> result = this.page(page, wrapper);
        return new PageResult<>(result.getTotal(), result.getRecords());
    }

    public PageResult<Appointment> getLandlordAppointmentPage(Integer pageNum, Integer pageSize) {
        Long landlordId = UserContext.getUserId();
        Page<Appointment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(Appointment::getLandlordId, landlordId);
        wrapper.orderByDesc(Appointment::getCreateTime);
        
        Page<Appointment> result = this.page(page, wrapper);
        return new PageResult<>(result.getTotal(), result.getRecords());
    }

    public PageResult<Appointment> getAdminAppointmentPage(Integer pageNum, Integer pageSize) {
        Page<Appointment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.orderByDesc(Appointment::getCreateTime);
        
        Page<Appointment> result = this.page(page, wrapper);
        return new PageResult<>(result.getTotal(), result.getRecords());
    }

    public boolean createAppointment(Appointment appointment) {
        Long userId = UserContext.getUserId();

        Property property = propertyService.getById(appointment.getPropertyId());
        if (property == null) {
            throw new RuntimeException("房源不存在");
        }

        appointment.setUserId(userId);
        appointment.setLandlordId(property.getLandlordId());
        appointment.setStatus(0);
        // 补齐必填字段，避免数据库 NOT NULL 约束报错
        if (appointment.getAppointmentTime() == null) {
            // 默认预约时间为当前时间 + 1 天
            appointment.setAppointmentTime(java.time.LocalDateTime.now().plusDays(1));
        }
        if (appointment.getCreateTime() == null) {
            appointment.setCreateTime(java.time.LocalDateTime.now());
        }
        if (appointment.getUpdateTime() == null) {
            appointment.setUpdateTime(java.time.LocalDateTime.now());
        }
        if (appointment.getContactPhone() == null || appointment.getContactPhone().trim().isEmpty()) {
            // 联系电话：优先从用户信息取
            appointment.setContactPhone("未填写");
        }
        if (appointment.getRemark() == null) {
            appointment.setRemark("");
        }
        if (appointment.getTransactionType() == null) {
            appointment.setTransactionType(0);
        }
        if (appointment.getCurrentStage() == null) {
            appointment.setCurrentStage("预约登记");
        }
        return this.save(appointment);
    }

    public boolean updateAppointment(Appointment appointment) {
        Long userId = UserContext.getUserId();
        Integer role = UserContext.getRole();
        
        Appointment exist = this.getById(appointment.getId());
        if (exist == null) {
            throw new RuntimeException("预约不存在");
        }
        
        if (!RoleConstants.isAdmin(role) && !exist.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改");
        }
        
        return this.updateById(appointment);
    }

    public boolean confirmAppointment(Long id) {
        Long landlordId = UserContext.getUserId();
        
        Appointment appointment = this.getById(id);
        if (appointment == null) {
            throw new RuntimeException("预约不存在");
        }
        
        if (!appointment.getLandlordId().equals(landlordId)) {
            throw new RuntimeException("无权操作");
        }
        
        appointment.setStatus(1);
        return this.updateById(appointment);
    }

    public boolean completeAppointment(Long id) {
        Appointment appointment = this.getById(id);
        if (appointment == null) {
            throw new RuntimeException("预约不存在");
        }
        
        appointment.setStatus(2);
        return this.updateById(appointment);
    }

    public boolean cancelAppointment(Long id) {
        Long userId = UserContext.getUserId();
        Integer role = UserContext.getRole();
        
        Appointment appointment = this.getById(id);
        if (appointment == null) {
            throw new RuntimeException("预约不存在");
        }
        
        if (!RoleConstants.isAdmin(role) && !appointment.getUserId().equals(userId) && !appointment.getLandlordId().equals(userId)) {
            throw new RuntimeException("无权取消");
        }
        
        appointment.setStatus(3);
        return this.updateById(appointment);
    }

    public boolean deleteAppointment(Long id) {
        return this.removeById(id);
    }
}
