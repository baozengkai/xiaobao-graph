package com.eisoo.rainbow.elastic;

import com.eisoo.rainbow.query.*;
import com.jcabi.aspects.Loggable;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.BaseRequest;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.tinkerpop.gremlin.process.traversal.Compare;
import org.apache.tinkerpop.gremlin.process.traversal.Contains;
import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.util.OrP;
import org.json.*;
import org.javatuples.Pair;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author tong.sha@eisoo.com
 * @version 1.0 , Copyright (c) 2014 AnyRobot, EISOO
 * @date 2018.02.28
 */

public class ElasticSourceProvider implements SourceProvider {
    private final DSLHelper dslHelper = new DSLHelper();
    private String dataSetUrl;
    private Set<String> dataSetIndex;
    private JSONObject dataSetQuery;

    /**
     * 将去重查询和非去重查询的最多结果数统一限定为：ElasticSearch 的 [index.max_result_window] 默认值
     */
    private int sizeLimit = 10000;

    @Override
    public void init(final JSONObject dataSetConf) throws Exception {
        this.dataSetUrl = dataSetConf.getString("url");
        this.dataSetIndex = IteratorUtils.toList(dataSetConf.getJSONArray("index").iterator())
                .stream().map(obj -> (String) obj).collect(Collectors.toSet());
        this.dataSetQuery = dataSetConf.getJSONObject("query");
        this.dslHelper.init(this.getAllFields());
    }

    @Loggable(logThis = true, trim = false)
    private String getSearchUrl() {
        return this.dataSetUrl + "/" + String.join(",", this.dataSetIndex) + "/_search";
    }

    @Loggable(logThis = true, trim = false)
    private String getFieldsUrl() {
        return this.dataSetUrl + "/_field_stats?fields=*";
    }

    @Override
    @Loggable(logThis = true, trim = false, skipResult = true)
    public List<Map<String, Object>> search(final SourceSearchHolder searchHolder) {
        this.checkSearchHolderValidation(searchHolder);

        if (searchHolder.getSize() == 0) {
            return Collections.EMPTY_LIST;
        }

        JsonNode body = this.toQueryBody(searchHolder);
        HttpResponse<JsonNode> response = this.request(Unirest.post(this.getSearchUrl()).body(body));
        return this.hitsToDataSet(this.collectHits(response));
    }

    @Override
    @Loggable(logThis = true, trim = false)
    public long searchCount(final SourceSearchHolder sourceSearchHolder) {
        JsonNode body = this.toCountBody(sourceSearchHolder);
        HttpResponse<JsonNode> response = this.request(Unirest.post(this.getSearchUrl()).body(body));
        return this.dslHelper.getCountValue(response.getBody().getObject());
    }

    @Override
    @Loggable(logThis = true, trim = false)
    public Map<Object, Long> searchGroupCount(final SourceSearchHolder searchHolder, final String byField) {
        if (byField.equals("_id")) {
            searchHolder.setSize(10000);
            JsonNode body = this.toQueryBody(searchHolder);
            HttpResponse<JsonNode> response = this.request(Unirest.post(this.getSearchUrl()).body(body));
            List<Map<String, Object>> dataSet = this.hitsToDataSet(this.collectHits(response));

            Map<Object, Long> results = new HashMap<>();
            dataSet.forEach(dataRow -> results.put(dataRow.get(byField), 1L));
            return results;
        } else {
            JsonNode body = this.toGroupCountBody(searchHolder, byField);
            HttpResponse<JsonNode> response = this.request(Unirest.post(this.getSearchUrl()).body(body));
            return this.dslHelper.getGroupCountValue(response.getBody().getObject());
        }
    }

    private void checkSearchHolderValidation(final SourceSearchHolder searchHolder) {
        if (searchHolder.getSize() > this.sizeLimit) {
            throw new RuntimeException(String.format(
                    "Batch size is too large, size must be less than or equal to: [%d] but was [%d].",
                    this.sizeLimit, searchHolder.getSize()));
        }
    }

    @Loggable(logThis = true, skipResult = true)
    private JSONObject getAllFields() {
        HttpResponse<JsonNode> response = this.request(Unirest.get(this.getFieldsUrl()));
        return response.getBody().getObject().getJSONObject("indices").getJSONObject("_all").getJSONObject("fields");
    }

    private boolean needDistinctAggregation(final SourceSearchHolder searchHolder) {
        return searchHolder.hasDistinctField() &&
                this.dslHelper.getAggregatableField(searchHolder.getDistinctField()) != null;
    }

    @Loggable(logThis = true, trim = false)
    private JsonNode toQueryBody(final SourceSearchHolder searchHolder) {
        JsonNode body = new JsonNode("");
        body.getObject().put("query", this.toFinalQueryDSL(searchHolder));

        if (this.needDistinctAggregation(searchHolder)) {
            String aggsField = this.dslHelper.getAggregatableField(searchHolder.getDistinctField());
            JSONObject aggsJson = this.dslHelper.distinctQueryDSL(aggsField, searchHolder.getSize());
            body.getObject().put("aggs", aggsJson);
            body.getObject().put("size", 0);
        } else {
            body.getObject().put("from", searchHolder.getFrom());
            body.getObject().put("size", searchHolder.getSize());
        }

        List<Pair<String, Order>> orders = searchHolder.getOrders().stream().collect(Collectors.toList());
        Collections.reverse(orders);
        orders = orders.stream()
                .map(p -> new Pair<String, Order>(this.dslHelper.getFilterableField(p.getValue0()), p.getValue1()))
                .collect(Collectors.toList());
        JSONArray orderDSL = this.dslHelper.orderDSL(orders);
        body.getObject().put("sort", orderDSL);

        return body;
    }

    @Loggable(logThis = true, trim = false)
    private JsonNode toCountBody(final SourceSearchHolder searchHolder) {
        JsonNode body = new JsonNode("");
        body.getObject().put("query", this.toFinalQueryDSL(searchHolder));
        body.getObject().put("size", 0);

        String aggsField = this.dslHelper.getAggregatableField(searchHolder.getDistinctField());
        if (this.needDistinctAggregation(searchHolder)) {
            JSONObject aggsJson = this.dslHelper.distinctCountDSL(aggsField);
            body.getObject().put("aggs", aggsJson);
        }

        return body;
    }

    @Loggable(logThis = true, trim = false)
    private JsonNode toGroupCountBody(final SourceSearchHolder searchHolder, final String byField) {
        JsonNode body = new JsonNode("");
        body.getObject().put("query", this.toFinalQueryDSL(searchHolder));
        body.getObject().put("size", 0);

        String aggsField = this.dslHelper.getAggregatableField(byField);
        if (null == aggsField)
            throw new IllegalArgumentException(String.format(
                    "Does not support group count by field %s.", byField));

        JSONObject aggsJson = this.dslHelper.groupCountDSL(aggsField);
        body.getObject().put("aggs", aggsJson);

        return body;
    }

    @Loggable(logThis = true, trim = false)
    private JSONObject toFinalQueryDSL(final SourceSearchHolder searchHolder) {
        List<JSONObject> clauseObjs = Arrays.asList(
                this.dataSetQuery,
                this.toQueryJson(searchHolder.getQueryHolder()));
        JSONObject boolJson = this.dslHelper.boolDSL(clauseObjs, Collections.EMPTY_LIST);
        return boolJson;
    }

    private JSONObject toQueryJson(final SourceQueryHolder sourceQueryHolder) {
        List<JSONObject> clauseObjs = new ArrayList<>();

        sourceQueryHolder.getChildren().stream()
                .map(queryHolder -> this.toQueryJson(queryHolder))
                .forEach(clauseObjs::add);

        sourceQueryHolder.getQueryClauses().stream()
                .map(queryClause -> this.toQueryClauseJson(queryClause))
                .filter(clauseObj -> clauseObj != null)
                .forEach(clauseObjs::add);

        JSONObject boolJson = this.dslHelper.boolDSL(
                (sourceQueryHolder.getType() == SourceQueryHolder.TYPE.And) ? clauseObjs : Collections.EMPTY_LIST,
                (sourceQueryHolder.getType() == SourceQueryHolder.TYPE.Or) ? clauseObjs : Collections.EMPTY_LIST);

        return boolJson;
    }

    private JSONObject toQueryClauseJson(final SourceQueryClause queryClause) {
        JSONObject clauseJson = null;

        // filter context
        String filterableField = this.dslHelper.getFilterableField(queryClause.getField());
        if (queryClause.getPredicate() instanceof ExistsP) {
            if (filterableField.equals("_id")) {
                return null;
            }
            clauseJson = this.dslHelper.existsDSL(filterableField);
        } else if (queryClause.getBiPredicate().equals(Compare.gt)) {
            clauseJson = this.dslHelper.gtDSL(filterableField, queryClause.getValue());
        } else if (queryClause.getBiPredicate().equals(Compare.gte)) {
            clauseJson = this.dslHelper.gteDSL(filterableField, queryClause.getValue());
        } else if (queryClause.getBiPredicate().equals(Compare.lt)) {
            clauseJson = this.dslHelper.ltDSL(filterableField, queryClause.getValue());
        } else if (queryClause.getBiPredicate().equals(Compare.lte)) {
            clauseJson = this.dslHelper.lteDSL(filterableField, queryClause.getValue());
        } else if (queryClause.getType() == SourceQueryClause.TYPE.Filter) {
            if (queryClause.getBiPredicate().equals(Contains.within)) {
                clauseJson = this.dslHelper.termsDSL(filterableField, queryClause.getValue());
            } else if (queryClause.getBiPredicate().equals(Contains.without)) {
                clauseJson = this.dslHelper.termsNotDSL(filterableField, queryClause.getValue());
            } else if (queryClause.getBiPredicate().equals(Compare.eq)) {
                clauseJson = this.dslHelper.termDSL(filterableField, queryClause.getValue());
            } else if (queryClause.getBiPredicate().equals(Compare.neq)) {
                clauseJson = this.dslHelper.termNotDSL(filterableField, queryClause.getValue());
            }
        }

        // query context
        if (null == clauseJson) {
            // queryExpr是组合的query_string中的query变量的内容 比如: +(D:\1\123\cc.txt")
            // 鲍增凯: 新增内容，将queryClause.getPredicate()传入到queryStringDSL()函数中
            // 如果判断是within或者是without，那么就使用keyword类型的查询
            String queryExpr = this.toQueryStringExpr(queryClause.getPredicate());
            clauseJson = this.dslHelper.queryStringDSL(queryClause.getPredicate(),queryClause.getField(), queryExpr);
        }

        return clauseJson;
    }

    private String toQueryStringExpr(final P predicate) {
        Function<Object, String> valueToString = (v) -> v instanceof String ? (String) v : JSONObject.valueToString(v);

        String queryExpr;
        if (predicate.getBiPredicate().equals(Contains.within)) {
            final List<String> values = ((Collection<?>) predicate.getValue()).stream()
                    .map(value -> valueToString.apply(value))
                    .collect(Collectors.toList());
            queryExpr = "+(" + String.join(" | ", values) + ")";
        } else if (predicate.getBiPredicate().equals(Contains.without)) {
            final List<String> values = ((Collection<?>) predicate.getValue()).stream()
                    .map(value -> valueToString.apply(value))
                    .collect(Collectors.toList());
            queryExpr = "-(" + String.join(" & ", values) + ")";
        } else if (predicate.getBiPredicate().equals(Compare.eq)) {
            queryExpr = valueToString.apply(predicate.getValue());
        } else if (predicate instanceof OrP) {
            List<P> predicates = ((OrP) predicate).getPredicates();
            List<String> queryExpressions = predicates.stream()
                    .map(p -> this.toQueryStringExpr(p))
                    .collect(Collectors.toList());
            queryExpr = String.join(" OR ", queryExpressions);
        } else if (predicate.getBiPredicate().equals(Compare.neq)) {
            queryExpr = "-(" + valueToString.apply(predicate.getValue()) + ")";
        } else if (predicate.getBiPredicate().equals(Compare.gt)) {
            queryExpr = ">" + valueToString.apply(predicate.getValue());
        } else if (predicate.getBiPredicate().equals(Compare.gte)) {
            queryExpr = ">=" + valueToString.apply(predicate.getValue());
        } else if (predicate.getBiPredicate().equals(Compare.lt)) {
            queryExpr = "<" + valueToString.apply(predicate.getValue());
        } else if (predicate.getBiPredicate().equals(Compare.lte)) {
            queryExpr = "<=" + valueToString.apply(predicate.getValue());
        } else {
            throw new NotImplementedException();
        }
        return queryExpr;
    }

    @Loggable(logThis = true, trim = false)
    private HttpResponse<JsonNode> request(final BaseRequest baseRequest) {
        HttpResponse<JsonNode> result;
        try {
            result = baseRequest.asJson();
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }

        if (result.getStatus() != 200) {
            String err = "Failed to request to " + baseRequest.getHttpRequest().getUrl() + ". Error:"
                    + result.getStatus() + " - " + result.getStatusText() + ". "
                    + "Response body: " + result.getBody().getObject().toString(2);
            throw new RuntimeException(err);
        } else if (result.getBody().getObject().getJSONObject("_shards").has("failures")) {
            JSONArray failures = result.getBody().getObject().getJSONObject("_shards").getJSONArray("failures");
            if (failures.length() > 0) {
                String err = "Failed to request to " + baseRequest.getHttpRequest().getUrl() + ". Error:"
                        + failures.getJSONObject(0).toString(2);
                throw new RuntimeException(err);
            }
        }

        return result;
    }

    private List<JSONObject> collectHits(final HttpResponse<JsonNode> response) {
        List<JSONObject> hits = new ArrayList<>();

        JSONArray queryHitsArray = this.dslHelper.getQueryHits(response.getBody().getObject());
        for (int i = 0; i < queryHitsArray.length(); i++) {
            hits.add(queryHitsArray.getJSONObject(i));
        }

        if (this.dslHelper.isDistinctQueryResponse(response.getBody().getObject())) {
            JSONArray bucketsArray = this.dslHelper.getDistinctQueryBuckets(response.getBody().getObject());
            for (int i = 0; i < bucketsArray.length(); i++) {
                JSONArray aggsHitsArray = this.dslHelper.getDistinctQueryHits(bucketsArray.getJSONObject(i));
                for (int j = 0; j < aggsHitsArray.length(); j++) {
                    hits.add(aggsHitsArray.getJSONObject(j));
                }
            }
        }

        return hits;
    }

    public List<Map<String, Object>> hitsToDataSet(final List<JSONObject> hits) {
        List<Map<String, Object>> dataSet = new ArrayList<>();
        for (final JSONObject hit : hits) {
            JSONObject source = hit.getJSONObject("_source");
            JSONObject notSource = new JSONObject(hit.toString());
            notSource.remove("_source");

            Map<String, Object> dataRow = new HashMap<>();
            dataRow.putAll(this.toDataRow(notSource));
            dataRow.putAll(this.toDataRow(source));
            dataSet.add(dataRow);
        }
        return dataSet;
    }

    private Map<String, Object> toDataRow(final JSONObject jsonObject) {
        Map<String, Object> dataRow = new HashMap<>();
        for (String key : jsonObject.keySet()) {
            Object value = jsonObject.get(key);
            if (value instanceof JSONObject) {
                Map<String, Object> childDataRow = this.toDataRow((JSONObject) value);
                childDataRow.keySet().stream()
                        .forEach(childKey -> dataRow.put(key + "." + childKey, childDataRow.get(childKey)));
            } else {
                dataRow.put(key, value);
            }
        }
        return dataRow;
    }
}
