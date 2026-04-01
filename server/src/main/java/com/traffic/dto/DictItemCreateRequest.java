package com.traffic.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DictItemCreateRequest {

    @NotBlank(message = "字典类型不能为空")
    @Size(max = 64, message = "字典类型长度不能超过64")
    private String dictType;

    @NotBlank(message = "字典编码不能为空")
    @Size(max = 64, message = "字典编码长度不能超过64")
    private String dictCode;

    @NotBlank(message = "字典名称不能为空")
    @Size(max = 64, message = "字典名称长度不能超过64")
    private String dictName;

    @Min(value = 0, message = "排序号不能小于0")
    @Max(value = 9999, message = "排序号不能大于9999")
    private Integer sortNo = 100;

    @Min(value = 0, message = "启用标记不合法")
    @Max(value = 1, message = "启用标记不合法")
    private Integer enabled = 1;

    @Size(max = 255, message = "备注长度不能超过255")
    private String remark;
}
