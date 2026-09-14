package com.evops.dto.search;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 冰芯层位切片检索（库位按冰芯层位分区）：库位、样本、层位集合、深度与温度区间、
 * 测定时间范围。排序固定为 layer_no ASC, id ASC——先按层位分区再以确定性主键收尾，
 * 保证 500,000 切片量级下稳定分页；游标锚点为 (lastLayerNo, lastId)。
 */
public class LayerSliceSearchQuery extends SearchScroll {
    private Long locationId;
    private Long sampleId;
    private List<String> layerNos;
    private BigDecimal depthMin;
    private BigDecimal depthMax;
    private BigDecimal temperatureMin;
    private BigDecimal temperatureMax;
    private LocalDateTime measuredFrom;
    private LocalDateTime measuredTo;

    /** 游标锚点：上一页最后一条的层位 */
    private String lastLayerNo;

    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
    public Long getSampleId() { return sampleId; }
    public void setSampleId(Long sampleId) { this.sampleId = sampleId; }
    public List<String> getLayerNos() { return layerNos; }
    public void setLayerNos(List<String> layerNos) { this.layerNos = layerNos; }
    public BigDecimal getDepthMin() { return depthMin; }
    public void setDepthMin(BigDecimal depthMin) { this.depthMin = depthMin; }
    public BigDecimal getDepthMax() { return depthMax; }
    public void setDepthMax(BigDecimal depthMax) { this.depthMax = depthMax; }
    public BigDecimal getTemperatureMin() { return temperatureMin; }
    public void setTemperatureMin(BigDecimal temperatureMin) { this.temperatureMin = temperatureMin; }
    public BigDecimal getTemperatureMax() { return temperatureMax; }
    public void setTemperatureMax(BigDecimal temperatureMax) { this.temperatureMax = temperatureMax; }
    public LocalDateTime getMeasuredFrom() { return measuredFrom; }
    public void setMeasuredFrom(LocalDateTime measuredFrom) { this.measuredFrom = measuredFrom; }
    public LocalDateTime getMeasuredTo() { return measuredTo; }
    public void setMeasuredTo(LocalDateTime measuredTo) { this.measuredTo = measuredTo; }
    public String getLastLayerNo() { return lastLayerNo; }
    public void setLastLayerNo(String lastLayerNo) { this.lastLayerNo = lastLayerNo; }
}
