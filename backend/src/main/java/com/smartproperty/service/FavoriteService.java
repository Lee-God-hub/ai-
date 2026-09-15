package com.smartproperty.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartproperty.dto.PageResult;
import com.smartproperty.entity.Favorite;
import com.smartproperty.mapper.FavoriteMapper;
import com.smartproperty.util.UserContext;
import org.springframework.stereotype.Service;

@Service
public class FavoriteService extends ServiceImpl<FavoriteMapper, Favorite> {

    public PageResult<Favorite> getFavoritePage(Integer pageNum, Integer pageSize) {
        Long userId = UserContext.getUserId();
        Page<Favorite> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(Favorite::getUserId, userId);
        wrapper.orderByDesc(Favorite::getCreateTime);
        
        Page<Favorite> result = this.page(page, wrapper);
        return new PageResult<>(result.getTotal(), result.getRecords());
    }

    public PageResult<Favorite> getAdminFavoritePage(Integer pageNum, Integer pageSize) {
        Page<Favorite> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.orderByDesc(Favorite::getCreateTime);
        
        Page<Favorite> result = this.page(page, wrapper);
        return new PageResult<>(result.getTotal(), result.getRecords());
    }

    public boolean addFavorite(Long propertyId) {
        Long userId = UserContext.getUserId();
        
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId).eq(Favorite::getPropertyId, propertyId);
        if (this.count(wrapper) > 0) {
            throw new RuntimeException("已经收藏过了");
        }
        
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setPropertyId(propertyId);
        return this.save(favorite);
    }

    public boolean removeFavorite(Long propertyId) {
        Long userId = UserContext.getUserId();
        
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId).eq(Favorite::getPropertyId, propertyId);
        return this.remove(wrapper);
    }

    public boolean deleteFavorite(Long id) {
        return this.removeById(id);
    }

    public boolean checkFavorited(Long propertyId) {
        Long userId = UserContext.getUserId();
        if (userId == null) return false;
        
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId).eq(Favorite::getPropertyId, propertyId);
        return this.count(wrapper) > 0;
    }
}
