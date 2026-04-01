package com.traffic.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class IdBatchRequest {

    @NotEmpty(message = "ID列表不能为空")
    private List<Long> ids;
}
