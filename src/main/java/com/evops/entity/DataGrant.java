package com.evops.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;

/**
 * 对象级数据授权：把 TASK/LOCATION/BOX/SAMPLE/BATCH 授权给账号
 */
@TableName("t_data_grant")
public class DataGrant extends BaseEntity {
    private Long accountId;
    private Long tenantId;
    private String objectType;
    private Long objectId;

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getObjectType() { return objectType; }
    public void setObjectType(String objectType) { this.objectType = objectType; }
    public Long getObjectId() { return objectId; }
    public void setObjectId(Long objectId) { this.objectId = objectId; }
}
