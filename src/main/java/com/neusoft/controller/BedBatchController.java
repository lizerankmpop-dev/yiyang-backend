package com.neusoft.controller;

import com.neusoft.common.R;
import com.neusoft.service.BedBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 床位批量操作 Controller
 * 修复：路径从 /bed/batch 改为 /api/bed/batch，走统一鉴权拦截
 */
@RestController
@RequestMapping("/api/bed/batch")
@RequiredArgsConstructor
public class BedBatchController {

    private final BedBatchService bedBatchService;

    /**
     * 批量修改床位状态
     */
    @PostMapping("/updateStatus")
    public R<String> batchUpdateStatus(@RequestParam List<Integer> bedIds,
                                       @RequestParam Integer status) {
        bedBatchService.batchUpdateBedStatus(bedIds, status);
        return R.ok("操作成功");
    }
}
