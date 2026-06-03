package com.neusoft.controller;

import com.neusoft.common.R;
import com.neusoft.entity.*;
import com.neusoft.service.*;
import com.neusoft.utils.ValidateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/nursing/task")
@RequiredArgsConstructor
public class NursingTaskController {

    private final NurseCustomerService nurseCustomerService;
    private final NursingRecordService nursingRecordService;
    private final CustomerNursingLevelService customerNursingLevelService;
    private final NursingLevelService nursingLevelService;
    private final NursingLevelItemService nursingLevelItemService;
    private final NurseItemService nurseItemService;
    private final CustomerService customerService;
    private final NurseService nurseService;

    @GetMapping("/todo")
    public R<List<NursingTask>> todo(@RequestParam(required = false) Integer nurseId,
                                     @RequestParam(required = false) String taskDate) {
        return buildTasks(nurseId, taskDate, true);
    }

    @GetMapping("/list")
    public R<List<NursingTask>> list(@RequestParam(required = false) Integer nurseId,
                                     @RequestParam(required = false) String taskDate) {
        // 如果不传 nurseId，返回所有任务（用于管理员查看）
        if (nurseId == null) {
            return buildAllTasks(taskDate, false);
        }
        return buildTasks(nurseId, taskDate, false);
    }

    @PutMapping("/status")
    public R<String> updateTaskStatus(@RequestBody Map<String, Object> params) {
        Integer nurseId = params.get("nurseId") != null ? Integer.parseInt(params.get("nurseId").toString()) : null;
        Integer customerId = params.get("customerId") != null ? Integer.parseInt(params.get("customerId").toString()) : null;
        Integer itemId = params.get("itemId") != null ? Integer.parseInt(params.get("itemId").toString()) : null;
        String taskDate = params.get("taskDate") != null ? params.get("taskDate").toString() : null;
        String content = params.get("content") != null ? params.get("content").toString() : null;

        if (nurseId == null || customerId == null || itemId == null) {
            return R.badRequest("nurseId、customerId、itemId不能为空");
        }

        String date = (taskDate != null && !taskDate.isEmpty()) ? taskDate : LocalDate.now().toString();

        // 检查是否已完成
        if (isTaskFinished(nurseId, customerId, itemId, date)) {
            return R.ok("该任务已完成");
        }

        NursingRecord record = new NursingRecord();
        record.setNurseId(nurseId);
        record.setCustomerId(customerId);
        record.setItemId(itemId);
        record.setRecordDate(LocalDate.parse(date));
        record.setRecordTime(LocalTime.now().withNano(0));
        record.setContent(content != null ? content : "护理任务完成");
        nursingRecordService.save(record);
        return R.ok("护理任务已完成");
    }

    /**
     * 构建所有护工的任务列表（管理员查看）
     */
    private R<List<NursingTask>> buildAllTasks(String taskDate, boolean onlyTodo) {
        String date = ValidateUtil.isBlank(taskDate) ? LocalDate.now().toString() : ValidateUtil.trim(taskDate);
        if (!date.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            return R.badRequest("任务日期格式应为yyyy-MM-dd");
        }

        List<NursingTask> tasks = new ArrayList<>();
        // 获取所有护工
        List<Nurse> nurses = nurseService.list();
        for (Nurse nurse : nurses) {
            List<NurseCustomer> assignments = nurseCustomerService.lambdaQuery()
                    .eq(NurseCustomer::getNurseId, nurse.getId())
                    .list();
            for (NurseCustomer assignment : assignments) {
                Customer customer = customerService.getById(assignment.getCustomerId());
                if (customer == null) {
                    continue;
                }
                CustomerNursingLevel customerLevel = getCustomerLevel(customer.getId());
                if (customerLevel == null) {
                    continue;
                }
                NursingLevel level = nursingLevelService.getById(customerLevel.getLevelId());
                if (level == null) {
                    continue;
                }
                List<NursingLevelItem> levelItems = nursingLevelItemService.lambdaQuery()
                        .eq(NursingLevelItem::getLevelId, level.getId())
                        .list();
                for (NursingLevelItem levelItem : levelItems) {
                    NurseItem nurseItem = nurseItemService.getById(levelItem.getItemId());
                    if (nurseItem == null) {
                        continue;
                    }
                    boolean finished = isTaskFinished(nurse.getId(), customer.getId(), nurseItem.getId(), date);
                    if (onlyTodo && finished) {
                        continue;
                    }
                    NursingTask task = new NursingTask();
                    task.setNurseId(nurse.getId());
                    task.setNurseName(nurse.getName());
                    task.setCustomerId(customer.getId());
                    task.setCustomerName(customer.getName());
                    task.setLevelId(level.getId());
                    task.setLevelName(level.getLevelName());
                    task.setItemId(nurseItem.getId());
                    task.setItemName(nurseItem.getItemName());
                    task.setTaskName(nurseItem.getItemName()); // 兼容前端taskName字段
                    task.setTaskDate(date);
                    task.setStatus(finished ? "已完成" : "待护理");
                    tasks.add(task);
                }
            }
        }
        return R.ok(tasks);
    }

    private R<List<NursingTask>> buildTasks(Integer nurseId, String taskDate, boolean onlyTodo) {
        if (!ValidateUtil.isPositive(nurseId)) {
            return R.badRequest("护工ID必须是正整数");
        }
        Nurse nurse = nurseService.getById(nurseId);
        if (nurse == null) {
            return R.notFound("护工不存在");
        }
        String date = ValidateUtil.isBlank(taskDate) ? LocalDate.now().toString() : ValidateUtil.trim(taskDate);
        if (!date.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            return R.badRequest("任务日期格式应为yyyy-MM-dd");
        }

        List<NursingTask> tasks = new ArrayList<>();
        List<NurseCustomer> assignments = nurseCustomerService.lambdaQuery()
                .eq(NurseCustomer::getNurseId, nurseId)
                .list();
        for (NurseCustomer assignment : assignments) {
            Customer customer = customerService.getById(assignment.getCustomerId());
            if (customer == null) {
                continue;
            }
            CustomerNursingLevel customerLevel = getCustomerLevel(customer.getId());
            if (customerLevel == null) {
                continue;
            }
            NursingLevel level = nursingLevelService.getById(customerLevel.getLevelId());
            if (level == null) {
                continue;
            }
            List<NursingLevelItem> levelItems = nursingLevelItemService.lambdaQuery()
                    .eq(NursingLevelItem::getLevelId, level.getId())
                    .list();
            for (NursingLevelItem levelItem : levelItems) {
                NurseItem nurseItem = nurseItemService.getById(levelItem.getItemId());
                if (nurseItem == null) {
                    continue;
                }
                boolean finished = isTaskFinished(nurseId, customer.getId(), nurseItem.getId(), date);
                if (onlyTodo && finished) {
                    continue;
                }
                NursingTask task = new NursingTask();
                task.setNurseId(nurseId);
                task.setNurseName(nurse.getName());
                task.setCustomerId(customer.getId());
                task.setCustomerName(customer.getName());
                task.setLevelId(level.getId());
                task.setLevelName(level.getLevelName());
                task.setItemId(nurseItem.getId());
                task.setItemName(nurseItem.getItemName());
                task.setTaskName(nurseItem.getItemName()); // 兼容前端taskName字段
                task.setTaskDate(date);
                task.setStatus(finished ? "已完成" : "待护理");
                tasks.add(task);
            }
        }
        return R.ok(tasks);
    }

    private CustomerNursingLevel getCustomerLevel(Integer customerId) {
        List<CustomerNursingLevel> list = customerNursingLevelService.lambdaQuery()
                .eq(CustomerNursingLevel::getCustomerId, customerId)
                .list();
        return list.isEmpty() ? null : list.get(0);
    }

    private boolean isTaskFinished(Integer nurseId, Integer customerId, Integer itemId, String date) {
        long count = nursingRecordService.lambdaQuery()
                .eq(NursingRecord::getNurseId, nurseId)
                .eq(NursingRecord::getCustomerId, customerId)
                .eq(NursingRecord::getItemId, itemId)
                .eq(NursingRecord::getRecordDate, date)
                .count();
        return count > 0;
    }
}
