#!/usr/bin/env python
# -*- coding:utf-8 -*-
import os
import json
import jpype
import requests
import re
from jpype import *
from log.log_graph import My_Logger
from utils.error.ar_exception import ARGraphException
from utils.error.status_pool import GRAPH_STATUS_POOL
from utils.global_info import (ELASTIC_SERVER,
                               SEARCH_PARSE
                               )


class GraphBaseManager():
    """
    """
    def __init__(self):
        self.log = My_Logger()

    def get_graph(self, graph_json):
        """
        启动JVM 创建RainbowGraph及g
        """
        jarpath = os.path.join(
            os.path.dirname(os.path.dirname(os.path.dirname(os.path.realpath(__file__)))),
            "rainbow")

        if not isJVMStarted():
            startJVM(jpype.getDefaultJVMPath(), "-ea", "-Djava.ext.dirs=%s" % (jarpath))

        rainbow_graph = jpype.JClass('com.eisoo.rainbow.gremlin.structure.RainbowGraph')
        graph_traversal_source = jpype.JClass('org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource')
        rainbow_graph_instance = rainbow_graph.open(json.dumps(graph_json))
        g = graph_traversal_source(rainbow_graph_instance)

        return g

    def get_dsl(self, data_set):
        """
        获取dsl语句
        """
        dsl = requests.post(SEARCH_PARSE, data=json.dumps(data_set))
        # dsl = requests.post("http://192.168.84.111:11002/v1/search/parse", data=json.dumps(data_set))

        if dsl.status_code == 200:
            if dsl.json()["index"]:
                new_dsl = {}
                new_dsl["class"] = "com.eisoo.rainbow.elastic.ElasticSourceProvider"
                new_dsl["dataSet"] = {}
                new_dsl["dataSet"]["url"] = ELASTIC_SERVER
                # new_dsl["dataSet"]["url"] = "http://192.168.84.111:9200"
                new_dsl["dataSet"]["index"] = dsl.json()["index"]
                new_dsl["dataSet"]["query"] = dsl.json()["query"]
                return new_dsl
            else:
               new_dsl = {}
               return new_dsl
        else:
            return dsl.text



    def get_vertices_response(self, script, req):
        """
        接收gemlin脚本，调用rainbow插件解析并返回查询的实体结果
        """
        new_dsl = self.get_dsl(req["dataSet"])
        # 如果new_dsl返回为空，说明search中的index为空，那么直接返回空。
        if not new_dsl:
            return new_dsl
        # 如果search服务器不正常，一定包含code参数，那么就将search返回的信息返回到前端
        if "code" in new_dsl:
            return eval(new_dsl)
        graph_json = {}
        graph_json["class"] = "com.eisoo.rainbow.elastic.ElasticSourceProvider"
        graph_json["dataSet"] = new_dsl["dataSet"]
        graph_json["graphSchema"] = req["graphSchema"]

        g = self.get_graph(graph_json)

        P = jpype.JClass('org.apache.tinkerpop.gremlin.process.traversal.P')
        JSONObject = jpype.JClass("org.json.JSONObject")


        # 如果是../vertex/../search接口的话调用如下的方法
        if req.get("keyword", ""):
            try:
                vertices_properties_result = eval(script)
            except Exception as e:
                err_info = {"code":500,"decription":"graph server have encountered a internal error","detail":str(e)}
                return err_info
            vertices_search_response = {"values": []}
            for i in range(0, len(vertices_properties_result)):
                vertices_search_response["values"].append(vertices_properties_result.get(i))
            return vertices_search_response
        # 如果是../vertex/filter和../vertex/neighbors 调用如下的方法
        else:
            try:
                vertices_result = eval(script)
            except Exception as e:
                err_info = {"code":500,"decription":"graph server have encountered a internal error","detail":str(e)}
                return err_info
            vertices_response = {"vertices": []}
            i = 0
            for k in range(0, len(vertices_result)):
                tmp = eval(vertices_result[k].toString().replace("null","\"null\""))
                vertices_response["vertices"].append(tmp)
        return vertices_response

    def is_path_or_unicode_string(self, str):
        """
        """
        if('\\u' in str):
            #如果是\\u开头的unicode字段，那么就返回标识符0
            return 0
        elif('\\' in str or '\"' in str):
            return 1
        else:
            return 2

    def replace_escape_or_unicode_character_in_parse_match(self, str):
        """
        unicode和路径字符串需要短语匹配，所以需要进行反斜杠的替换
        """
        if(self.is_path_or_unicode_string(str) == 0):
            return str.replace('\\u', '\\\\u')
        elif(self.is_path_or_unicode_string(str) == 1):
            return str.replace('\\', '\\\\\\\\').replace('\"', '\\\\\\\"')
        else:
            return str

    def replace_escape_or_unicode_character_in_term_match(self, str):
        """
        term精确匹配中也需要根据情况进行反斜杠的替换
        """
        if(self.is_path_or_unicode_string(str) == 0):
            return str
        elif(self.is_path_or_unicode_string(str) == 1):
            return str.replace('\\','\\\\').replace('\"', '\\\"')
        else:
            return str

    def get_start_vertices_script(self, req):
        """
        根据条件筛选实体的接口，返回gemlin脚本
        """
        def quote(s):
            return "\\\"" + self.replace_escape_or_unicode_character_in_parse_match(s) + "\\\""

        def quote_with(s):
            return "\"" + s + "\""

        object_java = []
        string_java = []
        string_java_empty = []

        script = "g.V(" + str(object_java) + ")"
        if req["query"].get("label"):
            for i in range(0, len(req["query"]["label"])):
                script = script + ".hasLabel(\"%s\", %s)" % (req["query"]["label"][i], str(string_java_empty))

        if req["query"].get("idField"):
            for i in range(0, len(req["query"]["idField"])):
                script = script + ".has(\"__.id.field\", \"" + str(req["query"]["idField"][0]) + "\")"

        while req["query"].get("propertyValue"):
            i = 0
            while i < len(req["query"]["propertyValue"]):
                key = req["query"]["propertyValue"][i]["key"]
                if req["query"]["propertyValue"][i]["operator"] == "eq":
                    value = req["query"]["propertyValue"][i]["value"]
                    if value:
                        script = script + ".has(\"" + key + "\", P.eq(\"" + quote(value[0]) +"\"))"
                elif req["query"]["propertyValue"][i]["operator"] == "neq":
                    value = req["query"]["propertyValue"][i]["value"]
                    if value:
                        script = script + ".has(\"" + key + "\", P.neq(\"" + quote(value[0]) + "\"))"
                elif req["query"]["propertyValue"][i]["operator"] == "within":
                    value = req["query"]["propertyValue"][i]["value"]
                    for j in range(0, len(value)):
                        string_java.append(quote_with(value[j]))
                    if value:
                        script = script + ".has(\"" + key + "\", P.within("+str(string_java)+"))"

                elif req["query"]["propertyValue"][i]["operator"] == "without":
                    value = req["query"]["propertyValue"][i]["value"]
                    for j in range(0, len(value)):
                        string_java.append(quote(value[j]))
                    if value:
                        script = script + ".has(\"" + key + "\", P.without(" + str(string_java) + "))"
                elif req["query"]["propertyValue"][i]["operator"] == "between":
                    value = req["query"]["propertyValue"][i]["value"]
                    if value:
                        script = script + ".has(\"" + key + "\", P.between(\"" + value[0] + "\", \"" + value[1] + "\"))"
                elif req["query"]["propertyValue"][i]["operator"] == "inside":
                    value = req["query"]["propertyValue"][i]["value"]
                    if value:
                        script = script + ".has(\"" + key + "\", P.inside(\"" + value[0] + "\", \"" + value[1] + "\"))"
                elif req["query"]["propertyValue"][i]["operator"] == "outside":
                    value = req["query"]["propertyValue"][i]["value"]
                    if value:
                        script = script + ".has(\"" + key + "\", P.outside(\"" + value[0] + "\", \"" + value[1] + "\"))"
                i = i + 1
            break
        if req.get("limit", ""):
            limit = req["limit"]
            script = script + ".dedup(" + str(string_java_empty) + ").limit(" + str(limit) + ")"
        else:
            script = script + ".dedup(" + str(string_java_empty) + ")"

        script = script + ".toList()"


        self.log.info("The Vertex Filter script is: %s" % script)

        return script

    def get_vertices_neighbors_script(self, req):
        """
        获取实体的相邻实体的接口，返回gemlin脚本
        """
        vertex_id = req["vertex"]["id"]
        vertex_label = req["vertex"]["label"]
        object_java = ["" + vertex_id + ""]
        string_java = []

        script = "g.V(" + str(object_java) + ").hasLabel(\"" + vertex_label + "\", " + str(string_java) + ").dedup(" + str(string_java) + ").both(" + str(string_java) + ").dedup(" + str(string_java) + ").toList()"
        self.log.info("The Vertex Neighbors script is: %s" % script)

        return script

    def get_vertices_search_properties_script(self, req):
        """
        获取筛选的实体属性的接口，返回gemlin脚本
        """
        object_java = []
        string_java = []
        string_java_value = ["" + req["propertyKey"] + ""]

        if req.get("limit", ""):
            limit = req["limit"]
            script = "g.V(" + str(object_java) + ").has(\"__.id.field\", \"" + req["idField"] + "\").has(\"" + req["propertyKey"] + "\", P.eq(\"" + req["keyword"] + "\")).values("+str(string_java_value) + ").dedup("+str(string_java)+").limit(" + str(limit) + ").toList()"
        else:
            script = "g.V(" + str(object_java) + ").has(\"__.id.field\", \"" + req["idField"] + "\").has(\"" + req["propertyKey"] + "\", P.eq(\"" + req["keyword"] + "\")).values("+str(string_java_value) + ").dedup("+str(string_java)+").toList()"

        self.log.info("The Vertex Property Search script is: %s" % script)
        return script

    def get_common_query(self, script, req):
        """
        共有的查询条件
        """
        string_java = []
        string_java_empty = []

        # 判断查询条件是否有outVertex.label
        while req["query"]["outVertex"].get("label", ""):
            script = script + "has(\"__.out.label\", \"" + req["query"]["outVertex"]["label"] + "\")."
            break

        # 判断查询条件是否有inVertex.label
        while req["query"]["inVertex"].get("label", ""):
            script = script + "has(\"__.in.label\", \"" + req["query"]["inVertex"]["label"] + "\")."
            break

        # 判断查询条件是否有propertyValue
        while req["query"].get("propertyValue"):
            i = 0
            while i < len(req["query"]["propertyValue"]):
                key = req["query"]["propertyValue"][i]["key"]
                if req["query"]["propertyValue"][i]["operator"] == "eq":
                    value = req["query"]["propertyValue"][i]["value"]
                    script = script + "has(\"" + key + "\", P.eq(\"" + value[0] + "\"))."
                elif req["query"]["propertyValue"][i]["operator"] == "neq":
                    value = req["query"]["propertyValue"][i]["value"]
                    script = script + "has(\"" + key + "\", P.neq(\"" + value[0] + "\"))."
                elif req["query"]["propertyValue"][i]["operator"] == "within":
                    value = req["query"]["propertyValue"][i]["value"]
                    for j in range(0, len(value)):
                        string_java.append(value[j])
                    script = script + "has(\"" + key + "\",P.within("+str(string_java_empty)+"))."
                elif req["query"]["propertyValue"][i]["operator"] == "without":
                    value = req["query"]["propertyValue"][i]["value"]
                    for j in range(0, len(value)):
                        string_java.append(value[j])
                    script = script + "has(\"" + key + "\",P.without("+str(string_java_empty)+"))."
                elif req["query"]["propertyValue"][i]["operator"] == "between":
                    value = req["query"]["propertyValue"][i]["value"]
                    script = script + "has(\"" + key + "\", P.between(\"" + value[0] + "\", \"" + value[1] + "\"))."
                elif req["query"]["propertyValue"][i]["operator"] == "inside":
                    value = req["query"]["propertyValue"][i]["value"]
                    script = script + "has(\"" + key + "\", P.inside(\"" + value[0] + "\", \"" + value[1] + "\"))."
                elif req["query"]["propertyValue"][i]["operator"] == "outside":
                    value = req["query"]["propertyValue"][i]["value"]
                    script = script + "has(\"" + key + "\", P.outside(\"" + value[0] + "\", \"" + value[1] + "\"))."
                i = i + 1
            break
        return script

    def get_edges_filter_script(self, req):
        """
        筛选关系的接口，返回gemlin脚本
        """
        object_java = []
        string_java_empty = []

        script = "g.E(" + str(object_java) + ").has(\"__.out.id\", \""+ self.replace_escape_or_unicode_character_in_term_match(req["query"]["outVertex"]["id"])+"\").has(\"__.in.id\", \"" + self.replace_escape_or_unicode_character_in_term_match(req["query"]["inVertex"]["id"]) + "\")."

        # 判断查询条件是否有[query][label]
        while req["query"].get("label", ""):
            for i in range(0,len(req["query"]["label"])):
                script = script + "hasLabel(\"" + req["query"]["label"][i] + "\", " + str(string_java_empty) + ")."
            break

        script = self.get_common_query(script, req)

        # 判断查询条件是否有sort
        while req.get("sort", ""):
            for k in range(0, len(req["sort"])):
                for h in range(0, len(list(req["sort"][k].keys()))):
                    if req["sort"][k]["" + list(req["sort"][k].keys())[h] + ""] == "desc":
                        script = script + "order().by(\"" + list(req["sort"][k].keys())[h] + "\",Order.decr)."
                    elif req["sort"][k]["" + list(req["sort"][k].keys())[h] + ""] == "asc":
                        script = script + "order().by(\"" + list(req["sort"][k].keys())[h] + "\",Order.incr)."
            break
        # 判断查询条件是否有from size
        if req.get("from", "") and req.get("size", ""):
            if req.get("from", "") < 0 or req.get("size", "") > 10000:
                script = script
            else:
                script = script + "range(" + str(req["from"]) + "," + str(req["from"] + req["size"]) + ")."
        elif not req.get("from", "") and req.get("size",""):
            if req.get("size", "") > 10000:
                script = script
            else:
                script = script + "range(0, " + str(req["size"]) + ")."
        elif req.get("from", "") and not req.get("size", ""):
            if req.get("from", "") < 0:
                script = script
            else:
                script = script + "range(" + str(req["from"]) + ", " + str(req["from"] + 10) + ")." 
                
        script = script + "toList()"
        self.log.info("The Edge Filter script is: %s" % script)

        return script

    def get_edges_response(self, script, req, *tags):
        """
        接收gemlin脚本，调用rainbow插件解析并返回查询的关系结果
        """
        new_dsl = self.get_dsl(req["dataSet"])
        if not new_dsl:
            return new_dsl
        if new_dsl.get("code",""):
            return new_dsl
        graph_json = {}
        graph_json["class"] = "com.eisoo.rainbow.elastic.ElasticSourceProvider"
        graph_json["dataSet"] = new_dsl["dataSet"]
        graph_json["graphSchema"] = req["graphSchema"]

        g = self.get_graph(graph_json)
        JSONObject = jpype.JClass("org.json.JSONObject")
        Order =jpype.JClass('org.apache.tinkerpop.gremlin.process.traversal.Order')
        P = jpype.JClass('org.apache.tinkerpop.gremlin.process.traversal.P')
        T = jpype.JClass("org.apache.tinkerpop.gremlin.structure.T")

        if tags:
            try:
                edges_filter_result = eval(script)
            except Exception as e:
                err_info = {"code":500,"decription":"graph server have encountered a internal error","detail":str(e)}
                return err_info
            # 组合实体信息返回
            edges_response = {"edges": []}
            for k in range(0, len(edges_filter_result)):
                tmp = eval(edges_filter_result[k].toString())
                edges_response["edges"].append(tmp)
            return edges_response
        else:
            java_script = "JSONObject.valueToString(" + script + ".get(0))"
            try:
                group_by_property_result = eval(java_script)
            except Exception as e:
                err_info = {"code":500,"decription":"graph server have encountered a internal error","detail":str(e)}
                return err_info
            buckets = {"buckets": []}
            i = 0
            for k in eval(group_by_property_result):
                buckets["buckets"].append({})
                buckets["buckets"][i]["key"] = k
                buckets["buckets"][i]["value"] = eval(group_by_property_result)[k]
                i = i + 1

            return buckets

    def get_edge_group_count_by_property_script(self, req):
        """
        根据属性分组统计关系的接口，返回gemlin脚本
        """
        object_java = []
        string_java_empty = []


        script = "g.E(" + str(object_java) + ").hasLabel(\"" + req["query"]["label"] + "\", " + str(string_java_empty) + ").has(\"__.out.id\", \"" + self.replace_escape_or_unicode_character_in_term_match(req["query"]["outVertex"]["id"])  + "\").has(\"__.in.id\", \"" +self.replace_escape_or_unicode_character_in_term_match(req["query"]["inVertex"]["id"])  + "\")."

        script = self.get_common_query(script, req) + "groupCount().by(\"" + req["propertyKey"] + "\").toList()"

        self.log.info("The Edge GroupBy Property script is: %s" % script)
        return script


    def get_edge_group_count_by_label_script(self, req):
        """
        根据标签分组统计关系的接口，返回gemlin脚本
        """
        object_java = []
        string_java_empty = []


        script = "g.E(" + str(object_java) + ").has(\"__.out.id\", \"" + self.replace_escape_or_unicode_character_in_term_match(req["query"]["outVertex"]["id"]) + "\").has(\"__.in.id\", \"" + self.replace_escape_or_unicode_character_in_term_match(req["query"]["inVertex"]["id"]) + "\")."

        script = self.get_common_query(script, req) + "groupCount().by(T.label).toList()"

        self.log.info("The Edge GroupBy Label script is: %s" % script)
        return script
