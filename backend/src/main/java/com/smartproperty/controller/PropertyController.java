package com.smartproperty.controller;

import com.smartproperty.common.Result;
import com.smartproperty.common.RoleConstants;
import com.smartproperty.dto.PageResult;
import com.smartproperty.entity.Property;
import com.smartproperty.service.PropertyService;
import com.smartproperty.service.UserBehaviorService;
import com.smartproperty.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 房源Controller
 *
 * @author 毕业设计项目
 */
@RestController
@RequestMapping("/property")
public class PropertyController {

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private UserBehaviorService userBehaviorService;

    /**
     * 房东发布房源
     */
    @PostMapping("/publish")
    public Result<Long> publish(@RequestBody Property property) {
        Long userId = UserContext.getUserId();
        if (!RoleConstants.isLandlord(UserContext.getRole())) {
            return Result.forbidden("只有房东可以发布房源");
        }
        boolean result = propertyService.publish(property, userId);
        return result ? Result.success("发布成功", property.getId()) : Result.error("发布失败");
    }

    /**
     * 房东更新房源
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody Property property) {
        Long userId = UserContext.getUserId();
        try {
            boolean result = propertyService.update(property, userId);
            return result ? Result.success("更新成功") : Result.error("更新失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员更新房源
     */
    @PutMapping("/admin-update")
    public Result<String> adminUpdate(@RequestBody Property property) {
        try {
            boolean result = propertyService.adminUpdate(property);
            return result ? Result.success("更新成功") : Result.error("更新失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 房东删除房源
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        try {
            boolean result = propertyService.delete(id, userId);
            return result ? Result.success("删除成功") : Result.error("删除失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 房东上架房源
     */
    @PostMapping("/online/{id}")
    public Result<String> online(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        try {
            boolean result = propertyService.online(id, userId);
            return result ? Result.success("上架成功") : Result.error("上架失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 房东下架房源
     */
    @PostMapping("/offline/{id}")
    public Result<String> offline(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        try {
            boolean result = propertyService.offline(id, userId);
            return result ? Result.success("下架成功") : Result.error("下架失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员审核通过
     */
    @PostMapping("/approve/{id}")
    public Result<String> approve(@PathVariable Long id) {
        if (!RoleConstants.isAdmin(UserContext.getRole())) {
            return Result.forbidden("只有管理员可以审核");
        }
        boolean result = propertyService.approve(id);
        return result ? Result.success("审核通过") : Result.error("操作失败");
    }

    /**
     * 管理员审核驳回
     */
    @PostMapping("/reject/{id}")
    public Result<String> reject(@PathVariable Long id, @RequestParam String reason) {
        if (!RoleConstants.isAdmin(UserContext.getRole())) {
            return Result.forbidden("只有管理员可以审核");
        }
        boolean result = propertyService.reject(id, reason);
        return result ? Result.success("已驳回") : Result.error("操作失败");
    }

    /**
     * 获取房源详情（浏览次数+1，包含房东信息）
     * 同时记录用户浏览行为日志
     */
    @GetMapping("/{id}")
    public Result<Property> getDetail(@PathVariable Long id) {
        Property property = propertyService.getDetailWithLandlord(id);
        if (property == null) {
            return Result.notFound("房源不存在");
        }
        
        // 异步记录用户浏览行为（仅登录用户）
        userBehaviorService.recordView(id);
        
        return Result.success(property);
    }

    /**
     * 获取房东的房源列表
     */
    @GetMapping("/my-list")
    public Result<List<Property>> getMyList() {
        Long userId = UserContext.getUserId();
        List<Property> list = propertyService.getMyProperties(userId);
        return Result.success(list);
    }

    /**
     * 获取待审核列表（管理员）
     */
    @GetMapping("/pending")
    public Result<List<Property>> getPendingList() {
        if (!RoleConstants.isAdmin(UserContext.getRole())) {
            return Result.forbidden("只有管理员可以查看");
        }
        List<Property> list = propertyService.getPendingApproval();
        return Result.success(list);
    }

    /**
     * 获取所有房源列表（管理员，分页）
     */
    @GetMapping("/admin-list")
    public Result<PageResult<Property>> getAdminList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        if (!RoleConstants.isAdmin(UserContext.getRole())) {
            return Result.forbidden("只有管理员可以查看");
        }
        PageResult<Property> result = propertyService.getAdminPropertyPage(pageNum, pageSize, keyword, status);
        return Result.success(result);
    }

    /**
     * 获取房源统计（管理员）
     */
    @GetMapping("/admin-statistics")
    public Result<Map<String, Long>> getAdminStatistics() {
        if (!RoleConstants.isAdmin(UserContext.getRole())) {
            return Result.forbidden("只有管理员可以查看");
        }
        Map<String, Long> stats = propertyService.getAdminStatistics();
        return Result.success(stats);
    }

    /**
     * 用户浏览房源列表（带筛选）
     */
    @GetMapping("/list")
    public Result<List<Property>> getList(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Integer propertyType,
            @RequestParam(required = false) Integer transactionType,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        List<Property> list = propertyService.getPublishedList(city, district, propertyType, transactionType, minPrice, maxPrice, pageNum, pageSize);
        return Result.success(list);
    }
}
