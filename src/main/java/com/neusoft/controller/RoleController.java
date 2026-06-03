package com.neusoft.controller;

import com.neusoft.common.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neusoft.entity.Role;
import com.neusoft.entity.RoleMenu;
import com.neusoft.service.RoleService;
import com.neusoft.service.RoleMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/role")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;
    private final RoleMenuService roleMenuService;

    @GetMapping("/list")
    public R<Page<Role>> list(@RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "10") Integer size,
                               @RequestParam(required = false) String keyword) {
        return R.ok(roleService.lambdaQuery()
            .like(keyword != null && !keyword.isEmpty(), Role::getRoleName, keyword)
            .page(new Page<>(page, size)));
    }

    @GetMapping("/{id}")
    public R<Role> getById(@PathVariable Integer id) {
        return R.ok(roleService.getById(id));
    }

    @PostMapping
    public R<String> save(@RequestBody Role entity) {
        roleService.save(entity);
        return R.ok("保存成功");
    }

    @PutMapping
    public R<String> update(@RequestBody Role entity) {
        roleService.updateById(entity);
        return R.ok("更新成功");
    }

    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable Integer id) {
        roleService.removeById(id);
        return R.ok("删除成功");
    }

    @GetMapping("/{id}/menus")
    public R<List<Integer>> getRoleMenus(@PathVariable Integer id) {
        List<Integer> menuIds = roleMenuService.lambdaQuery()
            .eq(RoleMenu::getRoleId, id)
            .list()
            .stream()
            .map(RoleMenu::getMenuId)
            .collect(Collectors.toList());
        return R.ok(menuIds);
    }

    @PutMapping("/{id}/menus")
    public R<String> assignRoleMenus(@PathVariable Integer id, @RequestBody List<Integer> menuIds) {
        roleMenuService.lambdaQuery()
            .eq(RoleMenu::getRoleId, id)
            .list()
            .forEach(rm -> roleMenuService.removeById(rm.getId()));
        for (Integer menuId : menuIds) {
            RoleMenu rm = new RoleMenu();
            rm.setRoleId(id);
            rm.setMenuId(menuId);
            roleMenuService.save(rm);
        }
        return R.ok("分配菜单成功");
    }
}
