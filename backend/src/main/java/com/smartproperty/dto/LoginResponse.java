package com.smartproperty.dto;

/**
 * 登录响应DTO
 * 封装登录成功后返回的信息
 *
 * @author 毕业设计项目
 */
public class LoginResponse {

    /** JWT令牌 */
    private String token;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 用户角色 */
    private Integer role;

    /** 头像 */
    private String avatar;

    public LoginResponse() {
    }

    public LoginResponse(String token, Long userId, String username, String realName, 
                         String phone, String email, Integer role, String avatar) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.realName = realName;
        this.phone = phone;
        this.email = email;
        this.role = role;
        this.avatar = avatar;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
