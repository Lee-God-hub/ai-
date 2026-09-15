package com.smartproperty;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 智能房产交易平台主启动类
 * 项目名称：基于AI的智能房产交易平台
 * 技术栈：SpringBoot + Vue3 + MySQL
 * 
 * @author 毕业设计项目
 * @version 1.0.0
 */
@SpringBootApplication
@MapperScan("com.smartproperty.mapper")
public class SmartPropertyApplication {
    
    /**
     * 主启动方法
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SmartPropertyApplication.class, args);
        System.out.println("==========================================");
        System.out.println("智能房产交易平台启动成功！");
        System.out.println("后端服务地址：http://localhost:8080");
        System.out.println("API文档地址：http://localhost:8080/swagger-ui.html");
        System.out.println("==========================================");
    }
}