package com.neusoft.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neusoft.common.R;
import com.neusoft.entity.NursingRecord;
import com.neusoft.service.CustomerService;
import com.neusoft.service.NurseItemService;
import com.neusoft.service.NurseService;
import com.neusoft.service.NursingRecordService;
import lombok.RequiredArgsConstructor;
import lombok.var;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 护理记录 Controller
 * 修复：
 * 1. 移除 nextId()（遍历全表取 max+1），主键改由 DB 自增
 * 2. recordDate/recordTime 改用 LocalDate/LocalTime 参数
 * 3. 列表接口增加分页
 * 4. 列表接口增加联表查询，返回 customerName 和 nurseName
 */
@RestController
@RequestMapping("/api/nursing-record")
@RequiredArgsConstructor
public class NursingRecordController {

    private final NursingRecordService nursingRecordService;
    private final CustomerService customerService;
    private final NurseService nurseService;
    private final NurseItemService nurseItemService;

    /**
     * 分页列表查询（联表查询返回完整信息）
     */
    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer customerId,
            @RequestParam(required = false) Integer nurseId,
            @RequestParam(required = false) Integer itemId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate recordDate) {

        Page<NursingRecord> pageParam = new Page<>(page, size);
        Page<NursingRecord> result = nursingRecordService.lambdaQuery()
                .eq(customerId != null, NursingRecord::getCustomerId, customerId)
                .eq(nurseId != null, NursingRecord::getNurseId, nurseId)
                .eq(itemId != null, NursingRecord::getItemId, itemId)
                .eq(recordDate != null, NursingRecord::getRecordDate, recordDate)
                .orderByDesc(NursingRecord::getId)
                .page(pageParam);

        // 联表查询，填充 customerName 和 nurseName
        List<NursingRecord> records = result.getRecords();
        for (NursingRecord record : records) {
            if (record.getCustomerId() != null) {
                var customer = customerService.getById(record.getCustomerId());
                if (customer != null) {
                    record.setCustomerName(customer.getName());
                }
            }
            if (record.getNurseId() != null) {
                var nurse = nurseService.getById(record.getNurseId());
                if (nurse != null) {
                    record.setNurseName(nurse.getName());
                }
            }
        }

        HashMap<String,Object> map = new HashMap<>();
        map.put("records",records);
        map.put("total",result.getTotal());
        map.put("pages",result.getPages());
        map.put("current",result.getCurrent());
        return R.ok(map);
    }

    /**
     * 新增护理记录（主键由 DB 自增，无需手动设置）
     */
    @PostMapping("/add")
    public R<String> add(@RequestBody NursingRecord nursingRecord) {
        R<String> error = validateNursingRecord(nursingRecord);
        if (error != null) {
            return error;
        }
        // 默认时间
        if (nursingRecord.getRecordDate() == null) {
            nursingRecord.setRecordDate(LocalDate.now());
        }
        if (nursingRecord.getRecordTime() == null) {
            nursingRecord.setRecordTime(LocalTime.now().withNano(0));
        }
        nursingRecordService.save(nursingRecord);
        return R.ok("护理记录录入成功");
    }

    /**
     * 删除护理记录
     */
    @DeleteMapping("/delete")
    public R<String> delete(@RequestParam Integer id) {
        if (id == null || id <= 0) {
            return R.fail("ID无效");
        }
        if (nursingRecordService.getById(id) == null) {
            return R.fail("护理记录不存在");
        }
        nursingRecordService.removeById(id);
        return R.ok("删除成功");
    }

    // ------------------- 私有校验方法 -------------------

    private R<String> validateNursingRecord(NursingRecord nursingRecord) {
        if (nursingRecord == null) {
            return R.fail("护理记录不能为空");
        }
        if (nursingRecord.getCustomerId() == null || nursingRecord.getCustomerId() <= 0) {
            return R.fail("老人ID必须是正整数");
        }
        if (customerService.getById(nursingRecord.getCustomerId()) == null) {
            return R.fail("老人不存在");
        }
        if (nursingRecord.getNurseId() == null || nursingRecord.getNurseId() <= 0) {
            return R.fail("护工ID必须是正整数");
        }
        if (nurseService.getById(nursingRecord.getNurseId()) == null) {
            return R.fail("护工不存在");
        }
        if (nursingRecord.getItemId() == null || nursingRecord.getItemId() <= 0) {
            return R.fail("护理项目ID必须是正整数");
        }
        if (nurseItemService.getById(nursingRecord.getItemId()) == null) {
            return R.fail("护理项目不存在");
        }
        String content = nursingRecord.getContent();
        if (content == null || content.trim().isEmpty()) {
            return R.fail("护理内容不能为空");
        }
        if (content.trim().length() > 200) {
            return R.fail("护理内容长度不能超过200个字符");
        }
        nursingRecord.setContent(content.trim());
        if (nursingRecord.getRemark() != null) {
            if (nursingRecord.getRemark().trim().length() > 100) {
                return R.fail("备注长度不能超过100个字符");
            }
            nursingRecord.setRemark(nursingRecord.getRemark().trim());
        }
        return null;
    }
}
