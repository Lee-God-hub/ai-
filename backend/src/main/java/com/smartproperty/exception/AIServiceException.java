package com.smartproperty.exception;

/**
 * AI服务业务异常类
 * 用于AI功能模块的业务异常处理
 * 包含自定义错误码和错误信息
 * 
 * 错误码规范：
 * - 5001-5099: AI问答相关错误
 * - 5101-5199: 文案生成相关错误
 * - 5201-5299: 智能推荐相关错误
 *
 * @author 毕业设计项目
 */
public class AIServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 错误码 */
    private Integer errorCode;

    /** 错误信息 */
    private String errorMessage;

    /**
     * 构造函数
     * 
     * @param errorCode 错误码
     * @param errorMessage 错误信息
     */
    public AIServiceException(Integer errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * 构造函数（仅错误信息，使用默认错误码5000）
     * 
     * @param errorMessage 错误信息
     */
    public AIServiceException(String errorMessage) {
        this(5000, errorMessage);
    }

    /**
     * 构造函数（包含原始异常）
     * 
     * @param errorCode 错误码
     * @param errorMessage 错误信息
     * @param cause 原始异常
     */
    public AIServiceException(Integer errorCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    // ==================== 预定义异常工厂方法 ====================

    /**
     * AI问答服务异常
     */
    public static AIServiceException qaServiceError(String message) {
        return new AIServiceException(5001, "AI问答服务异常: " + message);
    }

    /**
     * 问题内容无效
     */
    public static AIServiceException invalidQuestion(String message) {
        return new AIServiceException(5002, "问题内容无效: " + message);
    }

    /**
     * 问答历史查询失败
     */
    public static AIServiceException qaHistoryQueryError(String message) {
        return new AIServiceException(5003, "问答历史查询失败: " + message);
    }

    /**
     * 文案生成服务异常
     */
    public static AIServiceException contentGenerationError(String message) {
        return new AIServiceException(5101, "文案生成服务异常: " + message);
    }

    /**
     * 房源信息不完整
     */
    public static AIServiceException incompletePropertyInfo(String message) {
        return new AIServiceException(5102, "房源信息不完整: " + message);
    }

    /**
     * 文案生成记录不存在
     */
    public static AIServiceException contentRecordNotFound(Long generationId) {
        return new AIServiceException(5103, "文案生成记录不存在: ID=" + generationId);
    }

    /**
     * 智能推荐服务异常
     */
    public static AIServiceException recommendationError(String message) {
        return new AIServiceException(5201, "智能推荐服务异常: " + message);
    }

    /**
     * 用户行为数据不足
     */
    public static AIServiceException insufficientUserData(Long userId) {
        return new AIServiceException(5202, "用户行为数据不足，无法生成推荐: userId=" + userId);
    }

    /**
     * 推荐算法计算失败
     */
    public static AIServiceException recommendationCalculationError(String message) {
        return new AIServiceException(5203, "推荐算法计算失败: " + message);
    }

    /**
     * 用户行为记录失败
     */
    public static AIServiceException behaviorRecordError(String message) {
        return new AIServiceException(5204, "用户行为记录失败: " + message);
    }

    // ==================== Getter / Setter ====================

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "AIServiceException{" +
                "errorCode=" + errorCode +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
