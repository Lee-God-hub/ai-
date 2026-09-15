package com.smartproperty.dto;

import java.util.List;

public class PageResult<T> {

    private Long total;
    private List<T> records;

    public PageResult() {}

    public PageResult(Long total, List<T> records) {
        this.total = total;
        this.records = records;
    }

    public Long getTotal() { return total; }
    public void setTotal(Long total) { this.total = total; }

    public List<T> getRecords() { return records; }
    public void setRecords(List<T> records) { this.records = records; }
}
