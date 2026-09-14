package com.evops.constant;

/**
 * 数据权限对象类型：钻取任务 / 库位 / 样本盒 / 冰芯样本 / 分析批次
 */
public final class ObjectType {
    public static final String TASK = "TASK";
    public static final String LOCATION = "LOCATION";
    public static final String BOX = "BOX";
    public static final String SAMPLE = "SAMPLE";
    public static final String BATCH = "BATCH";

    private ObjectType() {
    }

    public static boolean isValid(String type) {
        return TASK.equals(type) || LOCATION.equals(type) || BOX.equals(type)
                || SAMPLE.equals(type) || BATCH.equals(type);
    }
}
