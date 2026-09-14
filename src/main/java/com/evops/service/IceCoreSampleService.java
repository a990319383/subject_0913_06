package com.evops.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.evops.dto.IceCoreSampleReq;
import com.evops.entity.IceCoreSample;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface IceCoreSampleService extends IService<IceCoreSample> {
    IceCoreSample create(IceCoreSampleReq req);

    IceCoreSample modify(Long id, IceCoreSampleReq req);

    void deleteById(Long id);

    Page<IceCoreSample> pageQuery(Long taskId, Long boxId, Long batchId, String status, int page, int size);

    /** 样本详情：样本本体 + 任务 + 样本盒 + 库位 + 关联批次列表 */
    Map<String, Object> detail(Long id);

    /** 冻融监测：在库/分析中样本实测温度偏离库位设定温度超过阈值的告警列表 */
    List<Map<String, Object>> temperatureAlerts(BigDecimal threshold);
}
