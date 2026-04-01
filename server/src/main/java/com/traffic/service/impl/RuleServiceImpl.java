package com.traffic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.traffic.common.ApiException;
import com.traffic.dto.RuleUpdateRequest;
import com.traffic.entity.WarningRuleConfig;
import com.traffic.mapper.WarningRuleConfigMapper;
import com.traffic.service.OperationLogService;
import com.traffic.service.RuleChangeLogService;
import com.traffic.service.RuleService;
import com.traffic.util.SecurityUtils;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RuleServiceImpl implements RuleService {

    private final WarningRuleConfigMapper ruleMapper;
    private final OperationLogService operationLogService;
    private final RuleChangeLogService ruleChangeLogService;

    @Override
    public List<WarningRuleConfig> listRules() {
        SecurityUtils.requireAdmin();
        return ruleMapper.selectList(new LambdaQueryWrapper<WarningRuleConfig>()
            .orderByAsc(WarningRuleConfig::getRuleKey));
    }

    @Override
    public void updateRule(RuleUpdateRequest request) {
        SecurityUtils.requireAdmin();
        WarningRuleConfig existed = ruleMapper.selectOne(new LambdaQueryWrapper<WarningRuleConfig>()
            .eq(WarningRuleConfig::getRuleKey, request.getRuleKey())
            .last("limit 1"));
        if (existed == null) {
            throw new ApiException(404, "规则不存在: " + request.getRuleKey());
        }
        String oldValue = existed.getRuleValue();
        existed.setRuleValue(request.getRuleValue());
        existed.setDescription(request.getDescription());
        existed.setUpdatedBy(SecurityUtils.currentUserId());
        existed.setUpdatedAt(LocalDateTime.now());
        ruleMapper.updateById(existed);
        ruleChangeLogService.logChange(request.getRuleKey(), oldValue, request.getRuleValue(),
            request.getDescription());
        operationLogService.log("ADMIN", "UPDATE_RULE", "RULE_CONFIG", existed.getRuleKey(),
            "更新规则为:" + request.getRuleValue());
    }

    @Override
    public Map<String, Double> readRuleValues() {
        Map<String, Double> values = new HashMap<>();
        values.put("W_FREQ_7D", 0.45);
        values.put("W_FREQ_30D", 0.25);
        values.put("W_NIGHT", 0.15);
        values.put("W_SEVERE", 0.15);
        values.put("THRESHOLD_MEDIUM", 40.0);
        values.put("THRESHOLD_HIGH", 70.0);

        List<WarningRuleConfig> list = ruleMapper.selectList(
            new LambdaQueryWrapper<WarningRuleConfig>().orderByAsc(WarningRuleConfig::getRuleKey));
        for (WarningRuleConfig config : list) {
            try {
                values.put(config.getRuleKey(), Double.parseDouble(config.getRuleValue()));
            } catch (Exception ignored) {
                // 非数字规则值忽略
            }
        }
        return values;
    }
}
