package com.traffic.config;

import com.traffic.service.RiskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "risk.scheduler.enabled", havingValue = "true")
public class RiskEvaluateScheduler {

    private final RiskService riskService;

    @Scheduled(cron = "${risk.scheduler.cron:0 0/30 * * * ?}")
    public void evaluate() {
        try {
            int size = riskService.evaluateAndGenerateWarnings().size();
            log.info("RiskEvaluateScheduler finished. hotspots={}", size);
        } catch (Exception e) {
            log.error("RiskEvaluateScheduler failed: {}", e.getMessage(), e);
        }
    }
}
