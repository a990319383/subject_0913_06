package com.evops.security;

/**
 * 按当前账号解析查询数据权限。
 */
public interface DataScopeService {
    /** 平台→ALL；租户管理员→TENANT；租户成员→GRANTED（由 SQL EXISTS 落到授权/关联表） */
    QueryScope currentScope();
}
