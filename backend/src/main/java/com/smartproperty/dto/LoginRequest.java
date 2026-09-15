package com.smartproperty.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求DTO
 * 封装用户登录请求参数
 *
 * @author 毕业设计项目
 */
public class LoginRequest {

    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
