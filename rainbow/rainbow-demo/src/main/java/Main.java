import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import com.eisoo.rainbow.gremlin.structure.RainbowGraph;

/**
 * @author tong.sha@eisoo.com
 * @version 1.0 , Copyright (c) 2014 AnyRobot, EISOO
 * @date 2018.02.28
 */

public class Main {

    public static void main(String args[]) {
        try {
            System.out.println("hello");
            long startTime = System.currentTimeMillis();

            Configuration conf = new BaseConfiguration();

            String confPath = ClassLoader.getSystemResource("elastic.json").getPath();
            conf.addProperty("provider", confPath);
            RainbowGraph graph = RainbowGraph.open(conf);
            GraphTraversalSource g = graph.traversal();

            // TEST
            System.out.println("-------------------------- test g.V() ----------------------------");
            PrintHelper.print_c(g.V().count());
            PrintHelper.print_e(g.V().limit(2));
            PrintHelper.print_c(g.V().hasLabel("用户").count());
            PrintHelper.print_e(g.V().hasLabel("用户"));
            PrintHelper.print_c(g.V().hasLabel("文档").count());
            PrintHelper.print_e(g.V().hasLabel("文档").limit(2));
            PrintHelper.print_c(g.V().hasKey("操作对象").count());
            PrintHelper.print_e(g.V().hasKey("操作对象").limit(2));
            PrintHelper.print_c(g.V().hasKey("操作后对象").count());
            PrintHelper.print_e(g.V().hasKey("操作后对象").limit(2));
            PrintHelper.print_e(g.V().hasKey("操作后对象"));
            PrintHelper.print_e(g.V("陈燕"));
            PrintHelper.print_e(g.V().has("父路径").limit(2));
            PrintHelper.print_e(g.V().has("操作对象", "渠道售前现场培训报名表.xlsx"));
            PrintHelper.print_e(g.V().has("文档", "操作对象", "渠道售前现场培训报名表.xlsx"));
            PrintHelper.print_e(g.V().hasId("渠道售前现场培训报名表.xlsx"));
//            PrintHelper.print_e(g.V().hasNot("操作后对象").limit(2));// bug
//            PrintHelper.print_e(g.V().hasValue("渠道售前现场培训报名表.xlsx"));// bug
            PrintHelper.print_c(g.V().hasLabel("文档").has("父路径", "AnyShare://陈燕").count());
            PrintHelper.print_e(g.V().hasLabel("文档").has("父路径", "AnyShare://陈燕").limit(2));
            PrintHelper.print_e(g.V().hasKey("no-exists"));

            System.out.println("-------------------------- test g.E() ----------------------------");
            PrintHelper.print_c(g.E().count());
            PrintHelper.print_e(g.E().limit(2));
            PrintHelper.print_c(g.E().hasLabel("操作").count());
            PrintHelper.print_e(g.E().hasLabel("操作").limit(2));
            PrintHelper.print_c(g.E().hasLabel("操作后").count());
            PrintHelper.print_e(g.E().hasLabel("操作后").limit(2));
            PrintHelper.print_c(g.E().hasLabel("被操作为").count());
            PrintHelper.print_e(g.E().hasLabel("被操作为").limit(2));
            PrintHelper.print_e(g.E("AV-V3--LbcKGrP6qsnZ-"));

            System.out.println("-------------------------- test in/out ----------------------------");
            PrintHelper.print_e(g.V().hasKey("操作后对象").dedup().limit(10000).out());
            PrintHelper.print_c(g.V("童莎").hasLabel("用户").out("操作").count());
//            PrintHelper.print_c(g.V("童莎").hasLabel("用户").out("操作").id().count());// unsupport
            PrintHelper.print_e(g.V("童莎").hasLabel("用户").out("操作").limit(2));

//            PrintHelper.print_e(g.V().has("操作对象", "集群软件版开发计划及方案.doc")
//                    .inE("操作").has("操作动作", "下载")
//                    .outV()); // Unsupported
//            PrintHelper.print_e(g.V("童莎").hasLabel("用户").outE().groupCount().by(T.label)); // unsupported

//            PrintHelper.print_e(g.E().hasLabel("操作").groupCount().by("操作动作")); //bug:Expected a ':'
              // after a key at 5 [character 6 line 1]
//            PrintHelper.print_e(g.E().hasLabel("操作后").has("操作动作", "重命名")
//                    .and(__.outV().hasId("杨云彩").hasLabel("用户"))
//                    .and(__.inV().hasId("AnyShare单节点&&易享云1U单节点设备自备份方案.docx").hasLabel("文档")));// unsupported
//            PrintHelper.print_e(g.E().hasLabel("操作").has("操作动作", "下载")
//                    .and(__.outV().hasId("杨云彩").hasLabel("用户"))
//                    .and(__.inV().hasId("AnyShare单节点&&易享云1U单节点设备自备份方案.docx").hasLabel("文档")));// unsupported

            System.out.println("-------------------------- test range ----------------------------");
            PrintHelper.print_e(g.V().hasLabel("用户").dedup().limit(5));
            PrintHelper.print_e(g.V().hasLabel("用户").dedup().limit(8));
            PrintHelper.print_e(g.V().hasLabel("用户").dedup().limit(10));
            PrintHelper.print_e(g.V().hasLabel("用户").dedup().limit(10).range(0, 5));
            PrintHelper.print_e(g.V().hasLabel("用户").dedup().limit(10).range(5, 10));
            PrintHelper.print_e(g.V().hasLabel("用户").dedup().range(0, 5));
            PrintHelper.print_e(g.V().hasLabel("用户").dedup().range(0, 10));
            PrintHelper.print_e(g.V().hasLabel("用户").dedup().range(6, 8));
            PrintHelper.print_e(g.V().hasLabel("用户").limit(10));
            PrintHelper.print_e(g.V().hasLabel("用户").range(0, 5));
            PrintHelper.print_e(g.V().hasLabel("用户").range(5, 10));
            PrintHelper.print_e(g.E().hasLabel("操作后").limit(10));
            PrintHelper.print_e(g.E().hasLabel("操作后").limit(10).range(0, 5));
            PrintHelper.print_e(g.E().hasLabel("操作后").limit(10).range(5, 10));
//            PrintHelper.print_e(g.V().has("__.id.field", "操作对象").has("父路径", P.eq("产品"))
//                    .dedup().limit(10).values("父路径").dedup().limit(10));// bug:A JSONObject text must begin with '{'
//            PrintHelper.print_e(g.V().has("__.id.field", "操作对象").has("父路径", P.eq("产品"))
//                    .dedup().values("父路径").limit(10)); // bug:A JSONObject text must begin with '{'

            System.out.println("Spend: " + (System.currentTimeMillis() - startTime) + "ms");
            System.out.println("end.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
