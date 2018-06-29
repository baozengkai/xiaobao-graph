#!/usr/bin/env python
# -*- coding:utf-8 -*-
import simplejson as json
import tornado.web
from log.log_graph import My_Logger
from modules.graph.graph_db_manager import GraphDBManager
import time

class GraphSaveHandler(tornado.web.RequestHandler):
    """
    保存类
    """

    def post(self):
        """
        处理关系图谱保存的post请求
        """
        log = My_Logger()
        log.info("[" + time.strftime('%Y-%m-%d %H:%M:%S',time.localtime(time.time())) + "]" + " Handler For Graph Save......")

        req = json.loads(self.request.body.decode('utf-8'))
        db = GraphDBManager()

        # 如果可以覆盖保存，那么就显示成功
        if(db.save_record(req)):
            success_info = {"status":"OK","detail":"Save Success!"}
            db.close()
            self.write(json.dumps(success_info, ensure_ascii=False, encoding="utf-8"))
        # 如果不可以覆盖保存，那么就显示错误信息
        else:
            db.close()
            err_info = {"status":"FAILED","detail":"Save Failed!"}
            self.write(json.dumps(err_info, ensure_ascii=False, encoding="utf-8"))