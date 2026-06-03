package com.neusoft.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.entity.ReminderMessage;
import com.neusoft.mapper.ReminderMessageMapper;
import org.springframework.stereotype.Service;

@Service
public class ReminderMessageService extends ServiceImpl<ReminderMessageMapper, ReminderMessage> {
}
