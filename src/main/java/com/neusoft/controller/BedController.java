package com.neusoft.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neusoft.common.R;
import com.neusoft.entity.Bed;
import com.neusoft.mapper.BedMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 床位管理控制器（路径完全匹配前端请求）
 */
@Slf4j
@RestController
@RequestMapping("/bed") // 必须和前端请求的前缀一致：/api/bed
@RequiredArgsConstructor
public class BedController {

    private final BedMapper bedMapper;

    /**
     * 床位列表接口（和前端请求路径 /api/bed/list 完全匹配）
     */
    @GetMapping("/list") // 必须和前端请求的方法路径一致：/list
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String bedNo,
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) Integer status) {
        log.info("【床位管理】分页查询，page={}, size={}, bedNo={}, floor={}, status={}", page, size, bedNo, floor, status);

        try {
            // 1. 构建查询条件
            LambdaQueryWrapper<Bed> wrapper = new LambdaQueryWrapper<>();
            // 床位号模糊查询
            if (StringUtils.hasText(bedNo)) {
                wrapper.like(Bed::getBedNo, bedNo);
            }
            // 楼层筛选（Bed.floor 是 String 类型，需转 String 比较）
            if (floor != null) {
                wrapper.eq(Bed::getFloor, String.valueOf(floor));
            }
            // 状态查询（0-空闲，1-占用）
            if (status != null) {
                wrapper.eq(Bed::getStatus, status);
            }
            // 按房间号排序
            wrapper.orderByAsc(Bed::getRoomNo);

            // 2. 分页查询（MyBatis-Plus 自带分页）
            Page<Bed> pageResult = bedMapper.selectPage(new Page<>(page, size), wrapper);

            // 3. 封装和前端兼容的返回格式
            Map<String, Object> result = new HashMap<>();
            result.put("records", pageResult.getRecords());
            result.put("total", pageResult.getTotal());
            result.put("pages", pageResult.getPages());
            result.put("current", pageResult.getCurrent());

            return R.ok(result);
        } catch (Exception e) {
            // 异常捕获：前端不会挂起，菜单立刻恢复点击
            log.error("床位列表查询失败", e);
            return R.fail("床位数据加载失败：" + e.getMessage());
        }
    }

    // 其他床位操作方法（新增/修改/删除等，根据你的业务补充）
    @PostMapping("/add")
    public R<String> add(@RequestBody Bed bed) {
        bedMapper.insert(bed);
        return R.ok("床位新增成功");
    }

    @PostMapping("/update")
    public R<String> update(@RequestBody Bed bed) {
        bedMapper.updateById(bed);
        return R.ok("床位修改成功");
    }

    @DeleteMapping("/delete/{id}")
    public R<String> delete(@PathVariable Integer id) {
        bedMapper.deleteById(id);
        return R.ok("床位删除成功");
    }

    /**
     * 查询空闲床位列表（供客户新增/编辑时选择）
     */
    @GetMapping("/available")
    public R<List<Bed>> available() {
        log.info("【床位管理】查询空闲床位");
        try {
            LambdaQueryWrapper<Bed> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Bed::getStatus, 0); // 0-空闲
            wrapper.orderByAsc(Bed::getRoomNo, Bed::getBedNo);
            List<Bed> list = bedMapper.selectList(wrapper);
            return R.ok(list);
        } catch (Exception e) {
            log.error("查询空闲床位失败", e);
            return R.fail("查询空闲床位失败：" + e.getMessage());
        }
    }
}
