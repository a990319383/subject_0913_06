package com.evops.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.evops.dto.BatchCreateReq;
import com.evops.entity.AnalysisBatch;

import java.util.List;
import java.util.Map;

public interface AnalysisBatchService extends IService<AnalysisBatch> {
    /** 建立批次：校验样本可分析，写入明细并把样本置为分析中 */
    AnalysisBatch createBatch(BatchCreateReq req);

    void addSamples(Long batchId, List<Long> sampleIds);

    void removeSample(Long batchId, Long sampleId);

    AnalysisBatch transit(Long id, String targetStatus);

    void deleteById(Long id);

    Page<AnalysisBatch> pageQuery(String status, int page, int size);

    /** 批次详情：批次本体 + 明细（含样本层位/温度/融水量/完整度） */
    Map<String, Object> detail(Long id);
}
