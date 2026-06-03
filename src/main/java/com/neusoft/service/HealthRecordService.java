package com.neusoft.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neusoft.entity.HealthRecord;
import java.util.List;

public interface HealthRecordService extends IService<HealthRecord> {

    /**
     * 根据老人ID查询健康记录列表
     * @param customerId 老人ID
     * @return 健康记录列表
     */
    List<HealthRecord> getByCustomerId(Long customerId);

}