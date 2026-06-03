package com.neusoft.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.neusoft.common.R;
import com.neusoft.entity.Customer;
import com.neusoft.entity.HealthRecord;
import com.neusoft.mapper.CustomerMapper;
import com.neusoft.service.HealthRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/health-record")
@RequiredArgsConstructor
public class HealthRecordController {

    private final HealthRecordService healthRecordService;
    private final CustomerMapper customerMapper;

    @GetMapping("/list")
    public R list(@RequestParam(required = false) Integer customerId) {
        if (customerId != null) {
            return R.ok(healthRecordService.lambdaQuery()
                    .eq(HealthRecord::getCustomerId, customerId)
                    .eq(HealthRecord::getIsDeleted, 0)
                    .orderByDesc(HealthRecord::getCreateTime)
                    .list());
        }
        return R.ok(healthRecordService.lambdaQuery()
                .eq(HealthRecord::getIsDeleted, 0)
                .orderByDesc(HealthRecord::getCreateTime)
                .list());
    }

    @PostMapping("/add")
    public R add(@RequestBody HealthRecord healthRecord) {
        if (healthRecord.getCustomerId() == null) {
            return R.fail("请选择客户");
        }
        if (healthRecord.getMeasureTime() == null) {
            healthRecord.setMeasureTime(LocalDateTime.now());
        }
        healthRecord.setCreateTime(LocalDateTime.now());
        healthRecord.setUpdateTime(LocalDateTime.now());
        healthRecord.setIsDeleted(0);

        boolean success = healthRecordService.save(healthRecord);
        return success ? R.ok("新增成功") : R.fail("新增失败");
    }

    @GetMapping("/{id}")
    public R getById(@PathVariable Long id) {
        HealthRecord record = healthRecordService.getById(id);
        return record != null ? R.ok(record) : R.fail("记录不存在");
    }

    @GetMapping("/customer/{customerId}")
    public R getByCustomerId(@PathVariable Long customerId) {
        List<HealthRecord> records = healthRecordService.getByCustomerId(customerId);
        return R.ok(records);
    }

    @PutMapping
    public R update(@RequestBody HealthRecord healthRecord) {
        healthRecord.setUpdateTime(LocalDateTime.now());

        boolean success = healthRecordService.updateById(healthRecord);
        return success ? R.ok("更新成功") : R.fail("更新失败");
    }

    @DeleteMapping("/{id}")
    public R delete(@PathVariable Long id) {
        LambdaUpdateWrapper<HealthRecord> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(HealthRecord::getIsDeleted, 1);
        updateWrapper.set(HealthRecord::getUpdateTime, LocalDateTime.now());
        updateWrapper.eq(HealthRecord::getId, id);

        boolean success = healthRecordService.update(updateWrapper);
        return success ? R.ok("删除成功") : R.fail("删除失败");
    }

    /**
     * 获取健康异常的客户床位信息（供楼层总览使用）
     * 异常标准：
     *   收缩压 < 90 或 > 139 mmHg
     *   舒张压 < 60 或 > 89 mmHg
     *   血糖 < 3.9 或 > 6.1 mmol/L
     */
    @GetMapping("/abnormal-beds")
    public R getAbnormalBeds() {
        // 获取所有在住客户
        List<Customer> customers = customerMapper.selectList(
            new LambdaQueryWrapper<Customer>().eq(Customer::getIsCheckIn, 1)
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (Customer c : customers) {
            // 查最新一条健康记录
            HealthRecord latest = healthRecordService.lambdaQuery()
                .eq(HealthRecord::getCustomerId, c.getId())
                .eq(HealthRecord::getIsDeleted, 0)
                .orderByDesc(HealthRecord::getMeasureTime)
                .last("LIMIT 1")
                .one();

            if (latest == null) continue;

            List<String> issues = new ArrayList<>();
            // 收缩压
            if (latest.getSystolicPressure() != null) {
                if (latest.getSystolicPressure() > 139)
                    issues.add("收缩压偏高(" + latest.getSystolicPressure() + "mmHg)");
                if (latest.getSystolicPressure() < 90)
                    issues.add("收缩压偏低(" + latest.getSystolicPressure() + "mmHg)");
            }
            // 舒张压
            if (latest.getDiastolicPressure() != null) {
                if (latest.getDiastolicPressure() > 89)
                    issues.add("舒张压偏高(" + latest.getDiastolicPressure() + "mmHg)");
                if (latest.getDiastolicPressure() < 60)
                    issues.add("舒张压偏低(" + latest.getDiastolicPressure() + "mmHg)");
            }
            // 血糖
            if (latest.getBloodSugar() != null) {
                if (latest.getBloodSugar() > 6.1)
                    issues.add("血糖偏高(" + latest.getBloodSugar() + "mmol/L)");
                if (latest.getBloodSugar() < 3.9)
                    issues.add("血糖偏低(" + latest.getBloodSugar() + "mmol/L)");
            }

            if (!issues.isEmpty()) {
                Map<String, Object> info = new HashMap<>();
                info.put("customerId", c.getId());
                info.put("customerName", c.getName());
                info.put("bedId", c.getBedId());
                info.put("issues", String.join("、", issues));
                info.put("measureTime", latest.getMeasureTime() != null ? latest.getMeasureTime().toString() : "");
                info.put("level",  // 严重程度: 1=单个指标异常, 2=两个指标异常, 3=三个指标异常
                    issues.size() >= 3 ? 3 : issues.size() >= 2 ? 2 : 1);
                result.add(info);
            }
        }
        return R.ok(result);
    }
}
