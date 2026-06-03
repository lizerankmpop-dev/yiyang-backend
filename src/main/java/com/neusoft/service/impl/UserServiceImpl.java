package com.neusoft.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.entity.User;
import com.neusoft.mapper.UserMapper;
import com.neusoft.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 用户 Service 实现
 * 修复：原 UserService 直接是实现类，改为标准接口+Impl结构，支持 MyBatis-Plus 全部特性
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
