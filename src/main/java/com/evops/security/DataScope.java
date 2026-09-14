package com.evops.security;

import java.util.Collections;
import java.util.List;

/**
 * 一次查询针对某类对象解析出的数据权限：
 * ALL     平台运营，不加权限条件
 * TENANT  租户管理员，限定 tenant_id
 * GRANTED 租户成员，限定对象主键在授权集合内
 * NONE    无任何授权，查询必须返回空
 */
public class DataScope {
    public enum Level {ALL, TENANT, GRANTED, NONE}

    private final Level level;
    private final Long tenantId;
    private final List<Long> objectIds;

    private DataScope(Level level, Long tenantId, List<Long> objectIds) {
        this.level = level;
        this.tenantId = tenantId;
        this.objectIds = objectIds;
    }

    public static DataScope all() {
        return new DataScope(Level.ALL, null, Collections.emptyList());
    }

    public static DataScope tenant(Long tenantId) {
        return new DataScope(Level.TENANT, tenantId, Collections.emptyList());
    }

    public static DataScope granted(List<Long> ids) {
        return new DataScope(Level.GRANTED, null,
                ids == null ? Collections.emptyList() : ids);
    }

    public static DataScope none() {
        return new DataScope(Level.NONE, null, Collections.emptyList());
    }

    public Level getLevel() { return level; }
    public Long getTenantId() { return tenantId; }
    public List<Long> getObjectIds() { return objectIds; }
}
