#!/usr/bin/env python
# -*- coding:utf-8 -*-
import simplejson as json
import tornado.web
import time
from log.log_graph import My_Logger
from modules.graph.graph_base_manager import GraphBaseManager
from arpylibs.basehandler.handler_result import deco_deal_error


class VertexFilterHandler(tornado.web.RequestHandler):
    """
    实体筛选类
    """
    @deco_deal_error
    def post(self):
        """
        """
        log = My_Logger()
        log.info("[" + time.strftime('%Y-%m-%d %H:%M:%S',
                                     time.localtime(time.time())) + "]" + " Handler For Vertex Filter......")

        req = json.loads(self.request.body.decode('utf-8').replace('\\u','\\\\u'))

        start_vertices_script = GraphBaseManager().get_start_vertices_script(req)
        start_vertices_response = GraphBaseManager().get_vertices_response(start_vertices_script, req)
        self.write(json.dumps(start_vertices_response, ensure_ascii=False, encoding="utf-8"))
