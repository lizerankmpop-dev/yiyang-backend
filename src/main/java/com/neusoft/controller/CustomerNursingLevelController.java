package com.neusoft.controller;

import com.neusoft.common.R;
import com.neusoft.entity.Customer;
import com.neusoft.entity.CustomerNursingLevel;
import com.neusoft.entity.Nurse;
import com.neusoft.entity.NurseCustomer;
import com.neusoft.service.*;
import com.neusoft.utils.ValidateUtil;
import lombok.RequiredArgsConstructor;
import lombok.var;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer-nursing-level")
@RequiredArgsConstructor
public class CustomerNursingLevelController {

    private final CustomerNursingLevelService customerNursingLevelService;
    private final CustomerService customerService;
    private final NursingLevelService nursingLevelService;
    private final NurseCustomerService nurseCustomerService;
    private final NurseService nurseService;

    @GetMapping("/list")
    public R<List<CustomerNursingLevel>> list() {
        List<CustomerNursingLevel> list = customerNursingLevelService.lambdaQuery()
                .orderByAsc(CustomerNursingLevel::getId)
                .list();
        // 联表查询，填充 customerName、levelName 和 nurseName
        for (CustomerNursingLevel item : list) {
            if (item.getCustomerId() != null) {
                Customer customer = customerService.getById(item.getCustomerId());
                if (customer != null) {
                    item.setCustomerName(customer.getName());
                }
                // 通过 nurse_customer 表查询护工
                List<NurseCustomer> ncList = nurseCustomerService.lambdaQuery()
                        .eq(NurseCustomer::getCustomerId, item.getCustomerId())
                        .list();
                if (!ncList.isEmpty()) {
                    Nurse nurse = nurseService.getById(ncList.get(0).getNurseId());
                    if (nurse != null) {
                        item.setNurseName(nurse.getName());
                    }
                }
            }
            if (item.getLevelId() != null) {
                var level = nursingLevelService.getById(item.getLevelId());
                if (level != null) {
                    item.setLevelName(level.getLevelName());
                }
            }
        }
        return R.ok(list);
    }

    @PostMapping("/add")
    public R<String> add(@RequestBody CustomerNursingLevel customerNursingLevel) {
        R<String> error = validateCustomerNursingLevel(customerNursingLevel, false);
        if (error != null) {
            return error;
        }
        long count = customerNursingLevelService.lambdaQuery()
                .eq(CustomerNursingLevel::getCustomerId, customerNursingLevel.getCustomerId())
                .count();
        if (count > 0) {
            return R.fail("该老人已经设置过护理级别，请使用修改功能");
        }
        // 不再手动设置 ID，依赖数据库自增
        customerNursingLevelService.save(customerNursingLevel);
        return R.ok("新增成功");
    }

    @PutMapping("/update")
    public R<String> update(@RequestBody CustomerNursingLevel customerNursingLevel) {
        R<String> error = validateCustomerNursingLevel(customerNursingLevel, true);
        if (error != null) {
            return error;
        }
        if (customerNursingLevelService.getById(customerNursingLevel.getId()) == null) {
            return R.fail("老人护理级别记录不存在");
        }
        List<CustomerNursingLevel> sameCustomerList = customerNursingLevelService.lambdaQuery()
                .eq(CustomerNursingLevel::getCustomerId, customerNursingLevel.getCustomerId())
                .list();
        for (CustomerNursingLevel item : sameCustomerList) {
            if (!item.getId().equals(customerNursingLevel.getId())) {
                return R.fail("该老人已经设置过护理级别");
            }
        }
        customerNursingLevelService.updateById(customerNursingLevel);
        return R.ok("修改成功");
    }

    @PostMapping("/assign")
    public R<String> assign(@RequestBody CustomerNursingLevel customerNursingLevel) {
        R<String> error = validateCustomerNursingLevel(customerNursingLevel, false);
        if (error != null) {
            return error;
        }
        List<CustomerNursingLevel> oldList = customerNursingLevelService.lambdaQuery()
                .eq(CustomerNursingLevel::getCustomerId, customerNursingLevel.getCustomerId())
                .list();
        CustomerNursingLevel old = oldList.isEmpty() ? null : oldList.get(0);
        if (old == null) {
            // 不再手动设置 ID，依赖数据库自增
            customerNursingLevelService.save(customerNursingLevel);
        } else {
            customerNursingLevel.setId(old.getId());
            customerNursingLevelService.updateById(customerNursingLevel);
        }
        return R.ok("分配成功");
    }

    @DeleteMapping("/delete")
    public R<String> delete(@RequestParam Integer id) {
        if (!ValidateUtil.isPositive(id)) {
            return R.fail("ID无效");
        }
        if (customerNursingLevelService.getById(id) == null) {
            return R.fail("老人护理级别记录不存在");
        }
        customerNursingLevelService.removeById(id);
        return R.ok("删除成功");
    }

    private R<String> validateCustomerNursingLevel(CustomerNursingLevel customerNursingLevel, boolean requireId) {
        if (customerNursingLevel == null) {
            return R.fail("老人护理级别信息不能为空");
        }
        if (requireId && !ValidateUtil.isPositive(customerNursingLevel.getId())) {
            return R.fail("ID无效");
        }
        if (!ValidateUtil.isPositive(customerNursingLevel.getCustomerId())) {
            return R.fail("老人ID必须是正整数");
        }
        if (customerService.getById(customerNursingLevel.getCustomerId()) == null) {
            return R.fail("老人不存在");
        }
        if (!ValidateUtil.isPositive(customerNursingLevel.getLevelId())) {
            return R.fail("护理级别ID必须是正整数");
        }
        if (nursingLevelService.getById(customerNursingLevel.getLevelId()) == null) {
            return R.fail("护理级别不存在");
        }
        if (!ValidateUtil.isBlank(customerNursingLevel.getStartDate())
                && !customerNursingLevel.getStartDate().trim().matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            return R.fail("开始日期格式应为yyyy-MM-dd");
        }
        if (customerNursingLevel.getRemark() != null && customerNursingLevel.getRemark().trim().length() > 100) {
            return R.fail("备注长度不能超过100个字符");
        }
        customerNursingLevel.setStartDate(ValidateUtil.trim(customerNursingLevel.getStartDate()));
        customerNursingLevel.setRemark(ValidateUtil.trim(customerNursingLevel.getRemark()));
        return null;
    }
}
