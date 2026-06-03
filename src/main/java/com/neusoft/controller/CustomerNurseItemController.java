package com.neusoft.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neusoft.common.R;
import com.neusoft.entity.Customer;
import com.neusoft.entity.CustomerNurseItem;
import com.neusoft.entity.NurseItem;
import com.neusoft.service.CustomerNurseItemService;
import com.neusoft.service.CustomerService;
import com.neusoft.service.NurseItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer-nurse-item")
@RequiredArgsConstructor
public class CustomerNurseItemController {
    private final CustomerNurseItemService customerNurseItemService;
    private final CustomerService customerService;
    private final NurseItemService nurseItemService;

    @GetMapping("/list")
    public R<Page<CustomerNurseItem>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer customerId) {
        Page<CustomerNurseItem> result = customerNurseItemService.lambdaQuery()
                .eq(customerId != null, CustomerNurseItem::getCustomerId, customerId)
                .orderByDesc(CustomerNurseItem::getId)
                .page(new Page<>(page, size));
        
        // 联表查询，填充 customerName 和 itemName
        for (CustomerNurseItem item : result.getRecords()) {
            if (item.getCustomerId() != null) {
                Customer customer = customerService.getById(item.getCustomerId());
                if (customer != null) {
                    item.setCustomerName(customer.getName());
                }
            }
            if (item.getItemId() != null) {
                NurseItem nurseItem = nurseItemService.getById(item.getItemId());
                if (nurseItem != null) {
                    item.setItemName(nurseItem.getItemName());
                }
            }
        }
        
        return R.ok(result);
    }

    @PostMapping("/buy")
    public R<String> buy(@RequestBody CustomerNurseItem item) {
        customerNurseItemService.save(item);
        return R.ok("购买成功");
    }

    @PutMapping("/renew/{id}")
    public R<String> renew(@PathVariable Integer id, @RequestBody Map<String, Object> params) {
        CustomerNurseItem item = customerNurseItemService.getById(id);
        if (item == null) return R.fail("记录不存在");
        if (params.get("totalCount") != null) {
            item.setTotalCount(Integer.parseInt(params.get("totalCount").toString()));
        }
        if (params.get("expirationDate") != null) {
            item.setExpirationDate(java.time.LocalDate.parse(params.get("expirationDate").toString()));
        }
        customerNurseItemService.updateById(item);
        return R.ok("续费成功");
    }

    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable Integer id) {
        customerNurseItemService.removeById(id);
        return R.ok("删除成功");
    }
}
