package com.neusoft.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neusoft.common.R;
import com.neusoft.entity.CustomerFamily;
import com.neusoft.service.CustomerFamilyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/customer-family")
@RequiredArgsConstructor
public class CustomerFamilyController {

    private final CustomerFamilyService familyService;

    @PostMapping("/add")
    public R addFamily(@RequestBody CustomerFamily family) {
        familyService.addFamily(family);
        return R.ok("操作成功");
    }

    /**
     * 保存或更新紧急联系人：如果该客户已有家属记录则更新第一条，否则新增
     */
    @PostMapping("/save-or-update")
    public R saveOrUpdateFamily(@RequestBody CustomerFamily family) {
        CustomerFamily existing = familyService.lambdaQuery()
                .eq(CustomerFamily::getCustomerId, family.getCustomerId())
                .last("LIMIT 1")
                .one();
        if (existing != null) {
            existing.setName(family.getName());
            existing.setPhone(family.getPhone());
            existing.setRelation(family.getRelation());
            familyService.updateById(existing);
        } else {
            familyService.save(family);
        }
        return R.ok("操作成功");
    }

    @GetMapping("/list/{customerId}")
    public R getFamily(@PathVariable Integer customerId) {
        List<CustomerFamily> list = familyService.getFamilyByCustomerId(customerId);
        return R.ok(list);
    }
}
