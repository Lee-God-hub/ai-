package com.smartproperty.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartproperty.common.AIPermission;
import com.smartproperty.common.Result;
import com.smartproperty.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Arrays;

/**
 * AI功能权限验证拦截器
 * 拦截带有@AIPermission注解的接口，验证用户角色权限
 * 
 * 工作流程：
 * 1. 检查方法是否有@AIPermission注解
 * 2. 从UserContext获取当前用户角色
 * 3. 验证用户角色是否在允许的角色列表中
 * 4. 如果验证失败，返回403错误
 *
 * @author 毕业设计项目
 */
@Component
public class AIPermissionInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AIPermissionInterceptor.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 在请求处理之前进行权限验证
     *
     * @param request  HTTP请求
     * @param response HTTP响应
     * @param handler  处理器
     * @return true-继续处理，false-拦截请求
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只处理方法级别的处理器
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 检查方法是否有@AIPermission注解
        AIPermission permission = handlerMethod.getMethodAnnotation(AIPermission.class);
        if (permission == null) {
            // 没有注解，放行
            return true;
        }

        // 获取允许的角色列表
        int[] allowedRoles = permission.roles();
        String description = permission.description();

        log.info("AI权限验证 - 接口: {}, 允许角色: {}, 描述: {}",
                request.getRequestURI(), Arrays.toString(allowedRoles), description);

        // 从UserContext获取当前用户角色
        Integer userRole = UserContext.getRole();

        if (userRole == null) {
            log.warn("AI权限验证失败 - 用户角色为空，URI: {}", request.getRequestURI());
            sendForbiddenError(response, "用户角色信息缺失，请重新登录");
            return false;
        }

        // 验证用户角色是否在允许的角色列表中
        boolean hasPermission = false;
        for (int allowedRole : allowedRoles) {
            if (userRole == allowedRole) {
                hasPermission = true;
                break;
            }
        }

        if (!hasPermission) {
            log.warn("AI权限验证失败 - 用户角色: {}, 允许角色: {}, URI: {}",
                    userRole, Arrays.toString(allowedRoles), request.getRequestURI());

            // 记录权限验证失败的访问尝试
            logAccessDenied(request, userRole, allowedRoles);

            sendForbiddenError(response, "您没有权限访问此AI功能");
            return false;
        }

        log.info("AI权限验证通过 - 用户角色: {}, URI: {}", userRole, request.getRequestURI());
        return true;
    }

    /**
     * 发送403禁止访问错误响应
     *
     * @param response HTTP响应
     * @param message  错误消息
     */
    private void sendForbiddenError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");

        Result<Void> result = Result.forbidden(message);
        String jsonResponse = objectMapper.writeValueAsString(result);

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }

    /**
     * 记录权限验证失败的访问尝试
     * 用于安全审计和异常检测
     *
     * @param request      HTTP请求
     * @param userRole     用户角色
     * @param allowedRoles 允许的角色列表
     */
    private void logAccessDenied(HttpServletRequest request, Integer userRole, int[] allowedRoles) {
        Long userId = UserContext.getUserId();
        String username = UserContext.getUsername();
        String uri = request.getRequestURI();
        String method = request.getMethod();
        String remoteAddr = request.getRemoteAddr();

        log.warn("=== AI权限验证失败记录 ===");
        log.warn("用户ID: {}, 用户名: {}, 角色: {}", userId, username, userRole);
        log.warn("请求URI: {}, 方法: {}", uri, method);
        log.warn("允许角色: {}", Arrays.toString(allowedRoles));
        log.warn("客户端IP: {}", remoteAddr);
        log.warn("========================");

        // TODO: 可以将此信息保存到数据库，用于安全审计
        // 例如：创建一个access_denied_log表，记录所有被拒绝的访问尝试
    }
}
