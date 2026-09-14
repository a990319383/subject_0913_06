package com.evops.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evops.common.BizException;
import com.evops.constant.SampleBoxStatus;
import com.evops.constant.StorageLocationStatus;
import com.evops.dto.SampleBoxReq;
import com.evops.entity.IceCoreSample;
import com.evops.entity.SampleBox;
import com.evops.entity.StorageLocation;
import com.evops.mapper.IceCoreSampleMapper;
import com.evops.mapper.SampleBoxMapper;
import com.evops.security.DataScopeFilters;
import com.evops.security.DataScopeService;
import com.evops.security.QueryScope;
import com.evops.service.DrillTaskService;
import com.evops.service.SampleBoxService;
import com.evops.service.StorageLocationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class SampleBoxServiceImpl extends ServiceImpl<SampleBoxMapper, SampleBox> implements SampleBoxService {

    private final DrillTaskService drillTaskService;
    private final StorageLocationService storageLocationService;
    private final IceCoreSampleMapper sampleMapper;
    private final DataScopeService dataScopeService;

    public SampleBoxServiceImpl(DrillTaskService drillTaskService,
                                StorageLocationService storageLocationService,
                                IceCoreSampleMapper sampleMapper,
                                DataScopeService dataScopeService) {
        this.drillTaskService = drillTaskService;
        this.storageLocationService = storageLocationService;
        this.sampleMapper = sampleMapper;
        this.dataScopeService = dataScopeService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SampleBox create(SampleBoxReq req) {
        Long count = count(new LambdaQueryWrapper<SampleBox>().eq(SampleBox::getBoxNo, req.getBoxNo()));
        if (count != null && count > 0) {
            throw new BizException("样本盒编号已存在: " + req.getBoxNo());
        }
        com.evops.entity.DrillTask task = drillTaskService.getById(req.getTaskId());
        if (task == null) {
            throw new BizException("钻取任务不存在: " + req.getTaskId());
        }
        ensureLocationUsable(req.getLocationId());
        SampleBox box = new SampleBox();
        box.setBoxNo(req.getBoxNo());
        box.setTaskId(req.getTaskId());
        box.setLocationId(req.getLocationId());
        box.setCapacity(req.getCapacity());
        box.setRemark(req.getRemark());
        box.setStatus(SampleBoxStatus.EMPTY);
        // 盒跟随任务租户，保证归属一致
        box.setTenantId(task.getTenantId() == null ? 0L : task.getTenantId());
        save(box);
        storageLocationService.refreshLoadStatus(req.getLocationId());
        return box;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SampleBox modify(Long id, SampleBoxReq req) {
        SampleBox box = mustExist(id);
        if (SampleBoxStatus.SEALED.equals(box.getStatus()) || SampleBoxStatus.ARCHIVED.equals(box.getStatus())) {
            throw new BizException("已封存或已归档的样本盒不能修改");
        }
        long sampleCount = sampleCount(id);
        if (req.getCapacity() != null && req.getCapacity() < sampleCount) {
            throw new BizException("样本盒容量不能小于当前样本数量 " + sampleCount);
        }
        Long oldLocationId = box.getLocationId();
        if (!Objects.equals(oldLocationId, req.getLocationId())) {
            ensureLocationUsable(req.getLocationId());
        }
        // 业务键 boxNo、所属任务不可修改
        box.setLocationId(req.getLocationId());
        box.setCapacity(req.getCapacity());
        box.setRemark(req.getRemark());
        updateById(box);
        // 库位变更时同步盒内样本的库位，并刷新两侧库位负载
        if (!Objects.equals(oldLocationId, req.getLocationId())) {
            List<IceCoreSample> samples = sampleMapper.selectList(new LambdaQueryWrapper<IceCoreSample>()
                    .eq(IceCoreSample::getBoxId, id));
            for (IceCoreSample sample : samples) {
                sample.setLocationId(req.getLocationId());
                sampleMapper.updateById(sample);
            }
            storageLocationService.refreshLoadStatus(oldLocationId);
            storageLocationService.refreshLoadStatus(req.getLocationId());
        }
        refreshLoadStatus(id);
        return box;
    }

    @Override
    public SampleBox transit(Long id, String targetStatus) {
        SampleBox box = mustExist(id);
        if (!SampleBoxStatus.canTransit(box.getStatus(), targetStatus)) {
            throw new BizException("样本盒状态不允许从 " + box.getStatus() + " 流转为 " + targetStatus);
        }
        box.setStatus(targetStatus);
        if (SampleBoxStatus.SEALED.equals(targetStatus)) {
            box.setSealedTime(LocalDateTime.now());
        }
        updateById(box);
        return box;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        SampleBox box = mustExist(id);
        if (SampleBoxStatus.ARCHIVED.equals(box.getStatus())) {
            throw new BizException("已归档的样本盒不能删除");
        }
        if (sampleCount(id) > 0) {
            throw new BizException("样本盒内仍有冰芯样本，不能删除");
        }
        removeById(id);
        storageLocationService.refreshLoadStatus(box.getLocationId());
    }

    @Override
    public Page<SampleBox> pageQuery(Long taskId, Long locationId, String status, int page, int size) {
        com.evops.common.page.PageParams.validate(page, size);
        QueryScope scope = dataScopeService.currentScope();
        LambdaQueryWrapper<SampleBox> wrapper = new LambdaQueryWrapper<SampleBox>()
                .eq(taskId != null, SampleBox::getTaskId, taskId)
                .eq(locationId != null, SampleBox::getLocationId, locationId)
                .eq(StringUtils.hasText(status), SampleBox::getStatus, status);
        if (DataScopeFilters.isNone(scope)) {
            return new Page<>(page, size);
        }
        DataScopeFilters.applyBox(wrapper, scope);
        wrapper.orderByDesc(SampleBox::getId);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public Map<String, Object> detail(Long id) {
        SampleBox box = mustExist(id);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("box", box);
        result.put("task", drillTaskService.getById(box.getTaskId()));
        result.put("location", box.getLocationId() == null ? null : storageLocationService.getById(box.getLocationId()));
        result.put("samples", sampleMapper.selectList(new LambdaQueryWrapper<IceCoreSample>()
                .eq(IceCoreSample::getBoxId, id)));
        return result;
    }

    @Override
    public void refreshLoadStatus(Long boxId) {
        SampleBox box = getById(boxId);
        if (box == null || SampleBoxStatus.SEALED.equals(box.getStatus())
                || SampleBoxStatus.ARCHIVED.equals(box.getStatus())) {
            return;
        }
        long count = sampleCount(boxId);
        String next;
        if (count == 0) {
            next = SampleBoxStatus.EMPTY;
        } else if (count >= box.getCapacity()) {
            next = SampleBoxStatus.FULL;
        } else {
            next = SampleBoxStatus.IN_USE;
        }
        if (!next.equals(box.getStatus())) {
            box.setStatus(next);
            updateById(box);
        }
    }

    private void ensureLocationUsable(Long locationId) {
        if (locationId == null) {
            return;
        }
        StorageLocation location = storageLocationService.getById(locationId);
        if (location == null) {
            throw new BizException("库位不存在: " + locationId);
        }
        if (!StorageLocationStatus.AVAILABLE.equals(location.getStatus())) {
            throw new BizException("库位当前状态为 " + location.getStatus() + "，不能存放样本盒");
        }
    }

    private long sampleCount(Long boxId) {
        Long count = sampleMapper.selectCount(new LambdaQueryWrapper<IceCoreSample>()
                .eq(IceCoreSample::getBoxId, boxId));
        return count == null ? 0 : count;
    }

    private SampleBox mustExist(Long id) {
        SampleBox box = getById(id);
        if (box == null) {
            throw new BizException("样本盒不存在: " + id);
        }
        return box;
    }
}
