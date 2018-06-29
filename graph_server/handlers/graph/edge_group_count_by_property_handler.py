#!/usr/bin/env python
# -*- coding:utf-8 -*-
import simplejson as json
import tornado.web
import time
from log.log_graph import My_Logger
from modules.graph.graph_base_manager import GraphBaseManager
from arpylibs.basehandler.handler_result import deco_deal_error


class EdgeGroupCountByPropertyHandler(tornado.web.RequestHandler):
    """
    根据标签分组统计类
    """
    @deco_deal_error
    def post(self):
        """
        """
        log = My_Logger()
        log.info("[" + time.strftime('%Y-%m-%d %H:%M:%S',
                                     time.localtime(time.time())) + "]" + "Handler For Edge GroupBy Property......")

        req = json.loads(self.request.body.decode('utf-8').replace('\\u','\\\\u'))

        edges_group_property_script = GraphBaseManager().get_edge_group_count_by_property_script(req)
        edges_group_property_response = GraphBaseManager().get_edges_response(edges_group_property_script, req)
        self.write(json.dumps(edges_group_property_response, ensure_ascii=False, encoding="utf-8"))
