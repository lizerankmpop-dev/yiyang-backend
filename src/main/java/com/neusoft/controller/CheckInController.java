package com.neusoft.controller;

import com.neusoft.common.R;
import com.neusoft.entity.Bed;
import com.neusoft.entity.Customer;
import com.neusoft.mapper.BedMapper;
import com.neusoft.mapper.CheckInRecordMapper;
import com.neusoft.mapper.CustomerMapper;
import com.neusoft.utils.OperationLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
public class CheckInController {
    private final BedMapper bedMapper;
    private final CustomerMapper customerMapper;
    private final CheckInRecordMapper checkInRecordMapper;
    private final OperationLogger operationLogger;
    private final HttpServletRequest httpRequest;

    /**
     * 获取空闲床位列表（status=0）
     */
    @GetMapping("/available-beds")
    public R<List<Bed>> availableBeds() {
        List<Bed> beds = bedMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Bed>()
                .eq(Bed::getStatus, 0)
                .orderByAsc(Bed::getBedNo)
        );
        return R.ok(beds);
    }

    /**
     * 办理入住
     * 接收 {customerId, bedId, checkinDate}
     * 只允许对待分配床位（bedId IS NULL）的客户办理入住
     */
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public R<String> checkIn(@RequestBody Map<String, Object> body) {
        try {
            Integer customerId = body.get("customerId") != null
                    ? Integer.valueOf(body.get("customerId").toString()) : null;
            Integer bedId = body.get("bedId") != null
                    ? Integer.valueOf(body.get("bedId").toString()) : null;
            String checkinDateStr = body.get("checkinDate") != null
                    ? body.get("checkinDate").toString() : null;

            if (customerId == null || checkinDateStr == null || checkinDateStr.isEmpty()) {
                return R.badRequest("请填写完整的入住信息（客户、日期）");
            }

            // 1. 查询客户
            Customer customer = customerMapper.selectById(customerId);
            if (customer == null) {
                return R.badRequest("客户不存在");
            }
            // 不允许重复入住
            if (customer.getIsCheckIn() != null && customer.getIsCheckIn() == 1) {
                return R.badRequest("该客户已在住，请勿重复办理入住");
            }

            // 2. 如果选择了床位，校验并绑定
            Bed bed = null;
            if (bedId != null) {
                if (customer.getBedId() != null) {
                    return R.badRequest("该客户已分配床位 " + customer.getBedId() + "，如需换床位请先退住");
                }
                bed = bedMapper.selectById(bedId);
                if (bed == null) {
                    return R.badRequest("床位不存在");
                }
                if (bed.getStatus() != null && bed.getStatus() == 1) {
                    return R.badRequest("床位 " + bed.getBedNo() + " 已被占用，请重新选择");
                }
            }

            // 3. 更新客户：标记入住（有床位则绑定）
            customer.setIsCheckIn(1);
            customer.setIsOuting(0);
            customer.setCheckInDate(LocalDate.parse(checkinDateStr));
            if (bedId != null) {
                customer.setBedId(bedId);
            }
            customerMapper.updateById(customer);

            // 4. 更新床位状态为已占用（如果有选择床位）
            if (bed != null) {
                bed.setStatus(1);
                bedMapper.updateById(bed);
                // 5. 写入入住记录
                checkInRecordMapper.addCheckInRecord(bedId, customerId);

                log.info("【入住管理】客户 {} 入住床位 {}，日期={}", customer.getName(), bed.getBedNo(), checkinDateStr);

                operationLogger.log(getCurrentUserId(), getCurrentUserName(),
                        "办理入住", "客户-" + customer.getName(),
                        "入住床位 " + bed.getBedNo() + "（" + bed.getFloor() + "楼 " + bed.getRoomNo() + "）");

                return R.ok("入住办理成功：客户 " + customer.getName() + " 入住床位 " + bed.getBedNo());
            } else {
                log.info("【入住管理】客户 {} 入住登记（待分配床位），日期={}", customer.getName(), checkinDateStr);

                operationLogger.log(getCurrentUserId(), getCurrentUserName(),
                        "办理入住", "客户-" + customer.getName(),
                        "待分配床位");

                return R.ok("入住办理成功：客户 " + customer.getName() + "（待分配床位）");
            }
        } catch (Exception e) {
            log.error("办理入住失败", e);
            return R.fail("办理入住失败：" + e.getMessage());
        }
    }

    private Integer getCurrentUserId() {
        try { return (Integer) httpRequest.getAttribute("userId"); } catch (Exception e) { return 0; }
    }
    private String getCurrentUserName() {
        try { return (String) httpRequest.getAttribute("username"); } catch (Exception e) { return "未知"; }
    }
}
