package com.neusoft.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.neusoft.entity.Bed;
import com.neusoft.entity.Customer;
import com.neusoft.mapper.BedMapper;
import com.neusoft.mapper.CustomerMapper;
import com.neusoft.service.BedHistoryService;
import com.neusoft.service.BedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckOutService {

    private final BedService bedService;
    private final BedHistoryService bedHistoryService;
    private final CustomerMapper customerMapper;
    private final BedMapper bedMapper;

    public String checkOut(Integer customerId) {
        // 1. 查询老人信息
        Customer customer = customerMapper.selectById(customerId);
        if (customer == null) {
            log.error("退住失败：未找到ID为{}的老人", customerId);
            return "退住失败：未找到对应老人信息";
        }

        // 2. 获取床位信息
        Integer bedId = customer.getBedId();
        Bed bed = null;
        if (bedId != null) {
            bed = bedMapper.selectById(bedId);
        }

        // 3. 更新床位状态为空闲
        if (bed != null) {
            bed.setStatus(0);
            bedService.updateById(bed);
        }

        // 4. 记录床位退住历史（核心修复：补全第3个参数）
        if (bedId != null) {
            // 传入退住时间作为第3个参数，匹配方法定义
            bedHistoryService.addCheckOutRecord(bedId, customerId, LocalDateTime.now());
        }

        // 5. 更新客户状态：解绑床位 + 标记退住（Wrapper强制更新null字段）
        LambdaUpdateWrapper<Customer> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Customer::getId, customerId)
            .set(Customer::getIsCheckIn, 0)
            .set(Customer::getBedId, null)
            .set(Customer::getCheckOutTime, LocalDateTime.now());
        customerMapper.update(null, updateWrapper);

        String bedNum = (bed != null && bed.getBedNo() != null) ? bed.getBedNo() : "无";
        log.info("退住成功：老人={}, 床位号={}", customer.getName(), bedNum);

        // 6. 退住结算单
        return "===== 退住结算单 =====\n" +
                "老人姓名：" + customer.getName() + "\n" +
                "退住时间：" + LocalDateTime.now() + "\n" +
                "床位号：" + bedNum + "\n" +
                "状态：退住完成，床位已释放";
    }
}