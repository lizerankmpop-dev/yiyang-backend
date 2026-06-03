package com.neusoft.controller;

import com.neusoft.common.R;
import com.neusoft.entity.Menu;
import com.neusoft.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;

    @GetMapping("/list")
    public R<List<Menu>> list(@RequestParam(required = false) String keyword) {
        return R.ok(menuService.lambdaQuery()
            .like(keyword != null && !keyword.isEmpty(), Menu::getMenuName, keyword)
            .orderByAsc(Menu::getSortOrder)
            .list());
    }
}
