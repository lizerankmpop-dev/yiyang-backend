package com.neusoft.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neusoft.common.R;
import com.neusoft.entity.*;
import com.neusoft.mapper.NursingTaskMapper;
import com.neusoft.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 护工 Controller
 */
@RestController
@RequestMapping("/api/nurse")
@RequiredArgsConstructor
public class NurseController {

    private final NurseService nurseService;
    private final NursingTaskMapper nursingTaskMapper;
    private final NurseCustomerService nurseCustomerService;
    private final CustomerService customerService;
    private final CustomerNursingLevelService customerNursingLevelService;
    private final NursingLevelService nursingLevelService;
    private final NursingLevelItemService nursingLevelItemService;
    private final NurseItemService nurseItemService;

    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Nurse> pageParam = new Page<>(page, size);
        Page<Nurse> result = nurseService.page(pageParam);
        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        return R.ok(data);
    }

    @GetMapping("/{id}")
    public R<Nurse> getById(@PathVariable Integer id) {
        Nurse nurse = nurseService.getById(id);
        if (nurse == null) return R.fail("护工不存在");
        return R.ok(nurse);
    }

    @PostMapping("/add")
    public R<String> add(@RequestBody Nurse nurse) {
        if (nurse.getName() == null || nurse.getName().trim().isEmpty()) {
            return R.fail("护工姓名不能为空");
        }
        nurseService.save(nurse);
        return R.ok("添加成功");
    }

    @PutMapping("/update")
    public R<String> update(@RequestBody Nurse nurse) {
        if (nurse.getId() == null) return R.fail("ID不能为空");
        nurseService.updateById(nurse);
        return R.ok("更新成功");
    }

    @DeleteMapping("/delete/{id}")
    public R<String> delete(@PathVariable Integer id) {
        if (nurseService.getById(id) == null) return R.fail("护工不存在");
        nurseService.removeById(id);
        return R.ok("删除成功");
    }

    /**
     * 新增任务安排（存储到 nursing_task 表）
     */
    @PostMapping("/task/add")
    public R<String> addTask(@RequestBody NursingTask task) {
        if (task.getNurseId() == null || task.getCustomerId() == null
                || task.getItemId() == null || task.getTaskDate() == null || task.getTaskDate().trim().isEmpty()) {
            return R.fail("护工、客户、护理项目、任务日期不能为空");
        }
        // 补充关联名称
        Nurse nurse = nurseService.getById(task.getNurseId());
        if (nurse != null) task.setNurseName(nurse.getName());
        Customer customer = customerService.getById(task.getCustomerId());
        if (customer != null) task.setCustomerName(customer.getName());
        if (task.getLevelId() != null) {
            NursingLevel level = nursingLevelService.getById(task.getLevelId());
            if (level != null) task.setLevelName(level.getLevelName());
        }
        NurseItem item = nurseItemService.getById(task.getItemId());
        if (item != null) {
            task.setItemName(item.getItemName());
            task.setTaskName(item.getItemName());
        }
        if (task.getStatus() == null || task.getStatus().trim().isEmpty()) {
            task.setStatus("待护理");
        }
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        nursingTaskMapper.insert(task);
        return R.ok("任务已安排");
    }

    /**
     * 删除任务安排
     */
    @DeleteMapping("/task/delete/{id}")
    public R<String> deleteTask(@PathVariable Integer id) {
        nursingTaskMapper.deleteById(id);
        return R.ok("已删除");
    }

    /**
     * 获取指定客户的护理任务历史（已存储的 nursing_task 记录）
     */
    @GetMapping("/task/by-customer/{customerId}")
    public R<List<NursingTask>> getTasksByCustomer(@PathVariable Integer customerId) {
        List<NursingTask> tasks = nursingTaskMapper.selectList(
                new LambdaQueryWrapper<NursingTask>()
                        .eq(NursingTask::getCustomerId, customerId)
                        .orderByDesc(NursingTask::getTaskDate)
                        .last("LIMIT 50")
        );
        // 补充关联名称
        for (NursingTask t : tasks) {
            Nurse n = nurseService.getById(t.getNurseId());
            if (n != null) t.setNurseName(n.getName());
            NurseItem item = nurseItemService.getById(t.getItemId());
            if (item != null) {
                t.setItemName(item.getItemName());
                t.setTaskName(item.getItemName());
            }
            Customer c = customerService.getById(t.getCustomerId());
            if (c != null) t.setCustomerName(c.getName());
        }
        return R.ok(tasks);
    }

    /**
     * 护工排班查询
     * 合并存储任务 + 动态生成任务，确保数据库变动实时同步到前端
     */
    @GetMapping("/schedule")
    public R<Map<String, Object>> schedule(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        // 默认显示本周
        LocalDate now = LocalDate.now();
        LocalDate monday = now.with(java.time.DayOfWeek.MONDAY);
        LocalDate sunday = now.with(java.time.DayOfWeek.SUNDAY);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String start = (startDate != null && !startDate.trim().isEmpty()) ? startDate : monday.format(dtf);
        String end = (endDate != null && !endDate.trim().isEmpty()) ? endDate : sunday.format(dtf);

        // 生成日期列表
        LocalDate startD = LocalDate.parse(start, dtf);
        LocalDate endD = LocalDate.parse(end, dtf);
        List<String> dates = new ArrayList<>();
        LocalDate cur = startD;
        while (!cur.isAfter(endD)) {
            dates.add(cur.format(dtf));
            cur = cur.plusDays(1);
        }

        // 获取所有护工
        List<Nurse> nurses = nurseService.list();

        // ===== 1. 从数据库获取已存储的任务 =====
        List<NursingTask> storedTasks = nursingTaskMapper.selectList(
                new LambdaQueryWrapper<NursingTask>()
                        .ge(NursingTask::getTaskDate, start)
                        .le(NursingTask::getTaskDate, end)
                        .orderByAsc(NursingTask::getTaskTime)
        );

        // 去重键: nurseId_customerId_levelId_itemId_taskDate
        Set<String> storedKeys = storedTasks.stream()
                .map(t -> t.getNurseId() + "_" + t.getCustomerId() + "_" + t.getLevelId() + "_" + t.getItemId() + "_" + t.getTaskDate())
                .collect(Collectors.toSet());

        // 按 (nurseId, taskDate) 分组存储任务
        Map<String, List<NursingTask>> storedTaskMap = storedTasks.stream()
                .collect(Collectors.groupingBy(t -> t.getNurseId() + "_" + t.getTaskDate()));

        // ===== 2. 动态生成任务（基于护工分配 + 护理级别 + 护理项目） =====
        List<NursingTask> dynamicTasks = new ArrayList<>();
        for (Nurse nurse : nurses) {
            List<NurseCustomer> assignments = nurseCustomerService.lambdaQuery()
                    .eq(NurseCustomer::getNurseId, nurse.getId())
                    .list();
            for (NurseCustomer assignment : assignments) {
                Customer customer = customerService.getById(assignment.getCustomerId());
                if (customer == null) continue;

                List<CustomerNursingLevel> levelList = customerNursingLevelService.lambdaQuery()
                        .eq(CustomerNursingLevel::getCustomerId, customer.getId())
                        .list();
                if (levelList.isEmpty()) continue;
                CustomerNursingLevel customerLevel = levelList.get(0);

                NursingLevel level = nursingLevelService.getById(customerLevel.getLevelId());
                if (level == null) continue;

                List<NursingLevelItem> levelItems = nursingLevelItemService.lambdaQuery()
                        .eq(NursingLevelItem::getLevelId, level.getId())
                        .list();

                for (NursingLevelItem levelItem : levelItems) {
                    NurseItem nurseItem = nurseItemService.getById(levelItem.getItemId());
                    if (nurseItem == null) continue;

                    // 每天的动态任务
                    for (String date : dates) {
                        String dedupKey = nurse.getId() + "_" + customer.getId() + "_" + level.getId() + "_" + nurseItem.getId() + "_" + date;
                        if (storedKeys.contains(dedupKey)) continue; // 跳过已有存储任务的

                        NursingTask task = new NursingTask();
                        task.setNurseId(nurse.getId());
                        task.setNurseName(nurse.getName());
                        task.setCustomerId(customer.getId());
                        task.setCustomerName(customer.getName());
                        task.setLevelId(level.getId());
                        task.setLevelName(level.getLevelName());
                        task.setItemId(nurseItem.getId());
                        task.setItemName(nurseItem.getItemName());
                        task.setTaskName(nurseItem.getItemName());
                        task.setTaskDate(date);
                        task.setStatus("待护理");
                        dynamicTasks.add(task);
                    }
                }
            }
        }

        // 按 (nurseId, taskDate) 分组动态任务
        Map<String, List<NursingTask>> dynamicTaskMap = dynamicTasks.stream()
                .collect(Collectors.groupingBy(t -> t.getNurseId() + "_" + t.getTaskDate()));

        // 预加载客户和护理项目信息（避免N+1查询）
        Map<Integer, Customer> customerMap = customerService.list().stream()
                .filter(c -> c.getId() != null)
                .collect(Collectors.toMap(Customer::getId, c -> c, (a, b) -> a));
        Map<Integer, NurseItem> itemMap = nurseItemService.list().stream()
                .filter(i -> i.getId() != null)
                .collect(Collectors.toMap(NurseItem::getId, i -> i, (a, b) -> a));

        // ===== 3. 构建返回数据（合并存储+动态） =====
        List<Map<String, Object>> nurseSchedules = new ArrayList<>();
        for (Nurse nurse : nurses) {
            Map<String, Object> ns = new LinkedHashMap<>();
            ns.put("id", nurse.getId());
            ns.put("name", nurse.getName());
            ns.put("phone", nurse.getPhone());
            ns.put("status", nurse.getStatus());

            Map<String, Object> dailySchedule = new LinkedHashMap<>();
            int totalTasks = 0;
            for (String date : dates) {
                String key = nurse.getId() + "_" + date;
                List<NursingTask> storedDay = storedTaskMap.getOrDefault(key, Collections.emptyList());
                List<NursingTask> dynamicDay = dynamicTaskMap.getOrDefault(key, Collections.emptyList());

                List<NursingTask> allDay = new ArrayList<>();
                allDay.addAll(storedDay);
                allDay.addAll(dynamicDay);

                // 补充客户详情和护理项目详情
                // 按任务时间排序
                allDay.sort(Comparator.comparing(NursingTask::getTaskTime, Comparator.nullsLast(Comparator.naturalOrder())));

                List<Map<String, Object>> enrichedTasks = new ArrayList<>();
                for (NursingTask task : allDay) {
                    Map<String, Object> t = new LinkedHashMap<>();
                    t.put("id", task.getId());
                    t.put("nurseId", task.getNurseId());
                    t.put("nurseName", task.getNurseName());
                    t.put("customerId", task.getCustomerId());
                    t.put("customerName", task.getCustomerName());
                    t.put("levelId", task.getLevelId());
                    t.put("levelName", task.getLevelName());
                    t.put("itemId", task.getItemId());
                    t.put("itemName", task.getItemName());
                    t.put("taskName", task.getTaskName());
                    t.put("taskDate", task.getTaskDate());
                    t.put("taskTime", task.getTaskTime());
                    t.put("status", task.getStatus());
                    t.put("remark", task.getRemark());

                    // 补充客户详情
                    Customer c = customerMap.get(task.getCustomerId());
                    if (c != null) {
                        // 如果task中没有customerName，从customerMap补充
                        Object cn = t.get("customerName");
                        if (cn == null || (cn instanceof String && ((String) cn).trim().isEmpty())) {
                            t.put("customerName", c.getName());
                        }
                        t.put("customerAge", c.getAge());
                        t.put("customerGender", c.getGender());
                        t.put("customerBedNo", c.getBedId() != null ? c.getBedId().toString() : null);
                    } else {
                        // customerMap中找不到，尝试直接查询
                        Customer direct = customerService.getById(task.getCustomerId());
                        if (direct != null) {
                            t.put("customerName", direct.getName());
                            t.put("customerAge", direct.getAge());
                            t.put("customerGender", direct.getGender());
                            t.put("customerBedNo", direct.getBedId() != null ? direct.getBedId().toString() : null);
                        }
                    }

                    // 补充护理项目详情
                    NurseItem item = itemMap.get(task.getItemId());
                    if (item != null) {
                        t.put("itemCategory", item.getCategory());
                        t.put("itemDescription", item.getDescription());
                        t.put("itemUnit", item.getUnit());
                    }

                    enrichedTasks.add(t);
                }

                int count = enrichedTasks.size();
                totalTasks += count;
                Map<String, Object> dayInfo = new HashMap<>();
                dayInfo.put("count", count);
                dayInfo.put("tasks", enrichedTasks);
                dailySchedule.put(date, dayInfo);
            }
            ns.put("schedule", dailySchedule);
            ns.put("totalTasks", totalTasks);
            nurseSchedules.add(ns);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("dates", dates);
        result.put("nurses", nurseSchedules);
        result.put("totalNurses", nurses.size());

        return R.ok(result);
    }
}
