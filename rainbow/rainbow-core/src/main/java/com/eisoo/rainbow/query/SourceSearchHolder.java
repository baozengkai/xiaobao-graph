package com.eisoo.rainbow.query;

import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.javatuples.Pair;

import java.util.*;

/**
 * @author tong.sha@eisoo.com
 * @version 1.0 , Copyright (c) 2014 AnyRobot, EISOO
 * @date 2018.02.28
 */

public final class SourceSearchHolder {

    /**
     * 查询条件
     */
    private SourceQueryHolder queryHolder = null;

    /**
     * 去重字段
     */
    private String distinctField = null;

    /**
     * 排序 by <field, order>
     */
    private List<Pair<String, Order>> orders = new ArrayList<>();

    /**
     * 返回结果数量：-1 则由 SourceProvider 自行决定；非 -1 则最多返回该指定数量
     */
    private long size = -1L;
    private long from = 0L;

    public final SourceQueryHolder getQueryHolder() {
        return this.queryHolder;
    }

    public void setQueryHolder(final SourceQueryHolder queryHolder) {
        this.queryHolder = queryHolder;
    }

    public void setDistinctField(final String field) {
        this.distinctField = field;
    }

    public String getDistinctField() {
        return this.distinctField;
    }

    public boolean hasDistinctField() {
        return null != this.distinctField;
    }

    public void addOrder(final String field, final Order order) {
        this.orders.add(new Pair(field, order));
    }

    public List<Pair<String, Order>> getOrders() {
        return this.orders;
    }

    public final long getSize() {
        return this.size;
    }

    public void setSize(final long size) {
        this.size = size;
    }

    public long getFrom() {
        return this.from;
    }

    public void setFrom(final long from) {
        this.from = from;
    }

    @Override
    public String toString() {
        final List<String> strings = new ArrayList<>();
        if (this.queryHolder != null) {
            strings.add(this.queryHolder.toString());
        }
        if (this.distinctField != null) {
            strings.add("distinctField:" + this.distinctField);
        }
        if (this.orders.size() > 0) {
            strings.add("order:" + this.orders);
        }
        strings.add("from(" + this.from + ")");
        strings.add("size(" + this.size + ")");
        return this.getClass().getSimpleName() + "(" + String.join(", ", strings) + ")";
    }

}
