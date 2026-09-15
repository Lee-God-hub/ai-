package com.smartproperty.controller;

import com.smartproperty.common.Result;
import com.smartproperty.common.RoleConstants;
import com.smartproperty.entity.Favorite;
import com.smartproperty.entity.Property;
import com.smartproperty.mapper.FavoriteMapper;
import com.smartproperty.mapper.PropertyMapper;
import com.smartproperty.service.FavoriteService;
import com.smartproperty.service.UserBehaviorService;
import com.smartproperty.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 收藏Controller
 *
 * @author 毕业设计项目
 */
@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private PropertyMapper propertyMapper;

    @Autowired
    private UserBehaviorService userBehaviorService;

    /**
     * 添加收藏
     * 同时记录用户收藏行为日志
     */
    @PostMapping("/add/{propertyId}")
    public Result<String> add(@PathVariable Long propertyId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        try {
            favoriteService.addFavorite(propertyId);

            // 异步记录用户收藏行为
            userBehaviorService.recordFavorite(propertyId);

            return Result.success("收藏成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消收藏
     * 同时记录用户取消收藏行为日志
     */
    @DeleteMapping("/remove/{propertyId}")
    public Result<String> remove(@PathVariable Long propertyId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        try {
            boolean success = favoriteService.removeFavorite(propertyId);

            if (success) {
                // 异步记录用户取消收藏行为
                userBehaviorService.recordUnfavorite(propertyId);
            }

            return success ? Result.success("取消收藏成功") : Result.error("操作失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 检查是否已收藏（未登录返回false）
     */
    @GetMapping("/check/{propertyId}")
    public Result<Boolean> check(@PathVariable Long propertyId) {
        boolean favorited = favoriteService.checkFavorited(propertyId);
        return Result.success(favorited);
    }

    /**
     * 获取我的收藏列表（带房源信息）
     */
    @GetMapping("/my-list")
    public Result<List<Favorite>> getMyList() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        List<Favorite> list = favoriteMapper.selectByUserId(userId);
        
        // 关联查询房源信息
        for (Favorite favorite : list) {
            Property property = propertyMapper.selectById(favorite.getPropertyId());
            favorite.setProperty(property);
        }
        
        return Result.success(list);
    }

    /**
     * 管理员获取所有收藏列表
     */
    @GetMapping("/admin/list")
    public Result<List<Favorite>> getAdminList() {
        if (!RoleConstants.isAdmin(UserContext.getRole())) {
            return Result.forbidden("只有管理员可以查看");
        }
        List<Favorite> list = favoriteMapper.selectList(null);
        
        // 关联查询房源信息
        for (Favorite favorite : list) {
            Property property = propertyMapper.selectById(favorite.getPropertyId());
            favorite.setProperty(property);
        }
        
        return Result.success(list);
    }

    /**
     * 管理员删除收藏记录
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        if (!RoleConstants.isAdmin(UserContext.getRole())) {
            return Result.forbidden("只有管理员可以删除");
        }
        int result = favoriteMapper.deleteById(id);
        return result > 0 ? Result.success("删除成功") : Result.error("删除失败");
    }
}
