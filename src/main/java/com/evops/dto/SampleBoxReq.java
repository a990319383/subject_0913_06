package com.evops.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 样本盒创建/修改请求；boxNo 为业务键，创建后不可修改。
 */
public class SampleBoxReq {
    @NotBlank(message = "样本盒编号不能为空")
    @Size(max = 32, message = "样本盒编号长度不能超过32")
    private String boxNo;

    @NotNull(message = "所属钻取任务不能为空")
    private Long taskId;

    private Long locationId;

    @NotNull(message = "样本盒容量不能为空")
    @Min(value = 1, message = "样本盒容量至少为1")
    private Integer capacity;

    @Size(max = 512, message = "备注长度不能超过512")
    private String remark;

    public String getBoxNo() { return boxNo; }
    public void setBoxNo(String boxNo) { this.boxNo = boxNo; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
