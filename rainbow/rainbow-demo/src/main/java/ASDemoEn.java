import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import com.eisoo.rainbow.gremlin.structure.RainbowGraph;

/**
 * @author tong.sha@eisoo.com
 * @version 1.0 , Copyright (c) 2014 AnyRobot, EISOO
 * @date 2018.02.28
 */

public class ASDemoEn {
    public static void main(String[] args) {
        try {
            System.out.println("hello");
            long startTime = System.currentTimeMillis();

            Configuration conf = new BaseConfiguration();

            String confPath = ClassLoader.getSystemResource("elastic-en.json").getPath();
            conf.addProperty("provider", confPath);
            RainbowGraph graph = RainbowGraph.open(conf);
            GraphTraversalSource g = graph.traversal();

            PrintHelper.print_e(g.V()
                    .has("DOC", "after_operation object", "01-EISOO AnyShare POC Guide for Cluster (7).docx"));

            PrintHelper.print_e(g.V()
                    .has("DOC", "after_operation object", "\"01-EISOO AnyShare POC Guide for Cluster (7).docx\""));

            System.out.println("Spend: " + (System.currentTimeMillis() - startTime) + "ms");
            System.out.println("end.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
