package com.neusoft.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neusoft.common.R;
import com.neusoft.entity.Bed;
import com.neusoft.entity.Customer;
import com.neusoft.entity.NursingLevel;
import com.neusoft.mapper.BedMapper;
import com.neusoft.mapper.CustomerMapper;
import com.neusoft.mapper.NurseMapper;
import com.neusoft.mapper.NursingLevelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final CustomerMapper customerMapper;
    private final BedMapper bedMapper;
    private final NurseMapper nurseMapper;
    private final NursingLevelMapper nursingLevelMapper;

    @GetMapping("/stats")
    public R<Map<String, Object>> stats() {
        Map<String, Object> data = new HashMap<>();
        
        // 总床位数（只统计未删除的）
        LambdaQueryWrapper<Bed> bedQuery = new LambdaQueryWrapper<>();
        bedQuery.eq(Bed::getIsDeleted, 0);
        Long totalBeds = bedMapper.selectCount(bedQuery);
        
        // 在住客户数（只统计未删除的）
        LambdaQueryWrapper<Customer> customerQuery = new LambdaQueryWrapper<>();
        customerQuery.eq(Customer::getIsDeleted, 0).eq(Customer::getIsCheckIn, 1);
        Long inHouseCustomers = customerMapper.selectCount(customerQuery);
        
        // 外出客户数（只统计未删除的）
        LambdaQueryWrapper<Customer> outingQuery = new LambdaQueryWrapper<>();
        outingQuery.eq(Customer::getIsDeleted, 0).eq(Customer::getIsOuting, 1);
        Long outingCustomers = customerMapper.selectCount(outingQuery);
        
        // 空闲床位 = 查询status=0的床位（和StatisticsController保持一致）
        long availableBeds = bedMapper.countAvailableBeds();

        data.put("totalBeds", totalBeds);
        data.put("availableBeds", availableBeds);
        data.put("inHouseCustomers", inHouseCustomers);
        data.put("outingCustomers", outingCustomers);
        data.put("totalCustomers", customerMapper.selectCount(new LambdaQueryWrapper<Customer>().eq(Customer::getIsDeleted, 0)));
        data.put("totalNurses", nurseMapper.selectCount(new LambdaQueryWrapper<com.neusoft.entity.Nurse>().eq(com.neusoft.entity.Nurse::getIsDeleted, 0)));
        
        return R.ok(data);
    }

    @GetMapping("/nursing-level-distribution")
    public R<List<Map<String, Object>>> getNursingLevelDistribution() {
        // 1. 查询所有启用的护理级别
        List<NursingLevel> allLevels = nursingLevelMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<NursingLevel>()
                        .eq(NursingLevel::getIsDeleted, 0)
                        .eq(NursingLevel::getLevelStatus, 1)
                        .orderByAsc(NursingLevel::getId)
        );

        // 2. 查询各级别的客户数量
        List<Map<String, Object>> countData = customerMapper.countNursingLevelDistribution();
        Map<Object, Object> countMap = countData.stream()
                .collect(Collectors.toMap(m -> m.get("name"), m -> m.get("value"), (a, b) -> a));

        // 3. 构建结果：所有级别都展示，无客户显示0
        List<Map<String, Object>> result = new ArrayList<>();
        for (NursingLevel level : allLevels) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", level.getLevelName());
            item.put("value", countMap.getOrDefault(level.getLevelName(), 0));
            item.put("levelId", level.getId());
            result.add(item);
        }

        // 4. 补充"未分配级别"（如果有客户没有设置护理级别）
        Object unassignedCount = countMap.get("未分配级别");
        if (unassignedCount != null && ((Number) unassignedCount).intValue() > 0) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", "未分配级别");
            item.put("value", unassignedCount);
            result.add(item);
        }

        return R.ok(result);
    }

    @GetMapping("/age-distribution")
    public R<List<Map<String, Object>>> getAgeDistribution() {
        List<Map<String, Object>> data = customerMapper.countAgeDistribution();
        return R.ok(data);
    }
}
