package com.traffic.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AccidentUpdateRequest {

    @NotNull(message = "事故时间不能为空")
    private LocalDateTime occurTime;

    @NotBlank(message = "道路名称不能为空")
    private String roadName;

    private String areaCode;

    @NotNull(message = "经度不能为空")
    private BigDecimal longitude;

    @NotNull(message = "纬度不能为空")
    private BigDecimal latitude;

    @NotBlank(message = "事故类型不能为空")
    private String accidentType;

    @NotNull(message = "车辆数量不能为空")
    @Min(value = 1, message = "车辆数量最小为1")
    @Max(value = 100, message = "车辆数量超出范围")
    private Integer vehicleCount;

    @NotNull(message = "伤亡数量不能为空")
    @Min(value = 0, message = "伤亡数量不能为负")
    private Integer casualtyCount;

    private String description;
}
