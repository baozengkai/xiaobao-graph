package com.eisoo.rainbow.gremlin.structure;

import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.Property;
import java.util.Iterator;

/**
 * @author tong.sha@eisoo.com
 * @version 1.0 , Copyright (c) 2014 AnyRobot, EISOO
 * @date 2018.02.28
 */

public class RainbowVertexProperty<V> extends RainbowProperty<V> implements VertexProperty<V> {

    public RainbowVertexProperty(final RainbowVertex vertex, final String key, final V value) {
        super(vertex, key, value);
    }

    @Override
    public Vertex element() {
        return (Vertex) super.element();
    }

    /* Unsupported readble methods*/

    @Override
    public Object id() {
        throw VertexProperty.Exceptions.identicalMultiPropertiesNotSupported();
    }

    @Override
    public <U> Iterator<Property<U>> properties(String... propertyKeys) {
        throw VertexProperty.Exceptions.multiPropertiesNotSupported();
    }

    /* Unsupported writable methods*/

    @Override
    public <U> Property<U> property(final String key, final U value) {
        throw VertexProperty.Exceptions.multiPropertiesNotSupported();
    }
}
