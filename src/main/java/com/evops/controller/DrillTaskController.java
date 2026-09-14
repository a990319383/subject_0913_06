package com.evops.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evops.common.ApiResponse;
import com.evops.dto.DrillTaskReq;
import com.evops.dto.TransitionReq;
import com.evops.entity.DrillTask;
import com.evops.service.DrillTaskService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/icecore/tasks")
public class DrillTaskController {
    private final DrillTaskService drillTaskService;

    public DrillTaskController(DrillTaskService drillTaskService) {
        this.drillTaskService = drillTaskService;
    }

    @PostMapping
    public ApiResponse<DrillTask> create(@Valid @RequestBody DrillTaskReq req) {
        return ApiResponse.ok(drillTaskService.create(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<DrillTask> modify(@PathVariable Long id, @Valid @RequestBody DrillTaskReq req) {
        return ApiResponse.ok(drillTaskService.modify(id, req));
    }

    @PostMapping("/{id}/transition")
    public ApiResponse<DrillTask> transit(@PathVariable Long id, @Valid @RequestBody TransitionReq req) {
        return ApiResponse.ok(drillTaskService.transit(id, req.getTargetStatus()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        drillTaskService.deleteById(id);
        return ApiResponse.ok(null);
    }

    @GetMapping
    public ApiResponse<Page<DrillTask>> page(@RequestParam(required = false) String status,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(drillTaskService.pageQuery(status, keyword, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(drillTaskService.detail(id));
    }
}
