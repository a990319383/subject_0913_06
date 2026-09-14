package com.evops.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 钻取任务
 */
@TableName("t_drill_task")
public class DrillTask extends BaseEntity {
    private String taskNo;
    private String taskName;
    private String siteName;
    private BigDecimal drillDepth;
    private String status;
    private LocalDate plannedStart;
    private LocalDate plannedEnd;
    private Long tenantId;
    private String remark;

    public String getTaskNo() { return taskNo; }
    public void setTaskNo(String taskNo) { this.taskNo = taskNo; }
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }
    public BigDecimal getDrillDepth() { return drillDepth; }
    public void setDrillDepth(BigDecimal drillDepth) { this.drillDepth = drillDepth; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getPlannedStart() { return plannedStart; }
    public void setPlannedStart(LocalDate plannedStart) { this.plannedStart = plannedStart; }
    public LocalDate getPlannedEnd() { return plannedEnd; }
    public void setPlannedEnd(LocalDate plannedEnd) { this.plannedEnd = plannedEnd; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
