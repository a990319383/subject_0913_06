package com.evops.controller;

import com.evops.common.ApiResponse;
import com.evops.common.page.PageResult;
import com.evops.dto.search.LayerSliceSearchQuery;
import com.evops.dto.search.SampleSearchQuery;
import com.evops.dto.search.TemperatureSearchQuery;
import com.evops.service.OperationsSearchService;
import com.evops.vo.LayerSliceVO;
import com.evops.vo.SampleSearchVO;
import com.evops.vo.TemperatureVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * 冰芯样本库与冻融环境监测运营检索入口。
 * 身份通过 X-Account-Id / X-Account-Username 请求头指定；缺省按平台运营处理。
 * 所有接口强制租户/角色数据权限，分页 size 限定 1-100。
 */
@RestController
@RequestMapping("/api/icecore/search")
public class OperationsSearchController {

    private final OperationsSearchService searchService;

    public OperationsSearchController(OperationsSearchService searchService) {
        this.searchService = searchService;
    }

    /** 冰芯样本组合检索：任务/样本盒/库位/批次/状态/日期范围/层位/深度/温度（AND 组合） */
    @GetMapping("/samples")
    public ApiResponse<PageResult<SampleSearchVO>> samples(
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) Long boxId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) String status,
            @RequestParam(name = "layerNo", required = false) List<String> layerNos,
            @RequestParam(required = false) String sampledFrom,
            @RequestParam(required = false) String sampledTo,
            @RequestParam(required = false) BigDecimal depthTopMin,
            @RequestParam(required = false) BigDecimal depthTopMax,
            @RequestParam(required = false) BigDecimal depthBottomMin,
            @RequestParam(required = false) BigDecimal depthBottomMax,
            @RequestParam(required = false) BigDecimal temperatureMin,
            @RequestParam(required = false) BigDecimal temperatureMax,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String sortOrder) {
        SampleSearchQuery q = new SampleSearchQuery();
        q.setTaskId(taskId);
        q.setBoxId(boxId);
        q.setLocationId(locationId);
        q.setBatchId(batchId);
        q.setStatus(status);
        q.setLayerNos(layerNos);
        q.setSampledFrom(parseStart(sampledFrom));
        q.setSampledTo(parseEnd(sampledTo));
        q.setDepthTopMin(depthTopMin);
        q.setDepthTopMax(depthTopMax);
        q.setDepthBottomMin(depthBottomMin);
        q.setDepthBottomMax(depthBottomMax);
        q.setTemperatureMin(temperatureMin);
        q.setTemperatureMax(temperatureMax);
        applyScroll(q, page, size, cursor);
        q.setSortOrder(sortOrder);
        return ApiResponse.ok(searchService.searchSamples(q));
    }

    /** 层位切片检索：库位按冰芯层位分区稳定分页 */
    @GetMapping("/slices")
    public ApiResponse<PageResult<LayerSliceVO>> slices(
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) Long sampleId,
            @RequestParam(name = "layerNo", required = false) List<String> layerNos,
            @RequestParam(required = false) BigDecimal depthMin,
            @RequestParam(required = false) BigDecimal depthMax,
            @RequestParam(required = false) BigDecimal temperatureMin,
            @RequestParam(required = false) BigDecimal temperatureMax,
            @RequestParam(required = false) String measuredFrom,
            @RequestParam(required = false) String measuredTo,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String cursor) {
        LayerSliceSearchQuery q = new LayerSliceSearchQuery();
        q.setLocationId(locationId);
        q.setSampleId(sampleId);
        q.setLayerNos(layerNos);
        q.setDepthMin(depthMin);
        q.setDepthMax(depthMax);
        q.setTemperatureMin(temperatureMin);
        q.setTemperatureMax(temperatureMax);
        q.setMeasuredFrom(parseStart(measuredFrom));
        q.setMeasuredTo(parseEnd(measuredTo));
        applyScroll(q, page, size, cursor);
        return ApiResponse.ok(searchService.searchLayerSlices(q));
    }

    /** 分钟级库温事件检索：时间倒序稳定分页 */
    @GetMapping("/temperatures")
    public ApiResponse<PageResult<TemperatureVO>> temperatures(
            @RequestParam(name = "locationId", required = false) List<Long> locationIds,
            @RequestParam(name = "layerNo", required = false) List<String> layerNos,
            @RequestParam(required = false) String recordedFrom,
            @RequestParam(required = false) String recordedTo,
            @RequestParam(required = false) Boolean alarmOnly,
            @RequestParam(required = false) BigDecimal minDeviation,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String cursor) {
        TemperatureSearchQuery q = new TemperatureSearchQuery();
        q.setLocationIds(locationIds);
        q.setLayerNos(layerNos);
        q.setRecordedFrom(parseStart(recordedFrom));
        q.setRecordedTo(parseEnd(recordedTo));
        q.setAlarmOnly(alarmOnly);
        q.setMinDeviation(minDeviation);
        applyScroll(q, page, size, cursor);
        return ApiResponse.ok(searchService.searchTemperatures(q));
    }

    /** 库位层位分区汇总：每个层位的切片数，按层位稳定排序 */
    @GetMapping("/partitions")
    public ApiResponse<PageResult<Map<String, Object>>> partitions(
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.ok(searchService.layerPartitionSummary(locationId, page, size));
    }

    private void applyScroll(com.evops.dto.search.SearchScroll q, Integer page, Integer size, String cursor) {
        q.setPage(page);
        q.setSize(size);
        q.setCursor(cursor);
    }

    /** 支持 yyyy-MM-dd（按当天零点起）或 ISO 日期时间 */
    private LocalDateTime parseStart(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(text.trim());
        } catch (DateTimeParseException ignore) {
            try {
                return LocalDate.parse(text.trim()).atStartOfDay();
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("时间格式非法: " + text);
            }
        }
    }

    /** 日期（yyyy-MM-dd）按含当日处理为次日零点（上界开区间）；日期时间原样使用 */
    private LocalDateTime parseEnd(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(text.trim());
        } catch (DateTimeParseException ignore) {
            try {
                return LocalDate.parse(text.trim()).plusDays(1).atStartOfDay();
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("时间格式非法: " + text);
            }
        }
    }
}
