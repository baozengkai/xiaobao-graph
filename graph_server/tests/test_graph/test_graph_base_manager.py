#!/usr/bin/env python
# -*- coding:utf-8 -*-
import unittest
import init_env
from modules.graph.graph_base_manager import GraphBaseManager


class TestGraphBaseManager(unittest.TestCase):
    """
    """
    req = {
        "dataSet": {
            "logGroup": "fe5b7f96-443a-11e7-a467-000c29253e90",
            "timeRange": [1357869678123, 1515636078123],
            "query": "*",
            "filter": {
                "must": [],
                "must_not": []
            },
            "sort": [{"@timestamp": "desc"}]
        },
        "graphSchema": {
            "vertices": [
                {
                    "id": "用户",
                    "label": "用户",
                    "properties": ["@timestamp", "用户"]
                },
                {
                    "id": "操作对象",
                    "label": "文档",
                    "properties": ["@timestamp", "操作对象", "父路径"]
                },
                {
                    "id": "操作后对象",
                    "label": "文档",
                    "properties": ["@timestamp", "操作后对象", "父路径", "目标路径"]
                }
            ],
            "edges": [
                {
                    "id": "_id",
                    "label": "操作",
                    "properties": ["_id", "@timestamp", "geoip.city_name", "操作动作", "操作状态", "操作详情"],
                    "outVertex": {
                        "id": "用户",
                        "label": "用户"
                    },
                    "inVertex": {
                        "id": "操作对象",
                        "label": "文档"
                    }
                },
                {
                    "id": "_id",
                    "label": "操作后",
                    "properties": ["_id", "@timestamp", "geoip.city_name", "操作动作", "操作状态", "操作详情", "操作对象"],
                    "outVertex": {
                        "id": "用户",
                        "label": "用户"
                    },
                    "inVertex": {
                        "id": "操作后对象",
                        "label": "文档"
                    }
                },
                {
                    "id": "_id",
                    "label": "被操作为",
                    "properties": ["_id", "@timestamp", "geoip.city_name", "操作动作", "操作状态", "操作详情", "用户"],
                    "outVertex": {
                        "id": "操作对象",
                        "label": "文档"
                    },
                    "inVertex": {
                        "id": "操作后对象",
                        "label": "文档"
                    }
                }
            ]
        }
    }

    def test_get_start_vertices_script(self):
        """
        测试筛选实体脚本的接口
        """
        query = {
            "label": ["用户"],
            "idField": ["用户"],
            "propertyValue": [
                {
                    "key": "用户",
                    "operator": "eq",
                    "value": ["巨昭"]
                }
            ]
        }
        self.req["query"] = query
        script_eq = "g.V([]).hasLabel(\"用户\", []).has(\"__.id.field\", \"用户\").has(\"用户\", P.eq(\"\\\"巨昭\\\"\")).dedup([]).toList()"
        # 测试eq操作符
        self.assertEqual(script_eq, GraphBaseManager().get_start_vertices_script(self.req))

        query["propertyValue"][0]["operator"] = "neq"
        self.req["query"] = query
        script_neq = "g.V([]).hasLabel(\"用户\", []).has(\"__.id.field\", \"用户\").has(\"用户\", P.neq(\"\\\"巨昭\\\"\")).dedup([]).toList()"
        # 测试neq操作符
        self.assertEqual(script_neq, GraphBaseManager().get_start_vertices_script(self.req))

        query["propertyValue"][0]["operator"] = "within"
        query["propertyValue"][0]["value"] = ["巨昭", "储成钢", "杨天忠"]
        self.req["query"] = query
        script_within = "g.V([]).hasLabel(\"用户\", []).has(\"__.id.field\", \"用户\").has(\"用户\", P.within(['\"巨昭\"', '\"储成钢\"', '\"杨天忠\"'])).dedup([]).toList()"
        # 测试within操作符
        self.assertEqual(script_within, GraphBaseManager().get_start_vertices_script(self.req))

        query["propertyValue"][0]["operator"] = "without"
        query["propertyValue"][0]["value"] = ["巨昭", "储成钢", "杨天忠"]
        self.req["query"] = query
        script_without = "g.V([]).hasLabel(\"用户\", []).has(\"__.id.field\", \"用户\").has(\"用户\", P.without(['\"巨昭\"', '\"储成钢\"', '\"杨天忠\"'])).dedup([]).toList()"
        # 测试without操作符
        self.assertEqual(script_without, GraphBaseManager().get_start_vertices_script(self.req))

        query["propertyValue"][0]["operator"] = "between"
        query["propertyValue"][0]["value"] = ["2015-01-01", "2015-12-31"]
        self.req["query"] = query
        script_between = "g.V([]).hasLabel(\"用户\", []).has(\"__.id.field\", \"用户\").has(\"用户\", P.between(\"2015-01-01\", \"2015-12-31\")).dedup([]).toList()"
        # 测试between操作符
        self.assertEqual(script_between, GraphBaseManager().get_start_vertices_script(self.req))

        query["propertyValue"][0]["operator"] = "inside"
        query["propertyValue"][0]["value"] = ["2015-01-01", "2015-12-31"]
        self.req["query"] = query
        script_inside = "g.V([]).hasLabel(\"用户\", []).has(\"__.id.field\", \"用户\").has(\"用户\", P.inside(\"2015-01-01\", \"2015-12-31\")).dedup([]).toList()"
        # 测试inside操作符
        self.assertEqual(script_inside, GraphBaseManager().get_start_vertices_script(self.req))

        query["propertyValue"][0]["operator"] = "outside"
        query["propertyValue"][0]["value"] = ["2015-01-01", "2015-12-31"]
        self.req["query"] = query
        script_outside = "g.V([]).hasLabel(\"用户\", []).has(\"__.id.field\", \"用户\").has(\"用户\", P.outside(\"2015-01-01\", \"2015-12-31\")).dedup([]).toList()"
        # 测试outside操作符
        self.assertEqual(script_outside, GraphBaseManager().get_start_vertices_script(self.req))

        # 测试label参数为空
        query["label"] = []
        self.req["query"] = query
        script_label_empty = "g.V([]).has(\"__.id.field\", \"用户\").has(\"用户\", P.outside(\"2015-01-01\", \"2015-12-31\")).dedup([]).toList()"
        self.assertEqual(script_label_empty, GraphBaseManager().get_start_vertices_script(self.req))

        # 测试label参数不存在
        query.pop("label")
        self.req["query"] = query
        script_label_notExist = "g.V([]).has(\"__.id.field\", \"用户\").has(\"用户\", P.outside(\"2015-01-01\", \"2015-12-31\")).dedup([]).toList()"
        self.assertEqual(script_label_notExist, GraphBaseManager().get_start_vertices_script(self.req))

        # 测试idField参数为空
        query["idField"] = []
        self.req["query"] = query
        script_idField_empty = "g.V([]).has(\"用户\", P.outside(\"2015-01-01\", \"2015-12-31\")).dedup([]).toList()"
        self.assertEqual(script_idField_empty, GraphBaseManager().get_start_vertices_script(self.req))

        # 测试idField参数不存在
        query.pop("idField")
        self.req["query"] = query
        script_idField_empty = "g.V([]).has(\"用户\", P.outside(\"2015-01-01\", \"2015-12-31\")).dedup([]).toList()"
        self.assertEqual(script_idField_empty, GraphBaseManager().get_start_vertices_script(self.req))

        # 测试limit参数存在
        self.req["limit"] = 5
        script_limit_exist = "g.V([]).has(\"用户\", P.outside(\"2015-01-01\", \"2015-12-31\")).dedup([]).limit(5).toList()"
        self.assertEqual(script_limit_exist, GraphBaseManager().get_start_vertices_script(self.req))

        # 测试req["query"]["propertyValue"][k]["value"]为空
        query["propertyValue"][0]["value"] = []
        self.req["query"] = query
        script_property_value_notExist = "g.V([]).dedup([]).limit(5).toList()"
        self.assertEqual(script_property_value_notExist, GraphBaseManager().get_start_vertices_script(self.req))

        # 测试req["query"]["propertyValue"]为空
        query["propertyValue"] = []
        self.req["query"] = query
        script_property_value_notExist = "g.V([]).dedup([]).limit(5).toList()"
        self.assertEqual(script_property_value_notExist, GraphBaseManager().get_start_vertices_script(self.req))

        self.req.pop("query")
        self.req.pop("limit")

    def test_get_vertices_neighbors_script(self):
        """
        测试获取相邻实体脚本的接口
        """
        vertex = {
            "id": "巨昭",
            "label": "用户"
        }
        self.req["vertex"] = vertex
        script_vertices_neighbors = "g.V(['巨昭']).hasLabel(\"用户\", []).dedup([]).both([]).dedup([]).toList()"
        self.assertEqual(script_vertices_neighbors, GraphBaseManager().get_vertices_neighbors_script(self.req))

        self.req.pop("vertex")

    def test_get_vertices_search_properties_script(self):
        """
        测试搜寻实体属性值脚本的接口
        """
        self.req["idField"] = "操作对象"
        self.req["propertyKey"] = "父路径"
        self.req["keyword"] = "产品"
        self.req["limit"] = 5

        script_vertices_search = "g.V([]).has(\"__.id.field\", \"操作对象\").has(\"父路径\", P.eq(\"产品\")).values(['父路径']).dedup([]).limit(5).toList()"
        self.assertEqual(script_vertices_search, GraphBaseManager().get_vertices_search_properties_script(self.req))

        self.req.pop("limit")
        script_vertices_search_without_limit = "g.V([]).has(\"__.id.field\", \"操作对象\").has(\"父路径\", P.eq(\"产品\")).values(['父路径']).dedup([]).toList()"
        self.assertEqual(script_vertices_search_without_limit, GraphBaseManager().get_vertices_search_properties_script(self.req))

        self.req.pop("idField")
        self.req.pop("propertyKey")
        self.req.pop("keyword")

    def test_get_edges_filter_script(self):
        """
        测试筛选关系脚本接口
        """
        query = {
            "outVertex": {
                "id": "胡佳妮"
            },
            "inVertex": {
                "id": "渠道售前现场培训报名表.xlsx"
            }
        }

        self.req["query"] = query
        script_edges_query_basic = "g.E([]).has(\"__.out.id\", \"胡佳妮\").has(\"__.in.id\", \"渠道售前现场培训报名表.xlsx\").toList()"
        self.assertEqual(script_edges_query_basic, GraphBaseManager().get_edges_filter_script(self.req))

        # 测试包含vertex/label参数
        self.req["query"]["outVertex"]["label"] = "用户"
        self.req["query"]["inVertex"]["label"] = "文档"
        script_edges_query_with_vertex_label = "g.E([]).has(\"__.out.id\", \"胡佳妮\").has(\"__.in.id\", \"渠道售前现场培训报名表.xlsx\").has(\"__.out.label\", \"用户\").has(\"__.in.label\", \"文档\").toList()"
        self.assertEqual(script_edges_query_with_vertex_label, GraphBaseManager().get_edges_filter_script(self.req))

        # 测试包含label参数
        self.req["query"]["label"] = ["操作"]
        script_edges_query_with_label = "g.E([]).has(\"__.out.id\", \"胡佳妮\").has(\"__.in.id\", \"渠道售前现场培训报名表.xlsx\").hasLabel(\"操作\", []).has(\"__.out.label\", \"用户\").has(\"__.in.label\", \"文档\").toList()"
        self.assertEqual(script_edges_query_with_label, GraphBaseManager().get_edges_filter_script(self.req))


        # 测试包含propertyValue参数
        self.req["query"]["propertyValue"]=[
            {
                "key": "操作动作",
                "operator": "eq",
                "value": ["上传"]
            }
        ]
        script_edges_query_with_propertyValue = "g.E([]).has(\"__.out.id\", \"胡佳妮\").has(\"__.in.id\", \"渠道售前现场培训报名表.xlsx\").hasLabel(\"操作\", []).has(\"__.out.label\", \"用户\").has(\"__.in.label\", \"文档\").has(\"操作动作\", P.eq(\"上传\")).toList()"
        self.assertEqual(script_edges_query_with_propertyValue, GraphBaseManager().get_edges_filter_script(self.req))

        # 测试包含from和size参数
        self.req["from"] = 0
        self.req["size"] = 5
        script_edges_from_size = "g.E([]).has(\"__.out.id\", \"胡佳妮\").has(\"__.in.id\", \"渠道售前现场培训报名表.xlsx\").hasLabel(\"操作\", []).has(\"__.out.label\", \"用户\").has(\"__.in.label\", \"文档\").has(\"操作动作\", P.eq(\"上传\")).range(0, 5).toList()"
        self.assertEqual(script_edges_from_size, GraphBaseManager().get_edges_filter_script(self.req))

        # 测试包含sort参数
        self.req["sort"] = [
            {
                "操作状态": "desc"
            }
        ]
        script_edges_sort = "g.E([]).has(\"__.out.id\", \"胡佳妮\").has(\"__.in.id\", \"渠道售前现场培训报名表.xlsx\").hasLabel(\"操作\", []).has(\"__.out.label\", \"用户\").has(\"__.in.label\", \"文档\").has(\"操作动作\", P.eq(\"上传\")).order().by(\"操作状态\",Order.decr).range(0, 5).toList()"
        self.assertEqual(script_edges_sort, GraphBaseManager().get_edges_filter_script(self.req))

        self.req.pop("from")
        self.req.pop("size")
        self.req.pop("sort")
        self.req.pop("query")

    def test_get_edge_group_count_by_label_script(self):
        """
        """
        query = {
            "outVertex": {
                "id": "胡佳妮"
            },
            "inVertex": {
                "id": "渠道售前现场培训报名表.xlsx"
            }
        }

        self.req["query"] = query
        script_edges_query_basic = "g.E([]).has(\"__.out.id\", \"胡佳妮\").has(\"__.in.id\", \"渠道售前现场培训报名表.xlsx\").groupCount().by(T.label).toList()"
        self.assertEqual(script_edges_query_basic, GraphBaseManager().get_edge_group_count_by_label_script(self.req))

        # 测试包含vertex/label参数
        self.req["query"]["outVertex"]["label"] = "用户"
        self.req["query"]["inVertex"]["label"] = "文档"
        script_edges_query_with_vertex_label = "g.E([]).has(\"__.out.id\", \"胡佳妮\").has(\"__.in.id\", \"渠道售前现场培训报名表.xlsx\").has(\"__.out.label\", \"用户\").has(\"__.in.label\", \"文档\").groupCount().by(T.label).toList()"
        self.assertEqual(script_edges_query_with_vertex_label, GraphBaseManager().get_edge_group_count_by_label_script(self.req))

        # 测试包含label参数
        # self.req["query"]["label"] = "操作"
        # script_edges_query_with_label = "g.E([]).has(\"__.out.id\", \"胡佳妮\").has(\"__.in.id\", \"渠道售前现场培训报名表.xlsx\").hasLabel(\"操作\", []).has(\"__.out.label\", \"用户\").has(\"__.in.label\", \"文档\").groupCount().by(T.label)"
        # self.assertEqual(script_edges_query_with_label, GraphBaseManager().get_edge_group_count_by_label_script(self.req))


        # # 测试包含propertyValue参数
        self.req["query"]["propertyValue"]=[
            {
                "key": "操作动作",
                "operator": "eq",
                "value": ["上传"]
            }
        ]
        script_edges_query_with_propertyValue = "g.E([]).has(\"__.out.id\", \"胡佳妮\").has(\"__.in.id\", \"渠道售前现场培训报名表.xlsx\").has(\"__.out.label\", \"用户\").has(\"__.in.label\", \"文档\").has(\"操作动作\", P.eq(\"上传\")).groupCount().by(T.label).toList()"
        self.assertEqual(script_edges_query_with_propertyValue, GraphBaseManager().get_edge_group_count_by_label_script(self.req))

        self.req.pop("query")

    def test_get_edge_group_count_by_property_script(self):
        """
        """
        query = {
            "outVertex": {
                "id": "胡佳妮"
            },
            "inVertex": {
                "id": "渠道售前现场培训报名表.xlsx"
            },
            "label": "操作"
        }

        self.req["propertyKey"] = "操作动作"
        self.req["query"] = query
        script_edges_query_basic = "g.E([]).hasLabel(\"操作\", []).has(\"__.out.id\", \"胡佳妮\").has(\"__.in.id\", \"渠道售前现场培训报名表.xlsx\").groupCount().by(\"操作动作\").toList()"
        self.assertEqual(script_edges_query_basic, GraphBaseManager().get_edge_group_count_by_property_script(self.req))

        # 测试包含vertex/label参数
        self.req["query"]["outVertex"]["label"] = "用户"
        self.req["query"]["inVertex"]["label"] = "文档"
        script_edges_query_with_vertex_label = "g.E([]).hasLabel(\"操作\", []).has(\"__.out.id\", \"胡佳妮\").has(\"__.in.id\", \"渠道售前现场培训报名表.xlsx\").has(\"__.out.label\", \"用户\").has(\"__.in.label\", \"文档\").groupCount().by(\"操作动作\").toList()"
        self.assertEqual(script_edges_query_with_vertex_label, GraphBaseManager().get_edge_group_count_by_property_script(self.req))

        # # 测试包含label参数
        # self.req["query"]["label"] = "操作"
        # script_edges_query_with_label = "g.E([]).has(\"__.out.id\", \"胡佳妮\").has(\"__.in.id\", \"渠道售前现场培训报名表.xlsx\").hasLabel(\"操作\", []).has(\"__.out.label\", \"用户\").has(\"__.in.label\", \"文档\").groupCount().by(\"操作动作\")"
        # self.assertEqual(script_edges_query_with_label, GraphBaseManager().get_edge_group_count_by_property_script(self.req))


        # 测试包含propertyValue参数
        self.req["query"]["propertyValue"] = [
            {
                "key": "操作动作",
                "operator": "eq",
                "value": ["上传"]
            }
        ]
        script_edges_query_with_propertyValue = "g.E([]).hasLabel(\"操作\", []).has(\"__.out.id\", \"胡佳妮\").has(\"__.in.id\", \"渠道售前现场培训报名表.xlsx\").has(\"__.out.label\", \"用户\").has(\"__.in.label\", \"文档\").has(\"操作动作\", P.eq(\"上传\")).groupCount().by(\"操作动作\").toList()"
        self.assertEqual(script_edges_query_with_propertyValue, GraphBaseManager().get_edge_group_count_by_property_script(self.req))

if __name__ == "__main__":
    unittest.main()
