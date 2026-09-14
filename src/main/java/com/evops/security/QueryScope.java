package com.evops.security;

/**
 * 传给 Mapper 的查询级数据权限描述。GRANTED 不在应用层展开主键列表，
 * 而是由 XML 用 t_data_grant / 关联表的 EXISTS 半连接实现，避免授权对象很多时 IN 列表膨胀。
 */
public class QueryScope {
    /** ALL / TENANT / GRANTED / NONE */
    private final String level;
    private final Long tenantId;
    private final Long accountId;

    private QueryScope(String level, Long tenantId, Long accountId) {
        this.level = level;
        this.tenantId = tenantId;
        this.accountId = accountId;
    }

    public static QueryScope all() {
        return new QueryScope("ALL", null, null);
    }

    public static QueryScope tenant(Long tenantId) {
        return new QueryScope("TENANT", tenantId, null);
    }

    public static QueryScope granted(Long accountId) {
        return new QueryScope("GRANTED", null, accountId);
    }

    public static QueryScope none() {
        return new QueryScope("NONE", null, null);
    }

    public String getLevel() { return level; }
    public Long getTenantId() { return tenantId; }
    public Long getAccountId() { return accountId; }
}
