package com.traffic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiExplainRequest {

    @NotBlank(message = "道路名称不能为空")
    private String roadName;

    @NotNull(message = "评分不能为空")
    private Double score;

    @NotBlank(message = "触发原因不能为空")
    private String triggerReason;
}
