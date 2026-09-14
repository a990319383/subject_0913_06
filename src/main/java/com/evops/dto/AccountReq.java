package com.evops.dto;

import com.evops.constant.RoleCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class AccountReq {
    @NotBlank(message = "账号名不能为空")
    @Size(max = 64, message = "账号名长度不能超过64")
    private String username;

    /** PLATFORM 可为空；租户角色必填 */
    private Long tenantId;

    @NotBlank(message = "角色不能为空")
    private String roleCode;

    @Size(max = 512, message = "备注长度不能超过512")
    private String remark;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public boolean platformRole() {
        return RoleCode.PLATFORM.equals(roleCode);
    }
}
