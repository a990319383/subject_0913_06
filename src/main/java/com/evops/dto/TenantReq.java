package com.evops.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class TenantReq {
    @NotBlank(message = "租户编码不能为空")
    @Size(max = 32, message = "租户编码长度不能超过32")
    private String tenantCode;

    @NotBlank(message = "租户名称不能为空")
    @Size(max = 128, message = "租户名称长度不能超过128")
    private String tenantName;

    @Size(max = 512, message = "备注长度不能超过512")
    private String remark;

    public String getTenantCode() { return tenantCode; }
    public void setTenantCode(String tenantCode) { this.tenantCode = tenantCode; }
    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
