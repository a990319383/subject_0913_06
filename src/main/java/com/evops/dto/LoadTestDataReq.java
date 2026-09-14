package com.evops.dto;

/**
 * 压测数据播种参数：分钟级库温事件数（默认 100,000）、层位切片数（默认 500,000）。
 */
public class LoadTestDataReq {
    private Integer temperatureEvents;
    private Integer sliceCount;
    /** 是否先清掉历史压测数据（按压测专用租户清理） */
    private Boolean reset;
    /** 播种后是否立即执行分页基准并返回耗时 */
    private Boolean benchmark;

    public Integer getTemperatureEvents() { return temperatureEvents; }
    public void setTemperatureEvents(Integer temperatureEvents) { this.temperatureEvents = temperatureEvents; }
    public Integer getSliceCount() { return sliceCount; }
    public void setSliceCount(Integer sliceCount) { this.sliceCount = sliceCount; }
    public Boolean getReset() { return reset; }
    public void setReset(Boolean reset) { this.reset = reset; }
    public Boolean getBenchmark() { return benchmark; }
    public void setBenchmark(Boolean benchmark) { this.benchmark = benchmark; }
}
