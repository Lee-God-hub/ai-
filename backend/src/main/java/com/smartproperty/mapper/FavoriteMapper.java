package com.smartproperty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartproperty.entity.Favorite;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 收藏Mapper接口
 *
 * @author 毕业设计项目
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    @Delete("DELETE FROM favorite WHERE user_id = #{userId} AND property_id = #{propertyId}")
    int deleteByUserAndProperty(@Param("userId") Long userId, @Param("propertyId") Long propertyId);

    @Select("SELECT * FROM favorite WHERE user_id = #{userId} AND property_id = #{propertyId}")
    Favorite selectByUserAndProperty(@Param("userId") Long userId, @Param("propertyId") Long propertyId);

    @Select("SELECT * FROM favorite WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Favorite> selectByUserId(Long userId);

    @Select("SELECT COUNT(*) FROM favorite WHERE user_id = #{userId}")
    int countByUserId(Long userId);

    @Select("SELECT COUNT(*) FROM favorite WHERE property_id = #{propertyId}")
    int countByPropertyId(Long propertyId);
}
