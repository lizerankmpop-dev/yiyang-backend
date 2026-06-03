package com.neusoft.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neusoft.entity.User;

/**
 * 用户 Service 接口（规范化改造）
 * 修复：原 UserService 直接是实现类，改为接口+Impl结构，支持 MyBatis-Plus 分页
 */
public interface UserService extends IService<User> {
}
