import com.eisoo.rainbow.gremlin.structure.RainbowGraph
import org.apache.tinkerpop.gremlin.process.traversal.Order
import org.apache.tinkerpop.gremlin.process.traversal.P
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource
import org.apache.tinkerpop.gremlin.structure.T
import org.json.JSONObject
import spock.lang.*
import ElasticHelper

/**
 * @author bao.zengkai@eisoo.com
 * @author tong.sha@eisoo.com
 * @version 1.0 , Copyright (c) 2014 AnyRobot, EISOO
 * @date 2018.04.28
 */

class GremlinSimpleTest extends Specification {
    static GraphTraversalSource g

    def setupSpec() {
        given:
        ElasticHelper.teardownData("rainbow-ut")

        List<String> data = [
                '{ "@timestamp":"2018-03-02T07:56:15.000Z", "用户":"张一", "操作动作":"创建", "操作对象":"文档1.docx", "操作后对象":null}',
                '{ "@timestamp":"2018-03-02T08:56:15.000Z", "用户":"张二", "操作动作":"下载", "操作对象":"文档1.docx", "操作后对象":null}',
                '{ "@timestamp":"2018-03-02T09:56:15.000Z", "用户":"张一", "操作动作":"重命名", "操作对象":"文档1.docx", "操作后对象":"文档2.docx"}',
                '{ "@timestamp":"2018-03-02T10:56:15.000Z", "用户":"张三", "操作动作":"下载", "操作对象":"文档2.docx", "操作后对象":null}',

                '{ "@timestamp":"2018-03-03T11:56:15.000Z", "用户":"张一", "操作动作":"创建", "操作对象":"文档3.docx", "操作后对象":null}',
                '{ "@timestamp":"2018-03-03T12:56:15.000Z", "用户":"张三", "操作动作":"预览", "操作对象":"文档3.docx", "操作后对象":null}',
                '{ "@timestamp":"2018-03-03T13:56:15.000Z", "用户":"张四", "操作动作":"复制", "操作对象":"文档3.docx", "操作后对象":"文档4.docx"}',
                '{ "@timestamp":"2018-03-03T14:56:15.000Z", "用户":"张四四", "操作动作":"复制", "操作对象":"文档3.docx", "操作后对象":"文档5.docx"}'
        ]

        ElasticHelper.setupData("GremlinSimpleTest","rainbow-ut", data)

        String graphSchema = """
{
    "vertices": [
        { "id": "用户", "label": "用户", "properties": ["@timestamp", "用户"] },
        { "id": "操作对象", "label": "文档", "properties": ["@timestamp", "操作对象"] },
        { "id": "操作后对象", "label": "文档", "properties": ["@timestamp", "操作后对象"] }
    ],
    "edges": [
      {
        "id": "_id", "label": "操作", "properties": ["_id", "@timestamp", "操作动作"],
        "outVertex": { "id": "用户", "label": "用户" },
        "inVertex": { "id": "操作对象", "label": "文档" }
      },
      {
        "id": "_id", "label": "操作后", "properties": ["_id", "@timestamp", "操作动作", "操作对象"],
        "outVertex": { "id": "用户", "label": "用户" },
        "inVertex": { "id": "操作后对象", "label": "文档" }
      },
      {
        "id": "_id", "label": "被操作为", "properties": ["_id", "@timestamp", "操作动作", "用户"],
        "outVertex": { "id": "操作对象", "label": "文档" },
        "inVertex": { "id": "操作后对象", "label": "文档" }
      }
    ]
}
"""
        def confJson = ElasticHelper.getRainbowConf("rainbow-ut")
        confJson.put("graphSchema", new JSONObject(graphSchema))
        g = RainbowGraph.open(confJson.toString()).traversal()
    }

    def teardownSpec() {
        ElasticHelper.teardownData()
    }

    def "test RainbowGraphStep g.V.hasId"() {
        expect: "all vertices"
        g.V().toList().size() == 19

        and: "vertex id can not fuzzy matching"
        g.V("张").toList().size() == 0
        g.V("*").toList().size() == 0
        g.V("*.docx").toList().size() == 0
        g.V().hasId("张四").toList().size() == 1
        g.V().hasId("文档").toList().size() == 0

        and: "vertices in single schema"
        g.V("张一").toList().size() == 3
        g.V().hasId("文档1.docx").toList().size() == 3
        g.V().hasLabel("用户").hasId("张二").toList().size() == 1
        g.V().hasLabel("文档").hasId("文档3.docx").toList().size() == 4
        g.V().has("__.id.field", "操作对象").hasId("文档2.docx").toList().size() == 1
        g.V().has("__.id.field", "操作后对象").hasId("文档2.docx").toList().size() == 1

        and: "vertices in multi schemas"
        g.V().hasId("文档2.docx").toList().size() == 2
        g.V().hasLabel("文档").hasId("文档3.docx", "文档4.docx", "文档5.docx").toList().size() == 6

        and: "query vertices by multi ids"
        g.V(["张一", "张四"]).toList().size() == 4
        g.V().hasId("文档1.docx", "文档2.docx", "文档3.docx", "文档4.docx", "文档5.docx").toList().size() == 11
    }

    def "test RainbowGraphStep g.E.hasId"() {
        given:
        List<String> edgeIdList = g.E().hasLabel("操作").values("_id").toList()
        List<String> edgeIdListOfSingleSchema = g.E().has("操作动作","预览").values("_id").toList()
        List<String> edgeIdListOfMultiSchema = g.E().has("操作动作","重命名").values("_id").toList()

        expect:
        edgeIdList.size() == 8

        and: "all edges"
        g.E().toList().size() == 14

        and: "edge id can not fuzzy matching"
        g.E(edgeIdList.get(0).substring(0, 5)).toList().size() == 0
        g.E("*").toList().size() == 0

        and: "edges in single schema"
        g.E(edgeIdListOfSingleSchema.get(0)).toList().size() == 1

        and: "edges in multi schemas"
        g.E(edgeIdListOfMultiSchema.get(0)).toList().size() == 3

        and: "query edges by multi ids"
        g.E(edgeIdList.get(7), edgeIdList.get(6), edgeIdList.get(5)).toList().size() == 5
    }

    def "test RainbowGraphStep g.V.hasLabel"() {
        expect: "label can not fuzzy matching"
        g.V().hasLabel("用").toList().size() == 0
        g.V().hasLabel("文").toList().size() == 0
        g.V().hasLabel("*").toList().size() == 0

        and: "label for single schema"
        g.V().hasLabel("用户").toList().size() == 8

        and: "label for multi schemas"
        g.V().hasLabel("文档").toList().size() == 11
    }

    def "test RainbowGraphStep g.E.hasLabel"() {
        expect: "label can not fuzzy matching"
        g.E().hasLabel("操作").toList().size() == 8
        g.E().hasLabel("操").toList().size() == 0
        g.E().hasLabel("*").toList().size() == 0

        and: "label for single schema"
        g.E().hasLabel("操作").toList().size() == 8
        g.E().hasLabel("操作后").toList().size() == 3
        g.E().hasLabel("被操作为").toList().size() == 3
    }

    def "test RainbowGraphStep g.V.hasKey"() {
        expect: "key can not fuzzy matching"
        g.V().hasKey("*").toList().size() == 0
        g.V().hasKey("用").toList().size() == 0
        g.V().hasKey("操作").toList().size() == 0

        and: "key in single schema"
        g.V().hasKey("用户").toList().size() == 8
        g.V().hasKey("操作对象").toList().size() == 8
        g.V().hasKey("操作后对象").toList().size() == 3

        and: "key in multi schemas"
        g.V().hasKey("@timestamp").toList().size() == 19
    }

    def "test RainbowGraphStep g.E.hasKey"() {
        expect: "key can not fuzzy matching"
        g.E().hasKey("*").toList().size() == 0
        g.E().hasKey("用").toList().size() == 0
        g.E().hasKey("操作").toList().size() == 0

        and: "key in single schema"
        g.E().hasKey("用户").toList().size() == 3
        g.E().hasKey("操作对象").toList().size() == 3

        and: "key in multi schemas"
        g.E().hasKey("操作动作").toList().size() == 14
        g.E().hasKey("_id").toList().size() == 14
        g.E().hasKey("@timestamp").toList().size() == 14
    }

    @Unroll
    def "test RainbowGraphStep g.V.has >> property value can fuzzy matching"() {
        expect:
        g.V().has(propertyKey, value).toList().size() == expectedSize

        where:
        propertyKey | value || expectedSize
        "用户" | "*" || 8
        "用户" | "张" || 6
        "用户" | "张*" || 8
        "用户" | "张四" || 1
        "用户" | "张四四" || 1
        "操作对象" | "*" || 8
        "操作对象" | "文档" || 8
        "操作对象" | "docx" || 8
        "操作对象" | "文档1" || 0
//        "操作对象" | "*.docx" || 8
//        "操作对象" | "文档*" || 8
    }

    @Unroll
    def "test RainbowGraphStep g.V.has >> property key in single schema"() {
        expect:
        g.V().has(propertyKey, value).toList().size() == expectedSize

        where:
        propertyKey | value || expectedSize
        "用户" | "张一" || 3
        "操作对象" | "文档1.docx" || 3
        "操作后对象" | "文档2.docx" || 1
    }

    def "test RainbowGraphStep g.V.has >> property key in multi schemas"() {
        expect:
        g.V().has("@timestamp", "2018-03-02T07\\:56\\:15.000Z").toList().size() == 2
    }

    @Unroll
    def "test RainbowGraphStep g.V.has >> P.eq"() {
        expect:
        g.V().has(propertyKey, P.eq(value)).toList().size() == expectedSize

        where:
        propertyKey | value || expectedSize
        "用户" | "*" || 8
        "用户" | "张" || 6
        "用户" | "张一" || 3
        "操作对象" | "文档1.docx" || 3
        "操作后对象" | "文档2.docx" || 1
    }

    @Unroll
    def "test RainbowGraphStep g.V.has >> P.neq"() {
        expect:
        g.V().has(propertyKey, P.neq(value)).toList().size() == expectedSize

        where:
        propertyKey | value || expectedSize
        "用户" | "*" || 0
        "用户" | "张" || 2
        "用户" | "张一" || 5
        "操作对象" | "文档1.docx" || 5
        "操作后对象" | "文档2.docx" || 7
    }

    @Unroll
    def "test RainbowGraphStep g.V.has >> P.within"() {
        expect:
        g.V().has(propertyKey, P.within(value)).toList().size() == expectedSize

        where:
        propertyKey | value || expectedSize
        "用户" | ["*", "张三"] || 8
        "用户" | ["张一", "王五"] || 3
        "用户" | ["张一", "张二"] || 4
        "操作对象" | ["文档1.docx", "文档2.docx"] || 4
        "操作后对象" | ["文档1.docx", "文档2.docx"] || 1
    }

    @Unroll
    def "test RainbowGraphStep g.V.has >> P.without"() {
        expect:
        g.V().has(propertyKey, P.without(value)).toList().size() == expectedSize

        where:
        propertyKey | value || expectedSize
        "用户" | ["*", "张三"] || 0
        "用户" | ["张一", "王五"] || 5
        "用户" | ["张一", "张二", "张三"] || 2
        "操作对象" | ["文档1.docx", "文档2.docx"] || 4
        "操作后对象" | ["文档1.docx", "文档2.docx"] || 7
    }

    @Unroll
    def "test RainbowGraphStep g.V.has >> P.between"(){
        expect: ""
        g.V().has(propertyKey, P.between(startTime,endTime)).toList().size() == expectSize

        where:
        propertyKey | startTime | endTime | expectSize
        "@timestamp" | "2017-01-01" | "2018-01-01" | 0
        "@timestamp" | "2017-01-01" | "2019-01-01" | 19
        "@timestamp" | "2017-01-01" | "2018-03-02" | 0
        "@timestamp" | "2018-03-02" | "2018-03-03" | 9
        "@timestamp" | "2018-03-02" | "2018-03-04" | 19
    }

    @Unroll
    def "test RainbowGraphStep g.V.has >> P.inside"(){
        expect:
        g.V().has(propertyKey, P.inside(startTime,endTime)).toList().size() == expectSize

        where:
        propertyKey | startTime | endTime | expectSize
        "@timestamp" | "2017-01-01" | "2018-01-01" | 0
        "@timestamp" | "2017-01-01" | "2019-01-01" | 19
        "@timestamp" | "2017-01-01" | "2018-03-02" | 0
        "@timestamp" | "2018-03-02" | "2018-03-03" | 0
        "@timestamp" | "2018-03-02" | "2018-03-04" | 10
    }

    @Unroll
    def "test RainbowGraphStep g.V.has >> P.outside"(){
        expect:
        g.V().has(propertyKey, P.outside(startTime,endTime)).toList().size() == expectSize

        where:
        propertyKey | startTime | endTime | expectSize
        "@timestamp" | "2017-01-01" | "2018-01-01" | 19
        "@timestamp" | "2017-01-01" | "2019-01-01" | 0
        "@timestamp" | "2017-01-01" | "2018-03-02" | 10
        "@timestamp" | "2018-03-02" | "2018-03-03" | 0
        "@timestamp" | "2018-03-02" | "2018-03-04" | 0
    }

    def "test RainbowGraphStep g.V.dedup"(){
        expect:
        g.V().dedup().toList().size() == 10

        and: "dedup for single schema"
        g.V().hasLabel("用户").dedup().toList().size() == 5

        and: "label for mutil schema"
        g.V().hasLabel("文档").dedup().toList().size() == 5
    }

    def "test RainbowGraphStep g.V.hasLabel.limit"(){
        expect: "dedup for single schema"
        g.V().hasLabel("用户").limit(5).toList().size() == 5

        and: "dedup for mutil schema"
        g.V().hasLabel("文档").limit(2).toList().size() == 4
    }


    def "test RainbowGraphStep g.V.both"(){
        expect: "both for single schema"
        g.V("张三").both().toList().size() == 2
        g.V("张三").out().toList().size() == 2
        g.V("张三").in().toList().size() == 0

        and: "both for mutil schema"
        g.V("张一").both().toList().size() == 4
        g.V("张一").dedup().both().toList().size() == 4
        g.V("张一").dedup().both().dedup().toList().size() == 3
        g.V("张一").out().toList().size() == 4
        g.V("张一").out().dedup().size() == 3
        g.V("张一").in().toList().size() == 0
    }

    def "test RainbowGraphStep g.V.has.values"(){
        expect: "values for single schema"
        g.V().hasLabel("用户").values("测试").toList().size() == 0
        g.V().hasLabel("用户").values("用户").toList().size() == 8

        and:"values for multi schema"
        g.V().hasLabel("文档").values("操作对象").toList().size() == 8
    }

    def "test RainbowGraphStep g.E.hasLabel.range"(){
        expect: "range for single schema"
        g.E().hasLabel("操作").range(0,4).toList().size() == 4

        and: "range for multi schema"
        g.E().has("操作动作").range(0,2).toList().size() == 6
    }

    def "test RainbowGraphStep g.E.order.by"(){
        expect:
        g.E().hasLabel("操作后").order().by("@timestamp", Order.incr).values("@timestamp").toList().toString().equals("[2018-03-02T09:56:15.000Z, 2018-03-03T13:56:15.000Z, 2018-03-03T14:56:15.000Z]")
        g.E().hasLabel("操作后").order().by("操作动作",Order.incr).values("操作动作").toList().toString().equals("[复制, 复制, 重命名]")
    }

    def "test RainbowGraphStep g.E.groupCount.by(T.label)"(){
        expect:
        g.E().groupCount().by(T.label).toList()[0].size() == 3
        g.E().groupCount().by(T.label).toList()[0].keySet().toString().equals("[操作, 被操作为, 操作后]")
    }

    def "test RainbowGraphStep g.E.groupCount.by(PropertyKey)"(){
        expect:
        g.E().groupCount().by("操作动作").toList()[0].size() == 5
        g.E().groupCount().by("操作动作").toList()[0].keySet().toString().equals("[重命名, 复制, 创建, 预览, 下载]")
    }

    def "test startVertices query script"() {
        expect:
        g.V().hasLabel("用户").has("用户", P.eq("张三")).dedup().toList().size() == 1
        g.V().hasLabel("用户").has("用户", P.eq("张 三")).dedup().toList().size() == 5
        g.V().hasLabel("用户").has("用户", P.eq("*")).dedup().toList().size() == 5
        g.V().hasLabel("用户").has("用户", P.eq("张*")).dedup().toList().size() == 5
        g.V().hasLabel("用户").has("用户",P.eq("\"张\"")).dedup().toList().size() == 4
        g.V().hasLabel("用户").has("用户", P.neq("张三")).dedup().toList().size() == 4
        g.V().hasLabel("用户").has("用户", P.within(["张一", "张二"])).dedup().toList().size() == 2
        g.V().hasLabel("用户").has("用户", P.without(["张一", "张二"])).dedup().toList().size() == 3
        g.V().hasLabel("用户").has("@timestamp", P.between("2017-01-01", "2018-03-02")).dedup().toList().size() == 0
        g.V().hasLabel("用户").has("@timestamp", P.inside("2017-01-01", "2018-03-02")).dedup().toList().size() == 0
        g.V().hasLabel("用户").has("@timestamp", P.outside("2017-01-01", "2018-03-02")).dedup().toList().size() == 4

        and:
        g.V().hasLabel("用户").hasKey("@timestamp").dedup().size() == 5
        g.V().hasLabel("用户").hasKey("@timestamp").has("用户","张一").dedup().size() == 1
    }

    def "test verticesSearch query script"() {
        expect:
        g.V().has("用户", P.eq("张")).values("用户").dedup().toList().size() == 4
        g.V().has("用户", P.eq("张")).dedup().values("用户").toList().size() == 4
        g.V().has("用户", P.eq("张")).values("@timestamp").dedup().toList().size() == 6
        g.V().has("用户", P.eq("张")).values("用户").dedup().limit(3).toList().size() == 3
    }

    def "test verticesNeighbors query script"(){
        expect:
        g.V("张一").hasLabel("用户").both().dedup().toList().size() == 3
        g.V("张一").hasLabel("用户").dedup().both().dedup().toList().size() == 3
        g.V("张一").hasLabel("用户").dedup().both().dedup().limit(1).toList().size() == 2
        g.V("张一").hasLabel("用户").dedup().out().dedup().toList().size() == 3
        g.V("张一").hasLabel("用户").dedup().in().dedup().toList().size() == 0
        g.V("文档1.docx").hasLabel("文档").dedup().in().dedup().toList().size() == 2
        g.V("张三").hasLabel("用户").dedup().out().dedup().limit(1).toList().size() == 1
        g.V("文档1.docx").hasLabel("文档").dedup().in().dedup().limit(1).toList().size() == 1

        and:
        g.V("文档1.docx").dedup().hasLabel("文档").bothE().toList().size() == 4
        g.V("文档1.docx").dedup().hasLabel("文档").outE().toList().size() == 1
        g.V("文档1.docx").dedup().hasLabel("文档").inE().toList().size() == 3
    }

    def "test edgesFilter query script"() {
        expect:
        g.E().has("操作动作", "下载").toList().size() == 2
        g.E().hasLabel("操作后").has("操作动作", "复制").has("操作对象", P.eq("文档3.docx")).toList().size() == 2
        g.E().hasLabel("操作后").has("操作动作", "复制").has("操作对象", P.eq("文档3.docx")).range(0, 1).toList().size() == 1
        g.E().hasLabel("操作").has("操作动作", "创建").order().by("@timestamp", Order.decr).values("@timestamp").toList().toString() == "[2018-03-03T11:56:15.000Z, 2018-03-02T07:56:15.000Z]"
        g.E().hasLabel("操作").hasKey("操作动作").order().by("操作动作", Order.decr).values("操作动作").toList().toString() == "[预览, 重命名, 复制, 复制, 创建, 创建, 下载, 下载]"
        g.E().hasLabel("操作").hasKey("操作动作").order().by("操作动作", Order.decr).by("@timestamp",Order.decr).values("操作动作").toList().toString() == "[复制, 复制, 预览, 创建, 下载, 重命名, 下载, 创建]"
    }

    def "test edgesGroupCountByLabel query script"() {
        expect:
        g.E().has("操作动作","复制").groupCount().by(T.label).toList()[0].size() == 3
        g.E().hasKey("操作动作").groupCount().by(T.label).toList()[0].keySet().toString() == "[操作, 被操作为, 操作后]"
    }

    def "test edgesGroupCountByProperty query script"(){
        expect:
        g.E().has("操作动作","创建").groupCount().by("@timestamp").toList()[0].size() == 2
        g.E().hasKey("操作动作").groupCount().by("操作动作").toList()[0].keySet().toString() == "[重命名, 复制, 创建, 预览, 下载]"
        g.E().hasLabel("操作").groupCount().by("操作动作").toList()[0].get("重命名").toString() == "1"
        g.E().hasLabel("操作").groupCount().by("操作动作").toList()[0].get("下载").toString() == "2"
        g.E().hasLabel("操作").groupCount().by("操作动作").toList()[0].get("复制").toString() == "2"
    }

    def "test RainbowGraphStep g.V.hasValue"() {
        when:
        g.V().hasValue("*").toList()
        then: "Not support operation for <g.V.hasValue> "
        thrown(UnsupportedOperationException)
    }

    def "test RainbowGraphStep g.E.hasValue"() {
        when:
        g.E().hasValue("*").toList()
        then: "Not support operation for <g.E.hasValue> "
        thrown(UnsupportedOperationException)
    }

    def "testRainbowGraphStep g.V.hasId.dedup.order.by"() {
        when:
        g.V().hasId("张一").dedup().order().by("用户").toList()
        then: "Not support sort for dedup query"
        thrown(UnsupportedOperationException)
    }

    def "testRainbowGraphStep g.V.dedup.values.dedup"(){
        when:
        g.V().dedup().values("用户").dedup().toList()
        then: "Not support operation for <g.V.dedup.values.dedup>"
        thrown(UnsupportedOperationException)
    }

    def "testRainbowGraphStep g.V.hasLabel.both.dedup.count"(){
        when:
        g.V("张一").hasLabel("用户").both().dedup().count().toList()
        then: "Count after dedup for multi schemas"
        thrown(UnsupportedOperationException)
    }

}
