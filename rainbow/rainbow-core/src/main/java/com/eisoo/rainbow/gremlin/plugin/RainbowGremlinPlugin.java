package com.eisoo.rainbow.gremlin.plugin;

import org.apache.tinkerpop.gremlin.jsr223.AbstractGremlinPlugin;
import org.apache.tinkerpop.gremlin.jsr223.DefaultImportCustomizer;
import org.apache.tinkerpop.gremlin.jsr223.ImportCustomizer;
import com.eisoo.rainbow.gremlin.structure.RainbowGraph;

/**
 * @author tong.sha@eisoo.com
 * @version 1.0 , Copyright (c) 2014 AnyRobot, EISOO
 * @date 2018.02.28
 */

public class RainbowGremlinPlugin extends AbstractGremlinPlugin {
    private static final RainbowGremlinPlugin instance = new RainbowGremlinPlugin();
    private static final ImportCustomizer imports;

    static {
        try {
            imports = DefaultImportCustomizer.build()
                    .addClassImports(RainbowGraph.class)
                    .create();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    public RainbowGremlinPlugin() {
        super("rainbow", imports);
    }

    public static RainbowGremlinPlugin instance() {
        return instance;
    }

    @Override
    public boolean requireRestart() {
        return true;
    }
}
