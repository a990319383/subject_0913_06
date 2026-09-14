package com.evops.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evops.common.ApiResponse;
import com.evops.dto.SampleBoxReq;
import com.evops.dto.TransitionReq;
import com.evops.entity.SampleBox;
import com.evops.service.SampleBoxService;
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
@RequestMapping("/api/icecore/boxes")
public class SampleBoxController {
    private final SampleBoxService sampleBoxService;

    public SampleBoxController(SampleBoxService sampleBoxService) {
        this.sampleBoxService = sampleBoxService;
    }

    @PostMapping
    public ApiResponse<SampleBox> create(@Valid @RequestBody SampleBoxReq req) {
        return ApiResponse.ok(sampleBoxService.create(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<SampleBox> modify(@PathVariable Long id, @Valid @RequestBody SampleBoxReq req) {
        return ApiResponse.ok(sampleBoxService.modify(id, req));
    }

    @PostMapping("/{id}/transition")
    public ApiResponse<SampleBox> transit(@PathVariable Long id, @Valid @RequestBody TransitionReq req) {
        return ApiResponse.ok(sampleBoxService.transit(id, req.getTargetStatus()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        sampleBoxService.deleteById(id);
        return ApiResponse.ok(null);
    }

    @GetMapping
    public ApiResponse<Page<SampleBox>> page(@RequestParam(required = false) Long taskId,
                                             @RequestParam(required = false) Long locationId,
                                             @RequestParam(required = false) String status,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(sampleBoxService.pageQuery(taskId, locationId, status, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(sampleBoxService.detail(id));
    }
}
