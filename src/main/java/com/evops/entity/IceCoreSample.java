package com.evops.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 冰芯样本：记录层位、温度、融水量、取样完整度
 */
@TableName("t_ice_core_sample")
public class IceCoreSample extends BaseEntity {
    private String sampleNo;
    private Long taskId;
    private Long boxId;
    private Long locationId;
    private String layerNo;
    private BigDecimal depthTop;
    private BigDecimal depthBottom;
    private BigDecimal temperature;
    private BigDecimal meltWaterMl;
    private BigDecimal integrityPct;
    private String status;
    private LocalDateTime sampledAt;
    private String remark;

    public String getSampleNo() { return sampleNo; }
    public void setSampleNo(String sampleNo) { this.sampleNo = sampleNo; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public Long getBoxId() { return boxId; }
    public void setBoxId(Long boxId) { this.boxId = boxId; }
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
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
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
