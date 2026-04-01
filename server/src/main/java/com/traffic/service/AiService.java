package com.traffic.service;

import java.util.Map;

public interface AiService {

    String generateWarningExplanation(String roadName, Double score, String triggerReason);

    Map<String, Object> extractAccidentFields(String text);

    String generateDisposalSuggestions(String riskLevel, String roadName, String triggerReason);

    String assistantQuery(String question);
}
