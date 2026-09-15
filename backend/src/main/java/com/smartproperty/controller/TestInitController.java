package com.smartproperty.controller;

import com.smartproperty.common.Result;
import com.smartproperty.entity.User;
import com.smartproperty.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 测试初始化控制器
 * 用于开发环境初始化测试数据
 */
@RestController
@RequestMapping("/test")
public class TestInitController {

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 初始化测试用户 - 强制重建
     * 注意：生产环境请删除此接口！
     */
    @GetMapping("/init-users")
    public Result<String> initTestUsers() {
        try {
            // 先用明文密码 123456 生成一个新的 BCrypt 哈希
            String passwordHash = encoder.encode("123456");
            
            // 测试一下验证逻辑是否正常工作
            boolean testValid = encoder.matches("123456", passwordHash);
            if (!testValid) {
                return Result.error("密码验证测试失败!");
            }

            System.out.println("===== 正在重建测试用户 =====");
            System.out.println("Password hash: " + passwordHash);

            // 先删除可能存在的旧用户
            User adminOld = userMapper.selectByUsername("admin");
            if (adminOld != null) {
                userMapper.deleteById(adminOld.getId());
                System.out.println("删除旧管理员成功");
            }
            
            User landlordOld = userMapper.selectByUsername("landlord1");
            if (landlordOld != null) {
                userMapper.deleteById(landlordOld.getId());
                System.out.println("删除旧房东成功");
            }
            
            User userOld = userMapper.selectByUsername("user1");
            if (userOld != null) {
                userMapper.deleteById(userOld.getId());
                System.out.println("删除旧用户成功");
            }

            // 创建全新的管理员
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordHash);
            admin.setRealName("系统管理员");
            admin.setPhone("13800138000");
            admin.setEmail("admin@smartproperty.com");
            admin.setRole(2);
            admin.setStatus(1);
            admin.setCreateTime(LocalDateTime.now());
            admin.setUpdateTime(LocalDateTime.now());
            userMapper.insert(admin);
            System.out.println("创建管理员成功: " + admin.getId());

            // 创建全新的房东
            User landlord = new User();
            landlord.setUsername("landlord1");
            landlord.setPassword(passwordHash);
            landlord.setRealName("张房东");
            landlord.setPhone("13900139001");
            landlord.setEmail("landlord1@smartproperty.com");
            landlord.setRole(1);
            landlord.setStatus(1);
            landlord.setCreateTime(LocalDateTime.now());
            landlord.setUpdateTime(LocalDateTime.now());
            userMapper.insert(landlord);
            System.out.println("创建房东成功: " + landlord.getId());

            // 创建全新的普通用户
            User user = new User();
            user.setUsername("user1");
            user.setPassword(passwordHash);
            user.setRealName("王用户");
            user.setPhone("13700137001");
            user.setEmail("user1@smartproperty.com");
            user.setRole(0);
            user.setStatus(1);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            userMapper.insert(user);
            System.out.println("创建用户成功: " + user.getId());

            System.out.println("===== 用户重建完成 =====");
            
            return Result.success("✅ 用户重建成功！\n"
                + "账号: admin / landlord1 / user1\n"
                + "密码: 123456");
                
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("初始化失败: " + e.getMessage());
        }
    }
}
