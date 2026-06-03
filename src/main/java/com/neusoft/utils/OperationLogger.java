package com.neusoft.utils;

import com.neusoft.entity.OperationLog;
import com.neusoft.mapper.OperationLogMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 操作日志工具类，在 Controller 中注入后即可快捷记录操作
 */
@Component
public class OperationLogger {
    private final OperationLogMapper mapper;

    public OperationLogger(OperationLogMapper mapper) {
        this.mapper = mapper;
    }

    public void log(Integer adminId, String adminName, String operation, String target, String detail) {
        OperationLog log = new OperationLog();
        log.setAdminId(adminId);
        log.setAdminName(adminName);
        log.setOperation(operation);
        log.setTarget(target);
        log.setDetail(detail);
        log.setCreateTime(LocalDateTime.now());
        mapper.insert(log);
    }

    /** 无详情版本 */
    public void log(Integer adminId, String adminName, String operation, String target) {
        log(adminId, adminName, operation, target, null);
    }
}
