import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.JsonNode
import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.exceptions.UnirestException
import com.mashape.unirest.request.BaseRequest
import org.json.JSONObject

/**
 * @author tong.sha@eisoo.com
 * @version 1.0 , Copyright (c) 2014 AnyRobot, EISOO
 * @date 2018.04.28
 */

class ElasticHelper {
    def static baseUrl = initBaseUrl()
    def static index = "rainbow-ut"

    static JSONObject getRainbowConf(String index) {
        String conf = """
{
    "class": "com.eisoo.rainbow.elastic.ElasticSourceProvider",
    "dataSet": {
        "url": "$baseUrl",
        "index": ["$index"],
        "query": {
            "query_string": {
                "query": "*",
                "analyze_wildcard": true
            }
        }
    }
}
"""
        return new JSONObject(conf)
    }

    static def setupData(final String type, final String index, final List<String> dataList) {
        def url = "$baseUrl/$index/$type"
        for (String data : dataList) {
            request(Unirest.post(url).body(data), 201)
        }

        while (true) {
            if (count(index) >= dataList.size())
                break
            Thread.sleep(100)
        }
    }

    static def teardownData(final String index) {
        if (count(index) == 0)
            return

        def url = "$baseUrl/$index/_delete_by_query"
        def body = """{"query": {"query_string": {"query": "*", "analyze_wildcard": true}}}"""
        request(Unirest.post(url).body(body), 400)
        while (true) {
            if (count(index) == 0)
                break
            Thread.sleep(100)
        }
    }

    private static def initBaseUrl() {
        def prop = new Properties()
        prop.load(ClassLoader.getSystemResourceAsStream("es.properties"))
        prop.getProperty("es.url")
    }

    private static def count(final String index) {
        def url = "$baseUrl/$index/_search"
        HttpResponse<JsonNode> result = request(Unirest.post(url).body(""), 404)
        if (result.getStatus() == 404)
            return 0
        return result.getBody().getObject().getJSONObject("hits").getInt("total")
    }

    private static def initSettings() {
        def url = "$baseUrl/$index/_settings"
        def body = """{"index": {"number_of_replicas": 1, "write": {"wait_for_active_shards": 1}}}"""
        request(Unirest.put(url).body(body))
    }

    private static HttpResponse<JsonNode> request(final BaseRequest baseRequest, final int... ignoreStatusCodes) {
        HttpResponse<JsonNode> response

        try {
            response = baseRequest.asJson()
        } catch (UnirestException e) {
            throw new RuntimeException(e)
        }

        if (ignoreStatusCodes.contains(response.getStatus()))
            return response

        if (response.getStatus() != 200) {
            def body = response.getBody().toString()
            def url = baseRequest.getHttpRequest().getUrl()
            throw new RuntimeException("Failed to request to $url. Error: $body")
        }

        return response
    }
}