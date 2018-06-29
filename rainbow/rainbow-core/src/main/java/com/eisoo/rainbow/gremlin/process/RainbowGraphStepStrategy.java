package com.eisoo.rainbow.gremlin.process;

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.GraphStep;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.AbstractTraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author tong.sha@eisoo.com
 * @version 1.0 , Copyright (c) 2014 AnyRobot, EISOO
 * @date 2018.02.28
 */

public final class RainbowGraphStepStrategy extends AbstractTraversalStrategy<TraversalStrategy
        .ProviderOptimizationStrategy>
        implements TraversalStrategy.ProviderOptimizationStrategy {

    private static final RainbowGraphStepStrategy INSTANCE = new RainbowGraphStepStrategy();

    private RainbowGraphStepStrategy() {
    }

    @Override
    public void apply(final Traversal.Admin<?, ?> traversal) {
        for (final GraphStep originalGraphStep : TraversalHelper.getStepsOfClass(GraphStep.class, traversal)) {
            final RainbowGraphStep<?, ?> rainbowGraphStep = new RainbowGraphStep<>(originalGraphStep);
            TraversalHelper.replaceStep(originalGraphStep, rainbowGraphStep, traversal);
            RainbowStepStrategyUtil.collectNextSteps(rainbowGraphStep, traversal);
        }
    }

    @Override
    public Set<Class<? extends ProviderOptimizationStrategy>> applyPost() {
        return Arrays.asList(
                RainbowCountStepStrategy.class,
                RainbowGroupCountStepStrategy.class,
                RainbowPropertiesStepStrategy.class
        ).stream().collect(Collectors.toSet());
    }

    public static RainbowGraphStepStrategy instance() {
        return INSTANCE;
    }

}
