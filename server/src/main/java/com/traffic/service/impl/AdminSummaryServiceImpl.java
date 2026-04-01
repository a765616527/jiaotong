package com.traffic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.traffic.entity.AccidentRecord;
import com.traffic.entity.AiCallLog;
import com.traffic.entity.DictItem;
import com.traffic.entity.SysOperationLog;
import com.traffic.entity.SysUser;
import com.traffic.entity.WarningEvent;
import com.traffic.entity.WarningRuleConfig;
import com.traffic.enums.WarningStatus;
import com.traffic.mapper.AccidentRecordMapper;
import com.traffic.mapper.AiCallLogMapper;
import com.traffic.mapper.DictItemMapper;
import com.traffic.mapper.SysOperationLogMapper;
import com.traffic.mapper.SysUserMapper;
import com.traffic.mapper.WarningEventMapper;
import com.traffic.mapper.WarningRuleConfigMapper;
import com.traffic.service.AdminSummaryService;
import com.traffic.util.SecurityUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminSummaryServiceImpl implements AdminSummaryService {

    private final SysUserMapper sysUserMapper;
    private final AccidentRecordMapper accidentRecordMapper;
    private final WarningEventMapper warningEventMapper;
    private final WarningRuleConfigMapper warningRuleConfigMapper;
    private final DictItemMapper dictItemMapper;
    private final AiCallLogMapper aiCallLogMapper;
    private final SysOperationLogMapper sysOperationLogMapper;

    @Override
    public Map<String, Object> summary() {
        SecurityUtils.requireAdmin();
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

        Long userTotal = sysUserMapper.selectCount(new LambdaQueryWrapper<>());
        Long adminCount = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getRoleCode, "ROLE_ADMIN"));
        Long officerCount = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getRoleCode, "ROLE_TRAFFIC_OFFICER"));
        Long enabledUserCount = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getEnabled, 1));

        Long accidentTotal = accidentRecordMapper.selectCount(new LambdaQueryWrapper<>());
        Long warningTotal = warningEventMapper.selectCount(new LambdaQueryWrapper<>());
        Long activeWarningCount = warningEventMapper.selectCount(new LambdaQueryWrapper<WarningEvent>()
            .in(WarningEvent::getStatus,
                WarningStatus.DRAFT.name(),
                WarningStatus.CONFIRMED.name(),
                WarningStatus.PUBLISHED.name(),
                WarningStatus.PROCESSING.name()));
        Long resolvedWarningCount = warningEventMapper.selectCount(new LambdaQueryWrapper<WarningEvent>()
            .eq(WarningEvent::getStatus, WarningStatus.RESOLVED.name()));

        Long ruleTotal = warningRuleConfigMapper.selectCount(new LambdaQueryWrapper<WarningRuleConfig>());
        Long dictTotal = dictItemMapper.selectCount(new LambdaQueryWrapper<DictItem>());
        Long enabledDictCount = dictItemMapper.selectCount(new LambdaQueryWrapper<DictItem>()
            .eq(DictItem::getEnabled, 1));

        Long aiCallTotal = aiCallLogMapper.selectCount(new LambdaQueryWrapper<AiCallLog>());
        Long aiSuccessTotal = aiCallLogMapper.selectCount(new LambdaQueryWrapper<AiCallLog>()
            .eq(AiCallLog::getSuccess, 1));
        Long todayAiCallCount = aiCallLogMapper.selectCount(new LambdaQueryWrapper<AiCallLog>()
            .ge(AiCallLog::getCalledAt, startOfDay));

        Long operationLogTotal = sysOperationLogMapper.selectCount(new LambdaQueryWrapper<SysOperationLog>());
        Long todayLoginCount = sysOperationLogMapper.selectCount(new LambdaQueryWrapper<SysOperationLog>()
            .eq(SysOperationLog::getModule, "AUTH")
            .eq(SysOperationLog::getAction, "LOGIN")
            .ge(SysOperationLog::getOperatedAt, startOfDay));
        Long todayLoginFailCount = sysOperationLogMapper.selectCount(new LambdaQueryWrapper<SysOperationLog>()
            .eq(SysOperationLog::getModule, "AUTH")
            .eq(SysOperationLog::getAction, "LOGIN_FAIL")
            .ge(SysOperationLog::getOperatedAt, startOfDay));

        Map<String, Object> summary = new HashMap<>();
        summary.put("userTotal", safe(userTotal));
        summary.put("adminCount", safe(adminCount));
        summary.put("officerCount", safe(officerCount));
        summary.put("enabledUserCount", safe(enabledUserCount));
        summary.put("accidentTotal", safe(accidentTotal));
        summary.put("warningTotal", safe(warningTotal));
        summary.put("activeWarningCount", safe(activeWarningCount));
        summary.put("resolvedWarningCount", safe(resolvedWarningCount));
        summary.put("ruleTotal", safe(ruleTotal));
        summary.put("dictTotal", safe(dictTotal));
        summary.put("enabledDictCount", safe(enabledDictCount));
        summary.put("aiCallTotal", safe(aiCallTotal));
        summary.put("aiSuccessTotal", safe(aiSuccessTotal));
        summary.put("todayAiCallCount", safe(todayAiCallCount));
        summary.put("operationLogTotal", safe(operationLogTotal));
        summary.put("todayLoginCount", safe(todayLoginCount));
        summary.put("todayLoginFailCount", safe(todayLoginFailCount));
        return summary;
    }

    @Override
    public Map<String, Object> trend(int days) {
        SecurityUtils.requireAdmin();
        int safeDays = Math.max(7, Math.min(days, 30));
        LocalDate today = LocalDate.now();
        LocalDateTime currentStart = today.minusDays(safeDays - 1L).atStartOfDay();
        LocalDateTime previousStart = today.minusDays(safeDays * 2L - 1L).atStartOfDay();

        List<String> dates = new ArrayList<>();
        Map<String, Integer> accidentCountMap = new LinkedHashMap<>();
        Map<String, Integer> warningCreatedCountMap = new LinkedHashMap<>();
        Map<String, Integer> loginCountMap = new LinkedHashMap<>();
        Map<String, Integer> loginFailCountMap = new LinkedHashMap<>();
        for (int i = safeDays - 1; i >= 0; i--) {
            String d = today.minusDays(i).toString();
            dates.add(d);
            accidentCountMap.put(d, 0);
            warningCreatedCountMap.put(d, 0);
            loginCountMap.put(d, 0);
            loginFailCountMap.put(d, 0);
        }

        int accidentCurrent = 0;
        int accidentPrevious = 0;
        List<AccidentRecord> accidents = accidentRecordMapper.selectList(
            new LambdaQueryWrapper<AccidentRecord>().ge(AccidentRecord::getOccurTime, previousStart));
        for (AccidentRecord row : accidents) {
            if (row.getOccurTime() == null) {
                continue;
            }
            if (!row.getOccurTime().isBefore(currentStart)) {
                accidentCurrent++;
            } else {
                accidentPrevious++;
            }
            String key = row.getOccurTime().toLocalDate().toString();
            if (accidentCountMap.containsKey(key)) {
                accidentCountMap.put(key, accidentCountMap.get(key) + 1);
            }
        }

        int warningCurrent = 0;
        int warningPrevious = 0;
        List<WarningEvent> warnings = warningEventMapper.selectList(
            new LambdaQueryWrapper<WarningEvent>().ge(WarningEvent::getCreatedAt, previousStart));
        for (WarningEvent row : warnings) {
            if (row.getCreatedAt() == null) {
                continue;
            }
            if (!row.getCreatedAt().isBefore(currentStart)) {
                warningCurrent++;
            } else {
                warningPrevious++;
            }
            String key = row.getCreatedAt().toLocalDate().toString();
            if (warningCreatedCountMap.containsKey(key)) {
                warningCreatedCountMap.put(key, warningCreatedCountMap.get(key) + 1);
            }
        }

        int loginCurrent = 0;
        int loginPrevious = 0;
        int loginFailCurrent = 0;
        int loginFailPrevious = 0;
        List<SysOperationLog> logs = sysOperationLogMapper.selectList(
            new LambdaQueryWrapper<SysOperationLog>()
                .eq(SysOperationLog::getModule, "AUTH")
                .in(SysOperationLog::getAction, "LOGIN", "LOGIN_FAIL")
                .ge(SysOperationLog::getOperatedAt, previousStart));
        for (SysOperationLog row : logs) {
            if (row.getOperatedAt() == null) {
                continue;
            }
            String key = row.getOperatedAt().toLocalDate().toString();
            boolean inCurrent = !row.getOperatedAt().isBefore(currentStart);
            if ("LOGIN".equals(row.getAction()) && loginCountMap.containsKey(key)) {
                loginCountMap.put(key, loginCountMap.get(key) + 1);
                if (inCurrent) {
                    loginCurrent++;
                } else {
                    loginPrevious++;
                }
            }
            if ("LOGIN_FAIL".equals(row.getAction()) && loginFailCountMap.containsKey(key)) {
                loginFailCountMap.put(key, loginFailCountMap.get(key) + 1);
                if (inCurrent) {
                    loginFailCurrent++;
                } else {
                    loginFailPrevious++;
                }
            }
        }

        Map<String, Object> compare = new HashMap<>();
        compare.put("accidentCurrent", accidentCurrent);
        compare.put("accidentPrevious", accidentPrevious);
        compare.put("accidentRate", calcRate(accidentCurrent, accidentPrevious));
        compare.put("warningCurrent", warningCurrent);
        compare.put("warningPrevious", warningPrevious);
        compare.put("warningRate", calcRate(warningCurrent, warningPrevious));
        compare.put("loginCurrent", loginCurrent);
        compare.put("loginPrevious", loginPrevious);
        compare.put("loginRate", calcRate(loginCurrent, loginPrevious));
        compare.put("loginFailCurrent", loginFailCurrent);
        compare.put("loginFailPrevious", loginFailPrevious);
        compare.put("loginFailRate", calcRate(loginFailCurrent, loginFailPrevious));

        Map<String, Object> trend = new HashMap<>();
        trend.put("days", safeDays);
        trend.put("dates", dates);
        trend.put("accidentCounts", toSeries(dates, accidentCountMap));
        trend.put("warningCreatedCounts", toSeries(dates, warningCreatedCountMap));
        trend.put("loginCounts", toSeries(dates, loginCountMap));
        trend.put("loginFailCounts", toSeries(dates, loginFailCountMap));
        trend.put("compare", compare);
        return trend;
    }

    private long safe(Long count) {
        return count == null ? 0L : count;
    }

    private List<Integer> toSeries(List<String> dates, Map<String, Integer> valueMap) {
        List<Integer> values = new ArrayList<>();
        for (String date : dates) {
            values.add(valueMap.getOrDefault(date, 0));
        }
        return values;
    }

    private Double calcRate(int current, int previous) {
        if (previous == 0) {
            return null;
        }
        double rate = (current - previous) * 100.0 / previous;
        return Math.round(rate * 100.0) / 100.0;
    }
}
