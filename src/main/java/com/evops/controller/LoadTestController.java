package com.evops.controller;

import com.evops.common.ApiResponse;
import com.evops.dto.LoadTestDataReq;
import com.evops.service.LoadTestDataService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 压测数据播种与分页基准入口。
 * 默认播种 100,000 条分钟级库温与 500,000 个层位切片，并可回传稳定分页耗时。
 */
@RestController
@RequestMapping("/api/icecore/loadtest")
public class LoadTestController {

    private static final int DEFAULT_TEMPERATURE_EVENTS = 100_000;
    private static final int DEFAULT_SLICE_COUNT = 500_000;

    private final LoadTestDataService loadTestDataService;

    public LoadTestController(LoadTestDataService loadTestDataService) {
        this.loadTestDataService = loadTestDataService;
    }

    @PostMapping("/seed")
    public ApiResponse<Map<String, Object>> seed(@RequestBody(required = false) LoadTestDataReq req) {
        int events = req == null || req.getTemperatureEvents() == null
                ? DEFAULT_TEMPERATURE_EVENTS : req.getTemperatureEvents();
        int slices = req == null || req.getSliceCount() == null
                ? DEFAULT_SLICE_COUNT : req.getSliceCount();
        boolean reset = req != null && Boolean.TRUE.equals(req.getReset());
        boolean benchmark = req == null || req.getBenchmark() == null || req.getBenchmark();
        return ApiResponse.ok(loadTestDataService.seedAndBenchmark(events, slices, reset, benchmark));
    }
}
