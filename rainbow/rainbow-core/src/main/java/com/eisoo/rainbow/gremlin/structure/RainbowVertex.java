package com.eisoo.rainbow.gremlin.structure;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;
import org.apache.commons.lang.NotImplementedException;

import java.util.Iterator;
import java.util.Map;

/**
 * @author tong.sha@eisoo.com
 * @version 1.0 , Copyright (c) 2014 AnyRobot, EISOO
 * @date 2018.02.28
 */

public class RainbowVertex extends RainbowElement implements Vertex {

    public RainbowVertex(Object id, String label, Map<String, Object> keyValues, RainbowGraph graph) {
        super(id, label, keyValues, graph);
    }

    @Override
    public <V> Iterator<VertexProperty<V>> properties(final String... propertyKeys) {
        return IteratorUtils.stream(this.properties.keySet().iterator())
                .filter(key -> ElementHelper.keyExists(key, propertyKeys))
                .map(key -> (VertexProperty<V>) new RainbowVertexProperty<>(this, key, (V) this.properties.get(key)))
                .iterator();
    }

    @Override
    public Iterator<Edge> edges(Direction direction, String... edgeLabels) {
        // TODO
        throw new NotImplementedException();
    }

    @Override
    public Iterator<Vertex> vertices(Direction direction, String... edgeLabels) {
        // TODO
        throw new NotImplementedException();
    }

    /* Unsupported writable methods*/

    @Override
    public <V> VertexProperty<V> property(VertexProperty.Cardinality cardinality, String key,
                                          V value, final Object... keyValues) {
        throw Element.Exceptions.propertyAdditionNotSupported();
    }

    @Override
    public Edge addEdge(final String label, final Vertex vertex, final Object... keyValues) {
        throw Vertex.Exceptions.edgeAdditionsNotSupported();
    }

    @Override
    public void remove() {
        throw Vertex.Exceptions.vertexRemovalNotSupported();
    }

    @Override
    public String toString() {
        return this.toJSONObject().toString();
    }

    @Override
    public Map<String, Object> getInternalProperties() {
        Map<String, Object> properties = super.getInternalProperties();
        properties.put("__.desc.#", StringFactory.vertexString(this));
        return properties;
    }
}
