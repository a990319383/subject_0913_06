package com.evops.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.evops.dto.StorageLocationReq;
import com.evops.entity.StorageLocation;

import java.util.Map;

public interface StorageLocationService extends IService<StorageLocation> {
    StorageLocation create(StorageLocationReq req);

    StorageLocation modify(Long id, StorageLocationReq req);

    StorageLocation transit(Long id, String targetStatus);

    void deleteById(Long id);

    Page<StorageLocation> pageQuery(String status, int page, int size);

    /** 库位详情：库位本体 + 已存放样本盒列表 */
    Map<String, Object> detail(Long id);

    /** 按已存放样本盒数量刷新库位 AVAILABLE/FULL 状态（维护中不触碰） */
    void refreshLoadStatus(Long locationId);
}
