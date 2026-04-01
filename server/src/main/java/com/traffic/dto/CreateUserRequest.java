package com.traffic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateUserRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "姓名不能为空")
    private String displayName;

    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "ROLE_ADMIN|ROLE_TRAFFIC_OFFICER", message = "角色不合法")
    private String roleCode;
}
