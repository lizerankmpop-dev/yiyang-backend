package com.neusoft.controller;

import com.neusoft.common.R;
import com.neusoft.entity.Bed;
import com.neusoft.entity.Customer;
import com.neusoft.service.BedService;
import com.neusoft.service.ChangeBedService;
import com.neusoft.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/change-bed")
@RequiredArgsConstructor
public class ChangeBedController {
    private final ChangeBedService changeBedService;
    private final BedService bedService;
    private final CustomerService customerService;

    @GetMapping("/available-beds")
    public R<List<Bed>> getAvailableBeds() {
        return R.ok(bedService.lambdaQuery().eq(Bed::getStatus, 0).list());
    }

    @PostMapping
    public R<String> changeBed(@RequestBody Map<String, Integer> params) {
        Integer customerId = params.get("customerId");
        Integer newBedId = params.get("newBedId");
        if (customerId == null || newBedId == null) {
            return R.badRequest("customerId和newBedId不能为空");
        }
        Customer customer = customerService.getById(customerId);
        Bed newBed = bedService.getById(newBedId);
        if (customer == null) return R.fail("客户不存在");
        if (newBed == null) return R.fail("床位不存在");
        boolean success = changeBedService.changeCustomerBed(customer, newBed);
        return success ? R.ok("换床成功") : R.fail("换床失败，床位可能已被占用");
    }
}
