package com.neusoft.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neusoft.entity.ReminderMessage;

import java.util.List;

public interface ReminderService extends IService<ReminderMessage> {

    List<ReminderMessage> listByReceiver(String receiver);

    List<ReminderMessage> listUnreadByReceiver(String receiver);

    Integer countUnreadByReceiver(String receiver);

    boolean markAsRead(Long id);

    boolean markAllAsRead(String receiver);
}
