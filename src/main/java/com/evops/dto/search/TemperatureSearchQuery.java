package com.evops.dto.search;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 分钟级库温事件检索：库位（可多个）、层位集合、记录时间范围、仅告警、最小偏差。
 * 排序固定 recorded_at DESC, id DESC；游标锚点为 (lastRecordedAt, lastId)。
 */
public class TemperatureSearchQuery extends SearchScroll {
    private List<Long> locationIds;
    private List<String> layerNos;
    private LocalDateTime recordedFrom;
    private LocalDateTime recordedTo;
    private Boolean alarmOnly;
    private BigDecimal minDeviation;

    /** 游标锚点：上一页最后一条的记录时间 */
    private LocalDateTime lastRecordedAt;

    public List<Long> getLocationIds() { return locationIds; }
    public void setLocationIds(List<Long> locationIds) { this.locationIds = locationIds; }
    public List<String> getLayerNos() { return layerNos; }
    public void setLayerNos(List<String> layerNos) { this.layerNos = layerNos; }
    public LocalDateTime getRecordedFrom() { return recordedFrom; }
    public void setRecordedFrom(LocalDateTime recordedFrom) { this.recordedFrom = recordedFrom; }
    public LocalDateTime getRecordedTo() { return recordedTo; }
    public void setRecordedTo(LocalDateTime recordedTo) { this.recordedTo = recordedTo; }
    public Boolean getAlarmOnly() { return alarmOnly; }
    public void setAlarmOnly(Boolean alarmOnly) { this.alarmOnly = alarmOnly; }
    public BigDecimal getMinDeviation() { return minDeviation; }
    public void setMinDeviation(BigDecimal minDeviation) { this.minDeviation = minDeviation; }
    public LocalDateTime getLastRecordedAt() { return lastRecordedAt; }
    public void setLastRecordedAt(LocalDateTime lastRecordedAt) { this.lastRecordedAt = lastRecordedAt; }
}
