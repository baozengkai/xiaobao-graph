package com.eisoo.rainbow.elastic;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import org.apache.tinkerpop.gremlin.process.traversal.Contains;
import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.json.JSONArray;
import org.json.JSONObject;
import org.javatuples.Pair;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author tong.sha@eisoo.com
 * @version 1.0 , Copyright (c) 2014 AnyRobot, EISOO
 * @date 2018.02.28
 */

public class DSLHelper {
    private JSONObject fields;

    private Template distinctQueryMustache;
    private Template boolMustache;
    private Template existsMustache;
    private Template termsMustache;
    private Template termMustache;
    private Template termsNotMustache;
    private Template termNotMustache;
    private Template gtMustache;
    private Template gteMustache;
    private Template ltMustache;
    private Template lteMustache;
    private Template queryStringMustache;
    private Template distinctCountMustache;
    private Template groupCountMustache;
    private Template orderMustache;

    public void init(final JSONObject fields) throws Exception {
        this.fields = fields;
        this.distinctQueryMustache = Mustache.compiler().compile(new InputStreamReader(
                ClassLoader.getSystemResourceAsStream("distinct_query.mustache")));
        this.boolMustache = Mustache.compiler().compile(new InputStreamReader(
                ClassLoader.getSystemResourceAsStream("bool.mustache")));
        this.existsMustache = Mustache.compiler().compile(
                "{\"constant_score\": { \"filter\": {\"exists\": { \"field\": {{{field}}} } } } }");
        this.termsMustache = Mustache.compiler().compile(
                "{\"constant_score\": { \"filter\": {\"terms\": { {{{field}}}: {{{value}}} } } } }");
        this.termMustache = Mustache.compiler().compile(
                "{\"constant_score\": { \"filter\": {\"term\": { {{{field}}}: {{{value}}} } } } }");
        this.termsNotMustache = Mustache.compiler().compile(
                "{\"bool\": {\"must_not\": {\"terms\": { {{{field}}}: {{{value}}} } } } }");
        this.termNotMustache = Mustache.compiler().compile(
                "{\"bool\": {\"must_not\": {\"term\": { {{{field}}}: {{{value}}} } } } }");
        this.gtMustache = Mustache.compiler().compile(
                "{\"constant_score\": { \"filter\": {\"range\": { {{{field}}}: {\"gt\": {{{value}}} } } } } }");
        this.gteMustache = Mustache.compiler().compile(
                "{\"constant_score\": { \"filter\": {\"range\": { {{{field}}}: {\"gte\": {{{value}}} } } } } }");
        this.ltMustache = Mustache.compiler().compile(
                "{\"constant_score\": { \"filter\": {\"range\": { {{{field}}}: {\"lt\": {{{value}}} } } } } }");
        this.lteMustache = Mustache.compiler().compile(
                "{\"constant_score\": { \"filter\": {\"range\": { {{{field}}}: {\"lte\": {{{value}}} } } } } }");
        this.queryStringMustache = Mustache.compiler().compile(
                "{\"query_string\": {\"default_field\": {{{field}}}, \"query\": {{{query}}}," +
                          " \"auto_generate_phrase_queries\": true } }");
        this.distinctCountMustache = Mustache.compiler().compile(
                "{\"distinct_count\" : {\"cardinality\" : {\"field\" : {{{field}}} } } }");
        this.groupCountMustache = Mustache.compiler().compile("{\"group_count\" : {\"terms\" :" +
                          " {\"field\" : {{{field}}} } } }");
        this.orderMustache = Mustache.compiler().compile("{\"{{{field}}}\" : \"{{{order}}}\"}");
    }

    public JSONObject boolDSL(final List<JSONObject> must, final List<JSONObject> should) {
        return new JSONObject(this.boolMustache.execute(new Object() {
            public String getMust() {
                return JSONObject.valueToString(must);
            }
            public String getShould() {
                return JSONObject.valueToString(should);
            }
        }));
    }

    public JSONObject distinctQueryDSL(final String field, final long size) {
        return new JSONObject(this.distinctQueryMustache.execute(new Object() {
            public String getField() {
                return JSONObject.valueToString(field);
            }
            public long getSize() {
                return size;
            }
        }));
    }

    public JSONObject existsDSL(final String field) {
        return new JSONObject(this.existsMustache.execute(new Object() {
            public String getField() {
                return JSONObject.valueToString(field);
            }
        }));
    }

    private Object getFiledValueData(final String field, final Object value) {
        return new Object() {
            public String getField() {
                return JSONObject.valueToString(field);
            }
            public String getValue() {
                return JSONObject.valueToString(value);
            }
        };
    }

    public JSONObject termsDSL(final String field, final Object value) {
        return new JSONObject(this.termsMustache.execute(this.getFiledValueData(field, value)));
    }

    public JSONObject termDSL(final String field, final Object value) {
        return new JSONObject(this.termMustache.execute(this.getFiledValueData(field, value)));
    }

    public JSONObject termsNotDSL(final String field, final Object value) {
        return new JSONObject(this.termsNotMustache.execute(this.getFiledValueData(field, value)));
    }

    public JSONObject termNotDSL(final String field, final Object value) {
        return new JSONObject(this.termNotMustache.execute(this.getFiledValueData(field, value)));
    }

    public JSONObject gtDSL(final String field, final Object value) {
        return new JSONObject(this.gtMustache.execute(this.getFiledValueData(field, value)));
    }

    public JSONObject gteDSL(final String field, final Object value) {
        return new JSONObject(this.gteMustache.execute(this.getFiledValueData(field, value)));
    }

    public JSONObject ltDSL(final String field, final Object value) {
        return new JSONObject(this.ltMustache.execute(this.getFiledValueData(field, value)));
    }

    public JSONObject lteDSL(final String field, final Object value) {
        return new JSONObject(this.lteMustache.execute(this.getFiledValueData(field, value)));
    }

    // 组合filed和query到 queryStringDSL的模板中
    public JSONObject queryStringDSL(final P predicate, final String field, final String query) {

        // 鲍增凯: 增加判断，如果query变量存储的是unicode字符，那么需要返回field为关键字查询
        if(query.contains("\\u")){
            return new JSONObject(this.queryStringMustache.execute(new Object() {
                public String getField() {
                    return JSONObject.valueToString(field + ".keyword");
                }
                public String getQuery() {
                    return JSONObject.valueToString(query);
                }
            }));
            // 鲍增凯: 新增判断，如果是within 或者是without类型的查询，改为关键字和短语查询
        }else if(predicate.getBiPredicate().equals(Contains.within) || predicate.getBiPredicate().equals(Contains.without)){
            return new JSONObject(this.queryStringMustache.execute(new Object() {
                public String getField() {
                    return JSONObject.valueToString(field + ".keyword");
                }
                public String getQuery() {
                    return JSONObject.valueToString(query);
                }
            }));

        }else{
            return new JSONObject(this.queryStringMustache.execute(new Object() {
                public String getField() {
                    return JSONObject.valueToString(field);
                }
                public String getQuery() {
                    return JSONObject.valueToString(query);
                }
            }));
        }
    }

    public JSONObject distinctCountDSL(final String field) {
        return new JSONObject(this.distinctCountMustache.execute(new Object() {
            public String getField() {
                return JSONObject.valueToString(field);
            }
        }));
    }

    public JSONObject groupCountDSL(final String field) {
        return new JSONObject(this.groupCountMustache.execute(new Object() {
            public String getField() {
                return JSONObject.valueToString(field);
            }
        }));
    }

    public JSONArray orderDSL(final List<Pair<String, Order>> order) {

        List<String> orderMustaches = order.stream().map(p -> this.orderMustache.execute(new Object() {
            public String getField() {
                return p.getValue0();
            }
            public String getOrder() {
                return order(p.getValue1());
            }
        })).collect(Collectors.toList());
        return new JSONArray(orderMustaches.toString());
    }

    public long getCountValue(final JSONObject response) {
        if (response.has("aggregations")) {
            return response.getJSONObject("aggregations").getJSONObject("distinct_count").getLong("value");
        } else {
            return response.getJSONObject("hits").getLong("total");
        }
    }

    public Map<Object, Long> getGroupCountValue(final JSONObject response) {
        Map<Object, Long> results = new HashMap<>();

        JSONArray buckets = response.getJSONObject("aggregations").getJSONObject("group_count").getJSONArray("buckets");
        for (int i = 0; i < buckets.length(); i++) {
            JSONObject bucket = buckets.getJSONObject(i);
            results.put(bucket.get("key"), new Long(bucket.getInt("doc_count")));
        }

        return results;
    }

    public boolean isDistinctQueryResponse(final JSONObject response) {
        return response.has("aggregations") && response.getJSONObject("aggregations").has("distinct_query");
    }

    public JSONArray getDistinctQueryBuckets(final JSONObject response) {
        return response.getJSONObject("aggregations").getJSONObject("distinct_query").getJSONArray("buckets");
    }

    public JSONArray getQueryHits(final JSONObject response) {
        return response.getJSONObject("hits").getJSONArray("hits");
    }

    public JSONArray getDistinctQueryHits(final JSONObject bucket) {
        return bucket.getJSONObject("distinct_query_hits").getJSONObject("hits").getJSONArray("hits");
    }

    public String getAggregatableField(final String field) {
        String filterableField = this.getFilterableField(field);
        if (!this.fields.has(filterableField)) {
            return null;
        }
        if (!this.fields.getJSONObject(filterableField).getBoolean("aggregatable")) {
            return null;
        }
        return filterableField;
    }

    public String getFilterableField(final String field) {
        if (this.fields.keySet().contains(field + ".keyword")) {
            return field + ".keyword";
        } else {
            return field;
        }
    }

    private String order(final Order order) {
        if (order.equals(Order.decr))
            return "desc";
        else if (order.equals(Order.incr))
            return "asc";
        throw new IllegalArgumentException(order.toString());
    }
}
