package graph_server

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource
import org.json.JSONObject
import com.eisoo.rainbow.gremlin.structure.RainbowGraph

import grails.test.mixin.TestFor
import spock.lang.Specification

import util.ElasticHelper

@TestFor(GraphService)
class GraphServiceSpec extends Specification{

    static GraphTraversalSource g

    def setupSpec() {
        given:
        ElasticHelper.teardownData("graph-server-ut")

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

        ElasticHelper.setupData("GremlinSimpleTest","graph-server-ut", data)

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
        def confJson = ElasticHelper.getRainbowConf("graph-server-ut")
        confJson.put("graphSchema", new JSONObject(graphSchema))
        g = RainbowGraph.open(confJson.toString()).traversal()
    }

        def cleanupSpec() {
                ElasticHelper.teardownData("graph-server-ut")
        }

    void "test GraphService get_start_vertex"()
    {
        given:
        String startVertexRequest = """{
                "query": {
                    "label": [
                    ],
                    "idField": [
                        "用户"
                    ],
                    "propertyValue": [
                        {
                            "key": "用户",
                            "operator": "eq",
                            "value": [
                                "张三"
                            ]
                        },
                        {
                            "key": "@timestamp",
                            "operator": "eq",
                            "value": [
                                
                            ]
                        }
                    ]
                },
        "limit": 100
        }
        """
        when:
        def startVertexrResponse = service.get_start_vertex(new JSONObject(startVertexRequest),g)

        then:
        startVertexrResponse.size() == 1
    }

    void "test GraphService get_vertex_search"()
    {
        given:
        String vertexSearchRequest= """
            {
                "idField": "用户",
                "propertyKey": "用户",
                "keyword": "*",
                "limit": 10
            }
        """

        when:
        def vertexSearchResponse = service.get_vertex_search(new JSONObject(vertexSearchRequest),g)

        then:
        vertexSearchResponse.size() == 5
    }

    void "test GraphService get_vertex_neighbors"()
    {
        given:
        String vertexNeighborsRequest = """
            {
                "vertex":{
                    "id":"张三",
                    "label":"用户"
                    }
            }
        """
        when:
        def vertexNeighborsResponse = service.get_vertex_neighbors(new JSONObject(vertexNeighborsRequest), g)

        then:
        vertexNeighborsResponse.size() == 2
    }

    void "test GraphService get_edge_filter"()
    {
        given:
        String edgeFilterRequest = """
            {
                "query" :{
                    "outVertex":{
                     "label":"用户",
                      "id": "张一"
                    },
                    "inVertex":{
                      "label":"文档",
                      "id":"文档1.docx"
                    },
                    "label": [],
                    "propertyValue":{}
                    },
                    "from":0,
                    "size":5,
                    "sort":[]
            }
        """

        when:
        def edgeFilterResponse = service.get_edge_filter(new JSONObject(edgeFilterRequest),g)

        then:
        edgeFilterResponse.size() == 2
    }

    void "test GraphService get_edge_group_count_by_label"()
    {
        given:
        String edgeGroupByLabelRequet = """
           {
                "query" :{
                    "outVertex":{
                     "label":"用户",
                      "id": "张一"
                    },
                    "inVertex":{
                      "label":"文档",
                      "id":"文档1.docx"
                    },
                    "label": [],
                    "propertyValue":{}
                    }
           } 
        """
        when:
        def edgeGroupByLabelResponse = service.get_edge_group_count_by_label(new JSONObject(edgeGroupByLabelRequet), g)

        then:
        edgeGroupByLabelResponse.size() == 1
        edgeGroupByLabelResponse[0].toString() == "[操作:2]"
    }

    void "test GraphService get_edge_group_count_by_property"()
    {
        given:
        String edgeGroupByPropertyRequet = """
           {
                "query" :{
                    "outVertex":{
                     "label":"用户",
                      "id": "张一"
                    },
                    "inVertex":{
                      "label":"文档",
                      "id":"文档1.docx"
                    },
                    "label": [],
                    "propertyValue":{}
                    },
                 "propertyKey": "操作动作"
           } 
        """
        when:
        def edgeGroupByPropertyResponse = service.get_edge_group_count_by_property(new JSONObject(edgeGroupByPropertyRequet), g)

        then:
        edgeGroupByPropertyResponse[0].size() == 2
        edgeGroupByPropertyResponse[0].toString() == "[重命名:1, 创建:1]"
    }
}
