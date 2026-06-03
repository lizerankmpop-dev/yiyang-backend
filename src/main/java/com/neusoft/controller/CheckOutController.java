package com.neusoft.controller;

import com.neusoft.common.R;
import com.neusoft.service.CheckOutService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckOutController {

    private final CheckOutService checkOutService;

    @GetMapping("/{customerId}")
    public R checkOut(@PathVariable Integer customerId) {
        String bill = checkOutService.checkOut(customerId);
        return R.ok(bill);
    }
}
