import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.T;
import com.eisoo.rainbow.gremlin.structure.RainbowGraph;


import static org.apache.tinkerpop.gremlin.process.traversal.Order.decr;
import static org.apache.tinkerpop.gremlin.process.traversal.Order.incr;

/**
 * @author tong.sha@eisoo.com
 * @version 1.0 , Copyright (c) 2014 AnyRobot, EISOO
 * @date 2018.02.28
 */

public class ASDemo {

    public static void main(String[] args) {
        try {
            System.out.println("hello");
            long startTime = System.currentTimeMillis();

            Configuration conf = new BaseConfiguration();

            String confPath = ClassLoader.getSystemResource("elastic.json").getPath();
            conf.addProperty("provider", confPath);
            RainbowGraph graph = RainbowGraph.open(conf);
            GraphTraversalSource g = graph.traversal();

            System.out.println("=============== /graph/vertex/filter ===============");
            // PrintHelper.print(g.V().hasId("陈武").dedup().order().by("用户").toList());// unsupport
            PrintHelper.print_e(g.V().hasId("陈武").dedup());
            PrintHelper.print_e(g.V().has("用户", "陈武").dedup());
            PrintHelper.print_e(g.V().has("__.id.field", "用户").has("用户", P.eq("陈武")).dedup());
            PrintHelper.print_e(g.V().has("用户", "*").dedup());
            PrintHelper.print_e(g.V().has("用户", "陈武").dedup().limit(10));
            PrintHelper.print_e(g.V().has("用户", P.eq("*")).dedup());
            PrintHelper.print_e(g.V().has("用户", P.eq("陈 武")).dedup());
            PrintHelper.print_e(g.V().has("用户", P.eq("陈")).dedup());
            PrintHelper.print_e(g.V().has("用户", P.eq("\"陈\"")).dedup());
            PrintHelper.print_e(g.V().has("用户", P.eq("陈武")).dedup());
            PrintHelper.print_e(g.V().has("用户", P.eq("\"陈武\"")).dedup());
            PrintHelper.print_e(g.V().has("用户", P.eq("储成钢")).dedup());
            PrintHelper.print_e(g.V().has("用户", P.eq("\"储成钢\"")).dedup());
            PrintHelper.print_e(g.V().has("用户", P.neq("储成钢")).dedup());
            PrintHelper.print_e(g.V().has("用户", P.without("储成钢")).dedup());
            PrintHelper.print_e(g.V().has("用户", P.within("储成钢", "杨自晓", "陈武")).dedup());
            PrintHelper.print_e(g.V().hasLabel("用户").has("@timestamp", P.between("2015-01-01", "2015-12-31")).dedup());
            PrintHelper.print_e(g.V().hasLabel("文档").hasKey("操作对象").dedup());
            PrintHelper.print_e(g.V().hasLabel("文档").hasKey("操作对象").has("操作对象", "招聘职位梳理表.xlsx").dedup());
            PrintHelper.print_e(g.V().hasLabel("文档").hasKey("操作对象").has("操作对象", P.eq("招聘职位梳理表.xlsx")).dedup());
            PrintHelper.print_e(g.V().hasLabel("文档").has("@timestamp", P.between("2015-01-01", "2015-12-31")).dedup());
            PrintHelper.print_e(g.V().hasLabel("文档").has("@timestamp", P.outside("2015-01-01", "2015-12-31")).dedup());
            PrintHelper.print_e(g.V("渠道售前现场培训报名表.xlsx").hasLabel("文档").dedup());

            System.out.println("=============== /graph/vertex/property/value/search ===============");
            PrintHelper.print_v(g.V().has("__.id.field", "用户").has("用户", P.eq("陈"))
                    .values("用户").dedup());
            PrintHelper.print_v(g.V().has("__.id.field", "用户").has("用户", P.eq("陈"))
                    .values("用户").dedup().limit(10));
            PrintHelper.print_v(g.V().has("__.id.field", "操作对象").has("父路径", P.eq("产品"))
                    .values("父路径").dedup().limit(10));
            PrintHelper.print_v(g.V().has("__.id.field", "操作后对象").has("父路径", P.eq("产品"))
                    .values("父路径").dedup().limit(10));
            //PrintHelper.print_v(g.V().dedup().values("父路径").dedup()); // unsupport
            //PrintHelper.print_v(g.V().values("父路径").dedup().range(1, 2)); // unsupport
            PrintHelper.print_v(g.V().has("__.id.field", "用户").has("用户", P.eq("巨")).values("用户").dedup().limit(10));
            PrintHelper.print_v(g.V().has("__.id.field", "用户").has("用户", P.eq("巨")).dedup().values("用户").limit(10));

            System.out.println("=============== /graph/vertex/neighbors ===============");
            PrintHelper.print_e(g.V("陈武").hasLabel("用户").both().dedup());
            PrintHelper.print_e(g.V("陈武").hasLabel("用户").dedup().both().dedup());
            PrintHelper.print_e(g.V("陈武").hasLabel("用户").dedup().out().dedup());
            PrintHelper.print_e(g.V("陈武").hasLabel("用户").dedup().in().dedup());
            //PrintHelper.print(g.V("渠道售前现场培训报名表.xlsx").hasLabel("文档").both().dedup().count()); // unsupport
            PrintHelper.print_c(g.V("渠道售前现场培训报名表.xlsx").hasLabel("文档").dedup().both().count());
            PrintHelper.print_e(g.V("渠道售前现场培训报名表.xlsx").hasLabel("文档").dedup().both().limit(10));
            PrintHelper.print_e(g.V("渠道售前现场培训报名表.xlsx").hasLabel("文档").dedup().both().dedup());
            PrintHelper.print_e(g.V("渠道售前现场培训报名表.xlsx").hasLabel("文档").dedup().both().dedup().limit(10));
            PrintHelper.print_c(g.V("渠道售前现场培训报名表.xlsx").hasLabel("文档").dedup().in().count());
            PrintHelper.print_e(g.V("渠道售前现场培训报名表.xlsx").hasLabel("文档").dedup().in().dedup().limit(10));
            PrintHelper.print_c(g.V("渠道售前现场培训报名表.xlsx").hasLabel("文档").dedup().out().count());
            PrintHelper.print_e(g.V("渠道售前现场培训报名表.xlsx").hasLabel("文档").dedup().out().dedup().limit(10));
            //PrintHelper.print(g.V("陈武").hasLabel("用户").out().dedup().count().toList()); // unsupport
            PrintHelper.print_c(g.V("陈武").hasLabel("用户").dedup().out("操作").dedup().count());
            PrintHelper.print_e(g.V("陈武").hasLabel("用户").dedup().out().dedup().limit(10));
            PrintHelper.print_e(g.V("渠道售前现场培训报名表.xlsx").dedup().hasLabel("文档").bothE().dedup());
            PrintHelper.print_e(g.V("渠道售前现场培训报名表.xlsx").dedup().hasLabel("文档").inE().dedup());
            PrintHelper.print_e(g.V("渠道售前现场培训报名表.xlsx").dedup().hasLabel("文档").outE().dedup());

            System.out.println("=============== /graph/edge/filter ===============");
            PrintHelper.print_e(g.E().hasLabel("操作")
                    .has("__.out.id", "陈武")
                    .has("操作动作", "文件锁"));
            PrintHelper.print_e(g.E().hasLabel("操作")
                    .has("__.out.id", "陈武（Max）")
                    .has("操作动作", "共享"));
            PrintHelper.print_e(g.E()
                    .has("__.out.id", "胡佳妮")
                    .has("__.out.label", "用户")
                    .has("__.in.id", "渠道售前现场培训报名表.xlsx")
                    .has("__.in.label", "文档"));
            PrintHelper.print_e(g.E().hasLabel("操作")
                    .has("__.out.id", "胡佳妮")
                    .has("__.in.id", "渠道售前现场培训报名表.xlsx"));
            PrintHelper.print_e(g.E().hasLabel("操作后")
                    .has("__.out.id", "胡佳妮")
                    .has("__.in.id", "渠道售前现场培训报名表.xlsx"));
            PrintHelper.print_e(g.E().hasLabel("操作后").has("操作动作", "重命名")
                    .has("__.out.id", "胡佳妮")
                    .has("__.in.id", "渠道售前现场培训报名表.xlsx"));
            PrintHelper.print_e(g.E().hasLabel("操作").has("操作动作", "下载")
                    .has("__.out.id", "胡佳妮")
                    .has("__.in.id", "渠道售前现场培训报名表.xlsx"));
            PrintHelper.print_e(g.E()
                    .hasLabel("操作")
                    .has("__.out.id", "胡佳妮")
                    .has("__.out.label", "用户")
                    .has("__.in.id", "渠道售前现场培训报名表.xlsx")
                    .has("__.in.label", "文档")
                    .order().by("@timestamp", decr).by("操作动作", incr)
                    .range(0, 15));
            PrintHelper.print_e(g.E()
                    .hasLabel("操作")
                    .has("__.out.id", "胡佳妮")
                    .has("__.out.label", "用户")
                    .has("__.in.id", "渠道售前现场培训报名表.xlsx")
                    .has("__.in.label", "文档")
                    .order().by("@timestamp", decr).by("操作动作", incr)
                    .range(0, 5));
            PrintHelper.print_e(g.E()
                    .hasLabel("操作")
                    .has("__.out.id", "胡佳妮")
                    .has("__.out.label", "用户")
                    .has("__.in.id", "渠道售前现场培训报名表.xlsx")
                    .has("__.in.label", "文档")
                    .order().by("@timestamp", decr).by("操作动作", incr)
                    .range(5, 10));
            PrintHelper.print_e(g.E()
                    .hasLabel("操作")
                    .has("__.out.id", "胡佳妮")
                    .has("__.out.label", "用户")
                    .has("__.in.id", "渠道售前现场培训报名表.xlsx")
                    .has("__.in.label", "文档")
                    .order().by("@timestamp", decr).by("操作动作", incr)
                    .range(10, 15));
            PrintHelper.print_e(g.E()
                    .hasLabel("操作")
                    .has("__.out.id", "胡佳妮")
                    .has("__.out.label", "用户")
                    .has("__.in.id", "渠道售前现场培训报名表.xlsx")
                    .has("__.in.label", "文档")
                    .order().by("@timestamp", decr).by("操作动作", decr).by("操作状态", incr)
                    .range(0, 10));
            PrintHelper.print_e(g.E()
                    .hasLabel("操作")
                    .has("__.out.id", "胡佳妮")
                    .has("__.out.label", "用户")
                    .has("__.in.id", "渠道售前现场培训报名表.xlsx")
                    .has("__.in.label", "文档")
                    .order().by("@timestamp", decr).by("操作动作", decr).by("操作状态", decr).by("__.out.label")
                    .range(0, 10));

            System.out.println("=============== /graph/edge/groupCountByLabel ===============");
            PrintHelper.print_c(g.E().has("__.out.id", "胡佳妮")
                    .has("__.out.label", "用户")
                    .has("__.in.id", "渠道售前现场培训报名表.xlsx")
                    .has("__.in.label", "文档")
                    .groupCount().by(T.label));
            PrintHelper.print_c(g.E().has("__.in.id", "胡佳妮")
                    .has("__.in.label", "用户")
                    .has("__.out.id", "渠道售前现场培训报名表.xlsx")
                    .has("__.out.label", "文档")
                    .groupCount().by(T.label));
            PrintHelper.print_c(g.E().has("__.out.id", "陈亮")
                    .has("__.in.id", "AnyShare3.5访问控制开放API接口文档.docx")
                    .has("__.out.label", "用户")
                    .has("__.in.label", "文档").groupCount().by(T.label));

            System.out.println("=============== /graph/edge/groupCountByProperty ===============");
            PrintHelper.print_c(g.E().hasLabel("操作")
                    .has("__.out.id", "胡佳妮")
                    .has("__.in.id", "渠道售前现场培训报名表.xlsx")
                    .groupCount().by("操作动作"));
            PrintHelper.print_c(g.E().hasLabel("操作后")
                    .has("__.out.id", "胡佳妮")
                    .has("__.in.id", "渠道售前现场培训报名表.xlsx")
                    .groupCount().by("操作动作"));
            PrintHelper.print_c(g.E().hasLabel("操作")
                    .has("__.out.id", "胡佳妮")
                    .has("__.in.id", "渠道售前现场培训报名表.xlsx")
                    .groupCount().by("操作状态"));
            PrintHelper.print_c(g.E().hasLabel("操作")
                    .has("__.out.id", "胡佳妮")
                    .has("__.in.id", "渠道售前现场培训报名表.xlsx")
                    .groupCount().by("_id"));
            PrintHelper.print_c(g.E().hasLabel("操作")
                    .has("__.out.id", "陈亮")
                    .has("__.in.id", "AnyShare3.5访问控制开放API接口文档.docx")
                    .has("__.out.label", "用户")
                    .has("__.in.label", "文档")
                    .groupCount().by("geoip.city_name"));

            System.out.println("Spend: " + (System.currentTimeMillis() - startTime)+"ms");
            System.out.println("end.");
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
