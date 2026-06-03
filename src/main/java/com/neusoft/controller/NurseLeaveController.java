package com.neusoft.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neusoft.common.R;
import com.neusoft.entity.Admin;
import com.neusoft.entity.Nurse;
import com.neusoft.entity.NurseLeave;
import com.neusoft.service.AdminService;
import com.neusoft.service.NurseLeaveService;
import com.neusoft.service.NurseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 护工请假 Controller
 */
@RestController
@RequestMapping("/api/nurse-leave")
@RequiredArgsConstructor
public class NurseLeaveController {

    private final NurseLeaveService nurseLeaveService;
    private final NurseService nurseService;
    private final AdminService adminService;
    private final HttpServletRequest httpRequest;

    /**
     * 分页查询请假列表
     */
    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        Page<NurseLeave> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<NurseLeave> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索：护工姓名
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(NurseLeave::getNurseName, keyword.trim());
        }

        // 状态筛选
        if (status != null) {
            wrapper.eq(NurseLeave::getStatus, status);
        }

        // 日期范围筛选
        if (startDate != null && !startDate.trim().isEmpty()) {
            wrapper.ge(NurseLeave::getStartDate, startDate.trim());
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            wrapper.le(NurseLeave::getEndDate, endDate.trim());
        }

        wrapper.orderByDesc(NurseLeave::getCreateTime);
        Page<NurseLeave> result = nurseLeaveService.page(pageParam, wrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        return R.ok(data);
    }

    /**
     * 提交请假申请
     */
    @PostMapping("/apply")
    public R<String> apply(@RequestBody NurseLeave leave) {
        if (leave.getNurseId() == null) {
            return R.fail("请选择护工");
        }
        if (leave.getLeaveType() == null || leave.getLeaveType().trim().isEmpty()) {
            return R.fail("请选择请假类型");
        }
        if (leave.getStartDate() == null || leave.getEndDate() == null) {
            return R.fail("请选择请假日期范围");
        }
        if (leave.getEndDate().isBefore(leave.getStartDate())) {
            return R.fail("结束日期不能早于开始日期");
        }

        // 补充护工姓名
        Nurse nurse = nurseService.getById(leave.getNurseId());
        if (nurse != null) {
            leave.setNurseName(nurse.getName());
        }

        // 计算请假天数
        int days = (int) ChronoUnit.DAYS.between(leave.getStartDate(), leave.getEndDate()) + 1;
        leave.setTotalDays(days);

        // 设置申请人信息
        Integer userId = getCurrentUserId();
        leave.setApplicantId(userId);
        Admin admin = adminService.getById(userId);
        if (admin != null) {
            leave.setApplicantName(admin.getUsername());
        }

        leave.setStatus(0); // 待审批
        leave.setCreateTime(LocalDateTime.now());
        leave.setUpdateTime(LocalDateTime.now());

        nurseLeaveService.save(leave);
        return R.ok("请假申请已提交");
    }

    /**
     * 编辑请假申请
     */
    @PutMapping("/update/{id}")
    public R<String> update(@PathVariable Integer id, @RequestBody NurseLeave leave) {
        NurseLeave existing = nurseLeaveService.getById(id);
        if (existing == null) {
            return R.fail("请假记录不存在");
        }
        // 只能编辑待审批的记录
        if (existing.getStatus() != 0) {
            return R.fail("只能编辑待审批的请假申请");
        }

        if (leave.getNurseId() != null) {
            Nurse nurse = nurseService.getById(leave.getNurseId());
            if (nurse != null) {
                leave.setNurseName(nurse.getName());
            }
        }
        if (leave.getStartDate() != null && leave.getEndDate() != null) {
            if (leave.getEndDate().isBefore(leave.getStartDate())) {
                return R.fail("结束日期不能早于开始日期");
            }
            int days = (int) ChronoUnit.DAYS.between(leave.getStartDate(), leave.getEndDate()) + 1;
            leave.setTotalDays(days);
        }

        leave.setId(id);
        leave.setUpdateTime(LocalDateTime.now());
        nurseLeaveService.updateById(leave);
        return R.ok("更新成功");
    }

    /**
     * 删除请假记录
     */
    @DeleteMapping("/delete/{id}")
    public R<String> delete(@PathVariable Integer id) {
        NurseLeave existing = nurseLeaveService.getById(id);
        if (existing == null) {
            return R.fail("请假记录不存在");
        }
        nurseLeaveService.removeById(id);
        return R.ok("删除成功");
    }

    /**
     * 审批请假（通过/拒绝）
     */
    @PutMapping("/approve/{id}")
    public R<String> approve(
            @PathVariable Integer id,
            @RequestParam Integer status,
            @RequestParam(required = false) String remark) {

        NurseLeave existing = nurseLeaveService.getById(id);
        if (existing == null) {
            return R.fail("请假记录不存在");
        }
        if (existing.getStatus() != 0) {
            return R.fail("该请假申请已处理，无法重复审批");
        }
        if (status != 1 && status != 2) {
            return R.fail("审批状态参数错误");
        }

        Integer userId = getCurrentUserId();
        Admin admin = adminService.getById(userId);

        existing.setStatus(status);
        existing.setApproverId(userId);
        if (admin != null) {
            existing.setApproverName(admin.getUsername());
        }
        if (remark != null && !remark.trim().isEmpty()) {
            existing.setApproveRemark(remark.trim());
        }
        existing.setUpdateTime(LocalDateTime.now());

        nurseLeaveService.updateById(existing);
        return R.ok(status == 1 ? "已通过请假申请" : "已拒绝请假申请");
    }

    /**
     * 销假
     */
    @PutMapping("/cancel-leave/{id}")
    public R<String> cancelLeave(@PathVariable Integer id) {
        NurseLeave existing = nurseLeaveService.getById(id);
        if (existing == null) {
            return R.fail("请假记录不存在");
        }
        if (existing.getStatus() != 1) {
            return R.fail("只有已通过的请假申请才能销假");
        }

        existing.setStatus(3); // 已销假
        existing.setUpdateTime(LocalDateTime.now());
        nurseLeaveService.updateById(existing);
        return R.ok("销假成功");
    }

    /**
     * 获取已通过的请假记录（用于时间安排标注）
     */
    @GetMapping("/approved")
    public R<List<NurseLeave>> approved(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        LambdaQueryWrapper<NurseLeave> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NurseLeave::getStatus, 1); // 已通过

        if (startDate != null && !startDate.trim().isEmpty()) {
            wrapper.ge(NurseLeave::getStartDate, startDate.trim());
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            wrapper.le(NurseLeave::getEndDate, endDate.trim());
        }

        List<NurseLeave> list = nurseLeaveService.list(wrapper);
        return R.ok(list);
    }

    /**
     * 获取节假日信息（静态数据，供前端参考）
     */
    @GetMapping("/holidays")
    public R<Map<String, Object>> holidays() {
        Map<String, Object> data = new HashMap<>();
        data.put("statutory", new ArrayList<>());
        data.put("industry", new ArrayList<>());
        return R.ok(data);
    }

    private Integer getCurrentUserId() {
        try {
            return (Integer) httpRequest.getAttribute("userId");
        } catch (Exception e) {
            return 0;
        }
    }
}
