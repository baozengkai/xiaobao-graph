package com.eisoo.rainbow.query;

import org.apache.tinkerpop.gremlin.process.traversal.P;

/**
 * @author tong.sha@eisoo.com
 * @version 1.0 , Copyright (c) 2014 AnyRobot, EISOO
 * @date 2018.02.28
 */

public class ExistsP<V> extends P<V> {
    public ExistsP() {
        super(null, null);
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return "exist";
    }
}
