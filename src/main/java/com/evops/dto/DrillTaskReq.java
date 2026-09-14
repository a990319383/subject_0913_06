package com.evops.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 钻取任务创建/修改请求；taskNo 为业务键，创建后不可修改。
 */
public class DrillTaskReq {
    @NotBlank(message = "任务编号不能为空")
    @Size(max = 32, message = "任务编号长度不能超过32")
    private String taskNo;

    @NotBlank(message = "任务名称不能为空")
    @Size(max = 128, message = "任务名称长度不能超过128")
    private String taskName;

    @NotBlank(message = "钻取地点不能为空")
    @Size(max = 128, message = "钻取地点长度不能超过128")
    private String siteName;

    @Positive(message = "钻取深度必须大于0")
    private BigDecimal drillDepth;

    private LocalDate plannedStart;
    private LocalDate plannedEnd;

    @Size(max = 512, message = "备注长度不能超过512")
    private String remark;

    public String getTaskNo() { return taskNo; }
    public void setTaskNo(String taskNo) { this.taskNo = taskNo; }
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }
    public BigDecimal getDrillDepth() { return drillDepth; }
    public void setDrillDepth(BigDecimal drillDepth) { this.drillDepth = drillDepth; }
    public LocalDate getPlannedStart() { return plannedStart; }
    public void setPlannedStart(LocalDate plannedStart) { this.plannedStart = plannedStart; }
    public LocalDate getPlannedEnd() { return plannedEnd; }
    public void setPlannedEnd(LocalDate plannedEnd) { this.plannedEnd = plannedEnd; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
