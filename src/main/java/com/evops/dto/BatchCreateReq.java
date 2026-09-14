package com.evops.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 分析批次建立请求；batchNo 为业务键。
 */
public class BatchCreateReq {
    @NotBlank(message = "批次编号不能为空")
    @Size(max = 32, message = "批次编号长度不能超过32")
    private String batchNo;

    @NotBlank(message = "批次名称不能为空")
    @Size(max = 128, message = "批次名称长度不能超过128")
    private String batchName;

    @Size(max = 64, message = "分析类型长度不能超过64")
    private String analysisType;

    @NotEmpty(message = "批次样本不能为空")
    private List<Long> sampleIds;

    @Size(max = 512, message = "备注长度不能超过512")
    private String remark;

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public String getBatchName() { return batchName; }
    public void setBatchName(String batchName) { this.batchName = batchName; }
    public String getAnalysisType() { return analysisType; }
    public void setAnalysisType(String analysisType) { this.analysisType = analysisType; }
    public List<Long> getSampleIds() { return sampleIds; }
    public void setSampleIds(List<Long> sampleIds) { this.sampleIds = sampleIds; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
