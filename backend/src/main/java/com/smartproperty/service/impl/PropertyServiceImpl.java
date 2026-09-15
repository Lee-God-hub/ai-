package com.smartproperty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartproperty.dto.PageResult;
import com.smartproperty.entity.Property;
import com.smartproperty.entity.User;
import com.smartproperty.mapper.PropertyMapper;
import com.smartproperty.mapper.UserMapper;
import com.smartproperty.service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 房源Service实现类
 *
 * @author 毕业设计项目
 */
@Service
public class PropertyServiceImpl extends ServiceImpl<PropertyMapper, Property> implements PropertyService {

    @Autowired
    private PropertyMapper propertyMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public boolean publish(Property property, Long landlordId) {
        property.setLandlordId(landlordId);
        property.setStatus(0); // 待审核
        property.setViewCount(0);
        property.setCreateTime(LocalDateTime.now());
        property.setUpdateTime(LocalDateTime.now());
        return save(property);
    }

    @Override
    @Transactional
    public boolean update(Property property, Long landlordId) {
        Property existing = getById(property.getId());
        if (existing == null || !existing.getLandlordId().equals(landlordId)) {
            throw new RuntimeException("无权限修改此房源");
        }
        property.setUpdateTime(LocalDateTime.now());
        return updateById(property);
    }

    @Override
    @Transactional
    public boolean adminUpdate(Property property) {
        Property existing = getById(property.getId());
        if (existing == null) {
            throw new RuntimeException("房源不存在");
        }
        property.setUpdateTime(LocalDateTime.now());
        return updateById(property);
    }

    @Override
    @Transactional
    public boolean delete(Long id, Long landlordId) {
        Property existing = getById(id);
        if (existing == null || !existing.getLandlordId().equals(landlordId)) {
            throw new RuntimeException("无权限删除此房源");
        }
        return removeById(id);
    }

    @Override
    @Transactional
    public boolean approve(Long id) {
        Property property = getById(id);
        if (property == null) {
            throw new RuntimeException("房源不存在");
        }
        property.setStatus(1); // 已审核
        property.setUpdateTime(LocalDateTime.now());
        return updateById(property);
    }

    @Override
    @Transactional
    public boolean reject(Long id, String reason) {
        Property property = getById(id);
        if (property == null) {
            throw new RuntimeException("房源不存在");
        }
        property.setStatus(2); // 已拒绝
        property.setUpdateTime(LocalDateTime.now());
        return updateById(property);
    }

    @Override
    @Transactional
    public boolean online(Long id, Long landlordId) {
        Property property = getById(id);
        if (property == null || !property.getLandlordId().equals(landlordId)) {
            throw new RuntimeException("无权限操作此房源");
        }
        if (property.getStatus() != 1) {
            throw new RuntimeException("只有已审核的房源才能上架");
        }
        property.setStatus(3); // 已上架
        property.setUpdateTime(LocalDateTime.now());
        return updateById(property);
    }

    @Override
    @Transactional
    public boolean offline(Long id, Long landlordId) {
        Property property = getById(id);
        if (property == null || !property.getLandlordId().equals(landlordId)) {
            throw new RuntimeException("无权限操作此房源");
        }
        property.setStatus(4); // 已下架
        property.setUpdateTime(LocalDateTime.now());
        return updateById(property);
    }

    @Override
    public Property getById(Long id) {
        return super.getById(id);
    }

    @Override
    @Transactional
    public Property getDetailWithLandlord(Long id) {
        Property property = super.getById(id);
        if (property == null) {
            return null;
        }
        
        // 关联查询房东信息
        User landlord = userMapper.selectById(property.getLandlordId());
        property.setLandlord(landlord);
        
        // 增加浏览次数
        property.setViewCount(property.getViewCount() + 1);
        updateById(property);
        
        return property;
    }

    @Override
    public List<Property> getMyProperties(Long landlordId) {
        LambdaQueryWrapper<Property> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Property::getLandlordId, landlordId)
                .orderByDesc(Property::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<Property> getPendingApproval() {
        LambdaQueryWrapper<Property> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Property::getStatus, 0)
                .orderByAsc(Property::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<Property> getAllProperties() {
        LambdaQueryWrapper<Property> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Property::getCreateTime);
        return list(wrapper);
    }

    @Override
    public PageResult<Property> getAdminPropertyPage(Integer pageNum, Integer pageSize, String keyword, Integer status) {
        LambdaQueryWrapper<Property> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            String k = keyword.trim();
            wrapper.and(w -> w.like(Property::getTitle, k).or().like(Property::getAddress, k));
        }
        if (status != null) {
            wrapper.eq(Property::getStatus, status);
        }
        wrapper.orderByDesc(Property::getCreateTime);
        Page<Property> page = new Page<>(pageNum, pageSize);
        Page<Property> result = page(page, wrapper);
        return new PageResult<>(result.getTotal(), result.getRecords());
    }

    @Override
    public Map<String, Long> getAdminStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", count(new LambdaQueryWrapper<>()));
        stats.put("online", count(new LambdaQueryWrapper<Property>().eq(Property::getStatus, 3)));
        stats.put("pending", count(new LambdaQueryWrapper<Property>().eq(Property::getStatus, 0)));
        stats.put("offline", count(new LambdaQueryWrapper<Property>().eq(Property::getStatus, 4)));
        return stats;
    }

    @Override
    public List<Property> getPublishedList(String city, String district, Integer propertyType,
                                            Integer transactionType, Double minPrice, Double maxPrice,
                                            int pageNum, int pageSize) {
        LambdaQueryWrapper<Property> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Property::getStatus, 3); // 已上架
        
        if (city != null && !city.isEmpty()) {
            wrapper.eq(Property::getCity, city);
        }
        if (district != null && !district.isEmpty()) {
            wrapper.eq(Property::getDistrict, district);
        }
        if (propertyType != null) {
            wrapper.eq(Property::getPropertyType, propertyType);
        }
        if (transactionType != null) {
            wrapper.eq(Property::getTransactionType, transactionType);
        }
        if (minPrice != null) {
            wrapper.ge(Property::getPrice, minPrice);
        }
        if (maxPrice != null) {
            wrapper.le(Property::getPrice, maxPrice);
        }
        
        wrapper.orderByDesc(Property::getCreateTime);
        
        int offset = (pageNum - 1) * pageSize;
        wrapper.last("LIMIT " + offset + ", " + pageSize);
        
        return list(wrapper);
    }

    @Override
    public int countPublished(String city, String district, Integer propertyType,
                               Integer transactionType, Double minPrice, Double maxPrice) {
        LambdaQueryWrapper<Property> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Property::getStatus, 3); // 已上架
        
        if (city != null && !city.isEmpty()) {
            wrapper.eq(Property::getCity, city);
        }
        if (district != null && !district.isEmpty()) {
            wrapper.eq(Property::getDistrict, district);
        }
        if (propertyType != null) {
            wrapper.eq(Property::getPropertyType, propertyType);
        }
        if (transactionType != null) {
            wrapper.eq(Property::getTransactionType, transactionType);
        }
        if (minPrice != null) {
            wrapper.ge(Property::getPrice, minPrice);
        }
        if (maxPrice != null) {
            wrapper.le(Property::getPrice, maxPrice);
        }
        
        return (int) count(wrapper);
    }

    @Override
    @Transactional
    public void incrementViewCount(Long id) {
        Property property = getById(id);
        if (property != null) {
            property.setViewCount(property.getViewCount() + 1);
            updateById(property);
        }
    }
}
