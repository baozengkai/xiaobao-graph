#!/usr/bin/env python
# -*- coding:utf-8 -*-
import pymysql
import simplejson as json
import warnings
from log.log_graph import My_Logger
from utils.global_info import (MYSQL_IP,
                               MYSQL_PORT
                               )

class GraphDBManager():
    """
    关系网络持久化类
    """
    # 初始化 连接数据库并创建事件表和关系图谱表(如果不存在的话)
    def __init__(self):
        warnings.filterwarnings("ignore")

        self.db = pymysql.Connect(host=MYSQL_IP, port=MYSQL_PORT, user="root", passwd="eisoo.com", db="AnyRobot",
                                  charset="utf8")
        # self.db = pymysql.Connect(host="192.168.84.150",port=3306,user="root",passwd="123456",db="BAOZENGKAI",charset="utf8")
        self.cursor = self.db.cursor()
        self.log = My_Logger()


        self.sql_graph = """CREATE TABLE IF NOT EXISTS graph(
            id INT NOT NULL AUTO_INCREMENT,
            graph_name VARCHAR(30) NOT NULL,
            event_name VARCHAR(30) NOT NULL,
            json_value JSON NOT NULL,
            PRIMARY  KEY(id))ENGINE=InnoDB DEFAULT CHARSET=utf8"""
        try:
            self.cursor.execute(self.sql_graph)
        except Exception as e:
            print(e.with_traceback())

    def is_exists_record(self, req):
        sql = "SELECT * FROM graph WHERE graph_name='%s'" % (req["graphName"])
        self.cursor.execute(sql)
        data = self.cursor.fetchall()
        if(len(data) != 0):
            return True
        return False

    def save_record(self, req):
        try:
            sql = """
                  INSERT INTO graph
                  (graph_name,event_name,json_value)
                  VALUES
                  ('%s','%s','%s')
                  """ % (req["graphName"],req["eventName"],json.dumps(req["value"],ensure_ascii=False, encoding="utf-8"))
            self.cursor.execute(sql)
            self.db.commit()
            return True
        except Exception as e:
            self.db.rollback()
            self.log.info(" Save Failed! The operation has rollback,please check your sql syntax......")
            print(e.with_traceback())
            return False

    def replace_record(self, req):
        try:
            sql = """
                  UPDATE graph 
                  SET
                  graph_name = '%s', event_name = '%s', json_value = '%s'
                  WHERE graph_name='%s'
                  """ % (req["newGraphName"],req["eventName"],json.dumps(req["value"],ensure_ascii=False, encoding="utf-8"),req["oldGraphName"])
            self.cursor.execute(sql)
            self.db.commit()
            return True
        except Exception as e:
            self.db.rollback()
            self.log.info(" Replace Failed! The operation has rollback,please check your sql syntax......")
            print(e.with_traceback())
            return False

    def load_record(self, req):
        """
        加载关系图谱
        """
        sql = "SELECT event_name,json_value FROM graph WHERE graph_name='%s'" % (req["graphName"])
        self.cursor.execute(sql)
        data = self.cursor.fetchall()
        json_value = {"status":"OK","detail":"Load Success!","eventName":data[0][0],"value":json.loads(data[0][1])}
        return json_value

    def list(self):
        """
        加载关系图谱所有的关系图谱标识
        """
        sql = "SELECT graph_name FROM graph"
        self.cursor.execute(sql)
        data = self.cursor.fetchall()
        graph_names = {"graphNames":data}
        return graph_names

    def delete_record(self, req):
        """
        删除事件或关系图谱
        """
        try:
            sql = "DELETE FROM graph WHERE graph_name = '%s'" % (req["graphName"])
            self.cursor.execute(sql)
            self.db.commit()
            return 1
        except:
            self.db.rollback()
            self.log.info(" Delete Failed! The operation has rollback,please check your sql syntax......")
            return 0

    def close(self):
        """
        关闭数据库
        """
        self.db.close()