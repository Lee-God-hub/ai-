package com.smartproperty;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
public class PasswordTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void generateBCryptHash() {
        String rawPassword = "123456";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        System.out.println("=".repeat(60));
        System.out.println("原始密码: " + rawPassword);
        System.out.println("加密后密码: " + encodedPassword);
        System.out.println("密码长度: " + encodedPassword.length());
        System.out.println("密码验证: " + passwordEncoder.matches(rawPassword, encodedPassword));
        System.out.println("=".repeat(60));
        
        // Test with the invalid hash from database
        String invalidHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH";
        System.out.println("测试数据库中的无效哈希:");
        System.out.println("无效哈希: " + invalidHash);
        System.out.println("无效哈希长度: " + invalidHash.length());
        try {
            boolean matches = passwordEncoder.matches(rawPassword, invalidHash);
            System.out.println("验证结果: " + matches);
        } catch (Exception e) {
            System.out.println("验证失败: " + e.getMessage());
        }
        System.out.println("=".repeat(60));
    }
}
