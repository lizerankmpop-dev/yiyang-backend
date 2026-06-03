package com.neusoft.controller;

import com.neusoft.common.R;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neusoft.entity.Backdown;
import com.neusoft.entity.Bed;
import com.neusoft.entity.Customer;
import com.neusoft.service.BackdownService;
import com.neusoft.mapper.BedMapper;
import com.neusoft.mapper.CustomerMapper;
import com.neusoft.utils.OperationLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/backdown")
@RequiredArgsConstructor
public class BackdownController {
    private final BackdownService backdownService;
    private final CustomerMapper customerMapper;
    private final BedMapper bedMapper;
    private final OperationLogger operationLogger;
    private final HttpServletRequest httpRequest;

    /**
     * 分页查询退住记录（含客户姓名、床位等信息）
     */
    @GetMapping("/list")
    public R<Page<Backdown>> list(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer size,
                                   @RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) Integer status) {
        return R.ok(backdownService.lambdaQuery()
            .like(keyword != null && !keyword.isEmpty(), Backdown::getCustomerName, keyword)
            .eq(status != null, Backdown::getStatus, status)
            .orderByDesc(Backdown::getCreateTime)
            .page(new Page<>(page, size)));
    }

    @GetMapping("/{id}")
    public R<Backdown> getById(@PathVariable Integer id) {
        return R.ok(backdownService.getById(id));
    }

    /**
     * 提交退住申请（只保存申请记录，不立即释放床位）
     * 床位释放在审核通过时进行
     */
    @PostMapping("/apply")
    @Transactional(rollbackFor = Exception.class)
    public R<String> apply(@RequestBody Backdown entity) {
        if (entity.getCustomerId() == null) {
            return R.badRequest("请选择要退住的客户");
        }

        // 查询客户信息
        Customer customer = customerMapper.selectById(entity.getCustomerId());
        if (customer == null) {
            return R.badRequest("客户不存在");
        }
        if (customer.getIsCheckIn() == null || customer.getIsCheckIn() != 1) {
            return R.badRequest("该客户当前未在住，无需办理退住");
        }

        // 检查是否已有待审核的退住申请
        long pendingCount = backdownService.lambdaQuery()
                .eq(Backdown::getCustomerId, entity.getCustomerId())
                .eq(Backdown::getStatus, 0)
                .count();
        if (pendingCount > 0) {
            return R.badRequest("该客户已有待审核的退住申请，请勿重复提交");
        }

        // 补全客户信息
        entity.setCustomerName(customer.getName());
        entity.setApplyDate(LocalDate.now());
        entity.setStatus(0); // 待审核

        backdownService.save(entity);
        log.info("【退住管理】客户 {} 提交退住申请，日期={}", customer.getName(), entity.getCheckOutDate());

        operationLogger.log(getCurrentUserId(), getCurrentUserName(),
                "提交退住申请", "客户-" + customer.getName(),
                "退住原因：" + (entity.getReason() != null ? entity.getReason() : "未填写"));

        return R.ok("退住申请提交成功，请等待审核");
    }

    /**
     * 审核退住申请
     * 审核通过（status=1）时才真正释放床位并更新客户状态
     * 审核驳回（status=2）时不做任何状态变更
     */
    @PutMapping("/audit/{id}")
    @Transactional(rollbackFor = Exception.class)
    public R<String> audit(@PathVariable Integer id, @RequestBody Backdown entity) {
        Backdown backdown = backdownService.getById(id);
        if (backdown == null) {
            return R.notFound("退住申请记录不存在");
        }
        if (backdown.getStatus() != null && backdown.getStatus() != 0) {
            return R.badRequest("该申请已审核完成，不能重复审核");
        }

        Integer newStatus = entity.getStatus();
        if (newStatus == null) {
            return R.badRequest("请指定审核结果（1=通过，2=驳回）");
        }

        // 更新退住记录审核状态
        backdown.setStatus(newStatus);
        backdown.setAuditorId(getCurrentUserId());
        backdown.setAuditorName(getCurrentUserName());
        backdown.setAuditTime(LocalDateTime.now());
        if (entity.getAuditRemark() != null) {
            backdown.setAuditRemark(entity.getAuditRemark());
        }
        backdownService.updateById(backdown);

        if (newStatus == 1) {
            // ===== 审核通过：释放床位 + 更新客户状态 =====
            Customer customer = customerMapper.selectById(backdown.getCustomerId());
            if (customer != null) {
                Bed occupiedBed = null;

                // 1. 释放床位
                if (customer.getBedId() != null) {
                    occupiedBed = bedMapper.selectById(customer.getBedId());
                    if (occupiedBed != null) {
                        occupiedBed.setStatus(0); // 空闲
                        bedMapper.updateById(occupiedBed);
                        log.info("【退住管理】床位 {} 已释放为空闲", occupiedBed.getBedNo());
                    }
                }

                // 2. 更新客户状态：已退住 + 解绑床位（使用 Wrapper 强制更新 null 字段）
                LambdaUpdateWrapper<Customer> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(Customer::getId, customer.getId())
                        .set(Customer::getIsCheckIn, 0)
                        .set(Customer::getBedId, null)
                        .set(Customer::getCheckOutTime, LocalDateTime.now());
                customerMapper.update(null, updateWrapper);
                log.info("【退住管理】审核通过，客户 {} 已退住，床位已释放", customer.getName());

                operationLogger.log(getCurrentUserId(), getCurrentUserName(),
                        "审核退住通过", "客户-" + customer.getName(),
                        "释放床位：" + (occupiedBed != null ? occupiedBed.getBedNo() : "无床位"));
            }
        } else if (newStatus == 2) {
            // ===== 审核驳回：不做任何床位/客户状态操作 =====
            Customer customer = customerMapper.selectById(backdown.getCustomerId());
            String customerName = customer != null ? customer.getName() : "未知";
            log.info("【退住管理】审核驳回，客户 {} 退住申请已拒绝", customerName);
            operationLogger.log(getCurrentUserId(), getCurrentUserName(),
                    "审核退住驳回", "客户-" + customerName,
                    "驳回原因：" + (backdown.getAuditRemark() != null ? backdown.getAuditRemark() : "未填写"));
        }

        return R.ok(newStatus == 1 ? "审核通过，床位已释放" : "申请已驳回");
    }

    /**
     * 撤销退住申请（仅限待审核状态）
     */
    @PutMapping("/cancel/{id}")
    @Transactional(rollbackFor = Exception.class)
    public R<String> cancel(@PathVariable Integer id) {
        Backdown backdown = backdownService.getById(id);
        if (backdown == null) {
            return R.notFound("退住申请记录不存在");
        }
        if (backdown.getStatus() != null && backdown.getStatus() != 0) {
            return R.badRequest("只有待审核的申请可以撤销");
        }
        backdown.setStatus(3); // 3=已撤销
        backdownService.updateById(backdown);
        log.info("【退住管理】退住申请 {} 已撤销", id);
        return R.ok("申请已撤销");
    }

    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable Integer id) {
        backdownService.removeById(id);
        return R.ok("删除成功");
    }

    private Integer getCurrentUserId() {
        try { return (Integer) httpRequest.getAttribute("userId"); } catch (Exception e) { return 0; }
    }
    private String getCurrentUserName() {
        try { return (String) httpRequest.getAttribute("username"); } catch (Exception e) { return "未知"; }
    }
}
