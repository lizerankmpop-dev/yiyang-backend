package com.neusoft.controller;

import com.neusoft.common.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neusoft.entity.Preference;
import com.neusoft.service.PreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/preference")
@RequiredArgsConstructor
public class PreferenceController {
    private final PreferenceService preferenceService;

    @GetMapping("/list")
    public R<Page<Preference>> list(@RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "10") Integer size,
                                     @RequestParam(required = false) String keyword) {
        return R.ok(preferenceService.lambdaQuery()
            .like(keyword != null && !keyword.isEmpty(), Preference::getContent, keyword)
            .page(new Page<>(page, size)));
    }

    @PostMapping
    public R<String> save(@RequestBody Preference entity) {
        preferenceService.save(entity);
        return R.ok("保存成功");
    }

    @PutMapping
    public R<String> update(@RequestBody Preference entity) {
        preferenceService.updateById(entity);
        return R.ok("更新成功");
    }

    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable Integer id) {
        preferenceService.removeById(id);
        return R.ok("删除成功");
    }
}
