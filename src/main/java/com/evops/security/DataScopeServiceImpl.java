package com.evops.security;

import org.springframework.stereotype.Service;

@Service
public class DataScopeServiceImpl implements DataScopeService {

    @Override
    public QueryScope currentScope() {
        CurrentUser user = TenantContext.getOrPlatform();
        if (user.isPlatform()) {
            return QueryScope.all();
        }
        if (user.isTenantAdmin()) {
            return QueryScope.tenant(user.getTenantId());
        }
        // 租户成员：具体可见行由 t_data_grant 直接授权或经任务/库位/样本盒间接授权，
        // 在 Mapper XML 中以相关 EXISTS 表达，保证每次查询都强制数据权限且不放大主表。
        return QueryScope.granted(user.getAccountId());
    }
}
