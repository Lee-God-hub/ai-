package com.smartproperty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartproperty.entity.AIContentGeneration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * AI文案生成记录Mapper接口
 * 提供AI文案生成记录的数据库操作
 *
 * @author 毕业设计项目
 */
@Mapper
public interface AIContentGenerationMapper extends BaseMapper<AIContentGeneration> {

    /**
     * 根据房东ID查询文案生成记录，按时间倒序
     *
     * @param landlordId 房东ID
     * @param limit      查询数量限制
     * @return 文案生成记录列表
     */
    @Select("SELECT * FROM ai_content_generation WHERE landlord_id = #{landlordId} ORDER BY create_time DESC LIMIT #{limit}")
    List<AIContentGeneration> selectByLandlordIdWithLimit(@Param("landlordId") Long landlordId, @Param("limit") Integer limit);

    /**
     * 根据房源ID查询文案生成记录
     *
     * @param propertyId 房源ID
     * @return 文案生成记录
     */
    @Select("SELECT * FROM ai_content_generation WHERE property_id = #{propertyId} ORDER BY create_time DESC LIMIT 1")
    AIContentGeneration selectByPropertyId(@Param("propertyId") Long propertyId);

    /**
     * 标记文案为已应用
     *
     * @param id         记录ID
     * @param propertyId 房源ID
     * @return 影响行数
     */
    @Update("UPDATE ai_content_generation SET is_applied = 1, property_id = #{propertyId} WHERE id = #{id}")
    int markAsApplied(@Param("id") Long id, @Param("propertyId") Long propertyId);
}
