package com.evops.constant;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 样本盒状态：EMPTY 空盒 / IN_USE 使用中 / FULL 已满 / SEALED 已封存 / ARCHIVED 已归档。
 * EMPTY/IN_USE/FULL 随样本存取自动切换；手工流转仅允许封存与归档。
 */
public final class SampleBoxStatus {
    public static final String EMPTY = "EMPTY";
    public static final String IN_USE = "IN_USE";
    public static final String FULL = "FULL";
    public static final String SEALED = "SEALED";
    public static final String ARCHIVED = "ARCHIVED";

    private static final Map<String, Set<String>> TRANSITIONS = new HashMap<>();
    static {
        TRANSITIONS.put(IN_USE, new HashSet<>(Collections.singletonList(SEALED)));
        TRANSITIONS.put(FULL, new HashSet<>(Collections.singletonList(SEALED)));
        TRANSITIONS.put(SEALED, new HashSet<>(Collections.singletonList(ARCHIVED)));
    }

    private SampleBoxStatus() {
    }

    public static boolean canTransit(String from, String to) {
        Set<String> allowed = TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }
}
