package com.evops.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;

/**
 * 分析批次明细：批次与冰芯样本的关联
 */
@TableName("t_analysis_batch_item")
public class AnalysisBatchItem extends BaseEntity {
    private Long batchId;
    private Long sampleId;

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public Long getSampleId() { return sampleId; }
    public void setSampleId(Long sampleId) { this.sampleId = sampleId; }
}
