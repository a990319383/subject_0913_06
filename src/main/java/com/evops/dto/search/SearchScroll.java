package com.evops.dto.search;

/**
 * 检索滚动参数基类：同时支持 offset（page/size）与 keyset（cursor）两种稳定分页。
 * cursorMode / lastXxx / offset / limit 由服务层根据入参和游标解析后填充，供 Mapper 使用。
 */
public class SearchScroll {
    /** 入参：页码，从 1 开始（offset 模式） */
    private Integer page;
    /** 入参：页大小，1-100 */
    private Integer size;
    /** 入参：不透明游标，存在时优先走 keyset 模式 */
    private String cursor;
    /** 入参：排序字段，由各检索限定白名单 */
    private String sortField;
    /** 入参：ASC / DESC */
    private String sortOrder;

    /** 运行期：是否游标模式 */
    private boolean cursorMode;
    /** 运行期：确定性主键翻页锚点 */
    private Long lastId;
    /** 运行期：offset 模式偏移量 */
    private long offset;
    /** 运行期：SQL LIMIT（游标模式为 size+1，用于判断 hasMore） */
    private int limit;

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }
    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }
    public String getCursor() { return cursor; }
    public void setCursor(String cursor) { this.cursor = cursor; }
    public String getSortField() { return sortField; }
    public void setSortField(String sortField) { this.sortField = sortField; }
    public String getSortOrder() { return sortOrder; }
    public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }
    public boolean isCursorMode() { return cursorMode; }
    public void setCursorMode(boolean cursorMode) { this.cursorMode = cursorMode; }
    public Long getLastId() { return lastId; }
    public void setLastId(Long lastId) { this.lastId = lastId; }
    public long getOffset() { return offset; }
    public void setOffset(long offset) { this.offset = offset; }
    public int getLimit() { return limit; }
    public void setLimit(int limit) { this.limit = limit; }
}
