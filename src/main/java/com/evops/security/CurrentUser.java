package com.evops.security;

import com.evops.constant.RoleCode;

/**
 * 当前请求的账号身份。accountId 为 null 表示平台运营/未携带身份（跨租户全量）。
 */
public class CurrentUser {
    private final Long accountId;
    private final Long tenantId;
    private final String role;

    public CurrentUser(Long accountId, Long tenantId, String role) {
        this.accountId = accountId;
        this.tenantId = tenantId;
        this.role = role;
    }

    /** 平台运营身份：不受租户与对象授权限制 */
    public static CurrentUser platform() {
        return new CurrentUser(null, 0L, RoleCode.PLATFORM);
    }

    public Long getAccountId() { return accountId; }
    public Long getTenantId() { return tenantId; }
    public String getRole() { return role; }

    public boolean isPlatform() { return RoleCode.PLATFORM.equals(role); }
    public boolean isTenantAdmin() { return RoleCode.TENANT_ADMIN.equals(role); }
    public boolean isTenantUser() { return RoleCode.TENANT_USER.equals(role); }
}
