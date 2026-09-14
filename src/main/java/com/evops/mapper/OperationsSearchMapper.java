package com.evops.mapper;

import com.evops.dto.search.LayerSliceSearchQuery;
import com.evops.dto.search.SampleSearchQuery;
import com.evops.dto.search.TemperatureSearchQuery;
import com.evops.security.QueryScope;
import com.evops.vo.LayerSliceVO;
import com.evops.vo.SampleSearchVO;
import com.evops.vo.TemperatureVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 运营检索 Mapper：全部检索以主表为驱动，关联条件用 EXISTS 半连接，
 * 关联表一对多（如批次明细）不会放大主表行数。
 */
@Mapper
public interface OperationsSearchMapper {

    List<SampleSearchVO> searchSamples(@Param("q") SampleSearchQuery q, @Param("scope") QueryScope scope);

    long countSamples(@Param("q") SampleSearchQuery q, @Param("scope") QueryScope scope);

    List<LayerSliceVO> searchLayerSlices(@Param("q") LayerSliceSearchQuery q, @Param("scope") QueryScope scope);

    long countLayerSlices(@Param("q") LayerSliceSearchQuery q, @Param("scope") QueryScope scope);

    List<TemperatureVO> searchTemperatures(@Param("q") TemperatureSearchQuery q, @Param("scope") QueryScope scope);

    long countTemperatures(@Param("q") TemperatureSearchQuery q, @Param("scope") QueryScope scope);

    /** 层位分区汇总行：layerNo / sliceCount */
    List<Map<String, Object>> layerPartitions(@Param("locationId") Long locationId,
                                               @Param("scope") QueryScope scope,
                                               @Param("lastLayerNo") String lastLayerNo,
                                               @Param("cursorMode") boolean cursorMode,
                                               @Param("offset") long offset,
                                               @Param("limit") int limit);

    long countLayerPartitions(@Param("locationId") Long locationId, @Param("scope") QueryScope scope);
}
