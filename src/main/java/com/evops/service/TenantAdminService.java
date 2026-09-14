package com.evops.service;

import com.evops.dto.AccountReq;
import com.evops.dto.GrantReq;
import com.evops.dto.TenantReq;
import com.evops.entity.DataGrant;
import com.evops.entity.SecurityAccount;
import com.evops.entity.SecurityTenant;

import java.util.List;
import java.util.Map;

/**
 * 多租户、账号与对象授权管理（运营侧）。
 */
public interface TenantAdminService {

    SecurityTenant createTenant(TenantReq req);

    SecurityAccount createAccount(AccountReq req);

    /** 批量授权；自动补齐授权行 tenant_id，并校验对象存在 */
    int grant(GrantReq req);

    /** 回收授权 */
    int revoke(GrantReq req);

    List<DataGrant> listGrants(Long accountId);

    List<Map<String, Object>> listAccounts();
}
