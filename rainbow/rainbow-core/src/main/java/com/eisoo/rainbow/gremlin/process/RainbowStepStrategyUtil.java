package com.eisoo.rainbow.gremlin.process;

import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.lambda.ElementValueTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.HasContainerHolder;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.*;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.*;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.EmptyStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;
import org.apache.tinkerpop.gremlin.structure.T;
import org.javatuples.Pair;

import java.util.Comparator;
import java.util.List;

/**
 * @author tongsha
 * @date 2018.02.28
 */

public class RainbowStepStrategyUtil {

    public static void collectNextSteps(final RainbowStep<?, ?> rainbowStep, final Traversal.Admin<?, ?> traversal) {
        // 策略优化顺序：Predicates -> Dedup -> Order -> Range
        collectPredicates(rainbowStep, traversal);
        collectDedup(rainbowStep, traversal);
        collectOrder(rainbowStep, traversal);
        boolean hasRangeStep = collectRange(rainbowStep, traversal);

        // 检查rainbowStep合法性
        checkRainbowStepValidation(rainbowStep, hasRangeStep);
    }

    private static void collectPredicates(final RainbowStep<?, ?> rainbowStep, final Traversal.Admin<?, ?> traversal) {
        Step nextStep = rainbowStep.getNextStep();

        while (true) {
            if (nextStep instanceof EmptyStep) {
                return;
            }

            if (nextStep instanceof HasStep) {
                for (final HasContainer hasContainer : ((HasContainerHolder) nextStep).getHasContainers()) {
                    rainbowStep.getTargetPredicatesHolder().addHasContainer(hasContainer);
                }
                TraversalHelper.copyLabels(nextStep, rainbowStep, false);
                traversal.removeStep(nextStep);
                // continue: 继续嵌套后面的过滤步骤
            } else if (nextStep instanceof NoOpBarrierStep) {
                // continue: 继续嵌套后面的过滤步骤
            } else if (nextStep instanceof TraversalFilterStep) {
                TraversalFilterStep traversalFilterStep = (TraversalFilterStep) nextStep;
                Traversal.Admin filterTraversal = (Traversal.Admin) traversalFilterStep.getLocalChildren().get(0);
                if (filterTraversal.getSteps().size() == 1 &&
                        filterTraversal.getStartStep() instanceof PropertiesStep) {
                    // 仅处理 TraversalFilterStep([PropertiesStep(...)])
                    PropertiesStep propertiesStep = (PropertiesStep) filterTraversal.getStartStep();
                    rainbowStep.getTargetPredicatesHolder().addHasContainer(
                            new HasContainer(T.key.getAccessor(), P.within(propertiesStep.getPropertyKeys())));
                    TraversalHelper.copyLabels(nextStep, rainbowStep, false);
                    traversal.removeStep(nextStep);
                    // continue: 继续嵌套后面的过滤步骤
                } else {
                    // stop: 不是TraversalFilterStep([PropertiesStep(...)])，则无需再嵌套后面的步骤了
                    break;
                }
            } else if (nextStep instanceof NotStep) {
                NotStep notStep = (NotStep) nextStep;
                Traversal.Admin notTraversal = (Traversal.Admin) notStep.getLocalChildren().get(0);
                if (notTraversal.getSteps().size() == 1 &&
                        notTraversal.getStartStep() instanceof PropertiesStep) {
                    // 仅处理 NotStep([PropertiesStep(...)])
                    PropertiesStep propertiesStep = (PropertiesStep) notTraversal.getStartStep();
                    rainbowStep.getTargetPredicatesHolder().addHasContainer(
                            new HasContainer(T.key.getAccessor(), P.neq(propertiesStep.getPropertyKeys()[0])));
                    TraversalHelper.copyLabels(nextStep, rainbowStep, false);
                    traversal.removeStep(nextStep);
                    // continue: 继续嵌套后面的过滤步骤
                } else {
                    // stop: 不是NotStep([PropertiesStep(...)])，则无需再嵌套后面的步骤了
                    break;
                }
            } else {
                // stop: 不再嵌套后面的步骤了
                break;
            }

            nextStep = nextStep.getNextStep();
        }
    }

    private static void collectDedup(final RainbowStep<?, ?> rainbowStep, final Traversal.Admin<?, ?> traversal) {
        Step nextStep = rainbowStep.getNextStep();
        if (nextStep instanceof DedupGlobalStep) {
            DedupGlobalStep dedupStep = (DedupGlobalStep) nextStep;
            List<Traversal> dedupTraversals = dedupStep.getLocalChildren();
            if (dedupTraversals.isEmpty()) {
                // 暂仅优化 dedup by RainbowElement 的场景
                rainbowStep.getTargetPredicatesHolder().setDistinct();
                TraversalHelper.copyLabels(nextStep, rainbowStep, false);
                traversal.removeStep(nextStep);
            }
        }
    }

    private static void collectOrder(final RainbowStep<?, ?> rainbowStep, final Traversal.Admin<?, ?> traversal) {
        Step nextStep = rainbowStep.getNextStep();
        if (nextStep instanceof OrderGlobalStep) {
            OrderGlobalStep orderStep = (OrderGlobalStep) nextStep;
            List<Pair<Traversal.Admin, Comparator>> comparators = orderStep.getComparators();
            comparators.stream().forEach(comparator -> {
                if (!(comparator.getValue0() instanceof ElementValueTraversal)) {
                    throw new UnsupportedOperationException("Only support order by element property now.");
                }
                ElementValueTraversal by = (ElementValueTraversal) comparator.getValue0();
                Order order = (Order) comparator.getValue1();
                rainbowStep.getTargetPredicatesHolder().addOrder(by.getPropertyKey(), order);
            });
            TraversalHelper.copyLabels(nextStep, rainbowStep, false);
            traversal.removeStep(nextStep);
        }
    }

    private static boolean collectRange(final RainbowStep<?, ?> rainbowStep, final Traversal.Admin<?, ?> traversal) {
        Step nextStep = rainbowStep.getNextStep();
        if (nextStep instanceof RangeGlobalStep) {
            RangeGlobalStep rangeGlobalStep = (RangeGlobalStep) nextStep;
            rainbowStep.getTargetPredicatesHolder().setFrom(rangeGlobalStep.getLowRange());
            rainbowStep.getTargetPredicatesHolder().setSize(
                    rangeGlobalStep.getHighRange() - rangeGlobalStep.getLowRange());
            TraversalHelper.copyLabels(nextStep, rainbowStep, false);
            traversal.removeStep(nextStep);
            return true;
        }
        return false;
    }

    private static void checkRainbowStepValidation(final RainbowStep<?, ?> rainbowStep,
                                                   final boolean hasRangeStep) {
        // check rainbow step
        if (rainbowStep.getTargetPredicatesHolder().getDistinct() == true &&
                rainbowStep.getTargetPredicatesHolder().getOrders().size() > 0) {
            throw new UnsupportedOperationException("Not support sort for dedup query.");
        }

        // check next step
        if (hasRangeStep) {
            return;
        }

        Step nextStep = rainbowStep.getNextStep();

        if (nextStep instanceof EmptyStep ||
                nextStep instanceof CountGlobalStep ||
                nextStep instanceof GroupCountStep ||
                nextStep instanceof VertexStep ||
                nextStep instanceof OrderGlobalStep ||
                nextStep instanceof PropertiesStep) {
            return;
        }

        throw new UnsupportedOperationException(String.format("%s after RainbowStep.", nextStep.toString()));
    }
}
