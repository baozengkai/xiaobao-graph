package com.eisoo.rainbow.gremlin.process;

import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.CountGlobalStep;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.AbstractTraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tong.sha@eisoo.com
 * @version 1.0 , Copyright (c) 2014 AnyRobot, EISOO
 * @date 2018.02.28
 */

public final class RainbowCountStepStrategy extends AbstractTraversalStrategy<TraversalStrategy
        .ProviderOptimizationStrategy> implements TraversalStrategy.ProviderOptimizationStrategy {

    private static final RainbowCountStepStrategy INSTANCE = new RainbowCountStepStrategy();

    private RainbowCountStepStrategy() {
    }

    @Override
    public void apply(final Traversal.Admin<?, ?> traversal) {
        final List<Step> steps = new ArrayList<>();
        steps.addAll(TraversalHelper.getStepsOfClass(RainbowGraphStep.class, traversal));
        steps.addAll(TraversalHelper.getStepsOfClass(RainbowVertexStep.class, traversal));

        for (final Step step : steps) {
            final RainbowCountStep<?> rainbowCountStep = new RainbowCountStep<>((RainbowStep) step);
            Step nextStep = step.getNextStep();
            if (nextStep instanceof CountGlobalStep) {
                TraversalHelper.copyLabels(step, rainbowCountStep, false);
                TraversalHelper.copyLabels(nextStep, rainbowCountStep, false);
                TraversalHelper.replaceStep(step, rainbowCountStep, traversal);
                traversal.removeStep(nextStep);
            }
        }
    }

    public static RainbowCountStepStrategy instance() {
        return INSTANCE;
    }
}
