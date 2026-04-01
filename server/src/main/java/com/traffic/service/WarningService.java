package com.traffic.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.traffic.entity.WarningEvent;
import java.util.List;
import java.util.Map;

public interface WarningService {

    IPage<WarningEvent> pageWarnings(long pageNum, long pageSize, String status);

    void confirm(Long id);

    void publish(Long id);

    void resolve(Long id);

    void addDisposalRecord(Long id, String actionDesc);

    Map<String, Object> batchConfirm(List<Long> ids);

    Map<String, Object> batchPublish(List<Long> ids);

    Map<String, Object> batchResolve(List<Long> ids);

    Map<String, Object> generateExplanation(Long warningId);
}
