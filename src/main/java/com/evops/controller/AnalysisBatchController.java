package com.evops.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evops.common.ApiResponse;
import com.evops.dto.BatchCreateReq;
import com.evops.dto.BatchSamplesReq;
import com.evops.dto.TransitionReq;
import com.evops.entity.AnalysisBatch;
import com.evops.service.AnalysisBatchService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/icecore/batches")
public class AnalysisBatchController {
    private final AnalysisBatchService analysisBatchService;

    public AnalysisBatchController(AnalysisBatchService analysisBatchService) {
        this.analysisBatchService = analysisBatchService;
    }

    @PostMapping
    public ApiResponse<AnalysisBatch> create(@Valid @RequestBody BatchCreateReq req) {
        return ApiResponse.ok(analysisBatchService.createBatch(req));
    }

    @PostMapping("/{id}/samples")
    public ApiResponse<Void> addSamples(@PathVariable Long id, @Valid @RequestBody BatchSamplesReq req) {
        analysisBatchService.addSamples(id, req.getSampleIds());
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}/samples/{sampleId}")
    public ApiResponse<Void> removeSample(@PathVariable Long id, @PathVariable Long sampleId) {
        analysisBatchService.removeSample(id, sampleId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/transition")
    public ApiResponse<AnalysisBatch> transit(@PathVariable Long id, @Valid @RequestBody TransitionReq req) {
        return ApiResponse.ok(analysisBatchService.transit(id, req.getTargetStatus()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        analysisBatchService.deleteById(id);
        return ApiResponse.ok(null);
    }

    @GetMapping
    public ApiResponse<Page<AnalysisBatch>> page(@RequestParam(required = false) String status,
                                                 @RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(analysisBatchService.pageQuery(status, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(analysisBatchService.detail(id));
    }
}
