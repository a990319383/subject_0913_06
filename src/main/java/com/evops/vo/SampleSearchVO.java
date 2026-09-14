package com.evops.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 样本运营检索行：主表 t_ice_core_sample 一条记录恰好对应一行（不被批次一对多放大），
 * 附带任务/样本盒/库位的冗余展示字段与层位切片数。
 */
public class SampleSearchVO {
    private Long id;
    private String sampleNo;
    private Long taskId;
    private String taskNo;
    private String taskName;
    private Long boxId;
    private String boxNo;
    private Long locationId;
    private String locationCode;
    private String layerNo;
    private BigDecimal depthTop;
    private BigDecimal depthBottom;
    private BigDecimal temperature;
    private BigDecimal meltWaterMl;
    private BigDecimal integrityPct;
    private String status;
    private LocalDateTime sampledAt;
    private Long tenantId;
    private Integer sliceCount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSampleNo() { return sampleNo; }
    public void setSampleNo(String sampleNo) { this.sampleNo = sampleNo; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public String getTaskNo() { return taskNo; }
    public void setTaskNo(String taskNo) { this.taskNo = taskNo; }
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public Long getBoxId() { return boxId; }
    public void setBoxId(Long boxId) { this.boxId = boxId; }
    public String getBoxNo() { return boxNo; }
    public void setBoxNo(String boxNo) { this.boxNo = boxNo; }
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }
    public String getLayerNo() { return layerNo; }
    public void setLayerNo(String layerNo) { this.layerNo = layerNo; }
    public BigDecimal getDepthTop() { return depthTop; }
    public void setDepthTop(BigDecimal depthTop) { this.depthTop = depthTop; }
    public BigDecimal getDepthBottom() { return depthBottom; }
    public void setDepthBottom(BigDecimal depthBottom) { this.depthBottom = depthBottom; }
    public BigDecimal getTemperature() { return temperature; }
    public void setTemperature(BigDecimal temperature) { this.temperature = temperature; }
    public BigDecimal getMeltWaterMl() { return meltWaterMl; }
    public void setMeltWaterMl(BigDecimal meltWaterMl) { this.meltWaterMl = meltWaterMl; }
    public BigDecimal getIntegrityPct() { return integrityPct; }
    public void setIntegrityPct(BigDecimal integrityPct) { this.integrityPct = integrityPct; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getSampledAt() { return sampledAt; }
    public void setSampledAt(LocalDateTime sampledAt) { this.sampledAt = sampledAt; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Integer getSliceCount() { return sliceCount; }
    public void setSliceCount(Integer sliceCount) { this.sliceCount = sliceCount; }
}
