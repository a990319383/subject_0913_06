package com.evops;

import com.evops.common.BizException;
import com.evops.constant.AnalysisBatchStatus;
import com.evops.constant.DrillTaskStatus;
import com.evops.constant.IceCoreSampleStatus;
import com.evops.constant.SampleBoxStatus;
import com.evops.constant.StorageLocationStatus;
import com.evops.dto.BatchCreateReq;
import com.evops.dto.DrillTaskReq;
import com.evops.dto.IceCoreSampleReq;
import com.evops.dto.SampleBoxReq;
import com.evops.dto.StorageLocationReq;
import com.evops.entity.AnalysisBatch;
import com.evops.entity.DrillTask;
import com.evops.entity.IceCoreSample;
import com.evops.entity.SampleBox;
import com.evops.entity.StorageLocation;
import com.evops.service.AnalysisBatchService;
import com.evops.service.DrillTaskService;
import com.evops.service.IceCoreSampleService;
import com.evops.service.SampleBoxService;
import com.evops.service.StorageLocationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class IceCoreClosedLoopTest {

    @Autowired
    private DrillTaskService drillTaskService;
    @Autowired
    private StorageLocationService storageLocationService;
    @Autowired
    private SampleBoxService sampleBoxService;
    @Autowired
    private IceCoreSampleService iceCoreSampleService;
    @Autowired
    private AnalysisBatchService analysisBatchService;

    @Test
    void duplicateBusinessKeyRejected() {
        drillTaskService.create(taskReq("TASK-DUP"));
        BizException ex = assertThrows(BizException.class,
                () -> drillTaskService.create(taskReq("TASK-DUP")));
        assertTrue(ex.getMessage().contains("已存在"));

        storageLocationService.create(locationReq("LOC-DUP"));
        assertThrows(BizException.class, () -> storageLocationService.create(locationReq("LOC-DUP")));
    }

    @Test
    void batchLifecycleClosedLoop() {
        DrillTask task = drillTaskService.create(taskReq("TASK-FLOW"));
        StorageLocation location = storageLocationService.create(locationReq("LOC-FLOW"));
        SampleBox box = sampleBoxService.create(boxReq("BOX-FLOW", task.getId(), location.getId(), 2));
        assertEquals(SampleBoxStatus.EMPTY, box.getStatus());

        IceCoreSample s1 = iceCoreSampleService.create(sampleReq("IC-FLOW-1", box.getId()));
        IceCoreSample s2 = iceCoreSampleService.create(sampleReq("IC-FLOW-2", box.getId()));
        assertEquals(IceCoreSampleStatus.STORED, s1.getStatus());
        assertEquals(SampleBoxStatus.FULL, sampleBoxService.getById(box.getId()).getStatus());

        AnalysisBatch batch = analysisBatchService.createBatch(
                batchReq("BAT-FLOW", s1.getId(), s2.getId()));
        assertEquals(AnalysisBatchStatus.CREATED, batch.getStatus());
        assertEquals(IceCoreSampleStatus.IN_ANALYSIS, iceCoreSampleService.getById(s1.getId()).getStatus());

        analysisBatchService.transit(batch.getId(), AnalysisBatchStatus.TESTING);
        AnalysisBatch accepted = analysisBatchService.transit(batch.getId(), AnalysisBatchStatus.ACCEPTED);
        assertNotNull(accepted.getAcceptedTime());

        // 已验收不能删除
        BizException ex = assertThrows(BizException.class, () -> analysisBatchService.deleteById(batch.getId()));
        assertTrue(ex.getMessage().contains("已验收或已落账"));

        AnalysisBatch posted = analysisBatchService.transit(batch.getId(), AnalysisBatchStatus.POSTED);
        assertNotNull(posted.getPostedTime());
        assertEquals(IceCoreSampleStatus.CONSUMED, iceCoreSampleService.getById(s1.getId()).getStatus());
        assertEquals(IceCoreSampleStatus.CONSUMED, iceCoreSampleService.getById(s2.getId()).getStatus());

        // 已落账不能删除
        assertThrows(BizException.class, () -> analysisBatchService.deleteById(batch.getId()));
    }

    @Test
    void cancelBatchReleasesSamples() {
        DrillTask task = drillTaskService.create(taskReq("TASK-CXL"));
        SampleBox box = sampleBoxService.create(boxReq("BOX-CXL", task.getId(), null, 5));
        IceCoreSample sample = iceCoreSampleService.create(sampleReq("IC-CXL-1", box.getId()));

        AnalysisBatch batch = analysisBatchService.createBatch(batchReq("BAT-CXL", sample.getId()));
        assertEquals(IceCoreSampleStatus.IN_ANALYSIS, iceCoreSampleService.getById(sample.getId()).getStatus());

        analysisBatchService.transit(batch.getId(), AnalysisBatchStatus.CANCELLED);
        assertEquals(IceCoreSampleStatus.STORED, iceCoreSampleService.getById(sample.getId()).getStatus());

        // 已取消批次可删除，样本保持已入库
        analysisBatchService.deleteById(batch.getId());
        assertEquals(IceCoreSampleStatus.STORED, iceCoreSampleService.getById(sample.getId()).getStatus());
    }

    @Test
    void illegalTransitionRejected() {
        DrillTask task = drillTaskService.create(taskReq("TASK-ILL"));
        assertThrows(BizException.class,
                () -> drillTaskService.transit(task.getId(), DrillTaskStatus.COMPLETED));

        SampleBox box = sampleBoxService.create(boxReq("BOX-ILL", task.getId(), null, 5));
        IceCoreSample sample = iceCoreSampleService.create(sampleReq("IC-ILL-1", box.getId()));
        AnalysisBatch batch = analysisBatchService.createBatch(batchReq("BAT-ILL", sample.getId()));
        // 已建立不能直接验收，必须先检测
        assertThrows(BizException.class,
                () -> analysisBatchService.transit(batch.getId(), AnalysisBatchStatus.ACCEPTED));
    }

    @Test
    void boxCapacityEnforced() {
        DrillTask task = drillTaskService.create(taskReq("TASK-CAP"));
        SampleBox box = sampleBoxService.create(boxReq("BOX-CAP", task.getId(), null, 1));
        iceCoreSampleService.create(sampleReq("IC-CAP-1", box.getId()));
        BizException ex = assertThrows(BizException.class,
                () -> iceCoreSampleService.create(sampleReq("IC-CAP-2", box.getId())));
        assertTrue(ex.getMessage().contains("已满") || ex.getMessage().contains("不能存入"));
    }

    @Test
    void associationDeleteProtection() {
        DrillTask task = drillTaskService.create(taskReq("TASK-DEL"));
        StorageLocation location = storageLocationService.create(locationReq("LOC-DEL"));
        SampleBox box = sampleBoxService.create(boxReq("BOX-DEL", task.getId(), location.getId(), 5));
        IceCoreSample sample = iceCoreSampleService.create(sampleReq("IC-DEL-1", box.getId()));

        assertThrows(BizException.class, () -> drillTaskService.deleteById(task.getId()));
        assertThrows(BizException.class, () -> sampleBoxService.deleteById(box.getId()));
        assertThrows(BizException.class, () -> storageLocationService.deleteById(location.getId()));

        AnalysisBatch batch = analysisBatchService.createBatch(batchReq("BAT-DEL", sample.getId()));
        // 分析中的样本不能删除
        assertThrows(BizException.class, () -> iceCoreSampleService.deleteById(sample.getId()));

        // 移出批次后可删除
        analysisBatchService.removeSample(batch.getId(), sample.getId());
        iceCoreSampleService.deleteById(sample.getId());
        assertEquals(SampleBoxStatus.EMPTY, sampleBoxService.getById(box.getId()).getStatus());
    }

    @Test
    void temperatureAlertDetected() {
        DrillTask task = drillTaskService.create(taskReq("TASK-TMP"));
        StorageLocation location = storageLocationService.create(locationReq("LOC-TMP"));
        SampleBox box = sampleBoxService.create(boxReq("BOX-TMP", task.getId(), location.getId(), 5));
        IceCoreSampleReq warm = sampleReq("IC-TMP-1", box.getId());
        warm.setTemperature(new BigDecimal("-5.00"));
        iceCoreSampleService.create(warm);

        java.util.List<Map<String, Object>> alerts =
                iceCoreSampleService.temperatureAlerts(new BigDecimal("2"));
        assertEquals(1, alerts.size());
        assertEquals("IC-TMP-1", alerts.get(0).get("sampleNo"));
    }

    private DrillTaskReq taskReq(String taskNo) {
        DrillTaskReq req = new DrillTaskReq();
        req.setTaskNo(taskNo);
        req.setTaskName("冰芯钻取-" + taskNo);
        req.setSiteName("东南极冰盖Dome A");
        req.setDrillDepth(new BigDecimal("800.00"));
        return req;
    }

    private StorageLocationReq locationReq(String code) {
        StorageLocationReq req = new StorageLocationReq();
        req.setLocationCode(code);
        req.setWarehouse("极地冷库A");
        req.setShelfNo("S1");
        req.setLayerNo("L1");
        req.setSetTemperature(new BigDecimal("-18.00"));
        req.setCapacity(10);
        return req;
    }

    private SampleBoxReq boxReq(String boxNo, Long taskId, Long locationId, int capacity) {
        SampleBoxReq req = new SampleBoxReq();
        req.setBoxNo(boxNo);
        req.setTaskId(taskId);
        req.setLocationId(locationId);
        req.setCapacity(capacity);
        return req;
    }

    private IceCoreSampleReq sampleReq(String sampleNo, Long boxId) {
        IceCoreSampleReq req = new IceCoreSampleReq();
        req.setSampleNo(sampleNo);
        req.setBoxId(boxId);
        req.setLayerNo("L-001");
        req.setDepthTop(new BigDecimal("100.00"));
        req.setDepthBottom(new BigDecimal("100.55"));
        req.setTemperature(new BigDecimal("-17.50"));
        req.setMeltWaterMl(new BigDecimal("12.50"));
        req.setIntegrityPct(new BigDecimal("98.50"));
        return req;
    }

    private BatchCreateReq batchReq(String batchNo, Long... sampleIds) {
        BatchCreateReq req = new BatchCreateReq();
        req.setBatchNo(batchNo);
        req.setBatchName("同位素分析-" + batchNo);
        req.setAnalysisType("氧同位素");
        req.setSampleIds(Arrays.asList(sampleIds));
        return req;
    }
}
