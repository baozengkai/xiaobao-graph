package com.eisoo.rainbow.query;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.tinkerpop.gremlin.structure.Direction;
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

public final class SchemaHelper {
    public static final String P_TIMESTAMP = "__.timestamp";
    public static final String P_ID_FIELD = "__.id.field";
    public static final String P_OUT_VERTEX_ID = "__.out.id";
    public static final String P_OUT_VERTEX_LABEL = "__.out.label";
    public static final String P_IN_VERTEX_ID = "__.in.id";
    public static final String P_IN_VERTEX_LABEL = "__.in.label";

    private static final String F_TIMESTAMP = "@timestamp";

    private static final String[] P_DEFAULTS_ELEMENT = {
            P_ID_FIELD,
            P_TIMESTAMP
    };
    private static final String[] P_DEFAULTS_EDGE = {
            P_OUT_VERTEX_ID,
            P_OUT_VERTEX_LABEL,
            P_IN_VERTEX_ID,
            P_IN_VERTEX_LABEL
    };

    private static final String K_ID = "id";
    private static final String K_LABEL = "label";
    private static final String K_PROPERTIES = "properties";
    private static final String K_VERTICES = "vertices";
    private static final String K_EDGES = "edges";
    private static final String K_OUT_VERTEX = "outVertex";
    private static final String K_IN_VERTEX = "inVertex";

    // graph schema
    public static JSONArray getVertices(final JSONObject graphSchema) {
        return graphSchema.getJSONArray(K_VERTICES);
    }

    public static JSONArray getEdges(final JSONObject graphSchema) {
        return graphSchema.getJSONArray(K_EDGES);
    }

    // element(vertex|edge) schema
    public static String getSchemaUID(final JSONObject elementSchema) {
        return elementSchema.getString(K_LABEL) + "." + elementSchema.getString(K_ID);
    }

    public static String getIdField(final JSONObject elementSchema) {
        return elementSchema.getString(K_ID);
    }

    public static String getLabel(final JSONObject elementSchema) {
        return elementSchema.getString(K_LABEL);
    }

    // element property schema
    private static Set<String> getPropertyFieldsInSchema(final JSONObject elementSchema) {
        List<?> propertyFieldsInSchema = IteratorUtils.toList(elementSchema.getJSONArray(K_PROPERTIES).iterator());
        return propertyFieldsInSchema.stream().map(v -> (String) v).collect(Collectors.toSet());
    }

    public static Set<String> getPropertyKeys(final JSONObject elementSchema) {
        Set<String> propertyNames = new HashSet<>();

        // get default property keys
        propertyNames.addAll(Arrays.asList(P_DEFAULTS_ELEMENT));
        if (isEdge(elementSchema)) {
            propertyNames.addAll(Arrays.asList(P_DEFAULTS_EDGE));
        }

        // get property keys defined in schema
        propertyNames.addAll(getPropertyFieldsInSchema(elementSchema));
        return propertyNames;
    }

    public static Set<String> getFieldPropertyKeys(final JSONObject elementSchema) {
        return getPropertyKeys(elementSchema).stream()
                .filter(propertyKey -> !isConstProperty(propertyKey))
                .collect(Collectors.toSet());
    }

    public static Set<String> getConstPropertyKeys(final JSONObject elementSchema) {
        return getPropertyKeys(elementSchema).stream()
                .filter(propertyKey -> isConstProperty(propertyKey))
                .collect(Collectors.toSet());
    }

    public static Set<String> getCustomPropertyKeys(final JSONObject elementSchema) {
        return getPropertyKeys(elementSchema).stream()
                .filter(propertyKey -> !Arrays.asList(P_DEFAULTS_ELEMENT).contains(propertyKey))
                .filter(propertyKey -> !Arrays.asList(P_DEFAULTS_EDGE).contains(propertyKey))
                .collect(Collectors.toSet());
    }

    public static String getPropertyField(final JSONObject elementSchema, final String propertyKey) {
        if (isConstProperty(propertyKey)) {
            throw new RuntimeException(String.format(
                    "Property %s is a constant property, has no mapping field.", propertyKey));
        }

        if (propertyKey.equals(P_TIMESTAMP))
            return F_TIMESTAMP;
        else if (propertyKey.equals(P_OUT_VERTEX_ID))
            return getIDFieldOfOutVertex(elementSchema);
        else if (propertyKey.equals(P_IN_VERTEX_ID))
            return getIDFieldOfInVertex(elementSchema);

        if (!getPropertyKeys(elementSchema).contains(propertyKey)) {
            throw new IllegalArgumentException(String.format(
                    "%s is not a propertyKey in schema:%s", propertyKey, elementSchema.toString()));
        }
        return propertyKey;
    }

    public static boolean isIdProperty(final String propertyKey) {
        return propertyKey.equals(P_OUT_VERTEX_ID) ||
                propertyKey.equals(P_IN_VERTEX_ID);
    }

    public static boolean isConstProperty(final String propertyKey) {
        return propertyKey.equals(P_ID_FIELD) ||
                propertyKey.equals(P_OUT_VERTEX_LABEL) ||
                propertyKey.equals(P_IN_VERTEX_LABEL);
    }

    public static String getConstPropertyValue(final JSONObject elementSchema, final String propertyKey) {
        if (propertyKey.equals(P_ID_FIELD))
            return getIdField(elementSchema);
        else if (propertyKey.equals(P_OUT_VERTEX_LABEL))
            return getLabelOfOutVertex(elementSchema);
        else if (propertyKey.equals(P_IN_VERTEX_LABEL))
            return getLabelOfInVertex(elementSchema);

        throw new RuntimeException(String.format("Unrecognized constant property key %s.", propertyKey));
    }

    // edge schema
    public static boolean isEdge(final JSONObject elementSchema) {
        if (!elementSchema.has(K_OUT_VERTEX) && !elementSchema.has(K_IN_VERTEX)) return false;
        if (elementSchema.has(K_OUT_VERTEX) && elementSchema.has(K_IN_VERTEX)) return true;
        throw new RuntimeException("Edge schema must contains outVertex and inVertex both.");
    }

    public static String getIDFieldOfOutVertex(final JSONObject edgeSchema) {
        return SchemaHelper.getIdField(edgeSchema.getJSONObject(K_OUT_VERTEX));
    }

    public static String getLabelOfOutVertex(final JSONObject edgeSchema) {
        return SchemaHelper.getLabel(edgeSchema.getJSONObject(K_OUT_VERTEX));
    }

    public static String getIDFieldOfInVertex(final JSONObject edgeSchema) {
        return SchemaHelper.getIdField(edgeSchema.getJSONObject(K_IN_VERTEX));
    }

    public static String getLabelOfInVertex(final JSONObject edgeSchema) {
        return SchemaHelper.getLabel(edgeSchema.getJSONObject(K_IN_VERTEX));
    }

    public static String getSchemaUIDOfOutVertex(final JSONObject edgeSchema) {
        return SchemaHelper.getSchemaUID(edgeSchema.getJSONObject(K_OUT_VERTEX));
    }

    public static String getSchemaUIDOfInVertex(JSONObject edgeSchema) {
        return SchemaHelper.getSchemaUID(edgeSchema.getJSONObject(K_IN_VERTEX));
    }

    public static Set<String> getSchemaUIDsOfOutInVertex(final JSONObject edgeSchema, final Direction direction) {
        switch (direction) {
            case OUT:
                return Stream.of(getSchemaUIDOfOutVertex(edgeSchema)).collect(Collectors.toSet());
            case IN:
                return Stream.of(getSchemaUIDOfInVertex(edgeSchema)).collect(Collectors.toSet());
            case BOTH:
                return Stream.of(getSchemaUIDOfOutVertex(edgeSchema), getSchemaUIDOfInVertex(edgeSchema))
                        .collect(Collectors.toSet());
            default:
                throw new IllegalArgumentException("Direction not supported: " + direction);
        }
    }
}
