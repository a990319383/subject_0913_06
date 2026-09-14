package com.evops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evops.entity.SecurityTenant;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SecurityTenantMapper extends BaseMapper<SecurityTenant> {
}
