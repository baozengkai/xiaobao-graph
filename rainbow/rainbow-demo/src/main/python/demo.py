#!/usr/bin/env python
# -*- coding:utf-8 -*-

import os
import jpype
import json
import pprint


def graph():
    jar_path = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(
        os.path.dirname(os.path.realpath(__file__))))), "target", "jars")
    print(jar_path)
    jpype.startJVM(jpype.getDefaultJVMPath(), "-ea", "-Djava.ext.dirs={}".format(jar_path))
    RainbowGraph = jpype.JClass("com.eisoo.rainbow.gremlin.structure.RainbowGraph")

    graph_conf_path = os.path.join(os.path.dirname(os.path.dirname(
        os.path.realpath(__file__))), "resources", "elastic.json")
    print(graph_conf_path)
    graph_conf_file = open(graph_conf_path, encoding="utf-8")
    graph_conf = graph_conf_file.read()
    g = RainbowGraph.open(graph_conf).traversal()
    return g


def print_eval(script):
    print("\n==== gremlin begin ====")
    print(script)
    result = eval(script)
    values = json.loads(result)
    print("size:", len(values))
    pprint.pprint(values)
    print("==== gremlin end ====\n")


if __name__ == '__main__':
    g = graph()
    P = jpype.JClass("org.apache.tinkerpop.gremlin.process.traversal.P")
    Order = jpype.JClass("org.apache.tinkerpop.gremlin.process.traversal.Order")
    T = jpype.JClass("org.apache.tinkerpop.gremlin.structure.T")
    JSONObject = jpype.JClass("org.json.JSONObject")

    print("====================== /graph/vertex/filter =====================")
    print_eval(
        "g.V([]).has('__.id.field', '用户').has('用户', P.eq('陈武'))"
        ".dedup([]).toList().toString()")
    print_eval(
        "g.V([]).hasLabel('文档', []).has('@timestamp', P.between('2015-01-01', '2015-12-31'))"
        ".dedup([]).limit(3).toList().toString()")
    print_eval(
        "g.V([]).hasLabel('用户', ['用户', '操作对象'])"
        ".has('__.id.field', P.within(['用户']))"
        ".has('用户', P.within(['陈武', '杨']))"
        ".has('用户', P.neq('陈武（Max）'))"
        ".dedup([]).limit(10).toList().toString()")

    print("====================== /graph/vertex/property/value/search =====================")
    print_eval(
        "JSONObject.valueToString(g.V([]).has('__.id.field', '用户').has('用户', P.eq('陈'))"
        ".values(['用户']).dedup([]).toList())")
    print_eval(
        "JSONObject.valueToString(g.V([]).has('__.id.field', '用户').has('用户', P.eq('陈'))"
        ".values(['用户']).dedup([]).limit(3).toList())")
    print_eval(
        "JSONObject.valueToString(g.V([])"
        ".has('__.id.field', '操作对象').has('父路径', P.eq('产品')).values(['父路径'])"
        ".dedup([]).limit(3).toList())")
    print_eval(
        "JSONObject.valueToString(g.V([])"
        ".has('__.id.field', '操作后对象').has('父路径', P.eq('产品'))"
        ".values(['父路径']).dedup([]).limit(3).toList())")

    print("====================== /graph/vertex/neighbors =====================")
    print_eval(
        "g.V(['渠道售前现场培训报名表.xlsx']).hasLabel('文档', [])"
        ".both([]).limit(3).toList().toString()")

    print("====================== /graph/edge/filter =====================")
    print_eval(
        "JSONObject.valueToString(g.E([]).hasLabel('操作', [])"
        ".has('__.out.id', '胡佳妮').has('__.out.label', '用户')"
        ".has('__.in.id', '渠道售前现场培训报名表.xlsx').has('__.in.label', '文档')"
        ".count().toList())")
    print_eval(
        "g.E([]).hasLabel('操作', [])"
        ".has('__.out.id', '胡佳妮').has('__.out.label', '用户')"
        ".has('__.in.id', '渠道售前现场培训报名表.xlsx').has('__.in.label', '文档')"
        ".order().by('@timestamp', Order.decr)"
        ".order().by('操作动作', Order.decr)"
        ".order().by('操作状态', Order.incr)"
        ".range(0, 3).toList().toString()")

    print("====================== /graph/edge/groupCountByLabel =====================")
    print_eval(
        "JSONObject.valueToString(g.E([])"
        ".has('__.out.id', '胡佳妮').has('__.out.label', '用户')"
        ".has('__.in.id', '渠道售前现场培训报名表.xlsx').has('__.in.label', '文档')"
        ".groupCount().by(T.label).toList().get(0))")

    print("====================== /graph/edge/groupCountByProperty =====================")
    print_eval(
        "JSONObject.valueToString(g.E([]).hasLabel('操作', [])"
        ".has('__.out.id', '胡佳妮').has('__.in.id', '渠道售前现场培训报名表.xlsx')"
        ".groupCount().by('操作动作').toList().get(0))")

    print_eval(
        "JSONObject.valueToString(g.E([]).hasLabel('操作后', [])"
        ".has('__.out.id', '胡佳妮').has('__.in.id', '渠道售前现场培训报名表.xlsx')"
        ".groupCount().by('操作动作').toList().get(0))")
