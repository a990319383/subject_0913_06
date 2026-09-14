package com.evops.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;

import java.time.LocalDateTime;

/**
 * 分析批次
 */
@TableName("t_analysis_batch")
public class AnalysisBatch extends BaseEntity {
    private String batchNo;
    private String batchName;
    private String analysisType;
    private String status;
    private LocalDateTime acceptedTime;
    private LocalDateTime postedTime;
    private String remark;

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public String getBatchName() { return batchName; }
    public void setBatchName(String batchName) { this.batchName = batchName; }
    public String getAnalysisType() { return analysisType; }
    public void setAnalysisType(String analysisType) { this.analysisType = analysisType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getAcceptedTime() { return acceptedTime; }
    public void setAcceptedTime(LocalDateTime acceptedTime) { this.acceptedTime = acceptedTime; }
    public LocalDateTime getPostedTime() { return postedTime; }
    public void setPostedTime(LocalDateTime postedTime) { this.postedTime = postedTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
