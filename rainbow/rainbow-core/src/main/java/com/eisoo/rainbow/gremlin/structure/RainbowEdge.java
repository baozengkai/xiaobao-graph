package com.eisoo.rainbow.gremlin.structure;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;

/**
 * @author tong.sha@eisoo.com
 * @version 1.0 , Copyright (c) 2014 AnyRobot, EISOO
 * @date 2018.02.28
 */

public class RainbowEdge extends RainbowElement implements Edge {

    protected Vertex inVertex;
    protected Vertex outVertex;

    public RainbowEdge(Object id, String label, Map<String, Object> keyValues, Vertex outV,
                       Vertex inV, final RainbowGraph graph) {
        super(id, label, keyValues, graph);
        this.outVertex = outV;
        this.inVertex = inV;
    }

    @Override
    public <V> Iterator<Property<V>> properties(final String... propertyKeys) {
        return IteratorUtils.stream(this.properties.keySet().iterator())
                .filter(key -> ElementHelper.keyExists(key, propertyKeys))
                .map(key -> (Property<V>) new RainbowProperty<>(this, key, (V) this.properties.get(key)))
                .iterator();
    }

    @Override
    public Iterator<Vertex> vertices(Direction direction) {
        switch (direction) {
            case OUT:
                return IteratorUtils.of(outVertex);
            case IN:
                return IteratorUtils.of(inVertex);
            default:
                return IteratorUtils.of(outVertex, inVertex);
        }
    }

    /* Unsupported writable methods*/

    @Override
    public <V> Property<V> property(String key, V value) {
        throw Element.Exceptions.propertyAdditionNotSupported();
    }

    @Override
    public void remove() {
        throw Edge.Exceptions.edgeRemovalNotSupported();
    }

    @Override
    public String toString() {
        return this.toJSONObject().toString();
    }

    @Override
    public Map<String, Object> getInternalProperties() {
        Map<String, Object> properties = super.getInternalProperties();
        properties.put("__.desc.#", StringFactory.edgeString(this));
        return properties;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject json = super.toJSONObject();
        json.put("outVertex", new JSONObject(this.outVertex.toString()));
        json.put("inVertex", new JSONObject(this.inVertex.toString()));
        return json;
    }
}
