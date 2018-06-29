#!/usr/bin/env python
# -*- coding:utf-8 -*-
import simplejson as json
import tornado.web
import time
from log.log_graph import My_Logger
from modules.graph.graph_db_manager import GraphDBManager

class GraphDeleteHandler(tornado.web.RequestHandler):
    """
    删除类
    """

    def post(self):
        """
        处理关系图谱删除的post请求
        """
        log = My_Logger()
        log.info("[" + time.strftime('%Y-%m-%d %H:%M:%S',time.localtime(time.time())) + "]" + " Handler For Graph Delete......")

        req = json.loads(self.request.body.decode('utf-8'))
        db = GraphDBManager()

        # 如果该记录存在，那么就调用delete_record()来删除该记录内容
        if(db.is_exists_record(req)):
            if(db.delete_record(req)):
                response_info = {"status":"OK", "detail":"Delete %s record success!" %(req["graphName"])}
            else:
                response_info = {"status":"FAILED","detail":"Delete Failed. Some internal error have happen in graph_server!"}
            db.close()
            self.write(json.dumps(response_info, ensure_ascii=False, encoding="utf-8"))
        # 如果记录不存在，那么返回错误信息，表明该记录不存在
        else:
            db.close()
            err_info = {"status":"FAILED","detail":"Delete Failed.The %s record does not esists."% (req["graphName"])}
            self.write(json.dumps(err_info, ensure_ascii=False, encoding="utf-8"))