package com.traffic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.traffic.common.ApiException;
import com.traffic.entity.WarningDisposalRecord;
import com.traffic.entity.WarningEvent;
import com.traffic.enums.WarningStatus;
import com.traffic.mapper.WarningDisposalRecordMapper;
import com.traffic.mapper.WarningEventMapper;
import com.traffic.service.AiService;
import com.traffic.service.OperationLogService;
import com.traffic.service.WarningService;
import com.traffic.util.SecurityUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WarningServiceImpl implements WarningService {

    private final WarningEventMapper warningMapper;
    private final WarningDisposalRecordMapper disposalRecordMapper;
    private final OperationLogService operationLogService;
    private final AiService aiService;

    @Override
    public IPage<WarningEvent> pageWarnings(long pageNum, long pageSize, String status) {
        Page<WarningEvent> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WarningEvent> wrapper = new LambdaQueryWrapper<WarningEvent>()
            .orderByDesc(WarningEvent::getCreatedAt);
        if (status != null && !status.isBlank()) {
            wrapper.eq(WarningEvent::getStatus, status);
        }
        return warningMapper.selectPage(page, wrapper);
    }

    @Override
    public void confirm(Long id) {
        WarningEvent event = getWarning(id);
        ensureStatus(event, WarningStatus.DRAFT);
        event.setStatus(WarningStatus.CONFIRMED.name());
        event.setUpdatedAt(LocalDateTime.now());
        warningMapper.updateById(event);
        operationLogService.log("WARNING", "CONFIRM", "WARNING", String.valueOf(id), "预警确认");
    }

    @Override
    public void publish(Long id) {
        WarningEvent event = getWarning(id);
        ensureStatus(event, WarningStatus.CONFIRMED);
        event.setStatus(WarningStatus.PUBLISHED.name());
        event.setPublishedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        warningMapper.updateById(event);
        operationLogService.log("WARNING", "PUBLISH", "WARNING", String.valueOf(id), "预警发布");
    }

    @Override
    public void resolve(Long id) {
        WarningEvent event = getWarning(id);
        if (!WarningStatus.PUBLISHED.name().equals(event.getStatus())
            && !WarningStatus.PROCESSING.name().equals(event.getStatus())) {
            throw new ApiException(400, "仅已发布/处置中的预警可解除");
        }
        event.setStatus(WarningStatus.RESOLVED.name());
        event.setResolvedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        warningMapper.updateById(event);
        operationLogService.log("WARNING", "RESOLVE", "WARNING", String.valueOf(id), "预警解除");
    }

    @Override
    public void addDisposalRecord(Long id, String actionDesc) {
        WarningEvent event = getWarning(id);
        if (!WarningStatus.PUBLISHED.name().equals(event.getStatus())
            && !WarningStatus.PROCESSING.name().equals(event.getStatus())) {
            throw new ApiException(400, "仅已发布/处置中的预警可登记处置");
        }

        WarningDisposalRecord record = new WarningDisposalRecord();
        record.setWarningId(id);
        record.setActionType("DISPOSAL");
        record.setActionDesc(actionDesc);
        record.setOperatorId(SecurityUtils.currentUserId());
        record.setOperatedAt(LocalDateTime.now());
        disposalRecordMapper.insert(record);

        if (WarningStatus.PUBLISHED.name().equals(event.getStatus())) {
            event.setStatus(WarningStatus.PROCESSING.name());
            event.setUpdatedAt(LocalDateTime.now());
            warningMapper.updateById(event);
        }

        operationLogService.log("WARNING", "DISPOSAL", "WARNING", String.valueOf(id), actionDesc);
    }

    @Override
    public Map<String, Object> batchConfirm(List<Long> ids) {
        return doBatch(ids, this::confirm);
    }

    @Override
    public Map<String, Object> batchPublish(List<Long> ids) {
        return doBatch(ids, this::publish);
    }

    @Override
    public Map<String, Object> batchResolve(List<Long> ids) {
        return doBatch(ids, this::resolve);
    }

    @Override
    public Map<String, Object> generateExplanation(Long warningId) {
        WarningEvent warning = getWarning(warningId);
        String text = aiService.generateWarningExplanation(warning.getTargetRoad(), 80.0,
            warning.getTriggerReason());
        return Map.of(
            "warningId", warningId,
            "warningCode", warning.getWarningCode(),
            "explanation", text);
    }

    private WarningEvent getWarning(Long id) {
        WarningEvent event = warningMapper.selectById(id);
        if (event == null) {
            throw new ApiException(404, "预警不存在");
        }
        return event;
    }

    private void ensureStatus(WarningEvent event, WarningStatus expected) {
        if (!expected.name().equals(event.getStatus())) {
            throw new ApiException(400, "状态流转不合法，当前状态: " + event.getStatus());
        }
    }

    private Map<String, Object> doBatch(List<Long> ids, IdAction action) {
        int success = 0;
        List<Map<String, Object>> fails = new ArrayList<>();
        for (Long id : ids) {
            try {
                action.apply(id);
                success++;
            } catch (Exception e) {
                String reason = e.getMessage() == null ? "操作失败" : e.getMessage();
                fails.add(Map.of(
                    "id", id,
                    "reason", reason));
            }
        }
        return Map.of(
            "total", ids.size(),
            "success", success,
            "failed", ids.size() - success,
            "fails", fails);
    }

    @FunctionalInterface
    private interface IdAction {

        void apply(Long id);
    }
}
