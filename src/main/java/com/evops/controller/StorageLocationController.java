package com.evops.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evops.common.ApiResponse;
import com.evops.dto.StorageLocationReq;
import com.evops.dto.TransitionReq;
import com.evops.entity.StorageLocation;
import com.evops.service.StorageLocationService;
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
@RequestMapping("/api/icecore/locations")
public class StorageLocationController {
    private final StorageLocationService storageLocationService;

    public StorageLocationController(StorageLocationService storageLocationService) {
        this.storageLocationService = storageLocationService;
    }

    @PostMapping
    public ApiResponse<StorageLocation> create(@Valid @RequestBody StorageLocationReq req) {
        return ApiResponse.ok(storageLocationService.create(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<StorageLocation> modify(@PathVariable Long id, @Valid @RequestBody StorageLocationReq req) {
        return ApiResponse.ok(storageLocationService.modify(id, req));
    }

    @PostMapping("/{id}/transition")
    public ApiResponse<StorageLocation> transit(@PathVariable Long id, @Valid @RequestBody TransitionReq req) {
        return ApiResponse.ok(storageLocationService.transit(id, req.getTargetStatus()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        storageLocationService.deleteById(id);
        return ApiResponse.ok(null);
    }

    @GetMapping
    public ApiResponse<Page<StorageLocation>> page(@RequestParam(required = false) String status,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(storageLocationService.pageQuery(status, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(storageLocationService.detail(id));
    }
}
