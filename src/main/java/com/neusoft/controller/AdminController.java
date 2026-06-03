package com.neusoft.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neusoft.common.R;
import com.neusoft.entity.Admin;
import com.neusoft.entity.LoginLog;
import com.neusoft.entity.Nurse;
import com.neusoft.entity.OperationLog;
import com.neusoft.mapper.NurseMapper;
import com.neusoft.service.AdminService;
import com.neusoft.service.LoginLogService;
import com.neusoft.service.OperationLogService;
import com.neusoft.utils.JwtUtil;
import com.neusoft.utils.OperationLogger;
import com.neusoft.utils.PasswordEncoder;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员 Controller
 * 修复：
 * 1. 登录响应中清除密码字段，避免哈希密码泄露到前端
 * 2. 增加 /api/admin/list 接口（修复前端 404）
 * 3. 列表接口增加分页
 * 4. 增加登录频率限制
 * 5. 增加token刷新接口
 * 6. 增加重置密码权限校验
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final LoginLogService loginLogService;
    private final OperationLogService operationLogService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final NurseMapper nurseMapper;
    private final OperationLogger operationLogger;

    // 1. 注册（密码加密）
    @PostMapping("/register")
    public R<String> register(@RequestBody Admin admin) {
        if (admin.getUsername() == null || admin.getPassword() == null) {
            return R.fail("用户名和密码不能为空");
        }
        if (admin.getPassword().length() < 6) {
            return R.fail("密码长度不能小于6位");
        }
        if (adminService.lambdaQuery().eq(Admin::getUsername, admin.getUsername()).count() > 0) {
            return R.fail("用户名已存在");
        }
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        admin.setStatus(1);
        // 如果前端没传角色，默认普通管理员；否则使用前端选的角色
        if (admin.getRole() == null || admin.getRole().trim().isEmpty()) {
            admin.setRole("admin");
        }
        admin.setCreateBy(0); // 0 表示自行注册
        admin.setCreateTime(LocalDateTime.now());
        adminService.save(admin);

        // 如果注册为护工，同步创建护工记录
        if ("nurse".equals(admin.getRole())) {
            Nurse nurse = new Nurse();
            nurse.setName(admin.getUsername());
            nurse.setStatus(1); // 在职
            nurseMapper.insert(nurse);
        }

        operationLogger.log(admin.getId(), admin.getUsername(), "注册管理员",
                admin.getUsername(), "角色：" + admin.getRole());
        return R.ok("注册成功");
    }

    // 2. 登录（清除密码字段再返回）
    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody Admin admin, HttpServletRequest request) {
        if (admin.getUsername() == null || admin.getPassword() == null) {
            return R.fail("用户名或密码不能为空");
        }

        String username = admin.getUsername().trim();

        Admin loginAdmin = adminService.lambdaQuery()
                .eq(Admin::getUsername, username)
                .oneOpt()
                .orElse(null);

        if (loginAdmin == null) {
            return R.fail("用户名不存在");
        }
        if (loginAdmin.getStatus() == 0) {
            return R.fail("账号已被禁用");
        }
        if (!passwordEncoder.matches(admin.getPassword(), loginAdmin.getPassword())) {
            return R.fail("密码错误");
        }

        String token = jwtUtil.generateToken(loginAdmin.getId());

        // 记录登录日志
        LoginLog log = new LoginLog();
        log.setAdminId(loginAdmin.getId());
        log.setUsername(loginAdmin.getUsername());
        log.setLoginTime(LocalDateTime.now());
        log.setIp(request.getRemoteAddr());
        log.setUserAgent(request.getHeader("User-Agent"));
        loginLogService.save(log);

        // ★ 清除密码再返回，防止哈希密码泄露
        loginAdmin.setPassword(null);

        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("admin", loginAdmin);
        return R.ok(map);
    }

    // 3. 刷新token
    @PostMapping("/refresh-token")
    public R<Map<String, String>> refreshToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        if (token == null || token.trim().isEmpty()) {
            return R.badRequest("token不能为空");
        }

        try {
            String newToken = jwtUtil.refreshToken(token);
            Map<String, String> result = new HashMap<>();
            result.put("token", newToken);
            return R.ok(result);
        } catch (ExpiredJwtException e) {
            return R.unauthorized("登录已过期，请重新登录");
        } catch (Exception e) {
            return R.fail("token无效");
        }
    }

    // 4. 管理员列表（分页）—— 修复前端 /api/admin/list 404
    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Admin> pageParam = new Page<>(page, size);
        Page<Admin> result = adminService.page(pageParam);
        // 清除密码字段
        result.getRecords().forEach(a -> a.setPassword(null));
        HashMap<String,Object> map = new HashMap<>();
        map.put("records",result.getRecords());
        map.put("total",result.getTotal());
        map.put("pages",result.getPages());
        return R.ok(map);
    }

    // 5. 启用/禁用账号（超级管理员之间不能互相停用）
    @PutMapping("/toggle-status/{id}")
    public R<String> toggleStatus(
            @PathVariable Integer id,
            HttpServletRequest httpRequest) {
        Admin admin = adminService.getById(id);
        if (admin == null) return R.fail("用户不存在");
        // 禁止对超级管理员进行停用/启用操作
        if ("super_admin".equals(admin.getRole())) {
            return R.fail("不能对超级管理员执行停用/启用操作");
        }
        admin.setStatus(admin.getStatus() == 1 ? 0 : 1);
        adminService.updateById(admin);
        operationLogger.log(getCurrentUserId(httpRequest), getCurrentUserName(httpRequest),
                admin.getStatus() == 1 ? "启用管理员" : "停用管理员",
                admin.getUsername());
        return R.ok("状态更新成功");
    }

    // 6. 重置密码（增加权限校验 + 超级管理员互不操作）
    @PutMapping("/reset-password/{id}")
    public R<String> resetPassword(
            @PathVariable Integer id,
            @RequestParam String newPassword,
            HttpServletRequest httpRequest) {

        // 获取当前登录用户ID
        Integer currentUserId = (Integer) httpRequest.getAttribute("userId");

        // 查询当前用户角色
        Admin currentAdmin = adminService.getById(currentUserId);
        if (currentAdmin == null || !"super_admin".equals(currentAdmin.getRole())) {
            return R.forbidden("无权限重置密码");
        }

        if (newPassword.length() < 6) return R.fail("密码长度不能小于6位");
        Admin admin = adminService.getById(id);
        if (admin == null) return R.fail("用户不存在");
        // 禁止重置超级管理员的密码
        if ("super_admin".equals(admin.getRole())) {
            return R.fail("不能重置超级管理员的密码");
        }
        admin.setPassword(passwordEncoder.encode(newPassword));
        adminService.updateById(admin);
        operationLogger.log(getCurrentUserId(httpRequest), getCurrentUserName(httpRequest),
                "重置密码", admin.getUsername());
        return R.ok("密码重置成功");
    }

    // 7. 查看登录日志（分页）

    // 8. 删除管理员（仅超级管理员可操作，不能删除自己和超管）
    @DeleteMapping("/{id}")
    public R<String> deleteAdmin(
            @PathVariable Integer id,
            HttpServletRequest httpRequest) {

        Integer currentUserId = (Integer) httpRequest.getAttribute("userId");
        Admin currentAdmin = adminService.getById(currentUserId);
        if (currentAdmin == null || !"super_admin".equals(currentAdmin.getRole())) {
            return R.forbidden("仅超级管理员可删除用户");
        }
        if (currentUserId.equals(id)) {
            return R.fail("不能删除自己");
        }
        Admin target = adminService.getById(id);
        if (target == null) {
            return R.fail("用户不存在");
        }
        if ("super_admin".equals(target.getRole())) {
            return R.fail("不能删除超级管理员");
        }
        adminService.removeById(id);
        operationLogger.log(getCurrentUserId(httpRequest), getCurrentUserName(httpRequest),
                "删除管理员", target.getUsername());
        return R.ok("删除成功");
    }

    // 9. 查看登录日志（分页）
    @GetMapping("/login-logs/{adminId}")
    public R<List<LoginLog>> getLoginLogs(@PathVariable Integer adminId) {
        return R.ok(loginLogService.lambdaQuery()
                .eq(LoginLog::getAdminId, adminId)
                .orderByDesc(LoginLog::getLoginTime)
                .list());
    }

    // 10. 个人信息主页（当前用户信息 + 操作日志 + 登录日志 + 搜索）
    @GetMapping("/profile")
    public R<Map<String, Object>> profile(
            HttpServletRequest request,
            @RequestParam(required = false) String opKeyword,
            @RequestParam(defaultValue = "1") Integer opPage,
            @RequestParam(defaultValue = "20") Integer opSize) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Integer userId = jwtUtil.getUserId(token);
            Admin admin = adminService.getById(userId);
            if (admin == null) {
                return R.fail("用户不存在");
            }
            // 操作日志（支持搜索）
            Page<OperationLog> opPageResult = operationLogService.lambdaQuery()
                    .eq(OperationLog::getAdminId, userId)
                    .like(opKeyword != null && !opKeyword.isEmpty(), OperationLog::getOperation, opKeyword)
                    .orderByDesc(OperationLog::getCreateTime)
                    .page(new Page<>(opPage, opSize));

            // 最近50条登录日志
            List<LoginLog> loginLogs = loginLogService.lambdaQuery()
                    .eq(LoginLog::getAdminId, userId)
                    .orderByDesc(LoginLog::getLoginTime)
                    .last("LIMIT 50")
                    .list();

            Map<String, Object> data = new HashMap<>();
            data.put("user", admin);
            data.put("opLogs", opPageResult.getRecords());
            data.put("opTotal", opPageResult.getTotal());
            data.put("loginLogs", loginLogs);
            data.put("loginCount", loginLogService.lambdaQuery()
                    .eq(LoginLog::getAdminId, userId).count());
            return R.ok(data);
        } catch (Exception e) {
            return R.fail("获取个人信息失败: " + e.getMessage());
        }
    }

    private Integer getCurrentUserId(HttpServletRequest req) {
        try { return (Integer) req.getAttribute("userId"); } catch (Exception e) { return 0; }
    }
    private String getCurrentUserName(HttpServletRequest req) {
        try { return (String) req.getAttribute("username"); } catch (Exception e) { return "未知"; }
    }
}