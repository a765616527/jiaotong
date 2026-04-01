package com.traffic.controller;

import com.traffic.common.ApiResponse;
import com.traffic.service.StatsService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> overview(
        @RequestParam(defaultValue = "7") int days) {
        return ApiResponse.success(statsService.overview(days));
    }
}
