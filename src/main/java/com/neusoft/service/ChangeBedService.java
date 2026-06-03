package com.neusoft.service;

import com.neusoft.entity.Bed;
import com.neusoft.entity.Customer;
import com.neusoft.mapper.CustomerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChangeBedService {

    // 注入依赖服务
    private final BedService bedService;
    private final BedHistoryService bedHistoryService;
    private final CustomerMapper customerMapper;

    /**
     * 为老人更换床位（核心业务方法）
     * @param customer 待换床老人
     * @param newBed 目标新床位
     * @return true=换床成功 false=失败
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean changeCustomerBed(Customer customer, Bed newBed) {
        // 1. 基础空值校验（防非法调用）
        if (customer == null || newBed == null) {
            log.error("换床失败：参数为空 customer={}, newBed={}", customer, newBed);
            return false;
        }

        // 2. 检查新床位状态（必须为空闲状态 0）
        if (newBed.getStatus() != 0) {
            // 核心修复：getBedNumber → getBedNo，匹配Bed实体字段
            log.warn("换床失败：新床位 {} 已被占用", newBed.getBedNo());
            return false;
        }

        // 3. 获取老人当前床位ID
        Integer oldBedId = customer.getBedId();

        // 4. 处理旧床位：更新为空闲状态 + 记录退住历史
        if (oldBedId != null) {
            Bed oldBed = bedService.getById(oldBedId);
            if (oldBed != null) {
                oldBed.setStatus(0); // 标记为空闲
                bedService.updateById(oldBed);
                // 记录换床退住历史（适配BedHistoryService接口）
                bedHistoryService.addCheckOutRecord(oldBedId, customer.getId(), LocalDateTime.now(), "换床退住");
            }
        }

        // 5. 处理新床位：更新为占用状态 + 记录入住历史
        newBed.setStatus(1); // 标记为已占用
        bedService.updateById(newBed);
        bedHistoryService.addCheckInRecord(newBed.getId(), customer.getId());

        // 6. 更新老人床位信息（确保数据一致性）
        customer.setBedId(newBed.getId());
        customerMapper.updateById(customer);

        // 7. 业务日志（记录关键信息）
        log.info("换床成功：老人={}, 原床位={}, 新床位={}",
                customer.getName(),
                oldBedId != null ? oldBedId : "无",
                newBed.getBedNo());

        return true;
    }
}