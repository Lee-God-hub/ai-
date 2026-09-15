package com.smartproperty.exception;

import com.smartproperty.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * 全局异常处理器
 * 统一处理应用程序中的异常，返回规范的错误响应
 * 
 * 异常处理优先级（从高到低）：
 * 1. AIServiceException - AI业务异常
 * 2. IllegalArgumentException - 参数异常
 * 3. AccessDeniedException - 权限异常
 * 4. MethodArgumentNotValidException - 参数验证异常
 * 5. RuntimeException - 运行时异常
 * 6. Exception - 通用异常
 * 
 * @author 毕业设计项目
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 处理AI服务业务异常
     * 返回自定义错误码和错误信息
     * 
     * @param e AI服务异常
     * @return 错误响应
     */
    @ExceptionHandler(AIServiceException.class)
    public Result<String> handleAIServiceException(AIServiceException e) {
        logger.error("AI服务异常 [错误码: {}]: {}", e.getErrorCode(), e.getErrorMessage(), e);
        return Result.error(e.getErrorCode(), e.getErrorMessage());
    }
    
    /**
     * 处理参数异常
     * 返回400错误码
     * 
     * @param e 参数异常
     * @return 错误响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<String> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.warn("参数异常: {}", e.getMessage());
        return Result.paramError(e.getMessage());
    }
    
    /**
     * 处理权限拒绝异常
     * 返回403错误码
     * 
     * @param e 权限拒绝异常
     * @return 错误响应
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Result<String> handleAccessDeniedException(AccessDeniedException e) {
        logger.warn("权限拒绝: {}", e.getMessage());
        return Result.forbidden("您没有权限访问此功能");
    }
    
    /**
     * 处理参数验证异常
     * 返回400错误码
     * 
     * @param e 参数验证异常
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleValidationException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        
        StringBuilder errorMessage = new StringBuilder("参数验证失败: ");
        for (FieldError error : fieldErrors) {
            errorMessage.append(error.getField())
                       .append(" - ")
                       .append(error.getDefaultMessage())
                       .append("; ");
        }
        
        logger.warn("参数验证异常: {}", errorMessage.toString());
        return Result.paramError(errorMessage.toString());
    }
    
    /**
     * 处理业务逻辑异常
     * 返回500错误码
     * 
     * @param e 业务逻辑异常
     * @return 错误响应
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<String> handleBusinessException(RuntimeException e) {
        logger.error("业务逻辑异常: {}", e.getMessage(), e);
        return Result.error(e.getMessage());
    }
    
    /**
     * 处理所有其他异常
     * 返回500错误码
     * 
     * @param e 异常对象
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        logger.error("系统异常: {}", e.getMessage(), e);
        return Result.error("系统内部错误，请稍后重试");
    }
}