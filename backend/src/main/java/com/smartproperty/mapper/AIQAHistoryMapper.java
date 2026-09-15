package com.smartproperty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartproperty.entity.AIQAHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * AI问答历史Mapper接口
 * 提供AI问答历史记录的数据库操作
 *
 * @author 毕业设计项目
 */
@Mapper
public interface AIQAHistoryMapper extends BaseMapper<AIQAHistory> {

    /**
     * 根据用户ID查询问答历史，按时间倒序
     *
     * @param userId 用户ID
     * @param limit  查询数量限制
     * @return 问答历史列表
     */
    @Select("SELECT * FROM ai_qa_history WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit}")
    List<AIQAHistory> selectByUserIdWithLimit(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 根据问题类型统计数量
     *
     * @param questionType 问题类型
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM ai_qa_history WHERE question_type = #{questionType}")
    Long countByQuestionType(@Param("questionType") String questionType);
}
