import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import com.eisoo.rainbow.gremlin.structure.RainbowGraph;
import org.apache.tinkerpop.gremlin.structure.T;

import static org.apache.tinkerpop.gremlin.process.traversal.Order.decr;
import static org.apache.tinkerpop.gremlin.process.traversal.Order.incr;

public class ASPathUnicodeDemo {
    public static void main(String[] args) {
        try {
            System.out.println("hello");
            long startTime = System.currentTimeMillis();

            Configuration conf = new BaseConfiguration();

            String confPath = ClassLoader.getSystemResource("elastic-path-unicode.json").getPath();
            conf.addProperty("provider", confPath);
            RainbowGraph graph = RainbowGraph.open(conf);
            GraphTraversalSource g = graph.traversal();


            System.out.println("=============== 按接口去识别特殊字符(Unicode)及路径字符串 尽量测到各种情况 ===============");
            System.out.println("=============== vertex filter接口测试 ===============");
            PrintHelper.print_e(g.V().has("操作后对象", P.eq("\"directory created:\\\"D:\\\\1\\\\123123 - 副本 (55)\\\"\"")));
            PrintHelper.print_e(g.V().has("操作后对象", P.within("\"directory created:\\\"D:\\\\1\\\\123123 - 副本 (55)\\\"\"")));
            PrintHelper.print_e(g.V().has("操作后对象", P.within("\"directory\\\\ created:\\\"D:\\\\1\\\\123123 - 副本 (55)\\\"\"")));
            PrintHelper.print_e(g.V().has("操作后对象", P.eq("\"directory created:D:\\\\1\\\\123123 - 副本 (55)\"")));
            PrintHelper.print_e(g.V().has("操作后对象", P.eq("\"D:\\\\1\\\\123\\\\cc.txt\"")));
            PrintHelper.print_e(g.V().has("操作后对象", P.eq("\"directory created:\\\"D:\\\\1\\\\123123 - 副本 (55)\\\"\"")));
            PrintHelper.print_e(g.V().has("操作后对象", P.eq("\"directory created:D:\\\\1\\\\123123 - 副本 (55)\"")).dedup());

            PrintHelper.print_e(g.V().has("操作后对象", P.eq("\"directory\\\\ created:\\\"D:\\\\1\\\\123123 - 副本 (55)\\\"\"")));
            PrintHelper.print_e(g.V().has("系统账号", P.eq("\"\\u0003\"")));
            PrintHelper.print_e(g.V().has("__.id.field","系统账号").has("系统账号", P.eq("\"\\u0003\"")));
            PrintHelper.print_e(g.V().has("__.id.field","系统账号").has("系统账号", P.eq("\"\\u0003\"")));
            PrintHelper.print_e(g.V().has("操作后对象", P.eq("\"\\u0003\"")).dedup());
            PrintHelper.print_e(g.V().has("操作后对象", P.eq("\"\\u0003\\u000b\"")));
            PrintHelper.print_e(g.V().has("操作后对象", P.neq("\"\\u0003\"")));
            PrintHelper.print_e(g.V().has("系统账号", P.within("\"xavier.chen\"","\"\\u0003\"")));
            PrintHelper.print_e(g.V().has("操作后对象", P.within("\"D:\\\\1\\\\123\\\\cc.txt\"","\"\\u0003\"")));
//            PrintHelper.print_e(g.V().has("操作后对象", P.within("D:\\1\\123\\cc.txt"))); //不支持，在使用within查询文件路径类型的时候，需要使用短语匹配类型
            PrintHelper.print_e(g.V().has("操作后对象", P.within("\"D:\\\\1\\\\123\\\\cc.txt\"")));

            System.out.println("=============== g.V('XXX')采用的是term的精确值查询方式==================================");
            PrintHelper.print_e(g.V("xavier.chen"));
            PrintHelper.print_e(g.V("D:\\1\\123\\cc.txt"));
            PrintHelper.print_e(g.V("\u0003"));
            PrintHelper.print_e(g.V("xavier.chen").dedup());
            PrintHelper.print_e(g.V("directory created:\"D:\\1\\123123 - 副本 (55)\""));
            PrintHelper.print_e(g.V("directory\\ created:\"D:\\1\\123123 - 副本 (55)\""));

            System.out.println("=============== filter接口总结: 改进g.V().has(P.within)为短语匹配方法==================");

            System.out.println("=============== vertex search接口测试 ===============");
            PrintHelper.print_e(g.V().has("系统账号",P.eq("xavier")));
            PrintHelper.print_e(g.V().has("操作后对象",P.eq("d")));
            PrintHelper.print_e(g.V().has("操作后对象",P.eq("c")));
            PrintHelper.print_e(g.V().has("操作后对象",P.eq("d*")));
//            PrintHelper.print_e(g.V().has("操作后对象",P.eq("d:1")));//不支持这种方式查询，因为是全文检索方式，d:1找不到这种词元
            PrintHelper.print_e(g.V().has("系统账号",P.eq("x")));
            PrintHelper.print_e(g.V().has("系统账号",P.eq("x*")));
            PrintHelper.print_e(g.V().has("系统账号",P.eq("*")));
            PrintHelper.print_e(g.V().has("系统账号",P.eq("x*")).dedup());
            PrintHelper.print_e(g.V().has("系统账号",P.eq("x*")).limit(2));

            PrintHelper.print_v(g.V().has("系统账号",P.eq("x*")).values("系统账号"));
            PrintHelper.print_v(g.V().has("系统账号",P.eq("x*")).values("系统账号").dedup());
            PrintHelper.print_v(g.V().has("系统账号",P.eq("x*")).values("系统账号").limit(2));
            PrintHelper.print_v(g.V().has("操作后对象",P.eq("d*")).values("操作后对象"));
            System.out.println("=============== search接口总结: 对于路径字符串查询，不能采用d:\\1这种形式 ===============");


            System.out.println("=============== vertex neighbors接口测试 ===============");
            PrintHelper.print_e(g.V("xavier.chen").both());
            PrintHelper.print_e(g.V("xavier.chen").both().dedup());
            PrintHelper.print_e(g.V("xavier.chen").dedup().both().dedup());
            PrintHelper.print_e(g.V("xavier.chen").out().dedup());
            PrintHelper.print_e(g.V("xavier.chen").in().dedup());

            PrintHelper.print_e(g.V("\u0003").both());
            PrintHelper.print_e(g.V("\u0003").both().dedup());
            PrintHelper.print_e(g.V("\u0003").dedup().both().dedup());
            PrintHelper.print_e(g.V("\u0003\u000b").both());
            PrintHelper.print_e(g.V("\u0003\u000b").in());

            PrintHelper.print_e(g.V("D:\\1\\123\\cc.txt").both());
            PrintHelper.print_e(g.V("D:\\1\\123\\cc.txt").both().dedup());
            PrintHelper.print_e(g.V("D:\\1\\123\\cc.txt").dedup().both().dedup());
            PrintHelper.print_e(g.V("D:\\1\\123\\cc.txt").out());
            PrintHelper.print_e(g.V("D:\\1\\123\\新建文档.txt").in());
            System.out.println("=============== neighbors接口总结: 使用的是term精确查询方式 ===============");
            System.out.println("=============== edge filter接口测试 ===============");
            PrintHelper.print_e(g.E().hasLabel("关系0").has("__.out.id","xavier.chen"));
            PrintHelper.print_e(g.E().hasLabel("关系0").has("__.out.id","xavier.chen").has("操作动作","新建"));
            PrintHelper.print_e(g.E().hasLabel("关系0").has("__.out.id","xavier.chen").has("操作动作","复制"));

            PrintHelper.print_e(g.E().has("__.out.id","xavier.chen").has("__.out.label","系统账号"));
            PrintHelper.print_e(g.E()
                    .has("__.out.id","xavier.chen")
                    .has("__.out.label","系统账号")
                    .has("__.in.id","D:\\1\\123\\dd.txt")
                    .has("__.in.label","操作前对象"));

            PrintHelper.print_e(g.E()
                    .has("__.out.id","xavier.chen")
                    .has("__.out.label","系统账号"));

            PrintHelper.print_e(g.E()
                    .has("__.out.id","xavier.chen")
                    .has("__.out.label","系统账号")
                    .has("操作动作","新建")
            );

            PrintHelper.print_e(g.E().has("__.out.id","\u0003").has("__.out.label","系统账号"));
            PrintHelper.print_e(g.E()
                    .has("__.out.id","\u0003")
                    .has("__.out.label","系统账号")
                    .has("__.in.id","\u000b")
                    .has("__.in.label","操作后对象"));
            PrintHelper.print_e(g.E()
                    .has("__.out.id","\u0003")
                    .has("__.out.label","系统账号")
                    .has("__.in.id","directory created:\"D:\\1\\123123 - 副本 (55)\"")
                    .has("__.in.label","操作后对象"));
            PrintHelper.print_e(g.E()
                    .has("__.out.id","xavier.chen")
                    .has("__.out.label","系统账号")
                    .order().by("@timestamp",decr));
            PrintHelper.print_e(g.E()
                    .has("__.out.id","xavier.chen")
                    .has("__.out.label","系统账号")
                    .order().by("@timestamp",incr));
            PrintHelper.print_e(g.E()
                    .has("__.out.id","xavier.chen")
                    .has("__.out.label","系统账号")
                    .order().by("@timestamp",incr)
                    .range(0,5));
            System.out.println("=============== edge filter接口总结: has使用的是term精确查询，且可以按照属性选择升序或者降序 ===============");
            System.out.println("=============== edge groupByLabel接口测试 ===============");
            PrintHelper.print_c(g.E()
                .has("__.out.id","xavier.chen")
                .has("__.out.label","系统账号")
                .has("__.in.id","aaa.txt")
                .has("__.in.label","操作前对象")
                .groupCount().by(T.label));
            PrintHelper.print_c(g.E()
                    .has("__.out.id","aaa.txt")
                    .has("__.out.label","操作前对象")
                    .has("__.in.id","\u0003")
                    .has("__.in.label","操作后对象")
                    .groupCount().by(T.label));
            PrintHelper.print_c(g.E()
                    .has("__.out.id","aaa.txt")
                    .has("__.out.label","操作前对象")
                    .has("__.in.id","directory created:\"D:\\1\\123123 - 副本 (55)\"")
                    .has("__.in.label","操作后对象")
                    .groupCount().by(T.label));
            PrintHelper.print_e(g.E()
                    .has("__.out.id","D:\\1\\123\\dd.txt")
                    .has("__.out.label","操作前对象")
                    .has("__.in.id","D:\\1\\123\\cc.txt")
                    .has("__.in.label","操作后对象"));
            System.out.println("=============== edge groupByProperty接口测试 ===============");
            PrintHelper.print_c(g.E()
                    .has("__.out.id","xavier.chen")
                    .has("__.out.label","系统账号")
                    .has("__.in.id","aaa.txt")
                    .has("__.in.label","操作前对象")
                    .groupCount().by("操作动作"));
            PrintHelper.print_c(g.E()
                    .has("__.out.id","xavier.chen")
                    .has("__.out.label","系统账号")
                    .has("__.in.id","aaa.txt")
                    .has("__.in.label","操作前对象")
                    .groupCount().by("@timestamp"));
            PrintHelper.print_c(g.E()
                    .has("__.out.id","xavier.chen")
                    .has("__.out.label","系统账号")
                    .has("__.in.id","aaa.txt")
                    .has("__.in.label","操作前对象")
                    .groupCount().by("_id"));
            PrintHelper.print_c(g.E()
                    .has("__.out.id","D:\\1\\123\\dd.txt")
                    .has("__.out.label","操作前对象")
                    .has("__.in.id","D:\\1\\123\\cc.txt")
                    .has("__.in.label","操作后对象")
                    .groupCount().by("_id"));

        } catch (Exception e) {
        }
    }
}