package com.traffic.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.traffic.entity.RuleChangeLog;
import java.time.LocalDateTime;

public interface RuleChangeLogService {

    void logChange(String ruleKey, String oldValue, String newValue, String note);

    IPage<RuleChangeLog> page(long pageNum, long pageSize, String ruleKey, LocalDateTime startTime,
        LocalDateTime endTime);
}
