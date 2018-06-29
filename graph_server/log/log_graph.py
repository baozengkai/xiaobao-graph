# -*- coding:utf-8 -*-
import os
import logging
import logging.handlers


class My_Logger:
    def __init__(self, Clevel = logging.DEBUG, Flevel = logging.DEBUG):
        self.logger = logging.getLogger("GraphServer")
        self.logger.setLevel(logging.DEBUG)

       # logpath = os.path.join(
       #     os.path.dirname(os.path.dirname(os.path.realpath(__file__))),
       #     "log")

        if not self.logger.handlers:
            fmt = logging.Formatter('[%(asctime)s] [%(levelname)s] %(message)s', '%Y-%m-%d %H:%M:%S')
            # 设置文件日志
            fh = logging.handlers.RotatingFileHandler("/anyrobot/logs/graph/graph_server.log",'a',1000000,3)
            fh.setFormatter(fmt)
            fh.setLevel(Flevel)
            self.logger.addHandler(fh)

    def debug(self, message):
        self.logger.debug(message)

    def info(self, message):
        self.logger.info(message)

    def warn(self, message):
        self.logger.warn(message)

    def error(self, message):
        self.logger.error(message)
