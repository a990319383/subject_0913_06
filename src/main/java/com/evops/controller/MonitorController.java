package com.evops.controller;

import com.evops.common.ApiResponse;
import com.evops.service.IceCoreSampleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 冻融环境监测：对照库位设定温度巡检在库/分析中样本的实测温度。
 */
@RestController
@RequestMapping("/api/icecore/monitor")
public class MonitorController {
    private final IceCoreSampleService iceCoreSampleService;

    public MonitorController(IceCoreSampleService iceCoreSampleService) {
        this.iceCoreSampleService = iceCoreSampleService;
    }

    @GetMapping("/temperature")
    public ApiResponse<List<Map<String, Object>>> temperatureAlerts(
            @RequestParam(defaultValue = "2") BigDecimal threshold) {
        return ApiResponse.ok(iceCoreSampleService.temperatureAlerts(threshold));
    }
}
