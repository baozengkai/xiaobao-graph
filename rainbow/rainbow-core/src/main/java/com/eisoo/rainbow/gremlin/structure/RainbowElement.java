package com.eisoo.rainbow.gremlin.structure;

import org.apache.tinkerpop.gremlin.structure.*;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author tong.sha@eisoo.com
 * @version 1.0 , Copyright (c) 2014 AnyRobot, EISOO
 * @date 2018.02.28
 */

public abstract class RainbowElement implements Element {
    private static final String INTERNAL_PROPERTY_KEY_PREFIX = "__.";
    protected Object id;
    protected String label;
    protected Map<String, Object> properties;
    protected RainbowGraph graph;

    public RainbowElement(Object id, String label, Map<String, Object> properties, RainbowGraph graph) {
        this.id = id;
        this.label = label;
        this.properties = properties;
        this.graph = graph;
    }

    public String uid() {
        return this.label + "::" + this.id;
    }

    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof RainbowVertex || object instanceof RainbowEdge)) {
            return false;
        }

        return ((RainbowElement) object).uid().equals(this.uid());
    }

    @Override
    public Object id() {
        return this.id;
    }

    @Override
    public String label() {
        return this.label;
    }

    @Override
    public Graph graph() {
        return this.graph;
    }

    @Override
    public Set<String> keys() {
        return this.properties.keySet();
    }

    @Override
    public String toString() {
        return this.toJSONObject().toString();
    }

    @Override
    public int hashCode() {
        return this.uid().hashCode();
    }

    public Map<String, Object> getInternalProperties() {
        Map<String, Object> properties = new HashMap<>();
        this.properties.keySet().stream()
                .filter(key -> key.startsWith(INTERNAL_PROPERTY_KEY_PREFIX))
                .forEach(key -> properties.put(key, this.properties.get(key)));
        properties.put("__.uid.#", this.uid());
        return properties;
    }

    public Map<String, Object> getNonInternalProperties() {
        Map<String, Object> properties = new HashMap<>();
        this.properties.keySet().stream()
                .filter(key -> !key.startsWith(INTERNAL_PROPERTY_KEY_PREFIX))
                .forEach(key -> properties.put(key, this.properties.get(key)));
        return properties;
    }

    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        json.put("id", this.id);
        json.put("label", this.label);
        json.put("properties", this.getNonInternalProperties());
        json.put("__.properties", this.getInternalProperties());
        return json;
    }
}
