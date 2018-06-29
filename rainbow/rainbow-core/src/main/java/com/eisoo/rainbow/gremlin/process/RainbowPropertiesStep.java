package com.eisoo.rainbow.gremlin.process;

import com.eisoo.rainbow.gremlin.structure.RainbowGraph;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.PropertiesStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.AbstractStep;
import org.apache.tinkerpop.gremlin.process.traversal.util.FastNoSuchElementException;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.util.CloseableIterator;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;
import org.apache.tinkerpop.gremlin.util.iterator.EmptyIterator;

import java.util.*;
import java.util.function.Supplier;

/**
 * @author tong.sha@eisoo.com
 * @version 1.0 , Copyright (c) 2014 AnyRobot, EISOO
 * @date 2018.02.28
 */

public final class RainbowPropertiesStep<S extends Element, E> extends AbstractStep<S, E>
        implements AutoCloseable, Supplier<Iterator<E>> {

    private RainbowStep rainbowStep;
    private String[] propertyKeys;
    /**
     *  size: -1 则由 SourceProvider 自行决定；非 -1 则最多返回该指定数量
     */
    private long limit = -1L;

    protected boolean isStart = true;
    protected boolean done = false;
    private Traverser.Admin<S> head = null;
    private Iterator<E> iterator = EmptyIterator.instance();

    protected transient Supplier<Iterator<E>> iteratorSupplier;

    public RainbowPropertiesStep(final RainbowStep<S, E> rainbowStep, final PropertiesStep<E> propertiesStep) {
        super(rainbowStep.getTraversal());
        TraversalHelper.copyLabels(rainbowStep, this, false);
        this.rainbowStep = rainbowStep;

        TraversalHelper.copyLabels(propertiesStep, this, false);
        this.propertyKeys = propertiesStep.getPropertyKeys();

        this.setIteratorSupplier(this);
    }

    public RainbowStep getRainbowStep() {
        return this.rainbowStep;
    }

    public void setLimit(final long limit) {
        this.limit = limit;
    }

    public void setIteratorSupplier(final Supplier<Iterator<E>> iteratorSupplier) {
        this.iteratorSupplier = iteratorSupplier;
    }

    @Override
    public Iterator<E> get() {
        final RainbowGraph graph = (RainbowGraph) this.getTraversal().getGraph().get();
        return this.rainbowStep.propertyValues(this.propertyKeys, this.limit).iterator();
    }

    @Override
    protected Traverser.Admin<E> processNextStart() {
        while (true) {
            if (this.iterator.hasNext()) {
                return this.isStart ?
                        this.getTraversal().getTraverserGenerator().generate(this.iterator.next(), (Step) this, 1L) :
                        this.head.split(this.iterator.next(), this);
            } else {
                if (this.isStart) {
                    if (this.done) {
                        throw FastNoSuchElementException.instance();
                    } else {
                        this.done = true;
                        this.iterator = null == this.iteratorSupplier ? EmptyIterator.instance() :
                                this.iteratorSupplier.get();
                    }
                } else {
                    this.head = this.starts.next();
                    this.iterator = null == this.iteratorSupplier ? EmptyIterator.instance() :
                            this.iteratorSupplier.get();
                }
            }
        }
    }

    @Override
    public void reset() {
        super.reset();
        this.head = null;
        this.done = false;
        this.iterator = EmptyIterator.instance();
    }

    @Override
    public void close() {
        CloseableIterator.closeIterator(iterator);
    }

    @Override
    public String toString() {
        return StringFactory.stepString(this, this.rainbowStep, "values:" + this.propertyKeys.toString());
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ this.propertyKeys.hashCode() ^ this.rainbowStep.hashCode();
    }
}
