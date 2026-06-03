package com.neusoft.controller;

import com.neusoft.common.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neusoft.entity.Schedule;
import com.neusoft.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {
    private final ScheduleService scheduleService;

    @GetMapping("/list")
    public R<Page<Schedule>> list(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer size,
                                   @RequestParam(required = false) String keyword) {
        return R.ok(scheduleService.lambdaQuery()
            .like(keyword != null && !keyword.isEmpty(), Schedule::getNurseName, keyword)
            .page(new Page<>(page, size)));
    }

    @GetMapping("/{id}")
    public R<Schedule> getById(@PathVariable Integer id) {
        return R.ok(scheduleService.getById(id));
    }

    @PostMapping
    public R<String> save(@RequestBody Schedule entity) {
        scheduleService.save(entity);
        return R.ok("保存成功");
    }

    @PutMapping
    public R<String> update(@RequestBody Schedule entity) {
        scheduleService.updateById(entity);
        return R.ok("更新成功");
    }

    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable Integer id) {
        scheduleService.removeById(id);
        return R.ok("删除成功");
    }
}
