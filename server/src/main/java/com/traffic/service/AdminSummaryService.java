package com.traffic.service;

import java.util.Map;

public interface AdminSummaryService {

    Map<String, Object> summary();

    Map<String, Object> trend(int days);
}
