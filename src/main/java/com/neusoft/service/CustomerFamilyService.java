package com.neusoft.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.entity.CustomerFamily;
import com.neusoft.mapper.CustomerFamilyMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 家属信息 Service
 * 修复：getFamilyByCustomerId 从 selectList(null) 全表扫描改为按 customerId 条件查询
 */
@Slf4j
@Service
public class CustomerFamilyService extends ServiceImpl<CustomerFamilyMapper, CustomerFamily> {

    /**
     * 添加家属信息
     */
    public void addFamily(CustomerFamily family) {
        this.save(family);
        log.info("家属信息添加成功：{}", family.getName());
    }

    /**
     * 修改家属信息
     */
    public void updateFamily(CustomerFamily family) {
        this.updateById(family);
        log.info("家属信息修改成功：{}", family.getName());
    }

    /**
     * 根据老人ID查询家属列表
     */
    public List<CustomerFamily> getFamilyByCustomerId(Integer customerId) {
        return this.lambdaQuery()
                .eq(CustomerFamily::getCustomerId, customerId)
                .list();
    }
}
