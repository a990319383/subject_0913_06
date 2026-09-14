package com.evops.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evops.common.BizException;
import com.evops.constant.AnalysisBatchStatus;
import com.evops.constant.IceCoreSampleStatus;
import com.evops.dto.BatchCreateReq;
import com.evops.entity.AnalysisBatch;
import com.evops.entity.AnalysisBatchItem;
import com.evops.entity.IceCoreSample;
import com.evops.mapper.AnalysisBatchItemMapper;
import com.evops.mapper.AnalysisBatchMapper;
import com.evops.mapper.IceCoreSampleMapper;
import com.evops.service.AnalysisBatchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AnalysisBatchServiceImpl extends ServiceImpl<AnalysisBatchMapper, AnalysisBatch>
        implements AnalysisBatchService {

    private final AnalysisBatchItemMapper batchItemMapper;
    private final IceCoreSampleMapper sampleMapper;

    public AnalysisBatchServiceImpl(AnalysisBatchItemMapper batchItemMapper, IceCoreSampleMapper sampleMapper) {
        this.batchItemMapper = batchItemMapper;
        this.sampleMapper = sampleMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnalysisBatch createBatch(BatchCreateReq req) {
        Long count = count(new LambdaQueryWrapper<AnalysisBatch>()
                .eq(AnalysisBatch::getBatchNo, req.getBatchNo()));
        if (count != null && count > 0) {
            throw new BizException("批次编号已存在: " + req.getBatchNo());
        }
        List<Long> sampleIds = distinctIds(req.getSampleIds());
        Map<Long, IceCoreSample> samples = loadAndCheckSamples(sampleIds);

        AnalysisBatch batch = new AnalysisBatch();
        batch.setBatchNo(req.getBatchNo());
        batch.setBatchName(req.getBatchName());
        batch.setAnalysisType(req.getAnalysisType());
        batch.setRemark(req.getRemark());
        batch.setStatus(AnalysisBatchStatus.CREATED);
        save(batch);

        for (Long sampleId : sampleIds) {
            AnalysisBatchItem item = new AnalysisBatchItem();
            item.setBatchId(batch.getId());
            item.setSampleId(sampleId);
            batchItemMapper.insert(item);
        }
        markSamples(sampleIds, samples, IceCoreSampleStatus.IN_ANALYSIS);
        return batch;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSamples(Long batchId, List<Long> sampleIds) {
        AnalysisBatch batch = mustExist(batchId);
        if (!AnalysisBatchStatus.CREATED.equals(batch.getStatus())) {
            throw new BizException("只有已建立状态的批次才能添加样本");
        }
        List<Long> ids = distinctIds(sampleIds);
        Map<Long, IceCoreSample> samples = loadAndCheckSamples(ids);
        for (Long sampleId : ids) {
            Long exists = batchItemMapper.selectCount(new LambdaQueryWrapper<AnalysisBatchItem>()
                    .eq(AnalysisBatchItem::getBatchId, batchId)
                    .eq(AnalysisBatchItem::getSampleId, sampleId));
            if (exists != null && exists > 0) {
                throw new BizException("样本 " + samples.get(sampleId).getSampleNo() + " 已在该批次中");
            }
        }
        for (Long sampleId : ids) {
            AnalysisBatchItem item = new AnalysisBatchItem();
            item.setBatchId(batchId);
            item.setSampleId(sampleId);
            batchItemMapper.insert(item);
        }
        markSamples(ids, samples, IceCoreSampleStatus.IN_ANALYSIS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeSample(Long batchId, Long sampleId) {
        AnalysisBatch batch = mustExist(batchId);
        if (!AnalysisBatchStatus.CREATED.equals(batch.getStatus())) {
            throw new BizException("只有已建立状态的批次才能移出样本");
        }
        AnalysisBatchItem item = batchItemMapper.selectOne(new LambdaQueryWrapper<AnalysisBatchItem>()
                .eq(AnalysisBatchItem::getBatchId, batchId)
                .eq(AnalysisBatchItem::getSampleId, sampleId));
        if (item == null) {
            throw new BizException("样本不在该批次中: " + sampleId);
        }
        batchItemMapper.deleteById(item.getId());
        IceCoreSample sample = sampleMapper.selectById(sampleId);
        if (sample != null && IceCoreSampleStatus.IN_ANALYSIS.equals(sample.getStatus())) {
            sample.setStatus(IceCoreSampleStatus.STORED);
            sampleMapper.updateById(sample);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnalysisBatch transit(Long id, String targetStatus) {
        AnalysisBatch batch = mustExist(id);
        if (!AnalysisBatchStatus.canTransit(batch.getStatus(), targetStatus)) {
            throw new BizException("批次状态不允许从 " + batch.getStatus() + " 流转为 " + targetStatus);
        }
        List<Long> sampleIds = itemSampleIds(id);
        Map<Long, IceCoreSample> samples = loadSamples(sampleIds);
        if (AnalysisBatchStatus.ACCEPTED.equals(targetStatus)) {
            batch.setAcceptedTime(LocalDateTime.now());
        }
        if (AnalysisBatchStatus.POSTED.equals(targetStatus)) {
            // 落账：批次内样本核销为已消耗
            batch.setPostedTime(LocalDateTime.now());
            markSamples(sampleIds, samples, IceCoreSampleStatus.CONSUMED);
        }
        if (AnalysisBatchStatus.CANCELLED.equals(targetStatus)) {
            // 取消：释放仍在分析中的样本回库
            List<Long> inAnalysis = sampleIds.stream()
                    .filter(sid -> samples.get(sid) != null
                            && IceCoreSampleStatus.IN_ANALYSIS.equals(samples.get(sid).getStatus()))
                    .collect(Collectors.toList());
            markSamples(inAnalysis, samples, IceCoreSampleStatus.STORED);
        }
        batch.setStatus(targetStatus);
        updateById(batch);
        return batch;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        AnalysisBatch batch = mustExist(id);
        if (AnalysisBatchStatus.ACCEPTED.equals(batch.getStatus())
                || AnalysisBatchStatus.POSTED.equals(batch.getStatus())) {
            throw new BizException("已验收或已落账的批次不能删除");
        }
        List<Long> sampleIds = itemSampleIds(id);
        Map<Long, IceCoreSample> samples = loadSamples(sampleIds);
        List<Long> inAnalysis = sampleIds.stream()
                .filter(sid -> samples.get(sid) != null
                        && IceCoreSampleStatus.IN_ANALYSIS.equals(samples.get(sid).getStatus()))
                .collect(Collectors.toList());
        markSamples(inAnalysis, samples, IceCoreSampleStatus.STORED);
        batchItemMapper.delete(new LambdaQueryWrapper<AnalysisBatchItem>()
                .eq(AnalysisBatchItem::getBatchId, id));
        removeById(id);
    }

    @Override
    public Page<AnalysisBatch> pageQuery(String status, int page, int size) {
        LambdaQueryWrapper<AnalysisBatch> wrapper = new LambdaQueryWrapper<AnalysisBatch>()
                .eq(StringUtils.hasText(status), AnalysisBatch::getStatus, status)
                .orderByDesc(AnalysisBatch::getId);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public Map<String, Object> detail(Long id) {
        AnalysisBatch batch = mustExist(id);
        List<AnalysisBatchItem> items = batchItemMapper.selectList(
                new LambdaQueryWrapper<AnalysisBatchItem>().eq(AnalysisBatchItem::getBatchId, id));
        Map<Long, IceCoreSample> samples = loadSamples(
                items.stream().map(AnalysisBatchItem::getSampleId).collect(Collectors.toList()));
        List<Map<String, Object>> itemViews = new ArrayList<>();
        for (AnalysisBatchItem item : items) {
            IceCoreSample sample = samples.get(item.getSampleId());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("itemId", item.getId());
            row.put("sampleId", item.getSampleId());
            if (sample != null) {
                row.put("sampleNo", sample.getSampleNo());
                row.put("layerNo", sample.getLayerNo());
                row.put("sampleStatus", sample.getStatus());
                row.put("temperature", sample.getTemperature());
                row.put("meltWaterMl", sample.getMeltWaterMl());
                row.put("integrityPct", sample.getIntegrityPct());
            }
            itemViews.add(row);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("batch", batch);
        result.put("items", itemViews);
        return result;
    }

    private List<Long> distinctIds(List<Long> sampleIds) {
        if (sampleIds == null || sampleIds.isEmpty()) {
            throw new BizException("批次样本不能为空");
        }
        Set<Long> ids = new LinkedHashSet<>(sampleIds);
        return new ArrayList<>(ids);
    }

    private Map<Long, IceCoreSample> loadSamples(List<Long> sampleIds) {
        if (sampleIds == null || sampleIds.isEmpty()) {
            return new LinkedHashMap<>();
        }
        return sampleMapper.selectBatchIds(sampleIds).stream()
                .collect(Collectors.toMap(IceCoreSample::getId, Function.identity()));
    }

    private Map<Long, IceCoreSample> loadAndCheckSamples(List<Long> sampleIds) {
        Map<Long, IceCoreSample> samples = loadSamples(sampleIds);
        for (Long sampleId : sampleIds) {
            IceCoreSample sample = samples.get(sampleId);
            if (sample == null) {
                throw new BizException("冰芯样本不存在: " + sampleId);
            }
            if (!IceCoreSampleStatus.STORED.equals(sample.getStatus())) {
                throw new BizException("样本 " + sample.getSampleNo() + " 当前状态为 "
                        + sample.getStatus() + "，不能加入批次");
            }
        }
        return samples;
    }

    private void markSamples(List<Long> sampleIds, Map<Long, IceCoreSample> samples, String status) {
        for (Long sampleId : sampleIds) {
            IceCoreSample sample = samples.get(sampleId);
            if (sample != null) {
                sample.setStatus(status);
                sampleMapper.updateById(sample);
            }
        }
    }

    private List<Long> itemSampleIds(Long batchId) {
        return batchItemMapper.selectList(new LambdaQueryWrapper<AnalysisBatchItem>()
                        .eq(AnalysisBatchItem::getBatchId, batchId))
                .stream().map(AnalysisBatchItem::getSampleId).collect(Collectors.toList());
    }

    private AnalysisBatch mustExist(Long id) {
        AnalysisBatch batch = getById(id);
        if (batch == null) {
            throw new BizException("分析批次不存在: " + id);
        }
        return batch;
    }
}
