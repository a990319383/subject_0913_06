package com.evops.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evops.common.ApiResponse;
import com.evops.dto.IceCoreSampleReq;
import com.evops.entity.IceCoreSample;
import com.evops.service.IceCoreSampleService;
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
@RequestMapping("/api/icecore/samples")
public class IceCoreSampleController {
    private final IceCoreSampleService iceCoreSampleService;

    public IceCoreSampleController(IceCoreSampleService iceCoreSampleService) {
        this.iceCoreSampleService = iceCoreSampleService;
    }

    @PostMapping
    public ApiResponse<IceCoreSample> create(@Valid @RequestBody IceCoreSampleReq req) {
        return ApiResponse.ok(iceCoreSampleService.create(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<IceCoreSample> modify(@PathVariable Long id, @Valid @RequestBody IceCoreSampleReq req) {
        return ApiResponse.ok(iceCoreSampleService.modify(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        iceCoreSampleService.deleteById(id);
        return ApiResponse.ok(null);
    }

    @GetMapping
    public ApiResponse<Page<IceCoreSample>> page(@RequestParam(required = false) Long taskId,
                                                 @RequestParam(required = false) Long boxId,
                                                 @RequestParam(required = false) Long batchId,
                                                 @RequestParam(required = false) String status,
                                                 @RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(iceCoreSampleService.pageQuery(taskId, boxId, batchId, status, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(iceCoreSampleService.detail(id));
    }
}
