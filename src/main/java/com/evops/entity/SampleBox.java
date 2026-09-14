package com.evops.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;

import java.time.LocalDateTime;

/**
 * 样本盒
 */
@TableName("t_sample_box")
public class SampleBox extends BaseEntity {
    private String boxNo;
    private Long taskId;
    private Long locationId;
    private Integer capacity;
    private String status;
    private LocalDateTime sealedTime;
    private String remark;

    public String getBoxNo() { return boxNo; }
    public void setBoxNo(String boxNo) { this.boxNo = boxNo; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getSealedTime() { return sealedTime; }
    public void setSealedTime(LocalDateTime sealedTime) { this.sealedTime = sealedTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
