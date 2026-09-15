package com.smartproperty.controller;

import com.smartproperty.common.Result;
import com.smartproperty.common.RoleConstants;
import com.smartproperty.entity.Appointment;
import com.smartproperty.entity.Property;
import com.smartproperty.entity.User;
import com.smartproperty.mapper.AppointmentMapper;
import com.smartproperty.mapper.PropertyMapper;
import com.smartproperty.mapper.UserMapper;
import com.smartproperty.service.AppointmentService;
import com.smartproperty.service.UserBehaviorService;
import com.smartproperty.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预约Controller
 *
 * @author 毕业设计项目
 */
@RestController
@RequestMapping("/appointment")
public class AppointmentController {

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private PropertyMapper propertyMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserBehaviorService userBehaviorService;

    /**
     * 用户预约看房
     * 同时记录用户预约行为日志
     */
    @PostMapping("/create")
    public Result<Long> create(@RequestBody Appointment appointment) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        try {
            appointmentService.createAppointment(appointment);

            // 异步记录用户预约行为
            userBehaviorService.recordAppointment(appointment.getPropertyId());

            return Result.success("预约成功", appointment.getId());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户取消预约
     */
    @PostMapping("/cancel/{id}")
    public Result<String> cancel(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        try {
            boolean success = appointmentService.cancelAppointment(id);
            return success ? Result.success("已取消") : Result.error("操作失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 房东确认预约
     */
    @PostMapping("/confirm/{id}")
    public Result<String> confirm(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        try {
            boolean success = appointmentService.confirmAppointment(id);
            return success ? Result.success("已确认") : Result.error("操作失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 完成预约
     */
    @PostMapping("/complete/{id}")
    public Result<String> complete(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        try {
            boolean success = appointmentService.completeAppointment(id);
            return success ? Result.success("已完成") : Result.error("操作失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户获取我的预约列表（带房源信息）
     */
    @GetMapping("/my-list")
    public Result<List<Appointment>> getMyList() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        List<Appointment> list = appointmentMapper.selectByUserId(userId);
        
        // 关联查询房源信息
        for (Appointment appointment : list) {
            Property property = propertyMapper.selectById(appointment.getPropertyId());
            appointment.setProperty(property);
        }
        
        return Result.success(list);
    }

    /**
     * 房东获取预约列表（带房源和用户信息）
     */
    @GetMapping("/landlord-list")
    public Result<List<Appointment>> getLandlordList() {
        Long userId = UserContext.getUserId();
        if (!RoleConstants.isLandlord(UserContext.getRole())) {
            return Result.forbidden("只有房东可以查看");
        }
        List<Appointment> list = appointmentMapper.selectByLandlordId(userId);
        
        // 关联查询房源信息和用户信息
        for (Appointment appointment : list) {
            Property property = propertyMapper.selectById(appointment.getPropertyId());
            appointment.setProperty(property);
            
            User user = userMapper.selectById(appointment.getUserId());
            appointment.setUser(user);
        }
        
        return Result.success(list);
    }

    /**
     * 管理员获取所有预约列表
     */
    @GetMapping("/admin/list")
    public Result<List<Appointment>> getAdminList() {
        if (!RoleConstants.isAdmin(UserContext.getRole())) {
            return Result.forbidden("只有管理员可以查看");
        }
        List<Appointment> list = appointmentMapper.selectList(null);
        
        // 关联查询房源信息和用户信息
        for (Appointment appointment : list) {
            Property property = propertyMapper.selectById(appointment.getPropertyId());
            appointment.setProperty(property);
            
            User user = userMapper.selectById(appointment.getUserId());
            appointment.setUser(user);
        }
        
        return Result.success(list);
    }

    /**
     * 管理员删除预约
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        if (!RoleConstants.isAdmin(UserContext.getRole())) {
            return Result.forbidden("只有管理员可以删除");
        }
        int result = appointmentMapper.deleteById(id);
        return result > 0 ? Result.success("删除成功") : Result.error("删除失败");
    }
}
