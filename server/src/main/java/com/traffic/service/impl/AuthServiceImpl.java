package com.traffic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.traffic.common.ApiException;
import com.traffic.dto.LoginRequest;
import com.traffic.entity.SysUser;
import com.traffic.mapper.SysUserMapper;
import com.traffic.security.JwtTokenProvider;
import com.traffic.security.LoginUser;
import com.traffic.service.AuthService;
import com.traffic.service.OperationLogService;
import com.traffic.util.SecurityUtils;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final OperationLogService operationLogService;

    @Override
    public Map<String, Object> login(LoginRequest request) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getUsername, request.getUsername())
            .last("limit 1"));

        if (user == null || user.getEnabled() == null || user.getEnabled() != 1) {
            operationLogService.logByOperator(0L, "AUTH", "LOGIN_FAIL", "USER", "-",
                "登录失败，用户名:" + request.getUsername());
            throw new ApiException(401, "用户名或密码错误");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            operationLogService.logByOperator(user.getId(), "AUTH", "LOGIN_FAIL", "USER",
                String.valueOf(user.getId()), "登录失败，密码错误");
            throw new ApiException(401, "用户名或密码错误");
        }

        LoginUser loginUser = new LoginUser(user.getId(), user.getUsername(),
            user.getPassword(), user.getRoleCode(), true);
        String token = jwtTokenProvider.createToken(loginUser);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", Map.of(
            "id", user.getId(),
            "username", user.getUsername(),
            "displayName", user.getDisplayName(),
            "roleCode", user.getRoleCode()));
        operationLogService.logByOperator(user.getId(), "AUTH", "LOGIN", "USER",
            String.valueOf(user.getId()), "用户登录成功");
        return data;
    }

    @Override
    public Map<String, Object> me() {
        LoginUser loginUser = SecurityUtils.currentUser();
        return Map.of(
            "id", loginUser.getId(),
            "username", loginUser.getUsername(),
            "roleCode", loginUser.getAuthorities().iterator().next().getAuthority());
    }
}
