package com.evops.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.evops.dto.DrillTaskReq;
import com.evops.entity.DrillTask;

import java.util.Map;

public interface DrillTaskService extends IService<DrillTask> {
    DrillTask create(DrillTaskReq req);

    DrillTask modify(Long id, DrillTaskReq req);

    DrillTask transit(Long id, String targetStatus);

    void deleteById(Long id);

    Page<DrillTask> pageQuery(String status, String keyword, int page, int size);

    /** 任务详情：任务本体 + 样本盒列表 + 样本数量 */
    Map<String, Object> detail(Long id);
}
