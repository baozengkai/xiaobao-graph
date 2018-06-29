#!/usr/bin/env python
# -*- coding:utf-8 -*-
import simplejson as json
import tornado.web
from log.log_graph import My_Logger
from modules.graph.graph_db_manager import GraphDBManager
import time

class GraphExistsHandler(tornado.web.RequestHandler):
    """
    判断关系图谱是否存在类
    """

    def post(self):
        """
        处理关系图谱是否存在的post请求
        """
        log = My_Logger()
        log.info("[" + time.strftime('%Y-%m-%d %H:%M:%S',time.localtime(time.time())) + "]" + " Handler For Graph Exists......")

        req = json.loads(self.request.body.decode('utf-8'))
        db = GraphDBManager()

        # 如果存在相应的数据，说明已经存在，则返回500错误
        if(db.is_exists_record(req)):
            success_info = {"status": "OK", "detail": "%s exists" % (req["graphName"])}
            db.close()
            self.write(json.dumps(success_info, ensure_ascii=False, encoding="utf-8"))
        # 如果不存在相应的数据，说明不存在同名，则执行保存数据操作
        else:
            db.close()
            err_info = {"status": "FAILED", "detail": "%s not exists!" % (req["graphName"])}
            self.write(json.dumps(err_info, ensure_ascii=False, encoding="utf-8"))