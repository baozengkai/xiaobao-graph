package com.eisoo.rainbow.gremlin.process;

import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.AbstractStep;
import org.apache.tinkerpop.gremlin.process.traversal.util.FastNoSuchElementException;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @author tong.sha@eisoo.com
 * @version 1.0 , Copyright (c) 2014 AnyRobot, EISOO
 * @date 2018.02.28
 */

public final class RainbowGroupCountStep<S extends Element, E> extends AbstractStep<S, Map<E, Long>> {

    private RainbowStep rainbowStep;
    private Traversal.Admin<S, E> keyTraversal = null;
    private boolean done = false;

    public RainbowGroupCountStep(final RainbowStep rainbowStep, final Traversal.Admin<S, E> keyTraversal) {
        super(rainbowStep.getTraversal());
        this.rainbowStep = rainbowStep;
        TraversalHelper.copyLabels(rainbowStep, this, false);
        this.keyTraversal = keyTraversal;
    }

    @Override
    protected Traverser.Admin<Map<E, Long>> processNextStart() throws NoSuchElementException {
        if (!this.done) {
            this.done = true;
            return this.getTraversal().getTraverserGenerator().generate(
                    this.rainbowStep.groupCount(this.keyTraversal), (Step) this, 1L);
        }

        throw FastNoSuchElementException.instance();
    }

    @Override
    public String toString() {
        return StringFactory.stepString(this, this.rainbowStep, "by:" + this.keyTraversal.toString());
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ this.keyTraversal.hashCode() ^ this.rainbowStep.hashCode();
    }

    @Override
    public void reset() {
        this.done = false;
    }
}
