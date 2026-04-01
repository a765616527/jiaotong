package com.traffic.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.traffic.common.ApiResponse;
import com.traffic.dto.DisposalRecordRequest;
import com.traffic.dto.IdBatchRequest;
import com.traffic.entity.WarningEvent;
import com.traffic.service.RiskService;
import com.traffic.service.WarningService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class WarningController {

    private final WarningService warningService;
    private final RiskService riskService;

    @PostMapping("/risk/evaluate")
    public ApiResponse<List<Map<String, Object>>> evaluate() {
        return ApiResponse.success(riskService.evaluateAndGenerateWarnings());
    }

    @GetMapping("/risk/hotspots")
    public ApiResponse<List<Map<String, Object>>> hotspots() {
        return ApiResponse.success(riskService.hotspots());
    }

    @GetMapping("/warnings")
    public ApiResponse<IPage<WarningEvent>> warnings(
        @RequestParam(defaultValue = "1") long pageNum,
        @RequestParam(defaultValue = "10") long pageSize,
        @RequestParam(required = false) String status) {
        return ApiResponse.success(warningService.pageWarnings(pageNum, pageSize, status));
    }

    @PostMapping("/warnings/{id}/confirm")
    public ApiResponse<Void> confirm(@PathVariable Long id) {
        warningService.confirm(id);
        return ApiResponse.success();
    }

    @PostMapping("/warnings/{id}/publish")
    public ApiResponse<Void> publish(@PathVariable Long id) {
        warningService.publish(id);
        return ApiResponse.success();
    }

    @PostMapping("/warnings/{id}/resolve")
    public ApiResponse<Void> resolve(@PathVariable Long id) {
        warningService.resolve(id);
        return ApiResponse.success();
    }

    @PostMapping("/warnings/batch/confirm")
    public ApiResponse<Map<String, Object>> batchConfirm(@Valid @RequestBody IdBatchRequest request) {
        return ApiResponse.success(warningService.batchConfirm(request.getIds()));
    }

    @PostMapping("/warnings/batch/publish")
    public ApiResponse<Map<String, Object>> batchPublish(@Valid @RequestBody IdBatchRequest request) {
        return ApiResponse.success(warningService.batchPublish(request.getIds()));
    }

    @PostMapping("/warnings/batch/resolve")
    public ApiResponse<Map<String, Object>> batchResolve(@Valid @RequestBody IdBatchRequest request) {
        return ApiResponse.success(warningService.batchResolve(request.getIds()));
    }

    @PostMapping("/warnings/{id}/disposal-records")
    public ApiResponse<Void> addDisposalRecord(@PathVariable Long id,
        @Valid @RequestBody DisposalRecordRequest request) {
        warningService.addDisposalRecord(id, request.getActionDesc());
        return ApiResponse.success();
    }

    @GetMapping("/warnings/{id}/ai-explanation")
    public ApiResponse<Map<String, Object>> aiExplanation(@PathVariable Long id) {
        return ApiResponse.success(warningService.generateExplanation(id));
    }
}
