package com.smartproperty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartproperty.entity.AIRecommendationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI推荐日志Mapper接口
 * 提供AI推荐日志的数据库操作
 *
 * @author 毕业设计项目
 */
@Mapper
public interface AIRecommendationLogMapper extends BaseMapper<AIRecommendationLog> {

    /**
     * 根据用户ID查询推荐日志，按推荐分数倒序
     *
     * @param userId 用户ID
     * @param limit  查询数量限制
     * @return 推荐日志列表
     */
    @Select("SELECT * FROM ai_recommendation_log WHERE user_id = #{userId} ORDER BY recommend_score DESC, create_time DESC LIMIT #{limit}")
    List<AIRecommendationLog> selectByUserIdWithLimit(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 根据房源ID统计被推荐次数
     *
     * @param propertyId 房源ID
     * @return 推荐次数
     */
    @Select("SELECT COUNT(*) FROM ai_recommendation_log WHERE property_id = #{propertyId}")
    Long countByPropertyId(@Param("propertyId") Long propertyId);

    /**
     * 标记推荐为已点击
     *
     * @param userId     用户ID
     * @param propertyId 房源ID
     * @param clickTime  点击时间
     * @return 影响行数
     */
    @Update("UPDATE ai_recommendation_log SET is_clicked = 1, click_time = #{clickTime} " +
            "WHERE user_id = #{userId} AND property_id = #{propertyId} AND is_clicked = 0")
    int markAsClicked(@Param("userId") Long userId, @Param("propertyId") Long propertyId, @Param("clickTime") LocalDateTime clickTime);

    /**
     * 查询用户最近推荐的房源ID列表
     *
     * @param userId 用户ID
     * @param hours  最近多少小时内
     * @return 房源ID列表
     */
    @Select("SELECT DISTINCT property_id FROM ai_recommendation_log " +
            "WHERE user_id = #{userId} AND create_time > DATE_SUB(NOW(), INTERVAL #{hours} HOUR)")
    List<Long> selectRecentRecommendedPropertyIds(@Param("userId") Long userId, @Param("hours") Integer hours);
}
