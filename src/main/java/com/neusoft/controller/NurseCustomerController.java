package com.neusoft.controller;

import com.neusoft.common.R;
import com.neusoft.entity.Bed;
import com.neusoft.entity.Customer;
import com.neusoft.entity.CustomerNursingLevel;
import com.neusoft.entity.Nurse;
import com.neusoft.entity.NurseCustomer;
import com.neusoft.entity.NurseItem;
import com.neusoft.entity.NursingLevel;
import com.neusoft.entity.NursingLevelItem;
import com.neusoft.service.BedService;
import com.neusoft.service.CustomerNursingLevelService;
import com.neusoft.service.CustomerService;
import com.neusoft.service.NurseCustomerService;
import com.neusoft.service.NurseItemService;
import com.neusoft.service.NurseService;
import com.neusoft.service.NursingLevelItemService;
import com.neusoft.service.NursingLevelService;
import com.neusoft.utils.ValidateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/nurse-customer")
@RequiredArgsConstructor
public class NurseCustomerController {

    private final NurseCustomerService nurseCustomerService;
    private final NurseService nurseService;
    private final CustomerService customerService;
    private final CustomerNursingLevelService customerNursingLevelService;
    private final NursingLevelService nursingLevelService;
    private final NursingLevelItemService nursingLevelItemService;
    private final NurseItemService nurseItemService;
    private final BedService bedService;

    @GetMapping("/list")
    public R<List<NurseCustomer>> list(@RequestParam(required = false) Integer nurseId,
                                       @RequestParam(required = false) Integer customerId) {
        List<NurseCustomer> list = nurseCustomerService.lambdaQuery()
                .eq(nurseId != null, NurseCustomer::getNurseId, nurseId)
                .eq(customerId != null, NurseCustomer::getCustomerId, customerId)
                .orderByAsc(NurseCustomer::getId)
                .list();
        
        // 联表查询，填充 nurseName 和 customerName
        for (NurseCustomer nc : list) {
            if (nc.getNurseId() != null) {
                Nurse nurse = nurseService.getById(nc.getNurseId());
                if (nurse != null) {
                    nc.setNurseName(nurse.getName());
                }
            }
            if (nc.getCustomerId() != null) {
                Customer customer = customerService.getById(nc.getCustomerId());
                if (customer != null) {
                    nc.setCustomerName(customer.getName());
                }
            }
        }
        
        return R.ok(list);
    }

    @PostMapping("/assign")
    public R<String> assign(@RequestBody NurseCustomer request) {
        R<String> error = validateAssignRequest(request);
        if (error != null) {
            return error;
        }

        String assignDate = ValidateUtil.isBlank(request.getAssignDate())
                ? LocalDate.now().toString()
                : ValidateUtil.trim(request.getAssignDate());
        String remark = ValidateUtil.trim(request.getRemark());
        int savedCount = 0;

        for (Integer customerId : request.getCustomerIds()) {
            List<NurseCustomer> oldList = nurseCustomerService.lambdaQuery()
                    .eq(NurseCustomer::getCustomerId, customerId)
                    .list();
            NurseCustomer old = oldList.isEmpty() ? null : oldList.get(0);
            if (old == null) {
                NurseCustomer nurseCustomer = new NurseCustomer();
                // 不再手动设置 ID，依赖数据库自增
                nurseCustomer.setNurseId(request.getNurseId());
                nurseCustomer.setCustomerId(customerId);
                nurseCustomer.setAssignDate(assignDate);
                nurseCustomer.setRemark(remark);
                nurseCustomerService.save(nurseCustomer);
            } else {
                old.setNurseId(request.getNurseId());
                old.setAssignDate(assignDate);
                old.setRemark(remark);
                nurseCustomerService.updateById(old);
            }
            savedCount++;
        }
        return R.ok("分配成功，共分配" + savedCount + "位老人");
    }

    /**
     * 获取指定护工的护理安排（客户+护理项目），供排班新增任务选择
     * 返回格式：[{ customerId, customerName, customerAge, customerGender, customerBedNo, levelId, levelName, items: [{ itemId, itemName, itemCategory }] }]
     */
    @GetMapping("/assignments/{nurseId}")
    public R<List<Map<String, Object>>> assignments(@PathVariable Integer nurseId) {
        if (!ValidateUtil.isPositive(nurseId)) {
            return R.fail("护工ID无效");
        }

        // 1. 查询该护工负责的所有客户
        List<NurseCustomer> assignments = nurseCustomerService.lambdaQuery()
                .eq(NurseCustomer::getNurseId, nurseId)
                .list();

        List<Map<String, Object>> result = new ArrayList<>();
        for (NurseCustomer nc : assignments) {
            Customer customer = customerService.getById(nc.getCustomerId());
            if (customer == null || customer.getIsDeleted() == 1 || customer.getIsCheckIn() != 1) {
                continue; // 跳过已删除或未入住的客户
            }

            Map<String, Object> customerInfo = new LinkedHashMap<>();
            customerInfo.put("customerId", customer.getId());
            customerInfo.put("customerName", customer.getName());
            customerInfo.put("customerAge", customer.getAge());
            customerInfo.put("customerGender", customer.getGender());
            // 获取床位号
            if (customer.getBedId() != null) {
                Bed bed = bedService.getById(customer.getBedId());
                customerInfo.put("customerBedNo", bed != null ? bed.getBedNo() : "");
            } else {
                customerInfo.put("customerBedNo", "");
            }
            customerInfo.put("assignDate", nc.getAssignDate());

            // 2. 查询客户的护理级别（优先从 customer.level_id 获取）
            List<Map<String, Object>> itemList = new ArrayList<>();
            Integer levelId = customer.getLevelId();
            if (levelId != null) {
                NursingLevel level = nursingLevelService.getById(levelId);
                if (level != null) {
                    customerInfo.put("levelId", level.getId());
                    customerInfo.put("levelName", level.getLevelName());

                    // 3. 查询该护理级别关联的护理项目
                    List<NursingLevelItem> levelItems = nursingLevelItemService.lambdaQuery()
                            .eq(NursingLevelItem::getLevelId, level.getId())
                            .list();
                    for (NursingLevelItem li : levelItems) {
                        NurseItem item = nurseItemService.getById(li.getItemId());
                        if (item != null && item.getStatus() == 1) {
                            Map<String, Object> itemInfo = new LinkedHashMap<>();
                            itemInfo.put("itemId", item.getId());
                            itemInfo.put("itemName", item.getItemName());
                            itemInfo.put("itemCategory", item.getCategory());
                            itemInfo.put("itemUnit", item.getUnit());
                            itemList.add(itemInfo);
                        }
                    }
                }
            }
            customerInfo.put("items", itemList);
            result.add(customerInfo);
        }
        return R.ok(result);
    }

    @DeleteMapping("/delete")
    public R<String> delete(@RequestParam Integer id) {
        if (!ValidateUtil.isPositive(id)) {
            return R.fail("ID无效");
        }
        if (nurseCustomerService.getById(id) == null) {
            return R.fail("分配记录不存在");
        }
        nurseCustomerService.removeById(id);
        return R.ok("删除成功");
    }

    private R<String> validateAssignRequest(NurseCustomer request) {
        if (request == null) {
            return R.fail("分配信息不能为空");
        }
        if (!ValidateUtil.isPositive(request.getNurseId())) {
            return R.fail("护工ID必须是正整数");
        }
        if (nurseService.getById(request.getNurseId()) == null) {
            return R.fail("护工不存在");
        }
        if (request.getCustomerIds() == null || request.getCustomerIds().isEmpty()) {
            return R.fail("老人ID列表不能为空");
        }
        for (Integer customerId : request.getCustomerIds()) {
            if (!ValidateUtil.isPositive(customerId)) {
                return R.fail("老人ID必须是正整数");
            }
            if (customerService.getById(customerId) == null) {
                return R.fail("老人不存在：" + customerId);
            }
        }
        if (!ValidateUtil.isBlank(request.getAssignDate())
                && !request.getAssignDate().trim().matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            return R.fail("分配日期格式应为yyyy-MM-dd");
        }
        if (request.getRemark() != null && request.getRemark().trim().length() > 100) {
            return R.fail("备注长度不能超过100个字符");
        }
        return null;
    }
}
