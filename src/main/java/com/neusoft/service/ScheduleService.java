package com.neusoft.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.entity.Schedule;
import com.neusoft.mapper.ScheduleMapper;
import org.springframework.stereotype.Service;

@Service
public class ScheduleService extends ServiceImpl<ScheduleMapper, Schedule> {
}
