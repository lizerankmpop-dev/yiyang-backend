package com.neusoft.controller;

import com.neusoft.common.R;
import com.neusoft.entity.BedHistory;
import com.neusoft.service.BedHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bed-history")
@RequiredArgsConstructor
public class BedHistoryController {
    private final BedHistoryService bedHistoryService;

    @GetMapping("/list")
    public R<List<BedHistory>> list(@RequestParam(required = false) Integer bedId,
                                     @RequestParam(required = false) Integer customerId) {
        if (bedId != null) {
            return R.ok(bedHistoryService.getHistoryByBedId(bedId));
        }
        return R.ok(bedHistoryService.lambdaQuery()
                .eq(customerId != null, BedHistory::getCustomerId, customerId)
                .orderByDesc(BedHistory::getId)
                .list());
    }

    @PutMapping("/{id}/end-time")
    public R<String> updateEndTime(@PathVariable Integer id, @RequestBody Map<String, String> params) {
        BedHistory history = bedHistoryService.getById(id);
        if (history == null) return R.fail("记录不存在");
        if (params.get("endTime") != null) {
            history.setEndTime(java.time.LocalDateTime.parse(params.get("endTime"), java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        bedHistoryService.updateById(history);
        return R.ok("更新成功");
    }

    @GetMapping("/customer/{customerId}")
    public R<List<BedHistory>> getCustomerBedHistory(@PathVariable Integer customerId) {
        return R.ok(bedHistoryService.lambdaQuery()
                .eq(BedHistory::getCustomerId, customerId)
                .orderByDesc(BedHistory::getId)
                .list());
    }
}
