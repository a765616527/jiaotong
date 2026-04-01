package com.traffic.service;

import java.util.Map;

public interface StatsService {

    Map<String, Object> overview();

    Map<String, Object> overview(int days);
}
