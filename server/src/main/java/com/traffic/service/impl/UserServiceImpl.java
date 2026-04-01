package com.traffic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.traffic.common.ApiException;
import com.traffic.dto.CreateUserRequest;
import com.traffic.entity.SysUser;
import com.traffic.mapper.SysUserMapper;
import com.traffic.service.OperationLogService;
import com.traffic.service.UserService;
import com.traffic.util.SecurityUtils;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final OperationLogService operationLogService;

    @Override
    public IPage<SysUser> pageUsers(long pageNum, long pageSize, String username) {
        SecurityUtils.requireAdmin();
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
            .orderByDesc(SysUser::getCreatedAt);
        if (username != null && !username.isBlank()) {
            wrapper.like(SysUser::getUsername, username);
        }
        IPage<SysUser> result = userMapper.selectPage(page, wrapper);
        result.getRecords().forEach(u -> u.setPassword(null));
        return result;
    }

    @Override
    public void createUser(CreateUserRequest request) {
        SecurityUtils.requireAdmin();

        Long count = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getUsername, request.getUsername()));
        if (count != null && count > 0) {
            throw new ApiException(400, "用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(request.getDisplayName());
        user.setRoleCode(request.getRoleCode());
        user.setEnabled(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);
        operationLogService.log("ADMIN", "CREATE_USER", "SYS_USER", String.valueOf(user.getId()),
            "新增用户:" + user.getUsername());
    }
}
