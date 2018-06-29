#!/usr/bin/env python
# -*- coding:utf-8 -*-
import simplejson as json
import tornado.web
import time
from log.log_graph import My_Logger
from modules.graph.graph_base_manager import GraphBaseManager
from arpylibs.basehandler.handler_result import deco_deal_error


class VertexNeighborsHandler(tornado.web.RequestHandler):
    """
    获取邻居实体类
    """
    @deco_deal_error
    def post(self):
        """
        """
        log = My_Logger()
        log.info("[" + time.strftime('%Y-%m-%d %H:%M:%S',
                                     time.localtime(time.time())) + "]" + " Handler For Vertex neighbors......")

        req = json.loads(self.request.body.decode('utf-8').replace('\\u','\\\\u'))

        vertices_neighbors_script = GraphBaseManager().get_vertices_neighbors_script(req)
        vertices_neighbors_response = GraphBaseManager().get_vertices_response(vertices_neighbors_script, req)
        self.write(json.dumps(vertices_neighbors_response, ensure_ascii=False, encoding="utf-8"))
