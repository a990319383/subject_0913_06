package com.evops.dto;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 向批次追加样本的请求。
 */
public class BatchSamplesReq {
    @NotEmpty(message = "样本列表不能为空")
    private List<Long> sampleIds;

    public List<Long> getSampleIds() { return sampleIds; }
    public void setSampleIds(List<Long> sampleIds) { this.sampleIds = sampleIds; }
}
