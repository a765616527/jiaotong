package com.traffic.service;

import com.traffic.dto.RuleUpdateRequest;
import com.traffic.entity.WarningRuleConfig;
import java.util.List;
import java.util.Map;

public interface RuleService {

    List<WarningRuleConfig> listRules();

    void updateRule(RuleUpdateRequest request);

    Map<String, Double> readRuleValues();
}
