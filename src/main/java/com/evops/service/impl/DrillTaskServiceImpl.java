package com.evops.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evops.common.BizException;
import com.evops.constant.DrillTaskStatus;
import com.evops.dto.DrillTaskReq;
import com.evops.entity.DrillTask;
import com.evops.mapper.DrillTaskMapper;
import com.evops.mapper.IceCoreSampleMapper;
import com.evops.mapper.SampleBoxMapper;
import com.evops.security.DataScopeService;
import com.evops.security.QueryScope;
import com.evops.service.DrillTaskService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DrillTaskServiceImpl extends ServiceImpl<DrillTaskMapper, DrillTask> implements DrillTaskService {

    private final SampleBoxMapper sampleBoxMapper;
    private final IceCoreSampleMapper sampleMapper;
    private final DataScopeService dataScopeService;

    public DrillTaskServiceImpl(SampleBoxMapper sampleBoxMapper, IceCoreSampleMapper sampleMapper,
                                DataScopeService dataScopeService) {
        this.sampleBoxMapper = sampleBoxMapper;
        this.sampleMapper = sampleMapper;
        this.dataScopeService = dataScopeService;
    }

    @Override
    public DrillTask create(DrillTaskReq req) {
        ensureTaskNoAvailable(req.getTaskNo());
        DrillTask task = new DrillTask();
        BeanUtils.copyProperties(req, task);
        task.setId(null);
        task.setStatus(DrillTaskStatus.PLANNED);
        task.setTenantId(com.evops.security.TenantContext.currentTenantId());
        save(task);
        return task;
    }

    @Override
    public DrillTask modify(Long id, DrillTaskReq req) {
        DrillTask task = mustExist(id);
        if (DrillTaskStatus.COMPLETED.equals(task.getStatus()) || DrillTaskStatus.CANCELLED.equals(task.getStatus())) {
            throw new BizException("已完成或已取消的任务不能修改");
        }
        // 业务键 taskNo 不可修改，仅更新可编辑字段
        task.setTaskName(req.getTaskName());
        task.setSiteName(req.getSiteName());
        task.setDrillDepth(req.getDrillDepth());
        task.setPlannedStart(req.getPlannedStart());
        task.setPlannedEnd(req.getPlannedEnd());
        task.setRemark(req.getRemark());
        updateById(task);
        return task;
    }

    @Override
    public DrillTask transit(Long id, String targetStatus) {
        DrillTask task = mustExist(id);
        if (!DrillTaskStatus.canTransit(task.getStatus(), targetStatus)) {
            throw new BizException("任务状态不允许从 " + task.getStatus() + " 流转为 " + targetStatus);
        }
        task.setStatus(targetStatus);
        updateById(task);
        return task;
    }

    @Override
    public void deleteById(Long id) {
        mustExist(id);
        Long boxCount = sampleBoxMapper.selectCount(new LambdaQueryWrapper<com.evops.entity.SampleBox>()
                .eq(com.evops.entity.SampleBox::getTaskId, id));
        if (boxCount != null && boxCount > 0) {
            throw new BizException("任务下存在样本盒，不能删除");
        }
        Long sampleCount = sampleMapper.selectCount(new LambdaQueryWrapper<com.evops.entity.IceCoreSample>()
                .eq(com.evops.entity.IceCoreSample::getTaskId, id));
        if (sampleCount != null && sampleCount > 0) {
            throw new BizException("任务下存在冰芯样本，不能删除");
        }
        removeById(id);
    }

    @Override
    public Page<DrillTask> pageQuery(String status, String keyword, int page, int size) {
        com.evops.common.page.PageParams.validate(page, size);
        QueryScope scope = dataScopeService.currentScope();
        LambdaQueryWrapper<DrillTask> wrapper = new LambdaQueryWrapper<DrillTask>()
                .eq(StringUtils.hasText(status), DrillTask::getStatus, status)
                .and(StringUtils.hasText(keyword), w -> w.like(DrillTask::getTaskNo, keyword)
                        .or().like(DrillTask::getTaskName, keyword));
        if (com.evops.security.DataScopeFilters.isNone(scope)) {
            return new Page<>(page, size);
        }
        com.evops.security.DataScopeFilters.applyTask(wrapper, scope);
        wrapper.orderByDesc(DrillTask::getId);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public Map<String, Object> detail(Long id) {
        DrillTask task = mustExist(id);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("task", task);
        result.put("boxes", sampleBoxMapper.selectList(new LambdaQueryWrapper<com.evops.entity.SampleBox>()
                .eq(com.evops.entity.SampleBox::getTaskId, id)));
        result.put("sampleCount", sampleMapper.selectCount(new LambdaQueryWrapper<com.evops.entity.IceCoreSample>()
                .eq(com.evops.entity.IceCoreSample::getTaskId, id)));
        return result;
    }

    private void ensureTaskNoAvailable(String taskNo) {
        Long count = count(new LambdaQueryWrapper<DrillTask>().eq(DrillTask::getTaskNo, taskNo));
        if (count != null && count > 0) {
            throw new BizException("任务编号已存在: " + taskNo);
        }
    }

    private DrillTask mustExist(Long id) {
        DrillTask task = getById(id);
        if (task == null) {
            throw new BizException("钻取任务不存在: " + id);
        }
        return task;
    }
}
