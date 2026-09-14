package com.evops.service;

import com.evops.common.page.PageResult;
import com.evops.dto.search.LayerSliceSearchQuery;
import com.evops.dto.search.SampleSearchQuery;
import com.evops.dto.search.TemperatureSearchQuery;
import com.evops.vo.LayerSliceVO;
import com.evops.vo.SampleSearchVO;
import com.evops.vo.TemperatureVO;

import java.util.Map;

/**
 * 极地冰芯样本库与冻融环境监测运营检索。
 * 所有检索：组合条件 AND、租户/角色数据权限强制、关联表 EXISTS 不放大主表、
 * 总数 + 确定性稳定排序 + offset/游标双分页（pageSize 1-100）。
 */
public interface OperationsSearchService {

    /** 冰芯样本组合检索（任务/样本盒/库位/批次/状态/日期范围/层位/深度/温度） */
    PageResult<SampleSearchVO> searchSamples(SampleSearchQuery query);

    /** 冰芯层位切片检索：按冰芯层位分区稳定分页（500,000 切片量级） */
    PageResult<LayerSliceVO> searchLayerSlices(LayerSliceSearchQuery query);

    /** 分钟级库温事件检索：时间倒序稳定分页（100,000 事件量级） */
    PageResult<TemperatureVO> searchTemperatures(TemperatureSearchQuery query);

    /** 库位层位分区汇总：每个层位的切片数量，按层位稳定排序 */
    PageResult<Map<String, Object>> layerPartitionSummary(Long locationId, Integer page, Integer size);
}
