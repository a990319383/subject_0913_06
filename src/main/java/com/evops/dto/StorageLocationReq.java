package com.evops.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 库位创建/修改请求；locationCode 为业务键，创建后不可修改。
 */
public class StorageLocationReq {
    @NotBlank(message = "库位编码不能为空")
    @Size(max = 32, message = "库位编码长度不能超过32")
    private String locationCode;

    @NotBlank(message = "冷库名称不能为空")
    @Size(max = 64, message = "冷库名称长度不能超过64")
    private String warehouse;

    @Size(max = 32, message = "货架号长度不能超过32")
    private String shelfNo;

    @Size(max = 32, message = "层号长度不能超过32")
    private String layerNo;

    private BigDecimal setTemperature;

    @NotNull(message = "库位容量不能为空")
    @Min(value = 1, message = "库位容量至少为1")
    private Integer capacity;

    @Size(max = 512, message = "备注长度不能超过512")
    private String remark;

    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }
    public String getWarehouse() { return warehouse; }
    public void setWarehouse(String warehouse) { this.warehouse = warehouse; }
    public String getShelfNo() { return shelfNo; }
    public void setShelfNo(String shelfNo) { this.shelfNo = shelfNo; }
    public String getLayerNo() { return layerNo; }
    public void setLayerNo(String layerNo) { this.layerNo = layerNo; }
    public BigDecimal getSetTemperature() { return setTemperature; }
    public void setSetTemperature(BigDecimal setTemperature) { this.setTemperature = setTemperature; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
