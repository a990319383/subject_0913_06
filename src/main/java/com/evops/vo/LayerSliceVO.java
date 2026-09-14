package com.evops.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 层位切片检索行：一条 t_ice_layer_slice 一行，附样本/库位展示字段。
 */
public class LayerSliceVO {
    private Long id;
    private Long sampleId;
    private String sampleNo;
    private Long locationId;
    private String locationCode;
    private String layerNo;
    private Integer sliceIndex;
    private BigDecimal depthTop;
    private BigDecimal depthBottom;
    private BigDecimal temperature;
    private LocalDateTime measuredAt;
    private Long tenantId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSampleId() { return sampleId; }
    public void setSampleId(Long sampleId) { this.sampleId = sampleId; }
    public String getSampleNo() { return sampleNo; }
    public void setSampleNo(String sampleNo) { this.sampleNo = sampleNo; }
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }
    public String getLayerNo() { return layerNo; }
    public void setLayerNo(String layerNo) { this.layerNo = layerNo; }
    public Integer getSliceIndex() { return sliceIndex; }
    public void setSliceIndex(Integer sliceIndex) { this.sliceIndex = sliceIndex; }
    public BigDecimal getDepthTop() { return depthTop; }
    public void setDepthTop(BigDecimal depthTop) { this.depthTop = depthTop; }
    public BigDecimal getDepthBottom() { return depthBottom; }
    public void setDepthBottom(BigDecimal depthBottom) { this.depthBottom = depthBottom; }
    public BigDecimal getTemperature() { return temperature; }
    public void setTemperature(BigDecimal temperature) { this.temperature = temperature; }
    public LocalDateTime getMeasuredAt() { return measuredAt; }
    public void setMeasuredAt(LocalDateTime measuredAt) { this.measuredAt = measuredAt; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
}
