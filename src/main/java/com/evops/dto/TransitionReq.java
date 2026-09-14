package com.evops.dto;

import javax.validation.constraints.NotBlank;

/**
 * 状态流转请求。
 */
public class TransitionReq {
    @NotBlank(message = "目标状态不能为空")
    private String targetStatus;

    public String getTargetStatus() { return targetStatus; }
    public void setTargetStatus(String targetStatus) { this.targetStatus = targetStatus; }
}
