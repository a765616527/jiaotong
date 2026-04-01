package com.traffic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.traffic.entity.AiCallLog;
import com.traffic.mapper.AiCallLogMapper;
import com.traffic.service.AiLogService;
import com.traffic.util.SecurityUtils;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiLogServiceImpl implements AiLogService {

    private final AiCallLogMapper aiCallLogMapper;

    @Override
    public void log(String endpoint, boolean success, String errorMessage, int inputLength, int outputLength) {
        AiCallLog log = new AiCallLog();
        try {
            log.setOperatorId(SecurityUtils.currentUserId());
        } catch (Exception ignored) {
            log.setOperatorId(0L);
        }
        log.setEndpoint(endpoint);
        log.setSuccess(success ? 1 : 0);
        log.setErrorMessage(errorMessage);
        log.setInputLength(Math.max(0, inputLength));
        log.setOutputLength(Math.max(0, outputLength));
        log.setCalledAt(LocalDateTime.now());
        aiCallLogMapper.insert(log);
    }

    @Override
    public IPage<AiCallLog> page(long pageNum, long pageSize, String endpoint) {
        SecurityUtils.requireAdmin();
        Page<AiCallLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AiCallLog> wrapper = new LambdaQueryWrapper<AiCallLog>()
            .orderByDesc(AiCallLog::getCalledAt);
        if (endpoint != null && !endpoint.isBlank()) {
            wrapper.eq(AiCallLog::getEndpoint, endpoint);
        }
        return aiCallLogMapper.selectPage(page, wrapper);
    }
}
