package com.eisoo.rainbow.query;

import com.eisoo.rainbow.gremlin.structure.RainbowEdge;
import com.eisoo.rainbow.gremlin.structure.RainbowElement;
import com.eisoo.rainbow.gremlin.structure.RainbowVertex;
import com.jcabi.aspects.Loggable;
import org.apache.tinkerpop.gremlin.process.traversal.Compare;
import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author tong.sha@eisoo.com
 * @version 1.0 , Copyright (c) 2014 AnyRobot, EISOO
 * @date 2018.02.28
 */

public final class SchemaTranslator<E extends Element> {
    /**
     * Map<schema uid, schema>
     */
    private final Map<String, JSONObject> vertexSchemas = new HashMap<>();
    private final Map<String, JSONObject> edgeSchemas = new HashMap<>();

    public SchemaTranslator(JSONObject graphSchema) {
        JSONArray verticesSchema = SchemaHelper.getVertices(graphSchema);
        for (int i = 0; i < verticesSchema.length(); i++) {
            final JSONObject vertexSchema = verticesSchema.getJSONObject(i);
            this.vertexSchemas.put(SchemaHelper.getSchemaUID(vertexSchema), vertexSchema);
        }

        JSONArray edgesSchema = SchemaHelper.getEdges(graphSchema);
        for (int i = 0; i < edgesSchema.length(); i++) {
            final JSONObject edgeSchema = edgesSchema.getJSONObject(i);
            this.edgeSchemas.put(SchemaHelper.getSchemaUID(edgeSchema), edgeSchema);
        }
    }

    @Loggable(logThis = true, trim = false)
    public SourceSearchHolder toSourceSearchHolder(final JSONObject targetElementSchema,
                                                   final GraphPredicatesHolder<E> targetPredicatesHolder) {
        SourceSearchHolder searchHolder = new SourceSearchHolder();

        // 转换成search:query条件
        searchHolder.setQueryHolder(this.toSourceQueryHolder(targetElementSchema,
                targetPredicatesHolder.getHasContainers()));

        if (targetPredicatesHolder.getDistinct() == true) {
            // 设置去重字段为 id 映射字段
            searchHolder.setDistinctField(SchemaHelper.getIdField(targetElementSchema));
        }

        // 先默认按时间降序排序
        searchHolder.addOrder(
                SchemaHelper.getPropertyField(targetElementSchema, SchemaHelper.P_TIMESTAMP),
                Order.decr);
        // 再增加自定义排序
        targetPredicatesHolder.getOrders().stream()
                .filter(p -> !SchemaHelper.isConstProperty(p.getValue0()))
                .filter(p -> SchemaHelper.getPropertyKeys(targetElementSchema).contains(p.getValue0()))
                .forEach(p -> searchHolder.addOrder(
                        SchemaHelper.getPropertyField(targetElementSchema, p.getValue0()), p.getValue1()));

        long limit = targetPredicatesHolder.getSize() > 0 ?
                targetPredicatesHolder.getSize() + targetPredicatesHolder.getFrom() :
                targetPredicatesHolder.getSize();
        searchHolder.setSize(limit);

        return searchHolder;
    }

    @Loggable(logThis = true, trim = false)
    public SourceSearchHolder toSourceSearchHolder(final JSONObject targetElementSchema,
                                                   final GraphPredicatesHolder<E> targetPredicatesHolder,
                                                   final String propertyKey,
                                                   final long limit) {
        SourceSearchHolder searchHolder = this.toSourceSearchHolder(targetElementSchema, targetPredicatesHolder);
        searchHolder.setDistinctField(SchemaHelper.getPropertyField(targetElementSchema, propertyKey));
        searchHolder.setSize(limit);
        return searchHolder;
    }

    @Loggable(logThis = true, trim = false)
    public SourceSearchHolder toSourceSearchHolder(final JSONObject vertexToSchema,
                                                   final Collection<Vertex> verticesTo,
                                                   final JSONObject targetElementSchema,
                                                   final GraphPredicatesHolder<E> targetPredicatesHolder) {
        SourceSearchHolder searchHolder = this.toSourceSearchHolder(targetElementSchema, targetPredicatesHolder);

        // Add query clauses for @vertices
        GraphPredicatesHolder predicatesHolderForVertex = new GraphPredicatesHolder();
        predicatesHolderForVertex.addHasContainer(
                new HasContainer(T.label.getAccessor(), P.eq(SchemaHelper.getLabel(vertexToSchema))));
        predicatesHolderForVertex.addHasContainer(
                new HasContainer(T.id.getAccessor(), P.within(verticesTo.stream().map(v -> v.id()).toArray())));
        predicatesHolderForVertex.setReturnClass(Vertex.class);

        SourceQueryHolder queryHolderForVertexTo = this.toSourceQueryHolder(
                vertexToSchema, predicatesHolderForVertex.getHasContainers());

        searchHolder.getQueryHolder().addChild(queryHolderForVertexTo);

        return searchHolder;
    }

    public RainbowElement toRainbowElement(Map<String, Object> dataRow, JSONObject elementSchema) {
        Object id = dataRow.get(SchemaHelper.getIdField(elementSchema));
        if (null == id) {
            return null;
        }

        String label = SchemaHelper.getLabel(elementSchema);

        Map<String, Object> properties = new HashMap<>();
        properties.put(SchemaHelper.P_ID_FIELD, SchemaHelper.getIdField(elementSchema));
        properties.put(SchemaHelper.P_TIMESTAMP,
                dataRow.get(SchemaHelper.getPropertyField(elementSchema, SchemaHelper.P_TIMESTAMP)));
        SchemaHelper.getCustomPropertyKeys(elementSchema).stream()
                .map(propertyKey -> SchemaHelper.getPropertyField(elementSchema, propertyKey))
                .forEach(propertyField -> {
                    if (dataRow.containsKey(propertyField) && dataRow.get(propertyField) != null) {
                        properties.put(propertyField, dataRow.get(propertyField));
                    }
                });

        if (SchemaHelper.isEdge(elementSchema)) {
            JSONObject outVertexSchema = this.vertexSchemas.get(SchemaHelper.getSchemaUIDOfOutVertex(elementSchema));
            properties.put(SchemaHelper.P_OUT_VERTEX_ID, dataRow.get(SchemaHelper.getIdField(outVertexSchema)));
            properties.put(SchemaHelper.P_OUT_VERTEX_LABEL, SchemaHelper.getLabel(outVertexSchema));
            RainbowVertex outVertex = (RainbowVertex) this.toRainbowElement(dataRow, outVertexSchema);
            if (null == outVertex) {
                return null;
            }

            JSONObject inVertexSchema = this.vertexSchemas.get(SchemaHelper.getSchemaUIDOfInVertex(elementSchema));
            properties.put(SchemaHelper.P_IN_VERTEX_ID, dataRow.get(SchemaHelper.getIdField(inVertexSchema)));
            properties.put(SchemaHelper.P_IN_VERTEX_LABEL, SchemaHelper.getLabel(inVertexSchema));
            RainbowVertex inVertex = (RainbowVertex) this.toRainbowElement(dataRow, inVertexSchema);
            if (null == inVertex) {
                return null;
            }

            return new RainbowEdge(id, label, properties, outVertex, inVertex, null);
        }

        return new RainbowVertex(id, label, properties, null);
    }

    public List<RainbowElement> localFilterElements(final List<RainbowElement> elements,
                                                     final GraphPredicatesHolder targetPredicatesHolder) {
        final List<RainbowElement> finalElements = new ArrayList<>();

        // 去重情况下，保留最新时间状态的 element
        if (targetPredicatesHolder.getDistinct() == true) {
            elements.stream().forEach(element -> {
                int index = finalElements.indexOf(element);
                if (index != -1) {
                    String t1 = finalElements.get(index).value(SchemaHelper.P_TIMESTAMP).toString();
                    String t2 = element.value(SchemaHelper.P_TIMESTAMP).toString();
                    if (t1.compareTo(t2) < 0) {
                        finalElements.set(index, element);
                    }
                } else {
                    finalElements.add(element);
                }
            });
        } else {
            finalElements.addAll(elements);
        }

        return finalElements;
    }

    /**
     * 依据graph查询条件过滤出相匹配的vertices/edges schema
     */
    @Loggable(logThis = true, trim = false)
    public Collection<JSONObject> filterElementSchemas(final GraphPredicatesHolder<E> graphPredicatesHolder) {
        Collection<JSONObject> elementSchemas = Vertex.class.isAssignableFrom(graphPredicatesHolder.getReturnClass()) ?
                this.vertexSchemas.values() : this.edgeSchemas.values();
        return this.filterElementSchemas(elementSchemas, graphPredicatesHolder.getHasContainers());
    }

    @Loggable(logThis = true, trim = false)
    public Collection<JSONObject> filterElementSchemas(final Collection<Vertex> vertices) {
        return vertices.stream()
                .flatMap(vertex -> {
                    return this.vertexSchemas.values().stream()
                            .filter(vertexSchema -> SchemaHelper.getLabel(vertexSchema).equals(vertex.label()))
                            .collect(Collectors.toSet()).stream();
                })
                .collect(Collectors.toSet());
    }

    @Loggable(logThis = true, trim = false)
    public Collection<JSONObject> filterElementSchemas(final JSONObject vertexToSchema,
                                                       final Direction vertexToDirection,
                                                       final String[] vertexToEdgeLabels,
                                                       final GraphPredicatesHolder targetPredicatesHolder) {
        String vertexToSchemaUID = SchemaHelper.getSchemaUID(vertexToSchema);

        // First: filter edge schemas matched with @vertexSchema and @vertexToHolder
        Set<String> edgeLabelSet = Stream.of(vertexToEdgeLabels).collect(Collectors.toSet());
        Set<JSONObject> matchedEdgeSchemas = this.edgeSchemas.values().stream()
                .filter(edgeSchema ->
                        edgeLabelSet.size() == 0 || edgeLabelSet.contains(SchemaHelper.getLabel(edgeSchema)))
                .filter(edgeSchema ->
                        SchemaHelper.getSchemaUIDsOfOutInVertex(edgeSchema, vertexToDirection).stream()
                            .anyMatch(schemaUID -> schemaUID.equals(vertexToSchemaUID)))
                .collect(Collectors.toSet());

        // Second: filter matched reference vertex schemas in @matchedEdgeSchemas
        Set<JSONObject> matchedVertexSchemas = matchedEdgeSchemas.stream()
                .map(edgeSchema -> {
                    if (vertexToSchemaUID.equals(SchemaHelper.getSchemaUIDOfInVertex(edgeSchema))) {
                        return SchemaHelper.getSchemaUIDOfOutVertex(edgeSchema);
                    } else if (vertexToSchemaUID.equals(SchemaHelper.getSchemaUIDOfOutVertex(edgeSchema))) {
                        return SchemaHelper.getSchemaUIDOfInVertex(edgeSchema);
                    }
                    throw new RuntimeException("Unmatched edge schema.");
                })
                .map(schemaUID -> this.vertexSchemas.get(schemaUID))
                .collect(Collectors.toSet());

        // Last: filter matched element schemas with @graphPredicatesHolder
        if (Edge.class.isAssignableFrom(targetPredicatesHolder.getReturnClass())) {
            return this.filterElementSchemas(matchedEdgeSchemas, targetPredicatesHolder.getHasContainers());
        } else {
            return this.filterElementSchemas(matchedVertexSchemas, targetPredicatesHolder.getHasContainers());
        }
    }

    /**
     * 依据graph查询条件过滤出相匹配的vertices/edges schema
     */
    private List<JSONObject> filterElementSchemas(Collection<JSONObject> elementSchemas,
                                                  final List<HasContainer> hasContainers) {
        List<HasContainer> idHasContainers = hasContainers.stream()
                .filter(hasContainer -> hasContainer.getKey().equals(T.id.getAccessor()))
                .collect(Collectors.toList());
        List<HasContainer> labelHasContainers = hasContainers.stream()
                .filter(hasContainer -> hasContainer.getKey().equals(T.label.getAccessor()))
                .collect(Collectors.toList());
        List<HasContainer> keyHasContainers = hasContainers.stream()
                .filter(hasContainer -> hasContainer.getKey().equals(T.key.getAccessor()))
                .collect(Collectors.toList());
        List<HasContainer> valueHasContainers = hasContainers.stream()
                .filter(hasContainer -> hasContainer.getKey().equals(T.value.getAccessor()))
                .collect(Collectors.toList());
        List<HasContainer> propertyHasContainers = hasContainers.stream()
                .filter(hasContainer -> !idHasContainers.contains(hasContainer))
                .filter(hasContainer -> !labelHasContainers.contains(hasContainer))
                .filter(hasContainer -> !keyHasContainers.contains(hasContainer))
                .filter(hasContainer -> !valueHasContainers.contains(hasContainer))
                .collect(Collectors.toList());

        return elementSchemas.stream().filter(elementSchema -> {
            final String label = SchemaHelper.getLabel(elementSchema);
            if (!labelHasContainers.stream().allMatch(c -> ((P<String>) c.getPredicate()).test(label))) {
                return false;
            }

            final Set<String> propertyKeys = SchemaHelper.getPropertyKeys(elementSchema);
            if (!keyHasContainers.stream().allMatch(c -> {
                if (c.getBiPredicate().equals(Compare.neq)) {
                    return propertyKeys.stream()
                            .allMatch(propertyKey -> ((P<String>) c.getPredicate()).test(propertyKey));
                } else {
                    return propertyKeys.stream()
                            .anyMatch(propertyKey -> ((P<String>) c.getPredicate()).test(propertyKey));
                }
            })) {
                return false;
            }

            if (!propertyHasContainers.stream().allMatch(c -> propertyKeys.contains(c.getKey()))) {
                return false;
            }
            if (!propertyHasContainers.stream()
                    .filter(c -> SchemaHelper.isConstProperty(c.getKey()))
                    .allMatch(c -> {
                        Object propertyValue = SchemaHelper.getConstPropertyValue(elementSchema, c.getKey());
                        return ((P<Object>) c.getPredicate()).test(propertyValue);
                    })) {
                return false;
            }

            return true;
        }).collect(Collectors.toList());
    }

    private SourceQueryHolder toSourceQueryHolder(final JSONObject elementSchema,
                                                  final List<HasContainer> hasContainers) {
        SourceQueryHolder andQueryHolder = new SourceQueryHolder(SourceQueryHolder.TYPE.And);
        Set<String> existsPFields = new HashSet<>();

        // id 映射字段需不为空
        existsPFields.add(SchemaHelper.getIdField(elementSchema));
        if (SchemaHelper.isEdge(elementSchema)) {
            // edge 查询：其出入顶点的id映射字段也需不为空
            existsPFields.add(SchemaHelper.getIDFieldOfOutVertex(elementSchema));
            existsPFields.add(SchemaHelper.getIDFieldOfInVertex(elementSchema));
        }

        // graph has containers条件转换成fields query条件
        for (HasContainer hasContainer : hasContainers) {
            if (hasContainer.getKey().equals(T.id.getAccessor())) {
                // id 映射的字段去匹配hasContainer条件
                String field = SchemaHelper.getIdField(elementSchema);
                andQueryHolder.addQueryClause(new SourceQueryClause(
                        SourceQueryClause.TYPE.Filter, field, hasContainer.getPredicate()));
                existsPFields.remove(field);
            } else if (hasContainer.getKey().equals(T.value.getAccessor())) {
                // value 代表所有属性值去匹配hasContainer条件

                // 若存在常量属性可满足value条件，则无需增加字段搜索条件了
                if (SchemaHelper.getConstPropertyKeys(elementSchema).stream()
                        .map(propertyKey -> SchemaHelper.getConstPropertyValue(elementSchema, propertyKey))
                        .anyMatch(propertyValue -> ((P<Object>) hasContainer.getPredicate()).test(propertyValue))) {
                    continue;
                }

                // 所有 properties 映射的字段去匹配hasContainer条件
                SourceQueryHolder orQueryHolder = new SourceQueryHolder(SourceQueryHolder.TYPE.Or);
                SchemaHelper.getFieldPropertyKeys(elementSchema).stream()
                        .map(propertyKey -> new SourceQueryClause(
                                SchemaHelper.isIdProperty(propertyKey) ? SourceQueryClause.TYPE.Filter :
                                        SourceQueryClause.TYPE.Query,
                                SchemaHelper.getPropertyField(elementSchema, propertyKey),
                                hasContainer.getPredicate()))
                        .forEach(orQueryHolder::addQueryClause);
                andQueryHolder.addChild(orQueryHolder);
            } else if (hasContainer.getKey().equals(T.label.getAccessor())) {
                continue;
            } else if (hasContainer.getKey().equals(T.key.getAccessor())) {
                // key 代表需存在该属性
                String propertyKey = hasContainer.getValue() instanceof String ?
                        (String) hasContainer.getValue() : ((List<String>) hasContainer.getValue()).get(0);

                // 常量属性无映射字段，忽略之
                if (SchemaHelper.isConstProperty(propertyKey)) {
                    continue;
                }

                // key 对应的映射字段需不为空
                existsPFields.add(SchemaHelper.getPropertyField(elementSchema, propertyKey));
            } else {
                String propertyKey = hasContainer.getKey();

                // 常量属性无映射字段，忽略之
                if (SchemaHelper.isConstProperty(propertyKey)) {
                    continue;
                }

                // 对应propertyKey映射的字段去匹配hasContainer条件，精确匹配
                String field = SchemaHelper.getPropertyField(elementSchema, propertyKey);
                andQueryHolder.addQueryClause(new SourceQueryClause(
                        SchemaHelper.isIdProperty(propertyKey) ? SourceQueryClause.TYPE.Filter :
                                SourceQueryClause.TYPE.Query,
                        field,
                        hasContainer.getPredicate()));
                existsPFields.remove(field);
            }
        }

        existsPFields.stream().forEach(field -> {
            SourceQueryClause clause = new SourceQueryClause(SourceQueryClause.TYPE.Filter, field, new ExistsP<>());
            andQueryHolder.addQueryClause(clause);
        });

        return andQueryHolder;
    }
}
