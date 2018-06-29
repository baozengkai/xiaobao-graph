package com.eisoo.rainbow.query;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import java.util.function.BiPredicate;

/**
 * @author tong.sha@eisoo.com
 * @version 1.0 , Copyright (c) 2014 AnyRobot, EISOO
 * @date 2018.02.28
 */
public class SourceQueryClause {

    /**
     * 定义两种类型的枚举变量
     */
    public enum TYPE {
        Query,
        Filter
    }

    private final TYPE type;
    private final String field;
    private final P predicate;

    public SourceQueryClause(final SourceQueryClause.TYPE type, final String field, final P<?> predicate) {
        this.type = type;
        this.field = field;
        this.predicate = predicate;
    }

    public SourceQueryClause.TYPE getType() {
        return this.type;
    }

    public final String getField() {
        return this.field;
    }

    public final P<?> getPredicate() {
        return this.predicate;
    }

    public final BiPredicate<?, ?> getBiPredicate() {
        return this.predicate.getBiPredicate();
    }

    public final Object getValue() {
        return this.predicate.getValue();
    }

    @Override
    public final String toString() {
        return this.type.name() + '(' + this.field + '.' + this.predicate.toString() + ')';
    }

    @Override
    public int hashCode() {
        return (this.type.hashCode() ^ this.field.hashCode() ^ this.predicate.hashCode());
    }
}
