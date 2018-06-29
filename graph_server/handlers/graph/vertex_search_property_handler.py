#!/usr/bin/env python
# -*- coding:utf-8 -*-
import simplejson as json
import tornado.web
import time
from log.log_graph import My_Logger
from modules.graph.graph_base_manager import GraphBaseManager
from arpylibs.basehandler.handler_result import deco_deal_error


class VertexPropertyHandler(tornado.web.RequestHandler):
    """
    搜索实体属性类
    """
    @deco_deal_error
    def post(self):
        """
        """
        log = My_Logger()
        log.info("[" + time.strftime('%Y-%m-%d %H:%M:%S',
                                     time.localtime(time.time())) + "]" + " Handler For Vertex Property Search......")

        req = json.loads(self.request.body.decode('utf-8').replace('\\u','\\\\u'))

        vertices_search_properties_script = GraphBaseManager().get_vertices_search_properties_script(req)
        vertices_search_properties_response = GraphBaseManager().get_vertices_response(vertices_search_properties_script, req)
        self.write(json.dumps(vertices_search_properties_response, ensure_ascii=False, encoding="utf-8"))
