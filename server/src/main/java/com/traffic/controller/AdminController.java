package com.traffic.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.traffic.common.ApiResponse;
import com.traffic.dto.CreateUserRequest;
import com.traffic.dto.DictItemCreateRequest;
import com.traffic.dto.DictItemUpdateRequest;
import com.traffic.dto.RuleUpdateRequest;
import com.traffic.entity.AiCallLog;
import com.traffic.entity.DictItem;
import com.traffic.entity.RuleChangeLog;
import com.traffic.entity.SysOperationLog;
import com.traffic.entity.SysUser;
import com.traffic.entity.WarningRuleConfig;
import com.traffic.service.AdminSummaryService;
import com.traffic.service.AiLogService;
import com.traffic.service.DictService;
import com.traffic.service.OperationLogService;
import com.traffic.service.RuleChangeLogService;
import com.traffic.service.RuleService;
import com.traffic.service.UserService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final RuleService ruleService;
    private final OperationLogService operationLogService;
    private final AiLogService aiLogService;
    private final RuleChangeLogService ruleChangeLogService;
    private final DictService dictService;
    private final AdminSummaryService adminSummaryService;

    @GetMapping("/users")
    public ApiResponse<IPage<SysUser>> users(
        @RequestParam(defaultValue = "1") long pageNum,
        @RequestParam(defaultValue = "10") long pageSize,
        @RequestParam(required = false) String username) {
        return ApiResponse.success(userService.pageUsers(pageNum, pageSize, username));
    }

    @PostMapping("/users")
    public ApiResponse<Void> createUser(@Valid @RequestBody CreateUserRequest request) {
        userService.createUser(request);
        return ApiResponse.success();
    }

    @GetMapping("/rules")
    public ApiResponse<List<WarningRuleConfig>> rules() {
        return ApiResponse.success(ruleService.listRules());
    }

    @PostMapping("/rules/update")
    public ApiResponse<Void> updateRule(@Valid @RequestBody RuleUpdateRequest request) {
        ruleService.updateRule(request);
        return ApiResponse.success();
    }

    @GetMapping("/logs")
    public ApiResponse<IPage<SysOperationLog>> logs(
        @RequestParam(defaultValue = "1") long pageNum,
        @RequestParam(defaultValue = "10") long pageSize,
        @RequestParam(required = false) String module) {
        return ApiResponse.success(operationLogService.page(pageNum, pageSize, module));
    }

    @GetMapping("/ai-logs")
    public ApiResponse<IPage<AiCallLog>> aiLogs(
        @RequestParam(defaultValue = "1") long pageNum,
        @RequestParam(defaultValue = "10") long pageSize,
        @RequestParam(required = false) String endpoint) {
        return ApiResponse.success(aiLogService.page(pageNum, pageSize, endpoint));
    }

    @GetMapping("/rule-change-logs")
    public ApiResponse<IPage<RuleChangeLog>> ruleChangeLogs(
        @RequestParam(defaultValue = "1") long pageNum,
        @RequestParam(defaultValue = "10") long pageSize,
        @RequestParam(required = false) String ruleKey,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startTime,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endTime) {
        return ApiResponse.success(
            ruleChangeLogService.page(pageNum, pageSize, ruleKey, startTime, endTime));
    }

    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> summary() {
        return ApiResponse.success(adminSummaryService.summary());
    }

    @GetMapping("/summary/trend")
    public ApiResponse<Map<String, Object>> summaryTrend(
        @RequestParam(defaultValue = "7") int days) {
        return ApiResponse.success(adminSummaryService.trend(days));
    }

    @GetMapping("/dicts")
    public ApiResponse<IPage<DictItem>> dictItems(
        @RequestParam(defaultValue = "1") long pageNum,
        @RequestParam(defaultValue = "10") long pageSize,
        @RequestParam(required = false) String dictType,
        @RequestParam(required = false) Integer enabled) {
        return ApiResponse.success(dictService.pageAdmin(pageNum, pageSize, dictType, enabled));
    }

    @PostMapping("/dicts")
    public ApiResponse<Void> createDict(@Valid @RequestBody DictItemCreateRequest request) {
        dictService.create(request);
        return ApiResponse.success();
    }

    @PostMapping("/dicts/{id}/update")
    public ApiResponse<Void> updateDict(@PathVariable Long id,
        @Valid @RequestBody DictItemUpdateRequest request) {
        dictService.update(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/dicts/{id}/delete")
    public ApiResponse<Void> deleteDict(@PathVariable Long id) {
        dictService.delete(id);
        return ApiResponse.success();
    }
}
