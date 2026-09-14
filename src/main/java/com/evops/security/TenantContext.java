package com.evops.security;

/**
 * 基于 ThreadLocal 的当前账号身份，由 AccountContextFilter 在请求入口写入、出口清理。
 * 服务层单测/压测可通过 runAs 临时切换身份。
 */
public final class TenantContext {
    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    private TenantContext() {
    }

    public static CurrentUser get() {
        return HOLDER.get();
    }

    /** 未携带身份时按平台运营处理，保证历史数据（tenant_id=0）与既有闭环可被运营侧检索 */
    public static CurrentUser getOrPlatform() {
        CurrentUser user = HOLDER.get();
        return user == null ? CurrentUser.platform() : user;
    }

    public static void set(CurrentUser user) {
        HOLDER.set(user);
    }

    public static void clear() {
        HOLDER.remove();
    }

    /** 当前账号所属租户；平台运营或无身份时为 0（历史/平台数据） */
    public static long currentTenantId() {
        CurrentUser user = HOLDER.get();
        if (user == null || user.getTenantId() == null) {
            return 0L;
        }
        return user.getTenantId();
    }

    public static void runAs(CurrentUser user, Runnable action) {
        CurrentUser previous = HOLDER.get();
        HOLDER.set(user);
        try {
            action.run();
        } finally {
            if (previous == null) {
                HOLDER.remove();
            } else {
                HOLDER.set(previous);
            }
        }
    }
}
