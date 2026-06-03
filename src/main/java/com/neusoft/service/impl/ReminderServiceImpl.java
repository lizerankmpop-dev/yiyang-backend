package com.neusoft.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.entity.ReminderMessage;
import com.neusoft.mapper.ReminderMapper;
import com.neusoft.service.ReminderService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReminderServiceImpl extends ServiceImpl<ReminderMapper, ReminderMessage> implements ReminderService {

    @Override
    public List<ReminderMessage> listByReceiver(String receiver) {
        return baseMapper.selectByReceiver(receiver);
    }

    @Override
    public List<ReminderMessage> listUnreadByReceiver(String receiver) {
        return baseMapper.selectUnreadByReceiver(receiver);
    }

    @Override
    public Integer countUnreadByReceiver(String receiver) {
        Integer count = baseMapper.countUnreadByReceiver(receiver);
        return count == null ? 0 : count;
    }

    @Override
    public boolean markAsRead(Long id) {
        return baseMapper.markAsRead(id) > 0;
    }

    @Override
    public boolean markAllAsRead(String receiver) {
        return baseMapper.markAllAsRead(receiver) >= 0;
    }
}
