package com.evops.constant;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 分析批次状态：CREATED 已建立 -> TESTING 检测中 -> ACCEPTED 已验收 -> POSTED 已落账；
 * 建立/检测中可取消。已验收、已落账的批次受删除保护。
 */
public final class AnalysisBatchStatus {
    public static final String CREATED = "CREATED";
    public static final String TESTING = "TESTING";
    public static final String ACCEPTED = "ACCEPTED";
    public static final String POSTED = "POSTED";
    public static final String CANCELLED = "CANCELLED";

    private static final Map<String, Set<String>> TRANSITIONS = new HashMap<>();
    static {
        TRANSITIONS.put(CREATED, new HashSet<>(Arrays.asList(TESTING, CANCELLED)));
        TRANSITIONS.put(TESTING, new HashSet<>(Arrays.asList(ACCEPTED, CANCELLED)));
        TRANSITIONS.put(ACCEPTED, new HashSet<>(Collections.singletonList(POSTED)));
        TRANSITIONS.put(POSTED, Collections.<String>emptySet());
        TRANSITIONS.put(CANCELLED, Collections.<String>emptySet());
    }

    private AnalysisBatchStatus() {
    }

    public static boolean canTransit(String from, String to) {
        Set<String> allowed = TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }
}
