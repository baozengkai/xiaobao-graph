#!/usr/bin/env python
# -*- coding:utf-8 -*-
import simplejson as json
import tornado.web
import time
from log.log_graph import My_Logger
from modules.graph.graph_base_manager import GraphBaseManager
from arpylibs.basehandler.handler_result import deco_deal_error


class EdgeFilterHandler(tornado.web.RequestHandler):
    """
    关系筛选类
    """
    @deco_deal_error
    def post(self):
        """
        """
        log = My_Logger()
        log.info("[" + time.strftime('%Y-%m-%d %H:%M:%S',
                                     time.localtime(time.time())) + "]" + " Handler For Edge Filter......")

        req = json.loads(self.request.body.decode('utf-8').replace('\\u','\\\\u'))

        edges_filter_script = GraphBaseManager().get_edges_filter_script(req)
        edges_filter_response = GraphBaseManager().get_edges_response(edges_filter_script, req, tuple("filter"))
        self.write(json.dumps(edges_filter_response, ensure_ascii=False, encoding="utf-8"))
