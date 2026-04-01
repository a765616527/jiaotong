package com.traffic.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.traffic.entity.SysOperationLog;

public interface OperationLogService {

    void log(String module, String action, String targetType, String targetId, String detail);

    void logByOperator(Long operatorId, String module, String action, String targetType, String targetId,
        String detail);

    IPage<SysOperationLog> page(long pageNum, long pageSize, String module);
}
