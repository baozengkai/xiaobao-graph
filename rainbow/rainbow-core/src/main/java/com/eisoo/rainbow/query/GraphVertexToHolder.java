package com.eisoo.rainbow.query;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.*;

/**
 * @author tong.sha@eisoo.com
 * @version 1.0 , Copyright (c) 2014 AnyRobot, EISOO
 * @date 2018.02.28
 */

public final class GraphVertexToHolder {
    private final List<Vertex> vertices = new ArrayList<>();
    private final Direction direction;
    private final String[] edgeLabels;

    public GraphVertexToHolder(final Direction direction, final String... edgeLabels) {
        this.direction = direction;
        this.edgeLabels = edgeLabels;
    }

    public void addVertex(final Vertex vertex) {
        this.vertices.add(vertex);
    }

    public void addVertices(final List<Vertex> vertices) {
        this.vertices.addAll(vertices);
    }

    public List<Vertex> getVertices() {
        return this.vertices;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public String[] getEdgeLabels() {
        return this.edgeLabels;
    }

    @Override
    public String toString() {
        List<String> strings = new ArrayList<String>();
        strings.add(this.vertices.toString());
        strings.add(this.direction.toString());
        strings.add(Arrays.asList(this.edgeLabels).toString());
        return this.getClass().getSimpleName() + "(" + String.join(", ", strings) + ")";
    }
}
