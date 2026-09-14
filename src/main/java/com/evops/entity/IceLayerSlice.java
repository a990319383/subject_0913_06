package com.evops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 冰芯层位切片：冰芯样本按深度切出的层位级观测切片（压测量级 500,000+），
 * layer_no 为层位分区键，slice_index 为样本内序号。
 * 仅 create_time，不继承 BaseEntity 的审计列。
 */
@TableName("t_ice_layer_slice")
public class IceLayerSlice {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sampleId;
    private Long locationId;
    private Long tenantId;
    private String layerNo;
    private Integer sliceIndex;
    private BigDecimal depthTop;
    private BigDecimal depthBottom;
    private BigDecimal temperature;
    private LocalDateTime measuredAt;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSampleId() { return sampleId; }
    public void setSampleId(Long sampleId) { this.sampleId = sampleId; }
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
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
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
