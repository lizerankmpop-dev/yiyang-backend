package com.neusoft.config;

import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ⚠️ 此类已废弃，功能已由 LoginInterceptor 完整覆盖，且从未注册到 WebConfig。
 * 保留仅作历史记录，不会被 Spring 管理，不会影响运行。
 * TODO: 可在下次代码清理时直接删除此文件。
 *
 * @deprecated 使用 {@link LoginInterceptor} 替代
 */
@Deprecated
public class JwtInterceptor implements HandlerInterceptor {

    // 此类已停用，所有方法不会被调用
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        throw new UnsupportedOperationException("JwtInterceptor 已废弃，请使用 LoginInterceptor");
    }
}
