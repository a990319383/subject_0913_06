package com.evops.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.evops.dto.SampleBoxReq;
import com.evops.entity.SampleBox;

import java.util.Map;

public interface SampleBoxService extends IService<SampleBox> {
    SampleBox create(SampleBoxReq req);

    SampleBox modify(Long id, SampleBoxReq req);

    SampleBox transit(Long id, String targetStatus);

    void deleteById(Long id);

    Page<SampleBox> pageQuery(Long taskId, Long locationId, String status, int page, int size);

    /** 样本盒详情：盒本体 + 所属任务 + 所在库位 + 盒内样本列表 */
    Map<String, Object> detail(Long id);

    /** 按盒内样本数量刷新 EMPTY/IN_USE/FULL 状态（已封存/已归档不触碰） */
    void refreshLoadStatus(Long boxId);
}
