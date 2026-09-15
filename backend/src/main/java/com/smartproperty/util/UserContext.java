package com.smartproperty.util;

/**
 * 用户上下文工具类
 * 在请求线程中存储当前登录用户信息
 *
 * @author 毕业设计项目
 */
public class UserContext {

    private static final ThreadLocal<Long> userIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> usernameHolder = new ThreadLocal<>();
    private static final ThreadLocal<Integer> roleHolder = new ThreadLocal<>();

    /**
     * 设置用户ID
     */
    public static void setUserId(Long userId) {
        userIdHolder.set(userId);
    }

    /**
     * 获取用户ID
     */
    public static Long getUserId() {
        return userIdHolder.get();
    }

    /**
     * 设置用户名
     */
    public static void setUsername(String username) {
        usernameHolder.set(username);
    }

    /**
     * 获取用户名
     */
    public static String getUsername() {
        return usernameHolder.get();
    }

    /**
     * 设置用户角色
     */
    public static void setRole(Integer role) {
        roleHolder.set(role);
    }

    /**
     * 获取用户角色
     */
    public static Integer getRole() {
        return roleHolder.get();
    }

    /**
     * 清除所有用户信息
     */
    public static void clear() {
        userIdHolder.remove();
        usernameHolder.remove();
        roleHolder.remove();
    }
}
