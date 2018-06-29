#!/usr/bin/env python
# -*- coding:utf-8 -*-
import simplejson as json
import tornado.web
from log.log_graph import My_Logger
from modules.graph.graph_db_manager import GraphDBManager
import time

class GraphLoadHandler(tornado.web.RequestHandler):
    """
    加载类
    """

    def post(self):
        """
        处理关系图谱加载的post请求
        """
        log = My_Logger()
        log.info("["+time.strftime('%Y-%m-%d %H:%M:%S',time.localtime(time.time()))+"]"+" Handler For Graph Load......")

        req = json.loads(self.request.body.decode('utf-8'))
        db = GraphDBManager()

        # 如果该记录存在，那么就调用load_record()来加载该记录内容
        if(db.is_exists_record(req)):
            json_value = db.load_record(req)
            db.close()
            self.write(json.dumps(json_value))
        # 如果记录不存在，那么返回错误信息，表明该记录不存在
        else:
            db.close()
            err_info = {"status":"FAILED","detail":"Load Failed.The %s record does not esists!"% (req["graphName"])}
            self.write(json.dumps(err_info, ensure_ascii=False, encoding="utf-8"))