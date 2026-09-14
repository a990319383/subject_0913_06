package com.evops.common.page;

import java.util.Base64;
import java.util.Collections;
import java.util.List;

/**
 * 复合键集游标编解码：字段按排序顺序做 URL 编码后以 '' 连接，再做 URL-safe Base64。
 * 不暴露内部主键含义，且对包含任意字符（含分隔符）的排序值都安全。
 */
public final class Cursors {
    private static final String SEP = "";

    private Cursors() {
    }

    public static String encode(List<String> fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) {
                sb.append(SEP);
            }
            sb.append(urlEncode(fields.get(i)));
        }
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    public static List<String> decode(String cursor, int expected) {
        if (cursor == null) {
            return Collections.emptyList();
        }
        byte[] raw;
        try {
            raw = Base64.getUrlDecoder().decode(cursor);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("分页游标无效");
        }
        String[] parts = new String(raw, java.nio.charset.StandardCharsets.UTF_8).split(SEP, -1);
        if (parts.length != expected) {
            throw new IllegalArgumentException("分页游标无效");
        }
        java.util.List<String> values = new java.util.ArrayList<>(expected);
        for (String part : parts) {
            values.add(urlDecode(part));
        }
        return values;
    }

    private static final String UTF_8 = "UTF-8";

    private static String urlEncode(String value) {
        if (value == null) {
            return "";
        }
        try {
            return java.net.URLEncoder.encode(value, UTF_8);
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String urlDecode(String value) {
        try {
            return java.net.URLDecoder.decode(value, UTF_8);
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
