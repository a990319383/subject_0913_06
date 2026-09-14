package com.evops.constant;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 钻取任务状态：PLANNED 已计划 -> DRILLING 钻取中 -> COMPLETED 已完成；计划/钻取中可取消。
 */
public final class DrillTaskStatus {
    public static final String PLANNED = "PLANNED";
    public static final String DRILLING = "DRILLING";
    public static final String COMPLETED = "COMPLETED";
    public static final String CANCELLED = "CANCELLED";

    private static final Map<String, Set<String>> TRANSITIONS = new HashMap<>();
    static {
        TRANSITIONS.put(PLANNED, new HashSet<>(Arrays.asList(DRILLING, CANCELLED)));
        TRANSITIONS.put(DRILLING, new HashSet<>(Arrays.asList(COMPLETED, CANCELLED)));
        TRANSITIONS.put(COMPLETED, Collections.<String>emptySet());
        TRANSITIONS.put(CANCELLED, Collections.<String>emptySet());
    }

    private DrillTaskStatus() {
    }

    public static boolean canTransit(String from, String to) {
        Set<String> allowed = TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }
}
