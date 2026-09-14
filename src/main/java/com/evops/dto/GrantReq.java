package com.evops.dto;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 给账号批量授予/回收对象授权。
 */
public class GrantReq {
    @NotNull(message = "账号不能为空")
    private Long accountId;

    @NotNull(message = "对象类型不能为空")
    private String objectType;

    @NotNull(message = "对象ID集合不能为空")
    private List<Long> objectIds;

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public String getObjectType() { return objectType; }
    public void setObjectType(String objectType) { this.objectType = objectType; }
    public List<Long> getObjectIds() { return objectIds; }
    public void setObjectIds(List<Long> objectIds) { this.objectIds = objectIds; }
}
