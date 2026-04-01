package com.traffic.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RuleUpdateRequest {

    @NotBlank(message = "规则key不能为空")
    private String ruleKey;

    @NotBlank(message = "规则值不能为空")
    private String ruleValue;

    private String description;
}
