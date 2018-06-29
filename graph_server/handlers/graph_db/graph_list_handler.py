#!/usr/bin/env python
# -*- coding:utf-8 -*-
import simplejson as json
import tornado.web
from log.log_graph import My_Logger
from modules.graph.graph_db_manager import GraphDBManager
import time

class GraphListHandler(tornado.web.RequestHandler):
    """
    加载所有关系图谱的名称类
    """

    def get(self):
        """
        处理加载所有关系图谱的get请求
        """
        log = My_Logger()
        log.info("["+time.strftime('%Y-%m-%d %H:%M:%S',time.localtime(time.time()))+"]"+" Handler For Graph Load All Name......")

        db = GraphDBManager()

        # 如果该记录存在，那么就调用load_record()来加载该记录内容
        graph_names = db.list()
        db.close()
        self.write(json.dumps(graph_names,ensure_ascii=False, encoding="utf-8"))
