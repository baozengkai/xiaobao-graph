package com.eisoo.rainbow.query;

import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.step.HasContainerHolder;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.process.traversal.util.AndP;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.javatuples.Pair;

import java.util.*;

/**
 * @author tong.sha@eisoo.com
 * @version 1.0 , Copyright (c) 2014 AnyRobot, EISOO
 * @date 2018.02.28
 */

public final class GraphPredicatesHolder<E extends Element> implements HasContainerHolder {
    private Class<E> returnClass;
    private List<HasContainer> hasContainers = new ArrayList<>();
    private long from = 0L;
    /**
     *  size: -1 则由 SourceProvider 自行决定；非 -1 则最多返回该指定数量
     */
    private long size = -1L;
    private boolean distinct = false;
    private List<Pair<String, Order>> orders = new ArrayList<>();

    public Class<E> getReturnClass() {
        return this.returnClass;
    }

    public void setReturnClass(final Class<E> returnClass) {
        this.returnClass = returnClass;
    }

    @Override
    public List<HasContainer> getHasContainers() {
        return Collections.unmodifiableList(this.hasContainers);
    }

    @Override
    public void addHasContainer(final HasContainer hasContainer) {
        if (hasContainer.getPredicate() instanceof AndP) {
            for (final P<?> predicate : ((AndP<?>) hasContainer.getPredicate()).getPredicates()) {
                this.addHasContainer(new HasContainer(hasContainer.getKey(), predicate));
            }
        } else {
            this.hasContainers.add(hasContainer);
        }
    }

    public final long getFrom() {
        return this.from;
    }

    public final long getSize() {
        return this.size;
    }

    public void setFrom(final long from) {
        this.from = from;
    }

    public void setSize(final long size) {
        this.size = size;
    }

    public boolean getDistinct() {
        return this.distinct;
    }

    public void setDistinct() {
        this.distinct = true;
    }

    public void addOrder(final String propertyKey, final Order order) {
        this.orders.add(new Pair(propertyKey, order));
    }

    public List<Pair<String, Order>> getOrders() {
        return this.orders;
    }

    @Override
    public int hashCode() {
        return this.getClass().hashCode()
                ^ this.returnClass.hashCode()
                ^ this.getHasContainers().hashCode()
                ^ Long.hashCode(this.from)
                ^ Long.hashCode(this.size)
                ^ Boolean.hashCode(this.distinct)
                ^ orders.hashCode();
    }

    @Override
    public String toString() {
        List<String> strings = new ArrayList<String>();
        strings.add(this.returnClass.getSimpleName().toLowerCase());
        if (this.getHasContainers().size() > 0) {
            strings.add(this.getHasContainers().toString());
        }
        strings.add("distinct:" + this.distinct);
        strings.add("order:" + this.orders);
        strings.add("(from:" + this.from + ",size:" + this.size + ")");
        return this.getClass().getSimpleName() + "(" + String.join(", ", strings) + ")";
    }
}
