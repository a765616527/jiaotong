package com.traffic.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.traffic.common.ApiResponse;
import com.traffic.dto.AccidentCreateRequest;
import com.traffic.dto.AccidentUpdateRequest;
import com.traffic.entity.AccidentRecord;
import com.traffic.service.AccidentService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
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
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/accidents")
@RequiredArgsConstructor
public class AccidentController {

    private final AccidentService accidentService;

    @GetMapping
    public ApiResponse<IPage<AccidentRecord>> page(
        @RequestParam(defaultValue = "1") long pageNum,
        @RequestParam(defaultValue = "10") long pageSize,
        @RequestParam(required = false) String roadName,
        @RequestParam(required = false) String accidentType,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startTime,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endTime) {
        return ApiResponse.success(
            accidentService.pageAccidents(pageNum, pageSize, roadName, accidentType, startTime, endTime));
    }

    @GetMapping("/{id}")
    public ApiResponse<AccidentRecord> get(@PathVariable Long id) {
        return ApiResponse.success(accidentService.getById(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody AccidentCreateRequest request) {
        accidentService.create(request);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/update")
    public ApiResponse<Void> update(@PathVariable Long id,
        @Valid @RequestBody AccidentUpdateRequest request) {
        accidentService.update(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/delete")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        accidentService.delete(id);
        return ApiResponse.success();
    }

    @PostMapping("/import-csv")
    public ApiResponse<Map<String, Object>> importCsv(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(accidentService.importCsv(file));
    }
}
