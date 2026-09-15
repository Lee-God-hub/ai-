package com.smartproperty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartproperty.entity.Property;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 房源Mapper接口
 *
 * @author 毕业设计项目
 */
@Mapper
public interface PropertyMapper extends BaseMapper<Property> {

    @Update("UPDATE property SET status = #{status}, reject_reason = #{rejectReason}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status, @Param("rejectReason") String rejectReason);

    @Update("UPDATE property SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(Long id);

    @Select("SELECT * FROM property WHERE landlord_id = #{landlordId} ORDER BY create_time DESC")
    List<Property> selectByLandlordId(Long landlordId);

    @Select("SELECT * FROM property WHERE status IN (1, 3) ORDER BY create_time DESC")
    List<Property> selectPublished();

    @Select("SELECT * FROM property WHERE status = 0 ORDER BY create_time DESC")
    List<Property> selectPendingApproval();

    @Select("<script>" +
            "SELECT * FROM property WHERE status IN (1, 3) " +
            "<if test='city != null and city != \"\"'> AND city = #{city}</if>" +
            "<if test='district != null and district != \"\"'> AND district = #{district}</if>" +
            "<if test='propertyType != null'> AND property_type = #{propertyType}</if>" +
            "<if test='transactionType != null'> AND transaction_type = #{transactionType}</if>" +
            "<if test='minPrice != null'> AND price &gt;= #{minPrice}</if>" +
            "<if test='maxPrice != null'> AND price &lt;= #{maxPrice}</if>" +
            " ORDER BY create_time DESC " +
            "LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<Property> selectByPage(@Param("city") String city, @Param("district") String district,
                                 @Param("propertyType") Integer propertyType, @Param("transactionType") Integer transactionType,
                                 @Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice,
                                 @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("<script>" +
            "SELECT COUNT(*) FROM property WHERE status IN (1, 3) " +
            "<if test='city != null and city != \"\"'> AND city = #{city}</if>" +
            "<if test='district != null and district != \"\"'> AND district = #{district}</if>" +
            "<if test='propertyType != null'> AND property_type = #{propertyType}</if>" +
            "<if test='transactionType != null'> AND transaction_type = #{transactionType}</if>" +
            "<if test='minPrice != null'> AND price &gt;= #{minPrice}</if>" +
            "<if test='maxPrice != null'> AND price &lt;= #{maxPrice}</if>" +
            "</script>")
    int countByCondition(@Param("city") String city, @Param("district") String district,
                         @Param("propertyType") Integer propertyType, @Param("transactionType") Integer transactionType,
                         @Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);
}
