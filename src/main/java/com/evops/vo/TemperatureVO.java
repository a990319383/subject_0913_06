package com.evops.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 分钟级库温事件检索行：一条 t_freezer_temperature 一行，附库位/层位展示字段。
 */
public class TemperatureVO {
    private Long id;
    private Long locationId;
    private String locationCode;
    private String layerNo;
    private String warehouse;
    private LocalDateTime recordedAt;
    private BigDecimal measuredTemperature;
    private BigDecimal setTemperature;
    private BigDecimal deviation;
    private Integer alarmFlag;
    private Long tenantId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }
    public String getLayerNo() { return layerNo; }
    public void setLayerNo(String layerNo) { this.layerNo = layerNo; }
    public String getWarehouse() { return warehouse; }
    public void setWarehouse(String warehouse) { this.warehouse = warehouse; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
    public BigDecimal getMeasuredTemperature() { return measuredTemperature; }
    public void setMeasuredTemperature(BigDecimal measuredTemperature) { this.measuredTemperature = measuredTemperature; }
    public BigDecimal getSetTemperature() { return setTemperature; }
    public void setSetTemperature(BigDecimal setTemperature) { this.setTemperature = setTemperature; }
    public BigDecimal getDeviation() { return deviation; }
    public void setDeviation(BigDecimal deviation) { this.deviation = deviation; }
    public Integer getAlarmFlag() { return alarmFlag; }
    public void setAlarmFlag(Integer alarmFlag) { this.alarmFlag = alarmFlag; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
}
