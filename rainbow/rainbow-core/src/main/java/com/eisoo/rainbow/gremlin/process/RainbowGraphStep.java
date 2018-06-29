package com.eisoo.rainbow.gremlin.process;

import com.eisoo.rainbow.query.GraphPredicatesHolder;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.GraphStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;
import com.eisoo.rainbow.gremlin.structure.RainbowGraph;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author tong.sha@eisoo.com
 * @version 1.0 , Copyright (c) 2014 AnyRobot, EISOO
 * @date 2018.02.28
 */

public final class RainbowGraphStep<S, E extends Element> extends
        GraphStep<S, E> implements RainbowStep<S, E>, Supplier<Iterator<E>> {

    private final GraphPredicatesHolder<E> targetPredicatesHolder;

    public RainbowGraphStep(final GraphStep<S, E> originalStep) {
        super(originalStep.getTraversal(),
                originalStep.getReturnClass(),
                originalStep.isStartStep(),
                originalStep.getIds());
        originalStep.getLabels().forEach(this::addLabel);
        this.targetPredicatesHolder = new GraphPredicatesHolder();
        this.targetPredicatesHolder.setReturnClass(originalStep.getReturnClass());
        if (originalStep.getIds().length > 0) {
            this.targetPredicatesHolder.addHasContainer(new HasContainer(T.id.getAccessor(),
                    P.within(originalStep.getIds())));
        }

        this.setIteratorSupplier(this);
    }

    @Override
    public Iterator<E> get() {
        final RainbowGraph graph = (RainbowGraph) this.getTraversal().getGraph().get();
        return graph.getQuery().verticesOrEdges(this.targetPredicatesHolder).iterator();
    }

    @Override
    public Collection<Object> propertyValues(final String[] propertyKeys, final long limit) {
        final RainbowGraph graph = (RainbowGraph) this.getTraversal().getGraph().get();
        return graph.getQuery().propertyValues(this.targetPredicatesHolder, propertyKeys, limit);
    }

    @Override
    public long count() {
        final RainbowGraph graph = (RainbowGraph) this.getTraversal().getGraph().get();
        return graph.getQuery().verticesOrEdgesCount(this.targetPredicatesHolder);
    }

    @Override
    public Map<Object, Long> groupCount(final Traversal.Admin keyTraversal) {
        final RainbowGraph graph = (RainbowGraph) this.getTraversal().getGraph().get();
        return graph.getQuery().verticesOrEdgesGroupCount(this.targetPredicatesHolder, keyTraversal);
    }

    @Override
    public GraphPredicatesHolder getTargetPredicatesHolder() {
        return this.targetPredicatesHolder;
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ this.targetPredicatesHolder.hashCode();
    }

    @Override
    public String toString() {
        return StringFactory.stepString(this, this.targetPredicatesHolder);
    }
}
