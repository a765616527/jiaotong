package com.traffic.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.traffic.entity.AiCallLog;

public interface AiLogService {

    void log(String endpoint, boolean success, String errorMessage, int inputLength, int outputLength);

    IPage<AiCallLog> page(long pageNum, long pageSize, String endpoint);
}
