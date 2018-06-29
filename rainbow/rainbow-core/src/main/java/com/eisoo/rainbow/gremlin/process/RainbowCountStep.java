package com.eisoo.rainbow.gremlin.process;

import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.AbstractStep;
import org.apache.tinkerpop.gremlin.process.traversal.util.FastNoSuchElementException;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;

import java.util.NoSuchElementException;

/**
 * @author tong.sha@eisoo.com
 * @version 1.0 , Copyright (c) 2014 AnyRobot, EISOO
 * @date 2018.02.28
 */

public final class RainbowCountStep<S extends Element> extends AbstractStep<S, Long> {

    private RainbowStep rainbowStep = null;
    private boolean done = false;

    public RainbowCountStep(final RainbowStep rainbowStep) {
        super(rainbowStep.getTraversal());
        this.rainbowStep = rainbowStep;
    }

    @Override
    protected Traverser.Admin<Long> processNextStart() throws NoSuchElementException {
        if (!this.done) {
            this.done = true;
            return this.getTraversal().getTraverserGenerator().generate(this.rainbowStep.count(), (Step) this, 1L);
        }

        throw FastNoSuchElementException.instance();
    }

    @Override
    public String toString() {
        return StringFactory.stepString(this, this.rainbowStep);
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ this.rainbowStep.hashCode();
    }

    @Override
    public void reset() {
        this.done = false;
    }
}
