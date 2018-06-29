import com.eisoo.rainbow.gremlin.structure.RainbowGraph
import org.apache.tinkerpop.gremlin.process.traversal.Order
import org.apache.tinkerpop.gremlin.process.traversal.P
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource
import org.apache.tinkerpop.gremlin.structure.T
import org.json.JSONObject
import spock.lang.*
import ElasticHelper

class GremlinPathUnicodeTest extends Specification {
    static GraphTraversalSource g

    def setupSpec() {
        given:
        ElasticHelper.teardownData("rainbow-path-unicode")

        List<String> data = [
                '{ "@timestamp":"2018-03-02T07:56:15.000Z", "系统账号":"xavier.chen", "操作动作":"复制", "操作前对象":"aaa.txt", "操作后对象":"bbb.txt"}',
                '{ "@timestamp":"2018-03-02T07:56:15.000Z", "系统账号":"xavier.chen", "操作动作":"下载", "操作前对象":"aaa.txt", "操作后对象":"\\u0003"}',
                '{ "@timestamp":"2018-03-02T07:56:15.000Z", "系统账号":"xavier.chen", "操作动作":"复制", "操作前对象":"aaa.txt", "操作后对象":null}',
                '{ "@timestamp":"2018-03-02T07:56:15.000Z", "系统账号":"xavier.chen", "操作动作":"预览", "操作前对象":"aaa.txt", "操作后对象":"\\u0003\\u000b"}',
                '{ "@timestamp":"2018-03-02T08:56:15.000Z", "系统账号":"\\u0003", "操作动作":"重命名", "操作前对象":"aaa.txt", "操作后对象":"\\u000b"}',
                '{ "@timestamp":"2018-03-02T08:56:15.000Z", "系统账号":"\\u0003", "操作动作":"复制", "操作前对象":"aaa.txt", "操作后对象":"directory created:\\\"D:\\\\1\\\\123123 - 副本 (55)\\\""}',
                '{ "@timestamp":"2018-03-02T08:56:15.000Z", "系统账号":"\\u0003", "操作动作":"复制", "操作前对象":"aaa.txt", "操作后对象":"directory created:D:\\\\1\\\\123123 - 副本 (55)"}',
                '{ "@timestamp":"2018-03-02T08:56:15.000Z", "系统账号":"\\u0003", "操作动作":"复制", "操作前对象":"aaa.txt", "操作后对象":"directory\\\\ created:\\\"D:\\\\1\\\\123123 - 副本 (55)\\\""}',
                '{ "@timestamp":"2018-03-02T08:56:15.000Z", "系统账号":"xavier.chen", "操作动作":"复制", "操作前对象":"D:\\\\1\\\\123\\\\dd.txt", "操作后对象":"D:\\\\1\\\\123\\\\cc.txt"}',
                '{ "@timestamp":"2018-03-02T09:56:15.000Z", "系统账号":"xavier.chen", "操作动作":"新建", "操作前对象":"D:\\\\1\\\\123\\\\ee.txt", "操作后对象":"D:\\\\1\\\\123\\\\新建文档.txt"}'
        ]
        ElasticHelper.setupData("GremlinSimpleTest", "rainbow-path-unicode", data)

        String graphSchema = """
{
    "vertices": [
        { "id": "系统账号", "label": "系统账号", "properties": ["@timestamp", "系统账号"] },
        { "id": "操作前对象", "label": "操作前对象", "properties": ["@timestamp", "操作前对象"] },
        { "id": "操作后对象", "label": "操作后对象", "properties": ["@timestamp", "操作后对象"] },
    ],
    "edges": [
      {
        "id": "_id", "label": "关系0", "properties": ["_id", "@timestamp","系统账号","操作动作"],
        "outVertex": { "id": "系统账号", "label": "系统账号" },
        "inVertex": { "id": "操作前对象", "label": "操作前对象" }
      },
      {
        "id": "_id", "label": "关系1", "properties": ["_id", "@timestamp","操作后对象","操作动作"],
        "outVertex": { "id": "系统账号", "label": "系统账号" },
        "inVertex": { "id": "操作后对象", "label": "操作后对象" }
      },
      {
        "id": "_id", "label": "关系2", "properties": ["_id", "@timestamp","操作前对象","操作动作"],
        "outVertex": { "id": "操作前对象", "label": "操作前对象" },
        "inVertex": { "id": "操作后对象", "label": "操作后对象" }
      }
    ]
}
"""
        def confJson = ElasticHelper.getRainbowConf("rainbow-path-unicode")
        confJson.put("graphSchema", new JSONObject(graphSchema))
        g = RainbowGraph.open(confJson.toString()).traversal()
    }

    def teardownSpec() {
        ElasticHelper.teardownData()
    }

    def "test RainbowGraphStep g.V"() {
        expect: "all vertices"
        g.V().toList().size() == 29

        and: "vertex id can not fuzzy matching"
        g.V("x").toList().size() == 0
        g.V("*").toList().size() == 0
        g.V("*.txt").toList().size() == 0

        and: "vertex in single schema"
        g.V().hasId("xavier.chen").toList().size() == 6
        g.V().hasId("D:\\1\\123\\cc.txt").toList().size() == 1
        g.V().hasLabel("系统账号").hasId("\u0003").toList().size() == 4
        g.V().has("__.id.field", "系统账号").hasId("\u0003").toList().size() == 4
        g.V().has("__.id.field", "操作后对象").hasId("\u0003").toList().size() == 1

        and: "vertex in multi schemas"
        g.V().hasId("\u0003").toList().size() == 5

        and: "query vertices by multi ids"
        g.V(['xavier.chen', '\u0003']).toList().size() == 11
        g.V().hasId("\u0003", "D:\\1\\123\\cc.txt").toList().size() == 6
    }

    def "test RainbowGraphStep g.V.hasLabel"() {
        expect: "label can not fuzzy matching"
        g.V().hasLabel("系统").toList().size() == 0
        g.V().hasLabel("前").toList().size() == 0
        g.V().hasLabel("后").toList().size() == 0

        and: "label for singel schema"
        g.V().hasLabel("系统账号").toList().size() == 10
    }

    def "test RainbowGraphStep g.E.hasLabel"() {
        expect: "label can not fuzzy matching"
        g.E().hasLabel("关系").toList().size() == 0
        g.E().hasLabel("0").toList().size() == 0
        g.E().hasLabel("*").toList().size() == 0

        and: "label for single schema"
        g.E().hasLabel("关系0").toList().size() == 10
        g.E().hasLabel("关系1").toList().size() == 9
        g.E().hasLabel("关系2").toList().size() == 9
    }

    def "test RainbowGraphStep g.V.hasKey"() {
        expect: "key can not fuzzy matching"
        g.V().hasKey("*").toList().size() == 0
        g.V().hasKey("系统").toList().size() == 0
        g.V().hasKey("对象").toList().size() == 0

        and: "key in single schema"
        g.V().hasKey("系统账号").toList().size() == 10
        g.V().hasKey("操作前对象").toList().size() == 10
        g.V().hasKey("操作后对象").toList().size() == 9

        and: "key in multi schemas"
        g.V().hasKey("@timestamp").toList().size() == 29
    }

    def "test RainbowGraphStep g.E.hasKey"() {
        expect: "key can not fuzzy matching"
        g.E().hasKey("*").toList().size() == 0
        g.E().hasKey("系统").toList().size() == 0
        g.E().hasKey("对象").toList().size() == 0

        and: "key in single schema"
        g.E().hasKey("系统账号").toList().size() == 10
        g.E().hasKey("操作前对象").toList().size() == 9

        and: "key in multi schemas"
        g.E().hasKey("_id").toList().size() == 28
        g.E().hasKey("@timestamp").toList().size() == 28
        g.E().hasKey("操作动作").toList().size() == 28
    }

    def "test RainbowGraphStep g.V.has >> "() {
        expect: "property value can fuzzy matching"
        g.V().has("系统账号", "*").toList().size() == 10
        g.V().has("系统账号", "x*").toList().size() == 6


        and: "unicode value can not fuzzy matching and fulltext matching"
        g.V().has("系统账号", "u*").toList().size() == 0
        g.V().has("系统账号", "\u0003").toList().size() == 0

        and: "path value can fuzzy matching but can not fulltext matching"
        g.V().has("操作后对象", "c*").toList().size() == 4
        g.V().has("操作后对象", "d*").toList().size() == 5
        g.V().has("操作后对象", "D:\\1\\123\\cc.txt").toList().size() == 0
    }

    @Unroll
    def "test RainbowGraphStep g.V.has >> P.eq"() {
        expect:
        g.V().has(propertyKey, P.eq(value)).toList().size() == expectedSize

        where:
        propertyKey | value                                                           || expectedSize
        "系统账号"      | "\"xavier.chen\""                                           || 6
        "系统账号"      | "\"\\u0003\""                                               || 4
        "操作前对象"     | "\"aaa.txt\""                                               || 8
        "操作后对象"     | "\"bbb.txt\""                                               || 1
        "操作后对象"     | "\"directory created:\\\"D:\\\\1\\\\123123 - 副本 (55)\\\"\"" || 3
        "操作后对象"     | "\"directory created:D:\\\\1\\\\123123 - 副本 (55)\""         || 3
    }

    @Unroll
    def "test RainbowGraphStep g.V().has >> P.neq"() {
        expect:
        g.V().has(propertyKey, P.neq(value)).toList().size() == expectedSize

        where:
        propertyKey | value                                                       || expectedSize
        "系统账号"      | "\"xavier.chen\""                                           || 4
        "系统账号"      | "\"\\u0003\""                                               || 6
        "操作前对象"     | "\"aaa.txt\""                                               || 2
        "操作后对象"     | "\"bbb.txt\""                                               || 9
        "操作后对象"     | "\"directory created:\\\"D:\\\\1\\\\123123 - 副本 (55)\\\"\"" || 7
        "操作后对象"     | "\"directory created:D:\\\\1\\\\123123 - 副本 (55)\""         || 7
    }

    @Unroll
    def "test RainbowGraphStep g.V.has >> P.within"() {
        expect: "whinin is keyword and parse matching"
        g.V().has(propertyKey, P.within(value)).toList().size() == expectedSize

        where:
        propertyKey | value                                                                                         || expectedSize
        "系统账号"      | ["\"xavier.chen\""]                                                                           || 6
        "系统账号"      | ["\"xavier.chen\"", "\"\\u0003\""]                                                            || 10
        "操作前对象"     | ["\"aaa.txt\"", "\"D:\\\\1\\\\123\\\\dd.txt\""]                                               || 9
        "操作后对象"     | ["\"directory created:\\\"D:\\\\1\\\\123123 - 副本 (55)\\\"\"", "\"D:\\\\1\\\\123\\\\cc.txt\""] || 2
    }

    @Unroll
    def "test RainbowGraphStep g.V.has >> P.without"() {
        expect:
        g.V().has(propertyKey, P.without(value)).toList().size() == expectedSize

        where:
        propertyKey | value                                                                                         || expectedSize
        "系统账号"      | ["\"xavier.chen\""]                                                                           || 4
        "系统账号"      | ["\"xavier.chen\"", "\"\\u0003\""]                                                            || 0
        "操作前对象"     | ["\"aaa.txt\"", "\"D:\\\\1\\\\123\\\\dd.txt\""]                                               || 1
        "操作后对象"     | ["\"directory created:\\\"D:\\\\1\\\\123123 - 副本 (55)\\\"\"", "\"D:\\\\1\\\\123\\\\cc.txt\""] || 8
    }

    def "test RainbowGraphStep g.V.has >> P.between"(){}

    def "test RainbowGraphStep g.V.has >> P.inside"(){}

    def "test RainbowGraphStep g.V.has >> P.outside"(){}

    def "test RainbowGraphStep g.V.dedup"() {
        expect:
        g.V().dedup().toList().size() == 14

        and: "dedup for single schema"
        g.V().hasLabel("系统账号").dedup().toList().size() == 2

        and: "dedup for multi schema"
        g.V().hasKey("@timestamp").dedup().toList().size() == 14
    }

    def "test RainbowGraphStep g.V.limit"() {
        expect: "limit for single schema"
        g.V().hasLabel("系统账号").limit(3).toList().size() == 3

        and: "limit for multi schemas"
        g.V().limit(3).toList().size() == 9
    }

    def "test RainbowGraphStep g.V.both"() {
        expect: "both for single schema"
        g.V("D:\\1\\123\\dd.txt").in().toList().size() == 1

        and: "both for multi shcema"
        g.V("D:\\1\\123\\dd.txt").both().toList().size() == 2
        g.V("\u0003").both().toList().size() == 10
        g.V("directory created:\"D:\\1\\123123 - 副本 (55)\"").both().toList().size() == 2
        g.V("directory\\ created:\"D:\\1\\123123 - 副本 (55)\"").both().toList().size() == 2
        g.V("directory created:D:\\1\\123123 - 副本 (55)").both().toList().size() == 2
    }

    def "test RainbowGraphStep g.V.values"() {
        expect: "values for single schema"
        g.V().hasLabel("系统账号").values("号").toList().size() == 0
        g.V().hasLabel("系统账号").values("系统账号").toList().size() == 10

        and: "values for multi schema"
        g.V().values("@timestamp").toList().size() == 29

    }

    def "test RainbowGraphStep g.V.range"(){
        expect: "range for single schema"
        g.V().hasLabel("系统账号").range(0,2).toList().size() == 2

        and: "range for multi schema"
        g.V().range(0,2).toList().size() == 6
    }

    def "test RainbowGraphStep g.E.range"() {
        expect: "range for single schema"
        g.E().hasLabel("关系0").range(0,2).toList().size() == 2

        and: "range for multi schema"
        g.E().range(0,2).toList().size() == 6
    }

    def "test startVertex filter query"(){
        expect:
        g.V().has("系统账号",P.eq("\"xavier.chen\"")).toList().size() == 6
        g.V().has("系统账号",P.eq("\"\\u0003\"")).toList().size() == 4
        g.V().has("操作前对象",P.eq("\"aaa.txt\"")).toList().size() == 8
        g.V().has("操作后对象",P.eq("\"\\u0003\\u000b\"")).toList().size() == 1
        g.V().has("操作后对象",P.eq("\"directory created:\\\"D:\\\\1\\\\123123 - 副本 (55)\\\"\"")).toList().size() == 3
        g.V().has("操作后对象",P.eq("\"directory\\\\ created:\\\"D:\\\\1\\\\123123 - 副本 (55)\\\"\"")).toList().size() == 3
        g.V().has("操作后对象",P.eq("\"directory created:D:\\\\1\\\\123123 - 副本 (55)\"")).toList().size() == 3

        g.V().has("系统账号",P.neq("\"xavier.chen\"")).toList().size() == 4
        g.V().has("系统账号",P.neq("\"\\u0003\"")).toList().size() == 6

        g.V().has("系统账号",P.within("\"xavier.chen\"","\"\\u0003\"")).toList().size() == 10
        g.V().has("操作后对象",P.within("\"directory created:\\\"D:\\\\1\\\\123123 - 副本 (55)\\\"\"")).toList().size() == 1
        g.V().has("操作后对象",P.within("\"directory\\\\ created:\\\"D:\\\\1\\\\123123 - 副本 (55)\\\"\"")).toList().size() == 1
        g.V().has("操作后对象",P.within("\"directory created:D:\\\\1\\\\123123 - 副本 (55)\"")).toList().size() == 1
        g.V().has("操作后对象",P.within("\"directory created:D:\\\\1\\\\123123 - 副本 (55)\"", "\"\\u0003\"")).toList().size() == 2
        g.V().has("操作后对象",P.within("\"directory created:\\\"D:\\\\1\\\\123123 - 副本 (55)\\\"\"","\"directory\\\\ created:\\\"D:\\\\1\\\\123123 - 副本 (55)\\\"\"")).toList().size() == 2

        g.V().has("系统账号",P.without("\"xavier.chen\"","\"\\u0003\"")).toList().size() == 0
        g.V().has("操作后对象",P.without("\"directory created:\\\"D:\\\\1\\\\123123 - 副本 (55)\\\"\"")).toList().size() == 9
    }

    def "test vertex Search query"() {
        expect:"search property value can fuzzy matching"
        g.V().has("系统账号",P.eq("xavier")).toList().size() == 6
        g.V().has("系统账号",P.eq("x*")).toList().size() == 6
        g.V().has("操作后对象",P.eq("d")).toList().size() == 5
        g.V().has("操作后对象",P.eq("d*")).toList().size() == 5

        g.V().has("操作后对象",P.eq("d*")).dedup().toList().size() == 5
        g.V().has("系统账号",P.eq("xavier")).dedup().toList().size() == 1
        g.V().has("系统账号",P.eq("xavier")).limit(3).toList().size() == 3
        g.V().has("系统账号",P.eq("xavier")).dedup().values("系统账号").toList().toString().equals("[xavier.chen]")
    }

    def "test vertex Neighbors query"() {
        expect:
        g.V("xavier.chen").both().toList().size() == 11
        g.V("xavier.chen").both().dedup().toList().size() == 8
        g.V("xavier.chen").out().toList().size() == 11
        g.V("xavier.chen").out().dedup().toList().size() == 8
        g.V("xavier.chen").in().toList().size() == 0
        g.V("xavier.chen").in().dedup().toList().size() == 0

        and: "unicode string can term matching"
        g.V("\u0003").both().toList().size() == 10
        g.V("\u0003").both().dedup().toList().size() == 6
        g.V("\u0003").out().toList().size() == 8
        g.V("\u0003").in().toList().size() == 2

        and: "path string can term matching"
        g.V("D:\\1\\123\\dd.txt").both().toList().size() == 2
        g.V("directory created:D:\\1\\123123 - 副本 (55)").both().toList().size() == 2
        g.V("directory created:\"D:\\1\\123123 - 副本 (55)\"").both().toList().size() == 2
        g.V("directory\\ created:\"D:\\1\\123123 - 副本 (55)\"").both().toList().size() == 2
    }

    def "test edge filter query"() {
        expect: "unicode and path string can term matching"
        g.E()
                .has("__.out.id","xavier.chen")
                .has("__.out.label","系统账号")
                .has("__.in.id","aaa.txt")
                .has("__.in.label","操作前对象").toList().size() == 4

        g.E()
                .has("__.out.id","xavier.chen")
                .has("__.out.label","系统账号")
                .has("__.in.id","\u0003")
                .has("__.in.label","操作后对象").toList().size() == 1

        g.E()
                .has("__.out.id","xavier.chen")
                .has("__.out.label","系统账号")
                .has("__.in.id","D:\\1\\123\\dd.txt")
                .has("__.in.label","操作前对象").toList().size() == 1

        g.E()
                .has("__.out.id","\u0003")
                .has("__.out.label","系统账号")
                .has("__.in.id","directory created:D:\\1\\123123 - 副本 (55)")
                .has("__.in.label","操作后对象").toList().size() == 1

        g.E()
                .has("__.out.id","D:\\1\\123\\ee.txt")
                .has("__.out.label","操作前对象")
                .has("__.in.id","D:\\1\\123\\新建文档.txt")
                .has("__.in.label","操作后对象").toList().size() == 1
    }

    def "test edgeGroupCountByLabel query"() {
        expect:
        g.E().has("操作动作","下载").groupCount().by(T.label).toList()[0].size() == 3
        g.E().has("操作动作","预览").groupCount().by(T.label).toList()[0].size() == 3
        g.E().has("操作动作","下载").groupCount().by(T.label).toList()[0].keySet().toString() =="[关系0, 关系1, 关系2]"
    }

    def "test edgeGroupCountByProperty query"() {
        expect:
        g.E().has("操作动作","复制").groupCount().by("@timestamp").toList()[0].size() == 2
        g.E().groupCount().by("操作动作").toList()[0].get("下载").toInteger() == 3
        g.E().hasLabel("关系0").groupCount().by("操作动作").toList()[0].get("下载").toInteger() == 1
        g.E().groupCount().by("操作动作").toList()[0].get("复制").toInteger() == 16
    }
}