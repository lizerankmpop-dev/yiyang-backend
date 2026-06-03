package com.neusoft.controller;

import com.neusoft.common.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neusoft.entity.Customer;
import com.neusoft.entity.Outward;
import com.neusoft.service.CustomerService;
import com.neusoft.service.OutwardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/outward")
@RequiredArgsConstructor
public class OutwardController {
    private final OutwardService outwardService;
    private final CustomerService customerService;

    @GetMapping("/list")
    public R<Page<Outward>> list(@RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(defaultValue = "10") Integer size,
                                  @RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) Integer status) {
        Page<Outward> result = outwardService.lambdaQuery()
            .like(keyword != null && !keyword.isEmpty(), Outward::getCustomerName, keyword)
            .eq(status != null, Outward::getStatus, status)
            .orderByDesc(Outward::getCreateTime)
            .page(new Page<>(page, size));

        // 填充客户姓名
        List<Outward> records = result.getRecords();
        if (!records.isEmpty()) {
            List<Integer> customerIds = records.stream()
                .map(Outward::getCustomerId)
                .distinct()
                .collect(Collectors.toList());
            Map<Integer, String> nameMap = customerService.listByIds(customerIds).stream()
                .collect(Collectors.toMap(Customer::getId, Customer::getName));
            records.forEach(r -> {
                if (r.getCustomerName() == null || r.getCustomerName().isEmpty()) {
                    r.setCustomerName(nameMap.getOrDefault(r.getCustomerId(), ""));
                }
            });
        }
        return R.ok(result);
    }

    // 外出超时列表：status=1 且预计返回日期已过且未实际返回
    @GetMapping("/overdue-list")
    public R<List<Outward>> overdueList() {
        java.time.LocalDate today = java.time.LocalDate.now();
        List<Outward> records = outwardService.lambdaQuery()
            .eq(Outward::getStatus, 1)
            .isNotNull(Outward::getExpectedReturnDate)
            .lt(Outward::getExpectedReturnDate, today)
            .isNull(Outward::getActualReturnDate)
            .orderByDesc(Outward::getCreateTime)
            .list();

        // 填充客户姓名
        if (!records.isEmpty()) {
            List<Integer> customerIds = records.stream()
                .map(Outward::getCustomerId)
                .distinct()
                .collect(Collectors.toList());
            Map<Integer, String> nameMap = customerService.listByIds(customerIds).stream()
                .collect(Collectors.toMap(Customer::getId, Customer::getName));
            records.forEach(r -> {
                if (r.getCustomerName() == null || r.getCustomerName().isEmpty()) {
                    r.setCustomerName(nameMap.getOrDefault(r.getCustomerId(), ""));
                }
            });
        }
        return R.ok(records);
    }

    @GetMapping("/{id}")
    public R<Outward> getById(@PathVariable Integer id) {
        return R.ok(outwardService.getById(id));
    }

    @PostMapping("/apply")
    public R<String> save(@RequestBody Outward entity) {
        // 验证：不能申请过去日期的外出
        if (entity.getGoOutDate() != null && entity.getGoOutDate().isBefore(LocalDate.now())) {
            return R.badRequest("不能申请过去日期的外出");
        }
        // 验证：预计返回日期不能早于外出日期
        if (entity.getExpectedReturnDate() != null && entity.getGoOutDate() != null
                && entity.getExpectedReturnDate().isBefore(entity.getGoOutDate())) {
            return R.badRequest("预计返回日期不能早于外出日期");
        }
        outwardService.save(entity);
        return R.ok("保存成功");
    }

    @PutMapping("/audit/{id}")
    public R<String> audit(@PathVariable Integer id, @RequestBody Outward entity) {
        Outward record = outwardService.getById(id);
        if (record == null) {
            return R.notFound("外出记录不存在");
        }
        entity.setId(id);
        outwardService.updateById(entity);

        // 审批通过时同步客户外出状态
        if (entity.getStatus() != null && entity.getStatus() == 1 && record.getCustomerId() != null) {
            Customer customer = customerService.getById(record.getCustomerId());
            if (customer != null) {
                customer.setIsOuting(1);
                if (record.getGoOutDate() != null) {
                    customer.setGoOutStart(record.getGoOutDate().atStartOfDay());
                }
                if (record.getExpectedReturnDate() != null) {
                    customer.setGoOutEnd(record.getExpectedReturnDate().atStartOfDay());
                }
                customerService.updateById(customer);
            }
        }
        // 审批驳回时如果客户正在外出，重置状态
        if (entity.getStatus() != null && entity.getStatus() == 2 && record.getCustomerId() != null) {
            Customer customer = customerService.getById(record.getCustomerId());
            if (customer != null && customer.getIsOuting() != null && customer.getIsOuting() == 1) {
                customer.setIsOuting(0);
                customer.setGoOutStart(null);
                customer.setGoOutEnd(null);
                customerService.updateById(customer);
            }
        }
        return R.ok("更新成功");
    }

    @PutMapping("/return/{id}")
    public R<String> returnBack(@PathVariable Integer id, @RequestBody Outward entity) {
        Outward record = outwardService.getById(id);
        if (record == null) {
            return R.notFound("外出记录不存在");
        }
        // 验证：实际返回日期不能早于外出日期
        if (entity.getActualReturnDate() != null && record.getGoOutDate() != null
                && entity.getActualReturnDate().isBefore(record.getGoOutDate())) {
            return R.badRequest("实际返回日期不能早于外出日期");
        }
        entity.setId(id);
        entity.setStatus(3); // 已返回
        outwardService.updateById(entity);

        // 同步客户外出状态为已返回
        if (record.getCustomerId() != null) {
            Customer customer = customerService.getById(record.getCustomerId());
            if (customer != null) {
                customer.setIsOuting(0);
                if (entity.getActualReturnDate() != null) {
                    customer.setGoOutEnd(entity.getActualReturnDate().atStartOfDay());
                }
                customerService.updateById(customer);
            }
        }
        return R.ok("回院登记成功");
    }

    @PutMapping("/{id}")
    public R<String> updateOutward(@PathVariable Integer id, @RequestBody Outward entity) {
        // 验证：不能改成过去日期的外出
        if (entity.getGoOutDate() != null && entity.getGoOutDate().isBefore(LocalDate.now())) {
            return R.badRequest("外出日期不能是过去的日期");
        }
        // 验证：预计返回日期不能早于外出日期
        if (entity.getExpectedReturnDate() != null && entity.getGoOutDate() != null
                && entity.getExpectedReturnDate().isBefore(entity.getGoOutDate())) {
            return R.badRequest("预计返回日期不能早于外出日期");
        }
        entity.setId(id);
        outwardService.updateById(entity);
        return R.ok("更新成功");
    }

    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable Integer id) {
        outwardService.removeById(id);
        return R.ok("删除成功");
    }
}
