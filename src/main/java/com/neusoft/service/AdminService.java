package com.neusoft.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neusoft.entity.Admin;

// ✅ 必须继承 IService<Admin>，才能使用 lambdaQuery() 方法
public interface AdminService extends IService<Admin> {
}