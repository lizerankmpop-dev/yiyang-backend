package com.neusoft.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.entity.Admin;
import com.neusoft.mapper.AdminMapper;
import com.neusoft.service.AdminService;
import org.springframework.stereotype.Service;

@Service // ✅ 必须加：让 Spring 扫描并管理这个 Bean
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {
}