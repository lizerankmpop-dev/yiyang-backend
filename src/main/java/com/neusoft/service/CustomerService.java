package com.neusoft.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.entity.Customer;
import com.neusoft.mapper.CustomerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 客户服务实现层
 * 新增：关联床位+护理级别查询，解决前端表格空数据问题
 */
@Service
@RequiredArgsConstructor // 构造器注入（替代@Autowired，规范写法）
public class CustomerService extends ServiceImpl<CustomerMapper, Customer> {

    // 注入Mapper，用于调用自定义关联查询SQL
    private final CustomerMapper customerMapper;

    // ==================== 原有搜索功能（保留不变） ====================
    /**
     * 多条件模糊查询：姓名、手机号、标签（数据库级别过滤）
     */
    public List<Customer> searchCustomer(String name, String phone, String tag) {
        return this.lambdaQuery()
                .like(StringUtils.hasText(name), Customer::getName, name)
                .like(StringUtils.hasText(phone), Customer::getPhone, phone)
                .like(StringUtils.hasText(tag), Customer::getTags, tag)
                .list();
    }

    /**
     * 多条件模糊查询 - 分页版
     */
    public Page<Customer> searchCustomerPage(String name, String phone, String tag, int page, int size) {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<Customer>()
                .like(StringUtils.hasText(name), Customer::getName, name)
                .like(StringUtils.hasText(phone), Customer::getPhone, phone)
                .like(StringUtils.hasText(tag), Customer::getTags, tag)
                .orderByDesc(Customer::getId);
        return this.page(new Page<>(page, size), wrapper);
    }

    // ==================== 新增：关联查询（核心修复前端空数据） ====================
    /**
     * 查询客户列表（关联床位表 + 护理级别表）
     * 直接返回 bedNo（床位号）+ nurseLevel（护理级别名称），前端可直接渲染
     */
    public List<Customer> listWithBedAndNurseLevel(String name) {
        return customerMapper.selectCustomerList(name);
    }
}