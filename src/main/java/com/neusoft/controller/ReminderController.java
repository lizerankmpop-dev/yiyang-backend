package com.neusoft.controller;

import com.neusoft.common.R;
import com.neusoft.entity.ReminderMessage;
import com.neusoft.service.ReminderService;
import com.neusoft.utils.ValidateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reminder")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @PostMapping("/send")
    public R<String> send(@RequestBody ReminderMessage message) {
        R<String> error = validateMessage(message);
        if (error != null) {
            return error;
        }
        message.setTitle(ValidateUtil.trim(message.getTitle()));
        message.setContent(ValidateUtil.trim(message.getContent()));
        message.setReceiver(ValidateUtil.trim(message.getReceiver()));
        message.setType(message.getType() == null ? 0 : message.getType());
        message.setIsRead(false);
        message.setCreateTime(LocalDateTime.now());
        reminderService.save(message);
        return R.ok("消息发送成功");
    }

    @GetMapping("/list")
    public R<List<ReminderMessage>> list(@RequestParam String receiver,
                                         @RequestParam(required = false, defaultValue = "false") Boolean onlyUnread) {
        if (ValidateUtil.isBlank(receiver)) {
            return R.fail("接收人不能为空");
        }
        String username = ValidateUtil.trim(receiver);
        List<ReminderMessage> messages = Boolean.TRUE.equals(onlyUnread)
                ? reminderService.listUnreadByReceiver(username)
                : reminderService.listByReceiver(username);
        return R.ok(messages);
    }

    @GetMapping("/unread")
    public R<List<ReminderMessage>> unread(@RequestParam String receiver) {
        if (ValidateUtil.isBlank(receiver)) {
            return R.fail("接收人不能为空");
        }
        return R.ok(reminderService.listUnreadByReceiver(ValidateUtil.trim(receiver)));
    }

    @GetMapping("/unreadCount")
    public R<Integer> unreadCount(@RequestParam String receiver) {
        if (ValidateUtil.isBlank(receiver)) {
            return R.fail("接收人不能为空");
        }
        return R.ok(reminderService.countUnreadByReceiver(ValidateUtil.trim(receiver)));
    }

    @PostMapping("/read/{id}")
    public R<String> read(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return R.fail("消息ID无效");
        }
        if (reminderService.getById(id) == null) {
            return R.fail("消息不存在");
        }
        reminderService.markAsRead(id);
        return R.ok("消息已读");
    }

    @PostMapping("/readAll")
    public R<String> readAll(@RequestParam String receiver) {
        if (ValidateUtil.isBlank(receiver)) {
            return R.fail("接收人不能为空");
        }
        reminderService.markAllAsRead(ValidateUtil.trim(receiver));
        return R.ok("全部消息已读");
    }

    @DeleteMapping("/delete/{id}")
    public R<String> delete(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return R.fail("消息ID无效");
        }
        if (reminderService.getById(id) == null) {
            return R.fail("消息不存在");
        }
        reminderService.removeById(id);
        return R.ok("删除成功");
    }

    private R<String> validateMessage(ReminderMessage message) {
        if (message == null) {
            return R.fail("消息内容不能为空");
        }
        if (!ValidateUtil.lengthBetween(message.getTitle(), 1, 50)) {
            return R.fail("消息标题不能为空，且长度不能超过50个字符");
        }
        if (!ValidateUtil.lengthBetween(message.getContent(), 1, 500)) {
            return R.fail("消息内容不能为空，且长度不能超过500个字符");
        }
        if (!ValidateUtil.lengthBetween(message.getReceiver(), 1, 50)) {
            return R.fail("接收人不能为空，且长度不能超过50个字符");
        }
        return null;
    }
}
