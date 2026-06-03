package com.neusoft.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 修复：
 * 1. 拦截范围扩展到 /nurse/** 和 /user/**，消除未鉴权的接口漏洞
 * 2. 注释掉/删除了 JwtInterceptor（已由 LoginInterceptor 完整替代）
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;

    public WebConfig(LoginInterceptor loginInterceptor) {
        this.loginInterceptor = loginInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/admin/login",
                        "/api/admin/register",
                        "/api/user/login",
                        "/api/user/register",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/api/dashboard/**" // 加上这一行，放行所有dashboard接口
                );
    }
}
