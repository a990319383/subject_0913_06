package com.evops.constant;

/**
 * 冰芯样本状态：STORED 已入库 -> IN_ANALYSIS 分析中 -> CONSUMED 已消耗。
 * 状态由分析批次生命周期驱动，不提供手工流转。
 */
public final class IceCoreSampleStatus {
    public static final String STORED = "STORED";
    public static final String IN_ANALYSIS = "IN_ANALYSIS";
    public static final String CONSUMED = "CONSUMED";

    private IceCoreSampleStatus() {
    }
}
