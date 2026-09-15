package com.smartproperty.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt密码生成工具
 * 用于开发测试生成密码
 */
public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 生成 123456 的BCrypt哈希
        String password = "123456";
        String hash = encoder.encode(password);
        
        System.out.println("密码: " + password);
        System.out.println("BCrypt哈希: " + hash);
        
        // 验证一下
        boolean matches = encoder.matches(password, hash);
        System.out.println("验证结果: " + matches);
    }
}
