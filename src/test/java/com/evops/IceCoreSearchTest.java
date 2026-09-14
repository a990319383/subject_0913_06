package com.evops;

import com.evops.common.BizException;
import com.evops.common.page.PageResult;
import com.evops.constant.IceCoreSampleStatus;
import com.evops.constant.RoleCode;
import com.evops.dto.AccountReq;
import com.evops.dto.BatchCreateReq;
import com.evops.dto.DrillTaskReq;
import com.evops.dto.GrantReq;
import com.evops.dto.IceCoreSampleReq;
import com.evops.dto.SampleBoxReq;
import com.evops.dto.StorageLocationReq;
import com.evops.dto.TenantReq;
import com.evops.dto.search.LayerSliceSearchQuery;
import com.evops.dto.search.SampleSearchQuery;
import com.evops.dto.search.TemperatureSearchQuery;
import com.evops.entity.DrillTask;
import com.evops.entity.FreezerTemperature;
import com.evops.entity.IceCoreSample;
import com.evops.entity.IceLayerSlice;
import com.evops.entity.SampleBox;
import com.evops.entity.SecurityAccount;
import com.evops.entity.SecurityTenant;
import com.evops.entity.StorageLocation;
import com.evops.mapper.FreezerTemperatureMapper;
import com.evops.mapper.IceLayerSliceMapper;
import com.evops.security.CurrentUser;
import com.evops.security.TenantContext;
import com.evops.service.AnalysisBatchService;
import com.evops.service.DrillTaskService;
import com.evops.service.IceCoreSampleService;
import com.evops.service.OperationsSearchService;
import com.evops.service.SampleBoxService;
import com.evops.service.StorageLocationService;
import com.evops.service.TenantAdminService;
import com.evops.vo.LayerSliceVO;
import com.evops.vo.SampleSearchVO;
import com.evops.vo.TemperatureVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 运营检索功能测试：4+ 条件 AND/范围、批次一对多不放大主表、
 * 游标/offset 稳定分页、pageSize 1-100、租户与角色数据权限、层位分区。
 */
@SpringBootTest
@Transactional
class IceCoreSearchTest {

    @Autowired private TenantAdminService tenantAdminService;
    @Autowired private DrillTaskService drillTaskService;
    @Autowired private StorageLocationService storageLocationService;
    @Autowired private SampleBoxService sampleBoxService;
    @Autowired private IceCoreSampleService iceCoreSampleService;
    @Autowired private AnalysisBatchService analysisBatchService;
    @Autowired private OperationsSearchService searchService;
    @Autowired private IceLayerSliceMapper sliceMapper;
    @Autowired private FreezerTemperatureMapper temperatureMapper;

    private Long tenantA;
    private Long tenantB;
    private CurrentUser adminA;
    private CurrentUser adminB;
    private CurrentUser platform;
    private SecurityAccount userA;
    private SecurityAccount userAEmpty;

    private DrillTask taskA;
    private StorageLocation locA;
    private SampleBox boxA;
    private IceCoreSample s1;

    @BeforeEach
    void setUp() {
        platform = CurrentUser.platform();

        SecurityTenant ta = tenantAdminService.createTenant(tenantReq("T-A"));
        SecurityTenant tb = tenantAdminService.createTenant(tenantReq("T-B"));
        tenantA = ta.getId();
        tenantB = tb.getId();
        adminA = new CurrentUser(null, tenantA, RoleCode.TENANT_ADMIN);
        adminB = new CurrentUser(null, tenantB, RoleCode.TENANT_ADMIN);
        userA = tenantAdminService.createAccount(accountReq("user-a", tenantA, RoleCode.TENANT_USER));
        userAEmpty = tenantAdminService.createAccount(accountReq("user-empty", tenantA, RoleCode.TENANT_USER));

        // 租户 A 的任务/库位/样本盒
        TenantContext.runAs(adminA, () -> {
            DrillTaskReq tq = taskReq("TASK-SA");
            taskA = drillTaskService.create(tq);
            StorageLocationReq lq = locationReq("LOC-SA", "L-A");
            locA = storageLocationService.create(lq);
            SampleBoxReq bq = boxReq("BOX-SA", taskA.getId(), locA.getId(), 50);
            boxA = sampleBoxService.create(bq);
        });
    }

    // ---------- 4+ 条件 AND / 范围组合 ----------
    @Test
    void combinedAndRangeFilters() {
        TenantContext.runAs(adminA, () -> {
            s1 = iceCoreSampleService.create(sampleReq("IC-F-1", boxA.getId(), "L-A",
                    new BigDecimal("-18.00"), LocalDateTime.of(2026, 3, 1, 10, 0)));
            iceCoreSampleService.create(sampleReq("IC-F-2", boxA.getId(), "L-A",
                    new BigDecimal("-25.00"), LocalDateTime.of(2026, 3, 2, 11, 0)));
            iceCoreSampleService.create(sampleReq("IC-F-3", boxA.getId(), "L-B",
                    new BigDecimal("-18.00"), LocalDateTime.of(2026, 3, 3, 12, 0)));
            iceCoreSampleService.create(sampleReq("IC-F-4", boxA.getId(), "L-A",
                    new BigDecimal("-18.00"), LocalDateTime.of(2026, 2, 1, 9, 0)));
            IceCoreSample s5 = iceCoreSampleService.create(sampleReq("IC-F-5", boxA.getId(), "L-A",
                    new BigDecimal("-18.00"), LocalDateTime.of(2026, 3, 5, 9, 0)));
            s5.setStatus(IceCoreSampleStatus.CONSUMED);
            iceCoreSampleService.updateById(s5);
        });

        TenantContext.runAs(adminA, () -> {
            // 9 个 AND/范围条件同时生效：任务+盒+库位+状态+层位+日期区间+温度区间
            SampleSearchQuery q = new SampleSearchQuery();
            q.setTaskId(taskA.getId());
            q.setBoxId(boxA.getId());
            q.setLocationId(locA.getId());
            q.setStatus(IceCoreSampleStatus.STORED);
            q.setLayerNos(Arrays.asList("L-A"));
            q.setSampledFrom(LocalDateTime.of(2026, 3, 1, 0, 0));
            q.setSampledTo(LocalDateTime.of(2026, 3, 3, 0, 0)); // 上界开
            q.setTemperatureMin(new BigDecimal("-20"));
            q.setTemperatureMax(new BigDecimal("-10"));
            q.setPage(1);
            q.setSize(20);
            PageResult<SampleSearchVO> result = searchService.searchSamples(q);

            assertEquals(1, result.getTotal());
            assertEquals(1, result.getRecords().size());
            assertEquals("IC-F-1", result.getRecords().get(0).getSampleNo());
            // 主表一行一行，冗余关联字段被填充
            assertEquals("TASK-SA", result.getRecords().get(0).getTaskNo());
            assertEquals("BOX-SA", result.getRecords().get(0).getBoxNo());
            assertEquals("LOC-SA", result.getRecords().get(0).getLocationCode());
            assertEquals(tenantA, result.getRecords().get(0).getTenantId());
        });
    }

    // ---------- 批次一对多不放大主表 ----------
    @Test
    void batchOneToManyDoesNotAmplifyMainRows() {
        TenantContext.runAs(adminA, () -> {
            IceCoreSample b1 = iceCoreSampleService.create(sampleReq("IC-B-1", boxA.getId()));
            IceCoreSample b2 = iceCoreSampleService.create(sampleReq("IC-B-2", boxA.getId()));
            iceCoreSampleService.create(sampleReq("IC-B-3", boxA.getId()));
            com.evops.entity.AnalysisBatch batch1 =
                    analysisBatchService.createBatch(batchReq("BAT-B1", b1.getId(), b2.getId()));
            // 取消批次1，样本回库；再让 b2 加入批次2 —— b2 在批次明细表出现两行
            analysisBatchService.transit(batch1.getId(),
                    com.evops.constant.AnalysisBatchStatus.CANCELLED);
            analysisBatchService.createBatch(batchReq("BAT-B2", b2.getId()));

            SampleSearchQuery q = new SampleSearchQuery();
            q.setBatchId(batch1.getId());
            q.setPage(1);
            q.setSize(50);
            PageResult<SampleSearchVO> r = searchService.searchSamples(q);
            // b2 虽在批次明细表出现两行，按批次1 过滤仍只出现一次
            assertEquals(2, r.getTotal());
            assertEquals(2, r.getRecords().size());
            List<String> nos = r.getRecords().stream()
                    .map(SampleSearchVO::getSampleNo).sorted().collect(Collectors.toList());
            assertEquals(Arrays.asList("IC-B-1", "IC-B-2"), nos);
        });
    }

    // ---------- offset 与游标稳定分页一致 ----------
    @Test
    void offsetAndCursorPagingStable() {
        TenantContext.runAs(adminA, () -> {
            for (int i = 1; i <= 5; i++) {
                iceCoreSampleService.create(sampleReq("IC-P-" + i, boxA.getId()));
            }
        });
        TenantContext.runAs(adminA, () -> {
            List<Long> offsetIds = new ArrayList<>();
            for (int p = 1; p <= 3; p++) {
                SampleSearchQuery q = new SampleSearchQuery();
                q.setTaskId(taskA.getId());
                q.setPage(p);
                q.setSize(2);
                PageResult<SampleSearchVO> r = searchService.searchSamples(q);
                r.getRecords().forEach(v -> offsetIds.add(v.getId()));
            }
            assertEquals(5, offsetIds.size());
            assertEquals(5, new java.util.HashSet<>(offsetIds).size());
            // 默认 id DESC 稳定排序
            for (int i = 1; i < offsetIds.size(); i++) {
                assertTrue(offsetIds.get(i - 1) > offsetIds.get(i));
            }

            List<Long> cursorIds = new ArrayList<>();
            String cursor = null;
            long total = -1;
            for (int p = 0; p < 3; p++) {
                SampleSearchQuery q = new SampleSearchQuery();
                q.setTaskId(taskA.getId());
                q.setSize(2);
                q.setCursor(cursor);
                PageResult<SampleSearchVO> r = searchService.searchSamples(q);
                total = r.getTotal();
                r.getRecords().forEach(v -> cursorIds.add(v.getId()));
                cursor = r.getNextCursor();
                if (cursor == null) {
                    break;
                }
            }
            assertEquals(5, total);
            assertEquals(offsetIds, cursorIds);
        });
    }

    // ---------- pageSize 仅允许 1-100 ----------
    @Test
    void pageSizeMustBeBetween1And100() {
        TenantContext.runAs(adminA, () -> {
            assertThrows(BizException.class, () -> searchService.searchSamples(buildPaging(1, 0)));
            assertThrows(BizException.class, () -> searchService.searchSamples(buildPaging(1, 101)));
            assertThrows(BizException.class, () -> searchService.searchSamples(buildPaging(0, 20)));
            // 边界合法
            PageResult<SampleSearchVO> ok = searchService.searchSamples(buildPaging(1, 100));
            assertNotNull(ok);
        });
    }

    // ---------- 租户与角色数据权限 ----------
    @Test
    void tenantAndRoleDataIsolation() {
        TenantContext.runAs(adminA, () -> {
            iceCoreSampleService.create(sampleReq("IC-A-1", boxA.getId()));
        });
        TenantContext.runAs(adminB, () -> {
            DrillTask taskB = drillTaskService.create(taskReq("TASK-SB"));
            StorageLocation locB = storageLocationService.create(locationReq("LOC-SB", "L-A"));
            SampleBox boxB = sampleBoxService.create(boxReq("BOX-SB", taskB.getId(), locB.getId(), 10));
            iceCoreSampleService.create(sampleReq("IC-B-1", boxB.getId()));
        });

        // 租户管理员只见本租户
        TenantContext.runAs(adminA, () -> assertEquals(1L, allSampleTotal()));
        TenantContext.runAs(adminB, () -> assertEquals(1L, allSampleTotal()));
        // 平台跨租户全量
        TenantContext.runAs(platform, () -> assertEquals(2L, allSampleTotal()));
        // 未授权的租户成员什么都看不到
        TenantContext.runAs(new CurrentUser(userAEmpty.getId(), tenantA, RoleCode.TENANT_USER),
                () -> assertEquals(0L, allSampleTotal()));

        // 对 userA 授予 TASK 后可见租户 A 的样本（间接授权）
        GrantReq grant = new GrantReq();
        grant.setAccountId(userA.getId());
        grant.setObjectType(com.evops.constant.ObjectType.TASK);
        grant.setObjectIds(Arrays.asList(taskA.getId()));
        tenantAdminService.grant(grant);
        TenantContext.runAs(new CurrentUser(userA.getId(), tenantA, RoleCode.TENANT_USER),
                () -> assertEquals(1L, allSampleTotal()));
    }

    // ---------- 层位切片：按层位分区稳定分页与汇总 ----------
    @Test
    void layerSlicePartitionPaging() {
        TenantContext.runAs(adminA, () -> {
            s1 = iceCoreSampleService.create(sampleReq("IC-L-1", boxA.getId()));
            sliceMapper.insert(slice(s1.getId(), locA.getId(), tenantA, "L-A", 0));
            sliceMapper.insert(slice(s1.getId(), locA.getId(), tenantA, "L-A", 1));
            sliceMapper.insert(slice(s1.getId(), locA.getId(), tenantA, "L-B", 2));
        });

        TenantContext.runAs(adminA, () -> {
            // 首页走键集模式（不传 page），以便拿到 nextCursor
            LayerSliceSearchQuery q = new LayerSliceSearchQuery();
            q.setLocationId(locA.getId());
            q.setSize(2);
            PageResult<LayerSliceVO> r1 = searchService.searchLayerSlices(q);
            assertEquals(3, r1.getTotal());
            assertEquals(2, r1.getRecords().size());
            assertEquals("L-A", r1.getRecords().get(0).getLayerNo());
            assertNotNull(r1.getRecords().get(0).getSampleNo());

            LayerSliceSearchQuery q2 = new LayerSliceSearchQuery();
            q2.setLocationId(locA.getId());
            q2.setSize(2);
            q2.setCursor(r1.getNextCursor());
            PageResult<LayerSliceVO> r2 = searchService.searchLayerSlices(q2);
            assertEquals(1, r2.getRecords().size());
            assertEquals("L-B", r2.getRecords().get(0).getLayerNo());

            PageResult<java.util.Map<String, Object>> parts =
                    searchService.layerPartitionSummary(locA.getId(), 1, 20);
            assertEquals(2, parts.getTotal());
            assertEquals("L-A", parts.getRecords().get(0).get("layerNo"));
            assertEquals(2L, ((Number) parts.getRecords().get(0).get("sliceCount")).longValue());
            assertEquals("L-B", parts.getRecords().get(1).get("layerNo"));
        });
    }

    // ---------- 分钟级库温：倒序游标、仅告警、时间范围 ----------
    @Test
    void temperatureCursorAndAlarmFilter() {
        TenantContext.runAs(adminA, () -> {
            temperatureMapper.insert(temp(locA.getId(), tenantA,
                    LocalDateTime.of(2026, 3, 1, 10, 0), new BigDecimal("-15"), 1));
            temperatureMapper.insert(temp(locA.getId(), tenantA,
                    LocalDateTime.of(2026, 3, 1, 10, 1), new BigDecimal("-18"), 0));
            temperatureMapper.insert(temp(locA.getId(), tenantA,
                    LocalDateTime.of(2026, 3, 1, 10, 2), new BigDecimal("-14"), 1));
        });
        TenantContext.runAs(adminA, () -> {
            // 首页走键集模式（不传 page），按时间倒序
            TemperatureSearchQuery q = new TemperatureSearchQuery();
            q.setLocationIds(Arrays.asList(locA.getId()));
            q.setAlarmOnly(true);
            q.setSize(1);
            PageResult<TemperatureVO> r1 = searchService.searchTemperatures(q);
            assertEquals(2, r1.getTotal());
            assertEquals(1, r1.getRecords().size());
            // 倒序：最新告警在前
            assertEquals(LocalDateTime.of(2026, 3, 1, 10, 2), r1.getRecords().get(0).getRecordedAt());

            TemperatureSearchQuery q2 = new TemperatureSearchQuery();
            q2.setLocationIds(Arrays.asList(locA.getId()));
            q2.setAlarmOnly(true);
            q2.setSize(1);
            q2.setCursor(r1.getNextCursor());
            PageResult<TemperatureVO> r2 = searchService.searchTemperatures(q2);
            assertEquals(1, r2.getRecords().size());
            assertEquals(LocalDateTime.of(2026, 3, 1, 10, 0), r2.getRecords().get(0).getRecordedAt());
            assertNull(r2.getNextCursor());
        });
    }

    // ============================== helpers ==============================

    private SampleSearchQuery buildPaging(int page, int size) {
        SampleSearchQuery q = new SampleSearchQuery();
        q.setPage(page);
        q.setSize(size);
        return q;
    }

    private long allSampleTotal() {
        SampleSearchQuery q = new SampleSearchQuery();
        q.setPage(1);
        q.setSize(1);
        return searchService.searchSamples(q).getTotal();
    }

    private IceLayerSlice slice(Long sampleId, Long locationId, Long tenantId, String layer, int index) {
        IceLayerSlice slice = new IceLayerSlice();
        slice.setSampleId(sampleId);
        slice.setLocationId(locationId);
        slice.setTenantId(tenantId);
        slice.setLayerNo(layer);
        slice.setSliceIndex(index);
        slice.setDepthTop(new BigDecimal(index).multiply(new BigDecimal("0.5")));
        slice.setDepthBottom(new BigDecimal(index + 1).multiply(new BigDecimal("0.5")));
        slice.setTemperature(new BigDecimal("-19.00"));
        slice.setMeasuredAt(LocalDateTime.of(2026, 3, 1, 0, 0));
        slice.setCreateTime(LocalDateTime.now());
        return slice;
    }

    private FreezerTemperature temp(Long locationId, Long tenantId, LocalDateTime at,
                                    BigDecimal measured, int alarm) {
        FreezerTemperature t = new FreezerTemperature();
        t.setLocationId(locationId);
        t.setTenantId(tenantId);
        t.setRecordedAt(at);
        t.setMeasuredTemperature(measured);
        t.setSetTemperature(new BigDecimal("-18.00"));
        t.setDeviation(measured.subtract(new BigDecimal("-18.00")).abs());
        t.setAlarmFlag(alarm);
        t.setCreateTime(LocalDateTime.now());
        return t;
    }

    private TenantReq tenantReq(String code) {
        TenantReq req = new TenantReq();
        req.setTenantCode(code);
        req.setTenantName("租户-" + code);
        return req;
    }

    private AccountReq accountReq(String username, Long tenantId, String role) {
        AccountReq req = new AccountReq();
        req.setUsername(username);
        req.setTenantId(tenantId);
        req.setRoleCode(role);
        return req;
    }

    private DrillTaskReq taskReq(String no) {
        DrillTaskReq req = new DrillTaskReq();
        req.setTaskNo(no);
        req.setTaskName("任务-" + no);
        req.setSiteName("Dome A");
        req.setDrillDepth(new BigDecimal("800.00"));
        return req;
    }

    private StorageLocationReq locationReq(String code, String layer) {
        StorageLocationReq req = new StorageLocationReq();
        req.setLocationCode(code);
        req.setWarehouse("冷库A");
        req.setShelfNo("S1");
        req.setLayerNo(layer);
        req.setSetTemperature(new BigDecimal("-18.00"));
        req.setCapacity(20);
        return req;
    }

    private SampleBoxReq boxReq(String no, Long taskId, Long locationId, int capacity) {
        SampleBoxReq req = new SampleBoxReq();
        req.setBoxNo(no);
        req.setTaskId(taskId);
        req.setLocationId(locationId);
        req.setCapacity(capacity);
        return req;
    }

    private IceCoreSampleReq sampleReq(String no, Long boxId) {
        return sampleReq(no, boxId, "L-A", new BigDecimal("-18.00"),
                LocalDateTime.of(2026, 3, 1, 10, 0));
    }

    private IceCoreSampleReq sampleReq(String no, Long boxId, String layer,
                                       BigDecimal temp, LocalDateTime at) {
        IceCoreSampleReq req = new IceCoreSampleReq();
        req.setSampleNo(no);
        req.setBoxId(boxId);
        req.setLayerNo(layer);
        req.setDepthTop(new BigDecimal("100.00"));
        req.setDepthBottom(new BigDecimal("100.55"));
        req.setTemperature(temp);
        req.setMeltWaterMl(new BigDecimal("12.50"));
        req.setIntegrityPct(new BigDecimal("98.50"));
        req.setSampledAt(at);
        return req;
    }

    private BatchCreateReq batchReq(String no, Long... sampleIds) {
        BatchCreateReq req = new BatchCreateReq();
        req.setBatchNo(no);
        req.setBatchName("批次-" + no);
        req.setAnalysisType("氧同位素");
        req.setSampleIds(Arrays.asList(sampleIds));
        return req;
    }
}
