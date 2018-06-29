package com.eisoo.rainbow.gremlin.structure;

import com.eisoo.rainbow.gremlin.process.*;
import com.eisoo.rainbow.query.RainbowQuery;
import com.eisoo.rainbow.query.SchemaTranslator;
import com.eisoo.rainbow.query.SourceProvider;
import com.jcabi.aspects.LogExceptions;
import com.jcabi.aspects.Loggable;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.tinkerpop.gremlin.process.computer.GraphComputer;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.json.JSONObject;

import java.io.File;
import java.util.Iterator;

/**
 * @author tong.sha@eisoo.com
 * @version 1.0 , Copyright (c) 2014 AnyRobot, EISOO
 * @date 2018.02.28
 */

public final class RainbowGraph implements Graph {

    static {
        TraversalStrategies.GlobalCache.registerStrategies(
                RainbowGraph.class,
                TraversalStrategies.GlobalCache.getStrategies(Graph.class).clone().addStrategies(
                        RainbowGraphStepStrategy.instance(),
                        RainbowVertexStepStrategy.instance(),
                        RainbowCountStepStrategy.instance(),
                        RainbowGroupCountStepStrategy.instance(),
                        RainbowPropertiesStepStrategy.instance()));
    }

    private Configuration configuration;
    private SourceProvider sourceProvider;
    private SchemaTranslator schemaTranslator;
    private RainbowQuery query;

    @LogExceptions
    public static RainbowGraph open(final Configuration configuration) throws Exception {
        File file = new File(configuration.getString("provider"));
        String providerConfString = FileUtils.readFileToString(file, "UTF-8");
        String providerConfJson = (new JSONObject(providerConfString)).toString();
        RainbowGraph graph = open(providerConfJson);

        configuration.setProperty(Graph.GRAPH, RainbowGraph.class.getName());
        graph.configuration = configuration;

        return graph;
    }

    public static RainbowGraph open(final String providerConfJson) throws Exception {
        RainbowGraph graph = new RainbowGraph();
        graph.init(providerConfJson);
        return graph;
    }

    @Loggable(logThis = true, trim = false)
    private void init(final String providerConfJson) throws Exception {
        JSONObject providerConf = new JSONObject(providerConfJson);

        String sourceClass = providerConf.getString("class");
        try {
            this.sourceProvider = Class.forName(sourceClass).asSubclass(SourceProvider.class).newInstance();
        } catch (Exception e) {
            throw new RuntimeException("class: " + sourceClass + " not found");
        }
        this.sourceProvider.init(providerConf.getJSONObject("dataSet"));
        this.schemaTranslator = new SchemaTranslator(providerConf.getJSONObject("graphSchema"));
        this.query = new RainbowQuery(this.sourceProvider, this.schemaTranslator);
    }

    public SourceProvider getSourceProvider() {
        return this.sourceProvider;
    }

    public SchemaTranslator getSchemaTranslator() {
        return this.schemaTranslator;
    }

    public RainbowQuery getQuery() {
        return this.query;
    }

    @Override
    public String toString() {
        return StringFactory.graphString(this, "RainbowGraph");
    }

    @Override
    public Configuration configuration() {
        return this.configuration;
    }

    @Override
    public void close() {
    }

    /* Unsupported readble methods*/

    @Override
    public Iterator<Vertex> vertices(Object... ids) {
        throw new UnsupportedOperationException("Invalid access entry for vertices.");
    }

    @Override
    public Iterator<Edge> edges(Object... ids) {
        throw new UnsupportedOperationException("Invalid access entry for edges.");
    }

    /* Unsupported writable methods*/

    @Override
    public Transaction tx() {
        throw Exceptions.transactionsNotSupported();
    }

    @Override
    public Variables variables() {
        throw Exceptions.variablesNotSupported();
    }

    @Override
    public <C extends GraphComputer> C compute(Class<C> graphComputerClass) throws IllegalArgumentException {
        throw Exceptions.graphComputerNotSupported();
    }

    @Override
    public GraphComputer compute() throws IllegalArgumentException {
        throw Exceptions.graphComputerNotSupported();
    }

    @Override
    public Vertex addVertex(final Object... keyValues) {
        throw Exceptions.vertexAdditionsNotSupported();
    }
}
