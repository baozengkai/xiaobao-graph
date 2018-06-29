package com.eisoo.rainbow.gremlin.process;

import com.eisoo.rainbow.query.GraphPredicatesHolder;
import com.eisoo.rainbow.query.GraphVertexToHolder;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.GraphStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.VertexStep;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.TraverserRequirement;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;
import com.eisoo.rainbow.gremlin.structure.RainbowGraph;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;
import java.util.function.Supplier;

/**
 * @author tong.sha@eisoo.com
 * @version 1.0 , Copyright (c) 2014 AnyRobot, EISOO
 * @date 2018.02.28
 */

public final class RainbowVertexStep<E extends Element> extends GraphStep<Vertex, E>
        implements RainbowStep<Vertex, E>, Supplier<Iterator<E>> {

    private GraphVertexToHolder graphVertexToHolder;
    private final GraphPredicatesHolder targetPredicatesHolder;

    public RainbowVertexStep(final VertexStep<E> originalStep) {
        super(originalStep.getTraversal(), originalStep.getReturnClass(), true, new Object[0]);
        originalStep.getLabels().forEach(this::addLabel);
        this.graphVertexToHolder = new GraphVertexToHolder(
                originalStep.getDirection(), originalStep.getEdgeLabels());
        this.targetPredicatesHolder = new GraphPredicatesHolder();
        this.targetPredicatesHolder.setReturnClass(originalStep.getReturnClass());
        this.setIteratorSupplier(this);
    }

    private void collectVertices() {
        this.graphVertexToHolder.getVertices().clear();
        while (this.starts.hasNext()) {
            this.graphVertexToHolder.addVertex(this.starts.next().get());
        }
    }

    @Override
    public Iterator<E> get() {
        this.collectVertices();
        final RainbowGraph graph = (RainbowGraph) this.getTraversal().getGraph().get();
        return graph.getQuery().vertexToVerticesOrEdges(
                this.graphVertexToHolder, this.targetPredicatesHolder).iterator();
    }

    @Override
    public Collection<Object> propertyValues(final String[] propertyKeys, final long limit) {
        throw new NotImplementedException();
    }

    @Override
    public long count() {
        this.collectVertices();
        final RainbowGraph graph = (RainbowGraph) this.getTraversal().getGraph().get();
        long count = graph.getQuery().vertexToVerticesOrEdgesCount(
                this.graphVertexToHolder, this.targetPredicatesHolder);
        return count;
    }

    @Override
    public Map<Object, Long> groupCount(final Traversal.Admin keyTraversal) {
        throw new NotImplementedException();
    }

    @Override
    public Set<TraverserRequirement> getRequirements() {
        return Collections.singleton(TraverserRequirement.OBJECT);
    }

    @Override
    public GraphPredicatesHolder getTargetPredicatesHolder() {
        return this.targetPredicatesHolder;
    }

    @Override
    public int hashCode() {
        return this.graphVertexToHolder.getDirection().hashCode()
                ^ this.graphVertexToHolder.getEdgeLabels().hashCode()
                ^ this.targetPredicatesHolder.hashCode();
    }

    @Override
    public String toString() {
        return StringFactory.stepString(this,
                this.graphVertexToHolder.getDirection(),
                Arrays.asList(this.graphVertexToHolder.getEdgeLabels()),
                this.targetPredicatesHolder);
    }
}
