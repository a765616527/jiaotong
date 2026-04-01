package com.traffic.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DisposalRecordRequest {

    @NotBlank(message = "处置说明不能为空")
    private String actionDesc;
}
