package com.neusoft.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neusoft.common.R;
import com.neusoft.entity.StatisticsData;
import com.neusoft.entity.TrendData;
import com.neusoft.entity.Backdown;
import com.neusoft.entity.Bed;
import com.neusoft.entity.HealthRecord;
import com.neusoft.entity.Nurse;
import com.neusoft.entity.NurseLeave;
import com.neusoft.entity.Outward;
import com.neusoft.mapper.BedMapper;
import com.neusoft.mapper.CustomerMapper;
import com.neusoft.mapper.CustomerFamilyMapper;
import com.neusoft.mapper.HealthRecordMapper;
import com.neusoft.mapper.NurseMapper;
import com.neusoft.mapper.NurseCustomerMapper;
import com.neusoft.service.BackdownService;
import com.neusoft.service.NurseLeaveService;
import com.neusoft.service.OutwardService;
import com.neusoft.service.StatisticsService;
import com.neusoft.service.CustomerFamilyService;
import com.neusoft.service.NurseCustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据大屏统计接口
 */
@Slf4j  // 1. 新增日志注解，便于排查问题
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final BedMapper bedMapper;
    private final CustomerMapper customerMapper;
    private final HealthRecordMapper healthRecordMapper;
    private final NurseMapper nurseMapper;
    private final StatisticsService statisticsService;
    private final OutwardService outwardService;
    private final BackdownService backdownService;
    private final NurseLeaveService nurseLeaveService;
    private final CustomerFamilyService customerFamilyService;
    private final NurseCustomerService nurseCustomerService;

    // 1. 概览数据（总客户、床位、护士、入住率）
    @GetMapping("/overview")
    public R<Map<String, Object>> getOverview() {
        log.info("【统计】查询数据概览");
        try {
            Map<String, Object> overview = buildBedStats();
            
            // 客户统计：只统计未删除客户
            LambdaQueryWrapper<com.neusoft.entity.Customer> customerQuery = new LambdaQueryWrapper<>();
            customerQuery.eq(com.neusoft.entity.Customer::getIsDeleted, 0);
            int totalCustomers = Math.toIntExact(customerMapper.selectCount(customerQuery));
            overview.put("totalCustomers", totalCustomers);
            
            // 在住客户数（is_check_in = 1）
            LambdaQueryWrapper<com.neusoft.entity.Customer> inHouseQuery = new LambdaQueryWrapper<>();
            inHouseQuery.eq(com.neusoft.entity.Customer::getIsDeleted, 0)
                        .eq(com.neusoft.entity.Customer::getIsCheckIn, 1);
            int inHouseCustomers = Math.toIntExact(customerMapper.selectCount(inHouseQuery));
            overview.put("inHouseCustomers", inHouseCustomers);
            
            // 外出客户数（is_outing = 1）
            LambdaQueryWrapper<com.neusoft.entity.Customer> outingQuery = new LambdaQueryWrapper<>();
            outingQuery.eq(com.neusoft.entity.Customer::getIsDeleted, 0)
                       .eq(com.neusoft.entity.Customer::getIsOuting, 1);
            int outingCustomers = Math.toIntExact(customerMapper.selectCount(outingQuery));
            overview.put("outingCustomers", outingCustomers);
            
            // 护士统计：只统计未删除护士
            LambdaQueryWrapper<com.neusoft.entity.Nurse> nurseQuery = new LambdaQueryWrapper<>();
            nurseQuery.eq(com.neusoft.entity.Nurse::getIsDeleted, 0);
            overview.put("totalNurses", Math.toIntExact(nurseMapper.selectCount(nurseQuery)));
            return R.ok(overview);
        } catch (Exception e) {
            log.error("查询数据概览失败", e);
            return R.fail("数据概览加载失败：" + e.getMessage());
        }
    }

    // 2. 床位统计
    @GetMapping("/bed")
    public R<Map<String, Object>> getBedStatistics() {
        log.info("【统计】查询床位统计数据");
        try {
            return R.ok(buildBedStats());
        } catch (Exception e) {
            log.error("查询床位统计失败", e);
            return R.fail("床位统计加载失败：" + e.getMessage());
        }
    }

    // 3. 客户性别分布
    @GetMapping({"/gender", "/gender-distribution"})
    public R<List<Map<String, Object>>> getGenderDistribution() {
        log.info("【统计】查询客户性别分布");
        try {
            return R.ok(customerMapper.countGenderDistribution());
        } catch (Exception e) {
            log.error("查询性别分布失败", e);
            return R.fail("性别分布加载失败：" + e.getMessage());
        }
    }

    // 4. 客户年龄分布
    @GetMapping({"/age", "/age-distribution"})
    public R<List<Map<String, Object>>> getAgeDistribution() {
        log.info("【统计】查询客户年龄分布");
        try {
            return R.ok(customerMapper.countAgeDistribution());
        } catch (Exception e) {
            log.error("查询年龄分布失败", e);
            return R.fail("年龄分布加载失败：" + e.getMessage());
        }
    }

    // 5. 护工工作量排名
    @GetMapping("/nurse-workload")
    public R<List<StatisticsData>> getNurseWorkload() {
        log.info("【统计】查询护工工作量排名");
        try {
            return R.ok(statisticsService.getNurseWorkload());
        } catch (Exception e) {
            log.error("查询护工工作量失败", e);
            return R.fail("护工工作量加载失败：" + e.getMessage());
        }
    }

    // 6. 最近7天入住趋势
    @GetMapping({"/checkin-trend", "/check-in-trend"})
    public R<List<TrendData>> getCheckInTrend() {
        log.info("【统计】查询最近7天入住趋势");
        try {
            return R.ok(statisticsService.getRecentCheckInTrend());
        } catch (Exception e) {
            log.error("查询入住趋势失败", e);
            return R.fail("入住趋势加载失败：" + e.getMessage());
        }
    }

    // 7. 最近7天退住趋势
    @GetMapping({"/checkout-trend", "/check-out-trend"})
    public R<List<TrendData>> getCheckOutTrend() {
        log.info("【统计】查询最近7天退住趋势");
        try {
            return R.ok(statisticsService.getRecentCheckOutTrend());
        } catch (Exception e) {
            log.error("查询退住趋势失败", e);
            return R.fail("退住趋势加载失败：" + e.getMessage());
        }
    }

    // 8. 近30天客户数量趋势
    @GetMapping("/customer-count-trend")
    public R<List<TrendData>> getCustomerCountTrend() {
        log.info("【统计】查询近30天客户数量趋势");
        try {
            return R.ok(statisticsService.getCustomerCountTrend());
        } catch (Exception e) {
            log.error("查询客户数量趋势失败", e);
            return R.fail("客户数量趋势加载失败：" + e.getMessage());
        }
    }

    // 9. 近30天人力占用率趋势
    @GetMapping("/nurse-utilization-trend")
    public R<List<TrendData>> getNurseUtilizationTrend() {
        log.info("【统计】查询近30天人力占用率趋势");
        try {
            return R.ok(statisticsService.getNurseUtilizationTrend());
        } catch (Exception e) {
            log.error("查询人力占用率趋势失败", e);
            return R.fail("人力占用率趋势加载失败：" + e.getMessage());
        }
    }

    // 10. 风险热度统计（护理异常 / 健康预警 / 外出超时 / 无紧急联系人 / 待分配护工 / 退住待审核）
    @GetMapping("/risk")
    public R<Map<String, Object>> getRiskStats() {
        log.info("【统计】查询风险热度数据");
        try {
            Map<String, Object> result = new HashMap<>();

            // ① 高风险护理：三级护理（id=3, 需全天候护理）+ 特级护理（id=4, 危重病人24小时监护）
            LambdaQueryWrapper<com.neusoft.entity.Customer> highNursingQuery = new LambdaQueryWrapper<>();
            highNursingQuery.eq(com.neusoft.entity.Customer::getIsDeleted, 0)
                           .eq(com.neusoft.entity.Customer::getIsCheckIn, 1)
                           .in(com.neusoft.entity.Customer::getLevelId, 3, 4);
            result.put("nursingException", Math.toIntExact(customerMapper.selectCount(highNursingQuery)));

            // ② 健康预警：最近一条健康记录中血压或血糖异常的在住客户数
            //    异常标准：收缩压≥140或<90，舒张压≥90或<60，血糖≥11.1或<3.9
            int healthWarningCount = 0;
            LambdaQueryWrapper<com.neusoft.entity.Customer> inHouseQuery2 = new LambdaQueryWrapper<>();
            inHouseQuery2.eq(com.neusoft.entity.Customer::getIsDeleted, 0)
                         .eq(com.neusoft.entity.Customer::getIsCheckIn, 1);
            java.util.List<com.neusoft.entity.Customer> inHouseCustomers = customerMapper.selectList(inHouseQuery2);
            for (com.neusoft.entity.Customer c : inHouseCustomers) {
                LambdaQueryWrapper<HealthRecord> latestQuery = new LambdaQueryWrapper<>();
                latestQuery.eq(HealthRecord::getCustomerId, c.getId())
                            .eq(HealthRecord::getIsDeleted, 0)
                            .orderByDesc(HealthRecord::getMeasureTime)
                            .last("LIMIT 1");
                HealthRecord latest = healthRecordMapper.selectOne(latestQuery);
                if (latest != null) {
                    boolean abnormal = false;
                    if (latest.getSystolicPressure() != null && (latest.getSystolicPressure() >= 140 || latest.getSystolicPressure() < 90)) abnormal = true;
                    if (latest.getDiastolicPressure() != null && (latest.getDiastolicPressure() >= 90 || latest.getDiastolicPressure() < 60)) abnormal = true;
                    if (latest.getBloodSugar() != null && (latest.getBloodSugar() >= 11.1 || latest.getBloodSugar() < 3.9)) abnormal = true;
                    if (abnormal) healthWarningCount++;
                }
            }
            result.put("healthWarning", healthWarningCount);

            // ③ 外出超时：Outward表中 status=1（已通过）且预计返回日期已过且未实际返回的记录数
            java.time.LocalDate today = java.time.LocalDate.now();
            LambdaQueryWrapper<Outward> outingOverdueQuery = new LambdaQueryWrapper<>();
            outingOverdueQuery.eq(Outward::getStatus, 1)
                              .isNotNull(Outward::getExpectedReturnDate)
                              .lt(Outward::getExpectedReturnDate, today)
                              .isNull(Outward::getActualReturnDate);
            result.put("outingOverdue", Math.toIntExact(outwardService.count(outingOverdueQuery)));

            // ④ 无紧急联系人：在住客户中没有关联家属电话的数量
            int noEmergencyContact = 0;
            for (com.neusoft.entity.Customer c : inHouseCustomers) {
                long familyCount = customerFamilyService.lambdaQuery()
                        .eq(com.neusoft.entity.CustomerFamily::getCustomerId, c.getId())
                        .count();
                if (familyCount == 0) noEmergencyContact++;
            }
            result.put("noEmergencyContact", noEmergencyContact);

            // ⑤ 待分配护工：在住客户中未分配护工的数量（nurse_customer 表中无记录）
            int noNurseAssigned = 0;
            for (com.neusoft.entity.Customer c : inHouseCustomers) {
                long assignCount = nurseCustomerService.lambdaQuery()
                        .eq(com.neusoft.entity.NurseCustomer::getCustomerId, c.getId())
                        .count();
                if (assignCount == 0) noNurseAssigned++;
            }
            result.put("noNurseAssigned", noNurseAssigned);

            // ⑥ 退住待审核
            long backdownPending = backdownService.lambdaQuery()
                    .eq(Backdown::getStatus, 0)
                    .count();
            result.put("backdownPending", Math.toIntExact(backdownPending));

            return R.ok(result);
        } catch (Exception e) {
            log.error("查询风险热度数据失败", e);
            return R.fail("风险热度加载失败：" + e.getMessage());
        }
    }

    // 11. 待处理审批统计（外出待审 + 退住待审 + 请假待审）
    @GetMapping("/pending-approvals")
    public R<Map<String, Object>> getPendingApprovals() {
        log.info("【统计】查询待处理审批数量");
        try {
            // 外出待审批
            long outwardPending = outwardService.lambdaQuery()
                    .eq(Outward::getStatus, 0)
                    .count();
            // 退住待审批
            long backdownPending = backdownService.lambdaQuery()
                    .eq(Backdown::getStatus, 0)
                    .count();
            // 护工请假待审批
            long nurseLeavePending = nurseLeaveService.lambdaQuery()
                    .eq(NurseLeave::getStatus, 0)
                    .count();

            Map<String, Object> result = new HashMap<>();
            result.put("outward", outwardPending);
            result.put("backdown", backdownPending);
            result.put("nurseLeave", nurseLeavePending);
            result.put("total", outwardPending + backdownPending + nurseLeavePending);
            return R.ok(result);
        } catch (Exception e) {
            log.error("查询待处理审批失败", e);
            return R.fail("待处理审批加载失败：" + e.getMessage());
        }
    }

    // 12. 一次性获取所有统计（大屏首页初始化用）
    @GetMapping("/all")
    public R<Map<String, Object>> getAllStatistics() {
        log.info("【统计】一次性获取所有统计数据");
        try {
            Map<String, Object> allData = new HashMap<>();
            allData.put("bed", buildBedStats());
            allData.put("gender", customerMapper.countGenderDistribution());
            allData.put("age", customerMapper.countAgeDistribution());
            allData.put("nurseWorkload", statisticsService.getNurseWorkload());
            allData.put("checkInTrend", statisticsService.getRecentCheckInTrend());
            allData.put("checkOutTrend", statisticsService.getRecentCheckOutTrend());
            return R.ok(allData);
        } catch (Exception e) {
            log.error("获取所有统计数据失败", e);
            return R.fail("统计数据加载失败：" + e.getMessage());
        }
    }

    // ==================== 私有工具方法 ====================

    /**
     * 构建床位统计数据（复用）
     */
    private Map<String, Object> buildBedStats() {
        // 1. 总床位数：只统计未删除床位（逻辑删除兼容）
        LambdaQueryWrapper<Bed> bedQuery = new LambdaQueryWrapper<>();
        bedQuery.eq(Bed::getIsDeleted, 0);  // 过滤已删除数据
        int totalBeds = Math.toIntExact(bedMapper.selectCount(bedQuery));

        // 2. 已占用/空闲床位（通过Mapper方法获取，已包含逻辑删除过滤）
        int occupiedBeds = bedMapper.countOccupiedBeds();
        int availableBeds = bedMapper.countAvailableBeds();

        // 3. 外出中客户数
        LambdaQueryWrapper<com.neusoft.entity.Customer> outingQuery = new LambdaQueryWrapper<>();
        outingQuery.eq(com.neusoft.entity.Customer::getIsDeleted, 0)
                   .eq(com.neusoft.entity.Customer::getIsOuting, 1);
        int outwardBeds = Math.toIntExact(customerMapper.selectCount(outingQuery));

        // 4. 床位占用率：高精度计算，避免浮点数误差
        double occupancyRate = 0.0;
        if (totalBeds > 0) {
            // 优化：使用BigDecimal完整计算，避免中间转换丢失精度
            occupancyRate = new BigDecimal(occupiedBeds)
                    .divide(new BigDecimal(totalBeds), 4, RoundingMode.HALF_UP)  // 先保留4位小数
                    .multiply(new BigDecimal(100))  // 乘以100转百分比
                    .setScale(2, RoundingMode.HALF_UP)  // 最终保留2位小数
                    .doubleValue();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalBeds", totalBeds);
        result.put("occupiedBeds", occupiedBeds);
        result.put("availableBeds", availableBeds);
        result.put("outwardBeds", outwardBeds);
        result.put("occupancyRate", occupancyRate);
        // 兼容前端字段名
        result.put("available", availableBeds);
        result.put("occupied", occupiedBeds);
        result.put("outward", outwardBeds);
        return result;
    }
}