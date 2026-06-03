package com.neusoft.controller;

import com.neusoft.common.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neusoft.entity.Meal;
import com.neusoft.entity.MealFood;
import com.neusoft.service.MealService;
import com.neusoft.service.MealFoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meal")
@RequiredArgsConstructor
public class MealController {
    private final MealService mealService;
    private final MealFoodService mealFoodService;

    @GetMapping("/list")
    public R<Page<Meal>> list(@RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "10") Integer size,
                               @RequestParam(required = false) String keyword) {
        return R.ok(mealService.lambdaQuery()
            .like(keyword != null && !keyword.isEmpty(), Meal::getMealType, keyword)
            .page(new Page<>(page, size)));
    }

    @GetMapping("/{id}")
    public R<Meal> getById(@PathVariable Integer id) {
        return R.ok(mealService.getById(id));
    }

    @GetMapping("/{id}/foods")
    public R<List<MealFood>> getMealFoods(@PathVariable Integer id) {
        List<MealFood> foods = mealFoodService.lambdaQuery()
                .eq(MealFood::getMealId, id)
                .list();
        return R.ok(foods);
    }

    @PostMapping
    public R<String> save(@RequestBody Meal entity) {
        mealService.save(entity);
        return R.ok("保存成功");
    }

    @PutMapping
    public R<String> update(@RequestBody Meal entity) {
        mealService.updateById(entity);
        return R.ok("更新成功");
    }

    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable Integer id) {
        mealService.removeById(id);
        return R.ok("删除成功");
    }
}
