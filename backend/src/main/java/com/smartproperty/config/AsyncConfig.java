package com.smartproperty.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 异步任务配置类
 * 启用Spring异步方法支持，用于异步记录用户行为日志
 * 
 * @author 毕业设计项目
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    // 使用默认的异步执行器配置
    // 如需自定义线程池，可以在此配置
}
