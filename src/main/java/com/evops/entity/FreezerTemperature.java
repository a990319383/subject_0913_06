package com.evops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 分钟级冻库库温事件：每个库位按分钟记录实测温度、设定温度、偏差与告警标记。
 * 仅 create_time，不继承 BaseEntity 的审计列。
 */
@TableName("t_freezer_temperature")
public class FreezerTemperature {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long locationId;
    private Long tenantId;
    private LocalDateTime recordedAt;
    private BigDecimal measuredTemperature;
    private BigDecimal setTemperature;
    private BigDecimal deviation;
    private Integer alarmFlag;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
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
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
