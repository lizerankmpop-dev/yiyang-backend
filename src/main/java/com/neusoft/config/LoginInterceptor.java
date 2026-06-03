package com.neusoft.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.common.R;
import com.neusoft.entity.Admin;
import com.neusoft.service.AdminService;
import com.neusoft.utils.JwtUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final AdminService adminService;

    public LoginInterceptor(JwtUtil jwtUtil, ObjectMapper objectMapper, AdminService adminService) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
        this.adminService = adminService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = resolveToken(request);
        if (token == null) {
            writeUnauthorized(response, "请先登录");
            return false;
        }

        if (!jwtUtil.validateToken(token)) {
            writeUnauthorized(response, "登录状态已失效，请重新登录");
            return false;
        }

        Integer userId = jwtUtil.getUserId(token);
        request.setAttribute("userId", userId);

        // 角色权限校验
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        // 查询当前用户角色
        Admin admin = adminService.getById(userId);
        boolean isSuperAdmin = admin != null && "super_admin".equals(admin.getRole());

        // 超级管理员拥有所有权限
        if (isSuperAdmin) {
            return true;
        }

        // 普通管理员权限限制：
        // 1. 系统管理相关接口完全禁止访问（管理员管理、角色管理、菜单管理）
        if (requestURI.startsWith("/api/admin/")) {
            writeForbidden(response, "无权限访问管理员管理功能");
            return false;
        }
        if (requestURI.startsWith("/api/role/")) {
            writeForbidden(response, "无权限访问角色管理功能");
            return false;
        }
        if (requestURI.startsWith("/api/menu/")) {
            writeForbidden(response, "无权限访问菜单管理功能");
            return false;
        }

        // 2. 客户删除权限限制
        if (requestURI.startsWith("/api/customer/") && method.equals("DELETE")) {
            writeForbidden(response, "无权限删除客户");
            return false;
        }

        // 3. 护理记录删除权限限制
        if (requestURI.startsWith("/api/nursing-record/delete")) {
            writeForbidden(response, "无权限删除护理记录");
            return false;
        }

        // 4. 用户管理：允许查看（GET）和注册（POST /register），禁止其他修改操作
        if (requestURI.startsWith("/api/user/") && !method.equals("GET") && !requestURI.equals("/api/user/register")) {
            writeForbidden(response, "无权限操作用户数据");
            return false;
        }

        return true;
    }

    private String resolveToken(HttpServletRequest request) {
        // 优先处理标准 Bearer Token（前端现在发的就是这个）
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7).trim();
        }
        // 兼容旧的 token 头（只作为备选）
        String token = request.getHeader("token");
        if (token != null && !token.trim().isEmpty()) {
            return token.trim();
        }
        return null;
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(objectMapper.writeValueAsString(R.unauthorized(message)));
    }

    private void writeForbidden(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(objectMapper.writeValueAsString(R.forbidden(message)));
    }
}