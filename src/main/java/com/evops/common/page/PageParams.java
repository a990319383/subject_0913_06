package com.evops.common.page;

import com.evops.common.BizException;

/**
 * 分页入参：pageSize 严格限定 1-100；offset 模式页码从 1 开始。
 * 同时支持 offset（page）与 keyset（cursor）两种稳定分页。
 */
public class PageParams {
    public static final int MAX_SIZE = 100;
    public static final int DEFAULT_SIZE = 20;

    private final int page;
    private final int size;
    private final String cursor;
    private final boolean keyset;

    private PageParams(int page, int size, boolean keyset, String cursor) {
        this.page = page;
        this.size = size;
        this.keyset = keyset;
        this.cursor = cursor;
    }

    /** 既有 offset 分页入口复用：校验页码与页大小 */
    public static void validate(int page, int size) {
        if (page < 1) {
            throw new BizException("页码必须从 1 开始");
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new BizException("pageSize 只能为 1-100");
        }
    }

    /**
     * 解析分页参数：
     * - 显式传 page（&gt;=1）走 offset 模式；
     * - 带 cursor，或完全不传 page，走 keyset 游标模式（首页 cursor 可为空）。
     * size 无论何种模式都限定 1-100。
     */
    public static PageParams of(Integer page, Integer size, String cursor) {
        int resolvedSize = size == null ? DEFAULT_SIZE : size;
        if (resolvedSize < 1 || resolvedSize > MAX_SIZE) {
            throw new BizException("pageSize 只能为 1-100");
        }
        String trimmedCursor = cursor == null || cursor.trim().isEmpty() ? null : cursor.trim();
        boolean keyset = trimmedCursor != null || page == null;
        if (!keyset && page < 1) {
            throw new BizException("页码必须从 1 开始");
        }
        int resolvedPage = page == null || page < 1 ? 1 : page;
        return new PageParams(resolvedPage, resolvedSize, keyset, trimmedCursor);
    }

    public boolean isCursorMode() {
        return keyset;
    }

    /** offset 模式偏移量 */
    public long offset() {
        return (long) (page - 1) * size;
    }

    /** 多取 1 条判断是否还有下一页 */
    public int fetchLimit() {
        return size + 1;
    }

    public int getPage() { return page; }
    public int getSize() { return size; }
    public String getCursor() { return cursor; }
}
