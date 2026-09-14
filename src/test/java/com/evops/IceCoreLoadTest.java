package com.evops;

import com.evops.service.LoadTestDataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 领域压测：100,000 条分钟级库温 + 500,000 个层位切片。
 * 验证大数据量下总数正确、层位分区/时间倒序稳定排序、游标深翻页不丢不重。
 * 类级事务保证压测数据用后回滚，不影响其他测试。
 */
@SpringBootTest
@Transactional
class IceCoreLoadTest {

    @Autowired
    private LoadTestDataService loadTestDataService;

    @Test
    @SuppressWarnings("unchecked")
    void stablePagingOn100kTemperaturesAnd500kSlices() {
        Map<String, Object> result = loadTestDataService.seedAndBenchmark(
                100_000, 500_000, true, true);

        assertEquals(100_000L, ((Number) result.get("temperatureEvents")).longValue());
        assertEquals(500_000L, ((Number) result.get("layerSlices")).longValue());

        Map<String, Object> bench = (Map<String, Object>) result.get("benchmark");
        assertEquals(500_000L, ((Number) bench.get("sliceTotal")).longValue());
        // 50 页 * 100 = 5000 行游标翻页，层位分区 + 主键排序严格稳定
        assertEquals(50, ((Number) bench.get("sliceCursorPages")).intValue());
        assertEquals(5000, ((Number) bench.get("sliceCursorRows")).intValue());
        assertEquals(Boolean.TRUE, bench.get("sliceOrderStable"));
        // 25 万行处的深 offset 页仍能稳定返回整页 100 条
        assertEquals(100, ((Number) bench.get("sliceDeepOffsetSize")).intValue());
        // 100,000 条分钟级库温按 (recorded_at DESC, id DESC) 游标翻页严格递减
        assertEquals(Boolean.TRUE, bench.get("temperatureOrderStable"));
        // 100 个层位分区（L-000 .. L-099）
        assertEquals(100L, ((Number) bench.get("partitionCount")).longValue());

        // 各查询在合理时间内返回（宽松上限，覆盖 CI 抖动）
        assertTrue(((Number) bench.get("sliceFirstPageMillis")).longValue() < 5_000L,
                "切片首页超时");
        assertTrue(((Number) bench.get("temperatureCursorMillis")).longValue() < 10_000L,
                "库温游标翻页超时");
    }
}
