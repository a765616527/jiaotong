package com.traffic.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiQueryRequest {

    @NotBlank(message = "问题不能为空")
    private String question;
}
