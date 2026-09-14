package com.evops.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.evops.common.BizException;
import com.evops.constant.ObjectType;
import com.evops.constant.RoleCode;
import com.evops.dto.AccountReq;
import com.evops.dto.GrantReq;
import com.evops.dto.TenantReq;
import com.evops.entity.AnalysisBatch;
import com.evops.entity.DataGrant;
import com.evops.entity.DrillTask;
import com.evops.entity.IceCoreSample;
import com.evops.entity.SampleBox;
import com.evops.entity.SecurityAccount;
import com.evops.entity.SecurityTenant;
import com.evops.entity.StorageLocation;
import com.evops.mapper.AnalysisBatchMapper;
import com.evops.mapper.DataGrantMapper;
import com.evops.mapper.DrillTaskMapper;
import com.evops.mapper.IceCoreSampleMapper;
import com.evops.mapper.SampleBoxMapper;
import com.evops.mapper.SecurityAccountMapper;
import com.evops.mapper.SecurityTenantMapper;
import com.evops.mapper.StorageLocationMapper;
import com.evops.service.TenantAdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class TenantAdminServiceImpl implements TenantAdminService {

    private final SecurityTenantMapper tenantMapper;
    private final SecurityAccountMapper accountMapper;
    private final DataGrantMapper grantMapper;
    private final DrillTaskMapper taskMapper;
    private final StorageLocationMapper locationMapper;
    private final SampleBoxMapper boxMapper;
    private final IceCoreSampleMapper sampleMapper;
    private final AnalysisBatchMapper batchMapper;

    public TenantAdminServiceImpl(SecurityTenantMapper tenantMapper,
                                  SecurityAccountMapper accountMapper,
                                  DataGrantMapper grantMapper,
                                  DrillTaskMapper taskMapper,
                                  StorageLocationMapper locationMapper,
                                  SampleBoxMapper boxMapper,
                                  IceCoreSampleMapper sampleMapper,
                                  AnalysisBatchMapper batchMapper) {
        this.tenantMapper = tenantMapper;
        this.accountMapper = accountMapper;
        this.grantMapper = grantMapper;
        this.taskMapper = taskMapper;
        this.locationMapper = locationMapper;
        this.boxMapper = boxMapper;
        this.sampleMapper = sampleMapper;
        this.batchMapper = batchMapper;
    }

    @Override
    public SecurityTenant createTenant(TenantReq req) {
        if (exists(tenantMapper.selectCount(new LambdaQueryWrapper<SecurityTenant>()
                .eq(SecurityTenant::getTenantCode, req.getTenantCode())))) {
            throw new BizException("租户编码已存在: " + req.getTenantCode());
        }
        SecurityTenant tenant = new SecurityTenant();
        tenant.setTenantCode(req.getTenantCode());
        tenant.setTenantName(req.getTenantName());
        tenant.setStatus("ACTIVE");
        tenant.setRemark(req.getRemark());
        tenantMapper.insert(tenant);
        return tenant;
    }

    @Override
    public SecurityAccount createAccount(AccountReq req) {
        if (!RoleCode.isValid(req.getRoleCode())) {
            throw new BizException("角色非法，只支持 PLATFORM/TENANT_ADMIN/TENANT_USER");
        }
        if (exists(accountMapper.selectCount(new LambdaQueryWrapper<SecurityAccount>()
                .eq(SecurityAccount::getUsername, req.getUsername())))) {
            throw new BizException("账号名已存在: " + req.getUsername());
        }
        if (!RoleCode.PLATFORM.equals(req.getRoleCode())) {
            if (req.getTenantId() == null) {
                throw new BizException("租户角色必须指定租户");
            }
            if (tenantMapper.selectById(req.getTenantId()) == null) {
                throw new BizException("租户不存在: " + req.getTenantId());
            }
        }
        SecurityAccount account = new SecurityAccount();
        account.setUsername(req.getUsername());
        account.setRoleCode(req.getRoleCode());
        account.setTenantId(RoleCode.PLATFORM.equals(req.getRoleCode()) ? null : req.getTenantId());
        account.setStatus("ACTIVE");
        account.setRemark(req.getRemark());
        accountMapper.insert(account);
        return account;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int grant(GrantReq req) {
        String type = normalizeType(req.getObjectType());
        SecurityAccount account = mustAccount(req.getAccountId());
        Set<Long> ids = normalizeIds(req.getObjectIds());
        int created = 0;
        for (Long objectId : ids) {
            ensureObjectExists(type, objectId);
            Long duplicated = grantMapper.selectCount(new LambdaQueryWrapper<DataGrant>()
                    .eq(DataGrant::getAccountId, account.getId())
                    .eq(DataGrant::getObjectType, type)
                    .eq(DataGrant::getObjectId, objectId));
            if (exists(duplicated)) {
                continue;
            }
            DataGrant grant = new DataGrant();
            grant.setAccountId(account.getId());
            grant.setObjectType(type);
            grant.setObjectId(objectId);
            // 授权行冗余租户，便于按租户审计
            grant.setTenantId(account.getTenantId() == null ? 0L : account.getTenantId());
            grantMapper.insert(grant);
            created++;
        }
        return created;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int revoke(GrantReq req) {
        String type = normalizeType(req.getObjectType());
        Set<Long> ids = normalizeIds(req.getObjectIds());
        int removed = 0;
        for (Long objectId : ids) {
            removed += grantMapper.delete(new LambdaQueryWrapper<DataGrant>()
                    .eq(DataGrant::getAccountId, req.getAccountId())
                    .eq(DataGrant::getObjectType, type)
                    .eq(DataGrant::getObjectId, objectId));
        }
        return removed;
    }

    @Override
    public List<DataGrant> listGrants(Long accountId) {
        mustAccount(accountId);
        return grantMapper.selectList(new LambdaQueryWrapper<DataGrant>()
                .eq(DataGrant::getAccountId, accountId)
                .orderByAsc(DataGrant::getObjectType)
                .orderByAsc(DataGrant::getObjectId));
    }

    @Override
    public List<Map<String, Object>> listAccounts() {
        List<SecurityAccount> accounts = accountMapper.selectList(
                new LambdaQueryWrapper<SecurityAccount>().orderByAsc(SecurityAccount::getId));
        List<Map<String, Object>> views = new ArrayList<>();
        for (SecurityAccount account : accounts) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", account.getId());
            row.put("username", account.getUsername());
            row.put("tenantId", account.getTenantId());
            row.put("roleCode", account.getRoleCode());
            row.put("status", account.getStatus());
            views.add(row);
        }
        return views;
    }

    private String normalizeType(String type) {
        if (!ObjectType.isValid(type)) {
            throw new BizException("对象类型非法，只支持 TASK/LOCATION/BOX/SAMPLE/BATCH");
        }
        return type;
    }

    private Set<Long> normalizeIds(List<Long> objectIds) {
        if (objectIds == null || objectIds.isEmpty()) {
            throw new BizException("对象ID集合不能为空");
        }
        Set<Long> ids = new LinkedHashSet<>(objectIds);
        if (ids.size() > 500) {
            throw new BizException("单次授权对象不能超过 500 个");
        }
        return ids;
    }

    private SecurityAccount mustAccount(Long accountId) {
        SecurityAccount account = accountMapper.selectById(accountId);
        if (account == null) {
            throw new BizException("账号不存在: " + accountId);
        }
        return account;
    }

    private void ensureObjectExists(String type, Long objectId) {
        Object ref;
        switch (type) {
            case ObjectType.TASK:
                ref = taskMapper.selectById(objectId);
                break;
            case ObjectType.LOCATION:
                ref = locationMapper.selectById(objectId);
                break;
            case ObjectType.BOX:
                ref = boxMapper.selectById(objectId);
                break;
            case ObjectType.SAMPLE:
                ref = sampleMapper.selectById(objectId);
                break;
            case ObjectType.BATCH:
                ref = batchMapper.selectById(objectId);
                break;
            default:
                ref = null;
        }
        if (ref == null) {
            throw new BizException(type + " 对象不存在: " + objectId);
        }
    }

    private boolean exists(Long count) {
        return count != null && count > 0;
    }
}
