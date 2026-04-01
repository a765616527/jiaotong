package com.traffic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.traffic.entity.AccidentRecord;
import com.traffic.entity.WarningEvent;
import com.traffic.mapper.AccidentRecordMapper;
import com.traffic.mapper.WarningEventMapper;
import com.traffic.service.StatsService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final AccidentRecordMapper accidentRecordMapper;
    private final WarningEventMapper warningEventMapper;

    @Override
    public Map<String, Object> overview() {
        return overview(7);
    }

    @Override
    public Map<String, Object> overview(int days) {
        int safeDays = Math.max(7, Math.min(days, 30));
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.minusDays(safeDays - 1L).atStartOfDay();

        List<AccidentRecord> weekRecords = accidentRecordMapper.selectList(
            new LambdaQueryWrapper<AccidentRecord>().ge(AccidentRecord::getOccurTime, start));
        List<WarningEvent> warnings = warningEventMapper.selectList(new LambdaQueryWrapper<WarningEvent>());

        Map<String, Integer> trendMap = new LinkedHashMap<>();
        for (int i = safeDays - 1; i >= 0; i--) {
            trendMap.put(today.minusDays(i).toString(), 0);
        }

        Map<String, Integer> typeDist = new HashMap<>();
        Map<String, Integer> roadCount = new HashMap<>();

        for (AccidentRecord r : weekRecords) {
            if (r.getOccurTime() != null) {
                String key = r.getOccurTime().toLocalDate().toString();
                if (trendMap.containsKey(key)) {
                    trendMap.put(key, trendMap.get(key) + 1);
                }
            }
            if (r.getAccidentType() != null) {
                typeDist.put(r.getAccidentType(), typeDist.getOrDefault(r.getAccidentType(), 0) + 1);
            }
            if (r.getRoadName() != null) {
                roadCount.put(r.getRoadName(), roadCount.getOrDefault(r.getRoadName(), 0) + 1);
            }
        }

        Map<String, Integer> warningStatus = new HashMap<>();
        for (WarningEvent w : warnings) {
            warningStatus.put(w.getStatus(), warningStatus.getOrDefault(w.getStatus(), 0) + 1);
        }

        List<Map<String, Object>> topRoads = new ArrayList<>();
        roadCount.entrySet().stream()
            .sorted(Comparator.comparingInt((Map.Entry<String, Integer> e) -> e.getValue()).reversed())
            .limit(10)
            .forEach(e -> {
                Map<String, Object> row = new HashMap<>();
                row.put("roadName", e.getKey());
                row.put("count", e.getValue());
                topRoads.add(row);
            });

        return Map.of(
            "days", safeDays,
            "trend", trendMap,
            "typeDistribution", typeDist,
            "topRoads", topRoads,
            "warningStatus", warningStatus,
            "accidentTotal", weekRecords.size(),
            "warningTotal", warnings.size());
    }
}
