package com.smartproperty.service;

import com.smartproperty.dto.*;
import com.smartproperty.entity.Property;

import java.util.List;

/**
 * AI服务接口
 * 定义所有AI功能的服务接口方法
 * 包括：AI问答、文案生成、智能推荐
 *
 * @author 毕业设计项目
 */
public interface AIService {

    // ==================== AI问答相关 ====================

    /**
     * 提问AI并获取回答
     * 同时保存问答历史到数据库
     *
     * @param userId  用户ID
     * @param request 问答请求
     * @return 问答响应
     */
    QAResponse askQuestion(Long userId, QARequest request);

    /**
     * 获取用户的问答历史记录
     *
     * @param userId 用户ID
     * @param limit  查询数量限制，默认20条
     * @return 问答历史列表
     */
    List<QAHistoryDTO> getQAHistory(Long userId, Integer limit);

    // ==================== 文案生成相关 ====================

    /**
     * 生成房源宣传文案
     * 同时保存生成记录到数据库
     *
     * @param landlordId 房东ID
     * @param request    文案生成请求
     * @return 文案生成响应
     */
    ContentGenerationResponse generatePropertyContent(Long landlordId, ContentRequest request);

    /**
     * 标记文案为已应用
     * 当房东将生成的文案应用到房源时调用
     *
     * @param generationId 文案生成记录ID
     * @param propertyId   房源ID
     * @return 是否标记成功
     */
    boolean markContentAsApplied(Long generationId, Long propertyId);

    /**
     * 获取房东的文案生成历史
     *
     * @param landlordId 房东ID
     * @param limit      查询数量限制，默认10条
     * @return 文案生成记录列表
     */
    List<ContentGenerationResponse> getContentGenerationHistory(Long landlordId, Integer limit);

    // ==================== 智能推荐相关 ====================

    /**
     * 获取为用户推荐的房源列表
     * 基于用户行为数据进行个性化推荐
     * 同时记录推荐日志到数据库
     *
     * @param userId 用户ID
     * @param limit  推荐数量限制，默认10条
     * @return 推荐房源列表
     */
    List<RecommendationDTO> getRecommendedProperties(Long userId, Integer limit);

    /**
     * 记录用户点击推荐房源的行为
     * 用于优化推荐算法和统计点击率
     *
     * @param userId     用户ID
     * @param propertyId 房源ID
     * @param source     推荐来源（如：recommendation、hot等）
     */
    void recordRecommendClick(Long userId, Long propertyId, String source);

    /**
     * 记录用户行为日志
     * 用于推荐算法的数据支持
     *
     * @param userId       用户ID
     * @param propertyId   房源ID
     * @param behaviorType 行为类型（view/favorite/appointment/unfavorite）
     */
    void recordUserBehavior(Long userId, Long propertyId, String behaviorType);
}
