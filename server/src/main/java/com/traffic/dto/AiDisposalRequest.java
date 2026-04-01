package com.traffic.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiDisposalRequest {

    @NotBlank(message = "风险等级不能为空")
    private String riskLevel;

    @NotBlank(message = "道路名称不能为空")
    private String roadName;

    @NotBlank(message = "触发原因不能为空")
    private String triggerReason;
}
