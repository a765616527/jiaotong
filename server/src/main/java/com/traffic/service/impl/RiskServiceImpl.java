package com.traffic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.traffic.entity.AccidentRecord;
import com.traffic.entity.WarningEvent;
import com.traffic.enums.RiskLevel;
import com.traffic.enums.WarningStatus;
import com.traffic.mapper.AccidentRecordMapper;
import com.traffic.mapper.WarningEventMapper;
import com.traffic.service.OperationLogService;
import com.traffic.service.RiskService;
import com.traffic.service.RuleService;
import com.traffic.util.SecurityUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RiskServiceImpl implements RiskService {

    private final AccidentRecordMapper accidentMapper;
    private final WarningEventMapper warningEventMapper;
    private final RuleService ruleService;
    private final OperationLogService operationLogService;

    @Override
    public List<Map<String, Object>> evaluateAndGenerateWarnings() {
        Long operatorId = resolveOperatorId();
        List<Map<String, Object>> results = calculateScores();
        for (Map<String, Object> row : results) {
            String riskLevel = String.valueOf(row.get("riskLevel"));
            if (RiskLevel.HIGH.name().equals(riskLevel)) {
                String roadName = String.valueOf(row.get("roadName"));
                if (!hasActiveWarning(roadName)) {
                    WarningEvent warning = new WarningEvent();
                    warning.setWarningCode(buildWarningCode(roadName));
                    warning.setRiskLevel(RiskLevel.HIGH.name());
                    warning.setTargetRoad(roadName);
                    warning.setTargetTimeWindow("7D_30D");
                    warning.setTriggerReason(String.valueOf(row.get("triggerReason")));
                    warning.setStatus(WarningStatus.DRAFT.name());
                    warning.setCreatedBy(operatorId);
                    warning.setCreatedAt(LocalDateTime.now());
                    warning.setUpdatedAt(LocalDateTime.now());
                    warningEventMapper.insert(warning);

                    operationLogService.logByOperator(operatorId, "WARNING", "AUTO_GENERATE", "WARNING",
                        String.valueOf(warning.getId()), "自动生成预警:" + roadName);
                }
            }
        }
        return results;
    }

    @Override
    public List<Map<String, Object>> hotspots() {
        return calculateScores();
    }

    private boolean hasActiveWarning(String roadName) {
        Long count = warningEventMapper.selectCount(new LambdaQueryWrapper<WarningEvent>()
            .eq(WarningEvent::getTargetRoad, roadName)
            .in(WarningEvent::getStatus,
                WarningStatus.DRAFT.name(),
                WarningStatus.CONFIRMED.name(),
                WarningStatus.PUBLISHED.name(),
                WarningStatus.PROCESSING.name()));
        return count != null && count > 0;
    }

    private List<Map<String, Object>> calculateScores() {
        Map<String, Double> rules = ruleService.readRuleValues();
        double w7 = rules.getOrDefault("W_FREQ_7D", 0.45);
        double w30 = rules.getOrDefault("W_FREQ_30D", 0.25);
        double wNight = rules.getOrDefault("W_NIGHT", 0.15);
        double wSevere = rules.getOrDefault("W_SEVERE", 0.15);
        double thresholdMedium = rules.getOrDefault("THRESHOLD_MEDIUM", 40.0);
        double thresholdHigh = rules.getOrDefault("THRESHOLD_HIGH", 70.0);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime d30 = now.minusDays(30);
        LocalDateTime d7 = now.minusDays(7);

        List<AccidentRecord> records = accidentMapper.selectList(new LambdaQueryWrapper<AccidentRecord>()
            .ge(AccidentRecord::getOccurTime, d30));

        Map<String, Stats> statsMap = new HashMap<>();
        for (AccidentRecord r : records) {
            String road = r.getRoadName();
            if (road == null || road.isBlank()) {
                continue;
            }
            Stats stats = statsMap.computeIfAbsent(road, k -> new Stats());
            stats.total30++;

            LocalDateTime occur = r.getOccurTime();
            if (occur != null && !occur.isBefore(d7)) {
                stats.total7++;
            }
            if (occur != null) {
                int hour = occur.getHour();
                if (hour < 6 || hour >= 20) {
                    stats.nightCount++;
                }
            }
            if (r.getCasualtyCount() != null && r.getCasualtyCount() > 0) {
                stats.severeCount++;
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Stats> e : statsMap.entrySet()) {
            String road = e.getKey();
            Stats s = e.getValue();

            double freq7 = Math.min(100, s.total7 * 10.0);
            double freq30 = Math.min(100, s.total30 * 4.0);
            double nightRatio = s.total30 == 0 ? 0 : s.nightCount * 100.0 / s.total30;
            double severeRatio = s.total30 == 0 ? 0 : s.severeCount * 100.0 / s.total30;
            double score = w7 * freq7 + w30 * freq30 + wNight * nightRatio + wSevere * severeRatio;

            String riskLevel;
            if (score >= thresholdHigh) {
                riskLevel = RiskLevel.HIGH.name();
            } else if (score >= thresholdMedium) {
                riskLevel = RiskLevel.MEDIUM.name();
            } else {
                riskLevel = RiskLevel.LOW.name();
            }

            String reason = String.format("近7天事故%d起，近30天事故%d起，夜间占比%.1f%%，伤亡占比%.1f%%",
                s.total7, s.total30, nightRatio, severeRatio);

            Map<String, Object> row = new HashMap<>();
            row.put("roadName", road);
            row.put("score", Math.round(score * 100.0) / 100.0);
            row.put("riskLevel", riskLevel);
            row.put("triggerReason", reason);
            row.put("count7d", s.total7);
            row.put("count30d", s.total30);
            result.add(row);
        }

        result.sort(Comparator.comparingDouble(o -> -Double.parseDouble(String.valueOf(o.get("score")))));
        return result;
    }

    private String buildWarningCode(String roadName) {
        return "WN-" + System.currentTimeMillis() + "-" + Math.abs(roadName.hashCode() % 10000);
    }

    private Long resolveOperatorId() {
        try {
            return SecurityUtils.currentUserId();
        } catch (Exception ignored) {
            return 0L;
        }
    }

    private static class Stats {

        int total7;
        int total30;
        int nightCount;
        int severeCount;
    }
}
