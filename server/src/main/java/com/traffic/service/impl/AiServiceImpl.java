package com.traffic.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.traffic.service.AiLogService;
import com.traffic.service.AiService;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final AiLogService aiLogService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    @Value("${llm.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${llm.api-key:}")
    private String apiKey;

    @Value("${llm.model:gpt-4o-mini}")
    private String model;

    @Override
    public String generateWarningExplanation(String roadName, Double score, String triggerReason) {
        if (apiKey == null || apiKey.isBlank()) {
            String fallback = String.format("路段[%s]风险评分%.2f。触发原因：%s。建议加强夜间巡查与临时限速提示。",
                roadName, score, triggerReason);
            aiLogService.log("generate-warning-explanation", true, "fallback:no-api-key",
                triggerReason == null ? 0 : triggerReason.length(), fallback.length());
            return fallback;
        }

        String prompt = "你是交通管理分析助手。请把预警触发信息改写成100字以内中文说明，包含风险点和一条可执行建议。"
            + "\n路段:" + roadName + "\n评分:" + score + "\n触发依据:" + triggerReason;
        return callChatModel("generate-warning-explanation", prompt);
    }

    @Override
    public Map<String, Object> extractAccidentFields(String text) {
        if (apiKey == null || apiKey.isBlank()) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("accidentType", text.contains("追尾") ? "追尾" : "其他");
            fallback.put("isNight", text.contains("夜") || text.contains("凌晨"));
            fallback.put("severe", text.contains("受伤") || text.contains("死亡"));
            fallback.put("summary", "未配置大模型密钥，返回规则兜底解析");
            aiLogService.log("extract-accident-fields", true, "fallback:no-api-key",
                text == null ? 0 : text.length(), fallback.toString().length());
            return fallback;
        }

        String prompt = "从以下事故描述提取结构化字段，返回严格JSON对象，字段: accidentType,isNight,severe,summary。描述: "
            + text;
        String raw = callChatModel("extract-accident-fields", prompt);
        try {
            JsonNode node = objectMapper.readTree(raw);
            Map<String, Object> map = new HashMap<>();
            map.put("accidentType", node.path("accidentType").asText("其他"));
            map.put("isNight", node.path("isNight").asBoolean(false));
            map.put("severe", node.path("severe").asBoolean(false));
            map.put("summary", node.path("summary").asText(""));
            return map;
        } catch (Exception e) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("accidentType", "其他");
            fallback.put("isNight", false);
            fallback.put("severe", false);
            fallback.put("summary", raw);
            return fallback;
        }
    }

    @Override
    public String generateDisposalSuggestions(String riskLevel, String roadName, String triggerReason) {
        if (apiKey == null || apiKey.isBlank()) {
            String fallback = "建议动作: 1) 增加高峰与夜间巡逻 2) 设置临时限速与警示标识 3) 对该路段开展专项执法。";
            aiLogService.log("generate-disposal-suggestions", true, "fallback:no-api-key",
                triggerReason == null ? 0 : triggerReason.length(), fallback.length());
            return fallback;
        }
        String prompt = "请针对交通事故预警生成三条处置建议，返回中文短句。"
            + "\n风险等级:" + riskLevel
            + "\n道路:" + roadName
            + "\n触发依据:" + triggerReason;
        return callChatModel("generate-disposal-suggestions", prompt);
    }

    @Override
    public String assistantQuery(String question) {
        if (apiKey == null || apiKey.isBlank()) {
            String fallback = "未配置大模型密钥，当前仅返回兜底回答。问题: " + question;
            aiLogService.log("assistant-query", true, "fallback:no-api-key",
                question == null ? 0 : question.length(), fallback.length());
            return fallback;
        }
        String prompt = "你是交通管理数据助手，请用中文简洁回答问题: " + question;
        return callChatModel("assistant-query", prompt);
    }

    private String callChatModel(String endpoint, String prompt) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("temperature", 0.2);
            body.put("messages", new Object[] {
                Map.of("role", "system", "content", "你是严谨的城市交通分析助手"),
                Map.of("role", "user", "content", prompt)
            });

            String requestBody = objectMapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/chat/completions"))
                .timeout(Duration.ofSeconds(25))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = objectMapper.readTree(response.body());
            String content = root.path("choices").get(0).path("message").path("content").asText();
            aiLogService.log(endpoint, true, null, prompt.length(), content.length());
            return content;
        } catch (Exception e) {
            aiLogService.log(endpoint, false, e.getMessage(), prompt.length(), 0);
            return "模型调用失败，建议检查API密钥或网络。";
        }
    }
}
