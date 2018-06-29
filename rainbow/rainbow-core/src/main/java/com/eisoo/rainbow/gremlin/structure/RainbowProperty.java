package com.eisoo.rainbow.gremlin.structure;

import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;
import org.json.JSONObject;

/**
 * @author tong.sha@eisoo.com
 * @version 1.0 , Copyright (c) 2014 AnyRobot, EISOO
 * @date 2018.02.28
 */

public class RainbowProperty<V> implements Property<V> {
    protected final RainbowElement element;
    protected final String key;
    protected V value;

    public RainbowProperty(RainbowElement element, String key, V value) {
        ElementHelper.validateProperty(key, value);
        this.element = element;
        this.key = key;
        this.value = value;
    }

    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        json.put("str", StringFactory.propertyString(this));
        json.put("properties", new JSONObject());
        json.getJSONObject("properties").put(key, value);
        return json;
    }

    @Override
    public String toString() {
        return this.toJSONObject().toString();
    }

    @Override
    public boolean equals(final Object object) {
        return ElementHelper.areEqual(this, object);
    }

    @Override
    public int hashCode() {
        return ElementHelper.hashCode(this);
    }

    @Override
    public Element element() {
        return this.element;
    }

    @Override
    public String key() {
        return this.key;
    }

    @Override
    public V value() {
        return this.value;
    }

    @Override
    public boolean isPresent() {
        return null != this.value;
    }

    /* Unsupported writable methods*/

    @Override
    public void remove() {
        throw Exceptions.propertyRemovalNotSupported();
    }
}
