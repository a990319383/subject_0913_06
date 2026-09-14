package com.evops.dto;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 冰芯样本登记/测量修改请求；sampleNo 为业务键，创建后不可修改。
 * 样本归属任务与库位由样本盒决定，请求只需给出 boxId。
 */
public class IceCoreSampleReq {
    @NotBlank(message = "样本编号不能为空")
    @Size(max = 32, message = "样本编号长度不能超过32")
    private String sampleNo;

    @NotNull(message = "所属样本盒不能为空")
    private Long boxId;

    @NotBlank(message = "层位不能为空")
    @Size(max = 32, message = "层位长度不能超过32")
    private String layerNo;

    private BigDecimal depthTop;
    private BigDecimal depthBottom;
    private BigDecimal temperature;

    @DecimalMin(value = "0", message = "融水量不能为负")
    private BigDecimal meltWaterMl;

    @DecimalMin(value = "0", message = "取样完整度不能小于0")
    @DecimalMax(value = "100", message = "取样完整度不能大于100")
    private BigDecimal integrityPct;

    private LocalDateTime sampledAt;

    @Size(max = 512, message = "备注长度不能超过512")
    private String remark;

    public String getSampleNo() { return sampleNo; }
    public void setSampleNo(String sampleNo) { this.sampleNo = sampleNo; }
    public Long getBoxId() { return boxId; }
    public void setBoxId(Long boxId) { this.boxId = boxId; }
    public String getLayerNo() { return layerNo; }
    public void setLayerNo(String layerNo) { this.layerNo = layerNo; }
    public BigDecimal getDepthTop() { return depthTop; }
    public void setDepthTop(BigDecimal depthTop) { this.depthTop = depthTop; }
    public BigDecimal getDepthBottom() { return depthBottom; }
    public void setDepthBottom(BigDecimal depthBottom) { this.depthBottom = depthBottom; }
    public BigDecimal getTemperature() { return temperature; }
    public void setTemperature(BigDecimal temperature) { this.temperature = temperature; }
    public BigDecimal getMeltWaterMl() { return meltWaterMl; }
    public void setMeltWaterMl(BigDecimal meltWaterMl) { this.meltWaterMl = meltWaterMl; }
    public BigDecimal getIntegrityPct() { return integrityPct; }
    public void setIntegrityPct(BigDecimal integrityPct) { this.integrityPct = integrityPct; }
    public LocalDateTime getSampledAt() { return sampledAt; }
    public void setSampledAt(LocalDateTime sampledAt) { this.sampledAt = sampledAt; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
