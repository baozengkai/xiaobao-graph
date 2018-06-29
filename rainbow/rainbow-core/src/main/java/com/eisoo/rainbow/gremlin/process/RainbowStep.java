package com.eisoo.rainbow.gremlin.process;

import com.eisoo.rainbow.query.GraphPredicatesHolder;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;

import java.util.Collection;
import java.util.Map;

/**
 * @author tong.sha@eisoo.com
 * @version 1.0 , Copyright (c) 2014 AnyRobot, EISOO
 * @date 2018.02.28
 */

public interface RainbowStep<S, E> extends Step<S, E> {
    GraphPredicatesHolder getTargetPredicatesHolder();
    Collection<Object> propertyValues(final String[] propertyKeys, final long limit);
    long count();
    Map<Object, Long> groupCount(final Traversal.Admin keyTraversal);
}
