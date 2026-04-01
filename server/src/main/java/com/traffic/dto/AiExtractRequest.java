package com.traffic.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiExtractRequest {

    @NotBlank(message = "事故描述不能为空")
    private String text;
}
