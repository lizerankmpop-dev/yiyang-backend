package com.neusoft.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局CORS配置（唯一入口，优先级最高）
 * 核心特性：
 * 1. 环境隔离：dev环境宽松配置，prod环境严格限制
 * 2. 安全增强：避免"*"与allowCredentials=true共存的安全隐患
 * 3. 完整方法支持：覆盖GET/POST/PUT/DELETE/OPTIONS/PATCH
 * 4. 自定义头暴露：支持token/Authorization等认证头
 * 5. 预检缓存优化：maxAge=3600减少OPTIONS请求
 */
@Configuration
public class CorsConfig {

    /**
     * 开发环境CORS配置（宽松模式）
     * 激活条件：spring.profiles.active=dev
     */
    @Configuration
    @Profile("dev")
    public static class DevCorsConfig implements WebMvcConfigurer {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/**")
                    .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*","http://119.91.61.93:8081")                  .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                    .allowedHeaders("*")
                    .exposedHeaders("token", "Authorization", "Content-Disposition")  // 暴露常用自定义头
                    .allowCredentials(true)
                    .maxAge(3600);
        }
    }

    /**
     * 生产环境CORS配置（严格模式）
     * 激活条件：spring.profiles.active=prod
     * 注意：必须在application-prod.yml中配置允许的域名列表
     */
    @Configuration
    @Profile("prod")
    public static class ProdCorsConfig implements WebMvcConfigurer {

        // 从配置文件读取允许的前端域名（多个用逗号分隔）
        @Value("${app.cors.allowed-origins}")
        private String[] allowedOrigins;

        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/**")
                    .allowedOriginPatterns(allowedOrigins)  // 精确指定前端域名，禁止使用"*"
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                    .allowedHeaders("Authorization", "Content-Type", "X-Requested-With")  // 只允许必要头
                    .exposedHeaders("token", "Authorization")
                    .allowCredentials(true)
                    .maxAge(3600);
        }
    }

    /**
     * 默认配置（安全兜底，避免误配）
     * 激活条件：未指定profile时使用
     */
    @Configuration
    @Profile("!dev & !prod")
    public static class DefaultCorsConfig implements WebMvcConfigurer {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/**")
                    .allowedOriginPatterns("https://your-default-domain.com")  // 替换为实际默认域名
                    .allowedMethods("GET", "POST")  // 仅开放基础方法
                    .allowedHeaders("*")
                    .allowCredentials(false)  // 默认关闭凭证支持
                    .maxAge(3600);
        }
    }
}