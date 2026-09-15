package com.smartproperty.service;

import com.smartproperty.dto.PageResult;
import com.smartproperty.entity.Property;
import java.util.List;
import java.util.Map;

/**
 * 房源Service接口
 */
public interface PropertyService {
    boolean publish(Property property, Long landlordId);
    boolean update(Property property, Long landlordId);
    boolean adminUpdate(Property property);
    boolean delete(Long id, Long landlordId);
    boolean approve(Long id);
    boolean reject(Long id, String reason);
    boolean online(Long id, Long landlordId);
    boolean offline(Long id, Long landlordId);
    Property getById(Long id);
    Property getDetailWithLandlord(Long id);
    List<Property> getMyProperties(Long landlordId);
    List<Property> getPendingApproval();
    List<Property> getPublishedList(String city, String district, Integer propertyType,
                                      Integer transactionType, Double minPrice, Double maxPrice,
                                      int pageNum, int pageSize);
    List<Property> getAllProperties();
    PageResult<Property> getAdminPropertyPage(Integer pageNum, Integer pageSize, String keyword, Integer status);
    Map<String, Long> getAdminStatistics();
    int countPublished(String city, String district, Integer propertyType,
                        Integer transactionType, Double minPrice, Double maxPrice);
    void incrementViewCount(Long id);
}
