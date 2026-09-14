package com.evops.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evops.common.BizException;
import com.evops.constant.AnalysisBatchStatus;
import com.evops.constant.IceCoreSampleStatus;
import com.evops.constant.SampleBoxStatus;
import com.evops.dto.IceCoreSampleReq;
import com.evops.entity.AnalysisBatch;
import com.evops.entity.AnalysisBatchItem;
import com.evops.entity.IceCoreSample;
import com.evops.entity.SampleBox;
import com.evops.entity.StorageLocation;
import com.evops.mapper.AnalysisBatchItemMapper;
import com.evops.mapper.AnalysisBatchMapper;
import com.evops.mapper.IceCoreSampleMapper;
import com.evops.security.DataScopeService;
import com.evops.service.DrillTaskService;
import com.evops.service.IceCoreSampleService;
import com.evops.service.SampleBoxService;
import com.evops.service.StorageLocationService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class IceCoreSampleServiceImpl extends ServiceImpl<IceCoreSampleMapper, IceCoreSample>
        implements IceCoreSampleService {

    private final SampleBoxService sampleBoxService;
    private final DrillTaskService drillTaskService;
    private final StorageLocationService storageLocationService;
    private final AnalysisBatchItemMapper batchItemMapper;
    private final AnalysisBatchMapper batchMapper;
    private final DataScopeService dataScopeService;

    public IceCoreSampleServiceImpl(SampleBoxService sampleBoxService,
                                    DrillTaskService drillTaskService,
                                    StorageLocationService storageLocationService,
                                    AnalysisBatchItemMapper batchItemMapper,
                                    AnalysisBatchMapper batchMapper,
                                    DataScopeService dataScopeService) {
        this.sampleBoxService = sampleBoxService;
        this.drillTaskService = drillTaskService;
        this.storageLocationService = storageLocationService;
        this.batchItemMapper = batchItemMapper;
        this.batchMapper = batchMapper;
        this.dataScopeService = dataScopeService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IceCoreSample create(IceCoreSampleReq req) {
        Long count = count(new LambdaQueryWrapper<IceCoreSample>()
                .eq(IceCoreSample::getSampleNo, req.getSampleNo()));
        if (count != null && count > 0) {
            throw new BizException("样本编号已存在: " + req.getSampleNo());
        }
        SampleBox box = sampleBoxService.getById(req.getBoxId());
        if (box == null) {
            throw new BizException("样本盒不存在: " + req.getBoxId());
        }
        if (!SampleBoxStatus.EMPTY.equals(box.getStatus()) && !SampleBoxStatus.IN_USE.equals(box.getStatus())) {
            throw new BizException("样本盒当前状态为 " + box.getStatus() + "，不能存入样本");
        }
        Long boxSamples = count(new LambdaQueryWrapper<IceCoreSample>()
                .eq(IceCoreSample::getBoxId, box.getId()));
        if (boxSamples != null && boxSamples >= box.getCapacity()) {
            throw new BizException("样本盒容量已满，不能继续存入");
        }
        IceCoreSample sample = new IceCoreSample();
        BeanUtils.copyProperties(req, sample);
        sample.setId(null);
        // 任务与库位跟随样本盒，避免归属不一致
        sample.setTaskId(box.getTaskId());
        sample.setLocationId(box.getLocationId());
        sample.setStatus(IceCoreSampleStatus.STORED);
        // 样本跟随样本盒租户，保证归属一致
        sample.setTenantId(box.getTenantId() == null ? 0L : box.getTenantId());
        save(sample);
        sampleBoxService.refreshLoadStatus(box.getId());
        return sample;
    }

    @Override
    public IceCoreSample modify(Long id, IceCoreSampleReq req) {
        IceCoreSample sample = mustExist(id);
        if (!IceCoreSampleStatus.STORED.equals(sample.getStatus())) {
            throw new BizException("样本当前状态为 " + sample.getStatus() + "，不能修改测量数据");
        }
        // 业务键 sampleNo、所属盒/任务不可修改，仅更新层位与冻融测量数据
        sample.setLayerNo(req.getLayerNo());
        sample.setDepthTop(req.getDepthTop());
        sample.setDepthBottom(req.getDepthBottom());
        sample.setTemperature(req.getTemperature());
        sample.setMeltWaterMl(req.getMeltWaterMl());
        sample.setIntegrityPct(req.getIntegrityPct());
        sample.setSampledAt(req.getSampledAt());
        sample.setRemark(req.getRemark());
        updateById(sample);
        return sample;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        IceCoreSample sample = mustExist(id);
        if (IceCoreSampleStatus.CONSUMED.equals(sample.getStatus())) {
            throw new BizException("已消耗的样本不能删除");
        }
        List<AnalysisBatchItem> items = batchItemMapper.selectList(
                new LambdaQueryWrapper<AnalysisBatchItem>().eq(AnalysisBatchItem::getSampleId, id));
        if (!items.isEmpty()) {
            List<Long> batchIds = items.stream().map(AnalysisBatchItem::getBatchId).collect(Collectors.toList());
            List<AnalysisBatch> batches = batchMapper.selectBatchIds(batchIds);
            for (AnalysisBatch batch : batches) {
                if (AnalysisBatchStatus.ACCEPTED.equals(batch.getStatus())
                        || AnalysisBatchStatus.POSTED.equals(batch.getStatus())) {
                    throw new BizException("样本所属批次 " + batch.getBatchNo() + " 已验收或已落账，不能删除");
                }
            }
            if (IceCoreSampleStatus.IN_ANALYSIS.equals(sample.getStatus())) {
                throw new BizException("样本已加入分析批次，请先从批次中移出");
            }
        }
        removeById(id);
        sampleBoxService.refreshLoadStatus(sample.getBoxId());
    }

    @Override
    public Page<IceCoreSample> pageQuery(Long taskId, Long boxId, Long batchId, String status, int page, int size) {
        com.evops.common.page.PageParams.validate(page, size);
        com.evops.security.QueryScope scope = dataScopeService.currentScope();
        if (com.evops.security.DataScopeFilters.isNone(scope)) {
            return new Page<>(page, size);
        }
        LambdaQueryWrapper<IceCoreSample> wrapper = new LambdaQueryWrapper<IceCoreSample>()
                .eq(taskId != null, IceCoreSample::getTaskId, taskId)
                .eq(boxId != null, IceCoreSample::getBoxId, boxId)
                .eq(StringUtils.hasText(status), IceCoreSample::getStatus, status);
        if (batchId != null) {
            // 批次明细对样本是一对一（同一样本同批次唯一），IN 主键不会放大主表；
            // 批次无样本时直接空页
            List<Long> sampleIds = batchItemMapper.selectList(new LambdaQueryWrapper<AnalysisBatchItem>()
                            .eq(AnalysisBatchItem::getBatchId, batchId))
                    .stream().map(AnalysisBatchItem::getSampleId).collect(Collectors.toList());
            if (sampleIds.isEmpty()) {
                return new Page<>(page, size);
            }
            wrapper.in(IceCoreSample::getId, sampleIds);
        }
        com.evops.security.DataScopeFilters.applySample(wrapper, scope);
        wrapper.orderByDesc(IceCoreSample::getId);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public Map<String, Object> detail(Long id) {
        IceCoreSample sample = mustExist(id);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("sample", sample);
        result.put("task", drillTaskService.getById(sample.getTaskId()));
        result.put("box", sampleBoxService.getById(sample.getBoxId()));
        result.put("location", sample.getLocationId() == null ? null
                : storageLocationService.getById(sample.getLocationId()));
        List<AnalysisBatchItem> items = batchItemMapper.selectList(
                new LambdaQueryWrapper<AnalysisBatchItem>().eq(AnalysisBatchItem::getSampleId, id));
        List<Map<String, Object>> batches = new ArrayList<>();
        if (!items.isEmpty()) {
            List<Long> batchIds = items.stream().map(AnalysisBatchItem::getBatchId).collect(Collectors.toList());
            for (AnalysisBatch batch : batchMapper.selectBatchIds(batchIds)) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("batchId", batch.getId());
                row.put("batchNo", batch.getBatchNo());
                row.put("batchName", batch.getBatchName());
                row.put("status", batch.getStatus());
                batches.add(row);
            }
        }
        result.put("batches", batches);
        return result;
    }

    @Override
    public List<Map<String, Object>> temperatureAlerts(BigDecimal threshold) {
        List<IceCoreSample> samples = list(new LambdaQueryWrapper<IceCoreSample>()
                .in(IceCoreSample::getStatus, IceCoreSampleStatus.STORED, IceCoreSampleStatus.IN_ANALYSIS)
                .isNotNull(IceCoreSample::getLocationId)
                .isNotNull(IceCoreSample::getTemperature));
        List<Map<String, Object>> alerts = new ArrayList<>();
        for (IceCoreSample sample : samples) {
            StorageLocation location = storageLocationService.getById(sample.getLocationId());
            if (location == null || location.getSetTemperature() == null) {
                continue;
            }
            BigDecimal deviation = sample.getTemperature().subtract(location.getSetTemperature()).abs();
            if (deviation.compareTo(threshold) > 0) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("sampleId", sample.getId());
                row.put("sampleNo", sample.getSampleNo());
                row.put("status", sample.getStatus());
                row.put("boxId", sample.getBoxId());
                row.put("locationCode", location.getLocationCode());
                row.put("temperature", sample.getTemperature());
                row.put("setTemperature", location.getSetTemperature());
                row.put("deviation", deviation);
                alerts.add(row);
            }
        }
        alerts.sort(Comparator.comparing(r -> (BigDecimal) r.get("deviation"), Comparator.reverseOrder()));
        return alerts;
    }

    private IceCoreSample mustExist(Long id) {
        IceCoreSample sample = getById(id);
        if (sample == null) {
            throw new BizException("冰芯样本不存在: " + id);
        }
        return sample;
    }
}
