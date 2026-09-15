package com.smartproperty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartproperty.entity.UserBehaviorLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 用户行为日志Mapper接口
 * 提供用户行为日志的数据库操作
 *
 * @author 毕业设计项目
 */
@Mapper
public interface UserBehaviorLogMapper extends BaseMapper<UserBehaviorLog> {

    /**
     * 根据用户ID和行为类型查询行为日志
     *
     * @param userId       用户ID
     * @param behaviorType 行为类型
     * @param limit        查询数量限制
     * @return 行为日志列表
     */
    @Select("SELECT * FROM user_behavior_log WHERE user_id = #{userId} AND behavior_type = #{behaviorType} " +
            "ORDER BY create_time DESC LIMIT #{limit}")
    List<UserBehaviorLog> selectByUserIdAndType(@Param("userId") Long userId,
                                                  @Param("behaviorType") String behaviorType,
                                                  @Param("limit") Integer limit);

    /**
     * 根据用户ID查询所有行为日志
     *
     * @param userId 用户ID
     * @param limit  查询数量限制
     * @return 行为日志列表
     */
    @Select("SELECT * FROM user_behavior_log WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit}")
    List<UserBehaviorLog> selectByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 统计用户对某个房源的行为次数
     *
     * @param userId     用户ID
     * @param propertyId 房源ID
     * @return 行为次数
     */
    @Select("SELECT COUNT(*) FROM user_behavior_log WHERE user_id = #{userId} AND property_id = #{propertyId}")
    Long countByUserIdAndPropertyId(@Param("userId") Long userId, @Param("propertyId") Long propertyId);

    /**
     * 查询用户浏览过的房源ID列表（去重）
     *
     * @param userId 用户ID
     * @param limit  查询数量限制
     * @return 房源ID列表
     */
    @Select("SELECT DISTINCT property_id FROM (" +
            "SELECT property_id, create_time FROM user_behavior_log " +
            "WHERE user_id = #{userId} AND behavior_type = 'view' " +
            "ORDER BY create_time DESC LIMIT #{limit}" +
            ") AS temp")
    List<Long> selectViewedPropertyIds(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 查询用户收藏的房源ID列表（去重）
     *
     * @param userId 用户ID
     * @return 房源ID列表
     */
    @Select("SELECT DISTINCT property_id FROM (" +
            "SELECT property_id, create_time FROM user_behavior_log " +
            "WHERE user_id = #{userId} AND behavior_type = 'favorite' " +
            "ORDER BY create_time DESC" +
            ") AS temp")
    List<Long> selectFavoritePropertyIds(@Param("userId") Long userId);

    /**
     * 统计用户各类行为的数量
     *
     * @param userId 用户ID
     * @return 行为类型和数量的映射
     */
    @Select("SELECT behavior_type, COUNT(*) as count FROM user_behavior_log " +
            "WHERE user_id = #{userId} GROUP BY behavior_type")
    List<Map<String, Object>> countBehaviorsByType(@Param("userId") Long userId);
}
