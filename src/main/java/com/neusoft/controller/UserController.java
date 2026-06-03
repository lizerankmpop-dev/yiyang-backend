package com.neusoft.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neusoft.common.R;
import com.neusoft.entity.User;
import com.neusoft.service.UserService;
import com.neusoft.utils.JwtUtil;
import com.neusoft.utils.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户 Controller
 * 提供：注册、登录、列表查询、编辑、删除
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    /**
     * 用户注册
     * POST /api/user/register
     * 请求体：{ "username": "xxx", "password": "xxx", "role": "nurse" }
     */
    @PostMapping("/register")
    public R<String> register(@RequestBody User user) {
        try {
            if (user.getUsername() == null || user.getPassword() == null) {
                return R.badRequest("用户名和密码不能为空");
            }
            if (user.getUsername().trim().isEmpty()) {
                return R.badRequest("用户名不能为空");
            }
            if (user.getPassword().length() < 6) {
                return R.badRequest("密码长度不能小于6位");
            }
            // 检查用户名是否已存在
            long count = userService.lambdaQuery()
                    .eq(User::getUsername, user.getUsername().trim())
                    .count();
            if (count > 0) {
                return R.fail("用户名已存在");
            }
            // 设置默认值
            user.setUsername(user.getUsername().trim());
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            if (user.getRole() == null || user.getRole().trim().isEmpty()) {
                user.setRole("nurse");
            }
            user.setStatus(1);
            // 不设置 ID，依赖数据库自增
            userService.save(user);
            return R.ok("注册成功");
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("注册失败：" + e.getMessage());
        }
    }

    /**
     * 用户登录
     * POST /api/user/login
     * 请求体：{ "username": "xxx", "password": "xxx" }
     */
    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody User user) {
        if (user.getUsername() == null || user.getPassword() == null) {
            return R.badRequest("用户名或密码不能为空");
        }

        User loginUser = userService.lambdaQuery()
                .eq(User::getUsername, user.getUsername().trim())
                .oneOpt()
                .orElse(null);

        if (loginUser == null) {
            return R.fail("用户名不存在");
        }
        if (loginUser.getStatus() == 0) {
            return R.fail("账号已被禁用");
        }
        if (!passwordEncoder.matches(user.getPassword(), loginUser.getPassword())) {
            return R.fail("密码错误");
        }

        String token = jwtUtil.generateToken(loginUser.getId());

        // 清除密码再返回，防止哈希密码泄露
        loginUser.setPassword(null);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", loginUser);
        return R.ok(result);
    }

    /**
     * 用户列表（分页）
     * GET /api/user/list?page=1&size=10
     */
    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String role) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        // 护士管理页面只显示护士（role=nurse），如果指定了role则按role过滤
        if (role != null && !role.trim().isEmpty()) {
            queryWrapper.eq(User::getRole, role.trim());
        }
        Page<User> result = userService.page(pageParam, queryWrapper);
        // 清除密码字段
        result.getRecords().forEach(u -> u.setPassword(null));
        HashMap<String,Object> map = new HashMap<>();
        map.put("records", result.getRecords());
        map.put("total", result.getTotal());
        return R.ok(map);
    }

    /**
     * 编辑用户
     * POST /api/user/update
     */
    @PostMapping("/update")
    public R<String> update(@RequestBody User user) {
        if (user.getId() == null || user.getId() <= 0) {
            return R.fail("ID无效");
        }

        User existingUser = userService.getById(user.getId());
        if (existingUser == null) {
            return R.fail("用户不存在");
        }

        // 用户名不能修改
        user.setUsername(existingUser.getUsername());

        // 密码为空时不更新密码
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            user.setPassword(existingUser.getPassword());
        } else {
            if (user.getPassword().length() < 6) {
                return R.badRequest("密码长度不能小于6位");
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        userService.updateById(user);
        return R.ok("用户信息修改成功");
    }

    /**
     * 删除用户
     * DELETE /api/user/delete
     */
    @DeleteMapping("/delete")
    public R<String> delete(@RequestParam Integer id) {
        if (id == null || id <= 0) {
            return R.fail("ID无效");
        }
        // 禁止删除超级管理员
        if (id == 1) {
            return R.forbidden("不能删除超级管理员");
        }
        if (userService.getById(id) == null) {
            return R.fail("用户不存在");
        }
        userService.removeById(id);
        return R.ok("删除成功");
    }
}