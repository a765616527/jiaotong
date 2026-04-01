package com.traffic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.traffic.entity.SysOperationLog;
import com.traffic.mapper.SysOperationLogMapper;
import com.traffic.service.OperationLogService;
import com.traffic.util.SecurityUtils;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final SysOperationLogMapper logMapper;

    @Override
    public void log(String module, String action, String targetType, String targetId, String detail) {
        logByOperator(SecurityUtils.currentUserId(), module, action, targetType, targetId, detail);
    }

    @Override
    public void logByOperator(Long operatorId, String module, String action, String targetType,
        String targetId, String detail) {
        SysOperationLog log = new SysOperationLog();
        log.setOperatorId(operatorId == null ? 0L : operatorId);
        log.setModule(module);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetail(detail);
        log.setOperatedAt(LocalDateTime.now());
        logMapper.insert(log);
    }

    @Override
    public IPage<SysOperationLog> page(long pageNum, long pageSize, String module) {
        SecurityUtils.requireAdmin();
        Page<SysOperationLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<SysOperationLog>()
            .orderByDesc(SysOperationLog::getOperatedAt);
        if (module != null && !module.isBlank()) {
            wrapper.eq(SysOperationLog::getModule, module);
        }
        return logMapper.selectPage(page, wrapper);
    }
}
