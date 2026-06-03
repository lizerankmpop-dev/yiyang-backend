package com.neusoft.service;

import com.neusoft.entity.Customer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 外出提醒 Service
 * 修复：
 * 1. selectList(null) 全表扫描 → lambdaQuery 条件查询（仅查外出中的老人）
 * 2. java.util.Date → java.time.LocalDateTime
 * 3. System.out.println → Slf4j log
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutReminderService {

    private final CustomerService customerService;

    /**
     * 检查外出到期提醒
     */
    public void checkOutExpire() {
        LocalDateTime now = LocalDateTime.now();

        // 只查询正在外出的老人（条件查询，非全表扫描）
        List<Customer> outingList = customerService.lambdaQuery()
                .eq(Customer::getIsOuting, 1)
                .isNotNull(Customer::getGoOutEnd)
                .list();

        for (Customer c : outingList) {
            if (now.isAfter(c.getGoOutEnd())) {
                log.warn("外出超时提醒：老人[{}]外出已到期（预计返回：{}），请联系家属！",
                        c.getName(), c.getGoOutEnd());
            }
        }
    }
}
