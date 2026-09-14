package com.evops.constant;

/**
 * 账号角色：
 * PLATFORM      平台运营，跨租户可见全部对象
 * TENANT_ADMIN  租户管理员，可见本租户全部对象
 * TENANT_USER   租户成员，仅见对象级授权（t_data_grant）覆盖的对象
 */
public final class RoleCode {
    public static final String PLATFORM = "PLATFORM";
    public static final String TENANT_ADMIN = "TENANT_ADMIN";
    public static final String TENANT_USER = "TENANT_USER";

    private RoleCode() {
    }

    public static boolean isValid(String role) {
        return PLATFORM.equals(role) || TENANT_ADMIN.equals(role) || TENANT_USER.equals(role);
    }
}
