package com.traffic.controller;

import com.traffic.common.ApiResponse;
import com.traffic.dto.AiDisposalRequest;
import com.traffic.dto.AiExplainRequest;
import com.traffic.dto.AiExtractRequest;
import com.traffic.dto.AiQueryRequest;
import com.traffic.service.AiService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/generate-warning-explanation")
    public ApiResponse<Map<String, Object>> explain(@Valid @RequestBody AiExplainRequest request) {
        String explanation = aiService.generateWarningExplanation(
            request.getRoadName(), request.getScore(), request.getTriggerReason());
        return ApiResponse.success(Map.of("explanation", explanation));
    }

    @PostMapping("/extract-accident-fields")
    public ApiResponse<Map<String, Object>> extract(@Valid @RequestBody AiExtractRequest request) {
        return ApiResponse.success(aiService.extractAccidentFields(request.getText()));
    }

    @PostMapping("/generate-disposal-suggestions")
    public ApiResponse<Map<String, Object>> disposal(@Valid @RequestBody AiDisposalRequest request) {
        String suggestions = aiService.generateDisposalSuggestions(
            request.getRiskLevel(), request.getRoadName(), request.getTriggerReason());
        return ApiResponse.success(Map.of("suggestions", suggestions));
    }

    @PostMapping("/assistant-query")
    public ApiResponse<Map<String, Object>> assistant(@Valid @RequestBody AiQueryRequest request) {
        return ApiResponse.success(Map.of("answer", aiService.assistantQuery(request.getQuestion())));
    }
}
