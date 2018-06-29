package com.eisoo.rainbow.gremlin.process;

import org.apache.tinkerpop.gremlin.process.traversal.*;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.VertexStep;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.AbstractTraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;

import java.util.Collections;
import java.util.Set;

/**
 * @author tong.sha@eisoo.com
 * @version 1.0 , Copyright (c) 2014 AnyRobot, EISOO
 * @date 2018.02.28
 */

public final class RainbowVertexStepStrategy extends AbstractTraversalStrategy<TraversalStrategy
        .ProviderOptimizationStrategy>
        implements TraversalStrategy.ProviderOptimizationStrategy {

    private static final RainbowVertexStepStrategy INSTANCE = new RainbowVertexStepStrategy();

    private RainbowVertexStepStrategy() {
    }

    @Override
    public void apply(final Traversal.Admin<?, ?> traversal) {
        for (final VertexStep originalVertexStep : TraversalHelper.getStepsOfClass(VertexStep.class, traversal)) {
            final RainbowVertexStep<?> rainbowVertexStep = new RainbowVertexStep<>(originalVertexStep);
            TraversalHelper.replaceStep(originalVertexStep, rainbowVertexStep, traversal);
            RainbowStepStrategyUtil.collectNextSteps(rainbowVertexStep, traversal);

            if (rainbowVertexStep.getTargetPredicatesHolder().getOrders().size() > 0) {
                throw new UnsupportedOperationException("RainbowVertexStep does not support order-by now.");
            }
        }
    }

    @Override
    public Set<Class<? extends ProviderOptimizationStrategy>> applyPrior() {
        return Collections.singleton(RainbowGraphStepStrategy.class);
    }

    @Override
    public Set<Class<? extends ProviderOptimizationStrategy>> applyPost() {
        return Collections.singleton(RainbowCountStepStrategy.class);
    }

    public static RainbowVertexStepStrategy instance() {
        return INSTANCE;
    }
}
