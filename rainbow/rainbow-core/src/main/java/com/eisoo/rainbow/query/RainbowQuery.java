package com.eisoo.rainbow.query;

import com.eisoo.rainbow.gremlin.structure.RainbowElement;
import com.jcabi.aspects.Loggable;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.lambda.ElementValueTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.lambda.TokenTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.*;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author tong.sha@eisoo.com
 * @version 1.0 , Copyright (c) 2014 AnyRobot, EISOO
 * @date 2018.02.28
 */

public final class RainbowQuery<E extends Element> {
    private SourceProvider sourceProvider;
    private SchemaTranslator translator;
    /**
     * 一次vertex to查询最多支持指定 n 个vertices
     */
    private int vertexToLimit = 10000;

    public RainbowQuery(final SourceProvider sourceProvider, final SchemaTranslator translator) {
        this.sourceProvider = sourceProvider;
        this.translator = translator;
    }

    /**
     * 调用SchemaTranslaotr类的localFilterElements实现过滤数据
     * @param predicatesHolder
     */
    @Loggable(logThis = true, trim = false)
    public Collection<E> verticesOrEdges(final GraphPredicatesHolder predicatesHolder) {

        // 调用本类的verticesOrEdgesImpl实现所有数据的查询 以及原始数据到RainbowElement结构的映射
        // 在这一步加入，对predicateHolder的验证，如果类如GraphPredicatesHolder(vertex,
        // [~value.eq(张一)], distinct:false, order:[], (from:0,size:-1))
        // 判断其为不支持的方法，然后不支持该方法
        List<RainbowElement> allElements = new ArrayList<>();
        if (checkPredicatesHolderSupportMethod(predicatesHolder)) {
            allElements = this.verticesOrEdgesImpl(predicatesHolder);
        }
        return (Collection<E>) this.translator.localFilterElements(allElements, predicatesHolder);
    }

    /**
     * 鲍增凯:新增检查rainbow是否支持PredicatesHolder包含的方法
     * @param predicatesHolder
     */
    public boolean checkPredicatesHolderSupportMethod(final GraphPredicatesHolder predicatesHolder)
    {
        List<HasContainer> hasContainers = new ArrayList<>();
        hasContainers = predicatesHolder.getHasContainers();
        if (!hasContainers.isEmpty())
        {
            if (hasContainers.get(0).getKey().equals("~value"))
            {
                throw new UnsupportedOperationException("rainbow don't support the hasValue method of gremlin");
            }
        }
        return true;
    }

    public Collection<Object> propertyValues(final GraphPredicatesHolder<E> targetPredicatesHolder,
                                             final String[] propertyKeys,
                                             final long limit) {
        return this.propertyValuesImpl(targetPredicatesHolder, propertyKeys, limit);
    }

    @Loggable(logThis = true, trim = false)
    public long verticesOrEdgesCount(final GraphPredicatesHolder predicatesHolder) {
        return this.verticesOrEdgesCountImpl(predicatesHolder);
    }

    @Loggable(logThis = true, trim = false)
    public Map<Object, Long> verticesOrEdgesGroupCount(GraphPredicatesHolder<E> targetPredicatesHolder,
                                                       Traversal.Admin keyTraversal) {
        if (keyTraversal instanceof TokenTraversal) {
            if (((TokenTraversal) keyTraversal).getToken().equals(T.label)) {
                return this.verticesOrEdgesGroupCountByLabelImpl(targetPredicatesHolder);
            }
        } else if (keyTraversal instanceof ElementValueTraversal) {
            long totalCount = this.verticesOrEdgesGroupCountByLabelImpl(targetPredicatesHolder).values().stream()
                    .mapToLong(Long::longValue).sum();
            Map<Object, Long> groupCountMap = this.verticesOrEdgesGroupCountByPropertyKeyImpl(
                    targetPredicatesHolder, ((ElementValueTraversal) keyTraversal).getPropertyKey());

            String othersCountKey = "__.others";
            if (groupCountMap.keySet().contains(othersCountKey)) {
                groupCountMap.remove(othersCountKey);
            }
            long groupCount = groupCountMap.values().stream().mapToLong(Long::longValue).sum();
            long othersCount = totalCount - groupCount;
            if (othersCount > 0) {
                groupCountMap.put(othersCountKey, othersCount);
            }

            return groupCountMap;
        }

        throw new IllegalArgumentException(String.format("Group count by %s.", keyTraversal.toString()));
    }

    @Loggable(logThis = true, trim = true)
    public Collection<E> vertexToVerticesOrEdges(final GraphVertexToHolder graphVertexToHolder,
                                                           final GraphPredicatesHolder targetPredicatesHolder) {
        this.checkVertexToValidation(graphVertexToHolder);
        List<RainbowElement> allElements = this.vertexToVerticesOrEdgesImpl(graphVertexToHolder,
                targetPredicatesHolder);
        return (Collection<E>) this.translator.localFilterElements(allElements, targetPredicatesHolder);
    }

    @Loggable(logThis = true, trim = true)
    public long vertexToVerticesOrEdgesCount(final GraphVertexToHolder graphVertexToHolder,
                                             final GraphPredicatesHolder targetPredicatesHolder) {
        this.checkVertexToValidation(graphVertexToHolder);
        return this.vertexToVerticesOrEdgesCountImpl(graphVertexToHolder, targetPredicatesHolder);
    }

    private void checkVertexToValidation(final GraphVertexToHolder graphVertexToHolder) {
        int verticesNum = graphVertexToHolder.getVertices().size();
        if (verticesNum > this.vertexToLimit) {
            throw new RuntimeException(String.format(
                    "Vertices number is too large, vertices number for 'vertex to' query must be " +
                            "less than or equal to [%d] but was [%d].",
                    this.vertexToLimit, verticesNum));
        }
    }

    @Loggable(logThis = true, trim = false)
    private List<RainbowElement> verticesOrEdgesImpl(final GraphPredicatesHolder predicatesHolder) {
        List<RainbowElement> allElements = new ArrayList<>();
        Collection<JSONObject> elementSchemas = this.translator.filterElementSchemas(predicatesHolder);

        for (JSONObject elementSchema : elementSchemas) {
            SourceSearchHolder searchHolder = this.translator.toSourceSearchHolder(elementSchema, predicatesHolder);
            List<Map<String, Object>> dataSet = this.sourceProvider.search(searchHolder);
            List<RainbowElement> elements = dataSet.stream()
                    .map(dataRow -> this.translator.toRainbowElement(dataRow, elementSchema))
                    .filter(element -> element != null)
                    .collect(Collectors.toList());
            allElements.addAll(elements);
        }

        return allElements;
    }

    @Loggable(logThis = true, trim = false)
    public Collection<Object> propertyValuesImpl(final GraphPredicatesHolder<E> predicatesHolder,
                                                 final String[] propertyKeys,
                                                 final long limit) {
        Set<Object> allValues = new HashSet<>();
        Collection<JSONObject> elementSchemas = this.translator.filterElementSchemas(predicatesHolder);

        for (JSONObject elementSchema : elementSchemas) {
            for (String propertyKey: propertyKeys) {
                if (SchemaHelper.isConstProperty(propertyKey)) {
                    allValues.add(SchemaHelper.getConstPropertyValue(elementSchema, propertyKey));
                    continue;
                }

                SourceSearchHolder searchHolder = this.translator.toSourceSearchHolder(
                        elementSchema, predicatesHolder, propertyKey, limit);
                List<Map<String, Object>> dataSet = this.sourceProvider.search(searchHolder);
                dataSet.stream()
                        .map(dataRow -> dataRow.get(SchemaHelper.getPropertyField(elementSchema, propertyKey)))
                        .forEach(allValues::add);
            }
        }

        return allValues;
    }

    @Loggable(logThis = true, trim = false)
    private long verticesOrEdgesCountImpl(final GraphPredicatesHolder predicatesHolder) {
        long totalCount = 0L;
        Collection<JSONObject> elementSchemas = this.translator.filterElementSchemas(predicatesHolder);
        if (predicatesHolder.getDistinct() == true && elementSchemas.size() > 1) {
            throw new UnsupportedOperationException("Count after dedup for multi schemas.");
        }

        for (JSONObject elementSchema : elementSchemas) {
            SourceSearchHolder searchHolder = this.translator.toSourceSearchHolder(elementSchema, predicatesHolder);
            long count = this.sourceProvider.searchCount(searchHolder);
            totalCount += count;
        }

        return totalCount;
    }

    @Loggable(logThis = true, trim = false)
    private Map<Object, Long> verticesOrEdgesGroupCountByLabelImpl(final GraphPredicatesHolder predicatesHolder) {
        Map<Object, Long> groupCount = new HashMap<>();

        Collection<JSONObject> elementSchemas = this.translator.filterElementSchemas(predicatesHolder);
        if (predicatesHolder.getDistinct() == true && elementSchemas.size() > 1) {
            throw new UnsupportedOperationException("Count after dedup for multi schemas.");
        }

        for (JSONObject elementSchema : elementSchemas) {
            SourceSearchHolder searchHolder = this.translator.toSourceSearchHolder(elementSchema, predicatesHolder);
            long count = this.sourceProvider.searchCount(searchHolder);
            if (count > 0) {
                groupCount.put(SchemaHelper.getLabel(elementSchema), count);
            }
        }

        return groupCount;
    }

    @Loggable(logThis = true, trim = false)
    private Map<Object, Long> verticesOrEdgesGroupCountByPropertyKeyImpl(final GraphPredicatesHolder predicatesHolder,
                                                                         final String propertyKey) {
        Map<Object, Long> finalGroupCount = new HashMap<>();

        Collection<JSONObject> elementSchemas = this.translator.filterElementSchemas(predicatesHolder);
        if (predicatesHolder.getDistinct() == true && elementSchemas.size() > 1) {
            throw new UnsupportedOperationException("Count after dedup for multi schemas.");
        }

        for (JSONObject elementSchema : elementSchemas) {
            SourceSearchHolder searchHolder = this.translator.toSourceSearchHolder(elementSchema, predicatesHolder);
            Map<Object, Long> groupCount = this.sourceProvider.searchGroupCount(
                    searchHolder, SchemaHelper.getPropertyField(elementSchema, propertyKey));

            groupCount.keySet().stream().forEach(groupKey -> {
                long groupValue = finalGroupCount.getOrDefault(groupKey, 0L) + groupCount.get(groupKey);
                finalGroupCount.put(groupKey, groupValue);
            });
        }

        return finalGroupCount;
    }

    @Loggable(logThis = true, trim = false)
    private List<RainbowElement> vertexToVerticesOrEdgesImpl(final GraphVertexToHolder graphVertexToHolder,
                                                             final GraphPredicatesHolder targetPredicatesHolder) {
        List<RainbowElement> allElements = new ArrayList<>();

        Collection<JSONObject> vertexToSchemas =
                this.translator.filterElementSchemas(graphVertexToHolder.getVertices());

        for (JSONObject vertexToSchema: vertexToSchemas) {
            Collection<Vertex> verticesTo = graphVertexToHolder.getVertices().stream()
                    .filter(vertex -> vertex.label().equals(SchemaHelper.getLabel(vertexToSchema)))
                    .collect(Collectors.toList());
            Collection<JSONObject> targetElementSchemas = this.translator.filterElementSchemas(
                    vertexToSchema,
                    graphVertexToHolder.getDirection(),
                    graphVertexToHolder.getEdgeLabels(),
                    targetPredicatesHolder);

            for (JSONObject targetElementSchema : targetElementSchemas) {
                SourceSearchHolder searchHolder = this.translator.toSourceSearchHolder(
                        vertexToSchema, verticesTo, targetElementSchema, targetPredicatesHolder);

                List<Map<String, Object>> dataSet = this.sourceProvider.search(searchHolder);

                List<RainbowElement> elements = dataSet.stream()
                        .map(dataRow -> this.translator.toRainbowElement(dataRow, targetElementSchema))
                        .filter(element -> element != null)
                        .collect(Collectors.toList());

                allElements.addAll(elements);
            }
        }

        return allElements;
    }

    @Loggable(logThis = true, trim = false)
    private long vertexToVerticesOrEdgesCountImpl(final GraphVertexToHolder graphVertexToHolder,
                                                  final GraphPredicatesHolder targetPredicatesHolder) {
        long totalCount = 0L;

        Collection<JSONObject> vertexToSchemas =
                this.translator.filterElementSchemas(graphVertexToHolder.getVertices());

        Set<JSONObject> allTargetElementSchemas = new HashSet<>();
        for (JSONObject vertexToSchema: vertexToSchemas) {
            Collection<Vertex> verticesTo = graphVertexToHolder.getVertices().stream()
                    .filter(vertex -> vertex.label().equals(SchemaHelper.getLabel(vertexToSchema)))
                    .collect(Collectors.toList());
            Collection<JSONObject> targetElementSchemas = this.translator.filterElementSchemas(
                    vertexToSchema,
                    graphVertexToHolder.getDirection(),
                    graphVertexToHolder.getEdgeLabels(),
                    targetPredicatesHolder);

            allTargetElementSchemas.addAll(targetElementSchemas);
            if (targetPredicatesHolder.getDistinct() == true && allTargetElementSchemas.size() > 1) {
                throw new UnsupportedOperationException("Count after dedup for multi schemas.");
            }

            for (JSONObject targetElementSchema : targetElementSchemas) {
                SourceSearchHolder searchHolder = this.translator.toSourceSearchHolder(
                        vertexToSchema, verticesTo, targetElementSchema, targetPredicatesHolder);

                long count = this.sourceProvider.searchCount(searchHolder);
                totalCount += count;
            }
        }

        return totalCount;
    }

}
