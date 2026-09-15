package com.smartproperty.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartproperty.common.Result;
import com.smartproperty.util.JwtUtil;
import com.smartproperty.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT认证拦截器
 * 拦截需要认证的请求，验证JWT令牌
 *
 * @author 毕业设计项目
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 清除之前的用户上下文
        UserContext.clear();

        // 从请求头中获取token
        String authHeader = request.getHeader("Authorization");

        // 如果没有token，允许匿名访问（业务接口自己判断是否需要登录）
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return true;
        }

        // 提取token
        String token = authHeader.substring(7);

        try {
            // 解析token
            Long userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);
            Integer role = jwtUtil.getRoleFromToken(token);

            if (userId == null || username == null) {
                // token 格式错误，放行但不设置用户上下文
                return true;
            }

            // 验证token是否有效
            if (!jwtUtil.validateToken(token, username)) {
                // token 过期，放行但不设置用户上下文
                return true;
            }

            // 将用户信息存入上下文
            UserContext.setUserId(userId);
            UserContext.setUsername(username);
            UserContext.setRole(role);

            return true;

        } catch (Exception e) {
            // token 解析异常，放行但不设置用户上下文
            return true;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求完成后清除用户上下文
        UserContext.clear();
    }

    /**
     * 发送错误响应
     */
    private void sendError(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        Result<Object> result = Result.error(code, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
