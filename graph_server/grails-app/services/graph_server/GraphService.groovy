package graph_server

import com.eisoo.rainbow.gremlin.structure.RainbowGraph
import org.apache.tinkerpop.gremlin.process.traversal.Order
import org.apache.tinkerpop.gremlin.process.traversal.P
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.core.GrailsApplication
import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient
import org.apache.tinkerpop.gremlin.structure.T
import org.grails.web.json.JSONObject

import info.GlobalInfo

@Transactional
class GraphService {


    GrailsApplication grailsApplication
    GlobalInfo global

    //获取图
    def get_graph(Object request){
        def dsl = get_dsl(request)
        def graph_json = new JSONObject()
                .put("class","com.eisoo.rainbow.elastic.ElasticSourceProvider")
                .put("dataSet",dsl)
                .put("graphSchema",request.graphSchema)
        RainbowGraph graph = RainbowGraph.open(graph_json.toString())
        GraphTraversalSource g = graph.traversal()
        return g
    }

    // 获取dsl
    def get_dsl(Object request)
    {
        global = new GlobalInfo(grailsApplication.config)

        def data_set = request.dataSet

        def http = new RESTClient(global.SEARCH_SERVER)
//        def http = new RESTClient("http://192.168.84.227:11002")
        def resp = http.post(
                path:'/v1/search/parse',
                body: data_set,
                requestContentType: ContentType.JSON
        )

        if(resp.status < 400) {
            println("Request Success!")
            def json_data = JSON.parse(resp.data.toString())
            def data_set_json = new JSONObject()
                    .put("query", json_data.query)
                    .put("index", json_data.index)
                    .put("url", global.ELASTIC_SERVER)
//                    .put("url", "http://192.168.84.227:11002")

            return data_set_json
        }else{
            println("Request Failed!")
            println resp.data
        }
    }

    // 判断字符串是不是路径或者@timestamp字符串
    def is_path_or_date_string(def str)
    {
        if(str.contains("\\") || str.contains("\""))
        {
            return 1
        }
        else
            return 0
    }


    // 根据不同类型的字符串进行转义替换
    def quote(def str)
    {
        // 首先判断str是不是整形，如果是整形，那么先转为String类型
        if(str instanceof Integer)
        {
            return Integer.toString(str)
        }
        //如果不是整形继续判断是不是路径字符串
        else{
            //如果是路径字符串 那么就进行转义替换
            if(is_path_or_date_string(str) == 1) {
                String tmpStr = str.replace("\\", "\\\\").replace("\"", "\\\"")
                String finaStr = "\"${tmpStr}\""
                return finaStr
            }
            else
                return (String)"\"$str\""
        }
    }

    // 获取筛选的起始实体
    def get_start_vertex(def request, def g)
    {
        def query = request.query
        def graph_result =g.V()

        // 判断参数是否含有label参数
        if(query.has("label"))
        {
            for(def i in query.label)
            {
                graph_result = graph_result.hasLabel(i)
            }
        }
        // 判断参数是否含有idField参数
        if(query.has("idField"))
        {
            for(def i in query.idField)
            {
                graph_result = graph_result.has("__.id.field",i)
            }
        }

        // 判断参数是否含有propertyValue参数
        while(query.has("propertyValue")) {
            def i = 0
            while(i < query.propertyValue.length()) {
                def key = query["propertyValue"][i]["key"]
                // 匹配P.eq条件
                if(query["propertyValue"][i]["operator"] == "eq") {
                    def value = query["propertyValue"][i]["value"][0]
                    if(value) {
                        graph_result = graph_result.has(key, P.eq(quote(value)))
                    }
                }
                // 匹配P.neq条件
                else if(query["propertyValue"][i]["operator"] == "neq") {
                    def value = quote(query["propertyValue"][i]["value"][0])
                    if(value) {
                        graph_result = graph_result.has(key, P.neq(value))
                    }
                }
                else if(query["propertyValue"][i]["operator"] == "within") {
                    def values = query["propertyValue"][i]["value"]
                    List ls = new ArrayList<String>();
                    def value_within = []
                    if(values) {
                        for (def j = 0; j < values.length(); j++) {
                            value_within.add(quote(values[j]))
                        }
                // 匹配P.within条件
                    graph_result = graph_result.has(key, P.within(value_within))
                    }
                }
                // 匹配P.without条件
                else if(query["propertyValue"][i]["operator"] == "without") {
                    def values = query["propertyValue"][i]["value"]
                    def value_within = []
                    if(values) {
                        for (def j = 0; j < values.length();j++) {
                            value_within.add(quote(values[j]))
                        }
                        graph_result = graph_result.has(key, P.without(value_within))
                    }
                }
                i = i + 1
            }
            break
        }
        //判断参数中是否含有limit参数
        if(request.has("limit")) {
            graph_result = graph_result.dedup().limit(request["limit"]).toList()
        }
        else{
            graph_result = graph_result.dedup().toList()
        }
        return graph_result

    }

    // 获取实体的相邻实体
    def get_vertex_neighbors(def request, def g)
    {
        def graph_result =g.V(request["vertex"]["id"]).hasLabel(request["vertex"]["label"]).dedup().both().dedup().toList()
        return graph_result
    }

    // 获取根据属性筛选的实体
    def get_vertex_search(def request, def g)
    {
        def graph_result
        if(request.has("limit"))
        {
            graph_result = g.V().has("__.id.field", request["idField"])
                    .has(request["propertyKey"], P.eq(request["keyword"]))
                    .values(request["propertyKey"])
                    .dedup()
                    .limit(request["limit"])
                    .toList()
        }
        else
        {
            graph_result = g.V().has("__.id.field", request["idField"])
                    .has(request["propertyKey"], P.eq(request["keyword"]))
                    .values(request["propertyKey"])
                    .dedup()
                    .toList()
        }
        return graph_result

    }

    // 获取筛选的关系
    def get_edge_filter(def request, def g)
    {
        def query = request.query
        def graph_result = g.E()
                .has("__.out.id", query["outVertex"]["id"])
                .has("__.out.label", query["outVertex"]["label"])
                .has("__.in.id", query["inVertex"]["id"])
                .has("__.in.label", query["inVertex"]["label"])
        // 判断参数是否含有label参数
        while(query.has("label"))
        {
            for(def i in query["label"])
            {
                graph_result = graph_result.hasLabel(i)
            }
            break
        }

        // 判断参数是否含有propertyValue参数
        while(query.has("propertyValue"))
        {
            def i = 0
            while (i < query["propertyValue"].length())
            {
                def key = query["propertyValue"][i]["key"]
                // 匹配P.eq条件
                if(query["propertyValue"][i]["operator"] == "eq") {
                    def value = query["propertyValue"][i]["value"][0]
                    if(value) {
                        graph_result = graph_result.has(key, P.eq("\"${value}\""))
                    }
                }
                // 匹配P.neq条件
                else if(query["propertyValue"][i]["operator"] == "neq") {
                    def value = query["propertyValue"][i]["value"][0]
                    if(value) {
                        graph_result = graph_result.has(key, P.neq("\"${value}\""))
                    }
                }
                // 匹配P.within条件
                else if(query["propertyValue"][i]["operator"] == "within") {
                    def values = query["propertyValue"][i]["value"]
                    def value_within = []
                    if(values) {
                        for (def j = 0; j++; j < values.length()) {
                            value_within.add(quote(values[j]))
                        }
                        graph_result = graph_result.has(key, P.within(value_within))
                    }
                }
                // 匹配P.without条件
                else if(query["propertyValue"][i]["operator"] == "without") {
                    def values = query["propertyValue"][i]["value"]
                    def value_within = []
                    if(values) {
                        for (def j = 0; j++; j < values.length()) {
                            value_within.add(quote(values[j]))
                        }
                        graph_result = graph_result.has(key, P.without(value_within))
                    }
                }
                i = i + 1
            }
            break
        }
        // 判断参数是否含有sort参数
        while(request.has("sort")) {
            for(def h = 0; h < request["sort"].length(); h++)
            {
                def sort_key = request["sort"][h].keySet()[0]
                def sort_value = request["sort"][h].get(sort_key)

                //判断是按升序还是按照降序
                if(sort_value == "desc")
                {
                    graph_result =  graph_result.order().by(sort_key,Order.decr)
                }
                else
                {
                    graph_result = graph_result.order().by(sort_key,Order.incr)
                }
            }
            break
        }

        //判断参数是否含有from size参数
        if(request.has("from") && request.has("size"))
        {
            if(request["from"] > 0 || request["size"] < 10000)
            {
                graph_result =  graph_result.range(request["from"],request["size"])
            }
        }
        else if (!request.has("from") && request["size"])
        {
            if(request["size"] < 10000)
            {
                graph_result =  graph_result.range(0,request["size"])
            }
        }
        else if(request.has("from") && !request["size"])
        {
            if(request["from"] > 0)
            {
                graph_result = graph_result.range(request["from"], request["from"]+10)
            }
        }
        graph_result = graph_result.toList()
        return graph_result
    }

    // 根据标签对关系分组
    def get_edge_group_count_by_label(def request, def g)
    {
        def query = request.query
        def graph_result = g.E()
                .has("__.out.id", query["outVertex"]["id"])
                .has("__.out.label", query["outVertex"]["label"])
                .has("__.in.id", query["inVertex"]["id"])
                .has("__.in.label", query["inVertex"]["label"])
                .groupCount().by(T.label).toList()
        return graph_result
    }

    // 根据属性对关系分组
    def get_edge_group_count_by_property(def request, def g)
    {
        def query = request.query
        def graph_result = g.E()
                .has("__.out.id", query["outVertex"]["id"])
                .has("__.out.label", query["outVertex"]["label"])
                .has("__.in.id", query["inVertex"]["id"])
                .has("__.in.label", query["inVertex"]["label"])
                .groupCount().by(request["propertyKey"]).toList()
        return graph_result
    }
}
