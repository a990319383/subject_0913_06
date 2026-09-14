package com.evops.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evops.common.BizException;
import com.evops.constant.StorageLocationStatus;
import com.evops.dto.StorageLocationReq;
import com.evops.entity.IceCoreSample;
import com.evops.entity.SampleBox;
import com.evops.entity.StorageLocation;
import com.evops.mapper.IceCoreSampleMapper;
import com.evops.mapper.SampleBoxMapper;
import com.evops.mapper.StorageLocationMapper;
import com.evops.security.DataScopeFilters;
import com.evops.security.DataScopeService;
import com.evops.security.QueryScope;
import com.evops.service.StorageLocationService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class StorageLocationServiceImpl extends ServiceImpl<StorageLocationMapper, StorageLocation>
        implements StorageLocationService {

    private final SampleBoxMapper sampleBoxMapper;
    private final IceCoreSampleMapper sampleMapper;
    private final DataScopeService dataScopeService;

    public StorageLocationServiceImpl(SampleBoxMapper sampleBoxMapper, IceCoreSampleMapper sampleMapper,
                                      DataScopeService dataScopeService) {
        this.sampleBoxMapper = sampleBoxMapper;
        this.sampleMapper = sampleMapper;
        this.dataScopeService = dataScopeService;
    }

    @Override
    public StorageLocation create(StorageLocationReq req) {
        Long count = count(new LambdaQueryWrapper<StorageLocation>()
                .eq(StorageLocation::getLocationCode, req.getLocationCode()));
        if (count != null && count > 0) {
            throw new BizException("库位编码已存在: " + req.getLocationCode());
        }
        StorageLocation location = new StorageLocation();
        BeanUtils.copyProperties(req, location);
        location.setId(null);
        location.setStatus(StorageLocationStatus.AVAILABLE);
        location.setTenantId(com.evops.security.TenantContext.currentTenantId());
        save(location);
        return location;
    }

    @Override
    public StorageLocation modify(Long id, StorageLocationReq req) {
        StorageLocation location = mustExist(id);
        // 业务键 locationCode 不可修改
        Long boxCount = boxCount(id);
        if (req.getCapacity() != null && req.getCapacity() < boxCount) {
            throw new BizException("库位容量不能小于当前已存放样本盒数量 " + boxCount);
        }
        location.setWarehouse(req.getWarehouse());
        location.setShelfNo(req.getShelfNo());
        location.setLayerNo(req.getLayerNo());
        location.setSetTemperature(req.getSetTemperature());
        location.setCapacity(req.getCapacity());
        location.setRemark(req.getRemark());
        updateById(location);
        refreshLoadStatus(id);
        return location;
    }

    @Override
    public StorageLocation transit(Long id, String targetStatus) {
        StorageLocation location = mustExist(id);
        if (!StorageLocationStatus.canTransit(location.getStatus(), targetStatus)) {
            throw new BizException("库位状态不允许从 " + location.getStatus() + " 流转为 " + targetStatus);
        }
        location.setStatus(targetStatus);
        updateById(location);
        return location;
    }

    @Override
    public void deleteById(Long id) {
        mustExist(id);
        if (boxCount(id) > 0) {
            throw new BizException("库位内存放有样本盒，不能删除");
        }
        Long sampleCount = sampleMapper.selectCount(new LambdaQueryWrapper<IceCoreSample>()
                .eq(IceCoreSample::getLocationId, id));
        if (sampleCount != null && sampleCount > 0) {
            throw new BizException("库位关联有冰芯样本，不能删除");
        }
        removeById(id);
    }

    @Override
    public Page<StorageLocation> pageQuery(String status, int page, int size) {
        com.evops.common.page.PageParams.validate(page, size);
        QueryScope scope = dataScopeService.currentScope();
        LambdaQueryWrapper<StorageLocation> wrapper = new LambdaQueryWrapper<StorageLocation>()
                .eq(StringUtils.hasText(status), StorageLocation::getStatus, status);
        if (DataScopeFilters.isNone(scope)) {
            return new Page<>(page, size);
        }
        DataScopeFilters.applyLocation(wrapper, scope);
        wrapper.orderByDesc(StorageLocation::getId);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public Map<String, Object> detail(Long id) {
        StorageLocation location = mustExist(id);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("location", location);
        result.put("boxes", sampleBoxMapper.selectList(new LambdaQueryWrapper<SampleBox>()
                .eq(SampleBox::getLocationId, id)));
        return result;
    }

    @Override
    public void refreshLoadStatus(Long locationId) {
        if (locationId == null) {
            return;
        }
        StorageLocation location = getById(locationId);
        if (location == null || StorageLocationStatus.MAINTENANCE.equals(location.getStatus())) {
            return;
        }
        String next = boxCount(locationId) >= location.getCapacity()
                ? StorageLocationStatus.FULL : StorageLocationStatus.AVAILABLE;
        if (!next.equals(location.getStatus())) {
            location.setStatus(next);
            updateById(location);
        }
    }

    private long boxCount(Long locationId) {
        Long count = sampleBoxMapper.selectCount(new LambdaQueryWrapper<SampleBox>()
                .eq(SampleBox::getLocationId, locationId));
        return count == null ? 0 : count;
    }

    private StorageLocation mustExist(Long id) {
        StorageLocation location = getById(id);
        if (location == null) {
            throw new BizException("库位不存在: " + id);
        }
        return location;
    }
}
