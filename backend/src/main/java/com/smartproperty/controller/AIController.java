package com.smartproperty.controller;

import com.smartproperty.common.AIPermission;
import com.smartproperty.common.Result;
import com.smartproperty.dto.*;
import com.smartproperty.service.AIService;
import com.smartproperty.util.UserContext;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI功能控制器
 * 提供AI智能问答、房源文案生成、智能推荐等功能的REST API接口
 * 所有接口均需要JWT认证，并根据用户角色进行权限控制
 * 
 * 权限说明：
 * - AI问答(qa)：所有角色(0,1,2)均可使用
 * - 文案生成(content)：房东(1)和管理员(2)可使用
 * - 智能推荐(recommend)：普通用户(0)和管理员(2)可使用
 * - 行为记录(behavior)：所有角色(0,1,2)均可使用
 *
 * @author 毕业设计项目
 */
@RestController
@RequestMapping("/ai")
public class AIController {

    private static final Logger logger = LoggerFactory.getLogger(AIController.class);

    @Autowired
    private AIService aiService;

    // ==================== AI问答相关接口 ====================

    /**
     * AI智能问答接口
     * 用户或房东可以提问房产相关问题，获取AI回答
     * 
     * @param request 问答请求，包含问题内容
     * @return AI回答结果，包含答案和相关问题推荐
     */
    @PostMapping("/qa/ask")
    @AIPermission(roles = {0, 1, 2}, description = "AI问答功能-所有角色可用")
    public Result<QAResponse> askQuestion(@Valid @RequestBody QARequest request) {
        try {
            Long userId = UserContext.getUserId();
            if (userId == null) {
                return Result.unauthorized("用户未登录");
            }

            logger.info("用户 {} 提问: {}", userId, request.getQuestion());
            
            QAResponse response = aiService.askQuestion(userId, request);
            
            logger.info("AI回答成功，问题类型: {}", response.getQuestionType());
            return Result.success("问答成功", response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("问答参数错误: {}", e.getMessage());
            return Result.paramError(e.getMessage());
        } catch (Exception e) {
            logger.error("AI问答失败", e);
            return Result.error("AI问答服务异常，请稍后重试");
        }
    }

    /**
     * 获取用户的问答历史记录
     * 返回最近的问答记录，用于展示历史对话
     * 
     * @param limit 查询数量限制，默认20条
     * @return 问答历史列表
     */
    @GetMapping("/qa/history")
    @AIPermission(roles = {0, 1, 2}, description = "查看问答历史-所有角色可用")
    public Result<List<QAHistoryDTO>> getQAHistory(
            @RequestParam(defaultValue = "20") Integer limit) {
        try {
            Long userId = UserContext.getUserId();
            if (userId == null) {
                return Result.unauthorized("用户未登录");
            }

            if (limit <= 0 || limit > 100) {
                return Result.paramError("查询数量限制必须在1-100之间");
            }

            logger.info("用户 {} 查询问答历史，数量: {}", userId, limit);
            
            List<QAHistoryDTO> history = aiService.getQAHistory(userId, limit);
            
            return Result.success("查询成功", history);
            
        } catch (Exception e) {
            logger.error("查询问答历史失败", e);
            return Result.error("查询问答历史失败");
        }
    }

    // ==================== 文案生成相关接口 ====================

    /**
     * AI房源文案生成接口
     * 房东填写房源基础信息，一键生成优质宣传文案
     * 支持简短版和详细版两种风格
     * 
     * @param request 文案生成请求，包含房源信息和生成参数
     * @return 生成的文案内容
     */
    @PostMapping("/content/generate")
    @AIPermission(roles = {1, 2}, description = "AI文案生成-房东和管理员可用")
    public Result<ContentGenerationResponse> generateContent(
            @Valid @RequestBody ContentRequest request) {
        try {
            Long landlordId = UserContext.getUserId();
            if (landlordId == null) {
                return Result.unauthorized("用户未登录");
            }

            logger.info("房东 {} 请求生成文案，房源类型: {}, 交易类型: {}", 
                    landlordId, request.getPropertyType(), request.getTransactionType());
            
            ContentGenerationResponse response = aiService.generatePropertyContent(landlordId, request);
            
            logger.info("文案生成成功，生成ID: {}", response.getGenerationId());
            return Result.success("文案生成成功", response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("文案生成参数错误: {}", e.getMessage());
            return Result.paramError(e.getMessage());
        } catch (Exception e) {
            logger.error("文案生成失败", e);
            return Result.error("文案生成服务异常，请稍后重试");
        }
    }

    /**
     * 标记文案为已应用
     * 当房东将生成的文案应用到房源发布时调用
     * 
     * @param generationId 文案生成记录ID
     * @param propertyId 房源ID
     * @return 标记结果
     */
    @PostMapping("/content/apply")
    @AIPermission(roles = {1, 2}, description = "标记文案已应用-房东和管理员可用")
    public Result<String> markContentAsApplied(
            @RequestParam Long generationId,
            @RequestParam Long propertyId) {
        try {
            Long landlordId = UserContext.getUserId();
            if (landlordId == null) {
                return Result.unauthorized("用户未登录");
            }

            if (generationId == null || propertyId == null) {
                return Result.paramError("生成记录ID和房源ID不能为空");
            }

            logger.info("房东 {} 标记文案 {} 已应用到房源 {}", landlordId, generationId, propertyId);
            
            boolean success = aiService.markContentAsApplied(generationId, propertyId);
            
            if (success) {
                return Result.success("标记成功");
            } else {
                return Result.error("标记失败，请检查记录是否存在");
            }
            
        } catch (Exception e) {
            logger.error("标记文案应用失败", e);
            return Result.error("标记失败");
        }
    }

    /**
     * 获取房东的文案生成历史
     * 返回最近的文案生成记录
     * 
     * @param limit 查询数量限制，默认10条
     * @return 文案生成历史列表
     */
    @GetMapping("/content/history")
    @AIPermission(roles = {1, 2}, description = "查看文案生成历史-房东和管理员可用")
    public Result<List<ContentGenerationResponse>> getContentHistory(
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            Long landlordId = UserContext.getUserId();
            if (landlordId == null) {
                return Result.unauthorized("用户未登录");
            }

            if (limit <= 0 || limit > 50) {
                return Result.paramError("查询数量限制必须在1-50之间");
            }

            logger.info("房东 {} 查询文案生成历史，数量: {}", landlordId, limit);
            
            List<ContentGenerationResponse> history = aiService.getContentGenerationHistory(landlordId, limit);
            
            return Result.success("查询成功", history);
            
        } catch (Exception e) {
            logger.error("查询文案生成历史失败", e);
            return Result.error("查询文案生成历史失败");
        }
    }

    // ==================== 智能推荐相关接口 ====================

    /**
     * 获取智能推荐房源列表
     * 基于用户浏览历史、收藏偏好等进行个性化推荐
     * 
     * @param limit 推荐数量限制，默认10条
     * @return 推荐房源列表，包含推荐理由和评分
     */
    @GetMapping("/recommend/properties")
    @AIPermission(roles = {0, 2}, description = "智能推荐-用户和管理员可用")
    public Result<List<RecommendationDTO>> getRecommendations(
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            Long userId = UserContext.getUserId();
            
            if (limit <= 0 || limit > 50) {
                return Result.paramError("推荐数量限制必须在1-50之间");
            }

            logger.info("用户 {} 请求智能推荐，数量: {}", userId, limit);
            
            List<RecommendationDTO> recommendations = aiService.getRecommendedProperties(userId, limit);
            
            logger.info("推荐成功，返回 {} 条房源", recommendations.size());
            return Result.success("推荐成功", recommendations);
            
        } catch (Exception e) {
            logger.error("智能推荐失败", e);
            return Result.error("智能推荐服务异常，请稍后重试");
        }
    }

    /**
     * 记录用户点击推荐房源的行为
     * 用于优化推荐算法和统计点击率
     * 
     * @param request 点击记录请求，包含房源ID和推荐来源
     * @return 记录结果
     */
    @PostMapping("/recommend/click")
    @AIPermission(roles = {0, 2}, description = "记录推荐点击-用户和管理员可用")
    public Result<String> recordRecommendClick(@Valid @RequestBody RecommendClickRequest request) {
        try {
            Long userId = UserContext.getUserId();
            if (userId == null) {
                return Result.unauthorized("用户未登录");
            }

            logger.info("用户 {} 点击推荐房源 {}, 来源: {}", 
                    userId, request.getPropertyId(), request.getSource());
            
            aiService.recordRecommendClick(userId, request.getPropertyId(), request.getSource());
            
            return Result.success("记录成功");
            
        } catch (IllegalArgumentException e) {
            logger.warn("记录点击参数错误: {}", e.getMessage());
            return Result.paramError(e.getMessage());
        } catch (Exception e) {
            logger.error("记录推荐点击失败", e);
            return Result.error("记录失败");
        }
    }

    /**
     * 记录用户行为日志
     * 用于推荐算法的数据支持
     * 内部接口，由其他业务模块调用
     * 
     * @param propertyId 房源ID
     * @param behaviorType 行为类型（view/favorite/appointment/unfavorite）
     * @return 记录结果
     */
    @PostMapping("/behavior/record")
    @AIPermission(roles = {0, 1, 2}, description = "记录用户行为-所有角色可用")
    public Result<String> recordUserBehavior(
            @RequestParam Long propertyId,
            @RequestParam String behaviorType) {
        try {
            Long userId = UserContext.getUserId();
            if (userId == null) {
                return Result.unauthorized("用户未登录");
            }

            if (propertyId == null) {
                return Result.paramError("房源ID不能为空");
            }

            if (behaviorType == null || behaviorType.trim().isEmpty()) {
                return Result.paramError("行为类型不能为空");
            }

            // 验证行为类型
            if (!behaviorType.matches("view|favorite|appointment|unfavorite")) {
                return Result.paramError("行为类型必须是: view, favorite, appointment, unfavorite");
            }

            logger.info("记录用户 {} 行为: {} 房源 {}", userId, behaviorType, propertyId);
            
            aiService.recordUserBehavior(userId, propertyId, behaviorType);
            
            return Result.success("记录成功");
            
        } catch (Exception e) {
            logger.error("记录用户行为失败", e);
            return Result.error("记录失败");
        }
    }

    // ==================== 健康检查接口 ====================

    /**
     * AI服务健康检查接口
     * 用于监控服务状态
     * 
     * @return 服务状态
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("AI服务运行正常");
    }
}
