package com.evops.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

/**
 * 既有单表分页查询的数据权限条件构造。
 * ALL 不加条件；TENANT 限定 tenant_id；GRANTED 用 t_data_grant 直接授权或
 * 经任务/样本盒/库位间接授权的 EXISTS 表达；NONE 由调用方直接返回空页。
 * 全部走相关 EXISTS/IN 半连接，不会因授权一对多放大主表。
 */
public final class DataScopeFilters {

    private DataScopeFilters() {
    }

    public static boolean isNone(QueryScope scope) {
        return "NONE".equals(scope.getLevel());
    }

    private static String directGrant(String mainIdColumn, String objectType) {
        return "EXISTS (SELECT 1 FROM t_data_grant dg WHERE dg.account_id = {0} "
                + "AND dg.object_type = '" + objectType + "' AND dg.object_id = " + mainIdColumn + ")";
    }

    /** 钻取任务：TENANT 限租户；GRANTED 需 TASK 直接授权 */
    public static <T> void applyTask(LambdaQueryWrapper<T> w, QueryScope scope) {
        if ("TENANT".equals(scope.getLevel())) {
            w.apply("tenant_id = {0}", scope.getTenantId());
        } else if ("GRANTED".equals(scope.getLevel())) {
            w.apply(directGrant("t_drill_task.id", com.evops.constant.ObjectType.TASK),
                    scope.getAccountId());
        }
    }

    /** 库位：TENANT 限租户；GRANTED 需 LOCATION 直接授权 */
    public static <T> void applyLocation(LambdaQueryWrapper<T> w, QueryScope scope) {
        if ("TENANT".equals(scope.getLevel())) {
            w.apply("tenant_id = {0}", scope.getTenantId());
        } else if ("GRANTED".equals(scope.getLevel())) {
            w.apply(directGrant("t_storage_location.id", com.evops.constant.ObjectType.LOCATION),
                    scope.getAccountId());
        }
    }

    /** 样本盒：GRANTED 需 BOX 直接授权，或其所属任务被授权 */
    public static <T> void applyBox(LambdaQueryWrapper<T> w, QueryScope scope) {
        if ("TENANT".equals(scope.getLevel())) {
            w.apply("tenant_id = {0}", scope.getTenantId());
        } else if ("GRANTED".equals(scope.getLevel())) {
            Long accountId = scope.getAccountId();
            w.and(q -> q.apply(directGrant("t_sample_box.id", com.evops.constant.ObjectType.BOX), accountId)
                    .or().apply("EXISTS (SELECT 1 FROM t_data_grant dg WHERE dg.account_id = {0} "
                            + "AND dg.object_type = 'TASK' AND dg.object_id = t_sample_box.task_id)", accountId));
        }
    }

    /** 冰芯样本：GRANTED 需 SAMPLE 直接授权，或其盒/任务/库位被授权 */
    public static <T> void applySample(LambdaQueryWrapper<T> w, QueryScope scope) {
        if ("TENANT".equals(scope.getLevel())) {
            w.apply("tenant_id = {0}", scope.getTenantId());
        } else if ("GRANTED".equals(scope.getLevel())) {
            Long accountId = scope.getAccountId();
            w.and(q -> q.apply(directGrant("t_ice_core_sample.id", com.evops.constant.ObjectType.SAMPLE), accountId)
                    .or().apply("EXISTS (SELECT 1 FROM t_data_grant dg WHERE dg.account_id = {0} "
                            + "AND dg.object_type = 'BOX' AND dg.object_id = t_ice_core_sample.box_id)", accountId)
                    .or().apply("EXISTS (SELECT 1 FROM t_data_grant dg WHERE dg.account_id = {0} "
                            + "AND dg.object_type = 'TASK' AND dg.object_id = t_ice_core_sample.task_id)", accountId)
                    .or().apply("EXISTS (SELECT 1 FROM t_data_grant dg WHERE dg.account_id = {0} "
                            + "AND dg.object_type = 'LOCATION' AND dg.object_id = t_ice_core_sample.location_id)",
                            accountId));
        }
    }

    /**
     * 分析批次（无 tenant_id）：TENANT 管理员可见“内含本租户样本”的批次；
     * GRANTED 需 BATCH 直接授权，或批次内存在其被授权样本。
     */
    public static <T> void applyBatch(LambdaQueryWrapper<T> w, QueryScope scope) {
        if ("TENANT".equals(scope.getLevel())) {
            w.apply("EXISTS (SELECT 1 FROM t_analysis_batch_item bi "
                    + "JOIN t_ice_core_sample s ON s.id = bi.sample_id "
                    + "WHERE bi.batch_id = t_analysis_batch.id AND s.tenant_id = {0})", scope.getTenantId());
        } else if ("GRANTED".equals(scope.getLevel())) {
            Long accountId = scope.getAccountId();
            w.and(q -> q.apply(directGrant("t_analysis_batch.id", com.evops.constant.ObjectType.BATCH), accountId)
                    .or().apply("EXISTS (SELECT 1 FROM t_analysis_batch_item bi "
                            + "JOIN t_ice_core_sample s ON s.id = bi.sample_id "
                            + "JOIN t_data_grant dg ON dg.object_id = s.id AND dg.object_type = 'SAMPLE' "
                            + "WHERE dg.account_id = {0} AND bi.batch_id = t_analysis_batch.id)", accountId));
        }
    }
}
