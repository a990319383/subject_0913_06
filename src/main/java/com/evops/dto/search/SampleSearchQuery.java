package com.evops.dto.search;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 冰芯样本运营检索条件：钻取任务、样本盒、库位、分析批次、状态、取样日期范围、
 * 层位集合、深度区间、实测温度区间组合（AND）。
 * 至少支持 4 个条件的 AND/范围组合；批次通过 EXISTS 半连接过滤，不放大主表。
 */
public class SampleSearchQuery extends SearchScroll {
    private Long taskId;
    private Long boxId;
    private Long locationId;
    private Long batchId;
    private String status;
    /** 层位组合：IN 匹配 */
    private List<String> layerNos;
    /** 取样时间闭区间下界（>=） */
    private LocalDateTime sampledFrom;
    /** 取样时间上界（<，服务层把含当日转成次日零点） */
    private LocalDateTime sampledTo;
    private BigDecimal depthTopMin;
    private BigDecimal depthTopMax;
    private BigDecimal depthBottomMin;
    private BigDecimal depthBottomMax;
    private BigDecimal temperatureMin;
    private BigDecimal temperatureMax;

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public Long getBoxId() { return boxId; }
    public void setBoxId(Long boxId) { this.boxId = boxId; }
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<String> getLayerNos() { return layerNos; }
    public void setLayerNos(List<String> layerNos) { this.layerNos = layerNos; }
    public LocalDateTime getSampledFrom() { return sampledFrom; }
    public void setSampledFrom(LocalDateTime sampledFrom) { this.sampledFrom = sampledFrom; }
    public LocalDateTime getSampledTo() { return sampledTo; }
    public void setSampledTo(LocalDateTime sampledTo) { this.sampledTo = sampledTo; }
    public BigDecimal getDepthTopMin() { return depthTopMin; }
    public void setDepthTopMin(BigDecimal depthTopMin) { this.depthTopMin = depthTopMin; }
    public BigDecimal getDepthTopMax() { return depthTopMax; }
    public void setDepthTopMax(BigDecimal depthTopMax) { this.depthTopMax = depthTopMax; }
    public BigDecimal getDepthBottomMin() { return depthBottomMin; }
    public void setDepthBottomMin(BigDecimal depthBottomMin) { this.depthBottomMin = depthBottomMin; }
    public BigDecimal getDepthBottomMax() { return depthBottomMax; }
    public void setDepthBottomMax(BigDecimal depthBottomMax) { this.depthBottomMax = depthBottomMax; }
    public BigDecimal getTemperatureMin() { return temperatureMin; }
    public void setTemperatureMin(BigDecimal temperatureMin) { this.temperatureMin = temperatureMin; }
    public BigDecimal getTemperatureMax() { return temperatureMax; }
    public void setTemperatureMax(BigDecimal temperatureMax) { this.temperatureMax = temperatureMax; }
}
