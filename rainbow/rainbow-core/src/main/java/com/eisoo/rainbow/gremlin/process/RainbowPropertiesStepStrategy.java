package com.eisoo.rainbow.gremlin.process;

import org.apache.tinkerpop.gremlin.process.traversal.*;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.DedupGlobalStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.RangeGlobalStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.PropertiesStep;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.AbstractTraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tong.sha@eisoo.com
 * @version 1.0 , Copyright (c) 2014 AnyRobot, EISOO
 * @date 2018.02.28
 */

public final class RainbowPropertiesStepStrategy
        extends AbstractTraversalStrategy<TraversalStrategy.ProviderOptimizationStrategy>
        implements TraversalStrategy.ProviderOptimizationStrategy {

    private static final RainbowPropertiesStepStrategy INSTANCE = new RainbowPropertiesStepStrategy();

    private RainbowPropertiesStepStrategy() {
    }

    @Override
    public void apply(final Traversal.Admin<?, ?> traversal) {
        final List<Step> steps = new ArrayList<>();
        steps.addAll(TraversalHelper.getStepsOfClass(RainbowGraphStep.class, traversal));

        for (final Step step : steps) {
            if (!(step.getNextStep() instanceof PropertiesStep)) {
                continue;
            }
            // 无需优化 g.V.limit...
            // 无需优化 g.V.dedup.limit...
            // 无需优化 g.E.limit...
            RainbowStep rainbowStep = (RainbowStep) step;
            if (rainbowStep.getTargetPredicatesHolder().getSize() >= 0) {
                continue;
            }
            // 无需优化 .values 无dedup的操作
            // 暂仅优化 g.V.values.dedup 和 g.V.values.dedup.limit
            PropertiesStep propertiesStep = (PropertiesStep) step.getNextStep();
            if (!propertiesStep.getReturnType().forValues()) {
                continue;
            }
            if (!(propertiesStep.getNextStep() instanceof DedupGlobalStep)) {
                continue;
            }

            // 不支持 g.V.dedup.values.dedup
            if (rainbowStep.getTargetPredicatesHolder().getDistinct()) {
                throw new UnsupportedOperationException("Not support operation for <g.V.dedup.values.dedup>");
            }

            DedupGlobalStep dedupStep = (DedupGlobalStep) propertiesStep.getNextStep();

            // 不优化 .dedup.by
            if (!dedupStep.getLocalChildren().isEmpty()) {
                continue;
            }

            final RainbowPropertiesStep<?, ?> newStep = new RainbowPropertiesStep(rainbowStep, propertiesStep);
            TraversalHelper.replaceStep(rainbowStep, newStep, traversal);
            traversal.removeStep(propertiesStep);
            TraversalHelper.copyLabels(dedupStep, newStep, false);
            traversal.removeStep(dedupStep);

            if (newStep.getNextStep() instanceof RangeGlobalStep) {
                RangeGlobalStep rangeStep = (RangeGlobalStep) newStep.getNextStep();
                if (rangeStep.getLowRange() != 0) {
                    throw new UnsupportedOperationException("Not support operation for <.values.dedup.range>");
                }
                newStep.setLimit(rangeStep.getHighRange());
                TraversalHelper.copyLabels(rangeStep, newStep, false);
                // 无需删除该 rangeStep，确保最终结果的正确性
            }
        }
    }

    public static RainbowPropertiesStepStrategy instance() {
        return INSTANCE;
    }
}
