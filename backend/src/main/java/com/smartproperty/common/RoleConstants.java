package com.smartproperty.common;

/**
 * 角色常量类
 * 定义系统中的用户角色
 *
 * @author 毕业设计项目
 */
public class RoleConstants {

    /** 普通用户 */
    public static final Integer ROLE_USER = 0;

    /** 房东 */
    public static final Integer ROLE_LANDLORD = 1;

    /** 管理员 */
    public static final Integer ROLE_ADMIN = 2;

    /**
     * 检查是否是管理员
     * @param role 角色值
     * @return 是否是管理员
     */
    public static boolean isAdmin(Integer role) {
        return ROLE_ADMIN.equals(role);
    }

    /**
     * 检查是否是房东
     * @param role 角色值
     * @return 是否是房东
     */
    public static boolean isLandlord(Integer role) {
        return ROLE_LANDLORD.equals(role);
    }

    /**
     * 检查是否是普通用户
     * @param role 角色值
     * @return 是否是普通用户
     */
    public static boolean isUser(Integer role) {
        return ROLE_USER.equals(role);
    }

    /**
     * 获取角色名称
     * @param role 角色值
     * @return 角色名称
     */
    public static String getRoleName(Integer role) {
        if (ROLE_ADMIN.equals(role)) {
            return "管理员";
        } else if (ROLE_LANDLORD.equals(role)) {
            return "房东";
        } else {
            return "普通用户";
        }
    }
}
