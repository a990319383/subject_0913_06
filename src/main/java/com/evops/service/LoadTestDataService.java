package com.evops.service;

import java.util.Map;

/**
 * 压测数据播种与分页基准：分钟级库温 100,000、层位切片 500,000 量级。
 */
public interface LoadTestDataService {

    /**
     * 播种压测数据（幂等：默认复用专用压测租户，reset=true 先清理重灌），
     * 并可在播种后执行稳定分页基准。
     */
    Map<String, Object> seedAndBenchmark(int temperatureEvents, int sliceCount, boolean reset, boolean benchmark);
}
