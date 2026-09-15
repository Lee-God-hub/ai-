package com.smartproperty.config;

import com.smartproperty.interceptor.AIPermissionInterceptor;
import com.smartproperty.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;
import java.util.List;

/**
 * Web配置类
 * 配置跨域、拦截器和Security
 *
 * @author 毕业设计项目
 */
@Configuration
@EnableWebSecurity
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Autowired
    private AIPermissionInterceptor aiPermissionInterceptor;

    /**
     * 允许的前端源（精确匹配）
     * 说明：同时使用 AllowedOriginPatterns("*") 做兜底，避免因端口变化导致 CORS 失败
     */
    private static final String[] ALLOWED_ORIGIN_PATTERNS = {
            "http://localhost:3000",
            "http://localhost:3002",
            "http://localhost:5173",
            "http://localhost:5174",
            "http://localhost:5175",
            "http://localhost:5176",
            "http://localhost:8080",
            "http://127.0.0.1:3000",
            "http://127.0.0.1:5173",
            "http://127.0.0.1:5175"
    };

    /**
     * 配置CORS跨域（MVC层）
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * CORS配置源（Security层使用）
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(ALLOWED_ORIGIN_PATTERNS));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 配置拦截器
     * 拦截器执行顺序：
     * 1. JwtInterceptor - JWT认证拦截器（验证token）
     * 2. AIPermissionInterceptor - AI权限验证拦截器（验证角色权限）
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // JWT认证拦截器
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        "/user/check-username",
                        "/user/check-phone",
                        "/user/health",
                        "/test/**"
                );

        // AI权限验证拦截器
        // 只拦截AI相关接口，在JWT验证之后执行
        registry.addInterceptor(aiPermissionInterceptor)
                .addPathPatterns("/api/ai/**")
                .order(2); // 设置执行顺序，在JWT拦截器之后
    }

    /**
     * 配置Spring Security
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF（因为使用JWT）
                .csrf(csrf -> csrf.disable())
                // 配置CORS - 使用corsConfigurationSource Bean
                .cors(Customizer.withDefaults())
                // 允许所有请求（由我们的JWT拦截器处理认证）
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    /**
     * 配置密码加密器（BCrypt）
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
