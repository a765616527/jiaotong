package com.traffic.service;

import com.traffic.dto.LoginRequest;
import java.util.Map;

public interface AuthService {

    Map<String, Object> login(LoginRequest request);

    Map<String, Object> me();
}
