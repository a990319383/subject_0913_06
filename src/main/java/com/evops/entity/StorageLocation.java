package com.evops.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;

import java.math.BigDecimal;

/**
 * 冻库库位
 */
@TableName("t_storage_location")
public class StorageLocation extends BaseEntity {
    private String locationCode;
    private String warehouse;
    private String shelfNo;
    private String layerNo;
    private BigDecimal setTemperature;
    private Integer capacity;
    private String status;
    private Long tenantId;
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
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
