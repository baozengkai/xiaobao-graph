package com.eisoo.rainbow.gremlin.process;

import com.eisoo.rainbow.gremlin.structure.RainbowElement;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.lambda.ElementValueTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.lambda.TokenTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.GroupCountStep;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.AbstractTraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tong.sha@eisoo.com
 * @version 1.0 , Copyright (c) 2014 AnyRobot, EISOO
 * @date 2018.02.28
 */

public final class RainbowGroupCountStepStrategy
        extends AbstractTraversalStrategy<TraversalStrategy.ProviderOptimizationStrategy>
        implements TraversalStrategy.ProviderOptimizationStrategy {

    private static final RainbowGroupCountStepStrategy INSTANCE = new RainbowGroupCountStepStrategy();

    private RainbowGroupCountStepStrategy() {
    }

    @Override
    public void apply(final Traversal.Admin<?, ?> traversal) {
        final List<Step> steps = new ArrayList<>();
        steps.addAll(TraversalHelper.getStepsOfClass(RainbowGraphStep.class, traversal));
        steps.addAll(TraversalHelper.getStepsOfClass(RainbowVertexStep.class, traversal));

        for (final Step step : steps) {
            Step nextStep = step.getNextStep();
            if (nextStep instanceof GroupCountStep) {
                final GroupCountStep groupCountStep = (GroupCountStep) nextStep;
                Traversal.Admin keyTraversal = null;

                List<Traversal.Admin<?, ?>> subTraversals = groupCountStep.getLocalChildren();
                if (subTraversals.size() == 1) {
                    Traversal.Admin keyTraversalTemp = subTraversals.get(0);
                    if (keyTraversalTemp instanceof TokenTraversal) {
                        if (((TokenTraversal) keyTraversalTemp).getToken().equals(T.label)) {
                            keyTraversal = keyTraversalTemp;
                        }
                    } else if (keyTraversalTemp instanceof ElementValueTraversal) {
                        keyTraversal = keyTraversalTemp;
                    }
                }

                if (null == keyTraversal)
                    throw new UnsupportedOperationException(groupCountStep.toString());

                final RainbowGroupCountStep rainbowGroupCountStep =
                        new RainbowGroupCountStep<RainbowElement, Object>((RainbowStep) step, keyTraversal);

                TraversalHelper.copyLabels(groupCountStep, rainbowGroupCountStep, false);
                TraversalHelper.replaceStep(step, rainbowGroupCountStep, traversal);
                traversal.removeStep(nextStep);
            }
        }
    }

    public static RainbowGroupCountStepStrategy instance() {
        return INSTANCE;
    }
}
