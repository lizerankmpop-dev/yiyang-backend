package com.neusoft.controller;

import com.neusoft.common.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neusoft.entity.Food;
import com.neusoft.service.FoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/food")
@RequiredArgsConstructor
public class FoodController {
    private final FoodService foodService;

    @GetMapping("/list")
    public R<Page<Food>> list(@RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "10") Integer size,
                               @RequestParam(required = false) String keyword) {
        return R.ok(foodService.lambdaQuery()
            .like(keyword != null && !keyword.isEmpty(), Food::getFoodName, keyword)
            .page(new Page<>(page, size)));
    }

    @PostMapping
    public R<String> save(@RequestBody Food entity) {
        foodService.save(entity);
        return R.ok("保存成功");
    }

    @PutMapping
    public R<String> update(@RequestBody Food entity) {
        foodService.updateById(entity);
        return R.ok("更新成功");
    }

    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable Integer id) {
        foodService.removeById(id);
        return R.ok("删除成功");
    }
}
