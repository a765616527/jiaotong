package com.traffic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.traffic.entity.RuleChangeLog;
import com.traffic.mapper.RuleChangeLogMapper;
import com.traffic.service.RuleChangeLogService;
import com.traffic.util.SecurityUtils;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RuleChangeLogServiceImpl implements RuleChangeLogService {

    private final RuleChangeLogMapper ruleChangeLogMapper;

    @Override
    public void logChange(String ruleKey, String oldValue, String newValue, String note) {
        RuleChangeLog log = new RuleChangeLog();
        log.setRuleKey(ruleKey);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setOperatorId(SecurityUtils.currentUserId());
        log.setNote(note);
        log.setChangedAt(LocalDateTime.now());
        ruleChangeLogMapper.insert(log);
    }

    @Override
    public IPage<RuleChangeLog> page(long pageNum, long pageSize, String ruleKey,
        LocalDateTime startTime, LocalDateTime endTime) {
        SecurityUtils.requireAdmin();
        Page<RuleChangeLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<RuleChangeLog> wrapper = new LambdaQueryWrapper<RuleChangeLog>()
            .orderByDesc(RuleChangeLog::getChangedAt);
        if (ruleKey != null && !ruleKey.isBlank()) {
            wrapper.eq(RuleChangeLog::getRuleKey, ruleKey);
        }
        if (startTime != null) {
            wrapper.ge(RuleChangeLog::getChangedAt, startTime);
        }
        if (endTime != null) {
            wrapper.le(RuleChangeLog::getChangedAt, endTime);
        }
        return ruleChangeLogMapper.selectPage(page, wrapper);
    }
}
