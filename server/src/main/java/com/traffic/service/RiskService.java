package com.traffic.service;

import java.util.List;
import java.util.Map;

public interface RiskService {

    List<Map<String, Object>> evaluateAndGenerateWarnings();

    List<Map<String, Object>> hotspots();
}
