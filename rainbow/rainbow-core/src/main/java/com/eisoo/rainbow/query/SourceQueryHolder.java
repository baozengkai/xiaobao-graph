package com.eisoo.rainbow.query;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.util.AndP;

import java.util.*;

/**
 * @author tong.sha@eisoo.com
 * @version 1.0 , Copyright (c) 2014 AnyRobot, EISOO
 * @date 2018.02.28
 */

public final class SourceQueryHolder {

    /**
     * 定义两种类型的枚举变量
     */
    public enum TYPE {
        And,
        Or
    }

    private final TYPE type;
    private final Map<String, SourceQueryClause> queryClauses = new HashMap<>();
    private final Map<String, SourceQueryHolder> children = new HashMap<>();

    public SourceQueryHolder(final SourceQueryHolder.TYPE type) {
        this.type = type;
    }

    public SourceQueryHolder.TYPE getType() {
        return this.type;
    }

    public Collection<SourceQueryClause> getQueryClauses() {
        return Collections.unmodifiableCollection(this.queryClauses.values());
    }

    public void addQueryClause(final SourceQueryClause clause) {
        if (clause.getPredicate() instanceof AndP) {
            for (final P<?> predicate : ((AndP<?>) clause.getPredicate()).getPredicates()) {
                this.addQueryClause(new SourceQueryClause(clause.getType(), clause.getField(), predicate));
            }
        } else {
            this.queryClauses.put(clause.toString(), clause);
        }
    }

    public Collection<SourceQueryHolder> getChildren() {
        return Collections.unmodifiableCollection(this.children.values());
    }

    public void addChild(final SourceQueryHolder holder) {
        this.children.put(holder.toString(), holder);
    }

    @Override
    public int hashCode() {
        return (this.type.hashCode() ^ this.queryClauses.hashCode() ^ this.children.hashCode());
    }

    @Override
    public String toString() {
        List<String> strings = new ArrayList<String>();
        strings.add(this.type.name());
        if (!this.queryClauses.isEmpty()) strings.add(this.queryClauses.values().toString());
        if (!this.children.isEmpty()) strings.add(this.children.values().toString());
        return this.getClass().getSimpleName() + "(" + String.join(", ", strings) + ")";
    }
}
