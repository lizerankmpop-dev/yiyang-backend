package com.neusoft.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.entity.HealthRecord;
import com.neusoft.mapper.HealthRecordMapper;
import com.neusoft.service.HealthRecordService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HealthRecordServiceImpl extends ServiceImpl<HealthRecordMapper, HealthRecord> implements HealthRecordService {

    @Override
    public List<HealthRecord> getByCustomerId(Long customerId) {
        LambdaQueryWrapper<HealthRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HealthRecord::getCustomerId, customerId);
        queryWrapper.eq(HealthRecord::getIsDeleted, 0);
        queryWrapper.orderByDesc(HealthRecord::getMeasureTime); // 按测量时间倒序排列

        return this.list(queryWrapper);
    }

}