package com.evops.constant;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 库位状态：AVAILABLE 可用 / FULL 已满（按样本盒数量自动维护）/ MAINTENANCE 维护中。
 * 手工流转仅允许 AVAILABLE <-> MAINTENANCE，FULL 由系统按负载自动切换。
 */
public final class StorageLocationStatus {
    public static final String AVAILABLE = "AVAILABLE";
    public static final String FULL = "FULL";
    public static final String MAINTENANCE = "MAINTENANCE";

    private static final Map<String, Set<String>> TRANSITIONS = new HashMap<>();
    static {
        TRANSITIONS.put(AVAILABLE, new HashSet<>(Collections.singletonList(MAINTENANCE)));
        TRANSITIONS.put(FULL, new HashSet<>(Collections.singletonList(MAINTENANCE)));
        TRANSITIONS.put(MAINTENANCE, new HashSet<>(Collections.singletonList(AVAILABLE)));
    }

    private StorageLocationStatus() {
    }

    public static boolean canTransit(String from, String to) {
        Set<String> allowed = TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }
}
