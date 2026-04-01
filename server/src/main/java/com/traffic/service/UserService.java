package com.traffic.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.traffic.dto.CreateUserRequest;
import com.traffic.entity.SysUser;

public interface UserService {

    IPage<SysUser> pageUsers(long pageNum, long pageSize, String username);

    void createUser(CreateUserRequest request);
}
