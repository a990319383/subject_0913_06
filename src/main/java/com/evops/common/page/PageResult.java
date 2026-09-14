package com.evops.common.page;

import java.util.List;

/**
 * 统一分页结果：当前页记录、总数、页大小、是否有下一页、下一页游标（仅游标模式）。
 * 记录数恒等于主表行数——关联表只走 EXISTS 半连接，不会放大主记录。
 */
public class PageResult<T> {
    private List<T> records;
    private long total;
    private int pageSize;
    private long page;
    private boolean hasMore;
    private String nextCursor;

    public PageResult() {
    }

    public PageResult(List<T> records, long total, int pageSize, long page, boolean hasMore, String nextCursor) {
        this.records = records;
        this.total = total;
        this.pageSize = pageSize;
        this.page = page;
        this.hasMore = hasMore;
        this.nextCursor = nextCursor;
    }

    public List<T> getRecords() { return records; }
    public void setRecords(List<T> records) { this.records = records; }
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    public long getPage() { return page; }
    public void setPage(long page) { this.page = page; }
    public boolean isHasMore() { return hasMore; }
    public void setHasMore(boolean hasMore) { this.hasMore = hasMore; }
    public String getNextCursor() { return nextCursor; }
    public void setNextCursor(String nextCursor) { this.nextCursor = nextCursor; }
}
