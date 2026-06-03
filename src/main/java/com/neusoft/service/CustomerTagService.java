package com.neusoft.service;

import com.neusoft.entity.Customer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 客户标签 Service
 * 修复：使用 Service 层操作替代直接 Mapper 调用，System.out.println → Slf4j
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerTagService {

    private final CustomerService customerService;

    /**
     * 给老人添加标签（多个用逗号分隔）
     */
    public void addTagToCustomer(Integer customerId, String tag) {
        Customer customer = customerService.getById(customerId);
        if (customer == null) {
            log.warn("添加标签失败：老人不存在，customerId={}", customerId);
            return;
        }

        String oldTags = customer.getTags();
        String newTags;
        if (oldTags == null || oldTags.isEmpty()) {
            newTags = tag;
        } else {
            newTags = oldTags + "," + tag;
        }

        customer.setTags(newTags);
        customerService.updateById(customer);
        log.info("已给老人[{}]添加标签：{}", customer.getName(), tag);
    }
}
