package com.smartproperty.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartproperty.dto.*;
import com.smartproperty.entity.*;
import com.smartproperty.mapper.*;
import com.smartproperty.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI服务实现类
 * 整合AI问答、文案生成、智能推荐三大功能
 * 负责业务逻辑处理和数据持久化
 *
 * @author 毕业设计项目
 */
@Service
public class AIServiceImpl implements AIService {

    private static final Logger log = LoggerFactory.getLogger(AIServiceImpl.class);

    @Autowired
    private QAService qaService;

    @Autowired
    private ContentGenerationService contentGenerationService;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private AIQAHistoryMapper qaHistoryMapper;

    @Autowired
    private AIContentGenerationMapper contentGenerationMapper;

    @Autowired
    private AIRecommendationLogMapper recommendationLogMapper;

    @Autowired
    private UserBehaviorLogMapper behaviorLogMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==================== AI问答相关实现 ====================

    /**
     * 提问AI并获取回答
     * 1. 调用QAService获取答案
     * 2. 保存问答历史到数据库
     * 3. 返回响应结果
     */
    @Override
    @Transactional
    public QAResponse askQuestion(Long userId, QARequest request) {
        log.info("用户 {} 提问: {}", userId, request.getQuestion());

        try {
            // 1. 调用QAService获取答案
            String answer = qaService.getAnswer(request.getQuestion());

            // 2. 识别问题类型
            String questionType = request.getQuestionType();
            if (questionType == null || questionType.isEmpty()) {
                questionType = qaService.identifyQuestionType(request.getQuestion());
            }

            // 3. 获取相关问题推荐
            List<String> relatedQuestions = qaService.getRelatedQuestions(questionType);

            // 4. 保存问答历史
            AIQAHistory history = new AIQAHistory();
            history.setUserId(userId);
            history.setQuestion(request.getQuestion());
            history.setAnswer(answer);
            history.setQuestionType(questionType);
            qaHistoryMapper.insert(history);

            log.info("问答历史已保存，ID: {}", history.getId());

            // 5. 构建响应
            QAResponse response = new QAResponse(answer, questionType, relatedQuestions);
            response.setConfidence(0.85); // 模拟置信度

            return response;

        } catch (Exception e) {
            log.error("AI问答服务异常", e);
            throw new RuntimeException("AI问答服务暂时不可用，请稍后重试");
        }
    }

    /**
     * 获取用户的问答历史记录
     */
    @Override
    public List<QAHistoryDTO> getQAHistory(Long userId, Integer limit) {
        log.info("查询用户 {} 的问答历史，限制 {} 条", userId, limit);

        if (limit == null || limit <= 0) {
            limit = 20;
        }

        try {
            List<AIQAHistory> historyList = qaHistoryMapper.selectByUserIdWithLimit(userId, limit);

            return historyList.stream()
                    .map(this::convertToQAHistoryDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("查询问答历史失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 转换AIQAHistory实体为DTO
     */
    private QAHistoryDTO convertToQAHistoryDTO(AIQAHistory history) {
        QAHistoryDTO dto = new QAHistoryDTO();
        dto.setId(history.getId());
        dto.setUserId(history.getUserId());
        dto.setQuestion(history.getQuestion());
        dto.setAnswer(history.getAnswer());
        dto.setQuestionType(history.getQuestionType());
        dto.setCreateTime(history.getCreateTime());
        return dto;
    }

    // ==================== 文案生成相关实现 ====================

    /**
     * 生成房源宣传文案
     * 1. 调用ContentGenerationService生成文案
     * 2. 保存生成记录到数据库
     * 3. 返回响应结果
     */
    @Override
    @Transactional
    public ContentGenerationResponse generatePropertyContent(Long landlordId, ContentRequest request) {
        log.info("房东 {} 请求生成文案，房源标题: {}", landlordId, request.getTitle());

        try {
            // 1. 调用ContentGenerationService生成文案
            ContentGenerationResponse response = contentGenerationService.generate(request);

            // 2. 将请求数据转换为JSON
            String inputDataJson = objectMapper.writeValueAsString(request);

            // 3. 保存生成记录
            AIContentGeneration generation = new AIContentGeneration();
            generation.setLandlordId(landlordId);
            generation.setInputData(inputDataJson);
            generation.setShortContent(response.getShortVersion());
            generation.setDetailContent(response.getDetailVersion());
            generation.setIsApplied(0);
            contentGenerationMapper.insert(generation);

            log.info("文案生成记录已保存，ID: {}", generation.getId());

            // 4. 设置生成记录ID到响应
            response.setGenerationId(generation.getId());

            return response;

        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            throw new RuntimeException("文案生成失败，请检查输入数据");
        } catch (Exception e) {
            log.error("文案生成服务异常", e);
            throw new RuntimeException("文案生成服务暂时不可用，请稍后重试");
        }
    }

    /**
     * 标记文案为已应用
     */
    @Override
    @Transactional
    public boolean markContentAsApplied(Long generationId, Long propertyId) {
        log.info("标记文案 {} 为已应用，房源ID: {}", generationId, propertyId);

        try {
            int rows = contentGenerationMapper.markAsApplied(generationId, propertyId);
            return rows > 0;
        } catch (Exception e) {
            log.error("标记文案失败", e);
            return false;
        }
    }

    /**
     * 获取房东的文案生成历史
     */
    @Override
    public List<ContentGenerationResponse> getContentGenerationHistory(Long landlordId, Integer limit) {
        log.info("查询房东 {} 的文案生成历史，限制 {} 条", landlordId, limit);

        if (limit == null || limit <= 0) {
            limit = 10;
        }

        try {
            List<AIContentGeneration> generationList = 
                    contentGenerationMapper.selectByLandlordIdWithLimit(landlordId, limit);

            return generationList.stream()
                    .map(this::convertToContentGenerationResponse)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("查询文案生成历史失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 转换AIContentGeneration实体为DTO
     */
    private ContentGenerationResponse convertToContentGenerationResponse(AIContentGeneration generation) {
        ContentGenerationResponse response = new ContentGenerationResponse();
        response.setShortVersion(generation.getShortContent());
        response.setDetailVersion(generation.getDetailContent());
        response.setGenerationId(generation.getId());
        return response;
    }

    // ==================== 智能推荐相关实现 ====================

    /**
     * 获取为用户推荐的房源列表
     * 1. 调用RecommendationService获取推荐
     * 2. 保存推荐日志到数据库
     * 3. 返回推荐结果
     */
    @Override
    @Transactional
    public List<RecommendationDTO> getRecommendedProperties(Long userId, Integer limit) {
        log.info("为用户 {} 生成推荐，限制 {} 条", userId, limit);

        if (limit == null || limit <= 0) {
            limit = 10;
        }

        try {
            // 1. 调用RecommendationService获取推荐
            List<RecommendationService.PropertyRecommendation> recommendations = 
                    recommendationService.recommend(userId, limit);

            // 2. 转换为DTO并保存推荐日志
            List<RecommendationDTO> result = new ArrayList<>();
            for (RecommendationService.PropertyRecommendation recommendation : recommendations) {
                // 保存推荐日志（仅登录用户）
                if (userId != null) {
                    AIRecommendationLog logEntry = new AIRecommendationLog();
                    logEntry.setUserId(userId);
                    logEntry.setPropertyId(recommendation.getProperty().getId());
                    logEntry.setRecommendScore(recommendation.getScore());
                    logEntry.setRecommendReason(recommendation.getReason());
                    logEntry.setIsClicked(0);
                    recommendationLogMapper.insert(logEntry);
                }

                // 转换为DTO
                RecommendationDTO dto = new RecommendationDTO();
                dto.setProperty(recommendation.getProperty());
                dto.setRecommendScore(recommendation.getScore());
                dto.setRecommendReason(recommendation.getReason());

                result.add(dto);
            }

            log.info("推荐日志已保存，共 {} 条", result.size());

            return result;

        } catch (Exception e) {
            log.error("智能推荐服务异常", e);
            throw new RuntimeException("智能推荐服务暂时不可用，请稍后重试");
        }
    }

    /**
     * 记录用户点击推荐房源的行为
     */
    @Override
    @Transactional
    public void recordRecommendClick(Long userId, Long propertyId, String source) {
        log.info("记录用户 {} 点击推荐房源 {}，来源: {}", userId, propertyId, source);

        try {
            // 1. 更新推荐日志的点击状态
            LocalDateTime clickTime = LocalDateTime.now();
            int rows = recommendationLogMapper.markAsClicked(userId, propertyId, clickTime);

            if (rows > 0) {
                log.info("推荐点击记录已更新");
            }

            // 2. 记录用户行为日志
            recordUserBehavior(userId, propertyId, "view");

        } catch (Exception e) {
            log.error("记录推荐点击失败", e);
            // 不抛出异常，避免影响用户体验
        }
    }

    /**
     * 记录用户行为日志
     * 用于推荐算法的数据支持
     */
    @Override
    @Transactional
    public void recordUserBehavior(Long userId, Long propertyId, String behaviorType) {
        log.info("记录用户 {} 行为: {} 房源 {}", userId, behaviorType, propertyId);

        try {
            UserBehaviorLog behaviorLog = new UserBehaviorLog();
            behaviorLog.setUserId(userId);
            behaviorLog.setPropertyId(propertyId);
            behaviorLog.setBehaviorType(behaviorType);
            behaviorLog.setCreateTime(LocalDateTime.now());
            behaviorLogMapper.insert(behaviorLog);

            log.info("用户行为日志已保存");

        } catch (Exception e) {
            log.error("记录用户行为失败", e);
            // 不抛出异常，避免影响主业务流程
        }
    }
}
