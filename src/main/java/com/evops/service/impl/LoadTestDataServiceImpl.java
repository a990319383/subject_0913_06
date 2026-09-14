package com.evops.service.impl;

import com.evops.common.page.PageResult;
import com.evops.dto.search.LayerSliceSearchQuery;
import com.evops.dto.search.TemperatureSearchQuery;
import com.evops.entity.DrillTask;
import com.evops.entity.SampleBox;
import com.evops.entity.SecurityTenant;
import com.evops.entity.StorageLocation;
import com.evops.mapper.DrillTaskMapper;
import com.evops.mapper.FreezerTemperatureMapper;
import com.evops.mapper.IceCoreSampleMapper;
import com.evops.mapper.IceLayerSliceMapper;
import com.evops.mapper.SampleBoxMapper;
import com.evops.mapper.SecurityTenantMapper;
import com.evops.mapper.StorageLocationMapper;
import com.evops.security.CurrentUser;
import com.evops.security.TenantContext;
import com.evops.service.LoadTestDataService;
import com.evops.service.OperationsSearchService;
import com.evops.vo.LayerSliceVO;
import com.evops.vo.TemperatureVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 压测数据播种与基准。
 * 用专用租户 LOADTEST 隔离压测数据；分钟级库温按分钟递增、层位切片按层位循环，
 * 数据完全确定，便于校验稳定排序与游标翻页不丢不重。
 */
@Service
public class LoadTestDataServiceImpl implements LoadTestDataService {

    static final String LOAD_TENANT_CODE = "LOADTEST";
    private static final int LOCATION_COUNT = 5;
    private static final int SAMPLE_COUNT = 1000;
    private static final int LAYER_COUNT = 100;
    private static final int CHUNK = 5000;
    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 1, 1, 0, 0);

    private final JdbcTemplate jdbcTemplate;
    private final SecurityTenantMapper tenantMapper;
    private final DrillTaskMapper taskMapper;
    private final StorageLocationMapper locationMapper;
    private final SampleBoxMapper boxMapper;
    private final IceCoreSampleMapper sampleMapper;
    private final FreezerTemperatureMapper temperatureMapper;
    private final IceLayerSliceMapper sliceMapper;
    private final OperationsSearchService searchService;

    public LoadTestDataServiceImpl(JdbcTemplate jdbcTemplate,
                                   SecurityTenantMapper tenantMapper,
                                   DrillTaskMapper taskMapper,
                                   StorageLocationMapper locationMapper,
                                   SampleBoxMapper boxMapper,
                                   IceCoreSampleMapper sampleMapper,
                                   FreezerTemperatureMapper temperatureMapper,
                                   IceLayerSliceMapper sliceMapper,
                                   OperationsSearchService searchService) {
        this.jdbcTemplate = jdbcTemplate;
        this.tenantMapper = tenantMapper;
        this.taskMapper = taskMapper;
        this.locationMapper = locationMapper;
        this.boxMapper = boxMapper;
        this.sampleMapper = sampleMapper;
        this.temperatureMapper = temperatureMapper;
        this.sliceMapper = sliceMapper;
        this.searchService = searchService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> seedAndBenchmark(int temperatureEvents, int sliceCount,
                                                boolean reset, boolean benchmark) {
        if (temperatureEvents < 0 || sliceCount < 0) {
            throw new IllegalArgumentException("压测数据量不能为负");
        }
        if (temperatureEvents > 2_000_000 || sliceCount > 2_000_000) {
            throw new IllegalArgumentException("单次压测数据量上限 2,000,000");
        }
        long t0 = System.nanoTime();
        SecurityTenant tenant = prepareTenant();
        Long tenantId = tenant.getId();

        long existingTemp = countTemperatures(tenantId);
        long existingSlice = countSlices(tenantId);
        boolean needSeed = reset || existingTemp < temperatureEvents || existingSlice < sliceCount;
        if (needSeed) {
            cleanSeededData(tenantId);
            Skeleton skeleton = seedSkeleton(tenantId);
            List<Long> sampleIds = seedSamples(tenantId, skeleton);
            if (temperatureEvents > 0) {
                seedTemperatures(tenantId, skeleton.locationIds, temperatureEvents);
            }
            if (sliceCount > 0) {
                seedSlices(tenantId, sampleIds, skeleton.locationIds, sliceCount);
            }
        }
        long seedMillis = (System.nanoTime() - t0) / 1_000_000;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("tenantId", tenantId);
        result.put("temperatureEvents", countTemperatures(tenantId));
        result.put("layerSlices", countSlices(tenantId));
        result.put("seedMillis", seedMillis);
        result.put("seeded", needSeed);
        if (benchmark) {
            result.put("benchmark", runBenchmark(tenantId));
        }
        return result;
    }

    // ============================== 播种 ==============================

    private SecurityTenant prepareTenant() {
        List<SecurityTenant> tenants = tenantMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SecurityTenant>()
                        .eq(SecurityTenant::getTenantCode, LOAD_TENANT_CODE));
        if (!tenants.isEmpty()) {
            return tenants.get(0);
        }
        SecurityTenant tenant = new SecurityTenant();
        tenant.setTenantCode(LOAD_TENANT_CODE);
        tenant.setTenantName("压测专用租户");
        tenant.setStatus("ACTIVE");
        tenantMapper.insert(tenant);
        return tenant;
    }

    private void cleanSeededData(Long tenantId) {
        jdbcTemplate.update("DELETE FROM t_ice_layer_slice WHERE tenant_id = ?", tenantId);
        jdbcTemplate.update("DELETE FROM t_freezer_temperature WHERE tenant_id = ?", tenantId);
        jdbcTemplate.update("DELETE FROM t_ice_core_sample WHERE tenant_id = ?", tenantId);
        jdbcTemplate.update("DELETE FROM t_sample_box WHERE tenant_id = ?", tenantId);
        jdbcTemplate.update("DELETE FROM t_storage_location WHERE tenant_id = ?", tenantId);
        jdbcTemplate.update("DELETE FROM t_drill_task WHERE tenant_id = ?", tenantId);
    }

    private static final class Skeleton {
        private final Long taskId;
        private final Long boxId;
        private final List<Long> locationIds;

        private Skeleton(Long taskId, Long boxId, List<Long> locationIds) {
            this.taskId = taskId;
            this.boxId = boxId;
            this.locationIds = locationIds;
        }
    }

    private Skeleton seedSkeleton(Long tenantId) {
        DrillTask task = new DrillTask();
        task.setTaskNo("TASK-LOAD");
        task.setTaskName("压测钻取任务");
        task.setSiteName("东南极冰盖Dome A");
        task.setDrillDepth(new BigDecimal("3000.00"));
        task.setStatus(com.evops.constant.DrillTaskStatus.COMPLETED);
        task.setTenantId(tenantId);
        taskMapper.insert(task);

        List<Long> locationIds = new ArrayList<>();
        for (int i = 0; i < LOCATION_COUNT; i++) {
            StorageLocation location = new StorageLocation();
            location.setLocationCode(String.format("LOC-LOAD-%d", i + 1));
            location.setWarehouse("极地压测冷库");
            location.setShelfNo("S" + (i + 1));
            location.setLayerNo(String.format("L-%02d", i));
            location.setSetTemperature(new BigDecimal("-18.00"));
            location.setCapacity(SAMPLE_COUNT);
            location.setStatus(com.evops.constant.StorageLocationStatus.AVAILABLE);
            location.setTenantId(tenantId);
            locationMapper.insert(location);
            locationIds.add(location.getId());
        }

        SampleBox box = new SampleBox();
        box.setBoxNo("BOX-LOAD");
        box.setTaskId(task.getId());
        box.setLocationId(locationIds.get(0));
        box.setCapacity(SAMPLE_COUNT);
        box.setStatus(com.evops.constant.SampleBoxStatus.IN_USE);
        box.setTenantId(tenantId);
        boxMapper.insert(box);
        return new Skeleton(task.getId(), box.getId(), locationIds);
    }

    private List<Long> seedSamples(Long tenantId, Skeleton skeleton) {
        List<Long> ids = new ArrayList<>(SAMPLE_COUNT);
        String sql = "INSERT INTO t_ice_core_sample "
                + "(sample_no, task_id, box_id, location_id, layer_no, depth_top, depth_bottom, "
                + "temperature, melt_water_ml, integrity_pct, status, sampled_at, tenant_id, create_time) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        for (int start = 0; start < SAMPLE_COUNT; start += CHUNK) {
            int end = Math.min(start + CHUNK, SAMPLE_COUNT);
            int batchSize = end - start;
            final int chunkStart = start;
            jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
                @Override
                public void setValues(java.sql.PreparedStatement ps, int i) throws java.sql.SQLException {
                    int seq = chunkStart + i;
                    int locIndex = seq % LOCATION_COUNT;
                    BigDecimal top = new BigDecimal(seq).multiply(new BigDecimal("0.55"));
                    ps.setString(1, String.format("IC-LOAD-%04d", seq + 1));
                    ps.setLong(2, skeleton.taskId);
                    ps.setLong(3, skeleton.boxId);
                    ps.setLong(4, skeleton.locationIds.get(locIndex));
                    ps.setString(5, String.format("L-%03d", seq % LAYER_COUNT));
                    ps.setBigDecimal(6, top);
                    ps.setBigDecimal(7, top.add(new BigDecimal("0.50")));
                    ps.setBigDecimal(8, new BigDecimal("-18.00"));
                    ps.setBigDecimal(9, new BigDecimal("10.00"));
                    ps.setBigDecimal(10, new BigDecimal("99.00"));
                    ps.setString(11, com.evops.constant.IceCoreSampleStatus.STORED);
                    ps.setTimestamp(12, Timestamp.valueOf(BASE_TIME.plusMinutes(seq)));
                    ps.setLong(13, tenantId);
                    ps.setTimestamp(14, Timestamp.valueOf(BASE_TIME));
                }

                @Override
                public int getBatchSize() {
                    return batchSize;
                }
            });
            List<Long> generated = jdbcTemplate.queryForList(
                    "SELECT id FROM t_ice_core_sample WHERE tenant_id = ? ORDER BY id LIMIT ? OFFSET ?",
                    Long.class, tenantId, batchSize, (long) start);
            ids.addAll(generated);
        }
        return ids;
    }

    private void seedTemperatures(Long tenantId, List<Long> locationIds, int total) {
        String sql = "INSERT INTO t_freezer_temperature "
                + "(location_id, tenant_id, recorded_at, measured_temperature, set_temperature, "
                + "deviation, alarm_flag, create_time) VALUES (?,?,?,?,?,?,?,?)";
        BigDecimal setTemp = new BigDecimal("-18.00");
        for (int start = 0; start < total; start += CHUNK) {
            int end = Math.min(start + CHUNK, total);
            int batchSize = end - start;
            final int chunkStart = start;
            jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
                @Override
                public void setValues(java.sql.PreparedStatement ps, int i) throws java.sql.SQLException {
                    int seq = chunkStart + i;
                    long locationId = locationIds.get(seq % LOCATION_COUNT);
                    // 周期性温升，约 1/4 分钟越过 2℃ 告警线，数据确定
                    BigDecimal measured = setTemp.add(new BigDecimal(((seq % 13) - 6) * 0.5));
                    BigDecimal deviation = measured.subtract(setTemp).abs();
                    int alarm = deviation.compareTo(new BigDecimal("2")) > 0 ? 1 : 0;
                    ps.setLong(1, locationId);
                    ps.setLong(2, tenantId);
                    ps.setTimestamp(3, Timestamp.valueOf(BASE_TIME.plusMinutes(seq)));
                    ps.setBigDecimal(4, measured);
                    ps.setBigDecimal(5, setTemp);
                    ps.setBigDecimal(6, deviation);
                    ps.setInt(7, alarm);
                    ps.setTimestamp(8, Timestamp.valueOf(BASE_TIME));
                }

                @Override
                public int getBatchSize() {
                    return batchSize;
                }
            });
        }
    }

    private void seedSlices(Long tenantId, List<Long> sampleIds, List<Long> locationIds, int total) {
        String sql = "INSERT INTO t_ice_layer_slice "
                + "(sample_id, location_id, tenant_id, layer_no, slice_index, depth_top, depth_bottom, "
                + "temperature, measured_at, create_time) VALUES (?,?,?,?,?,?,?,?,?,?)";
        for (int start = 0; start < total; start += CHUNK) {
            int end = Math.min(start + CHUNK, total);
            int batchSize = end - start;
            final int chunkStart = start;
            jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
                @Override
                public void setValues(java.sql.PreparedStatement ps, int i) throws java.sql.SQLException {
                    int seq = chunkStart + i;
                    int samplePos = seq % SAMPLE_COUNT;
                    int sliceIndex = seq / SAMPLE_COUNT;
                    long sampleId = sampleIds.get(samplePos);
                    long locationId = locationIds.get(samplePos % LOCATION_COUNT);
                    BigDecimal top = new BigDecimal(sliceIndex).multiply(new BigDecimal("0.50"));
                    BigDecimal bottom = top.add(new BigDecimal("0.50"));
                    BigDecimal temp = new BigDecimal("-20.00").add(new BigDecimal((seq % 7) * 0.4));
                    ps.setLong(1, sampleId);
                    ps.setLong(2, locationId);
                    ps.setLong(3, tenantId);
                    ps.setString(4, String.format("L-%03d", seq % LAYER_COUNT));
                    ps.setInt(5, sliceIndex);
                    ps.setBigDecimal(6, top);
                    ps.setBigDecimal(7, bottom);
                    ps.setBigDecimal(8, temp);
                    ps.setTimestamp(9, Timestamp.valueOf(BASE_TIME.plusMinutes(seq)));
                    ps.setTimestamp(10, Timestamp.valueOf(BASE_TIME));
                }

                @Override
                public int getBatchSize() {
                    return batchSize;
                }
            });
        }
    }

    private long countTemperatures(Long tenantId) {
        Long c = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM t_freezer_temperature WHERE tenant_id = ?", Long.class, tenantId);
        return c == null ? 0 : c;
    }

    private long countSlices(Long tenantId) {
        Long c = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM t_ice_layer_slice WHERE tenant_id = ?", Long.class, tenantId);
        return c == null ? 0 : c;
    }

    // ============================== 基准 ==============================

    private Map<String, Object> runBenchmark(Long tenantId) {
        Map<String, Object> bench = new LinkedHashMap<>();
        CurrentUser admin = new CurrentUser(null, tenantId,
                com.evops.constant.RoleCode.TENANT_ADMIN);
        TenantContext.runAs(admin, () -> {
            // 1) 层位切片：总数 + 首页 + 深 offset 页（25 万行处）
            long s0 = System.nanoTime();
            LayerSliceSearchQuery firstQ = new LayerSliceSearchQuery();
            firstQ.setSize(100);
            firstQ.setPage(1);
            PageResult<LayerSliceVO> first = searchService.searchLayerSlices(firstQ);
            long firstMs = (System.nanoTime() - s0) / 1_000_000;

            long s1 = System.nanoTime();
            LayerSliceSearchQuery deepQ = new LayerSliceSearchQuery();
            deepQ.setSize(100);
            deepQ.setPage(2501);
            PageResult<LayerSliceVO> deep = searchService.searchLayerSlices(deepQ);
            long deepMs = (System.nanoTime() - s1) / 1_000_000;

            // 2) 游标连续翻 50 页：校验层位分区排序严格、ID 不丢不重
            long s2 = System.nanoTime();
            int pages = 0;
            int rows = 0;
            String cursor = null;
            String lastLayer = null;
            Long lastId = null;
            boolean ordered = true;
            while (pages < 50) {
                LayerSliceSearchQuery q = new LayerSliceSearchQuery();
                q.setSize(100);
                q.setCursor(cursor);
                PageResult<LayerSliceVO> r = searchService.searchLayerSlices(q);
                for (LayerSliceVO row : r.getRecords()) {
                    if (lastLayer != null) {
                        int cmp = row.getLayerNo().compareTo(lastLayer);
                        if (cmp < 0 || (cmp == 0 && row.getId() <= lastId)) {
                            ordered = false;
                        }
                    }
                    lastLayer = row.getLayerNo();
                    lastId = row.getId();
                }
                rows += r.getRecords().size();
                pages++;
                cursor = r.getNextCursor();
                if (cursor == null) {
                    break;
                }
            }
            long cursorMs = (System.nanoTime() - s2) / 1_000_000;

            // 3) 分钟级库温：倒序游标翻 20 页，校验 (recordedAt, id) 严格递减
            long s3 = System.nanoTime();
            int tempPages = 0;
            String tempCursor = null;
            LocalDateTime lastTime = null;
            Long lastTempId = null;
            boolean tempOrdered = true;
            while (tempPages < 20) {
                TemperatureSearchQuery q = new TemperatureSearchQuery();
                q.setSize(100);
                q.setCursor(tempCursor);
                PageResult<TemperatureVO> r = searchService.searchTemperatures(q);
                for (TemperatureVO row : r.getRecords()) {
                    if (lastTime != null) {
                        int cmp = row.getRecordedAt().compareTo(lastTime);
                        if (cmp > 0 || (cmp == 0 && row.getId() >= lastTempId)) {
                            tempOrdered = false;
                        }
                    }
                    lastTime = row.getRecordedAt();
                    lastTempId = row.getId();
                }
                tempPages++;
                tempCursor = r.getNextCursor();
                if (tempCursor == null) {
                    break;
                }
            }
            long tempMs = (System.nanoTime() - s3) / 1_000_000;

            // 4) 层位分区汇总
            long s4 = System.nanoTime();
            PageResult<Map<String, Object>> partitions = searchService.layerPartitionSummary(null, 1, 100);
            long partitionMs = (System.nanoTime() - s4) / 1_000_000;

            bench.put("sliceTotal", first.getTotal());
            bench.put("sliceFirstPageMillis", firstMs);
            bench.put("sliceDeepOffsetMillis", deepMs);
            bench.put("sliceDeepOffsetSize", deep.getRecords().size());
            bench.put("sliceCursorPages", pages);
            bench.put("sliceCursorRows", rows);
            bench.put("sliceCursorMillis", cursorMs);
            bench.put("sliceOrderStable", ordered);
            bench.put("temperatureCursorPages", tempPages);
            bench.put("temperatureCursorMillis", tempMs);
            bench.put("temperatureOrderStable", tempOrdered);
            bench.put("partitionCount", partitions.getTotal());
            bench.put("partitionMillis", partitionMs);
        });
        return bench;
    }
}
