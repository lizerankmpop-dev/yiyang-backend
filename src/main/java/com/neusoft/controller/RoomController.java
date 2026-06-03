package com.neusoft.controller;

import com.neusoft.common.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neusoft.entity.Room;
import com.neusoft.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/room")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    @GetMapping("/list")
    public R<Page<Room>> list(@RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "10") Integer size,
                               @RequestParam(required = false) String keyword) {
        return R.ok(roomService.lambdaQuery()
            .like(keyword != null && !keyword.isEmpty(), Room::getRoomNo, keyword)
            .page(new Page<>(page, size)));
    }

    @PostMapping
    public R<String> save(@RequestBody Room entity) {
        roomService.save(entity);
        return R.ok("保存成功");
    }

    @PutMapping
    public R<String> update(@RequestBody Room entity) {
        roomService.updateById(entity);
        return R.ok("更新成功");
    }

    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable Integer id) {
        roomService.removeById(id);
        return R.ok("删除成功");
    }
}
