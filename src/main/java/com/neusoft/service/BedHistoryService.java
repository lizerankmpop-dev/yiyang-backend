package com.neusoft.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.entity.BedHistory;
import com.neusoft.mapper.BedHistoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BedHistoryService extends ServiceImpl<BedHistoryMapper, BedHistory> {

    /**
     * 记录床位入住
     */
    @Transactional(rollbackFor = Exception.class)
    public void addCheckInRecord(Integer bedId, Integer customerId) {
        if (bedId == null || customerId == null) {
            log.error("入住记录失败：参数为空 bedId={}, customerId={}", bedId, customerId);
            throw new IllegalArgumentException("床位ID和客户ID不能为空");
        }

        BedHistory history = new BedHistory();
        history.setBedId(bedId);
        history.setCustomerId(customerId);
        history.setStartTime(LocalDateTime.now());
        history.setType("入住");
        this.save(history);
        log.info("✅ 床位入住记录成功：bedId={}, customerId={}", bedId, customerId);
    }

    /**
     * 记录床位退住（基础版：无备注，适配 CheckOutService 调用）
     */
    @Transactional(rollbackFor = Exception.class)
    public void addCheckOutRecord(Integer bedId, Integer customerId, LocalDateTime checkOutTime) {
        // 调用重载方法，默认备注为"正常退住"，复用核心逻辑
        addCheckOutRecord(bedId, customerId, checkOutTime, "正常退住");
    }

    /**
     * 记录床位退住/换床（完整版：带备注，修复activeRecord未定义问题）
     */
    @Transactional(rollbackFor = Exception.class)
    public void addCheckOutRecord(Integer bedId, Integer customerId, LocalDateTime checkOutTime, String remark) {
        // 1. 空值校验
        if (bedId == null || customerId == null || checkOutTime == null) {
            log.error("退住记录失败：参数为空 bedId={}, customerId={}, time={}",
                    bedId, customerId, checkOutTime);
            throw new IllegalArgumentException("退住参数不能为空");
        }

        // 2. 核心修复：先定义activeRecord变量，查询出未结束的记录
        BedHistory activeRecord = this.lambdaQuery()
                .eq(BedHistory::getBedId, bedId)
                .eq(BedHistory::getCustomerId, customerId)
                .isNull(BedHistory::getEndTime)
                .orderByDesc(BedHistory::getStartTime)
                .last("LIMIT 1")
                .one();

        // 3. 更新记录（activeRecord已定义，可正常使用）
        if (activeRecord != null) {
            activeRecord.setEndTime(checkOutTime);
            activeRecord.setRemark(remark);
            activeRecord.setType("退住");
            this.updateById(activeRecord);
            log.info("✅ 床位退住记录成功：bedId={}, customerId={}, remark={}",
                    bedId, customerId, remark);
        } else {
            log.warn("⚠️ 未找到有效入住记录：bedId={}, customerId={}",
                    bedId, customerId);
        }
    }

    /**
     * 查询床位的所有历史记录（修复方法调用，确保字段名完整）
     */
    public List<BedHistory> getHistoryByBedId(Integer bedId) {
        if (bedId == null) {
            log.error("查询失败：床位ID为空");
            return new ArrayList<>();
        }

        return this.lambdaQuery()
                .eq(BedHistory::getBedId, bedId)
                .orderByDesc(BedHistory::getStartTime)
                .list();
    }
}