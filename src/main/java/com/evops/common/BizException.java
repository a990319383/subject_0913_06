package com.evops.common;

/**
 * 业务规则异常：违反唯一键、状态流转、删除保护等业务约束时抛出，
 * 由全局异常处理器转换为统一失败返回。
 */
public class BizException extends RuntimeException {
    public BizException(String message) {
        super(message);
    }
}
