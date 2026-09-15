package com.smartproperty.common;

import java.lang.annotation.*;

/**
 * AI功能权限验证注解
 * 用于标注需要特定角色才能访问的AI功能接口
 * 
 * 使用示例：
 * @AIPermission(roles = {0, 1}) // 允许用户和房东访问
 * @AIPermission(roles = {1}) // 仅允许房东访问
 * @AIPermission(roles = {0}) // 仅允许普通用户访问
 *
 * @author 毕业设计项目
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AIPermission {

    /**
     * 允许访问的角色列表
     * 0: 普通用户
     * 1: 房东
     * 2: 管理员
     * 
     * @return 角色数组
     */
    int[] roles() default {};

    /**
     * 权限描述（可选）
     * 用于日志记录和错误提示
     * 
     * @return 权限描述
     */
    String description() default "";
}
