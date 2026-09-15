package com.smartproperty.entity;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库中的用户表，存储用户基本信息
 *
 * @author 毕业设计项目
 */
public class User {

    /** 用户ID（主键） */
    private Long id;

    /** 用户名（唯一） */
    private String username;

    /** 密码（加密存储） */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 手机号码 */
    private String phone;

    /** 电子邮箱 */
    private String email;

    /**
     * 用户角色
     * 0: 普通用户, 1: 经纪人, 2: 管理员
     */
    private Integer role;

    /**
     * 用户状态
     * 0: 禁用, 1: 正常
     */
    private Integer status;

    /** 头像URL */
    private String avatar;

    /** 注册时间 */
    private LocalDateTime createTime;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

    /** 最后更新时间 */
    private LocalDateTime updateTime;

    /** 备注信息 */
    private String remark;

    /** 默认构造方法 */
    public User() {
        this.role = 0;
        this.status = 1;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /** 注册用构造方法 */
    public User(String username, String password, String phone) {
        this();
        this.username = username;
        this.password = password;
        this.phone = phone;
    }

    // ===== Getter / Setter =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getRole() { return role; }
    public void setRole(Integer role) { this.role = role; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getLastLoginTime() { return lastLoginTime; }
    public void setLastLoginTime(LocalDateTime lastLoginTime) { this.lastLoginTime = lastLoginTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', phone='" + phone + "', role=" + role + ", status=" + status + "}";
    }
}
