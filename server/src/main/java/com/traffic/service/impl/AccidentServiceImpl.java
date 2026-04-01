package com.traffic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.traffic.common.ApiException;
import com.traffic.dto.AccidentCreateRequest;
import com.traffic.dto.AccidentUpdateRequest;
import com.traffic.entity.AccidentRecord;
import com.traffic.entity.DictItem;
import com.traffic.mapper.AccidentRecordMapper;
import com.traffic.mapper.DictItemMapper;
import com.traffic.service.AccidentService;
import com.traffic.service.OperationLogService;
import com.traffic.util.SecurityUtils;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AccidentServiceImpl implements AccidentService {

    private final AccidentRecordMapper accidentMapper;
    private final DictItemMapper dictItemMapper;
    private final OperationLogService operationLogService;

    @Override
    public IPage<AccidentRecord> pageAccidents(long pageNum, long pageSize, String roadName,
        String accidentType, LocalDateTime startTime, LocalDateTime endTime) {
        Page<AccidentRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AccidentRecord> wrapper = new LambdaQueryWrapper<AccidentRecord>()
            .orderByDesc(AccidentRecord::getOccurTime);
        if (roadName != null && !roadName.isBlank()) {
            wrapper.like(AccidentRecord::getRoadName, roadName);
        }
        if (accidentType != null && !accidentType.isBlank()) {
            wrapper.eq(AccidentRecord::getAccidentType, accidentType);
        }
        if (startTime != null) {
            wrapper.ge(AccidentRecord::getOccurTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(AccidentRecord::getOccurTime, endTime);
        }
        return accidentMapper.selectPage(page, wrapper);
    }

    @Override
    public AccidentRecord getById(Long id) {
        AccidentRecord record = accidentMapper.selectById(id);
        if (record == null) {
            throw new ApiException(404, "事故记录不存在");
        }
        return record;
    }

    @Override
    public void create(AccidentCreateRequest request) {
        validateLngLat(request.getLongitude().doubleValue(), request.getLatitude().doubleValue());
        AccidentRecord record = new AccidentRecord();
        record.setOccurTime(request.getOccurTime());
        record.setRoadName(request.getRoadName());
        record.setAreaCode(request.getAreaCode());
        record.setLongitude(request.getLongitude());
        record.setLatitude(request.getLatitude());
        record.setAccidentType(normalizeAccidentType(request.getAccidentType()));
        record.setVehicleCount(request.getVehicleCount());
        record.setCasualtyCount(request.getCasualtyCount());
        record.setDescription(request.getDescription());
        record.setCreatedBy(SecurityUtils.currentUserId());
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        record.setDeleted(0);
        accidentMapper.insert(record);

        operationLogService.log("ACCIDENT", "CREATE", "ACCIDENT", String.valueOf(record.getId()),
            "新增事故: " + request.getRoadName());
    }

    @Override
    public void update(Long id, AccidentUpdateRequest request) {
        validateLngLat(request.getLongitude().doubleValue(), request.getLatitude().doubleValue());
        AccidentRecord record = getById(id);
        record.setOccurTime(request.getOccurTime());
        record.setRoadName(request.getRoadName());
        record.setAreaCode(request.getAreaCode());
        record.setLongitude(request.getLongitude());
        record.setLatitude(request.getLatitude());
        record.setAccidentType(normalizeAccidentType(request.getAccidentType()));
        record.setVehicleCount(request.getVehicleCount());
        record.setCasualtyCount(request.getCasualtyCount());
        record.setDescription(request.getDescription());
        record.setUpdatedAt(LocalDateTime.now());
        accidentMapper.updateById(record);

        operationLogService.log("ACCIDENT", "UPDATE", "ACCIDENT", String.valueOf(id),
            "更新事故: " + request.getRoadName());
    }

    @Override
    public void delete(Long id) {
        AccidentRecord record = getById(id);
        accidentMapper.deleteById(id);
        operationLogService.log("ACCIDENT", "DELETE", "ACCIDENT", String.valueOf(id),
            "删除事故: " + record.getRoadName());
    }

    @Override
    public Map<String, Object> importCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(400, "上传文件不能为空");
        }

        int success = 0;
        List<Map<String, Object>> errors = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
            new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int lineNo = 0;
            boolean headerSkipped = false;
            while ((line = br.readLine()) != null) {
                lineNo++;
                if (line.isBlank()) {
                    continue;
                }
                String lower = line.toLowerCase();
                if (!headerSkipped && (lower.contains("occtime")
                    || lower.contains("occurtime")
                    || lower.contains("occur_time")
                    || lower.contains("road_name"))) {
                    headerSkipped = true;
                    continue;
                }
                headerSkipped = true;
                try {
                    String[] arr = splitCsv(line);
                    if (arr.length < 8) {
                        throw new ApiException(400, "字段数量不足，至少8列");
                    }
                    AccidentRecord record = new AccidentRecord();
                    record.setOccurTime(parseDateTime(arr[0]));
                    record.setRoadName(arr[1].trim());
                    record.setAreaCode(arr[2].trim());
                    record.setLongitude(new BigDecimal(arr[3].trim()));
                    record.setLatitude(new BigDecimal(arr[4].trim()));
                    record.setAccidentType(normalizeAccidentType(arr[5].trim()));
                    record.setVehicleCount(Integer.parseInt(arr[6].trim()));
                    record.setCasualtyCount(Integer.parseInt(arr[7].trim()));
                    record.setDescription(arr.length >= 9 ? arr[8].trim() : "");
                    validateLngLat(record.getLongitude().doubleValue(), record.getLatitude().doubleValue());
                    record.setCreatedBy(SecurityUtils.currentUserId());
                    record.setCreatedAt(LocalDateTime.now());
                    record.setUpdatedAt(LocalDateTime.now());
                    record.setDeleted(0);
                    accidentMapper.insert(record);
                    success++;
                } catch (Exception e) {
                    String reason = e.getMessage() == null ? "数据格式错误" : e.getMessage();
                    errors.add(Map.of(
                        "line", lineNo,
                        "content", line,
                        "reason", reason));
                }
            }
        } catch (Exception e) {
            throw new ApiException(400, "CSV解析失败: " + e.getMessage());
        }

        operationLogService.log("ACCIDENT", "IMPORT_CSV", "ACCIDENT", "-",
            "导入CSV success=" + success + ", failed=" + errors.size());
        return Map.of(
            "success", success,
            "failed", errors.size(),
            "errors", errors);
    }

    private void validateLngLat(double longitude, double latitude) {
        if (longitude < -180 || longitude > 180 || latitude < -90 || latitude > 90) {
            throw new ApiException(400, "经纬度超出范围");
        }
    }

    private LocalDateTime parseDateTime(String text) {
        String value = text.trim();
        if (value.contains("T")) {
            return LocalDateTime.parse(value);
        }
        return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String[] splitCsv(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuote = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuote = !inQuote;
                continue;
            }
            if (c == ',' && !inQuote) {
                fields.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        fields.add(cur.toString());
        return fields.toArray(new String[0]);
    }

    private String normalizeAccidentType(String accidentType) {
        String value = accidentType == null ? "" : accidentType.trim();
        if (value.isEmpty()) {
            throw new ApiException(400, "事故类型不能为空");
        }
        DictItem item = dictItemMapper.selectOne(new LambdaQueryWrapper<DictItem>()
            .eq(DictItem::getDictType, "ACCIDENT_TYPE")
            .eq(DictItem::getEnabled, 1)
            .and(w -> w.eq(DictItem::getDictName, value)
                .or()
                .eq(DictItem::getDictCode, value.toUpperCase()))
            .last("limit 1"));
        if (item == null) {
            throw new ApiException(400, "事故类型未在启用字典中配置: " + value);
        }
        return item.getDictName();
    }
}
