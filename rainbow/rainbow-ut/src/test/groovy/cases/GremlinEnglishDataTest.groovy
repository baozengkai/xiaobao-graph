import com.eisoo.rainbow.gremlin.structure.RainbowGraph
import org.apache.tinkerpop.gremlin.process.traversal.Order
import org.apache.tinkerpop.gremlin.process.traversal.P
import org.apache.tinkerpop.gremlin.structure.T
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource
import org.json.JSONObject
import spock.lang.*
import ElasticHelper

/**
 * @author bao.zengkai@eisoo.com
 * @version 1.0 , Copyright (c) 2014 AnyRobot, EISOO
 * @date 2018.04.28
 */

class GremlinEnglishDataTest extends Specification {
    static GraphTraversalSource g

    def setupSpec() {
        given:
        ElasticHelper.teardownData("rainbow-en-ut")

        List<String> data = [
                '{ "@timestamp":"2018-03-02T07:35:21.000Z", "user":"userA", "operation action":"create", "operation object":"one.docx", "operation object id":"A1","after_operation object":null,"after_operation object id":null}',
                '{ "@timestamp":"2018-03-02T08:35:21.000Z", "user":"userB", "operation action":"download", "operation object":"one.docx", "operation object id":"A1","after_operation object":null,"after_operation object id":null}',
                '{ "@timestamp":"2018-03-02T09:35:21.000Z", "user":"userA", "operation action":"rename", "operation object":"one.docx","operation object id":"A1","after_operation object":"two.docx", "after_operation object id":"A2"}',
                '{ "@timestamp":"2018-03-02T10:35:21.000Z", "user":"userC", "operation action":"download", "operation object":"two.docx", "operation object id":"A2","after_operation object":null, "after_operation object id":null}',

                '{ "@timestamp":"2018-03-03T11:35:21.000Z", "user":"userA", "operation action":"create", "operation object":"three.docx","operation object id":"A3" ,"after_operation object":null,"after_operation object id":null}',
                '{ "@timestamp":"2018-03-03T12:35:21.000Z", "user":"userC", "operation action":"preview", "operation object":"three.docx", "operation object id":"A3","after_operation object":null,"after_operation object id":null}',
                '{ "@timestamp":"2018-03-03T13:35:21.000Z", "user":"userD", "operation action":"copy", "operation object":"three.docx", "operation object id":"A3","after_operation object":"four.docx","after_operation object id":"B4"}',
                '{ "@timestamp":"2018-03-03T14:35:21.000Z", "user":"userD D", "operation action":"copy", "operation object":"three.docx", "operation object id":"A3","after_operation object":"five docx","after_operation object id":"B5"}'
        ]

        ElasticHelper.setupData("GremlinEnglishDataTest","rainbow-en-ut", data)

        String graphSchema = """
        {
        "vertices": [
            { "id": "user", "label": "USER", "properties": ["@timestamp", "user"] },
            { "id": "operation object id", "label": "DOC", "properties": ["@timestamp", "operation object id", "operation object"] },
            { "id": "after_operation object id", "label": "DOC", "properties": ["@timestamp", "after_operation object id", "after_operation object"] }
        ],
        "edges": [
          {
            "id": "_id", "label": "operation", "properties": ["_id", "@timestamp", "operation action"],
            "outVertex": { "id": "user", "label": "USER" },
            "inVertex": { "id": "operation object id", "label": "DOC" }
          },
          {
            "id": "_id", "label": "after_operation", "properties": ["_id", "@timestamp", "operation action", "operation object"],
            "outVertex": { "id": "user", "label": "USER" },
            "inVertex": { "id": "after_operation object id", "label": "DOC" }
          },
          {
            "id": "_id", "label": "be_operated_to", "properties": ["_id", "@timestamp", "operation action", "user"],
            "outVertex": { "id": "operation object id", "label": "DOC" },
            "inVertex": { "id": "after_operation object id", "label": "DOC" }
          }
        ]
        }
"""
        def confJson = ElasticHelper.getRainbowConf("rainbow-en-ut")
        confJson.put("graphSchema", new JSONObject(graphSchema))
        g = RainbowGraph.open(confJson.toString()).traversal()
    }

    def teardownSpec() {
        ElasticHelper.teardownData()
    }

    def "test RainbowGraphStep g.V.hasId"(){
        expect: "all vertices"
        g.V().toList().size() == 19

        and: "vertex id can not fuzzy matching"
        g.V("*").toList().size() == 0
        g.V("user").toList().size() == 0
        g.V("user*").toList().size() == 0
        g.V("A*").toList().size() == 0
        g.V("userB").toList().size() == 1
        g.V().hasId("userB").toList().size() == 1
        g.V().hasId("A").toList().size() == 0

        and: "vertices in single schema"
        g.V().hasId("userA").toList().size() == 3
        g.V().hasId("A1").toList().size() == 3
        g.V().has("__.id.field","operation object id").hasId("A2").toList().size() == 1
        g.V().has("__.id.field","after_operation object id").hasId("A2").toList().size() == 1

        and: "vertices in multi schemas"
        g.V().hasId("A2").toList().size() == 2

        and: "query vertices by multi ids"
        g.V(["userA","userB"]).toList().size() == 4
    }

    def "test RainbowGraphStep g.E.hasId"(){
        given:
        List<String> edgeIdOfSingleSchemaList = g.E().has("operation action","preview").values("_id").toList()
        List<String> edgeIdOfMultiSchemasList = g.E().has("operation action","rename").values("_id").toList()

        expect: "all edges"
        g.E().toList().size() == 14

        and: "edge id can not fuzzy matching"
        g.E("*").toList().size() == 0

        and: "edges in single schema"
        g.E(edgeIdOfSingleSchemaList.get(0)).toList().size() == 1

        and: "edge in multi schemas"
        g.E(edgeIdOfMultiSchemasList.get(0)).toList().size() == 3

        and: "query edges by multi ids"
        g.E(edgeIdOfSingleSchemaList.get(0),edgeIdOfMultiSchemasList.get(0)).toList().size() == 4
    }

    def "test RainbowGraphStep g.V.hasLabel"(){
        expect: "label can not fuzzy matching"
        g.V().hasLabel("*").toList().size() == 0
        g.V().hasLabel("u").toList().size() == 0
        g.V().hasLabel("DO").toList().size() == 0

        and: "label for single schema"
        g.V().hasLabel("USER").toList().size() == 8

        and: "label for multi schemas"
        g.V().hasLabel("DOC").toList().size() == 11
    }

    def "test RainbowGraphStep g.E.hasLabel"(){
        expect: "label can not fuzzy matching"
        g.E().hasLabel("*").toList().size() == 0
        g.E().hasLabel("opera").toList().size() == 0
        g.E().hasLabel("operation objec").toList().size() == 0

        and: "label for single shcema"
        g.E().hasLabel("operation").toList().size() == 8
        g.E().hasLabel("after_operation").toList().size() == 3
        g.E().hasLabel("be_operated_to").toList().size() == 3

    }

    def "test RainbowGraphStep g.V.hasKey"(){
        expect: "key can not fuzzy matching"
        g.V().hasKey("*").toList().size() == 0
        g.V().hasKey("u").toList().size() == 0
        g.V().hasKey("opera").toList().size() == 0

        and: "key in single schema"
        g.V().hasKey("user").toList().size() == 8
        g.V().hasKey("operation object").toList().size() == 8
        g.V().hasKey("after_operation object").toList().size() == 3

        and: "key in multi schemas"
        g.V().hasKey("@timestamp").toList().size() == 19
    }

    def "test RainbowGraphStep g.E.hasKey"(){
        expect: "key can not fuzzy matching"
        g.E().hasKey("*").toList().size() == 0
        g.E().hasKey("u").toList().size() == 0
        g.E().hasKey("opera").toList().size() == 0

        and: "key in single schema"
        g.E().hasKey("user").toList().size() == 3
        g.E().hasKey("operation object").toList().size() == 3

        and: "key in multi schemas"
        g.E().hasKey("@timestamp").toList().size() == 14
        g.E().hasKey("operation action").toList().size() == 14
    }

    @Unroll
    def "test RainbowGraphStep g.V.has >> property value can fuzzy matching"(){
        expect:
        g.V().has(propertyKey, value).toList().size() ==expectSize

        where:
        propertyKey | value  || expectSize
        "user"      | "*"    || 8
        "user"      | "userA*" || 3
        "user"      | "userD" || 2
        "user"      | "userD D" || 2
        "user"      | "\"userD D\"" || 1
        "operation object" | "*" || 8
        "operation object" | "one" || 3
        "operation object" | "docx" || 8
        "operation object" | "one*" || 3
        "operation object" | "*docx" || 8
    }
    @Unroll
    def "test RainbowGraphStep g.V.has >> property key in single schema"() {
        expect:
        g.V().has(propertyKey, value).toList().size() == expectedSize

        where:
        propertyKey | value      || expectedSize
        "user"        | "userA"       || 3
        "operation object"      | "one.docx" || 3
        "after_operation object"     | "two.docx" || 1
    }

    def "test RainbowGraphStep g.V.has >> property key in multi schemas"(){
        expect:
        g.V().has("@timestamp","2018-03-02T09\\:35\\:21.000Z").toList().size() == 3
    }

    @Unroll
    def "test RainbowGraphStep g.V.has >> P.eq"() {
        expect:
        g.V().has(propertyKey, P.eq(value)).toList().size() == expectedSize

        where:
        propertyKey | value         || expectedSize
        "user"      | "*"           || 8
        "user"      | "u*"          || 8
        "user"      | "*A"          || 3
        "user"      | "userA"       || 3
        "user"      | "userD"       || 2
        "user"      | "*D"          || 2
        "user"      | "userD D"     || 2
        "user"      | "\"userD D\"" || 1
        "operation object" | "one.docx" || 3
        "after_operation object" | "two.docx" || 1
    }

    @Unroll
    def "test RainbowGraphStep g.has >> P.neq"(){
        expect:
        g.V().has(propertyKey, P.neq(value)).toList().size() == expectedSize

        where:
        propertyKey | value         || expectedSize
        "user"      | "*"           || 0
        "user"      | "u*"          || 0
        "user"      | "*A"          || 5
        "user"      | "userA"       || 5
        "operation object" | "one.docx" || 5
        "after_operation object" | "two.docx" || 2
    }

    @Unroll
    def "testRainbowGraphStep g.has >> P.within"(){
        expect:
        g.V().has(propertyKey,P.within(value)).toList().size() == expectedSize

        where:
        propertyKey | value || expectedSize
        "user"      | ["*", "userA"]     || 8
        "user"      | ["userA", "userB"] || 4
        "user"      | ["usera", "A"]     || 0
        "operation object" | ["one.docx","two.docx"] || 4
        "after_operation object" | ["one.docx","two.docx"] || 1
    }

    @Unroll
    def "testRainbowGraphStep g.has >> P.without"(){
        expect:
        g.V().has(propertyKey, P.without(value)).toList().size() == expectedSize

        where:
        propertyKey | value                    || expectedSize
        "user"      | ["*", "userA"]           || 0
        "user"      | ["userA","userF"]        || 5
        "user"      | ["userA", "userB", "userC"] || 2
        "operation object" | ["one.docx","two.docx"] || 4
        "after_operation object" | ["two.docx","four.docx"] || 1
    }

    @Unroll
    def "test RainbowGraphStep g.V.has >> P.between"() {
        expect: ""
        g.V().has(propertyKey, P.between(startTime, endTime)).toList().size() == expectSize

        where:
        propertyKey  | startTime    | endTime      | expectSize
        "@timestamp" | "2017-01-01" | "2018-01-01" | 0
        "@timestamp" | "2017-01-01" | "2019-01-01" | 19
        "@timestamp" | "2017-01-01" | "2018-03-02" | 0
        "@timestamp" | "2018-03-02" | "2018-03-03" | 9
        "@timestamp" | "2018-03-02" | "2018-03-04" | 19
    }

    @Unroll
    def "test RainbowGraphStep g.V.has >> P.inside"() {
        expect:
        g.V().has(propertyKey, P.inside(startTime, endTime)).toList().size() == expectSize

        where:
        propertyKey  | startTime    | endTime      | expectSize
        "@timestamp" | "2017-01-01" | "2018-01-01" | 0
        "@timestamp" | "2017-01-01" | "2019-01-01" | 19
        "@timestamp" | "2017-01-01" | "2018-03-02" | 0
        "@timestamp" | "2018-03-02" | "2018-03-03" | 0
        "@timestamp" | "2018-03-02" | "2018-03-04" | 10
    }

    @Unroll
    def "test RainbowGraphStep g.V.has >> P.outside"() {
        expect:
        g.V().has(propertyKey, P.outside(startTime, endTime)).toList().size() == expectSize

        where:
        propertyKey  | startTime    | endTime      | expectSize
        "@timestamp" | "2017-01-01" | "2018-01-01" | 19
        "@timestamp" | "2017-01-01" | "2019-01-01" | 0
        "@timestamp" | "2017-01-01" | "2018-03-02" | 10
        "@timestamp" | "2018-03-02" | "2018-03-03" | 0
        "@timestamp" | "2018-03-02" | "2018-03-04" | 0
    }

    @Unroll
    def "test RainbowGraphstep g.V.dedup"(){
        expect:
        g.V().dedup().toList().size() == 10

        and: "dedup for single schema"
        g.V().hasLabel("USER").dedup().toList().size() ==5

        and: "dedup for multi schema"
        g.V().hasLabel("DOC").dedup().toList().size() == 5
    }

    def "test RainbowGraphStep g.V.hasLabel.limit"(){
        expect: "limit for single schema"
        g.V().hasLabel("USER").limit(5).toList().size() == 5

        and: "limit for multi shemas"
        g.V().hasLabel("DOC").limit(2).toList().size() == 4
    }

    def "test RainbowGraphStep g.V.both"(){
        expect: "both for single schema"
        g.V("userC").both().toList().size() == 2
        g.V("userC").out().toList().size() == 2
        g.V("userC").in().toList().size() == 0

        and: "both for mutil schema"
        g.V("userA").both().toList().size() == 4
        g.V("userA").dedup().both().toList().size() == 4
        g.V("userA").dedup().both().dedup().toList().size() == 3
        g.V("userA").out().toList().size() == 4
        g.V("userA").out().dedup().size() == 3
        g.V("userA").in().toList().size() == 0

    }

    def "test RainbowGraphStep g.E.hasLabel.range"(){
        expect: "range for single schema"
        g.E().hasLabel("operation").range(0,4).toList().size() == 4

        and: "range for multi schemas"
        g.E().has("operation action").range(0,2).toList().size() == 6
    }

    def "test RainbowGraphStep g.E.order.by"() {
        expect:
        g.E().hasLabel("after_operation").order().by("@timestamp", Order.incr).values("@timestamp").toList().toString().equals("[2018-03-02T09:35:21.000Z, 2018-03-03T13:35:21.000Z, 2018-03-03T14:35:21.000Z]")
        g.E().hasLabel("be_operated_to").order().by("operation action", Order.incr).values("operation action").toList().toString().equals("[copy, copy, rename]")
    }

    def "test RainbowGraphStep g.E.groupCount.by(T.label)"() {
        expect:
        g.E().groupCount().by(T.label).toList()[0].size() == 3
        g.E().groupCount().by(T.label).toList()[0].keySet().toString().equals("[be_operated_to, operation, after_operation]")
    }

    def "test RainbowGraphStep g.E.groupCount.by(PropertyKey)"() {
        expect:
        g.E().groupCount().by("operation action").toList()[0].size() == 5
        g.E().groupCount().by("operation action").toList()[0].keySet().toString().equals("[preview, download, rename, create, copy]")
    }

    def "test startVertices query script"() {
        expect:
        g.V().hasLabel("USER").has("user", P.eq("userC")).dedup().toList().size() == 1
        g.V().hasLabel("USER").has("user", P.eq("user C")).dedup().toList().size() == 0
        g.V().hasLabel("USER").has("user", P.eq("*")).dedup().toList().size() == 5
        g.V().hasLabel("USER").has("user", P.eq("user*")).dedup().toList().size() == 5
        g.V().hasLabel("USER").has("user", P.eq("\"user D\"")).dedup().toList().size() == 0
        g.V().hasLabel("USER").has("user", P.neq("userC")).dedup().toList().size() == 4
        g.V().hasLabel("USER").has("user", P.within(["userA", "userB"])).dedup().toList().size() == 2
        g.V().hasLabel("USER").has("user", P.without(["userA", "userB"])).dedup().toList().size() == 3
        g.V().hasLabel("USER").has("@timestamp", P.between("2017-01-01", "2018-03-02")).dedup().toList().size() == 0
        g.V().hasLabel("USER").has("@timestamp", P.inside("2017-01-01", "2018-03-02")).dedup().toList().size() == 0
        g.V().hasLabel("USER").has("@timestamp", P.outside("2017-01-01", "2018-03-02")).dedup().toList().size() == 4

        and:
        g.V().hasLabel("USER").hasKey("@timestamp").dedup().size() == 5
        g.V().hasLabel("USER").hasKey("@timestamp").has("user", "userA").dedup().size() == 1
    }

    def "test verticesSearch query script"() {
        expect:
        g.V().has("user", P.eq("user*")).values("user").dedup().toList().size() == 5
        g.V().has("user", P.eq("user*")).dedup().values("user").toList().size() == 5
        g.V().has("user", P.eq("user*")).values("@timestamp").dedup().toList().size() == 8
        g.V().has("user", P.eq("user*")).values("user").dedup().limit(3).toList().size() == 3
    }

    def "test verticesNeighbors query script"() {
        expect:
        g.V("userA").hasLabel("USER").both().dedup().toList().size() == 3
        g.V("userA").hasLabel("USER").dedup().both().dedup().toList().size() == 3
        g.V("userA").hasLabel("USER").dedup().both().dedup().limit(1).toList().size() == 2
        g.V("userA").hasLabel("USER").dedup().out().dedup().toList().size() == 3
        g.V("userA").hasLabel("USER").dedup().in().dedup().toList().size() == 0
        g.V("A1").hasLabel("DOC").dedup().in().dedup().toList().size() == 2
        g.V("userC").hasLabel("USER").dedup().out().dedup().limit(1).toList().size() == 1
        g.V("A1").hasLabel("DOC").dedup().in().dedup().limit(1).toList().size() == 1

        and:
        g.V("A1").dedup().hasLabel("DOC").bothE().toList().size() == 4
        g.V("A1").dedup().hasLabel("DOC").outE().toList().size() == 1
        g.V("A1").dedup().hasLabel("DOC").inE().toList().size() == 3
    }

    def "test edgesFilter query script"() {
        expect:
        g.E().has("operation action", "download").toList().size() == 2
        g.E().hasLabel("after_operation").has("operation action", "copy").has("operation object", P.eq("three.docx")).toList().size() == 2
        g.E().hasLabel("after_operation").has("operation action", "copy").has("operation object", P.eq("three.docx")).range(0, 1).toList().size() == 1
        g.E().hasLabel("operation").has("operation action", "create").order().by("@timestamp", Order.decr).values("@timestamp").toList().toString() == "[2018-03-03T11:35:21.000Z, 2018-03-02T07:35:21.000Z]"
        g.E().hasLabel("operation").hasKey("operation action").order().by("operation action", Order.decr).values("operation action").toList().toString() == "[rename, preview, download, download, create, create, copy, copy]"
        g.E().hasLabel("operation").hasKey("operation action").order().by("operation action", Order.decr).by("@timestamp", Order.decr).values("operation action").toList().toString() == "[copy, copy, preview, create, download, rename, download, create]"
    }

    def "test edgesGroupCountByLabel query script"() {
        expect:
        g.E().has("operation action", "copy").groupCount().by(T.label).toList()[0].size() == 3
        g.E().hasKey("operation action").groupCount().by(T.label).toList()[0].keySet().toString() == "[be_operated_to, operation, after_operation]"
    }

    def "test edgesGroupCountByProperty query script"() {
        expect:
        g.E().has("operation action", "create").groupCount().by("@timestamp").toList()[0].size() == 2
        g.E().hasKey("operation action").groupCount().by("operation action").toList()[0].keySet().toString() == "[preview, download, rename, create, copy]"
        g.E().hasLabel("operation").groupCount().by("operation action").toList()[0].get("rename").toString() == "1"
        g.E().hasLabel("operation").groupCount().by("operation action").toList()[0].get("download").toString() == "2"
        g.E().hasLabel("operation").groupCount().by("operation action").toList()[0].get("copy").toString() == "2"
    }
}