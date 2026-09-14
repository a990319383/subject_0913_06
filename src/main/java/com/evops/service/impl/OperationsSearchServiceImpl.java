package com.evops.service.impl;

import com.evops.common.BizException;
import com.evops.common.page.Cursors;
import com.evops.common.page.PageParams;
import com.evops.common.page.PageResult;
import com.evops.dto.search.LayerSliceSearchQuery;
import com.evops.dto.search.SampleSearchQuery;
import com.evops.dto.search.TemperatureSearchQuery;
import com.evops.mapper.OperationsSearchMapper;
import com.evops.security.DataScope;
import com.evops.security.DataScopeService;
import com.evops.security.QueryScope;
import com.evops.service.OperationsSearchService;
import com.evops.vo.LayerSliceVO;
import com.evops.vo.SampleSearchVO;
import com.evops.vo.TemperatureVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 运营检索实现：
 * - 条件 AND 组合、范围闭/半开区间，层位 IN 集合；
 * - 每次查询都经 DataScopeService 解析租户/角色权限，NONE 直接返回空；
 * - 关联表一对多只用 EXISTS 半连接，主表记录不放大；
 * - offset 与 keyset 双分页，排序均以确定性主键收尾，深分页不丢不重。
 */
@Service
public class OperationsSearchServiceImpl implements OperationsSearchService {

    /** IN 集合上限，防止超长 IN 列表拖垮计划 */
    private static final int MAX_IN_VALUES = 200;

    private final OperationsSearchMapper mapper;
    private final DataScopeService dataScopeService;

    public OperationsSearchServiceImpl(OperationsSearchMapper mapper, DataScopeService dataScopeService) {
        this.mapper = mapper;
        this.dataScopeService = dataScopeService;
    }

    @Override
    public PageResult<SampleSearchVO> searchSamples(SampleSearchQuery q) {
        PageParams params = PageParams.of(q.getPage(), q.getSize(), q.getCursor());
        q.setSortOrder("ASC".equalsIgnoreCase(q.getSortOrder()) ? "ASC" : "DESC");
        q.setLayerNos(normalizeStrings(q.getLayerNos()));

        QueryScope scope = dataScopeService.currentScope();
        if (isNone(scope)) {
            return empty(params);
        }

        if (params.isCursorMode()) {
            q.setCursorMode(true);
            if (params.getCursor() != null) {
                List<String> anchor = decodeCursor(params.getCursor(), 1);
                q.setLastId(parseId(anchor.get(0)));
            }
            q.setLimit(params.fetchLimit());
            List<SampleSearchVO> rows = mapper.searchSamples(q, scope);
            boolean hasMore = rows.size() > params.getSize();
            if (hasMore) {
                rows = new ArrayList<>(rows.subList(0, params.getSize()));
            }
            String next = hasMore && !rows.isEmpty()
                    ? Cursors.encode(Collections.singletonList(String.valueOf(rows.get(rows.size() - 1).getId())))
                    : null;
            // 游标模式仍返回全量总数，便于前端展示，且总数不含游标谓词
            long total = mapper.countSamples(q, scope);
            return new PageResult<>(rows, total, params.getSize(), 0, hasMore, next);
        }

        q.setCursorMode(false);
        q.setOffset(params.offset());
        q.setLimit(params.getSize());
        long total = mapper.countSamples(q, scope);
        List<SampleSearchVO> rows = total == 0 ? Collections.emptyList() : mapper.searchSamples(q, scope);
        boolean hasMore = params.offset() + rows.size() < total;
        return new PageResult<>(rows, total, params.getSize(), params.getPage(), hasMore, null);
    }

    @Override
    public PageResult<LayerSliceVO> searchLayerSlices(LayerSliceSearchQuery q) {
        PageParams params = PageParams.of(q.getPage(), q.getSize(), q.getCursor());
        q.setLayerNos(normalizeStrings(q.getLayerNos()));
        QueryScope scope = dataScopeService.currentScope();
        if (isNone(scope)) {
            return empty(params);
        }

        if (params.isCursorMode()) {
            q.setCursorMode(true);
            if (params.getCursor() != null) {
                List<String> anchor = decodeCursor(params.getCursor(), 2);
                q.setLastLayerNo(anchor.get(0));
                q.setLastId(parseId(anchor.get(1)));
            }
            q.setLimit(params.fetchLimit());
            List<LayerSliceVO> rows = mapper.searchLayerSlices(q, scope);
            boolean hasMore = rows.size() > params.getSize();
            if (hasMore) {
                rows = new ArrayList<>(rows.subList(0, params.getSize()));
            }
            String next = null;
            if (hasMore && !rows.isEmpty()) {
                LayerSliceVO last = rows.get(rows.size() - 1);
                next = Cursors.encode(toList(last.getLayerNo(), String.valueOf(last.getId())));
            }
            long total = mapper.countLayerSlices(q, scope);
            return new PageResult<>(rows, total, params.getSize(), 0, hasMore, next);
        }

        q.setCursorMode(false);
        q.setOffset(params.offset());
        q.setLimit(params.getSize());
        long total = mapper.countLayerSlices(q, scope);
        List<LayerSliceVO> rows = total == 0 ? Collections.emptyList() : mapper.searchLayerSlices(q, scope);
        boolean hasMore = params.offset() + rows.size() < total;
        return new PageResult<>(rows, total, params.getSize(), params.getPage(), hasMore, null);
    }

    @Override
    public PageResult<TemperatureVO> searchTemperatures(TemperatureSearchQuery q) {
        PageParams params = PageParams.of(q.getPage(), q.getSize(), q.getCursor());
        q.setLocationIds(normalizeLongs(q.getLocationIds()));
        q.setLayerNos(normalizeStrings(q.getLayerNos()));
        QueryScope scope = dataScopeService.currentScope();
        if (isNone(scope)) {
            return empty(params);
        }

        if (params.isCursorMode()) {
            q.setCursorMode(true);
            if (params.getCursor() != null) {
                List<String> anchor = decodeCursor(params.getCursor(), 2);
                q.setLastRecordedAt(parseTime(anchor.get(0)));
                q.setLastId(parseId(anchor.get(1)));
            }
            q.setLimit(params.fetchLimit());
            List<TemperatureVO> rows = mapper.searchTemperatures(q, scope);
            boolean hasMore = rows.size() > params.getSize();
            if (hasMore) {
                rows = new ArrayList<>(rows.subList(0, params.getSize()));
            }
            String next = null;
            if (hasMore && !rows.isEmpty()) {
                TemperatureVO last = rows.get(rows.size() - 1);
                next = Cursors.encode(toList(last.getRecordedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        String.valueOf(last.getId())));
            }
            long total = mapper.countTemperatures(q, scope);
            return new PageResult<>(rows, total, params.getSize(), 0, hasMore, next);
        }

        q.setCursorMode(false);
        q.setOffset(params.offset());
        q.setLimit(params.getSize());
        long total = mapper.countTemperatures(q, scope);
        List<TemperatureVO> rows = total == 0 ? Collections.emptyList() : mapper.searchTemperatures(q, scope);
        boolean hasMore = params.offset() + rows.size() < total;
        return new PageResult<>(rows, total, params.getSize(), params.getPage(), hasMore, null);
    }

    @Override
    public PageResult<Map<String, Object>> layerPartitionSummary(Long locationId, Integer page, Integer size) {
        PageParams params = PageParams.of(page, size, null);
        QueryScope scope = dataScopeService.currentScope();
        if (isNone(scope)) {
            return empty(params);
        }
        long total = mapper.countLayerPartitions(locationId, scope);
        List<Map<String, Object>> rows = total == 0 ? Collections.emptyList()
                : mapper.layerPartitions(locationId, scope, null, false, params.offset(), params.getSize());
        boolean hasMore = params.offset() + rows.size() < total;
        return new PageResult<>(rows, total, params.getSize(), params.getPage(), hasMore, null);
    }

    // ============================== 辅助方法 ==============================

    private boolean isNone(QueryScope scope) {
        return DataScope.Level.NONE.name().equals(scope.getLevel());
    }

    private <T> PageResult<T> empty(PageParams params) {
        return new PageResult<>(Collections.emptyList(), 0L, params.getSize(),
                params.isCursorMode() ? 0 : params.getPage(), false, null);
    }

    private List<String> decodeCursor(String cursor, int expected) {
        try {
            return Cursors.decode(cursor, expected);
        } catch (RuntimeException ex) {
            throw new BizException("分页游标无效");
        }
    }

    private Long parseId(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new BizException("分页游标无效");
        }
    }

    private LocalDateTime parseTime(String value) {
        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (RuntimeException ex) {
            throw new BizException("分页游标无效");
        }
    }

    private List<String> normalizeStrings(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        Set<String> deduped = new LinkedHashSet<>();
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                deduped.add(value.trim());
            }
        }
        if (deduped.isEmpty()) {
            return null;
        }
        if (deduped.size() > MAX_IN_VALUES) {
            throw new BizException("层位等集合条件最多支持 " + MAX_IN_VALUES + " 个取值");
        }
        return new ArrayList<>(deduped);
    }

    private List<Long> normalizeLongs(List<Long> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        Set<Long> deduped = new LinkedHashSet<>();
        for (Long value : values) {
            if (value != null) {
                deduped.add(value);
            }
        }
        if (deduped.isEmpty()) {
            return null;
        }
        if (deduped.size() > MAX_IN_VALUES) {
            throw new BizException("库位集合条件最多支持 " + MAX_IN_VALUES + " 个取值");
        }
        return new ArrayList<>(deduped);
    }

    private List<String> toList(String... values) {
        List<String> list = new ArrayList<>(values.length);
        Collections.addAll(list, values);
        return list;
    }
}
